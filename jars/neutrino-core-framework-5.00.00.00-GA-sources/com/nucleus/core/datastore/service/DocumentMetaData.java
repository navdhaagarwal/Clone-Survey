package com.nucleus.core.datastore.service;

import java.sql.Blob;
import java.util.Date;

public class DocumentMetaData {
	
	
	private byte[]  content;
	
	
	private String 	fileExtension;
	
	private String mimeType;

	private String fileName;
	
	
	public byte[] getContent() {
		return content;
	}


	public void setContent(byte[] content) {
		this.content = content;
	}
	
	
	public String getFileExtension() {
		return fileExtension;
	}


	public void setFileExtension(String fileExtnesion) {
		this.fileExtension = fileExtnesion;
	}


	public String getMimeType() {
		return mimeType;
	}


	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public DocumentMetaData(byte[]  content,String fileExtension) {
        super();
        this.content = content;
        this.fileExtension = fileExtension;
    }
	
	public DocumentMetaData(byte[]  content,String fileExtension,String mimeType,String fileName) {
        super();
        this.content = content;
        this.fileExtension = fileExtension;
        this.mimeType = mimeType;
        this.fileName= fileName;
    }

}
