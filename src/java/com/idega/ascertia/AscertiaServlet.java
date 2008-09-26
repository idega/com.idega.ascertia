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
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bouncycastle.util.encoders.Base64;

import com.ascertia.adss.client.api.DocumentHashingRequest;
import com.ascertia.adss.client.api.DocumentHashingResponse;
import com.ascertia.adss.client.api.EmptySignatureFieldRequest;
import com.ascertia.adss.client.api.EmptySignatureFieldResponse;
import com.ascertia.adss.client.api.SignatureAssemblyRequest;
import com.ascertia.adss.client.api.SignatureAssemblyResponse;
import com.ascertia.adss.client.api.SigningRequest;

public class AscertiaServlet extends HttpServlet {
	/**
	 * 
	 * Contents' type of http response
	 */

	private static final String CONTENT_TYPE = "text/xml";

	// Initialize global variables

	public void init(ServletConfig a_objServletConfig) throws ServletException {

		super.init(a_objServletConfig);

		System.out.println("Asctertia servlet started");

	}

	// Process the HTTP Get request

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		System.out.println("GoSign Request Has Been Recieved in the doGet Method...");

		response.setContentType("text/html");

		getServletContext().getRequestDispatcher("/gosign.html").include(request, response);

	}

	// Process the HTTP Post request

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		System.out.println("GoSign Request Has Been Recieved in the doPost Method...");

		/* Reading parameters' values from web.xml file */

		/**
		 * 
		 * URL of deployed PDF Signer Server
		 */

		String ADSS_URL = "http://82.221.28.123/adss/signing/";// getServletContext
		// ().
		// getInitParameter
		// ("ADSS_URL");
		/**
		 * 
		 * URL of deployed PDF Signer Server, where Document Hashing's request(s) will be sent
		 */

		String HASHING_URL = ADSS_URL + "dhi";

		/**
		 * 
		 * URL of deployed PDF Signer Server, where Signature Assembly's request(s) will be sent
		 */

		String ASSEMBLY_URL = ADSS_URL + "sai";

		/**
		 * 
		 * User name, registered on PDF Signer Server
		 */

		String ORIGINATOR_ID = getServletContext().getInitParameter("ORIGINATOR_ID");

		/**
		 * 
		 * Signing profile id that will be used to sign the existing user's blank signature field
		 */

		String USER_PROFILE_ID = "adss:signing:profile:007";// getServletContext(
		// )
		// .getInitParameter
		// (
		// "USER_PROFILE_ID"
		// );

		/* Printing the parameters' values on console */

		System.out.println("HASHING_URL : " + HASHING_URL);

		System.out.println("ASSEMBLY_URL : " + ASSEMBLY_URL);

		System.out.println("ORIGINATOR_ID : " + ORIGINATOR_ID);

		System.out.println("USER_PROFILE_ID : " + USER_PROFILE_ID);

		/**
		 * 
		 * variable that is used to detect, the server has been hit first or second time
		 */

		boolean isFirstTime = false;

		/**
		 * 
		 * Certificate that will be used for producing user's signature
		 */

		String certificate = null;

		/**
		 * 
		 * PKCS#7 bytes
		 */

		byte[] b_pkcs7 = null;

		/**
		 * 
		 * Http session, used to keep the pdf document's id in record during the complete signing request
		 */

		HttpSession session = null;

		// String adss_signing_profile = null;

		/**
		 * 
		 * Variable that is used to detect, the response from the PDF Signer Server is successfull or not
		 */

		boolean isResponseSuccessfull = false;

		try {

			/* The midware web application has been hit/called first time */

			if (request.getParameter("serverHit") != null &&

			request.getParameter("serverHit").equalsIgnoreCase("FIRST")) {

				isFirstTime = true;

				/*
				 * Reading the parameters' values from http request and printing them on console
				 */

				certificate = request.getParameter("certificate");

				/*
				 * Sometimes when user's selected certificate(Base 64 encoded) is posted on http request,
				 * 
				 * '+' character is converted into ' ' character, so replacing it back with the original
				 * 
				 * character ' ' to get the original certificate's bytes
				 */

				certificate = certificate.replaceAll(" ", "+");

				System.out.println("str_certificate: " + certificate);

			}

			// Get the body of the HTTP request

			System.out.println("Ready to download contents having length = " +

			request.getContentLength() + " bytes");

			/**
			 * 
			 * Response that will be recieved from PDF Signer Server
			 */

			byte[] soapResponseBytes = null;

			byte rawPdfFile[];

			String errorMessage = "Error message hasn't been set properly.";

			/* Web application has been called first time, for Document Hash */

			if (isFirstTime) {

				// getting pdf from url
				String documentURL = request.getParameter(AscertiaConstants.DOCUMENT_URL);

				URL url = new URL(documentURL);
				InputStream inputStream = url.openStream();
				rawPdfFile = new byte[inputStream.available()];
				inputStream.read(rawPdfFile);

				System.out.println("Server has been hit first time.");

				// Getting empty signature field
				byte[] pdfFileWithEmptySignature = null;

				EmptySignatureFieldRequest emptySigFieldRequest = new EmptySignatureFieldRequest("samples_test_client", "adss:signing:profile:005", rawPdfFile);	
				emptySigFieldRequest.overrideProfileAttribute(SigningRequest.SIGNING_REASON, "Testing");
				System.out.println("\nA request has been sent to create blank signature(s) on the PDF. Waiting for response...");

				String EMPTY_SIGNATURE_URL = ADSS_URL + "esi";

				/* Sending the above constructed request to the ADSS server */
				EmptySignatureFieldResponse emptySigFieldResponse = (EmptySignatureFieldResponse) emptySigFieldRequest.send(EMPTY_SIGNATURE_URL);

				System.out.println("EMPTY_SIGNATURE_URI: " + EMPTY_SIGNATURE_URL);

				if (emptySigFieldResponse.isResponseSuccessfull()) {
					pdfFileWithEmptySignature = emptySigFieldResponse.getSignedDocument();
					isResponseSuccessfull = true;
					System.out.println("\nRequest has been processed successfully.");
				} else {
					isResponseSuccessfull = false;
					System.out.println(emptySigFieldResponse.getErrorMessage());
				}
				
				if (isResponseSuccessfull) {
					isResponseSuccessfull = false;
					session = request.getSession(true);

					/* Constructing request for document hashing */

					DocumentHashingRequest documentHashingRequest = new DocumentHashingRequest(ORIGINATOR_ID, USER_PROFILE_ID, pdfFileWithEmptySignature, Base64.decode(certificate));

					documentHashingRequest.overrideProfileAttribute(SigningRequest.SIGNING_REASON, "asdasd");

					documentHashingRequest.overrideProfileAttribute(SigningRequest.SIGNING_LOCATION, "location");

					documentHashingRequest.overrideProfileAttribute(SigningRequest.CONTACT_INFO, "asd");

					/* Sending request to the ADSS server */

					DocumentHashingResponse documentHashingResponse = (DocumentHashingResponse) documentHashingRequest.send(HASHING_URL);

					if (documentHashingResponse.isResponseSuccessfull()) {

						isResponseSuccessfull = true;

						soapResponseBytes = documentHashingResponse.getDocumentHash();

						session.setAttribute("DocumentId",

						documentHashingResponse.getDocumentId());
						System.out.println("Document hashing was successfull");

					} else {
						isResponseSuccessfull = false;
						errorMessage = documentHashingResponse.getErrorMessage();

					}
				}

			}

			/*
			 * Web application has been called second time, for Signature Assembly
			 */

			else {

				System.out.println("Reading signature bytes");

				session = request.getSession(false);

				/*
				 * Input stream that reads the PKCS#7 bytes, that has been calculated by GoSign applet
				 */

				InputStream obj_inputStream = request.getInputStream();
				ByteArrayOutputStream obj_bos = new ByteArrayOutputStream();
				byte[] byte_buffer = new byte[128];
				int i_read = 0;
				while ((i_read = obj_inputStream.read(byte_buffer)) > 0) {
					System.out.println("Read : " + i_read);
					obj_bos.write(byte_buffer, 0, i_read);

				}

				/* PKCS#7 bytes */

				b_pkcs7 = obj_bos.toByteArray();
				String str_docId = (String) session.getAttribute("DocumentId");

				String str_signedDocPath = /* str_path + */str_docId + "last1.pdf";

				/* Constructing request for signature assembly */

				SignatureAssemblyRequest signatureAssemblyRequest = new SignatureAssemblyRequest(ORIGINATOR_ID, b_pkcs7, (String) session.getAttribute("DocumentId"));

				/* Sending request to the ADSS server */

				SignatureAssemblyResponse signatureAssemblyResponse = (SignatureAssemblyResponse) signatureAssemblyRequest.send(ASSEMBLY_URL);

				/* Setting dummy bytes */

				soapResponseBytes = new byte[] { '1', '2', '3' };

				if (signatureAssemblyResponse.isResponseSuccessfull()) {
					signatureAssemblyResponse.writeSignedPDFTo(str_signedDocPath);
					byte[] signedDoc = signatureAssemblyResponse.getSignedDocument();
					isResponseSuccessfull = true;
					System.out.println("Documend successfully signed");

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
				outputStream.write(soapResponseBytes, 0, soapResponseBytes.length);

				outputStream.flush();
				outputStream.close();

			} else {

				System.out.println("Response Failed...");

				if (isFirstTime) {

					System.out.println("Hash Response Message : " + errorMessage);

					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);

				} else {

					System.out.println("Assembly Response Message : " + errorMessage);
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);

				}

			}

		} catch (Exception ex) {

			ex.printStackTrace();

			throw new ServletException("Unable to receive request : " + ex.getMessage());

		}

	}

}
