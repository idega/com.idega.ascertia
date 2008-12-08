package com.idega.ascertia.presentation;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.idega.ascertia.AscertiaConstants;
import com.idega.bpm.jsfcomponentview.BPMCapableJSFComponent;
import com.idega.bpm.jsfcomponentview.JSFComponentView;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.variables.BinaryVariable;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.IFrame;
import com.idega.util.CoreConstants;

public class AscertiaBMPCapableSigningForm extends IWBaseComponent implements BPMCapableJSFComponent {

	private static final String SINGNING_FRAME = "signing_frame";
	
	private JSFComponentView view;
	
	public AscertiaBMPCapableSigningForm() {
		super();
	}
	
	@Override
	protected void initializeComponent(FacesContext context) {
		
		super.initializeComponent(context);
		initializeJSFView(context);
		
	}
	
	
	protected void initializeJSFView(FacesContext context){
		IWApplicationContext iwac = IWMainApplication.getIWMainApplication(context).getIWApplicationContext();
		
		Layer div = new Layer();
		div.setWidth("100%");
		div.setHeight("800");
		BuilderService builderService = null;
		try{
			builderService = BuilderServiceFactory.getBuilderService(iwac);
		}catch (Exception e) {
			// TODO: handle exception
			
			e.printStackTrace();
			
			return;
		}
		
		String pathToSigner = null;
		if(view.isSubmitable()){
		
		String serverURL = IWContext.getIWContext(context).getServerURL();
		serverURL = (serverURL.endsWith("/")) ? serverURL.substring(0, serverURL.length() - 1) : serverURL;
		
		String pathToDocument = serverURL +  CoreConstants.WEBDAV_SERVLET_URI + CoreConstants.SLASH + IWMainApplication.getDefaultIWMainApplication().getSettings()
			.getProperty(AscertiaConstants.APP_PROP_PATH_TO_DOCUMENT_TO_SIGN,"/rulling/");
		
		Locale currentLocale = IWContext.getIWContext(context).getCurrentLocale();
		pathToDocument += IWMainApplication.getDefaultIWMainApplication().getSettings()
			.getProperty(AscertiaConstants.APP_PROP_DOCUMENT_NAME,"rulling_aggrement");
		pathToDocument += "_" + currentLocale.getLanguage() + ".pdf";
		
		
		
		pathToSigner = builderService.getUriToObject(AscertiaBPMSigner.class,
			Arrays.asList(new AdvancedProperty[] {
					new AdvancedProperty(
							AscertiaConstants.UNSIGNED_DOCUMENT_URL, pathToDocument),
					new AdvancedProperty(
							AscertiaConstants.PARAM_TASK_ID, String.valueOf(view.getTaskInstanceId()))}));
		
		
		IFrame frame = new IFrame(SINGNING_FRAME, pathToSigner);
		frame.setWidth("100%");
		frame.setHeight("100%");
		
		div.add(frame);
		
		}else{
			div.add(new Text(iwac.getIWMainApplication().getLocalisedStringMessage(
				"see_attachement", "See attachment for signed document", "com.idega.ascertia", 
				IWContext.getIWContext(context).getCurrentLocale())));
		}
		add(div);
	}
	
	public String getDefaultDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDisplayName(Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

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
		this.view = (JSFComponentView)values[1];
	}

}
