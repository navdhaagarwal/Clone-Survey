package com.nucleus.master.audit.service.util;

import java.io.Serializable;
import java.lang.reflect.Method;

public class MasterChangeSetterMethodMeta implements Serializable{

	private Method setterMethod;

	public Method getSetterMethod() {
		return setterMethod;
	}

	public MasterChangeSetterMethodMeta(Method setterMethod) {
		super();
		this.setterMethod = setterMethod;
	}
	

}
