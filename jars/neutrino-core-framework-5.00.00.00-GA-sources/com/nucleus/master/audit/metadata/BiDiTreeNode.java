package com.nucleus.master.audit.metadata;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BiDiTreeNode implements Serializable{
	
	private BiDiTreeNode parentNode;
	
	private List<BiDiTreeNode> childNode ; // it can be primitive field, value reference, value object
	
	private Method getterMethodInParent;
	
	private Method setterMethodInparent;
	
	private String fieldName; // in case of root it will be same as identifer column of class
	
	private AuditableClassFieldMetadata fieldMetaData;

	private Class forClass;
	
	public BiDiTreeNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(BiDiTreeNode parentNode) {
		this.parentNode = parentNode;
	}

	public List<BiDiTreeNode> getChildNode() {
		return childNode;
	}

	public void setChildNode(List<BiDiTreeNode> childNode) {
		this.childNode = childNode;
	}
	
	public void addChildNode(BiDiTreeNode childNode) {
		if(this.childNode == null){
			this.childNode = new ArrayList<>();
		}
		this.childNode.add(childNode);
	}

	

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public BiDiTreeNode(String fieldName) {
		super();
		this.fieldName = fieldName;
	}

	public Method getGetterMethodInParent() {
		return getterMethodInParent;
	}

	public void setGetterMethodInParent(Method getterMethodInParent) {
		this.getterMethodInParent = getterMethodInParent;
	}

	public Method getSetterMethodInparent() {
		return setterMethodInparent;
	}

	public void setSetterMethodInparent(Method setterMethodInparent) {
		this.setterMethodInparent = setterMethodInparent;
	}

	public AuditableClassFieldMetadata getFieldMetaData() {
		return fieldMetaData;
	}

	public void setFieldMetaData(AuditableClassFieldMetadata fieldMetaData) {
		this.fieldMetaData = fieldMetaData;
	}

	public Class getForClass() {
		return forClass;
	}

	public void setForClass(Class forClass) {
		this.forClass = forClass;
	}

	
	
	

}
