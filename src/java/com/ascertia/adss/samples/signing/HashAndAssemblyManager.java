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


package com.ascertia.adss.samples.signing;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import com.ascertia.adss.client.api.DocumentHashingRequest;
import com.ascertia.adss.client.api.DocumentHashingResponse;
import com.ascertia.adss.client.api.SignatureAssemblyRequest;
import com.ascertia.adss.client.api.SignatureAssemblyResponse;
import com.ascertia.adss.client.api.SigningRequest;
import com.ascertia.adss.samples.Util;

/**
 *
 * <p>Title: HashAndAssemblyManager</p>
 *
 * <p>Description: This class describes that how document hashing and signature assembly can be achieved using the ADSS client API.</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: Ascertia Ltd.</p>
 *
 * @author MH
 * @version 1.0
 */

public class HashAndAssemblyManager {

  public static void main(String[] args) {

      /* if insufficient/inappropriate command line parameter(s) are provided.*/
      if (args.length < 3) {
      System.out.println("Insufficient Command Line parameters");
      System.out.println("Usage: ");
      System.out.println("args[0] - Input File name");
      System.out.println("args[1] - Output File name");
      System.out.println("args[2] - ADSS Server Address like http://localhost:8777");
      System.exit(0);
    }

    Util obj_util = new Util();
    byte[] byte_cert = null;
    byte[] b_signature = null;
    KeyPair obj_keyPair = null;
    X509Certificate obj_cert = null;
    try {
      obj_keyPair = obj_util.generateKeyPair();
      obj_cert = obj_util.generateSSCertificate(obj_keyPair);
      byte_cert = obj_cert.getEncoded();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

    /* Constructing request for document hashing */
    DocumentHashingRequest obj_documentHashingRequest = new DocumentHashingRequest("samples_test_client", "adss:signing:profile:001", args[0].trim(), byte_cert);
    obj_documentHashingRequest.overrideProfileAttribute(SigningRequest.SIGNING_REASON, "Testing");
    obj_documentHashingRequest.overrideProfileAttribute(SigningRequest.CONTACT_INFO, "Alice");
    obj_documentHashingRequest.overrideProfileAttribute(SigningRequest.SIGNING_FIELD, "Signature1");
    /* Writing request to disk */
      obj_documentHashingRequest.writeTo("../data/signing/HashAndAssemblyManager-request.xml");

    System.out.println("\n/**********************************************************************/");
    System.out.println("\nA request has been sent to calculate the document hash. Waiting for response...");

    /* Sending the above constructed document hashing request to the ADSS server */
    DocumentHashingResponse obj_documentHashingResponse = (DocumentHashingResponse) obj_documentHashingRequest.send(args[2].trim() + "/adss/signing/dhi");
    /* Writing response to disk */
      obj_documentHashingResponse.writeTo("../data/signing/HashAndAssemblyManager-response.xml");

    /* Parsing the document hashing response */
    if (obj_documentHashingResponse.isResponseSuccessfull()) {
      System.out.println("\nDocument hash recieved successfully.");
      try {
        b_signature = obj_util.createPKCS7(obj_keyPair.getPrivate(), obj_cert, obj_documentHashingResponse.getDocumentHash());
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }

      /* Constructing request for signature assembly */
      SignatureAssemblyRequest obj_signatureAssemblyRequest = new SignatureAssemblyRequest("samples_test_client", b_signature, obj_documentHashingResponse.getDocumentId());
      obj_signatureAssemblyRequest.setProfileId("adss:signing:profile:001");
      System.out.println("\nA request has been sent for signature assembling. Waiting for response...");

      /* Sending the above constructed signature assembly request to the ADSS server */
      SignatureAssemblyResponse obj_signatureAssemblyResponse = (SignatureAssemblyResponse) obj_signatureAssemblyRequest.send(args[2].trim() + "/adss/signing/sai");

      /* Parsing the signature assembly response */
      if (obj_documentHashingResponse.isResponseSuccessfull()) {
        obj_signatureAssemblyResponse.writeSignedPDFTo(args[1].trim());
        System.out.println("\nRequest has been processed successfully.");
      }
      else {
        System.out.println(obj_signatureAssemblyResponse.getErrorMessage());
      }
    }
    else {
      System.out.println(obj_documentHashingResponse.getErrorMessage());
    }
    System.out.println("\n/**********************************************************************/");
  }
}

