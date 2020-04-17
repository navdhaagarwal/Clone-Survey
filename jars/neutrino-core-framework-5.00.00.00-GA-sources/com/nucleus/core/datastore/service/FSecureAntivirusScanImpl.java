package com.nucleus.core.datastore.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.antivirus.AbstractAntivirusScanServiceImpl;
import com.nucleus.logging.BaseLoggers;
/**
 * 
 * @author gajendra.jatav
 * Anti-virus scanner for FSecure 
 * FSecure doesn't provide any api for scanning so this implementations calls FSecure command for scanning a file.
 * 
 */
public class FSecureAntivirusScanImpl extends AbstractAntivirusScanServiceImpl {
	
	public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
	
	/**
	 * set antivirus.fSecureCommand in properties file
	 * fSecureCommand is file scanning command to be run on system.
	 * Ex. on windows F-Secure\\Anti-Virus\\fsav.exe
	 */
    @Value(value = "#{'${antivirus.fSecureCommand}'}")
	private String fSecureCommand;
	

    @Value(value = "#{'${antivirus.fSecure.tmpDir}'}")
    private String        tmpDirForScanning;

    
    
	public String getTmpDirForScanning() {
		return tmpDirForScanning;
	}

	public void setTmpDirForScanning(String tmpDirForScanning) {
		this.tmpDirForScanning = tmpDirForScanning;
	}

	public static String getTmpDir() {
		return TMP_DIR;
	}

	public String getfSecureCommand() {
		return fSecureCommand;
	}

	public void setfSecureCommand(String fSecureCommand) {
		this.fSecureCommand = fSecureCommand;
	}

	@Override
	public AntiVirusStatus fileScanner(InputStream fileInputStream, String fileName) {

		String uploadedFileName=UUID.randomUUID().toString().substring(0, 5)+"_"+fileName;
		File uploadedFile=new File(getTempDir()+File.separator+uploadedFileName);
		try {
			
			FileUtils.copyInputStreamToFile(fileInputStream, uploadedFile);
			
		} catch (IOException e) {
			BaseLoggers.flowLogger.error("Not able write file ",e);
			return AntiVirusStatus.PROBLEM_OCCURED;
		}
		
		return executeAVonFile(uploadedFile);
	}

	private String getTempDir() {
		if(StringUtils.isNoneEmpty((this.tmpDirForScanning)))
		{
			return this.tmpDirForScanning;
		}
		else
		{
			return TMP_DIR;
		}
	}

	private AntiVirusStatus executeAVonFile(File uploadedFile) {
		
		String command=fSecureCommand+" "+uploadedFile.getAbsolutePath();
	    try {
	        Process process = Runtime.getRuntime().exec(command);
	        BufferedReader reader=new BufferedReader( new InputStreamReader(process.getInputStream()));
	        String s; 
	        boolean virusFound=false;
	        while ((s = reader.readLine()) != null){
	        	virusFound=pareseOutputAndGetResult(s);
	        	if(virusFound)
	        	{
	        	        break;
	        	}
	        }
	        reader.close();
	        if(virusFound)
	        {
	        	return AntiVirusStatus.VIRUS_FOUND;
	        }
	        else
	        {
	        	return AntiVirusStatus.FILE_CLEAN;
	        }
	    } catch (IOException e) {
	    	BaseLoggers.flowLogger.error("Error occured while scaning file ",e);
	    	return AntiVirusStatus.PROBLEM_OCCURED;
	    }
	}

	private boolean pareseOutputAndGetResult(String s) {
		if(s.matches("Viruses:[\\s]+[0-9]+"))
    	{
    		int result=Integer.parseInt(s.replaceAll("Viruses:[\\s]+", "").trim());
    		if(result>0)
    		{
    	        return true;
    		}
    	}
		return false;
	}

}
