package com.nucleus.finnone.pro.lmsbase.constants;

public enum TenantStatusEnum {
	SYSTEM_RELEASED_FOR_BUSINESS('N',"SYSTEM RELEASED FOR BUSINESS" , "SYSTEM RELEASED FOR BUSINESS"), 
	EOD_IN_PROGRESS('Y',"EOD IN PROGRESS" , "EOD IN PROGRESS"),
	BOD_IN_PROGRESS('I',"BOD IN PROGRESS" , "BOD IN PROGRESS"), 
	SYSTEM_PENDING_FOR_BOD('B', "SYSTEM PENDING FOR BOD", "SYSTEM PENDING FOR BOD"); 
	
	private Character enumValue;

	private String description;

	private String i18nCode;

	private TenantStatusEnum(Character enumValue, String i18nCode, String description) {
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
