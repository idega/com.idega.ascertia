package com.idega.ascertia.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.IFrame;

public class AscertiaSigningForm extends Block {
	
	private static final String SINGNING_FRAME = "signing_frame";
	
	public void main(IWContext iwc) throws RemoteException {
		Layer div = new Layer();
		div.setWidth("100%");
		div.setHeight("100%");
		
		BuilderService builderService = BuilderServiceFactory
		        .getBuilderService(iwc);
		
		List<AdvancedProperty> paramList = new ArrayList<AdvancedProperty>();
		Enumeration<String> paramNames = iwc.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			paramList.add(new AdvancedProperty(paramName, iwc
			        .getParameter(paramName)));
		}
		
		String pathToSigner = builderService.getUriToObject(
		    AscertiaSigner.class, paramList);
		
		IFrame frame = new IFrame(SINGNING_FRAME, pathToSigner);
		frame.setWidth("100%");
		frame.setHeight("100%");
		
		div.add(frame);
		add(div);
		
	}
}