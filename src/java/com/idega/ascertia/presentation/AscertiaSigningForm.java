package com.idega.ascertia.presentation;

import java.rmi.RemoteException;

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
		IFrame frame = new IFrame(SINGNING_FRAME, AscertiaSigner.class);
		div.add(frame);
		add(div);
		
	}
}
