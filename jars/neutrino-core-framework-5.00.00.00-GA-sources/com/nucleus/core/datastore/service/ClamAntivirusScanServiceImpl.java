package com.nucleus.core.datastore.service;

import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.service.BaseServiceImpl;

import net.bull.javamelody.MonitoredWithSpring;
import net.taldius.clamav.ClamAVScanner;
import net.taldius.clamav.ClamAVScannerFactory;
import net.taldius.clamav.impl.NetworkScanner;

@Named("clamAVService")
@MonitoredWithSpring(name = "clamAVService_IMPL_")
public class ClamAntivirusScanServiceImpl extends BaseServiceImpl implements AntivirusScanService {

    // Host where 'clamd' process is running
    @Value(value = "#{'${clamd.host}'}")
    private String         clamdHost;

    // Port on which 'clamd' process is listening
    @Value(value = "#{'${clamd.port}'}")
    private String         clamdPort;

    // Connection time out to connect 'clamd' process
    @Value(value = "#{'${clamd.timeOut}'}")
    private String         connTimeOut;

    @Value(value = "#{'${antivirus.scanningDisabled}'}")
    private boolean        scanningDisabled;

    private NetworkScanner scanner;
    
    
    public void setScanningDisabled(boolean scanningDisabled) {
		this.scanningDisabled = scanningDisabled;
	}

	public void setClamdHost(String clamdHost) {
        this.clamdHost = clamdHost;
    }

    public String getClamdHost() {
        return this.clamdHost;
    }

    public void setClamdPort(String clamdPort) {
        this.clamdPort = clamdPort;
    }

    public String getClamdPort() {
        return this.clamdPort;
    }

    public void setConnTimeOut(String connTimeOut) {
        this.connTimeOut = connTimeOut;
    }

    public String getConnTimeOut() {
        return this.connTimeOut;
    }


    /**
     * Method to initialize clamAV scanner
     */
    @PostConstruct
    public void initScanner() {

        ClamAVScannerFactory.setClamdHost(clamdHost);

        if (StringUtils.isNotEmpty(clamdPort))
            ClamAVScannerFactory.setClamdPort(Integer.valueOf(clamdPort));

        int connectionTimeOut = Integer.parseInt(connTimeOut);

        if (connectionTimeOut > 0) {
            ClamAVScannerFactory.setConnectionTimeout(connectionTimeOut);
        }
        this.scanner = (NetworkScanner) ClamAVScannerFactory.getScanner();
    }

    public ClamAVScanner getClamAVScanner() {
        return scanner;
    }

   public AntiVirusStatus fileScanner(InputStream fileInputStream) {

        boolean resScan = false;

        try {
            resScan = scanner.performScan(fileInputStream);
            scanner.reset();
            if (resScan) {
                return AntiVirusStatus.FILE_CLEAN;
            } else {
                return AntiVirusStatus.VIRUS_FOUND;
            }

        } catch (Exception e) {
            return AntiVirusStatus.PROBLEM_OCCURED;
        }
    }

	@Override
	public AntiVirusStatus fileScanner(InputStream fileInputStream, String fileName) {
		
		return fileScanner(fileInputStream);
	}

	@Override
	public boolean isScanningDisabled() {
		return scanningDisabled;
	}

}
