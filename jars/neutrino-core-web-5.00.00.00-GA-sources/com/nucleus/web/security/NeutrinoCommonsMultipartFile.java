package com.nucleus.web.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.nucleus.security.upload.MimeTypeSanitizer;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.datastore.service.AntiVirusStatus;
import com.nucleus.core.datastore.service.AntivirusScanService;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.security.filesanity.UploadSanitizer;

/**
 *
 * Enables lazy scanning of uploaded files
 * @author gajendra.jatav
 */
public class NeutrinoCommonsMultipartFile extends CommonsMultipartFile{


	private static final String COMMA = ",";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean isScanned=false;

	private CommonsMultipartFile commonsMultipartFile;

	public NeutrinoCommonsMultipartFile(CommonsMultipartFile commonsMultipartFile) {
		super(commonsMultipartFile.getFileItem());
		this.commonsMultipartFile=commonsMultipartFile;
	}

	@Override
	public InputStream getInputStream() throws IOException {

		if(isScanned)
		{
			return commonsMultipartFile.getInputStream();
		}
		sanitizeFile();
		return commonsMultipartFile.getInputStream();
	}

	@Override
	public byte[] getBytes() {
		if(isScanned)
		{
			return commonsMultipartFile.getBytes();
		}
		sanitizeFile();
		return commonsMultipartFile.getBytes();

	}

	private void sanitizeFile(){
		scanFile();
		sanitizeFileContent();
	}
	private void scanFile()
	{	
		validateFileExtension();
		AntivirusScanService antivirusScanService=NeutrinoSpringAppContextUtil.getBeanByName("antivirusScanService", AntivirusScanService.class);		
		if(antivirusScanService.isScanningDisabled())
		{
			isScanned=true;
			return;
		}
		AntiVirusStatus scanStatus=null;
		try{
			scanStatus=antivirusScanService.fileScanner(commonsMultipartFile.getInputStream(),this.commonsMultipartFile.getOriginalFilename());

		}catch (Exception e) {
			BaseLoggers.exceptionLogger.debug("Error occurred while scanning file",e);
			throw new SystemException(e);
		}

		if(AntiVirusStatus.FILE_CLEAN==scanStatus )
		{
			isScanned=true;
		}
		else
		{
			throw new XssException("File seems to be malicious");
		}
	}
	
	private void sanitizeFileContent() {

		MimeTypeSanitizer mimeTypeSanitizer = NeutrinoSpringAppContextUtil.getBeanByName("mimeTypeSanitizer",
				MimeTypeSanitizer.class);
		try {
			mimeTypeSanitizer.sanitize(commonsMultipartFile);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.info("Error occurred while sanitizing file",e);
			throw new SystemException(e);
		}
	}

	@Override
	public String getName() {

		return commonsMultipartFile.getName();
	}

	@Override
	public String getOriginalFilename() {
		return commonsMultipartFile.getOriginalFilename();
	}

	@Override
	public String getContentType() {
		return commonsMultipartFile.getContentType();
	}

	@Override
	public boolean isEmpty() {
		return commonsMultipartFile.isEmpty();
	}

	@Override
	public long getSize() {
		return commonsMultipartFile.getSize();
	}

	@Override
	public void transferTo(File dest) throws IOException {
		if(isScanned)
		{
			commonsMultipartFile.transferTo(dest);
		}
		sanitizeFile();

		commonsMultipartFile.transferTo(dest);
	}

	@Override
	public String getStorageDescription() {
		return commonsMultipartFile.getStorageDescription();
	}

	private void validateFileExtension(){

		NeutrinoCommonsMultipartFileConfig commonsMultipartFileConfig = new NeutrinoCommonsMultipartFileConfig();
		for (String extension : commonsMultipartFileConfig.getRestrictedFileExtension().split(COMMA)) {
			if(this.commonsMultipartFile.getOriginalFilename().endsWith(extension)){
				throw new XssException("File Extension "+extension+" is not supported");
			}
		}

	}
}
