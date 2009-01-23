package com.idega.ascertia.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.idega.ascertia.AscertiaConstants;
import com.idega.ascertia.AscertiaPDFPrinter;
import com.idega.ascertia.AscertiaPDFWriter;
import com.idega.block.web2.business.Web2Business;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.io.MediaWritable;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Script;
import com.idega.presentation.text.DownloadLink;
import com.idega.presentation.text.Heading5;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.IFrame;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

//TODO: refactoring, localization, styleclasses etc...

/**
 * A Block for signing documents (pdf) See <a href="http://www.ascertia.com">www.ascertia.com</a>
 * for details on the GoSign product.<br>
 * 
 * @author <a href="mailto:juozas@idega.com">Juozas</a>
 * @version 1.0
 */
public class AscertiaBPMSigner extends Block {
	
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.ascertia";
	private IWBundle bundle;
	private Web2Business web2 = null;
	private String goSignJSFile;
	private String goSignRootRootURI;
	
	private String targetURL;
	private String formName;
	// private String fileName;
	private String filePath;
	
	private static final String PARAMETER_ACTION = "signing_action";
	private static final int PARAMETER_SHOW_UNSIGNED_PDF = 1;
	private static final int PARAMETER_SHOW_SIGNED_PDF = 2;
	private static final String PARAMETER_ERROR_REASON = "errorReason";
	
	public AscertiaBPMSigner() {
		super();
	}
	
	public String getFormName() {
		return formName;
	}
	
	public void setFormName(String formName) {
		this.formName = formName;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public void main(IWContext iwc) throws RemoteException {
		
		web2 = ELUtil.getInstance().getBean(Web2Business.class);
		bundle = this.getBundle(iwc);
		goSignRootRootURI = bundle
		        .getVirtualPathWithFileNameString("javascript/GoSign");
		goSignJSFile = goSignRootRootURI + "/lib/gosign.js";
		setFormName("signingForm1");
		
		switch (parseAction(iwc)) {
			case PARAMETER_SHOW_UNSIGNED_PDF:
				showSigningForm(iwc);
				break;
			
			case PARAMETER_SHOW_SIGNED_PDF:
				showSignedPdf(iwc);
				break;
		}
		
	}
	
	private int parseAction(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		}
		return PARAMETER_SHOW_UNSIGNED_PDF;
	}
	
	public void showSigningForm(IWContext iwc) throws RemoteException {
		
		String serverURL = iwc.getServerURL();
		serverURL = (serverURL.endsWith("/")) ? serverURL.substring(0,
		    serverURL.length() - 1) : serverURL;
		String documentURL = iwc
		        .getParameter(AscertiaConstants.UNSIGNED_DOCUMENT_URL);
		
		targetURL = serverURL + "/sign/signer";
		
		Layer mainDiv = new Layer();
		
		// if error was when trying to sign, show error message
		if (iwc.getParameter(PARAMETER_ERROR_REASON) != null) {
			Layer errorDiv = new Layer();
			errorDiv.add(new Text(iwc.getParameter(PARAMETER_ERROR_REASON)));
			mainDiv.add(errorDiv);
			
		}
		
		// Adding js
		mainDiv.add(PresentationUtil.getJavaScriptSourceLine(iwc
		        .getIWMainApplication().getBundle(
		            CoreConstants.CORE_IW_BUNDLE_IDENTIFIER)
		        .getVirtualPathWithFileNameString("iw_core.js")));
		mainDiv.add(PresentationUtil.getJavaScriptSourceLine(web2
		        .getBundleURIToMootoolsLib()));
		mainDiv.add(PresentationUtil.getJavaScriptSourceLine(goSignJSFile));
		mainDiv
		        .add(PresentationUtil
		                .getJavaScriptSourceLine(bundle
		                        .getResourceURIWithoutContextPath("/javascript/AscertiaHelper.js")));
		mainDiv
		        .add(PresentationUtil
		                .getCssLine(
		                    bundle
		                            .getResourceURIWithoutContextPath("/style/ascertia.css"),
		                    true));
		mainDiv.add(PresentationUtil.getCssLine(iwc.getIWMainApplication()
		        .getBundle(CoreConstants.CORE_IW_BUNDLE_IDENTIFIER)
		        .getVirtualPathWithFileNameString("/style/iw_core.css"), true));
		
		IFrame documentFrame = new IFrame("document_view_frame", documentURL);
		documentFrame.addLanguageParameter(false);
		documentFrame.setStyleClass("pdf_frame");
		mainDiv.add(documentFrame);
		
		Map<String, String> formParams = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Enumeration<String> paramNames = iwc.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			formParams.put(paramName, iwc.getParameter(paramName));
		}
		formParams.put(AscertiaConstants.PARAM_LOCALIZED_FILE_PREFIX, iwc
		        .getIWMainApplication().getBundle("com.idega.ascertia")
		        .getResourceBundle(iwc).getLocalizedString("signed", "Signed"));
		
		Layer signingLayer = new Layer();
		signingLayer.add(addForm(iwc, formParams));
		signingLayer.setStyleClass("under_frame_layer");
		mainDiv.add(signingLayer);
		
