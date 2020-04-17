package com.nucleus.antivirus;

import org.springframework.beans.factory.annotation.Value;

import com.nucleus.core.datastore.service.AntivirusScanService;

/**
 * 
 * @author gajendra.jatav
 *
 */
public abstract class AbstractAntivirusScanServiceImpl implements AntivirusScanService{

    @Value(value = "#{'${antivirus.scanningDisabled}'}")
    private boolean        scanningDisabled;
	
	@Override
	public boolean isScanningDisabled() {

		return scanningDisabled;
	}

}
