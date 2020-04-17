package com.nucleus.finnone.pro.general.util.documentgenerator;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.util.Collection;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.io.XDocArchive;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.discovery.IXDocReportFactoryDiscovery;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;

public class NeutrinoXDocReportRegistry extends XDocReportRegistry {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	private static final String FILES_TYPE_ERROR = "Impossible to create report for the input stream. The report loader supports only [{0}] files type.";

    private static final NeutrinoXDocReportRegistry INSTANCE = new NeutrinoXDocReportRegistry();

    
	@Override
	public IXDocReport createReport(XDocArchive documentArchive) throws IOException, XDocReportException {
		initializeIfNeeded();
		for (IXDocReportFactoryDiscovery discovery : getReportFactoryDiscoveries()) {
			if (discovery != null && discovery.isAdaptFor(documentArchive)) {
				IXDocReport report = discovery.createReport();
				if (report != null) {
					report.setDocumentArchive(documentArchive);
				}
				return report;
			}
		}

		throw new XDocReportException(format(FILES_TYPE_ERROR, getFilesType()));
	}

	private String getFilesType() {
		StringBuilder filesType = new StringBuilder();
		Collection<IXDocReportFactoryDiscovery> discoveries = getReportFactoryDiscoveries();
		for (IXDocReportFactoryDiscovery discovery : discoveries) {
			if (discovery != null) {
				if (filesType.length() > 0) {
					filesType.append(",");
				}
				filesType.append(discovery.getMimeMapping().getExtension());
			}
		}
		return filesType.toString();
	}
	
	 public static NeutrinoXDocReportRegistry getRegistry()
	    {
	        return INSTANCE;
	    }
}
