package com.idega.ascertia.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

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
import com.idega.presentation.ui.SelectOption;
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
public class AscertiaSigner extends Block {
	
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.ascertia";
	private IWBundle bundle;
	private Web2Business web2 = null;
	private String goSignJSFile;
	private String goSignRootRootURI;
	
	private String targetURL;
	
	private static final String formName = "signingForm1";
	private static final String PARAMETER_ACTION = "signing_action";
	private static final int PARAMETER_SHOW_UNSIGNED_PDF = 1;
	private static final int PARAMETER_SHOW_SIGNED_PDF = 2;
	private static final String PARAMETER_ERROR_REASON = "errorReason";
	
	public AscertiaSigner() {
		super();
	}
	
	public void main(IWContext iwc) throws RemoteException {
		
		web2 = ELUtil.getInstance().getBean(Web2Business.class);
		bundle = this.getBundle(iwc);
		goSignRootRootURI = bundle
		        .getVirtualPathWithFileNameString("javascript/GoSign");
		goSignJSFile = goSignRootRootURI + "/lib/gosign.js";
		
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
		
		String fileName;
		String serverURL = iwc.getServerURL();
		serverURL = (serverURL.endsWith("/")) ? serverURL.substring(0,
		    serverURL.length() - 1) : serverURL;
		String documentURL;
		if (iwc.getParameter(AscertiaConstants.UNSIGNED_DOCUMENT_URL) != null
		        && !iwc.getParameter(AscertiaConstants.UNSIGNED_DOCUMENT_URL)
		                .equals("")
		        // sometimes we get a null string
		        && !iwc.getParameter(AscertiaConstants.UNSIGNED_DOCUMENT_URL)
		                .equals("null")) {
			
			documentURL = serverURL + CoreConstants.WEBDAV_SERVLET_URI
			        + CoreConstants.SLASH
			        + iwc.getParameter(AscertiaConstants.UNSIGNED_DOCUMENT_URL);
			
			fileName = documentURL;
			
			while (fileName.indexOf("/") != -1) {
				fileName = fileName.substring(fileName.indexOf("/") + 1);
			}
			iwc.setApplicationAttribute(AscertiaConstants.SIGNED_DOCUMENT_URL,
			    fileName);
			
		} else {
			documentURL = null;
		}
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
		
		// Frame for document to be signed
		IFrame documentFrame = new IFrame("document_view_frame", iwc
		        .getIWMainApplication().getMediaServletURI()
		        + "?"
		        + MediaWritable.PRM_WRITABLE_CLASS
		        + "="
		        + IWMainApplication
		                .getEncryptedClassName(AscertiaPDFPrinter.class)
		        + "&"
		        + AscertiaConstants.PARAM_TASK_ID
		        + "="
		        + iwc.getParameter(AscertiaConstants.PARAM_TASK_ID)
		        + "&"
		        + AscertiaConstants.PARAM_VARIABLE_HASH
		        + "="
		        + iwc.getParameter(AscertiaConstants.PARAM_VARIABLE_HASH));
		documentFrame.setStyleClass("pdf_frame");
		mainDiv.add(documentFrame);
		
		Layer signingLayer = new Layer();
		signingLayer.add(createForm(iwc, documentURL));
		signingLayer.setStyleClass("under_frame_layer");
		mainDiv.add(signingLayer);
		
		Script script = new Script();
		script.addScriptLine("embedAscertiaApplet('" + serverURL
		        + goSignRootRootURI + "','" + formName + "','"
		        + getLocalizedString("note_message", "", iwc) + "');");
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
	
	protected Form createForm(IWContext iwc, String documentURL)
	        throws RemoteException {
		Form form = new Form();
		form.setId(formName);
		form.setMarkupAttribute("name", formName);
		
		addFormParams(iwc, form);
		return form;
	}
	
	private void addSigningOptions(Form form, IWContext iwc) {
		
		String avalableSignaturePlaces = iwc
		        .getParameter(AscertiaConstants.PARAM_SIGNATURE_PLACES_USED);
		DropdownMenu signaturePlace = new DropdownMenu();
		signaturePlace.setId(AscertiaConstants.PARAM_SELECTED_SIGNATURE_PLACE);
		signaturePlace
		        .setName(AscertiaConstants.PARAM_SELECTED_SIGNATURE_PLACE);
		
		StringTokenizer stringTokenizer = new StringTokenizer(
		        avalableSignaturePlaces, ";");
		
		while (stringTokenizer.hasMoreElements()) {
			String menuElement = stringTokenizer.nextToken();
			signaturePlace.addMenuElement(menuElement, getLocalizedString(
			    menuElement, menuElement, iwc));
		}
		if (signaturePlace.getOptions().size() < 2) {
			HiddenInput hiddenInput = new HiddenInput();
			hiddenInput.setID(AscertiaConstants.PARAM_SELECTED_SIGNATURE_PLACE);
			hiddenInput
			        .setName(AscertiaConstants.PARAM_SELECTED_SIGNATURE_PLACE);
			hiddenInput.setValue(((SelectOption) signaturePlace.getOptions()
			        .iterator().next()).getValueAsString());
			form.add(hiddenInput);
		}else{
			form.add(signaturePlace);
		}
		DropdownMenu certificates = new DropdownMenu();
		certificates.setId("GoSignCertificateList");
		certificates.setName("GoSignCertificateList");
		form.add(certificates);
	}
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	
	protected void addFormParams(IWContext iwc, Form form)
	        throws RemoteException {
		
		List<AdvancedProperty> paramList = new ArrayList<AdvancedProperty>();
		
		@SuppressWarnings("unchecked")
		Enumeration<String> paramNames = iwc.getParameterNames();
		
		addSigningOptions(form, iwc);
		
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			
			HiddenInput hiddenInput = new HiddenInput();
			hiddenInput.setID(paramName);
			hiddenInput.setName(paramName);
			hiddenInput.setValue(iwc.getParameter(paramName));
			form.add(hiddenInput);
			
			paramList.add(new AdvancedProperty(paramName, iwc
			        .getParameter(paramName)));
		}
		
		paramList.add(new AdvancedProperty(PARAMETER_ACTION, String
		        .valueOf(PARAMETER_SHOW_UNSIGNED_PDF)));
		BuilderService builderService = BuilderServiceFactory
		        .getBuilderService(iwc);
		
		String successPath = builderService.getUriToObject(
		    AscertiaSigner.class, Arrays
		            .asList(new AdvancedProperty(PARAMETER_ACTION, String
		                    .valueOf(PARAMETER_SHOW_SIGNED_PDF))));
		
		String errorPath = builderService.getUriToObject(AscertiaSigner.class,
		    paramList);
		
		Link sign = new Link(getLocalizedString("sign_document",
		    "Sign document", iwc));
		sign.setURL("javascript:void(0)");
		sign.setOnClick("signDocument('" + successPath + "','" + errorPath
		        + "','" + getLocalizedString("signing", "Signing...", iwc)
		        + "');");
		form.add(sign);
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
}
