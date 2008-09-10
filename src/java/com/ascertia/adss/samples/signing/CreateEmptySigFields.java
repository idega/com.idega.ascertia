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

import com.ascertia.adss.client.api.EmptySignatureFieldRequest;
import com.ascertia.adss.client.api.EmptySignatureFieldResponse;
import com.ascertia.adss.client.api.SigningRequest;

/**
 * <p>Title: CreateEmptySigFields</p>
 *
 * <p>Description: This class describes that how blank signature field(s) can be created on a pdf document using the ADSS client API.</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: Ascertia Ltd.</p>
 *
 * @author MH
 * @version 1.0
 */
public class CreateEmptySigFields {

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

    /* Constructing request for blank signature field(s) creation on the pdf document  */
    EmptySignatureFieldRequest obj_emptySigFieldRequest = new EmptySignatureFieldRequest("samples_test_client", "adss:signing:profile:002", args[0].trim());
    obj_emptySigFieldRequest.setSigningInfo("adss:signing:profile:001", "samples_test_signing_certificate");
    obj_emptySigFieldRequest.overrideProfileAttribute(SigningRequest.SIGNING_REASON, "Testing");
    /* Writing request to disk */
      obj_emptySigFieldRequest.writeTo("../data/signing/CreateEmptySigFields-request.xml");

    System.out.println("\n/**********************************************************************/");
    System.out.println("\nA request has been sent to create blank signature(s) on the PDF. Waiting for response...");

    /* Sending the above constructed request to the ADSS server */
    EmptySignatureFieldResponse obj_emptySigFieldResponse = (EmptySignatureFieldResponse) obj_emptySigFieldRequest.send(args[2].trim() + "/adss/signing/esi");
    /* Writing response to disk */
      obj_emptySigFieldRequest.writeTo("../data/signing/CreateEmptySigFields-response.xml");

    /* Parsing the response */
    if (obj_emptySigFieldResponse.isResponseSuccessfull()) {
      obj_emptySigFieldResponse.writeResultantPDFTo(args[1].trim());
      System.out.println("\nRequest has been processed successfully.");
    }
    else {
      System.out.println(obj_emptySigFieldResponse.getErrorMessage());
    }
    System.out.println("\n/**********************************************************************/");

  }
}



