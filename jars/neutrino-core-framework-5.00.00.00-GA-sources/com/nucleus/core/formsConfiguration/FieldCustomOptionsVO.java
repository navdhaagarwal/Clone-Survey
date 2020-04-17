package com.nucleus.core.formsConfiguration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

public class FieldCustomOptionsVO implements Serializable{

    private static final long serialVersionUID = -4716031415755202863L;

    @ApiModelProperty(notes="This field is Custome Item Value",required=false,dataType="String",hidden=false)
    private String customeItemValue;

    @ApiModelProperty(notes="This field is Custome Item Label",required=false,dataType="String",hidden=false)
    private String customeItemLabel;

    /**
     * @return the customeItemValue
     */
    public String getCustomeItemValue() {
        return customeItemValue;
    }

    /**
     * @param customeItemValue the customeItemValue to set
     */
    public void setCustomeItemValue(String customeItemValue) {
        this.customeItemValue = customeItemValue;
    }

    /**
     * @return the customeItemLabel
     */
    public String getCustomeItemLabel() {
        return customeItemLabel;
    }

    /**
     * @param customeItemLabel the customeItemLabel to set
     */
    public void setCustomeItemLabel(String customeItemLabel) {
        this.customeItemLabel = customeItemLabel;
    }

}
