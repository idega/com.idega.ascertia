package com.idega.ascertia.presentation;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import com.idega.ascertia.AscertiaConstants;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.ui.IFrame;

public class AscertiaSigningForm extends Block{

	private static final String SINGNING_FRAME ="signing_frame";
	
	public void main(IWContext iwc) throws RemoteException {
		System.out.println(iwc.getParameter(AscertiaConstants.UNSIGNED_DOCUMENT_URL));
		Layer div = new Layer();
		div.setWidth("100%");
		div.setHeight("100%");
		IFrame frame = new IFrame(SINGNING_FRAME, AscertiaSigner.class);
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put(AscertiaConstants.UNSIGNED_DOCUMENT_URL, iwc.getParameter(AscertiaConstants.UNSIGNED_DOCUMENT_URL));
		frame.setParameters(parameterMap);
		frame.setWidth("100%");
		frame.setHeight("100%");
		
		div.add(frame);
		add(div);
		
	}
}
