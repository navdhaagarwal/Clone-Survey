package com.nucleus.sso;

public class BannerStorageVO {

	 
	private  Object[] ssoBannerStorageFields;
	private byte[] imageFile;
	private String contentType;
	
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Object[] getSsoBannerStorageFields() {
		return ssoBannerStorageFields;
	}
	
	public void setSsoBannerStorageFields(Object[] ssoBannerStorageFields) {
		this.ssoBannerStorageFields = ssoBannerStorageFields;
	}
	public byte[] getImageFile() {
		return imageFile;
	}
	public void setImageFile(byte[] imageFile) {
		this.imageFile = imageFile;
	}
	
}
