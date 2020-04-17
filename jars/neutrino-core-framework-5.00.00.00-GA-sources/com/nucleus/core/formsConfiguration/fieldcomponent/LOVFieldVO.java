package com.nucleus.core.formsConfiguration.fieldcomponent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

public class LOVFieldVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(notes="This field is Lov Hidden Value",required=false,dataType="String",hidden=false)
    private String lovHiddenValue;

    @ApiModelProperty(notes="This field is Lov Display Value",required=false,dataType="String",hidden=false)
    private String lovDisplayValue;

    @ApiModelProperty(notes="This field is Lov Entity Class",required=false,dataType="String",hidden=false)
    private String lovEntityClass;

    public String getLovHiddenValue() {
        return lovHiddenValue;
    }

    public void setLovHiddenValue(String lovHiddenValue) {
        this.lovHiddenValue = lovHiddenValue;
    }

    public String getLovDisplayValue() {
        return lovDisplayValue;
    }

    public void setLovDisplayValue(String lovDisplayValue) {
        this.lovDisplayValue = lovDisplayValue;
    }

    public String getLovEntityClass() {
        return lovEntityClass;
    }

    public void setLovEntityClass(String lovEntityClass) {
        this.lovEntityClass = lovEntityClass;
    }
}
