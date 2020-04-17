package com.nucleus.master.audit.metadata;

import java.lang.reflect.Method;

public class AuditableClassFieldMetadata {

	private String fieldName;
	
	// to be use for field annotated with embed as reference
	private boolean isReferenceField;
	
	private Class referenceClass;
	
	private String columnOfRefClass;
	
	// to be use for field annotated with emded as value object
	private boolean isValueObject;
	
	private String identifierColumn;
	
	private Class valueObjectClassInstance;

	private String displayKeyKey;
	
	private String displayKeyMessage;
	
	private Method displayGetterMethod;
	
	private boolean skipInDisplay;
	
	public String getDisplayKeyKey() {
		return displayKeyKey;
	}

	public void setDisplayKeyKey(String displayKeyKey) {
		this.displayKeyKey = displayKeyKey;
	}

	public String getDisplayKeyMessage() {
		return displayKeyMessage;
	}

	public void setDisplayKeyMessage(String displayKeyMessage) {
		this.displayKeyMessage = displayKeyMessage;
	}

	public boolean isValueObject() {
		return isValueObject;
	}

	public void setValueObject(boolean isValueObject) {
		this.isValueObject = isValueObject;
	}

	public String getIdentifierColumn() {
		return identifierColumn;
	}

	public void setIdentifierColumn(String identifierColumn) {
		this.identifierColumn = identifierColumn;
	}

	public Class getValueObjectClassInstance() {
		return valueObjectClassInstance;
	}

	public void setValueObjectClassInstance(Class valueObjectClassInstance) {
		this.valueObjectClassInstance = valueObjectClassInstance;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}


	public boolean isReferenceField() {
		return isReferenceField;
	}

	public void setReferenceField(boolean isReferenceField) {
		this.isReferenceField = isReferenceField;
	}

	public Class getReferenceClass() {
		return referenceClass;
	}

	public void setReferenceClass(Class referenceClass) {
		this.referenceClass = referenceClass;
	}

	public String getColumnOfRefClass() {
		return columnOfRefClass;
	}

	public void setColumnOfRefClass(String columnOfRefClass) {
		this.columnOfRefClass = columnOfRefClass;
	}

	public Method getDisplayGetterMethod() {
		return displayGetterMethod;
	}

	public void setDisplayGetterMethod(Method displayGetterMethod) {
		this.displayGetterMethod = displayGetterMethod;
	}

	public boolean isSkipInDisplay() {
		return skipInDisplay;
	}

	public void setSkipInDisplay(boolean skipInDisplay) {
		this.skipInDisplay = skipInDisplay;
	}

	
}
