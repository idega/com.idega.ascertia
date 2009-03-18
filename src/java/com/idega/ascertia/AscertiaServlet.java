package com.idega.ascertia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.webdav.lib.WebdavResource;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import com.ascertia.adss.client.api.DocumentHashingRequest;
import com.ascertia.adss.client.api.DocumentHashingResponse;
import com.ascertia.adss.client.api.EmptySignatureFieldRequest;
import com.ascertia.adss.client.api.EmptySignatureFieldResponse;
import com.ascertia.adss.client.api.SignatureAssemblyRequest;
import com.ascertia.adss.client.api.SignatureAssemblyResponse;
import com.ascertia.adss.client.api.SigningRequest;
import com.idega.block.pdf.util.PDFUtil;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.util.CoreConstants;
import com.idega.util.expression.ELUtil;

public class AscertiaServlet extends HttpServlet {
	
	/**
     * 
     */
	private static final long serialVersionUID = -8328885151732674569L;
	
	private static final String CONTENT_TYPE = "text/xml";
	
	private Logger logger = Logger.getLogger(AscertiaServlet.class.getName());
	
	private static final String SESSION_PARAM_FILENAME = "file_name";
	private static final String SESSION_PARAM_DOCUMENT_ID = "dociment_id";
	
	@Autowired
	private BPMHelper bpmHelper;
	
	// Initialize global variables
	
	public void init(ServletConfig a_objServletConfig) throws ServletException {
		
		ELUtil.getInstance().autowire(this);
		super.init(a_objServletConfig);
		
	}
	
	// Process the HTTP Get request
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
		
		response.setContentType("text/html");
		
