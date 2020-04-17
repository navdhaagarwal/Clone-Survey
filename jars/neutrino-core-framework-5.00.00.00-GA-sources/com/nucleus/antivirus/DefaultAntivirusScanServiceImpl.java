package com.nucleus.antivirus;

import java.io.InputStream;

import com.nucleus.core.datastore.service.AntiVirusStatus;

/**
 * 
 * Default Antivirus Scan service it's return scan status as will be AntiVirusStatus.FILE_CLEAN
 * @author gajendra.jatav
 *
 */
public class DefaultAntivirusScanServiceImpl extends AbstractAntivirusScanServiceImpl{

	
	@Override
	public boolean isScanningDisabled() {

		return true;
	}
	
	@Override
	public AntiVirusStatus fileScanner(InputStream fileInputStream, String fileName) {
		return AntiVirusStatus.FILE_CLEAN;
	}

}
