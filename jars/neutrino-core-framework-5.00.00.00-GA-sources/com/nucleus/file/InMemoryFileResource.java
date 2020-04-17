package com.nucleus.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.FileSystemResource;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class InMemoryFileResource extends FileSystemResource {

	
	byte[] fileContent;
	
    File file;
	
	public InMemoryFileResource (byte[] bs,String fileName)
	{ 
		super(new InMemoryFile(fileName));
		file=super.getFile();
		this.fileContent=bs;
	}
	
	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isOpen() {
		return true;
	}


	@Override
	public long contentLength() throws IOException {
		return this.fileContent.length;
	}


	@Override
	public String getFilename() {
		return file.getName();
	}


	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(this.fileContent);
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	

}
