package com.nucleus.core.formsConfiguration.fieldcomponent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

public class PhoneNumberTypeVO implements Serializable{

    
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(notes="This field is Code",required=false,dataType="String",hidden=false)
	private String             code;

    @ApiModelProperty(notes="This field is Name",required=false,dataType="String",hidden=false)
    private String             name;

    @ApiModelProperty(notes="This field is Description",required=false,dataType="String",hidden=false)
    private String             description;

    public static final String LANDLINE_NUMBER = "Phone";
    public static final String MOBILE_NUMBER   = "Mobile";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
