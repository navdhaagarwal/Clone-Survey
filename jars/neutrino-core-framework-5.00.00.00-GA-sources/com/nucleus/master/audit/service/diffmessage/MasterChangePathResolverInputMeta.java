package com.nucleus.master.audit.service.diffmessage;

import java.io.Serializable;

import com.nucleus.master.audit.metadata.AuditableClassMetadataFactory;

public class MasterChangePathResolverInputMeta implements Serializable{

	
	private AuditableClassMetadataFactory classMeta;
	private Object affectedGlobalObjectId;
	private String fieldName;
	private Object affectedObject;
	private Object dataObject;
	private String initBuilder;
	
	public AuditableClassMetadataFactory getClassMeta() {
		return classMeta;
	}
	public Object getAffectedGlobalObjectId() {
		return affectedGlobalObjectId;
	}
	public String getFieldName() {
		return fieldName;
	}
	public Object getAffectedObject() {
		return affectedObject;
	}
	public Object getDataObject() {
		return dataObject;
	}
	public String getInitBuilder() {
		return initBuilder;
	}
	public MasterChangePathResolverInputMeta(AuditableClassMetadataFactory classMeta,
			Object affectedGlobalObjectId, String fieldName, Object affectedObject, Object dataObject,
			String initBuilder) {
		super();
		this.classMeta = classMeta;
		this.affectedGlobalObjectId = affectedGlobalObjectId;
		this.fieldName = fieldName;
		this.affectedObject = affectedObject;
		this.dataObject = dataObject;
		this.initBuilder = initBuilder;
	}
	
	
}
