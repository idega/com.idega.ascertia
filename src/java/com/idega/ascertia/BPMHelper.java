package com.idega.ascertia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.form.business.FormConverterToPDF;
import com.idega.block.process.variables.Variable;
import com.idega.jbpm.exe.BPMFactory;
import com.idega.jbpm.exe.TaskInstanceW;
import com.idega.jbpm.variables.BinaryVariable;
import com.idega.jbpm.variables.VariablesHandler;
import com.idega.jbpm.view.ViewSubmission;
import com.idega.util.StringUtil;

/**
 * Helper class to work with BPM in Ascertia module
 * 
 * @author juozas
 */
@Service
@Scope("singleton")
public class BPMHelper {
	
	private Logger logger = Logger.getLogger(BPMHelper.class.getName());
	
	@Autowired
	private BPMFactory bpmFactory;
	
	@Autowired
	private VariablesHandler variablesHandler;
	
	@Autowired
	private FormConverterToPDF formConverterToPDF;
	
	public Map<String, Object> getVariables(Long taskInstanceId) {
		return getVariablesHandler().populateVariables(taskInstanceId);
	}
	
	AscertiaData saveSignedPDFAttachment(long taskInstanceId,
	        Integer binaryVariableHash, byte[] signedPDF,
	        String localizedPrefix, String signaturePlaceUsed,
	        String signatureProfileUsed) throws Exception {
		
		TaskInstanceW taskInstance = getBpmFactory()
		        .getProcessManagerByTaskInstanceId(taskInstanceId)
		        .getTaskInstance(taskInstanceId);
		
		BinaryVariable binaryVariable = getBinaryVariable(taskInstanceId,
		    binaryVariableHash);
		
		String fileName = binaryVariable.getFileName().replace(".pdf",
		    "_signed.pdf");
		InputStream inputStream = new ByteArrayInputStream(signedPDF);
		
		try {
			String description;
			if (localizedPrefix != null) {
				description = localizedPrefix;
			} else {
				description = "Signed";
			}
			description += " "
			        + (StringUtil.isEmpty(binaryVariable.getDescription()) ? binaryVariable
			                .getFileName()
			                : binaryVariable.getDescription());
			
			BinaryVariable signedBinaryVariable = taskInstance.addAttachment(
			    binaryVariable.getVariable(), fileName, description,
			    inputStream);
			signedBinaryVariable.setMetadata(binaryVariable.getMetadata());
			
			signedBinaryVariable.getMetadata().put(
			    AscertiaConstants.PARAM_ADD_EMPTY_SIGNATURES, Boolean.FALSE.toString());
			
			@SuppressWarnings("unchecked")
			List<String> signaturePlacesUsed = signedBinaryVariable
			        .getMetadata().get(
			            AscertiaConstants.PARAM_SIGNATURE_PLACES_USED) != null ? (List<String>) signedBinaryVariable
			        .getMetadata().get(
			            AscertiaConstants.PARAM_SIGNATURE_PLACES_USED)
			        : new ArrayList<String>();
			
			
			signedBinaryVariable.getMetadata().put(
			    AscertiaConstants.PARAM_SIGNATURE_PLACES_USED,
			    signaturePlacesUsed);
			
			signedBinaryVariable.getMetadata().put(
			    AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE,
			    signatureProfileUsed);
			
			signaturePlacesUsed.add(signaturePlaceUsed);
			signedBinaryVariable
			        .setSigned(canBeSignedAgain(signedBinaryVariable));
			signedBinaryVariable.update();
			
			binaryVariable.setHidden(true);
			binaryVariable.update();
			
			VariablesHandler variablesHandler = getVariablesHandler();
			
			inputStream = variablesHandler.getBinaryVariablesHandler()
			        .getBinaryVariableContent(signedBinaryVariable);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte buffer[] = new byte[1024];
			int noRead = 0;
			try {
				noRead = inputStream.read(buffer, 0, 1024);
				while (noRead != -1) {
					baos.write(buffer, 0, noRead);
					noRead = inputStream.read(buffer, 0, 1024);
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Unable to read from input stream", e);
				inputStream = null;
				return null;
			}
			
			AscertiaData data = new AscertiaData();
			data.setDocumentName(fileName);
			data.setByteDocument(baos.toByteArray());
			
			return data;
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,
			    "Unable to set binary variable with signed document for task instance: "
			            + taskInstanceId, e);
			throw new Exception(e);
			
		}
		
	}
	
