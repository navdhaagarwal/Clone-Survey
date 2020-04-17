package com.nucleus.core.datastore.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.exceptions.SystemException;

public class DataStoreDocument {
	
	private byte [] content; 
	private String fileName;
	private String contentType;
	private Map<String, String> metadata;
	
	public DataStoreDocument(byte[] content, String fileName, String contentType) {
		this(content, fileName, contentType, null);
	}
	
	public DataStoreDocument(byte[] content, String fileName, String contentType, Map<String, String> metadata) {
		if(content == null){
			throw new SystemException("Content cannot be null");
		}
		this.content = content;
		if(StringUtils.isBlank(fileName)){
			throw new SystemException("File name cannot be blank");
		}
		this.fileName = fileName;
		if(StringUtils.isBlank(contentType)){
			throw new SystemException("Content type cannot be blank");
		}
		this.contentType = contentType;
		this.metadata = metadata;
	}


	public byte[] getContent() {
		return content;
	}
	public String getFileName() {
		return fileName;
	}
	public String getContentType() {
		return contentType;
	}
	public Map<String, String> getMetadata() {
		return metadata;
	}
}
