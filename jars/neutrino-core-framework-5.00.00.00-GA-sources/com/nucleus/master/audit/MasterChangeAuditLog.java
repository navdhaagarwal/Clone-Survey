package com.nucleus.master.audit;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class MasterChangeAuditLog extends BaseEntity{

	private String entityURI;
	
	@Lob
	private String javersMessage;
	
	@Lob
	private String neutrinoMessage;
	
	
	public String getEntityURI() {
		return entityURI;
	}

	public void setEntityURI(String entityURI) {
		this.entityURI = entityURI;
	}

	public String getJaversMessage() {
		return javersMessage;
	}

	public void setJaversMessage(String javersMessage) {
		this.javersMessage = javersMessage;
	}

	public String getNeutrinoMessage() {
		return neutrinoMessage;
	}

	public void setNeutrinoMessage(String neutrinoMessage) {
		this.neutrinoMessage = neutrinoMessage;
	}

	
	
	
}
