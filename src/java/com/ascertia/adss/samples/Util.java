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


package com.ascertia.adss.samples;

import java.io.RandomAccessFile;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import javax.security.auth.x500.X500Principal;
import java.util.Calendar;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.io.FileOutputStream;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Transformer;
import javax.xml.soap.SOAPMessage;
import java.io.OutputStream;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Random;
import java.io.File;
import java.security.PrivateKey;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import java.util.ArrayList;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import org.bouncycastle.cms.CMSSignedData;

public class Util {
  /**
   * Constants for different services
   */
  public static final String CERTIFICATION = "shared/certification";
  public static final String VERIFICATION = "verification";
  public static final String SIGNING = "signing";
  public static final String REQUEST = "request";
  public static final String RESPONSE = "response";
  public static final String PDF = ".pdf";
  public static final String CERTIFICATE = ".cer";
  public static final String SIGN = "signing";
  public static final String EMPTY_SIG = "EmptySignature";
  public static final String PDFPREFERENCE = "PDFPreferences";
  public static final String HASH = "Hash";
  public static final String ASSEMBLY = "Assembly";
  public static final String CERT = "certificate";
  public static final String OPERATION_VERIFY_PDF = "verifyPdf";
  public static final String OPERATION_VERIFY_XML_ENVELOPED = "verifyXMLEnveloped";
  public static final String OPERATION_VERIFY_PKCS7 = "verifyPKCS7";
  public static final String OPERATION_VERIFY_CERTIFICATE = "verifyCertificate";
  // for historical verification
  public static final String OPERATION_VERIFY_HISTORICAL_PDF = "verifyHistoricalPdf";
  public static final String OPERATION_VERIFY_HISTORICAL_XML_ENVELOPED = "verifyHistoricalXMLEnveloped";
  public static final String OPERATION_VERIFY_HISTORICAL_PKCS7 = "verifyHistoricalPKCS7";
  public static final String OPERATION_VERIFY_HISTORICAL_CERTIFICATE = "verifyHistoricalCertificate";

  /**
   * SOAP Connection reference
   */
  public SOAPConnection obj_soapCon;
  public String str_fileName = "";
  public Util() {
  }

