package com.idega.ascertia.presentation;

import java.rmi.RemoteException;
import java.util.Arrays;

import com.idega.ascertia.AscertiaConstants;
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
		
		String pathToSigner = builderService.getUriToObject(AscertiaSigner.class,
			Arrays.asList(new AdvancedProperty[] {
					new AdvancedProperty(
							AscertiaConstants.UNSIGNED_DOCUMENT_URL,
								iwc.getParameter(AscertiaConstants.UNSIGNED_DOCUMENT_URL)),
					new AdvancedProperty(
							AscertiaConstants.PARAM_TASK_ID,
								iwc.getParameter(AscertiaConstants.PARAM_TASK_ID)),
					new AdvancedProperty(
							AscertiaConstants.PARAM_VARIABLE_HASH,
								iwc.getParameter(AscertiaConstants.PARAM_VARIABLE_HASH)) }));

		IFrame frame = new IFrame(SINGNING_FRAME, pathToSigner);
		frame.setWidth("100%");
		frame.setHeight("100%");

		div.add(frame);
		add(div);

	}
}
