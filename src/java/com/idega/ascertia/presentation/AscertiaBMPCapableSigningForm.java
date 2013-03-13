package com.idega.ascertia.presentation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.ascertia.AscertiaConstants;
import com.idega.ascertia.AscertiaData;
import com.idega.ascertia.AscertiaPDFPrinter;
import com.idega.block.process.variables.Variable;
import com.idega.bpm.jsfcomponentview.BPMCapableJSFComponent;
import com.idega.bpm.jsfcomponentview.JSFComponentView;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.idegaweb.IWMainApplication;
import com.idega.io.MediaWritable;
import com.idega.jbpm.exe.BPMFactory;
import com.idega.jbpm.exe.TaskInstanceW;
import com.idega.jbpm.variables.BinaryVariable;
import com.idega.jbpm.variables.VariablesHandler;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.IFrame;
import com.idega.util.CoreConstants;
import com.idega.util.FileUtil;
import com.idega.util.expression.ELUtil;

public class AscertiaBMPCapableSigningForm extends IWBaseComponent implements BPMCapableJSFComponent {

	private static final String SINGNING_FRAME = "signing_frame";

	private JSFComponentView view;

	@Autowired
	private VariablesHandler variablesHandler;

	@Autowired
	private BPMFactory bpmFactory;

	public AscertiaBMPCapableSigningForm() {
		super();
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		initializeJSFView(context);
	}

	protected void initializeJSFView(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);

		Layer div = new Layer();
		div.setWidth("100%");
		div.setHeight("800");
		BuilderService builderService = null;
		try {
			builderService = BuilderServiceFactory.getBuilderService(iwc);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		String pathToSigner = null;
		if (view.isSubmitable()) {
			String serverURL = iwc.getServerURL();
			serverURL = (serverURL.endsWith(CoreConstants.SLASH)) ? serverURL.substring(0, serverURL.length() - 1) : serverURL;

			String pathToDocument = serverURL
			        + CoreConstants.WEBDAV_SERVLET_URI
			        + CoreConstants.SLASH
			        + iwc.getApplicationSettings().getProperty(AscertiaConstants.APP_PROP_PATH_TO_DOCUMENT_TO_SIGN, "/rulling/");

			Locale currentLocale = iwc.getCurrentLocale();
			pathToDocument += iwc.getApplicationSettings().getProperty(AscertiaConstants.APP_PROP_DOCUMENT_NAME, "rulling_aggrement");
			pathToDocument += "_" + currentLocale.getLanguage() + ".pdf";

			pathToSigner = builderService.getUriToObject(AscertiaBPMSigner.class, Arrays.asList(
					new AdvancedProperty(AscertiaConstants.UNSIGNED_DOCUMENT_URL, pathToDocument),
			        new AdvancedProperty(AscertiaConstants.PARAM_TASK_ID, String.valueOf(view.getTaskInstanceId())),
			        new AdvancedProperty(AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE, AscertiaConstants.PROP_EMPTY_ONE_SIGNATURE_PROFILE),
			        new AdvancedProperty(AscertiaConstants.PARAM_SELECTED_SIGNATURE_PLACE, AscertiaConstants.PROP_ONE_SIGNATURE_PROFILE_PLACE)));

			IFrame frame = new IFrame(SINGNING_FRAME, pathToSigner);
			frame.setWidth("100%");
			frame.setHeight("100%");

			div.add(frame);
		} else {
			TaskInstanceW taskInstanceW = getBpmFactory()
			        .getProcessManagerByTaskInstanceId(view.getTaskInstanceId())
			        .getTaskInstance(view.getTaskInstanceId());

			Variable signedVar = Variable.parseDefaultStringRepresentation(AscertiaConstants.SIGNED_VARIABLE_NAME);
			BinaryVariable signedDocument = taskInstanceW.getAttachments(signedVar).iterator().next();

			VariablesHandler variablesHandler = getVariablesHandler();
			InputStream inputStream = variablesHandler.getBinaryVariablesHandler().getBinaryVariableContent(signedDocument);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			byte buffer[] = new byte[1024];
//			int noRead = 0;
//			try {
//				noRead = inputStream.read(buffer, 0, 1024);
//				while (noRead != -1) {
//					baos.write(buffer, 0, noRead);
//					noRead = inputStream.read(buffer, 0, 1024);
//				}
//			} catch (IOException e) {
//				inputStream = null;
//				return;
//			}
			try {
				FileUtil.streamToOutputStream(inputStream, baos);
			} catch (IOException e) {
				e.printStackTrace();
			}
			AscertiaData data = new AscertiaData();
			data.setDocumentName(signedDocument.getFileName());
			data.setByteDocument(baos.toByteArray());

			iwc.getSession().setAttribute(AscertiaConstants.PARAM_ASCERTIA_DATA, data);
			IFrame frame = new IFrame("signedDocument", iwc.getIWMainApplication().getMediaServletURI()
			        + "?"
			        + MediaWritable.PRM_WRITABLE_CLASS
			        + "="
			        + IWMainApplication.getEncryptedClassName(AscertiaPDFPrinter.class));
			frame.setWidth("100%");
			frame.setHeight("100%");
			div.add(frame);
		}
		add(div);
	}

	@Override
	public String getDefaultDisplayName() {
		return IWMainApplication.getDefaultIWMainApplication().getLocalizedStringMessage("sign_document", "Sign document", "com.idega.ascertia");
	}

	@Override
	public String getDisplayName(Locale locale) {
		return IWMainApplication.getDefaultIWMainApplication().getLocalizedStringMessage("sign_document", "Sign document", "com.idega.ascertia", locale);
	}

	@Override
	public void setView(JSFComponentView view) {
		this.view = view;
	}

	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[4];
		values[0] = super.saveState(ctx);
		values[1] = this.view;
		return values;
	}

	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.view = (JSFComponentView) values[1];
	}

	public VariablesHandler getVariablesHandler() {
		if (variablesHandler == null) {
			ELUtil.getInstance().autowire(this);
		}
		return variablesHandler;
	}

	public void setVariablesHandler(VariablesHandler variablesHandler) {
		this.variablesHandler = variablesHandler;
	}

	public BPMFactory getBpmFactory() {
		if (bpmFactory == null) {
			ELUtil.getInstance().autowire(this);
		}
		return bpmFactory;
	}

	public void setBpmFactory(BPMFactory bpmFactory) {
		this.bpmFactory = bpmFactory;
	}

}
