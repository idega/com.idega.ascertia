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

import com.ascertia.adss.client.api.CertificateInfo;
import com.ascertia.adss.client.api.CertificateValidationRequest;
import com.ascertia.adss.client.api.VerificationRequest;
import com.ascertia.adss.client.api.VerificationResponse;
import com.ascertia.adss.client.api.VerifyInfo;

/**
 *
 * <p>Title: VerifyCertificate</p>
 *
 * <p>Description: This class describes that how a certificate with historical validation can be validated using the ADSS client API.</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: Ascertia Ltd.</p>
 *
 * @author MH
 * @version 1.0
 */

public class VerifyCertificate {

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
        /* Constructing request for certificate validation */
      CertificateValidationRequest obj_certificateValidationRequest = new CertificateValidationRequest("X509_RT-C1_CT1", "samples_test_client");
      obj_certificateValidationRequest.setCertificateQualityLevel("5");
      obj_certificateValidationRequest.setSignatureQualityLevel("5");
      obj_certificateValidationRequest.addRespondWithItem(VerificationRequest.RESPOND_WITH_BASIC_CONSTRAINTS);
      obj_certificateValidationRequest.addRespondWithItem(VerificationRequest.RESPOND_WITH_KEY_USAGE);
      obj_certificateValidationRequest.addRespondWithItem(VerificationRequest.RESPOND_WITH_VALID_FROM);
      obj_certificateValidationRequest.addRespondWithItem(VerificationRequest.RESPOND_WITH_VALID_TO);
      obj_certificateValidationRequest.addRespondWithItem(VerificationRequest.RESPOND_WITH_ISSUER_NAME);

      /* Setting historical validation */
      obj_certificateValidationRequest.setHistoricalValidation( Calendar.getInstance() );
      /* Constructing certificate element so that it can be added into the certificate validaiton request */
      CertificateInfo obj_certificateInfo = new CertificateInfo("cert.id.001", CertificateInfo.CERTIFICATE_TYPE_X509, args[0].trim());
      /* Adding certificate(s) into the certificate validaiton request */
      obj_certificateValidationRequest.addCertificateInfo(obj_certificateInfo);
      /* Writing request to disk */
      obj_certificateValidationRequest.writeTo("../data/verification/VerifyCertificate_historical-request.xml");

      System.out.println("\n/**********************************************************************/");
      System.out.println("\nA request has been sent to validate the certificate. Waiting for response...");

      /* Sending the above constructed request to the ADSS server */
      VerificationResponse obj_verificationResponse = (VerificationResponse) obj_certificateValidationRequest.send(args[1].trim() + "/adss/verification/svi");
      /* Writing response to disk */
      obj_verificationResponse.writeTo("../data/verification/VerifyCertificate_historical-response.xml");

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