		Script script = new Script();
		
		script.addScriptLine("embedAscertiaApplet('" + serverURL
		        + goSignRootRootURI + "','" + formName + "');");
		script.addScriptLine("GoSign_SetTargetURL('" + targetURL + "');");
		
		BuilderService builderService = BuilderServiceFactory
		        .getBuilderService(iwc);
		
		String successPath = builderService.getUriToObject(
		    AscertiaSigningForm.class, Arrays
		            .asList(new AdvancedProperty[] { new AdvancedProperty(
		                    PARAMETER_ACTION, String
		                            .valueOf(PARAMETER_SHOW_SIGNED_PDF)) }));
		
		script.addScriptLine("GoSign_SetResultPage('" + successPath + "');");
		mainDiv.add(script);
		
		add(mainDiv);
	}
	
	protected Form addForm(IWContext iwc, Map<String, String> paramsMap)
	        throws RemoteException {
		Form form = new Form();
		form.setId(getFormName());
		form.setMarkupAttribute("name", getFormName());
		addFormParams(iwc, form, paramsMap);
		return form;
	}
	
	private void addCertificatesList(Form form) {
		DropdownMenu certificates = new DropdownMenu();
		certificates.setId("GoSignCertificateList");
		certificates.setName("GoSignCertificateList");
		form.add(certificates);
	}
	
	protected void showSignedPdf(IWContext iwc) throws RemoteException {
		
		String serverURL = iwc.getServerURL();
		serverURL = (serverURL.endsWith("/")) ? serverURL.substring(0,
		    serverURL.length() - 1) : serverURL;
		Script script = new Script();
		Layer mainDiv = new Layer();
		
		mainDiv.add(PresentationUtil.getJavaScriptSourceLine(iwc
		        .getIWMainApplication().getBundle(
		            CoreConstants.CORE_IW_BUNDLE_IDENTIFIER)
		        .getVirtualPathWithFileNameString("iw_core.js")));
		
		mainDiv
		        .add(PresentationUtil
		                .getCssLine(
		                    bundle
		                            .getResourceURIWithoutContextPath("/style/ascertia.css"),
		                    true));
		
		mainDiv.add(script);
		
		mainDiv.setStyleClass("main_layer");
		
		Layer headerDiv = new Layer();
		headerDiv.setStyleClass("signed_success_message_layer");
		headerDiv.add(new Heading5(getLocalizedString("signed_successfully",
		    "Document signed successfully", iwc)));
		
		mainDiv.add(headerDiv);
		
		IFrame frame = new IFrame("signedDocument", iwc.getIWMainApplication()
		        .getMediaServletURI()
		        + "?"
		        + MediaWritable.PRM_WRITABLE_CLASS
		        + "="
		        + IWMainApplication
		                .getEncryptedClassName(AscertiaPDFPrinter.class));
		frame.setStyleClass("pdf_frame");
		mainDiv.add(frame);
		
		Layer downLoadDiv = new Layer();
		downLoadDiv.setStyleClass("under_frame_layer");
		
		DownloadLink pdfLink = new DownloadLink(getLocalizedString("download",
		    "Download", iwc));
		pdfLink.setMediaWriterClass(AscertiaPDFWriter.class);
		
		downLoadDiv.add(pdfLink);
		
		mainDiv.add(downLoadDiv);
		
		add(mainDiv);
	}
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	
	protected void addFormParams(IWContext iwc, Form form,
	        Map<String, String> paramsMap) throws RemoteException {
		
		List<AdvancedProperty> paramList = new ArrayList<AdvancedProperty>();
		
		for (String key : paramsMap.keySet()) {
			HiddenInput hiddenInput = new HiddenInput();
			hiddenInput.setID(key);
			hiddenInput.setName(key);
			hiddenInput.setValue(paramsMap.get(key));
			form.add(hiddenInput);
			
			paramList.add(new AdvancedProperty(key, paramsMap.get(key)));
			
		}
		
		paramList.add(new AdvancedProperty(PARAMETER_ACTION, String
		        .valueOf(PARAMETER_SHOW_UNSIGNED_PDF)));
		BuilderService builderService = BuilderServiceFactory
		        .getBuilderService(iwc);
		
		String successPath = builderService.getUriToObject(
		    AscertiaBPMSigner.class, Arrays
		            .asList(new AdvancedProperty(PARAMETER_ACTION, String
		                    .valueOf(PARAMETER_SHOW_SIGNED_PDF))));
		
		String errorPath = builderService.getUriToObject(
		    AscertiaBPMSigner.class, paramList);
		
		addCertificatesList(form);
		Link sign = new Link(
		        /*getLocalizedString("sign_document", "Sign document", iwc)*/"Sign document");
		sign.setURL("javascript:void(0)");
		sign
		        .setOnClick("signDocument('"
		                + successPath
		                + "','"
		                + errorPath
		                + "','"
		                + /*getLocalizedString("signing", "Signing...", iwc)*/"Signing..."
		                + "');");
		form.add(sign);
	}
	
}
