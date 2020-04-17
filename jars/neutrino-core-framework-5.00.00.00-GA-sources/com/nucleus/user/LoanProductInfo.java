package com.nucleus.user;

import java.io.Serializable;

public class LoanProductInfo implements Serializable {

    private static final long serialVersionUID = 786864561472503556L;

    public Long               id;

    public String             productName;

    public String             productTypeShortName;

    public Long getId() {
        return id;
    }

    public String getProductTypeShortName() {
        return productTypeShortName;
    }

    public void setProductTypeShortName(String productTypeShortName) {
        this.productTypeShortName = productTypeShortName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

}
