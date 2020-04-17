package com.nucleus.sso.imageupload;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class SsoBannerStorage extends BaseEntity {

	private static final long serialVersionUID = 1L;
	private String fileName;
	private String storageId;
	private int imageStatus;
	
	@Column(name = "IMAGE_TITLE", length = 50)
	private String imageTitle;
	
	@Column(name = "IMAGE_CAPTION", length = 50)
	private String imageCaption;
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getStorageId() {
		return storageId;
	}
	
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
	
	public int getActive() {
		return imageStatus;
	}
	
	public void setActive(int active) {
		this.imageStatus = active;
	}

	public String getImageTitle() {
		return imageTitle;
	}

	public void setImageTitle(String imageTitle) {
		this.imageTitle = imageTitle;
	}

	public String getImageCaption() {
		return imageCaption;
	}

	public void setImageCaption(String imageCaption) {
		this.imageCaption = imageCaption;
	}
	
	

}
