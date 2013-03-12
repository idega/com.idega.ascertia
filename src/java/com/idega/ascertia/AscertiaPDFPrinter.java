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

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.core.file.util.MimeTypeUtil;
import com.idega.io.MediaWritable;
import com.idega.jbpm.variables.BinaryVariable;
import com.idega.jbpm.variables.VariablesHandler;
import com.idega.presentation.IWContext;
import com.idega.util.FileUtil;
import com.idega.util.IOUtil;
import com.idega.util.expression.ELUtil;

public class AscertiaPDFPrinter implements MediaWritable {

	private static final Logger logger = Logger.getLogger(AscertiaPDFWriter.class.getName());
	
	private InputStream inputStream;
	
	@Autowired
	private VariablesHandler variablesHandler;
	
	public String getMimeType() {
		return MimeTypeUtil.MIME_TYPE_PDF_1;
	}
	
	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		AscertiaData ascertiaData = (AscertiaData) iwc.getSession().getAttribute(AscertiaConstants.PARAM_ASCERTIA_DATA);
		String taskIdStr = iwc.getParameter(AscertiaConstants.PARAM_TASK_ID);
		String variableHashStr = iwc.getParameter(AscertiaConstants.PARAM_VARIABLE_HASH);
		
		byte[] document = null;
		if (taskIdStr != null && variableHashStr != null) {
			Integer variableHash = Integer.valueOf(variableHashStr);
			Long taskInstanceId = Long.valueOf(taskIdStr);

			VariablesHandler variablesHandler = getVariablesHandler(iwc.getServletContext());
			BinaryVariable binaryVariable = getBinVar(variablesHandler, taskInstanceId, variableHash);

			InputStream inputStream = variablesHandler.getBinaryVariablesHandler().getBinaryVariableContent(binaryVariable);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				FileUtil.streamToOutputStream(inputStream, baos);
			} catch (IOException e) {
				logger.log(Level.WARNING, "Error streaming!", e);
				IOUtil.close(baos);
				return;
			}
			
			document = baos.toByteArray();
		} else if (ascertiaData != null) {
			document = ascertiaData.getByteDocument();
		}
		if (document == null) {
			logger.warning("Document is undefined!");
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(document);	
		this.inputStream = bais;
		setOutpusAsPDF(iwc, document.length);
	}
	
	@Override
	public void writeTo(OutputStream streamOut) throws IOException {
		if (inputStream == null) {
			logger.severe("Unable to get input stream");
			return;
		}
		
		FileUtil.streamToOutputStream(inputStream, streamOut);
		
		streamOut.flush();
		streamOut.close();
	}
	
	private void setOutpusAsPDF(IWContext iwc, int fileLength) {
		iwc.getResponse().setHeader("Content-Disposition", "inline");
		if (fileLength > 0) {
			iwc.getResponse().setContentLength(fileLength);
		}
	}
	
	private BinaryVariable getBinVar(VariablesHandler variablesHandler, long taskInstanceId, Integer binaryVariableHash) {
		List<BinaryVariable> variables = variablesHandler.resolveBinaryVariables(taskInstanceId);

		for (BinaryVariable binaryVariable : variables) {
			if (binaryVariable.getHash().equals(binaryVariableHash)) {
				return binaryVariable;
			}
		}

		return null;
	}

	private VariablesHandler getVariablesHandler(ServletContext ctx) {
		if (variablesHandler == null) {
			ELUtil.getInstance().autowire(this);
		}
		return variablesHandler;
	}
}