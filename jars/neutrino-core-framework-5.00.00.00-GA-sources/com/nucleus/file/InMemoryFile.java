package com.nucleus.file;

import java.io.File;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class InMemoryFile extends File{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String fileName;
	
	public InMemoryFile(String pathname) {
		super(pathname);
		this.fileName=pathname;
	}

	@Override
	public String getName() {
		return fileName;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	

}
