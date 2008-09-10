// Copyright (C) 2001-2007 Ascertia
// email: support@ascertia.com
// All rights reserved.
//
// This source has to be kept in strict confidence and must not be disclosed to any third party in any case

// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE


package com.ascertia.adss.samples.verification.historical;


import java.util.Calendar;

import com.ascertia.adss.client.api.SignatureInfo;
import com.ascertia.adss.client.api.SignatureVerificationRequest;
import com.ascertia.adss.client.api.VerificationRequest;
import com.ascertia.adss.client.api.VerificationResponse;
import com.ascertia.adss.client.api.VerifyInfo;

/**
 *
 * <p>Title: VerifyPKCS7</p>
 *
 * <p>Description: This class describes that how a PKCS#7 signature with historical validation can be verified using the ADSS client API.</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: Ascertia Ltd.</p>
 *
 * @author MH
 * @version 1.0
 */

public class VerifyPKCS7 {

  public static void main(String[] args) {

      /* if insufficient/inappropriate command line parameter(s) are provided.*/
      if (args.length != 2) {
      System.out.println(" Insufficient Command Line Paremeters. ");
      System.out.println(" Usage : ");
      System.out.println(" args[0] - Input File name for Verification. ");
      System.out.println(" args[1] - ADSS Server Address. e.g http://localhost:8777 ");
      System.exit(0);
    }

    try {
      /* Constructing request for pdf signature verification */
      SignatureVerificationRequest obj_signatureVerificationRequest = new SignatureVerificationRequest("PKCS7_Type_RT-C1_S1-1024-C1", "samples_test_client");
      obj_signatureVerificationRequest.setCertificateQualityLevel("5");
      obj_signatureVerificationRequest.setSignatureQualityLevel("5");
      obj_signatureVerificationRequest.addRespondWithItem(VerificationRequest.RESPOND_WITH_BASIC_CONSTRAINTS);
      obj_signatureVerificationRequest.addRespondWithItem(VerificationRequest.RESPOND_WITH_KEY_USAGE);
      obj_signatureVerificationRequest.addRespondWithItem(VerificationRequest.RESPOND_WITH_VALID_FROM);
      obj_signatureVerificationRequest.addRespondWithItem(VerificationRequest.RESPOND_WITH_VALID_TO);
      obj_signatureVerificationRequest.addRespondWithItem(VerificationRequest.RESPOND_WITH_ISSUER_NAME);

      /* Setting historical validation */
      obj_signatureVerificationRequest.setHistoricalValidation( Calendar.getInstance() );

      /* Constructing signature element so that it can be added into the signature verification request */
      SignatureInfo obj_signatureInfo = new SignatureInfo("signature.id.001", args[0].trim(), SignatureInfo.SIGNED_DOCUMENT_TYPE_PKCS7);
      obj_signatureInfo.setSignatureFormat(SignatureInfo.SIGNATURE_FORMAT_OTHER);
      obj_signatureInfo.addRespondWithItem(VerificationRequest.RESPOND_WITH_BASIC_CONSTRAINTS);
      obj_signatureInfo.addRespondWithItem(VerificationRequest.RESPOND_WITH_KEY_USAGE);
      obj_signatureInfo.addRespondWithItem(VerificationRequest.RESPOND_WITH_VALID_FROM);
      obj_signatureInfo.addRespondWithItem(VerificationRequest.RESPOND_WITH_VALID_TO);
      obj_signatureInfo.addRespondWithItem(VerificationRequest.RESPOND_WITH_ISSUER_NAME);

      /* Adding signature element(s) into the signature verification request */
      obj_signatureVerificationRequest.addSignatureInfo(obj_signatureInfo);
      /* Writing request to disk */
      obj_signatureVerificationRequest.writeTo("../data/verification/VerifyPKCS7_historical-request.xml");

      System.out.println("\n/**********************************************************************/");
      System.out.println("\nA request has been sent to verify the PDF signature. Waiting for response...");

      /* Sending the above constructed request to the ADSS server */
      VerificationResponse obj_verificationResponse = (VerificationResponse) obj_signatureVerificationRequest.send(args[1].trim() + "/adss/verification/svi");
      /* Writing response to disk */
      obj_verificationResponse.writeTo("../data/verification/VerifyPKCS7_historical-response.xml");

      /* Parsing the response */
      if (obj_verificationResponse.isResponseSuccessfull()) {
        System.out.println("\nRequest has been processed successfully.");
        System.out.println("\nResponse Id : " + obj_verificationResponse.getRequestId());
        System.out.println("Response Type : " + obj_verificationResponse.getResponseType());
        System.out.println("Historical Validation : " + ((VerifyInfo)obj_verificationResponse.getVerifyInfo().get(0)).getHistoricalValidation());
      }
      else {
        System.out.println("\nRequest hasn't been processed successfully.");
        System.out.println("\nError Code : " + obj_verificationResponse.getErrorCode());
        System.out.println("Result Minor : " + obj_verificationResponse.getResultMinor());
      }
      System.out.println("\n/**********************************************************************/");

    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
