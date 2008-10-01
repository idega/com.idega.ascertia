package com.idega.ascertia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.ascertia.presentation.AscertiaSigningForm;
import com.idega.bpm.pdf.business.ProcessTaskInstanceConverterToPDF;
import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.jbpm.signing.SigningHandler;
import com.idega.util.CoreConstants;
import com.idega.util.StringUtil;

/**
 * @author <a href="mailto:juozas@idega.com">Juozapas Zabukas</a>
 * @version $Revision: 1.1 $
 * 
 *          Last modified: $Date: 2008/10/01 07:44:00 $ by $Author: juozas $
 */
@Scope("singleton")
@Service
public class AscertiaSigningHandlerImpl implements SigningHandler {

	@Autowired
	private BuilderLogicWrapper builderLogicWrapper;

	@Autowired
	private ProcessTaskInstanceConverterToPDF processTaskInstanceConverterToPDF;

	// IWResourceBundle iwrb, String taskInstanceId, String hashValue, String
	// image, String uri, String message,
	// String errorMessage
	public String getSigningAction(Long taskInstanceId, String hashValue) {

		String uri = getBuilderLogicWrapper().getBuilderService(
				IWMainApplication.getDefaultIWApplicationContext())
				.getUriToObject(AscertiaSigningForm.class, null);
		// String uri = null;

		IWResourceBundle iwrb = null;

		hashValue = StringUtil.isEmpty(hashValue) ? processTaskInstanceConverterToPDF
				.getHashValueForGeneratedPDFFromXForm(String
						.valueOf(taskInstanceId), true)
				: hashValue;

		// hashValue = StringUtil.isEmpty(hashValue) ? CoreConstants.MINUS
		// : hashValue;

		String parameters = new StringBuilder(AscertiaConstants.PARAM_TASK_ID)
				.append("=").append(taskInstanceId).append("&").append(
						AscertiaConstants.PARAM_VARIABLE_HASH).append("=")
				.append(hashValue).toString();

		hashValue = StringUtil.isEmpty(hashValue) ? CoreConstants.MINUS
				: hashValue;
		// String values = new
		// StringBuilder("['").append(taskInstanceId).append(
		// "', '").append(hashValue).append("']").toString();
		return new StringBuilder(uri).append("&").append(parameters).toString();
	}

	BuilderLogicWrapper getBuilderLogicWrapper() {
		return builderLogicWrapper;
	}

	void setBuilderLogicWrapper(BuilderLogicWrapper builderLogicWrapper) {
		this.builderLogicWrapper = builderLogicWrapper;
	}
}