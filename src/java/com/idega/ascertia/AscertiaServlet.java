/*
 * Copyright (C) 2008 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */

package com.idega.ascertia;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.faces.context.FacesContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bouncycastle.util.encoders.Base64;

import com.ascertia.adss.client.api.DocumentHashingRequest;
import com.ascertia.adss.client.api.DocumentHashingResponse;
import com.ascertia.adss.client.api.SignatureAssemblyRequest;
import com.ascertia.adss.client.api.SignatureAssemblyResponse;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.webface.WFUtil;



public class AscertiaServlet extends HttpServlet {
	/**

	 * Contents' type of http response

	 */

	private static final String CONTENT_TYPE = "text/xml";

	//Initialize global variables

	public void init(ServletConfig a_objServletConfig) throws ServletException {

		super.init(a_objServletConfig);
		
		System.out.println("Asctertia servlet started");

	}

	//Process the HTTP Get request

	public void doGet(HttpServletRequest request,

	HttpServletResponse response) throws

	ServletException, IOException {

		System.out.println(

		"GoSign Request Has Been Recieved in the doGet Method...");

		response.setContentType("text/html");

		getServletContext().getRequestDispatcher("/gosign.html").include(request, response);

	}

	//Process the HTTP Post request

	public void doPost(HttpServletRequest a_objRequest,

	HttpServletResponse a_objResponse) throws

