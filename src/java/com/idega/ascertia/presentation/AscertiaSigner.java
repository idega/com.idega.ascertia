package com.idega.ascertia.presentation;

import java.rmi.RemoteException;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

import com.idega.block.web2.business.Web2Business;
import com.idega.business.SpringBeanLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Script;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;

/**
 * A Block for signing documents (pdf)
 * See <a href="http://www.ascertia.com">www.ascertia.com</a> for details on the GoSign product.<br>
 * @author <a href="mailto:eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0
 */
public class AscertiaSigner extends Block {


	private static final String IW_BUNDLE_IDENTIFIER="com.idega.ascertia";
	private IWBundle bundle;
	private Web2Business web2 = null;
	private String goSignJSFile;
	private String goSignRootRootURI;
	
	
	public AscertiaSigner() {
		super();
	}
	
	
	public void main(IWContext iwc) throws RemoteException{
		web2 = (Web2Business) SpringBeanLookup.getInstance().getSpringBean(iwc, Web2Business.class);
		AddResource resourceAdder = AddResourceFactory.getInstance(iwc);
		bundle = this.getBundle(iwc);
		goSignRootRootURI =  bundle.getVirtualPathWithFileNameString("javascript/GoSign");
		goSignJSFile = goSignRootRootURI+"/lib/gosign.js";
		String serverURL = iwc.getServerURL();
		serverURL = serverURL.substring(0,serverURL.lastIndexOf("/"));
		
		addJS(iwc, resourceAdder);
		
		addForm(iwc);
		
		Script script = new Script();
		//script.addScriptLine("window.addEvent('domready', function() { embedAscertiaApplet('"+serverURL+goSignRootRootURI+"'); });");
		script.addScriptLine("embedAscertiaApplet('"+serverURL+goSignRootRootURI+"');");
		//script.addScriptLine("embedAscertiaApplet('"+goSignRootRootURI+"');");
		add(script);
		
	
	}


	protected void addForm(IWContext iwc) {
		Form form = new Form();
		DropdownMenu certificates = new DropdownMenu();
		certificates.setId("GoSignCertificateList");
		certificates.setName("GoSignCertificateList");
		
		Link sign = new Link("Sign document");
		sign.setOnClick("signDocument()");
		
		form.add(certificates);
		form.add(sign);
		
		this.add(form);
	}


	protected void addJS(IWContext iwc, AddResource resourceAdder)
			throws RemoteException {
		//add a javascript to the header :)
		
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, web2.getBundleURIToMootoolsLib());
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, goSignJSFile);
		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, bundle.getResourceURIWithoutContextPath("/javascript/AscertiaHelper.js"));
		//resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, web2.getMoodalboxScriptFilePath(false));
		//resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/interface/BuilderEngine.js");
//		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/interface/DWREventService.js");
//		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, "/dwr/engine.js");
		//resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN, iwc.getIWMainApplication().getBundle("com.idega.builder").getVirtualPathWithFileNameString("javascript/BuilderHelper.js"));
//		resourceAdder.addJavaScriptAtPosition(iwc, AddResource.HEADER_BEGIN,  iwc.getIWMainApplication().getBundle("com.idega.dwr").getVirtualPathWithFileNameString("javascript/EventsHelper.js"));
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}