		getServletContext().getRequestDispatcher("/gosign.html").include(
		    request, response);
		
	}
	
	// Process the HTTP Post request
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
		IWContext iwc = null;
		
		iwc = new IWContext(request, response, request.getSession()
		        .getServletContext());
		
		boolean isFirstTime = false;
		String certificate = null;
		HttpSession session = request.getSession(true);
		
		boolean isResponseSuccessfull = false;
		
		try {
			
			/* The midware web application has been hit/called first time */

			if (request.getParameter("serverHit") != null &&

			request.getParameter("serverHit").equalsIgnoreCase("FIRST")) {
				
				isFirstTime = true;
				
				/*
				 * Reading the parameters' values from http request and printing
				 * them on console
				 */

				certificate = request.getParameter("certificate");
				
				/*
				 * Sometimes when user's selected certificate(Base 64 encoded)
				 * is posted on http request,
				 * 
				 * '+' character is converted into ' ' character, so replacing
				 * it back with the original
				 * 
				 * character ' ' to get the original certificate's bytes
				 */

				certificate = certificate.replaceAll(" ", "+");
				
				// logger.log(Level.INFO,"str_certificate: " + certificate);
				
			}
			
			// Get the body of the HTTP request
			
			logger.log(Level.INFO,
			    "Ready to download contents having length = " +

			    request.getContentLength() + " bytes");
			
			/**
			 * Response that will be recieved from PDF Signer Server
			 */
			
			byte[] soapResponseBytes = null;
			byte[] signedDocument = null;
			
			byte rawPdfFile[];
			
			String errorMessage = "Error message hasn't been set properly.";
			
			/* Web application has been called first time, for Document Hash */

			if (isFirstTime) {
				// logger.log(Level.INFO,"Server has been hit first time.");
				InputStream inputStream;
				// getting pdf from url
				String documentURL = request
				        .getParameter(AscertiaConstants.UNSIGNED_DOCUMENT_URL);
				
				String fileName;
				if (documentURL == null || documentURL.trim().equals("")) {
					
					Integer variableHash = Integer
					        .valueOf(request
					                .getParameter(AscertiaConstants.PARAM_VARIABLE_HASH));
					Long taskInstanceId = Long.valueOf(request
					        .getParameter(AscertiaConstants.PARAM_TASK_ID));
					
					fileName = getBpmHelper().getFileName(variableHash,
					    taskInstanceId);
					rawPdfFile = getBpmHelper().getDocumentInputStream(
					    variableHash, taskInstanceId);
					
					session.setAttribute(AscertiaConstants.PARAM_VARIABLE_HASH,
					    variableHash);
					session.setAttribute(AscertiaConstants.PARAM_TASK_ID,
					    taskInstanceId);
					
					// if document url was passed
				} else {
					
					URL url = new URL(documentURL);
					inputStream = url.openStream();
					
					fileName = url.getFile();
					while (fileName.indexOf("/") != -1) {
						fileName = fileName
						        .substring(fileName.indexOf("/") + 1);
					}
					rawPdfFile = new byte[inputStream.available()];
					inputStream.read(rawPdfFile);
					
					Long taskInstanceId = Long.valueOf(request
					        .getParameter(AscertiaConstants.PARAM_TASK_ID));
					session.setAttribute(AscertiaConstants.PARAM_TASK_ID,
					    taskInstanceId);
					
				}
				
				try {
					rawPdfFile = addSignaturePage(rawPdfFile, iwc);
				} catch (Exception e) {
					// if empty page not found we will put signature on last page...
				}
				
				// Getting empty signature field
				byte[] pdfFileWithEmptySignature = null;
				if (request
				        .getParameter(AscertiaConstants.PARAM_ADD_EMPTY_SIGNATURES) == null
				        || !request.getParameter(
				            AscertiaConstants.PARAM_ADD_EMPTY_SIGNATURES)
				                .equalsIgnoreCase(Boolean.FALSE.toString())) {
					EmptySignatureFieldResponse emptySigFieldResponse = getEmtptySignatureField(
					    rawPdfFile,
					    request
					            .getParameter(AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE));
					
					if (emptySigFieldResponse.isResponseSuccessfull()) {
						
						pdfFileWithEmptySignature = emptySigFieldResponse
						        .getSignedDocument();
						isResponseSuccessfull = true;
						logger
						        .log(Level.INFO,
						            "Empty signature request has been processed successfully.");
					} else {
						isResponseSuccessfull = false;
						logger.log(Level.SEVERE, emptySigFieldResponse
						        .getErrorMessage());
					}
				} else {
					pdfFileWithEmptySignature = rawPdfFile;
					// empty signature fields already added
					isResponseSuccessfull = true;
					
				}
				if (isResponseSuccessfull) {
					isResponseSuccessfull = false;
					
					DocumentHashingResponse documentHashingResponse = getDocumentHash(
					    pdfFileWithEmptySignature,
					    certificate,
					    request
					            .getParameter(AscertiaConstants.PARAM_SELECTED_SIGNATURE_PLACE));
					
					if (documentHashingResponse.isResponseSuccessfull()) {
						
						isResponseSuccessfull = true;
						
						soapResponseBytes = documentHashingResponse
						        .getDocumentHash();
						session.setAttribute(SESSION_PARAM_FILENAME, fileName);
						session.setAttribute(SESSION_PARAM_DOCUMENT_ID,
						    documentHashingResponse.getDocumentId());
						session
						        .setAttribute(
						            AscertiaConstants.PARAM_LOCALIZED_FILE_PREFIX,
						            request
						                    .getParameter(AscertiaConstants.PARAM_LOCALIZED_FILE_PREFIX));
						logger.log(Level.INFO,
						    "Document hashing was successfull");
						session
						        .setAttribute(
						            AscertiaConstants.PARAM_SELECTED_SIGNATURE_PLACE,
						            request
						                    .getParameter(AscertiaConstants.PARAM_SELECTED_SIGNATURE_PLACE));
						session
						        .setAttribute(
						            AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE,
						            request
						                    .getParameter(AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE));
					} else {
						isResponseSuccessfull = false;
						errorMessage = documentHashingResponse
						        .getErrorMessage();
						
					}
				}
				
			}

			/*
			 * Web application has been called second time, for Signature
			 * Assembly
			 */

			else {
				
				SignatureAssemblyResponse signatureAssemblyResponse = getSignatureAssembly(request);
				
				/* Setting dummy bytes */

				soapResponseBytes = new byte[] { '1', '2', '3' };
				
				if (signatureAssemblyResponse.isResponseSuccessfull()) {
					
					// writing to disk
					isResponseSuccessfull = true;
					signedDocument = signatureAssemblyResponse
					        .getSignedDocument();
					
					Integer variableHash = (Integer) session
					        .getAttribute(AscertiaConstants.PARAM_VARIABLE_HASH);
					Long taskInstanceId = (Long) session
					        .getAttribute(AscertiaConstants.PARAM_TASK_ID);
					String fileName = (String) session
					        .getAttribute(SESSION_PARAM_FILENAME);
					
					String localizedPrefix = (String) session
					        .getAttribute(AscertiaConstants.PARAM_LOCALIZED_FILE_PREFIX);
					
					if (variableHash != null && taskInstanceId != null) {
						session
						        .removeAttribute(AscertiaConstants.PARAM_TASK_ID);
						session
						        .removeAttribute(AscertiaConstants.PARAM_VARIABLE_HASH);
						
						AscertiaData data = getBpmHelper()
						        .saveSignedPDFAttachment(
						            taskInstanceId,
						            variableHash,
						            signedDocument,
						            localizedPrefix,
						            (String) session
						                    .getAttribute(AscertiaConstants.PARAM_SELECTED_SIGNATURE_PLACE),
						            (String) session
						                    .getAttribute(AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE));
						
						session.setAttribute(
						    AscertiaConstants.PARAM_ASCERTIA_DATA, data);
						
					} else if (taskInstanceId != null && fileName != null) {
						
						session
						        .removeAttribute(AscertiaConstants.PARAM_TASK_ID);
						session.removeAttribute(SESSION_PARAM_FILENAME);
						
						AscertiaData data = getBpmHelper()
						        .saveSignedPDFAsNewVariable(
						            taskInstanceId,
						            signedDocument,
						            fileName,
						            (String) session
						                    .getAttribute(AscertiaConstants.PARAM_SELECTED_SIGNATURE_PLACE),
						            (String) session
						                    .getAttribute(AscertiaConstants.PARAM_SIGNATURE_PROFILE_TO_USE));
						session.setAttribute(
						    AscertiaConstants.PARAM_ASCERTIA_DATA, data);
					}
					session
					        .removeAttribute(AscertiaConstants.PARAM_LOCALIZED_FILE_PREFIX);
					logger.log(Level.INFO, "Documend successfully signed");
				} else {
					
					errorMessage = signatureAssemblyResponse.getErrorMessage();
					
				}
				
			}
			
			/* Sending the response back to the GoSign applet */

			if (isResponseSuccessfull) {
				
				/* Setting response headers */

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType(CONTENT_TYPE);
				response.setContentLength(soapResponseBytes.length);
				
				/* Writing message on the response output stream */

				OutputStream outputStream = response.getOutputStream();
				
				outputStream.write(soapResponseBytes, 0,
				    soapResponseBytes.length);
				
				outputStream.flush();
				outputStream.close();
				
			} else {
				
				logger.log(Level.INFO, "Response Failed...");
				
				if (isFirstTime) {
					
					logger.log(Level.SEVERE, "Hash Response Message : "
					        + errorMessage);
					
					response.sendError(
					    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					    errorMessage);
					
				} else {
					
					logger.log(Level.SEVERE, "Assembly Response Message : "
					        + errorMessage);
					response.sendError(
					    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					    errorMessage);
					
				}
				
			}
			
		} catch (Exception ex) {
			
			ex.printStackTrace();
			
			throw new ServletException("Unable to receive request : "
			        + ex.getMessage());
			
		}
		
	}
	
	protected byte[] addSignaturePage(byte[] rawPdfFile, IWContext iwc)
	        throws Exception {
		// Adding empty page at the end of document to be signed
		WebdavResource signingPage = getIWSlideService()
		        .getWebdavResourceAuthenticatedAsRoot(
		            CoreConstants.PATH_FILES_ROOT
		                    + IWMainApplication.getDefaultIWMainApplication()
		                            .getSettings().getProperty(
		                                AscertiaConstants.SIGNATURE_PAGE_URL));
		InputStream is = signingPage.getMethodData();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte buffer[] = new byte[1024];
		int noRead = 0;
		noRead = is.read(buffer, 0, 1024);
		
		while (noRead != -1) {
			baos.write(buffer, 0, noRead);
			noRead = is.read(buffer, 0, 1024);
		}
		byte[] signatresDoc = new byte[is.available()];
		signatresDoc = baos.toByteArray();
		
		List<byte[]> pdfsToMerge = new ArrayList<byte[]>(2);
		
		pdfsToMerge.add(rawPdfFile);
		pdfsToMerge.add(signatresDoc);
		
		ByteArrayOutputStream mergedPDFoutput = new ByteArrayOutputStream();
		PDFUtil.concatPDFs(pdfsToMerge, mergedPDFoutput, true);
		return mergedPDFoutput.toByteArray();
	}
	
	protected EmptySignatureFieldResponse getEmtptySignatureField(
	        byte[] rawPdfFile, String epmtySignateProfile) {
		
		String ADSS_URL = IWMainApplication.getDefaultIWMainApplication()
		        .getSettings().getProperty(
		            AscertiaConstants.PROP_ADSS_SERVER_URL,
		            "http://82.221.28.123/adss/signing/");
		
		String EMPTY_SIGNATURE_URL = ADSS_URL
		        + IWMainApplication
		                .getDefaultIWMainApplication()
		                .getSettings()
		                .getProperty(
		                    AscertiaConstants.PROP_ADDS_EMPTY_SIGNATURE_URI_END,
		                    "esi");
		String ORIGINATOR_ID = IWMainApplication.getDefaultIWMainApplication()
		        .getSettings().getProperty(
		            AscertiaConstants.PROP_ADSS_SERVER_ORIGINATOR_ID, "idega");
		
		EmptySignatureFieldRequest emptySigFieldRequest = new EmptySignatureFieldRequest(
		        ORIGINATOR_ID, IWMainApplication.getDefaultIWMainApplication()
		                .getSettings().getProperty(epmtySignateProfile,
		                    "adss:signing.profile:010"), rawPdfFile);
		emptySigFieldRequest.overrideProfileAttribute(
		    SigningRequest.SIGNING_REASON, "Testing");
		logger
		        .log(
		            Level.INFO,
		            "A request has been sent to create blank signature(s) on the PDF. Waiting for response...");
		
		/* Sending the above constructed request to the ADSS server */
		return (EmptySignatureFieldResponse) emptySigFieldRequest
		        .send(EMPTY_SIGNATURE_URL);
	}
	
	protected DocumentHashingResponse getDocumentHash(
	        byte[] pdfFileWithEmptySignature, String certificate,
	        String signaturePlace) {
		
		String ADSS_URL = IWMainApplication.getDefaultIWMainApplication()
		        .getSettings().getProperty(
		            AscertiaConstants.PROP_ADSS_SERVER_URL,
		            "http://82.221.28.123/adss/signing/");
		
		String HASHING_URL = ADSS_URL
		        + IWMainApplication.getDefaultIWMainApplication().getSettings()
		                .getProperty(AscertiaConstants.PROP_HASHING_URI_END,
		                    "dhi");
		
		String ORIGINATOR_ID = IWMainApplication.getDefaultIWMainApplication()
		        .getSettings().getProperty(
		            AscertiaConstants.PROP_ADSS_SERVER_ORIGINATOR_ID, "idega");
		String SIGNATURE_PROFILE = IWMainApplication
		        .getDefaultIWMainApplication().getSettings().getProperty(
		            signaturePlace, "adss:signing:profile:011");
		
		DocumentHashingRequest documentHashingRequest = new DocumentHashingRequest(
		        ORIGINATOR_ID, SIGNATURE_PROFILE, pdfFileWithEmptySignature,
		        Base64.decode(certificate));
		
		/*documentHashingRequest.overrideProfileAttribute(
		    SigningRequest.SIGNING_FIELD, "Right");*/
		return (DocumentHashingResponse) documentHashingRequest
		        .send(HASHING_URL);
	}
	
	public SignatureAssemblyResponse getSignatureAssembly(
	        HttpServletRequest request) throws Exception {
		
		/**
		 * URL of deployed PDF Signer Server
		 */
		String ADSS_URL = IWMainApplication.getDefaultIWMainApplication()
		        .getSettings().getProperty(
		            AscertiaConstants.PROP_ADSS_SERVER_URL,
		            "http://82.221.28.123/adss/signing/");
		
		/**
		 * URL of deployed PDF Signer Server, where Signature Assembly's request(s) will be sent
		 */
		String ASSEMBLY_URL = ADSS_URL
		        + IWMainApplication.getDefaultIWMainApplication().getSettings()
		                .getProperty(AscertiaConstants.PROP_ASSEMBLY_URL_END,
		                    "sai");
		
		/**
		 * User name, registered on PDF Signer Server
		 */
		String ORIGINATOR_ID = IWMainApplication.getDefaultIWMainApplication()
		        .getSettings().getProperty(
		            AscertiaConstants.PROP_ADSS_SERVER_ORIGINATOR_ID, "idega");
		/*
		 * Input stream that reads the PKCS#7 bytes, that has been
		 * calculated by GoSign applet
		 */
		InputStream obj_inputStream = request.getInputStream();
		ByteArrayOutputStream obj_bos = new ByteArrayOutputStream();
		byte[] byte_buffer = new byte[128];
		int i_read = 0;
		while ((i_read = obj_inputStream.read(byte_buffer)) > 0) {
			obj_bos.write(byte_buffer, 0, i_read);
			
		}
		
		byte[] pkcs7 = obj_bos.toByteArray();
		
		/* Constructing request for signature assembly */

		SignatureAssemblyRequest signatureAssemblyRequest = new SignatureAssemblyRequest(
		        ORIGINATOR_ID, pkcs7, (String) request.getSession(false)
		                .getAttribute(SESSION_PARAM_DOCUMENT_ID));
		
		/* Sending request to the ADSS server */

		return (SignatureAssemblyResponse) signatureAssemblyRequest
		        .send(ASSEMBLY_URL);
	}
	
	private IWSlideService getIWSlideService() throws IBOLookupException {
		return (IWSlideService) IBOLookup.getServiceInstance(IWMainApplication
		        .getDefaultIWMainApplication().getIWApplicationContext(),
		    IWSlideService.class);
	}
	
	public BPMHelper getBpmHelper() {
		return bpmHelper;
	}
	
	public void setBpmHelper(BPMHelper bpmHelper) {
		this.bpmHelper = bpmHelper;
	}
	
}