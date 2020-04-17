package com.nucleus.core.datastore.access.log.couch.service;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AccessLogBaseEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7939268223962345694L;
	
	private String id;
	private String revision;
	
	@JsonProperty("_id")
	public String getId() {
		return id;
	}
	
	@JsonProperty("_id")
	public void setId(String id) {
		this.id = id;
	}
	
	@JsonProperty("_rev")
	public String getRevision() {
		return revision;
	}
	
	@JsonProperty("_rev")
	public void setRevision(String revision) {
		this.revision = revision;
	}	
	
}
