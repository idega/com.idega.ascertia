package com.ascertia.forms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ascertia.adss.client.api.EmptySignatureFieldRequest;
import com.ascertia.adss.client.api.EmptySignatureFieldResponse;
import com.ascertia.adss.client.api.SigningRequest;


/**
 *
 * <p>Title: ASC_GoSignAction</p>
 *
 * <p>Description: This class fills the pdf form with the user's provided data and produces the company signature on it.</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Ascertia Ltd.</p>
 *
 * <p>Note: html form and the pdf form must have same fields' names</p>
 *
 * @author MH
 * @version 1.0
 */

public class ASC_GoSignAction extends DispatchAction {

    /**
     * This method fills the pdf form with the user's provided data
     * @param mapping ActionMapping
     * @param form ActionForm
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward fillPDF(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        /**
         * ASC_GoSignActionForm object, that contains user's provided data
         */
        ASC_GoSignActionForm obj_GSActionForm = (ASC_GoSignActionForm) form;

        /* Reading parameters' values from web.xml file */

        /**
         * User name, registered on PDF Signer Server
         */
        String ORIGINATOR_ID = getServlet().getServletContext().
                               getInitParameter("ORIGINATOR_ID");

        /**
         * Key alias configured with the user registered on the PDF Signer Server and that will be used
         *  for producing the company signature on the pdf document
         */
        String COMPANY_KEY_ALIAS = getServlet().getServletContext().
                                   getInitParameter("COMPANY_KEY_ALIAS");
        /**
         * Password of the key, explained above
         */
        String COMPANY_KEY_PASSWORD = getServlet().getServletContext().
                                      getInitParameter("COMPANY_KEY_PASSWORD");
        /**
         * Signing profile id that will be used to sign the existing company's blank signature field
         */
        String COMPANY_PROFILE_ID = getServlet().getServletContext().
                                    getInitParameter("COMPANY_PROFILE_ID");

        /**
         * Signing profile id that will be used to create the blank signature field(s) on the pdf document
         */
        String EMPTY_SIGFIELD_PROFILE_ID = getServlet().getServletContext().
                                           getInitParameter(
                "EMPTY_SIGFIELD_PROFILE_ID");

        /**
         * URL of deployed PDF Signer Server
         */
        String ADSS_URL = getServlet().getServletContext().getInitParameter(
                "ADSS_URL");
        /**
         * URL of deployed PDF Signer Server where xml request for producing blank signatures(one for company signature and other for user signature) and signing of existing company's blank signature, will be sent.
         */
        String EMPTY_SIGFIELD_URL = ADSS_URL + "/esi";

        /* Printing parameters' values on the console */
        System.out.println("ASC_GoSignAction->ORIGINATOR_ID : " +
                           ORIGINATOR_ID);
        System.out.println("ASC_GoSignAction->COMPANY_KEY_ALIAS : " +
                           COMPANY_KEY_ALIAS);
        System.out.println("ASC_GoSignAction->COMPANY_KEY_PASSWORD : " +
                           COMPANY_KEY_PASSWORD);
        System.out.println("ASC_GoSignAction->COMPANY_PROFILE_ID : " +
                           COMPANY_PROFILE_ID);
        System.out.println("ASC_GoSignAction->EMPTY_SIGFIELD_URL : " +
                           EMPTY_SIGFIELD_URL);
        System.out.println("ASC_GoSignAction->EMPTY_SIGFIELD_PROFILE_ID : " +
                           EMPTY_SIGFIELD_PROFILE_ID);

        /* Hashtable containing user's provided data, in the form of key-value pair, where key is the
          name of html/pdf form field */
        Hashtable obj_ht = new Hashtable();
        Class c = obj_GSActionForm.getClass();
        Method[] methods = c.getDeclaredMethods();
        Field[] fields = c.getDeclaredFields();

        /* Populating the hashtable */
        for (int i = 0; i < fields.length; i++) {
            obj_ht.put(fields[i].getName(),
                       getPropertyValue(fields[i].getName(), obj_GSActionForm));
        }

