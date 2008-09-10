package com.ascertia.zfp.gosign.forms;

import java.io.*;
import java.util.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

/**
 *
 * <p>Title: ASC_PDFFormFiller</p>
 *
 * <p>Description: This class fills the pdf form with the provided data</p></p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Ascertia Ltd.</p>
 *
 * @author KS
 * @version 1.0
 */

public class ASC_PDFFormFiller {

    /**
     * PDF Reader
     */
    private PdfReader m_objPDFReader = null;
    /**
     * PDF stamper, that is used to fill pdf form fields
     */
    private PdfStamper m_objPDFStamper = null;
    /**
     * PDF form fields
     */
    private AcroFields m_objPDFFields = null;
    /**
     * Values that will be entered in the pdf form fields
     */
    private Properties m_objKeyValues = null;
    /**
     * Hashtable containing pdf form fields' names and their corresponding values
     */
    private Hashtable m_objKeyValuesData = null;
    /**
     * Output stream for file which will be created after form filling
     */
    private OutputStream m_objPDFFOS = null;
    /**
     * Bytes of input pdf that is to be filled
     */
    private byte[] m_byteInputPDF = null;
    /**
     * Bytes of resultant/filled pdf
     */
    private byte[] m_byteOutputPDF = null;
    /**
     * Resultant/Filled pdf file
     */
    private File m_objOutputPDFFile = null;
    /**
     * Getting user home
     */
    private static String m_strUserConfigPath = System.getProperty("user.home") +
                                                "\\";
    /**
     * Path where resultant pdf will be created for temporary purpose
     */
    private static String m_strPDFFilePath = m_strUserConfigPath + "temp.pdf";


