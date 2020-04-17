package com.nucleus.master.audit.metadata;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AuditableEntityToBiDiTree {

	private BiDiTreeNode rootNode;
	
	private Map<BiDiTreeNodePointerByField, BiDiTreeNode> fieldToNode;
	
	private Map<BiDiTreeNodePointerByGetter,BiDiTreeNode> getterToNode;

	public Map<BiDiTreeNodePointerByGetter, BiDiTreeNode> getGetterToNode() {
		return getterToNode;
	}

	public void setGetterToNode(Map<BiDiTreeNodePointerByGetter, BiDiTreeNode> getterToNode) {
		this.getterToNode = getterToNode;
	}

	
	public void addGetterToNode(BiDiTreeNodePointerByGetter getterKey, BiDiTreeNode getterToNode) {
		if(this.getterToNode ==null){
			this.getterToNode = new HashMap<>();
		}
		this.getterToNode.put(getterKey, getterToNode);
	}
	
	
	public BiDiTreeNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(BiDiTreeNode rootNode) {
		this.rootNode = rootNode;
	}

	public Map<BiDiTreeNodePointerByField, BiDiTreeNode> getFieldToNode() {
		return fieldToNode;
	}

	public void setFieldToNode(Map<BiDiTreeNodePointerByField, BiDiTreeNode> fieldToNode) {
		this.fieldToNode = fieldToNode;
	}
	
	public void addFieldPointer(BiDiTreeNodePointerByField pointer, BiDiTreeNode fieldToNode) {
		if(this.fieldToNode == null){
			this.fieldToNode = new HashMap<>();
		}
		this.fieldToNode.put(pointer, fieldToNode);
	}
	
	public BiDiTreeNode getNodeByField(Class className,String fieldName){
		if(className == null){
			return null;
		}
		BiDiTreeNode resultNode = null;
		resultNode = this.fieldToNode.get(new BiDiTreeNodePointerByField(className, fieldName));
		if(resultNode != null){
			return resultNode;
		}
		return getNodeByField(className.getSuperclass(), fieldName);
	}
	
	
	public BiDiTreeNode getNodeByGetter(Class className,Method getterMethod){
		if(className == null){
			return null;
		}
		BiDiTreeNode resultNode = null;
		resultNode = this.getterToNode.get(new BiDiTreeNodePointerByGetter(className, getterMethod));
		if(resultNode != null){
			return resultNode;
		}
		return getNodeByGetter(className.getSuperclass(), getterMethod);
	}
	
}