        /* Getting the path of gosign.properties file, that is a mandatory part of the web application */
        String str_path = obj_GSActionForm.getClass().getClassLoader().
                          getResource(
                                  "gosign.properties").toString();

        /* Getting the path of the pdf document that is to be signed */
        str_path = str_path.substring(6, str_path.length() - 34);
        str_path = str_path.replaceAll("\"", "/");
        str_path = str_path + "/pages/pdfs";
        byte[] input_pdf = ASC_PDFFormFiller.getContents(new File(str_path +
                "/input.pdf"));

        /* Creating the ASC_PDFFormFiller object and initializing it using the to be signed document */
        ASC_PDFFormFiller obj_pdfFormFiller = new ASC_PDFFormFiller(input_pdf);

        /* Id of the to be signed document is unique and generated on random basis */
        String str_documentId = "" + System.currentTimeMillis();

        HttpSession obj_session = request.getSession(true);
        /* Saving the document id in the http session */
        obj_session.setAttribute("str_documentId", str_documentId);

        if (obj_pdfFormFiller.fillPDFData(obj_ht, false)) {

            /* if pdf form filling is successfull */

            /* Document containing filled pdf form */
            byte[] filled_pdf = obj_pdfFormFiller.getFilledPDF();

            /* Constructing request for blank signature(s) creation */
            EmptySignatureFieldRequest obj_emptySigFieldRequest = new
                    EmptySignatureFieldRequest(ORIGINATOR_ID,
                                               EMPTY_SIGFIELD_PROFILE_ID,
                                               filled_pdf);
            obj_emptySigFieldRequest.setSigningInfo(COMPANY_PROFILE_ID,
                    COMPANY_KEY_ALIAS);
            obj_emptySigFieldRequest.setPKCS12Password(COMPANY_KEY_PASSWORD);

            /* Getting path of the default images' directory */
            obj_emptySigFieldRequest.overrideProfileAttribute(SigningRequest.COMPANY_LOGO, readResourceStream("Sales-Company.jpg"));
            /* Sending request to the ADSS server */
            EmptySignatureFieldResponse obj_emptySigFieldResponse = ( EmptySignatureFieldResponse) obj_emptySigFieldRequest.send(EMPTY_SIGFIELD_URL);
            if (obj_emptySigFieldResponse.isResponseSuccessfull()) {
                byte[] byte_resultantDoc = obj_emptySigFieldResponse.
                                           getSignedDocument();
                ASC_PDFFormFiller.setContents(new File(str_path + "/" +
                        str_documentId +
                        ".pdf"),
                                              byte_resultantDoc);
            } else {
                System.out.println(obj_emptySigFieldResponse.getErrorMessage());
            }
        }
        return mapping.findForward("sign_pdf");
    }

    /**
     * This method returns the values of the corresponding properties of ASC_GoSignActionForm object
     * @param name String Property name whose value is to be returned
     * @param obj Object
     * @return Object
     * @throws Exception
     */
    public Object getPropertyValue(String name, Object obj) throws Exception {
        String prop = Character.toUpperCase(name.charAt(0)) +
                      name.substring(1);
        String mname = "get" + prop;
        Class[] types = new Class[] {};
        Method method = obj.getClass().getMethod(mname, types);
        Object result = method.invoke(obj, new Object[0]);

        return result;
    }

    public Properties getResourceBundle(String a_strModule) throws Exception {
        Properties obj_props = new Properties();
        try {
            obj_props.load((new ASC_GoSignAction()).getClass().getResourceAsStream("/" + a_strModule + ".properties"));
            return obj_props;
        } catch (Exception ex) {
            throw new Exception(ex);
        }
    }

    public byte[] readResourceStream(String a_strResource) throws Exception {
        InputStream obj_is = getClass().getResourceAsStream(a_strResource);
        ByteArrayOutputStream obj_baos = new ByteArrayOutputStream();
        byte[] byte_buffer = new byte[1024];
        int i_read = 0;
        while ( (i_read = obj_is.read(byte_buffer)) != -1 ) {
          obj_baos.write(byte_buffer, 0, i_read);
        }
        return obj_baos.toByteArray();
    }



}






