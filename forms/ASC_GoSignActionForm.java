package com.ascertia.zfp.gosign.forms;

import org.apache.struts.action.*;

/**
 *
 * <p>Title: ASC_GoSignActionForm</p>
 *
 * <p>Description: This is an action form class corresponding to the to be filled pdf's form</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Ascertia Ltd.</p>
 *
 * @author MH
 * @version 1.0
 */
public class ASC_GoSignActionForm extends ActionForm {

    /**
     * Name that will be entered in the 'name' field of the pdf form
     */
    private String name;
    /**
     * Email address that will be entered in the 'emailAddress' field of the pdf form
     */
    private String emailAddress;
    /**
     * Company name that will be entered in the 'companyName' field of the pdf form
     */
    private String companyName;
    /**
     * Purchase order number name that will be entered in the 'purchaseOrderNumber' field of the pdf form
     */
    private String purchaseOrderNumber;
    /**
     * Number of liscences that will be entered in the 'numberOfLiscences' field of the pdf form
     */
    private int numberOfLiscences;
    /**
     * Total order value that will be entered in the 'totalOrderValue' field of the pdf form
     */
    private String totalOrderValue;

    /**
     * Default constructer of the class
     */
    public ASC_GoSignActionForm() {
    }

    /**
     * Sets the name
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the email address
     * @param emailAddress String
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Sets the company name
     * @param companyName String
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Sets the purchase order number
     * @param purchaseOrderNumber String
     */
    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    /**
     * Sets the number of liscences
     * @param numberOfLiscences int
     */
    public void setNumberOfLiscences(int numberOfLiscences) {
        this.numberOfLiscences = numberOfLiscences;
    }

    /**
     * Sets the total order value
     * @param totalOrderValue String
     */
    public void setTotalOrderValue(String totalOrderValue) {
        this.totalOrderValue = totalOrderValue;
    }

    /**
     * Returns the name
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the email address
     * @return String
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Returns the company name
     * @return String
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Returns the purchase order number
     * @return String
     */
    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    /**
     * Returns the number of liscences
     * @return int
     */
    public int getNumberOfLiscences() {
        return numberOfLiscences;
    }

    /**
     * Returns the total order value
     * @return String
     */
    public String getTotalOrderValue() {
        return totalOrderValue;
    }
}


