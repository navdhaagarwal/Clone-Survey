package com.nucleus.core.formsConfiguration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.sql.Clob;

public class ProductSchemeMetaDataVo implements Serializable {

	@ApiModelProperty(notes="This field is Keyy",required=false,dataType="String",hidden=false)
	private String keyy;
    @ApiModelProperty(notes="This field is Hql",required=false,dataType="String",hidden=false)
    private String hql;
    
	public String getKeyy() {
		return keyy;
	}
	public void setKeyy(String keyy) {
		this.keyy = keyy;
	}
	public String getHql() {
		return hql;
	}
	public void setHql(String hql) {
		this.hql = hql;
	}
}
