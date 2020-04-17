package com.nucleus.core.formsConfiguration;
import java.util.List;
import java.util.Map;


public class DynamicFormData{
	

	Map<String,List<UIMetaDataVo>> dynamicForms;

	public DynamicFormData(Map<String, List<UIMetaDataVo>> dynamicForms) {
		super();
		this.dynamicForms = dynamicForms;
	}

	public Map<String, List<UIMetaDataVo>> getDynamicForms() {
		return dynamicForms;
	}

	public void setDynamicForms(Map<String, List<UIMetaDataVo>> dynamicForms) {
		this.dynamicForms = dynamicForms;
	}

	
}