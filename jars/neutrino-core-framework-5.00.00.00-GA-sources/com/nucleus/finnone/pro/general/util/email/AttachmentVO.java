package com.nucleus.finnone.pro.general.util.email;

public class AttachmentVO {
	public static enum AttachmentStyle{
		INLINE, ATTACHMENT;
	}
	private String fileName;
	private String filePath;
	private byte[] content;
	private String contentType;
	
	

	public AttachmentVO(String fileName, byte[] content, String contentType,
			AttachmentStyle attachmentStyle) {
		super();
		this.fileName = fileName;
		this.content = content;
		this.contentType = contentType;
		this.attachmentStyle = attachmentStyle;
	}
	private AttachmentStyle attachmentStyle; 
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public AttachmentStyle getAttachmentStyle() {
		return attachmentStyle;
	}
	public void setAttachmentStyle(AttachmentStyle attachmentStyle) {
		this.attachmentStyle = attachmentStyle;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public void validate(){
		
	}
	
}