  /**
   * This method reads the file and returns its bytes.
   * @param a_strFileName String
   * @return byte[]
   */
  public byte[] readFile(String a_strFileName) {
    try {
      File obj_file = new File(a_strFileName);
      str_fileName = obj_file.getName().substring(0, obj_file.getName().indexOf("."));
      RandomAccessFile obj_randomAccessFile = new RandomAccessFile(obj_file, "r");
      byte[] fileData = new byte[ (int) obj_randomAccessFile.length()];
      obj_randomAccessFile.read(fileData);
      obj_randomAccessFile.close();
      return fileData;
    }
    catch (Exception ex) {
      System.out.println("Exception occurred while reading file");
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * This method generates KeyPair
   * @return KeyPair
   */
  public KeyPair generateKeyPair() {
    KeyPair obj_keyPair = null;
    try {
      KeyPairGenerator obj_keyPairGen = KeyPairGenerator.getInstance("RSA");
      obj_keyPair = obj_keyPairGen.generateKeyPair();
    }
    catch (Exception ex) {
      System.out.println("Exception occurred while generating keypair : " + ex.getMessage());
      ex.printStackTrace();
    }
    return obj_keyPair;
  }

  /**
   * This method generates Self Signed Certificate
   * @return X509Certificate
   */
  public X509Certificate generateSSCertificate(KeyPair a_keyPair) {
    KeyPair obj_keyPair = a_keyPair;
    GregorianCalendar obj_date = new GregorianCalendar();
    try {
      Security.insertProviderAt(new BouncyCastleProvider(), 2);
      X509V3CertificateGenerator obj_certGenerator = new X509V3CertificateGenerator();
      X500Principal obj_subjectName = new X500Principal("CN=Rod Crook,O=Ascertia,OU=Sales,C=UK");
      obj_certGenerator.setSerialNumber(BigInteger.valueOf(0x1234L));
      obj_certGenerator.setIssuerDN(obj_subjectName);
      obj_certGenerator.setNotBefore(obj_date.getTime());
      obj_date.add(Calendar.MONTH, 12);
      obj_certGenerator.setNotAfter(obj_date.getTime());
      obj_certGenerator.setSubjectDN(obj_subjectName);
      obj_certGenerator.setPublicKey(obj_keyPair.getPublic());
      obj_certGenerator.setSignatureAlgorithm("SHA1withRSA");

      X509Certificate obj_cert = obj_certGenerator.generateX509Certificate(obj_keyPair.getPrivate());
      return obj_cert;
    }
    catch (Exception ex) {
      System.out.println(
          "Exception occurred while certificate generation");
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * This method writes byte data into file
   * @param a_data byte[]
   * @param a_strFileName String
   */
  public void writeFile(byte[] a_data, String a_strServiceType, String a_strFileExtension) {
    String str_signed = "";
    if (a_strServiceType.equalsIgnoreCase(Util.SIGNING)) {
      str_signed = "signed";
    }
    String str_filePath = "../data/" + a_strServiceType + "/" + str_fileName + "_" + str_signed + a_strFileExtension;
    try {
      FileOutputStream obj_outPutStream = new FileOutputStream(str_filePath);
      obj_outPutStream.write(a_data);
      obj_outPutStream.close();
    }
    catch (Exception ex) {
      System.out.println("Exception occurred writing data in file");
      ex.printStackTrace();
    }
  }

  /**
   * This method writes byte data into file
   * @param a_bFileData byte[]
   * @return byte[]
   */

  public void writeFile(byte[] a_data, String a_strFileName) {
    try {
      FileOutputStream obj_outputStream = new FileOutputStream(a_strFileName);
      obj_outputStream.write(a_data);
      obj_outputStream.close();
    }
    catch (Exception ex) {
      System.out.println("Exception occurred writing data in file");
      ex.printStackTrace();
    }
  }

  /**
   * This method encodes byte data
   * @param a_bFileData byte[]
   * @return byte[]
   */
  public byte[] encode(byte[] a_bFileData) {
    byte[] b_encodedData = null;
    try {
      org.bouncycastle.util.encoders.Base64 obj_base64 = new org.bouncycastle.util.encoders.Base64();
      b_encodedData = obj_base64.encode(a_bFileData);
    }
    catch (Exception ex) {
      System.out.println("Exception occurred while encoding data");
      ex.printStackTrace();
    }
    return b_encodedData;
  }

  /**
   * This method decodes byte data
   * @param a_bFileData byte[]
   * @return byte[]
   */
  public byte[] decode(byte[] a_bFileData) {
    byte[] b_decodedData = null;
    try {
      org.bouncycastle.util.encoders.Base64 obj_base64 = new org.bouncycastle.util.encoders.Base64();
      b_decodedData = obj_base64.decode(a_bFileData);
    }
    catch (Exception ex) {
      System.out.println("Exception occurred while encoding data");
      ex.printStackTrace();
    }
    return b_decodedData;
  }

  /**
   * This method will write request into file depending which service request is made
   * @param a_strService String
   * @param a_strRequestType String
   * @param a_strRequestID String
   */
  public void writeFile(String a_strService, String a_strServiceType, String a_strRequestType, Document a_document) {
    String str_filePath = "../data/" + a_strService + "/" + str_fileName + "_" + a_strServiceType + "_" + a_strRequestType + ".xml";
    try {
      FileOutputStream obj_fileOutputStream = new FileOutputStream(str_filePath);
      TransformerFactory obj_factory = TransformerFactory.newInstance();
      Transformer obj_tr = obj_factory.newTransformer();
      DOMSource obj_source = new DOMSource(a_document.getDocumentElement());
      StreamResult obj_result = new StreamResult(obj_fileOutputStream);
      obj_tr.transform(obj_source, obj_result);
      obj_fileOutputStream.close();
    }
    catch (Exception ex) {
      System.out.println("Exception occurred while writing file");
      ex.printStackTrace();
    }
  }

  /**
   * This method will write response or soap message into file depending which service request is made
   * @param a_strService String
   * @param a_strRequestType String
   * @param a_strRequestID String
   * @param a_soapMessage SOAPMessage
   */
  public void writeFile(String a_strService, String a_strServiceType, String a_strRequestType, SOAPMessage a_soapMessage) {
    String str_filePath = "";
    if (a_strService.equals(Util.CERTIFICATION)) {
      str_filePath = "../data/" + a_strService + "/" + a_strServiceType + "_" + a_strRequestType + ".xml";
    }
    else {
      str_filePath = "../data/" + a_strService + "/" + str_fileName + "_" + a_strServiceType + "_" + a_strRequestType + ".xml";
    }
    try {
      OutputStream obj_fileOutputStream = new FileOutputStream(str_filePath);
      a_soapMessage.writeTo(obj_fileOutputStream);
      obj_fileOutputStream.close();
    }
    catch (Exception ex) {
      System.out.println("Exception occurred while writing file");
      ex.printStackTrace();
    }
  }

  /**
   * This method returns the SOAP Message
   * @param a_document Document
   * @return SOAPMessage
   */
  public SOAPMessage getSOAPMessage(Document a_document) {
    SOAPMessage obj_soapMsg = null;
    try {
      SOAPConnectionFactory obj_soapConFactory = SOAPConnectionFactory.newInstance();
      SOAPConnection obj_soapCon = obj_soapConFactory.createConnection();
      setSOAPConnection(obj_soapCon);
      MessageFactory obj_msgFactory = MessageFactory.newInstance();
      obj_soapMsg = obj_msgFactory.createMessage();
      SOAPPart obj_soapPart = obj_soapMsg.getSOAPPart();
      SOAPEnvelope obj_soapEnvelope = obj_soapPart.getEnvelope();
      SOAPBody obj_soapBody = obj_soapEnvelope.getBody();
      SOAPFactory obj_soapFactory = SOAPFactory.newInstance();
      SOAPElement obj_soapElement = obj_soapFactory.createElement(a_document.getDocumentElement());
      obj_soapBody.addChildElement(obj_soapElement);
    }
    catch (Exception ex) {
      System.out.println("Exception occurred while creating SOAP Message");
      ex.printStackTrace();
    }
    return obj_soapMsg;
  }

  /**
   * This method sets the SOAP Connection
   * @param a_soapConnection SOAPConnection
   */
  public void setSOAPConnection(SOAPConnection a_soapConnection) {
    obj_soapCon = a_soapConnection;
  }

  /**
   * This method will return SOAP Connection
   * @return SOAPConnection
   */
  public SOAPConnection getSOAPConnection() {
    return obj_soapCon;
  }

  /**
   * This method returns a w3c document
   * @return Document
   */
  public Document getDocument() {
    Document document = null;
    try {
      DocumentBuilderFactory obj_dbFactory = DocumentBuilderFactory.newInstance();
//      obj_dbFactory.setNamespaceAware(true);
      DocumentBuilder obj_dbuilder = obj_dbFactory.newDocumentBuilder();
      document = obj_dbuilder.newDocument();
    }
    catch (Exception ex) {
      System.out.println("Exception occurred while getting Document");
      ex.printStackTrace();
    }
    return document;
  }

  public String generateRequestID() {
    Random obj_random = new Random();
    return Integer.toString(obj_random.nextInt());
  }

  /**
   * This method generates the signature on the provided data.
   * @param a_objPvtKey PrivateKey
   * @param a_objCertificate X509Certificate
   * @param a_byteTBSData byte[]
   * @return byte[]
   * @throws Exception
   */
  public byte[] createPKCS7(PrivateKey a_objPvtKey, X509Certificate a_objCertificate, byte[] a_byteTBSData) throws Exception {
    Security.insertProviderAt(new BouncyCastleProvider(), 2);
    CMSProcessable msg = new CMSProcessableByteArray(a_byteTBSData);
    CMSSignedDataGenerator obj_cmsGen = new CMSSignedDataGenerator();
    obj_cmsGen.addSigner(a_objPvtKey, a_objCertificate, CMSSignedDataGenerator.DIGEST_SHA1);
    ArrayList obj_al = new ArrayList();
    obj_al.add(a_objCertificate);
    CertStore certs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(obj_al), "BC");
    obj_cmsGen.addCertificatesAndCRLs(certs);
    CMSSignedData obj_cmsSD = obj_cmsGen.generate(CMSSignedDataGenerator.DATA, msg, true, "BC", false);
    return obj_cmsSD.getEncoded();
  }
}