	ServletException, IOException {

		System.out.println(

		"GoSign Request Has Been Recieved in the doPost Method...");

		/* Reading parameters' values from web.xml file */

		/**

		 * URL of deployed PDF Signer Server

		 */

		String ADSS_URL = "82.221.28.123/adss";//getServletContext().getInitParameter("ADSS_URL");

		Enumeration<String> pamarms = getServletContext().getInitParameterNames();
		
		/**

		 * URL of deployed PDF Signer Server, where Document Hashing's request(s) will be sent

		 */

		String HASHING_URL = ADSS_URL + "/dhi";

		/**

		 * URL of deployed PDF Signer Server, where Signature Assembly's request(s) will be sent

		 */

		String ASSEMBLY_URL = ADSS_URL + "/sai";

		/**

		 * User name, registered on PDF Signer Server

		 */

		String ORIGINATOR_ID = getServletContext().getInitParameter(

		"ORIGINATOR_ID");

		/**

		 * Signing profile id that will be used to sign the existing user's blank signature field

		 */

		String USER_PROFILE_ID = getServletContext().getInitParameter(

		"USER_PROFILE_ID");

		/* Printing the parameters' values on console */

		System.out.println("HASHING_URL : " + HASHING_URL);

		System.out.println("ASSEMBLY_URL : " + ASSEMBLY_URL);

		System.out.println("ORIGINATOR_ID : " + ORIGINATOR_ID);

		System.out.println("USER_PROFILE_ID : " + USER_PROFILE_ID);

		/**

		 * variable that is used to detect, the server has been hit first or second time

		 */

		boolean isFirstTime = false;

		/**

		 *  Signing reason, that will appear in user's signature

		 */

		String str_signingReason = null;

		/**

		 *  Signing location, that will appear in user's signature

		 */

		String str_signingLocation = null;

		/**

		 *  Contact details, that will appear in user's signature

		 */

		String str_contactInfo = null;

		/**

		 * Certificate that will be used for producing user's signature

		 */

		String str_certificate = null;

		/**

		 * Name of pdf document, that will be signed using the above details

		 */

		String str_targetPDF = null;

		/**

		 * PKCS#7 bytes

		 */

		byte[] b_pkcs7 = null;

		/**

		 * Http session, used to keep the pdf document's id in record during the complete signing request

		 */

		HttpSession obj_session = null;

		/**

		 * Variable that is used to detect, the response from the PDF Signer Server is successfull or not

		 */

		boolean isResponseSuccessfull = false;

		try {

			/* The midware web application has been hit/called first time  */

			if (a_objRequest.getParameter("serverHit") != null &&

			a_objRequest.getParameter("serverHit").equalsIgnoreCase("FIRST")) {

				isFirstTime = true;

				/* Reading the parameters' values from http request and printing them on console */

				str_signingReason = a_objRequest.getParameter(

				"txt_signingReason");

				System.out.println("str_signingReason: " + str_signingReason);

				str_signingLocation = a_objRequest.getParameter("txt_location");

				System.out.println("str_signingLocation: " +

				str_signingLocation);

				str_contactInfo = a_objRequest.getParameter(

				"txt_contactDetails");

				System.out.println("str_contactInfo: " + str_contactInfo);

				str_targetPDF = a_objRequest.getParameter("hdn_targetPDF");

				System.out.println("str_targetPDF: " + str_targetPDF);

				str_certificate = a_objRequest.getParameter("certificate");

				/* Sometimes when user's selected certificate(Base 64 encoded) is posted on http request,

				 '+' character is converted into ' ' character, so replacing it back with the original

				 character ' ' to get the original certificate's bytes */

				str_certificate = str_certificate.replaceAll(" ", "+");

				System.out.println("str_certificate: " + str_certificate);

			}

			// Get the body of the HTTP request

			System.out.println("Ready to download contents having length = " +

			a_objRequest.getContentLength() + " bytes");

			/**

			 * Response that will be recieved from PDF Signer Server

			 */

			byte[] byte_soapResponseBytes = null;

			/**

			 * Getting the path of gosign.properties file, that is a mandatory part of the web application

			 */
			
			FacesContext fctx = WFUtil.createFacesContext(a_objRequest.getSession().getServletContext(), a_objRequest, a_objResponse);
			IWContext iwc = IWContext.getIWContext(fctx);
			

			//IWBundleResourceFilter.checkCopyOfResourceToWebapp(fctx, resourceURI);*/
			
			
			
			/*String str_path = getClass().getClassLoader().getResource(

			"company.pdf").toString();*/ 
			

			IWBundle bundle = iwc.getIWMainApplication().getBundle("com.idega.ascertia");
			InputStream is = bundle.getResourceInputStream("resources/company.pdf");
			
			
			byte byte_file[];
	        //FileInputStream obj_fis = new FileInputStream(is);
	        byte_file = new byte[is.available()];
	        is.read(byte_file);
	        is.close();
			
			/*str_path = str_path.substring(6, str_path.length() - 34);

			str_path = str_path.replaceAll("\"", "/");

			str_path = str_path + "/pages/pdfs";*/

			String str_errorMessage = "Error message hasn't been set properly.";

			/* Web application has been called first time, for Document Hash */

			if (isFirstTime) {

				System.out.println("Server has been hit first time.");

				obj_session = a_objRequest.getSession(true);

				/* Constructing request for document hashing */

				DocumentHashingRequest obj_documentHashingRequest = new DocumentHashingRequest(

				ORIGINATOR_ID, USER_PROFILE_ID,

				/*str_path + "/" +*/  byte_file /*str_targetPDF + ".pdf"*/,

				Base64.decode(str_certificate));

				//obj_documentHashingRequest.overrideProfileAttribute(

				//SigningRequest.SIGNING_REASON, str_signingReason);

				//obj_documentHashingRequest.overrideProfileAttribute(

				//SigningRequest.SIGNING_LOCATION, str_signingLocation);

				//obj_documentHashingRequest.overrideProfileAttribute(

				//SigningRequest.CONTACT_INFO, str_contactInfo);

				/* Sending request to the ADSS server */

				DocumentHashingResponse obj_documentHashingResponse = (

				DocumentHashingResponse) obj_documentHashingRequest.

				send(HASHING_URL);

				if (obj_documentHashingResponse.isResponseSuccessfull()) {

					isResponseSuccessfull = true;

					byte_soapResponseBytes = obj_documentHashingResponse.

					getDocumentHash();

					obj_session.setAttribute("DocumentId",

					obj_documentHashingResponse.

					getDocumentId());

				} else {

					str_errorMessage = obj_documentHashingResponse.

					getErrorMessage();

				}

			}

			/* Web application has been called second time, for Signature Assembly */

			else {

				obj_session = a_objRequest.getSession(false);

				/* Input stream that reads the PKCS#7 bytes, that has been calculated by GoSign applet */

				InputStream obj_inputStream = a_objRequest.getInputStream();

				ByteArrayOutputStream obj_bos = new ByteArrayOutputStream();

				byte[] byte_buffer = new byte[128];

				int i_read = 0;

				while ((i_read = obj_inputStream.read(byte_buffer)) > 0) {

					System.out.println("Read : " + i_read);

					obj_bos.write(byte_buffer, 0, i_read);

				}

				/* PKCS#7 bytes */

				b_pkcs7 = obj_bos.toByteArray();

				String str_docId = (String) obj_session.getAttribute(

				"DocumentId");

				String str_signedDocPath = /*str_path +*/ "/" + str_docId + ".pdf";

				/* Constructing request for signature assembly */

				SignatureAssemblyRequest obj_signatureAssemblyRequest = new SignatureAssemblyRequest(

				ORIGINATOR_ID, b_pkcs7,

				(String) obj_session.getAttribute("DocumentId"));

				/* Sending request to the ADSS server */

				SignatureAssemblyResponse obj_signatureAssemblyResponse = (

				SignatureAssemblyResponse) obj_signatureAssemblyRequest.

				send(ASSEMBLY_URL);

				/* Setting dummy bytes */

				byte_soapResponseBytes = new byte[] { '1', '2', '3' };

				if (obj_signatureAssemblyResponse.isResponseSuccessfull()) {

					//obj_signatureAssemblyResponse.writeSignedPDFTo(

//					str_signedDocPath);

					byte [] signedDoc = obj_signatureAssemblyResponse.getSignedDocument();
					isResponseSuccessfull = true;

				} else {

					str_errorMessage = obj_signatureAssemblyResponse.

					getErrorMessage();

				}

			}

			/* Sending the response back to the GoSign applet */

			if (isResponseSuccessfull) {

				/* Setting response headers */

				a_objResponse.setStatus(HttpServletResponse.SC_OK);

				a_objResponse.setContentType(CONTENT_TYPE);

				a_objResponse.setContentLength(byte_soapResponseBytes.length);

				/* Writing message on the response output stream */

				OutputStream obj_os = a_objResponse.getOutputStream();

				obj_os.write(byte_soapResponseBytes, 0,

				byte_soapResponseBytes.length);

				obj_os.flush();

				obj_os.close();

			} else {

				System.out.println("Response Failed...");

				if (isFirstTime) {

					System.out.println("Hash Response Message : " +

					str_errorMessage);

					a_objResponse.sendError(HttpServletResponse.

					SC_INTERNAL_SERVER_ERROR,

					str_errorMessage);

				} else {

					System.out.println("Assembly Response Message : " +

					str_errorMessage);

					a_objResponse.sendError(HttpServletResponse.

					SC_INTERNAL_SERVER_ERROR,

					str_errorMessage);

				}

			}

		} catch (Exception ex) {

			ex.printStackTrace();

			throw new ServletException("Unable to receive request : " +

			ex.getMessage());

		}

	}

}
