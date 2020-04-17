package com.nucleus.web.rules.master;

public class AutocompleteTextbox {

    String label;
    String value;
    long   id;

    String productcode;
    String productName;
    long   productTypeId;

    long   lineBusinessCode;
    String businessName;

    long   loanCode;
    String loanName;

    String securedTypeLoan;

    public String getSecuredTypeLoan() {
        return securedTypeLoan;
    }

    public void setSecuredTypeLoan(String securedTypeLoan) {
        this.securedTypeLoan = securedTypeLoan;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public long getLoanCode() {
        return loanCode;
    }

    public void setLoanCode(long loanCode) {
        this.loanCode = loanCode;
    }

    String productTypeDesc;

    public String getProductTypeDesc() {
        return productTypeDesc;
    }

    public void setProductTypeDesc(String productTypeDesc) {
        this.productTypeDesc = productTypeDesc;
    }

    public long getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(long productTypeId) {
        this.productTypeId = productTypeId;
    }

    public long getLineBusinessCode() {
        return lineBusinessCode;
    }

    public void setLineBusinessCode(long lineBusinessCode) {
        this.lineBusinessCode = lineBusinessCode;
    }

    public String getProductcode() {
        return productcode;
    }

    public void setProductcode(String productcode) {
        this.productcode = productcode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
