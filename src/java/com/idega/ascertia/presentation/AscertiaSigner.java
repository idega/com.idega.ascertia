package com.idega.ascertia.presentation;

import java.rmi.RemoteException;

import org.apache.myfaces.renderkit.html.util.AddResource;

import com.idega.ascertia.AscertiaConstants;
import com.idega.block.pdf.PDFWriter;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Script;
import com.idega.presentation.text.DownloadLink;
import com.idega.presentation.text.Heading5;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.IFrame;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;


//TODO: refactoring, localization, styleclasses etc...

/**
 * A Block for signing documents (pdf) See <a href="http://www.ascertia.com">www.ascertia.com</a> for details on the GoSign product.<br>
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
	private String ADSS_signing_profile;
	private String ADSS_epty_siganture_profile;
	private String formName;
	private String fileName;
	private String filePath;

	private static final String PARAMETER_ACTION = "signing_action_parameter";
	private static final int PARAMETER_SHOW_UNSIGNED_PDF = 1;
	private static final int PARAMETER_SHOW_SIGNED_PDF = 2;

	public AscertiaSigner() {
		super();
	}

	public String getTargetURL() {
		return targetURL;
	}

	public void setTargetURL(String targetURL) {
		this.targetURL = targetURL;
	}

	public String getADSS_signing_profile() {
		return ADSS_signing_profile;
	}

	public void setADSS_signing_profile(String adss_signing_profile) {
		ADSS_signing_profile = adss_signing_profile;
	}

	public String getADSS_epty_siganture_profile() {
		return ADSS_epty_siganture_profile;
	}

	public void setADSS_epty_siganture_profile(String adss_epty_siganture_profile) {
		ADSS_epty_siganture_profile = adss_epty_siganture_profile;
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

		// AddResource resourceAdder = AddResourceFactory.getInstance(iwc);

		web2 = ELUtil.getInstance().getBean(Web2Business.class);
		bundle = this.getBundle(iwc);
		goSignRootRootURI = bundle.getVirtualPathWithFileNameString("javascript/GoSign");
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

		BuilderService builder = (BuilderService) IBOLookup.getServiceInstance(iwc, BuilderService.class);

		String serverURL = iwc.getServerURL();
		serverURL = (serverURL.endsWith("/")) ? serverURL.substring(0, serverURL.length() - 1) : serverURL;

		// TODO get dynamically
		
		String documentURL = serverURL + CoreConstants.WEBDAV_SERVLET_URI + CoreConstants.SLASH + iwc.getParameter(AscertiaConstants.UNSIGNED_DOCUMENT_URL);

		targetURL = serverURL + "/sign/signer";

		fileName = documentURL;
		while (fileName.indexOf("/") != -1) {
			fileName = fileName.substring(fileName.indexOf("/") + 1);
		}
		iwc.setApplicationAttribute(AscertiaConstants.SIGNED_DOCUMENT_URL, fileName);

		Layer mainDiv = new Layer();

		mainDiv.add(PresentationUtil.getJavaScriptSourceLine(web2.getBundleURIToMootoolsLib()));
		mainDiv.add(PresentationUtil.getJavaScriptSourceLine(bundle.getResourceURIWithoutContextPath("/javascript/AscertiaHelper.js")));
		mainDiv.add(PresentationUtil.getJavaScriptSourceLine(goSignJSFile));

		IFrame documentFrame = new IFrame("document_view_frame", documentURL);
		documentFrame.setWidth("100%");
		documentFrame.setHeight(400);
		mainDiv.add(documentFrame);
		
		Layer signingLayer = new Layer();
		signingLayer.add(addForm(iwc, documentURL));
		signingLayer.setStyleAttribute("text-align", "center");
		signingLayer.setStyleAttribute("padding-top", "10px");
		mainDiv.add(signingLayer);

		Script script = new Script();

		script.addScriptLine("embedAscertiaApplet('" + serverURL + goSignRootRootURI + "','" + formName + "');");
		script.addScriptLine("GoSign_SetTargetURL('" + targetURL + "');");
		script.addScriptLine("GoSign_SetResultPage('" + builder.getCurrentPageURI(iwc) + "');");

		mainDiv.add(script);

		add(mainDiv);
	}

	protected void showSignedPdf(IWContext iwc) {

		String serverURL = iwc.getServerURL();
		serverURL = (serverURL.endsWith("/")) ? serverURL.substring(0, serverURL.length() - 1) : serverURL;

		String documentURL = serverURL + CoreConstants.WEBDAV_SERVLET_URI + CoreConstants.CONTENT_PATH + CoreConstants.SLASH +"xforms/pdf/signed/" + iwc.getApplicationAttribute(AscertiaConstants.SIGNED_DOCUMENT_URL);// iwc.getParameter(AscertiaConstants.UNSINGNED_DOCUMENT_URL);

		Layer mainDiv = new Layer();
		mainDiv.setWidth("100%");
		mainDiv.setHeight("95%");
		
		Layer headerDiv = new Layer();
		headerDiv.setStyleAttribute("text-align", "center");
		headerDiv.setStyleAttribute("padding-bottom", "5px");
		//TODO: localization
		headerDiv.add(new Heading5("Document signed successfully"));
		
		mainDiv.add(headerDiv);
		
		IFrame frame = new IFrame("signedDocument", documentURL);
		frame.setWidth("100%");
		frame.setHeight("80%");
		mainDiv.add(frame);
		
		Layer downLoadDiv = new Layer();
		downLoadDiv.setStyleAttribute("padding-top", "5px");
		downLoadDiv.setStyleAttribute("text-align", "center");
				
		
		DownloadLink pdfLink = new DownloadLink("Download");
		//pdfLink.setStyleClass(CasesEngine.PDF_GENERATOR_AND_DOWNLOAD_LINK_STYLE_CLASS);
		pdfLink.setMediaWriterClass(PDFWriter.class);
		pdfLink.addParameter(PDFWriter.PDF_URL_PARAMETER, CoreConstants.WEBDAV_SERVLET_URI + CoreConstants.CONTENT_PATH + CoreConstants.SLASH +"xforms/pdf/signed/" + iwc.getApplicationAttribute(AscertiaConstants.SIGNED_DOCUMENT_URL));
		
		downLoadDiv.add(pdfLink);
		
		
		
		
		mainDiv.add(downLoadDiv);
		
		
		
		
		
		add(mainDiv);
	}

	protected Form addForm(IWContext iwc, String documentURL) {
		Form form = new Form();
		form.setName(getFormName());
		form.setMarkupAttribute("name", getFormName());
		HiddenInput hiddenInputProfile = new HiddenInput();
		hiddenInputProfile.setName(AscertiaConstants.DOCUMENT_URL_ID);
		hiddenInputProfile.setId(AscertiaConstants.DOCUMENT_URL_ID);

		hiddenInputProfile.setValue(documentURL);
		form.add(hiddenInputProfile);

		DropdownMenu certificates = new DropdownMenu();
		certificates.setId("GoSignCertificateList");
		certificates.setName("GoSignCertificateList");

		Link sign = new Link("Sign document");
		sign.setOnClick("signDocument()");
		sign.addParameter(PARAMETER_ACTION, PARAMETER_SHOW_SIGNED_PDF);

		form.add(certificates);
		form.add(sign);

		return form;
	}

	protected void addJS(IWContext iwc, AddResource resourceAdder) throws RemoteException {
		// add a javascript to the header :)

		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, web2.getBundleURIToMootoolsLib());
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, goSignJSFile);
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, bundle.getResourceURIWithoutContextPath("/javascript/AscertiaHelper.js"));
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}
