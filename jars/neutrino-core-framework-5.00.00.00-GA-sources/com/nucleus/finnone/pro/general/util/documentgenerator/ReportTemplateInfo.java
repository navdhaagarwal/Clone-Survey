package com.nucleus.finnone.pro.general.util.documentgenerator;

import java.io.File;

public class ReportTemplateInfo {

	private Long lastModifiedTimeStamp;
		
	private File file;

	public Long getLastModifiedTimeStamp() {
		return lastModifiedTimeStamp;
	}

	
	public boolean isTemplateModified()
	{
		
		Long currentLastModifiedTimeStamp=this.file.lastModified();
		
		if(currentLastModifiedTimeStamp.equals(lastModifiedTimeStamp))
		{
			return false;
		}
		return true;

	}
	
	public boolean isNotTemplateModified()
	{
		return !isTemplateModified();
	}
	
	public void setLastModifiedTimeStamp(Long lastModifiedTimeStamp) {
		this.lastModifiedTimeStamp = lastModifiedTimeStamp;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

		
	
}