    /**
     * Constructer of the class
     * @param a_byteInputPDF byte[] Bytes of input pdf
     */
    public ASC_PDFFormFiller(byte[] a_byteInputPDF) {
        try {
            m_byteInputPDF = a_byteInputPDF;
            m_objOutputPDFFile = new File(m_strPDFFilePath);
            m_objPDFFOS = new FileOutputStream(m_objOutputPDFFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Getting PDF Reader corresponding to the input pdf
     * @return PdfReader
     */
    private PdfReader getPDFReader() {
        try {
            m_objPDFReader = new PdfReader(m_byteInputPDF);
            return m_objPDFReader;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Getting PDF Reader corresponding to the input pdf
     * @return PdfStamper
     */
    private PdfStamper getPDFStamper() {
        try {
            m_objPDFStamper = new PdfStamper(m_objPDFReader, m_objPDFFOS,
                                             m_objPDFReader.getPdfVersion(), true);
            return m_objPDFStamper;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Getting the names of input pdf's form fields
     * @return AcroFields
     */
    private AcroFields getPDFFields() {
        m_objPDFFields = m_objPDFStamper.getAcroFields();
        return m_objPDFFields;
    }

    /**
     * Setting PDF Reader
     * @param m_objPDFReader PdfReader
     */
    private void setPDFReader(PdfReader m_objPDFReader) {
        m_objPDFReader = m_objPDFReader;
    }

    /**
     * Setting PDF Stamper
     * @param a_objPDFStamp PdfStamper
     */
    private void setPDFStamper(PdfStamper a_objPDFStamp) {
        m_objPDFStamper = a_objPDFStamp;
    }

    /**
     * Setting PDF form fields
     * @param a_objPDFFields AcroFields
     */
    private void setPDFAcroFields(AcroFields a_objPDFFields) {
        m_objPDFFields = a_objPDFFields;
    }

    /**
     * Filling pdf form with the data, available in hashtable
     * @param a_objData Hashtable
     */
    private void setNewDataInPDF(Hashtable a_objData) {
        try {
            m_objKeyValuesData = a_objData;
            Enumeration obj_enum = a_objData.keys();
            while (obj_enum.hasMoreElements()) {
                Object key = obj_enum.nextElement();
                System.out.println("Key..: " + key.toString());
                System.out.println("Value..: " + a_objData.get(key).toString());
                m_objPDFFields.setField(key.toString(),
                                        a_objData.get(key).toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Filling pdf form with the data, available in the properties' object
     * @param a_objData Properties
     */
    private void setNewDataInPDF(Properties a_objData) {
        try {
            m_objKeyValues = a_objData;
            m_objKeyValues.list(System.out);
            Enumeration obj_enum = m_objKeyValues.keys();
            while (obj_enum.hasMoreElements()) {
                String key = obj_enum.nextElement().toString();
                m_objPDFFields.setField(key, m_objKeyValues.getProperty(key));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Filling pdf form with the data, available in the iText provided XfdfReader's object
     * @param a_objData XfdfReader
     */
    private void setNewDataInPDF(XfdfReader a_objData) {
        try {
            m_objPDFFields.setFields(a_objData);
        } catch (Exception ex) {
        }
    }

    /**
     * Closing pdf stamper
     */
    private void closePDFStamper() {
        try {
            m_objPDFStamper.close();
            m_objPDFFOS.close();

            m_objPDFStamper = null;
            m_objPDFFOS = null;
        } catch (Exception ex) {
        }
    }

    /**
     * Filling pdf form through the properties' object
     * @param a_objKeyValues Properties
     * @return boolean
     */
    private boolean fillNormalPDFData(Properties a_objKeyValues) {

        //1 - Getting PDF Reader Object
        getPDFReader();

        //2 - Getting PDF Stamper Object
        getPDFStamper();

        //3 - Getting PDF Acro Fields Object
        getPDFFields();

        //4 - Setting Data in PDF
        setNewDataInPDF(a_objKeyValues);

        //5 - Closing PDFStamper
        closePDFStamper();

        return true;
    }

    /**
     * Filling pdf form through the hashtable
     * @param a_objKeyValues Hashtable
     * @return boolean
     */
    private boolean fillNormalPDFData(Hashtable a_objKeyValues) {

        //1 - Getting PDF Reader Object
        getPDFReader();

        //2 - Getting PDF Stamper Object
        getPDFStamper();

        //3 - Getting PDF Acro Fields Object
        getPDFFields();

        //4 - Setting Data in PDF
        setNewDataInPDF(a_objKeyValues);

        //5 - Closing PDFStamper
        closePDFStamper();

        return true;
    }

    /**
     * Getting bytes of filled pdf
     * @return byte[]
     */
    public byte[] getFilledPDF() {
        try {
            m_objOutputPDFFile = null;
            m_objOutputPDFFile = new File(m_strPDFFilePath);
            m_byteOutputPDF = getContents(m_objOutputPDFFile);
            m_objOutputPDFFile.delete();
            return m_byteOutputPDF;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    /**
     * This function returns the bytes of the provided file
     * @param a_objFile File
     * @return byte[]
     * @throws Exception
     */
    public static byte[] getContents(File a_objFile) throws Exception {
        FileInputStream obj_fis = new FileInputStream(a_objFile);
        ByteArrayOutputStream obj_data = new ByteArrayOutputStream();
        byte[] byte_buffer = new byte[200];

        int i_byteRead = obj_fis.read(byte_buffer);

        while (i_byteRead != -1) {
            obj_data.write(byte_buffer, 0, i_byteRead);
            i_byteRead = obj_fis.read(byte_buffer);
        }
        obj_fis.close();
        return obj_data.toByteArray();
    }

    /**
     * This function creates a file on the disk using the provided bytes
     * @param a_objFile File
     * @param a_byteContents byte[]
     * @throws Exception
     */
    public static void setContents(File a_objFile, byte[] a_byteContents) throws
            Exception {
        FileOutputStream obj_fos = new FileOutputStream(a_objFile);
        obj_fos.write(a_byteContents);
        obj_fos.close();
    }

    /**
     * Fills the pdf form using the hashtable and finally flattens it
     * @param a_objKeyValues Hashtable
     */
    private void fillFlattenedPDFData(Hashtable a_objKeyValues) {
        try {
            //1 - Creating Output Stream Object
            ByteArrayOutputStream m_objPDFBAOS = new ByteArrayOutputStream();

            //2 - Setting Contents to file
            setContents(m_objOutputPDFFile, m_byteInputPDF);

            //3 - Creating Document and Copying Contents of Document
            Document doc = new Document();
            PdfCopy copy = new PdfCopy(doc,
                                       new FileOutputStream(m_objOutputPDFFile));
            doc.open();

            //4 - Getting PDF Reader Object
            m_objPDFReader = new PdfReader(m_byteInputPDF);

            //5 - Getting PDF Stamper Object
            m_objPDFStamper = new PdfStamper(m_objPDFReader, m_objPDFBAOS);

            //6 - Getting PDF Acro Fields Object
            m_objPDFFields = m_objPDFStamper.getAcroFields();
            m_objPDFFields.setExtraMargin(0, 1);

            //7 - Setting Data in PDF
            setNewDataInPDF(a_objKeyValues);

            //8 - Flattening Form Data
            m_objPDFStamper.setFormFlattening(true);

            //9 - Closing PDFStamper
            m_objPDFStamper.close();

            //10 - Adding New Data in PDF Doc
            m_objPDFReader = new PdfReader(m_objPDFBAOS.toByteArray());
            copy.addPage(copy.getImportedPage(m_objPDFReader, 1));

            //5 - Closing PDFStamper, Streams and Document
            doc.close();
            m_objPDFBAOS.close();
            closePDFStamper();
            m_objPDFBAOS = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Fills the pdf form using the properties' object and finally flattens it
     * @param a_objKeyValues Properties
     */
    private void fillFlattenedPDFData(Properties a_objKeyValues) {
        try {
            //1 - Creating Output Stream Object
            ByteArrayOutputStream m_objPDFBAOS = new ByteArrayOutputStream();

            //2 - Setting Contents to file
            setContents(m_objOutputPDFFile, m_byteInputPDF);

            //3 - Creating Document and Copying Contents of Document
            Document doc = new Document();
            PdfCopy copy = new PdfCopy(doc,
                                       new FileOutputStream(m_objOutputPDFFile));
            doc.open();

            //4 - Getting PDF Reader Object
            m_objPDFReader = new PdfReader(m_byteInputPDF);

            //5 - Getting PDF Stamper Object
            m_objPDFStamper = new PdfStamper(m_objPDFReader, m_objPDFBAOS);

            //6 - Getting PDF Acro Fields Object
            m_objPDFFields = m_objPDFStamper.getAcroFields();
            m_objPDFFields.setExtraMargin(0, 1);

            //7 - Setting Data in PDF
            setNewDataInPDF(a_objKeyValues);

            //8 - Flattening Form Data
            m_objPDFStamper.setFormFlattening(true);

            //9 - Closing PDFStamper
            m_objPDFStamper.close();

            //10 - Adding New Data in PDF Doc
            m_objPDFReader = new PdfReader(m_objPDFBAOS.toByteArray());
            copy.addPage(copy.getImportedPage(m_objPDFReader, 1));

            //5 - Closing PDFStamper, Streams and Document
            doc.close();
            m_objPDFBAOS.close();
            closePDFStamper();
            m_objPDFBAOS = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This function calls the appropriate method to fill the pdf form using the hashtable, depending upon that pdf is to be flattened or not after filling it
     * @param a_objKeyValues Hashtable
     * @param a_bflattenData boolean
     * @return boolean
     */
    public boolean fillPDFData(Hashtable a_objKeyValues, boolean a_bflattenData) {
        if (a_bflattenData)
            fillFlattenedPDFData(a_objKeyValues);
        else
            fillNormalPDFData(a_objKeyValues);

        return true;
    }

    /**
     * This function calls the appropriate method to fill the pdf form using the properties' object, depending upon that pdf is to be flattened or not after filling it
     * @param a_objKeyValues Properties
     * @param a_bflattenData boolean
     * @return boolean
     */
    public boolean fillPDFData(Properties a_objKeyValues,
                               boolean a_bflattenData) {
        if (a_bflattenData)
            fillFlattenedPDFData(a_objKeyValues);
        else
            fillNormalPDFData(a_objKeyValues);

        return true;
    }
}


