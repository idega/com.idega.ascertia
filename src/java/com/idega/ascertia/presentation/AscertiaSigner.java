package com.idega.ascertia.presentation;

import java.rmi.RemoteException;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Script;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.TextInput;
import com.idega.util.expression.ELUtil;

/**
 * 
 * TODO, do a servlet that does the hashcodeing via util classes, sending and receiving and verification BYRJA a add mvn deploya jörunum sem tharf
 * 
 * A Block for signing documents (pdf) See <a href="http://www.ascertia.com">www.ascertia.com</a> for details on the GoSign product.<br>
 * 
 * @author <a href="mailto:eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0
 */
public class AscertiaSigner extends Block {

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.ascertia";
	private IWBundle bundle;
	private Web2Business web2 = null;
	private String goSignJSFile;
	private String goSignRootRootURI;

	public AscertiaSigner() {
		super();
	}

	public void main(IWContext iwc) throws RemoteException {


		web2 = (Web2Business) ELUtil.getInstance().getBean(Web2Business.class);
		AddResource resourceAdder = AddResourceFactory.getInstance(iwc);
		bundle = this.getBundle(iwc);
		goSignRootRootURI = bundle.getVirtualPathWithFileNameString("javascript/GoSign");
		goSignJSFile = goSignRootRootURI + "/lib/gosign.js";
		String serverURL = iwc.getServerURL();
		serverURL = (serverURL.endsWith("/")) ? serverURL.substring(0, serverURL.length() - 1) : serverURL;

		// TODO get dynamically
		String documentURL = "http://10.0.1.195:8080/content/files/public/company.pdf";
		BuilderService builder = (BuilderService) IBOLookup.getServiceInstance(iwc, BuilderService.class);
		addJS(iwc, resourceAdder);
		
		String targetURL="http://10.0.1.195:8080/sign/signplease";
		
		addForm(iwc);
		
		
		
		Script script = new Script();
	
		script.addScriptLine("embedAscertiaApplet('" + serverURL + goSignRootRootURI + "');");
		script.addScriptLine("GoSign_SetTargetURL('" + targetURL + "');");
		script.addScriptLine("GoSign_SetResultPage('" + builder.getCurrentPageURI(iwc) + "');");
	
		add(script);

	}

	protected void addForm(IWContext iwc) {
		Form form = new Form();
		form.setName("signing_form");
		TextInput textInput = new TextInput();
		textInput.setName("name");
		textInput.setId("name");
		form.add(textInput);
		
		DropdownMenu certificates = new DropdownMenu();
		certificates.setId("GoSignCertificateList");
		certificates.setName("GoSignCertificateList");

		Link sign = new Link("Sign document");
		sign.setOnClick("signDocument()");

		form.add(certificates);
		form.add(sign);

		this.add(form);
	}

	protected void addJS(IWContext iwc, AddResource resourceAdder) throws RemoteException {
		// add a javascript to the header :)

		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, web2.getBundleURIToMootoolsLib());
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, goSignJSFile);
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, bundle.getResourceURIWithoutContextPath("/javascript/AscertiaHelper.js"));
		// resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, web2.getMoodalboxScriptFilePath(false));
		// resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/interface/BuilderEngine.js");
		// resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/interface/DWREventService.js");
		// resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/engine.js");
		// resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, iwc.getIWMainApplication().getBundle("com.idega.builder").getVirtualPathWithFileNameString("javascript/BuilderHelper.js"));
		// resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, iwc.getIWMainApplication().getBundle("com.idega.dwr").getVirtualPathWithFileNameString("javascript/EventsHelper.js"));
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}
