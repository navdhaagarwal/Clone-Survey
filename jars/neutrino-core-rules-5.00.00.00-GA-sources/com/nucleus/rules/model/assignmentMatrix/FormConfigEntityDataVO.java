package com.nucleus.rules.model.assignmentMatrix;

import java.io.Serializable;

/**
 *
 * @author Nucleus Software Exports Limited
 *Form Config VO used in Assignment Master
 */

public class FormConfigEntityDataVO implements Serializable {

    private static final long serialVersionUID = 4098291024684436336L;

    private String            itemLabel;

    private String            itemValue;

    private String            webDataBinderName;

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public String getWebDataBinderName() {
        return webDataBinderName;
    }

    public void setWebDataBinderName(String webDataBinderName) {
        this.webDataBinderName = webDataBinderName;
    }

}
