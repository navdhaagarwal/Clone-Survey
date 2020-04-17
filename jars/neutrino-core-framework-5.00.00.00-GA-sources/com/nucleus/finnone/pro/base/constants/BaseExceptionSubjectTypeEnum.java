/*
 * Author: Abhishek Pallav
 * Creation Date: 08-Feb-2014
 * Copyright: Nucleus Software Exports Ltd.
 * Description: This enum is used in BaseException to set the subject Type while raising an exception.
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Revision:  Version         Last Revision Date                   Name                Function / Module affected  Modifications Done
 * ------------------------------------------------------------------------------------------------------------------------------------
 *                1.0             11/02/2014                    Abhishek Pallav           Initial Version created
 *                1.1             19/02/2014                    Abhishek Pallav           Added subjectType for Loan and Customer  
  */
package com.nucleus.finnone.pro.base.constants;

public enum BaseExceptionSubjectTypeEnum {
	SUBJECT_TYPE_LOAN('L', ExceptionMessages.SUBJECT_TYPE_LOAN, "LOAN"), 
	SUBJECT_TYPE_CUSTOMER('C',ExceptionMessages.SUBJECT_TYPE_CUSTOMER, "CUSTOMER"), 
	SUBJECT_TYPE_FC_UPDN_BATCH('B',ExceptionMessages.SUBJECT_TYPE_FC_UPDN_BATCH, "UPLOAD DOWNLOAD BATCH ID"),
	NULL(null, "NULL", "Null Enum"), 
	NOT_SUPPORTED(' ', "Not Supported", "This Enum code is Not Supported");

	public static BaseExceptionSubjectTypeEnum getEnumFromValue(Character enumValue) {
		BaseExceptionSubjectTypeEnum baseExceptionSubjectTypeEnum = null;
		if (enumValue == null) {
			return NULL;
		}
		for (BaseExceptionSubjectTypeEnum supportedEnum : values()) {
			if (enumValue.equals(supportedEnum.enumValue)) {
				baseExceptionSubjectTypeEnum = supportedEnum;
			}
		}
		if (baseExceptionSubjectTypeEnum == null) {
			baseExceptionSubjectTypeEnum = NOT_SUPPORTED;
		}
		return baseExceptionSubjectTypeEnum;
	}

	private Character enumValue;

	private String description;

	private String i18nCode;

	private BaseExceptionSubjectTypeEnum(Character enumValue, String i18nCode, String description) {
		this.i18nCode = i18nCode;
		this.enumValue = enumValue;
		this.description = description;
	}

	public boolean equalsValue(Character theEnumValue) {
		return theEnumValue != null ? enumValue.equals(theEnumValue) : false;
	}

	public String getDescription() {
		return description;
	}

	public Character getEnumValue() {
		return enumValue;
	}

	public String getI18nCode() {
		return i18nCode;
	}


}