	AscertiaData saveSignedPDFAsNewVariable(long taskInstanceId,
	        byte[] signedPDF, String fileName, String signaturePlaceUsed,
	        String signatureProfileUsed) throws Exception {
		
		TaskInstanceW taskInstance = getBpmFactory()
		        .getProcessManagerByTaskInstanceId(taskInstanceId)
		        .getTaskInstance(taskInstanceId);
		
		InputStream inputStream = new ByteArrayInputStream(signedPDF);
		
		try {
			
			Variable variable = Variable
			        .parseDefaultStringRepresentation(AscertiaConstants.SIGNED_VARIABLE_NAME);
			
			BinaryVariable signedBinaryVariable = taskInstance.addAttachment(
			    variable, fileName, fileName, inputStream);
			
			signedBinaryVariable.getMetadata().put(
			    AscertiaConstants.PARAM_ADD_EMPTY_SIGNATURES, "false");
			
			@SuppressWarnings("unchecked")
			List<String> signaturePlacesUsed = signedBinaryVariable
			        .getMetadata().get(
			            AscertiaConstants.PARAM_SIGNATURE_PLACES_USED) != null ? (List<String>) signedBinaryVariable
			        .getMetadata().get(
			            AscertiaConstants.PARAM_SIGNATURE_PLACES_USED)
			        : new ArrayList<String>();
			
			signaturePlacesUsed.add(signaturePlaceUsed);
			signedBinaryVariable.getMetadata().put(
			    AscertiaConstants.PARAM_SIGNATURE_PLACES_USED,
			    signaturePlacesUsed);
			
			signedBinaryVariable.getMetadata().put(
			    AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE,
			    signatureProfileUsed);
			
			signedBinaryVariable
			        .setSigned(canBeSignedAgain(signedBinaryVariable));
			signedBinaryVariable.update();
			
			VariablesHandler variablesHandler = getVariablesHandler();
			
			inputStream = variablesHandler.getBinaryVariablesHandler()
			        .getBinaryVariableContent(signedBinaryVariable);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte buffer[] = new byte[1024];
			int noRead = 0;
			try {
				noRead = inputStream.read(buffer, 0, 1024);
				while (noRead != -1) {
					baos.write(buffer, 0, noRead);
					noRead = inputStream.read(buffer, 0, 1024);
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Unable to read from input stream", e);
				inputStream = null;
				return null;
			}
			
			ViewSubmission viewSubmission = getBpmFactory().getViewSubmission();
			viewSubmission.setTaskInstanceId(taskInstance.getTaskInstanceId());
			taskInstance.submit(viewSubmission);
			
			AscertiaData data = new AscertiaData();
			data.setDocumentName(fileName);
			data.setByteDocument(baos.toByteArray());
			
			return data;
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,
			    "Unable to set binary variable with signed document for task instance: "
			            + taskInstanceId, e);
			throw new Exception(e);
			
		}
		
	}
	
	byte[] getDocumentInputStream(Integer variableHash, Long taskInstanceId)
	        throws IOException {
		
		BinaryVariable binaryVariable = getBinaryVariable(taskInstanceId,
		    variableHash);
		InputStream inputStream = getVariablesHandler()
		        .getBinaryVariablesHandler().getBinaryVariableContent(
		            binaryVariable);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte buffer[] = new byte[1024];
		int noRead = 0;
		noRead = inputStream.read(buffer, 0, 1024);
		// Write out the stream to the file
		while (noRead != -1) {
			baos.write(buffer, 0, noRead);
			noRead = inputStream.read(buffer, 0, 1024);
		}
		return baos.toByteArray();
	}
	
	public BinaryVariable getBinaryVariable(long taskInstanceId,
	        Integer binaryVariableHash) {
		
		List<BinaryVariable> variables = getVariablesHandler()
		        .resolveBinaryVariables(taskInstanceId);
		
		for (BinaryVariable binaryVariable : variables) {
			
			if (binaryVariable.getHash().equals(binaryVariableHash)) {
				
				return binaryVariable;
			}
		}
		
		return null;
	}
	
	public String getFileName(Integer variableHash, Long taskInstanceId) {
		BinaryVariable binaryVariable = getBinaryVariable(taskInstanceId,
		    variableHash);
		return binaryVariable.getFileName();
	}
	
	public String generateDocumentFromForm(Long taskInstanceId) {
		
		String variableHashString = getFormConverterToPDF()
		        .getHashValueForGeneratedPDFFromXForm(
		            String.valueOf(taskInstanceId), true);
		
		Integer binaryVariableHash = Integer.valueOf(variableHashString);
		
		Map<String, Object> variables = getVariables(taskInstanceId);
		// If profile is set for signing generated pdf from task document, adding it to binary
		// variables metadata
		if (variables.get(AscertiaConstants.BPM_TASK_SIGNING_PROFILE) != null) {
			
			BinaryVariable binaryVariable = getBinaryVariable(taskInstanceId,
			    binaryVariableHash);
			
			binaryVariable.getMetadata().put(
			    AscertiaConstants.BPM_TASK_SIGNING_PROFILE,
			    variables.get(AscertiaConstants.BPM_TASK_SIGNING_PROFILE));
		}
		
		return variableHashString;
	}
	
