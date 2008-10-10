package com.idega.ascertia;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.idega.io.MediaWritable;
import com.idega.jbpm.variables.BinaryVariable;
import com.idega.jbpm.variables.VariablesHandler;
import com.idega.presentation.IWContext;
import com.idega.util.FileUtil;
import com.idega.util.expression.ELUtil;

public class AscertiaPDFPrinter implements MediaWritable{

	public String getMimeType() {
		return "application/pdf";
	}

	private static final Logger logger = Logger.getLogger(AscertiaPDFWriter.class.getName());
	
	private InputStream inputStream;
		


	public void init(HttpServletRequest req, IWContext iwc) {
		AscertiaData ascertiaData = (AscertiaData)iwc.getSession().getAttribute(AscertiaConstants.PARAM_ASCERTIA_DATA);
		String taskIdStr = iwc.getParameter(AscertiaConstants.PARAM_TASK_ID);
		String variableHashStr =iwc.getParameter(AscertiaConstants.PARAM_VARIABLE_HASH);
		if (taskIdStr != null && variableHashStr != null){

			Integer variableHash = Integer.valueOf(variableHashStr);
			Long taskInstanceId = Long.valueOf(taskIdStr);

			VariablesHandler variablesHandler = getVariablesHandler(iwc.getServletContext());
			BinaryVariable binaryVariable = getBinVar(variablesHandler, taskInstanceId, variableHash);

			InputStream inputStream = variablesHandler.getBinaryVariablesHandler()
					.getBinaryVariableContent(binaryVariable);
			
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
				logger.log(Level.SEVERE, "Unable to read from input stream",e);
				inputStream = null;
				return;
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());	
			this.inputStream = bais;
			setOutpusAsPDF(iwc, bais.available());
		}else if(ascertiaData != null){
			byte[] document = ascertiaData.getByteDocument();

			ByteArrayInputStream bais = new ByteArrayInputStream(document);	
			this.inputStream = bais;
			
			setOutpusAsPDF(iwc, bais.available());
		}
	}
	
	public void writeTo(OutputStream streamOut) throws IOException {
		if (inputStream == null) {
			logger.log(Level.SEVERE, "Unable to get input stream");
			return;
		}
		
		FileUtil.streamToOutputStream(inputStream, streamOut);
		
		streamOut.flush();
		streamOut.close();
	}
	
	
	public void setOutpusAsPDF(IWContext iwc, int fileLength) {
		iwc.getResponse().setContentType("application/pdf");
		if (fileLength > 0) {
			iwc.getResponse().setContentLength(fileLength);
		}
	}
	
	private BinaryVariable getBinVar(VariablesHandler variablesHandler,
			long taskInstanceId, Integer binaryVariableHash) {

		List<BinaryVariable> variables = variablesHandler
				.resolveBinaryVariables(taskInstanceId);

		for (BinaryVariable binaryVariable : variables) {

			if (binaryVariable.getHash().equals(binaryVariableHash)) {

				return binaryVariable;
			}
		}

		return null;
	}

	private VariablesHandler getVariablesHandler(ServletContext ctx) {

		return ELUtil.getInstance().getBean("bpmVariablesHandler");
	}

}
