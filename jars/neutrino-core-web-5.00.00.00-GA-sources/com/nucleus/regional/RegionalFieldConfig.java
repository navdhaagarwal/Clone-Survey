package com.nucleus.regional;

import java.io.Serializable;

public class RegionalFieldConfig implements Serializable {

	private static final long serialVersionUID = -5514518410763470541L;
	private String fieldName;
	private String fieldLabel;
	private String fieldPlaceHolderKey;
	private String fieldToolTipKey;
	private String mandatory;
	private String maxLength;
	private String sourceEntityName;
	private String regionalVisibility;
	private String divId;
	private String viewMode;
	private String regionalGenericParameterType;
	private String isGeneric;
	private String regionalItemValue;
	private String regionalItemLabel;
	private String regionalListValue;
	private String regionalParentId;
	private String disabled;
	private String regionalItemCode;

	public String getRegionalGenericParameterType() {
		return regionalGenericParameterType;
	}

	public void setRegionalGenericParameterType(
			String regionalGenericParameterType) {
		this.regionalGenericParameterType = regionalGenericParameterType;
	}

	public String getIsGeneric() {
		return isGeneric;
	}

	public void setIsGeneric(String isGeneric) {
		this.isGeneric = isGeneric;
	}

	public String getRegionalItemValue() {
		return regionalItemValue;
	}

	public void setRegionalItemValue(String regionalItemValue) {
		this.regionalItemValue = regionalItemValue;
	}

	public String getRegionalItemLabel() {
		return regionalItemLabel;
	}

	public void setRegionalItemLabel(String regionalItemLabel) {
		this.regionalItemLabel = regionalItemLabel;
	}

	public String getRegionalListValue() {
		return regionalListValue;
	}

	public void setRegionalListValue(String regionalListValue) {
		this.regionalListValue = regionalListValue;
	}

	public String getViewMode() {
		return viewMode;
	}

	public void setViewMode(String viewMode) {
		this.viewMode = viewMode;
	}

	public String getSourceEntityName() {
		return sourceEntityName;
	}

	public void setSourceEntityName(String sourceEntityName) {
		this.sourceEntityName = sourceEntityName;
	}

	public String getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	public String getFieldPlaceHolderKey() {
		return fieldPlaceHolderKey;
	}

	public void setFieldPlaceHolderKey(String fieldPlaceHolderKey) {
		this.fieldPlaceHolderKey = fieldPlaceHolderKey;
	}
	
	public String getFieldToolTipKey() {
		return fieldToolTipKey;
	}

	public void setFieldToolTipKey(String fieldToolTipKey) {
		this.fieldToolTipKey = fieldToolTipKey;
	}
	
	public String getMandatory() {
		return mandatory;
	}

	public void setMandatory(String mandatory) {
		this.mandatory = mandatory;
	}

	public String getRegionalVisibility() {
		return regionalVisibility;
	}

	public void setRegionalVisibility(String regionalVisibility) {
		this.regionalVisibility = regionalVisibility;
	}

	public String getDivId() {
		return divId;
	}

	public void setDivId(String divId) {
		this.divId = divId;
	}


	public String getRegionalParentId() {
		return regionalParentId;
	}

	public void setRegionalParentId(String regionalParentid) {
		this.regionalParentId = regionalParentid;
	}

	public String getDisabled() {
		return disabled;
	}

	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}

	public String getRegionalItemCode() {
		return regionalItemCode;
	}

	public void setRegionalItemCode(String regionalItemCode) {
		this.regionalItemCode = regionalItemCode;
	}

	
}
