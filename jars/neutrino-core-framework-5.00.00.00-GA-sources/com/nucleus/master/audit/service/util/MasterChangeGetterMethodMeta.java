package com.nucleus.master.audit.service.util;

import java.io.Serializable;
import java.lang.reflect.Method;

public class MasterChangeGetterMethodMeta implements Serializable{

	private Method getterMethod;
	
	private String pathName;

	private Object[] getterMethodInput;
	
	private boolean isListMember;

	public Method getGetterMethod() {
		return getterMethod;
	}

	public String getPathName() {
		return pathName;
	}

	public Object[] getGetterMethodInput() {
		return getterMethodInput;
	}

	public MasterChangeGetterMethodMeta(Method getterMethod, String pathName) {
		super();
		this.getterMethod = getterMethod;
		this.pathName = pathName;
	}

	public MasterChangeGetterMethodMeta(Method getterMethod, String pathName, Object[] getterMethodInput,boolean isList) {
		super();
		this.getterMethod = getterMethod;
		this.pathName = pathName;
		this.getterMethodInput = getterMethodInput;
		this.isListMember = isList;
	}

	public boolean isListMember() {
		return isListMember;
	}
	
	
}
