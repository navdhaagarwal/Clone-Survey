package com.nucleus.finnone.pro.general.constants;


public enum ExceptionSeverityEnum {
	LOW(0,"SEVERITY_LOW","Severity Low"),
	SEVERITY_MEDIUM(5,"SEVERITY_MEDIUM","Severity medium"),
	SEVERITY_HIGH(10,"SEVERITY_HIGH","Severity high"),
	NO_ERROR(-1,"SEVERITY_NO_ERROR","No error Severity"),
	 NULL(null,"NULL","Null Enum");
	
	 
	private Integer enumValue;
	private String description;
	private String i18nCode;
	
	private ExceptionSeverityEnum(Integer enumValue,String i18nCode,String description){
		this.i18nCode=i18nCode;
		this.enumValue=enumValue;
		this.description=description;
	}
	
	
	 public Integer getEnumValue() {
			return enumValue;
		}



		public String getDescription() {
			return description;
		}



		public String getI18nCode() {
			return i18nCode;
		}




}
