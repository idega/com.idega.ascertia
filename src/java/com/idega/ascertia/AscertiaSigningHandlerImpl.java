package com.idega.ascertia;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.ascertia.presentation.AscertiaSigningForm;
import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.signing.SigningHandler;
import com.idega.util.StringUtil;
import com.idega.util.URIUtil;

/**
 * @author <a href="mailto:juozas@idega.com">Juozapas Zabukas</a>
 * @version $Revision: 1.5 $ Last modified: $Date: 2009/03/18 09:28:30 $ by $Author: juozas $
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AscertiaSigningHandlerImpl implements SigningHandler {
	
	@Autowired
	private BuilderLogicWrapper builderLogicWrapper;
	
	@Autowired
	private BPMHelper bpmHelper;
	
	public String getSigningAction(Long taskInstanceId, String hashValue) {
		URIUtil uri = new URIUtil(getBuilderLogicWrapper().getBuilderService(IWMainApplication.getDefaultIWApplicationContext())
				.getUriToObject(AscertiaSigningForm.class, null));
		
		hashValue = StringUtil.isEmpty(hashValue) ? getBpmHelper().generateDocumentFromForm(taskInstanceId) : hashValue;
		uri.setParameter(AscertiaConstants.PARAM_TASK_ID, String.valueOf(taskInstanceId));
		uri.setParameter(AscertiaConstants.PARAM_VARIABLE_HASH, hashValue);
		
		Map<String, String> paramsMap = getBpmHelper().getParametersForSigner(taskInstanceId, Integer.valueOf(hashValue));
		for (String param : paramsMap.keySet()) {
			uri.setParameter(param, paramsMap.get(param));
		}
		return uri.getUri();
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