package com.nucleus.core.datastore.service;

import java.io.InputStream;

public interface AntivirusScanService {

    public AntiVirusStatus fileScanner(InputStream fileInputStream,String fileName);

    public boolean isScanningDisabled();

}
