/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.fileTemplateService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Named;

import org.apache.commons.io.IOUtils;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

// TODO: Auto-generated Javadoc
/**
 * The Class FileTemplateServiceImpl.
 * @author Nucleus Software India Pvt Ltd
 */
@Named("fileTemplateService")
public class FileTemplateServiceImpl extends BaseServiceImpl implements FileTemplateService {

    /** The can generate pdf. */
    private static boolean canGeneratePdf    = false;

    /** The can itext check done. */
    private static boolean canItextCheckDone = false;

    /** The options. */
    private final Options  options           = Options.getTo(ConverterTypeTo.PDF);

    /* (non-Javadoc)
     * @see com.nucleus.fileTemplateService.FileTemplateService#generateDocument(java.io.File, java.util.Map, fr.opensagres.xdocreport.template.formatter.FieldsMetadata)
     */
    @Override
    public Object generateDocument(InputStream inputStream, String key, Map<Object, Object> context, FieldsMetadata metadata) {

        Object out = null;
        try {
            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(inputStream, key,
                    TemplateEngineKind.Freemarker, true);
            if (report != null) {
                out = processTemplateDocument(report, context, metadata);
            }
            return out;
        } catch (Exception e) {
            throw new SystemException("Exception while generating document from template. ", e);
        }
    }

    /* (non-Javadoc)
     * @see com.nucleus.fileTemplateService.FileTemplateService#generateDocumentFromKey(java.lang.String, java.util.Map, fr.opensagres.xdocreport.template.formatter.FieldsMetadata)
     */
    @Override
    public Object generateDocumentFromKey(String key, Map<Object, Object> context, FieldsMetadata metadata) {
        IXDocReport report = XDocReportRegistry.getRegistry().getReport(key);
        Object out = null;
        if (report != null) {
            out = processTemplateDocument(report, context, metadata);
        }
        return out;
    }

    /**
     * Process template document.
     *
     * @param report the report
     * @param context the context
     * @param metadata the metadata
     * @return the object
     */
    private Object processTemplateDocument(IXDocReport report, Map<Object, Object> context, FieldsMetadata metadata) {
        ByteArrayOutputStream out = null;
        if (!canItextCheckDone) {
            try {
                Class.forName("fr.opensagres.xdocreport.converter.odt.odfdom.itext.ODF2PDFViaITextConverter");
                canGeneratePdf = true;
            } catch (ClassNotFoundException e) {
                canGeneratePdf = false;
                BaseLoggers.exceptionLogger
                        .error("ClassNotFoundException while loading class:fr.opensagres.xdocreport.converter.odt.odfdom.itext.ODF2PDFViaITextConverter");
            }
        }
        canItextCheckDone = true;
        try {

            IContext reportContext = report.createContext();
            if (metadata != null) {
                report.setFieldsMetadata(metadata);
            }
            if (context != null) {
                for (Entry<Object, Object> contextEntry : context.entrySet()) {
					if (ValidatorUtils.notNull(contextEntry.getKey())) {
						if (contextEntry.getKey() instanceof String) {
							reportContext.put((String) contextEntry.getKey(), contextEntry.getValue());
						} else {
							reportContext.put(contextEntry.getKey().toString(), contextEntry.getValue());
						}
					}
                }
            }
            out = new ByteArrayOutputStream();
            if (canGeneratePdf) {
                report.convert(reportContext, options, out);
            } else {
                report.process(reportContext, out);
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new SystemException("Exception while generating document from template. ", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

}
