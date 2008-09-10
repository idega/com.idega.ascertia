// Copyright (C) 2001-2007 Ascertia
// email: support@ascertia.com
//
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
// SUCH DAMAGE.


package com.ascertia.adss.samples.shared.certification;

import com.ascertia.adss.client.api.*;

/**
 *
 * <p>Title: GenerateCertificate</p>
 *
 * <p>Description: This class describes that how we can get certificate(s) from the ADSS certification service using the ADSS client API.</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: Ascertia Ltd.</p>
 *
 * @author MH
 * @version 1.0
 */

public class GenerateCertificate {

  public static void main(String[] args) {

    /* if insufficient/inappropriate command line parameter(s) are provided.*/
    if (args.length < 1) {
      System.out.println("No Command Line Parameter provided");
    }
    if (args.length > 1) {
      System.out.println("Insufficient Command Line parameter");
      System.out.println("Usage: ");
      System.out.println("args[0] - ADSS Server Address like http://localhost:8777");
      System.exit(0);
    }

    /* Constructing request for getting certificate(s) from the ADSS certification service */
    CertificationRequest obj_certificationRequest = new CertificationRequest("samples_test_client", CertificationRequest.REQUEST_TYPE_CREATE_CERTIFICATE, "Test Certificate");
    obj_certificationRequest.setProfileId("adss:certification:profile:001");
    obj_certificationRequest.setPKCS12Password("password");
    obj_certificationRequest.addRespondWithItem(CertificationRequest.RESPOND_WITH_CERTIFICATE);
    obj_certificationRequest.addRespondWithItem(CertificationRequest.RESPOND_WITH_PKCS_12);
    obj_certificationRequest.addRespondWithItem(CertificationRequest.RESPOND_WITH_PKCS_7);
    obj_certificationRequest.addRespondWithItem(CertificationRequest.RESPOND_WITH_EXPIRY_DATE);
    obj_certificationRequest.overrideProfileAttribute(CertificationRequest.SUBJECT_DN, "CN=Alice");
    /* Writing request to disk */
      obj_certificationRequest.writeTo("../data/shared/certification/GenerateCertificate-request.xml");

    System.out.println("\n/**********************************************************************/");
    System.out.println("\nA request has been sent to get certificate. Waiting for response...");
    /* Sending the above constructed request to the ADSS server */
    CertificationResponse obj_certificationResponse = (CertificationResponse) obj_certificationRequest.send(args[0].trim() + "/adss/certification/csi");
    /* Writing response to disk */
      obj_certificationResponse.writeTo("../data/shared/certification/GenerateCertificate-response.xml");

    /* Parsing the response */
    if (obj_certificationResponse.isResponseSuccessfull()) {
      obj_certificationResponse.writeCertificateTo("../data/shared/certification/certificate.cer");
      obj_certificationResponse.writePKCS12To("../data/shared/certification/certificate.pfx");
      obj_certificationResponse.writePKCS7To("../data/shared/certification/certificate.p7b");
      System.out.println("\nRequest has been processed successfully.");
    }
    else {
      System.out.println(obj_certificationResponse.getErrorMessage());
    }
    System.out.println("\n/**********************************************************************/");
  }
}


