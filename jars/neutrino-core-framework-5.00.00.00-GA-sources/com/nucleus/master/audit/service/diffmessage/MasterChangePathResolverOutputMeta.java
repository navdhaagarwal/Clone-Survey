package com.nucleus.master.audit.service.diffmessage;

import java.io.Serializable;
import java.util.Stack;

import com.nucleus.master.audit.service.util.MasterChangeGetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeSetterMethodMeta;
import com.nucleus.master.audit.service.util.MasterChangeTuple2;

public class MasterChangePathResolverOutputMeta implements Serializable{

	private Class affectedClass;
	
	private String fieldName;
	
	private Class rootObjectClass;
	
	private Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> fieldStack;
	
	private Object dataObject;

	public Class getAffectedClass() {
		return affectedClass;
	}

	public void setAffectedClass(Class affectedClass) {
		this.affectedClass = affectedClass;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Class getRootObjectClass() {
		return rootObjectClass;
	}

	public void setRootObjectClass(Class rootObjectClass) {
		this.rootObjectClass = rootObjectClass;
	}

	public Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getFieldStack() {
		return fieldStack;
	}

	public void setFieldStack(
			Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> fieldStack) {
		this.fieldStack = fieldStack;
	}

	public Object getDataObject() {
		return dataObject;
	}

	public void setDataObject(Object dataObject) {
		this.dataObject = dataObject;
	}
	
	
}
