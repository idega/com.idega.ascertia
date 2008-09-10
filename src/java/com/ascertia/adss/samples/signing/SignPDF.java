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

import com.ascertia.adss.client.api.SigningRequest;
import com.ascertia.adss.client.api.SigningResponse;

/**
 *
 * <p>Title: SignPDF</p>
 *
 * <p>Description: This class describes that how a pdf document can be signed using the ADSS client API.</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: Ascertia Ltd.</p>
 *
 * @author MH
 * @version 1.0
 */

public class SignPDF {

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

    /* Constructing request for pdf signing */
    SigningRequest obj_signingRequest = new SigningRequest("samples_test_client", "samples_test_signing_certificate", args[0].trim(), SigningRequest.MIME_TYPE_PDF);
    obj_signingRequest.setProfileId("adss:signing:profile:001");
    /* Writing request to disk */
      obj_signingRequest.writeTo("../data/signing/SignPDF-request.xml");

    System.out.println("\n/**********************************************************************/");
    System.out.println("\nA request has been sent to sign the PDF. Waiting for response...");

    /* Sending the above constructed request to the ADSS server */
    SigningResponse obj_signingResponse = (SigningResponse) obj_signingRequest.send(args[2].trim() + "/adss/signing/dsi");
    /* Writing response to disk */
      obj_signingResponse.writeTo("../data/signing/SignPDF-response.xml");

    /* Parsing the response */
    if (obj_signingResponse.isResponseSuccessfull()) {
      obj_signingResponse.writeSignedPDFTo(args[1].trim());
      System.out.println("\nRequest has been processed successfully.");
    }
    else {
      System.out.println(obj_signingResponse.getErrorMessage());
    }
    System.out.println("\n/**********************************************************************/");
  }
}

