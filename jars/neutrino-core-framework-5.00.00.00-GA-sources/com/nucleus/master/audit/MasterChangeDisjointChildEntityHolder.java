package com.nucleus.master.audit;

import com.nucleus.entity.BaseEntity;
import com.nucleus.master.audit.metadata.AuditableClassMetadataFactory;

public class MasterChangeDisjointChildEntityHolder {

	private Object disJointEntity;
	
	private AuditableClassMetadataFactory metadataFactory;

	private String identifier;

	public AuditableClassMetadataFactory getMetadataFactory() {
		return metadataFactory;
	}

	public void setMetadataFactory(AuditableClassMetadataFactory metadataFactory) {
		this.metadataFactory = metadataFactory;
	}

	public Object getDisJointEntity() {
		return disJointEntity;
	}

	public void setDisJointEntity(Object disJointEntity) {
		this.disJointEntity = disJointEntity;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public MasterChangeDisjointChildEntityHolder(Object disJointEntity,String identifier) {
		super();
		this.disJointEntity = disJointEntity;
		this.identifier = identifier;
	}
	
	
	
}
