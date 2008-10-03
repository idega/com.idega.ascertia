package com.idega.ascertia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.ascertia.presentation.AscertiaSigningForm;
import com.idega.bpm.pdf.business.ProcessTaskInstanceConverterToPDF;
import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.signing.SigningHandler;
import com.idega.util.StringUtil;

/**
 * @author <a href="mailto:juozas@idega.com">Juozapas Zabukas</a>
 * @version $Revision: 1.3 $
 * 
 *          Last modified: $Date: 2008/10/03 10:55:46 $ by $Author: juozas $
 */
@Scope("singleton")
@Service
public class AscertiaSigningHandlerImpl implements SigningHandler {

	@Autowired
	private BuilderLogicWrapper builderLogicWrapper;

	@Autowired
	private ProcessTaskInstanceConverterToPDF processTaskInstanceConverterToPDF;
	
	public String getSigningAction(Long taskInstanceId, String hashValue) {

		String uri = getBuilderLogicWrapper().getBuilderService(
				IWMainApplication.getDefaultIWApplicationContext())
				.getUriToObject(AscertiaSigningForm.class, null);

		hashValue = StringUtil.isEmpty(hashValue) ? processTaskInstanceConverterToPDF
				.getHashValueForGeneratedPDFFromXForm(String
						.valueOf(taskInstanceId), true)
				: hashValue;

				
		
		
		String parameters = new StringBuilder(AscertiaConstants.PARAM_TASK_ID)
				.append("=").append(taskInstanceId).append("&").append(
						AscertiaConstants.PARAM_VARIABLE_HASH).append("=")
				.append(hashValue).toString();

		return new StringBuilder(uri).append("&").append(parameters).toString();
	}

	BuilderLogicWrapper getBuilderLogicWrapper() {
		return builderLogicWrapper;
	}

	void setBuilderLogicWrapper(BuilderLogicWrapper builderLogicWrapper) {
		this.builderLogicWrapper = builderLogicWrapper;
	}
}