	public Map<String, String> getParametersForSigner(Long taskInstanceId,
	        Integer variableHash) {
		
		Map<String, String> paramsMap = new HashMap<String, String>();
		
		BinaryVariable binaryVariable = getBinaryVariable(taskInstanceId,
		    variableHash);
		
		// Adding a profile to use
		if (binaryVariable.getMetadata().get(
		    AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE) != null) {
			
			paramsMap.put(AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE,
			    (String) binaryVariable.getMetadata().get(
			        AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE));
		} else {
			Map<String, Object> variables = getVariables(taskInstanceId);
			if (variables.get(AscertiaConstants.BPM_ATTACHMENT_SIGNING_PROFILE) != null) {
				
				paramsMap
				        .put(
				            AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE,
				            (String) variables
				                    .get(AscertiaConstants.BPM_ATTACHMENT_SIGNING_PROFILE));
			}
		}
		
		// flag to add empty signatures filed(s) or not
		if (binaryVariable.getMetadata().get(
		    AscertiaConstants.PARAM_ADD_EMPTY_SIGNATURES) != null) {
			
			paramsMap.put(AscertiaConstants.PARAM_ADD_EMPTY_SIGNATURES,
			    (String) binaryVariable.getMetadata().get(
			        AscertiaConstants.PARAM_ADD_EMPTY_SIGNATURES));
			
		} else {
			paramsMap.put(AscertiaConstants.PARAM_ADD_EMPTY_SIGNATURES,
			    Boolean.TRUE.toString());
		}
		
		// adding places for signatures
		if (paramsMap.get(AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE) != null
		        && paramsMap.get(
		            AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE).equals(
		            AscertiaConstants.PROP_EMPTY_ONE_SIGNATURE_PROFILE)) {
			
			paramsMap.put(AscertiaConstants.PARAM_SIGNATURE_PLACES_USED,
			    AscertiaConstants.PROP_ONE_SIGNATURE_PROFILE_PLACE);
		} else if (paramsMap
		        .get(AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE) != null
		        && paramsMap.get(
		            AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE).equals(
		            AscertiaConstants.PROP_EMPTY_TWO_SIGNATURE_PROFILE)) {
			
			@SuppressWarnings("unchecked")
			List<String> signingPlacesUsed = (List<String>) binaryVariable
			        .getMetadata().get(
			            AscertiaConstants.PARAM_SIGNATURE_PLACES_USED);
			
			StringBuilder optionsStringBuilder = new StringBuilder();
			if (signingPlacesUsed != null) {
				if (!signingPlacesUsed
				        .contains(AscertiaConstants.PROP_TWO_SIGNATURE_PROFILE_LEFT)) {
					
					optionsStringBuilder
					        .append(AscertiaConstants.PROP_TWO_SIGNATURE_PROFILE_LEFT);
				}
				
				if (!signingPlacesUsed
				        .contains(AscertiaConstants.PROP_TWO_SIGNATURE_PROFILE_RIGHT)) {
					
					optionsStringBuilder
					        .append(AscertiaConstants.PROP_TWO_SIGNATURE_PROFILE_RIGHT);
				}
			} else {
				optionsStringBuilder.append(
				    AscertiaConstants.PROP_TWO_SIGNATURE_PROFILE_LEFT).append(
				    ";").append(
				    AscertiaConstants.PROP_TWO_SIGNATURE_PROFILE_RIGHT);
			}
			paramsMap.put(AscertiaConstants.PARAM_SIGNATURE_PLACES_USED,
			    optionsStringBuilder.toString());
		}
		
		return paramsMap;
	}
	
	protected boolean canBeSignedAgain(BinaryVariable binaryVariable) {
		@SuppressWarnings("unchecked")
		List<String> signingPlacesUsed = (List<String>) binaryVariable
		        .getMetadata().get(
		            AscertiaConstants.PARAM_SIGNATURE_PLACES_USED);
		
		if (binaryVariable.getMetadata().get(
		    AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE) != null
		        && binaryVariable.getMetadata().get(
		            AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE).equals(
		            AscertiaConstants.PROP_EMPTY_TWO_SIGNATURE_PROFILE)) {
			if (signingPlacesUsed.size() >1) {
				return true;
			}
			
		} else if (binaryVariable.getMetadata().get(
		    AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE) != null
		        && binaryVariable.getMetadata().get(
		            AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE).equals(
		            AscertiaConstants.PROP_EMPTY_ONE_SIGNATURE_PROFILE)) {
			if (signingPlacesUsed.size() >0) {
				return true;
			}
			
		}
		return false;
	}
	
	protected VariablesHandler getVariablesHandler() {
		return variablesHandler;
	}
	
	protected FormConverterToPDF getFormConverterToPDF() {
		return formConverterToPDF;
	}
	
	protected void setFormConverterToPDF(FormConverterToPDF formConverterToPDF) {
		this.formConverterToPDF = formConverterToPDF;
	}
	
	protected BPMFactory getBpmFactory() {
		return bpmFactory;
	}
	
	protected void setBpmFactory(BPMFactory bpmFactory) {
		this.bpmFactory = bpmFactory;
	}
	
}
