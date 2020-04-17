package com.nucleus.notificationMaster;

import java.util.Map;

import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

public class NotificationMasterVO {

	private NotificationMaster notificationMaster;
	private Map contextMap;
	private String ownerEntityUri;
	private FieldsMetadata metadata;
	public NotificationMaster getNotificationMaster() {
		return notificationMaster;
	}
	public void setNotificationMaster(NotificationMaster notificationMaster) {
		this.notificationMaster = notificationMaster;
	}
	
	public String getOwnerEntityUri() {
		return ownerEntityUri;
	}
	public void setOwnerEntityUri(String ownerEntityUri) {
		this.ownerEntityUri = ownerEntityUri;
	}
	public FieldsMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(FieldsMetadata metadata) {
		this.metadata = metadata;
	}
	public Map getContextMap() {
		return contextMap;
	}
	public void setContextMap(Map contextMap) {
		this.contextMap = contextMap;
	}
	
	
}
