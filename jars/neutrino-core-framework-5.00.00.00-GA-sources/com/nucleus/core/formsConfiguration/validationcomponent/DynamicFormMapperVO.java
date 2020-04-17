package com.nucleus.core.formsConfiguration.validationcomponent;

import com.nucleus.cas.eligibility.service.ProductProcessor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;


public class DynamicFormMapperVO implements Serializable{

    private ProductProcessor productProcessor;
    private Map<String, String> selectedFieldsMap;
    private Map<String, String> allFieldsMap;
    private String[] selectedFieldsArr;
    private String selectedFields;

    public ProductProcessor getProductProcessor() {
        return productProcessor;
    }

    public void setProductProcessor(ProductProcessor productProcessor) {
        this.productProcessor = productProcessor;
    }

    public Map<String, String> getSelectedFieldsMap() {
        return selectedFieldsMap;
    }

    public void setSelectedFieldsMap(Map<String, String> selectedFieldsMap) {
        this.selectedFieldsMap = selectedFieldsMap;
    }

    public Map<String, String> getAllFieldsMap() {
        return allFieldsMap;
    }

    public void setAllFieldsMap(Map<String, String> allFieldsMap) {
        this.allFieldsMap = allFieldsMap;
    }

    public String[] getSelectedFieldsArr() {
        return selectedFieldsArr;
    }

    public void setSelectedFieldsArr(String[] selectedFieldsArr) {
        this.selectedFieldsArr = selectedFieldsArr;
    }

    public String getSelectedFields() {
        return selectedFields;
    }

    public void setSelectedFields(String selectedFields) {
        this.selectedFields = selectedFields;
    }

    @Override
    public String toString() {
        return "DynamicFormMapperVO{" +
                "productProcessor=" + productProcessor +
                ", selectedFieldsMap=" + selectedFieldsMap +
                ", allFieldsMap=" + allFieldsMap +
                ", selectedFieldsArr=" + Arrays.toString(selectedFieldsArr) +
                ", selectedFields='" + selectedFields + '\'' +
                '}';
    }
}
