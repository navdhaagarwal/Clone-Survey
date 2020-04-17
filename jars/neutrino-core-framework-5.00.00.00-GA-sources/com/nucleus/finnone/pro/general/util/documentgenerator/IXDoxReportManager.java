package com.nucleus.finnone.pro.general.util.documentgenerator;

import fr.opensagres.xdocreport.document.IXDocReport;

/**
 * 
 * @author gajendra.jatav
 *
 */
public interface IXDoxReportManager {
	
	IXDocReport getReport(String path, String lookUpAtClassPathOrFilePath);

}
