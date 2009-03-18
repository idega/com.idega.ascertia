package com.idega.ascertia;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.ascertia.presentation.AscertiaSigningForm;
import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.signing.SigningHandler;
import com.idega.util.StringUtil;

/**
 * @author <a href="mailto:juozas@idega.com">Juozapas Zabukas</a>
 * @version $Revision: 1.5 $ Last modified: $Date: 2009/03/18 09:28:30 $ by $Author: juozas $
 */
@Scope("singleton")
@Service
public class AscertiaSigningHandlerImpl implements SigningHandler {
	
	@Autowired
	private BuilderLogicWrapper builderLogicWrapper;
	
	@Autowired
	private BPMHelper bpmHelper;
	
	public String getSigningAction(Long taskInstanceId, String hashValue) {
		
		String uri = getBuilderLogicWrapper().getBuilderService(
		    IWMainApplication.getDefaultIWApplicationContext()).getUriToObject(
		    AscertiaSigningForm.class, null);
		
		hashValue = StringUtil.isEmpty(hashValue) ? getBpmHelper()
		        .generateDocumentFromForm(taskInstanceId) : hashValue;
		
		String parameters = new StringBuilder(AscertiaConstants.PARAM_TASK_ID)
		        .append("=").append(taskInstanceId).append("&").append(
		            AscertiaConstants.PARAM_VARIABLE_HASH).append("=").append(
		            hashValue).toString();
		
		Map<String, String> paramsMap = getBpmHelper().getParametersForSigner(
		    taskInstanceId, Integer.valueOf(hashValue));
		
		StringBuilder stringBuilder = new StringBuilder(parameters);
		for (String param : paramsMap.keySet()) {
			stringBuilder.append("&").append(param).append("=").append(paramsMap.get(param));
		}
		parameters = stringBuilder.toString();
		return new StringBuilder(uri).append("&").append(parameters).toString();
	}
	
	BuilderLogicWrapper getBuilderLogicWrapper() {
		return builderLogicWrapper;
	}
	
	void setBuilderLogicWrapper(BuilderLogicWrapper builderLogicWrapper) {
		this.builderLogicWrapper = builderLogicWrapper;
	}
	
	public BPMHelper getBpmHelper() {
		return bpmHelper;
	}
	
	public void setBpmHelper(BPMHelper bpmHelper) {
		this.bpmHelper = bpmHelper;
	}
}