package com.nucleus.pdfmerger;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.transform.TransformerException;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;

@Named("pdfMergerService")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfMergerUtilityImpl implements PdfMergerUtility {

    private static final String MAX_STORAGE_BYTE = "config.pdfmerger.maxstoragebyte";
    private static final String MAX_MAIN_MEMORY_BYTE = "config.pdfmerger.maxmainmemorybyte";
    protected static final String CONFIGURATION_QUERY = "Configuration.getPropertyValueFromPropertyKey";
    private long maxStorageBytes;
    private long maxMainMemoryBytes;
    public static final long DEFAULT_MAX_MAIN_MEMORY = 2500000000L;
    private static final long DEFAULT_STORAGE = 100000000000L;
    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    public PdfMergerUtilityImpl() {
        try {
            this.maxStorageBytes = Long.parseLong(configurationService
                    .getPropertyValueByPropertyKey(MAX_STORAGE_BYTE,
                            CONFIGURATION_QUERY));
            this.maxMainMemoryBytes = Long.parseLong(configurationService
                    .getPropertyValueByPropertyKey(MAX_MAIN_MEMORY_BYTE,
                            CONFIGURATION_QUERY));
        } catch (Exception e) {
            BaseLoggers.flowLogger
                    .error("config.pdfmerger.maxstoragebyte or config.pdfmerger.maxmainmemorybyte not found in configuration table using default values for MainMemory="
                            + DEFAULT_MAX_MAIN_MEMORY
                            + " Storage="
                            + DEFAULT_STORAGE + " For PDF Merging " + e);
            this.maxMainMemoryBytes = DEFAULT_MAX_MAIN_MEMORY;
            this.maxStorageBytes = DEFAULT_STORAGE;

        }
    }

    @Override
    public void merge(List<InputStream> sources, FileOutputStream outputStream,
            String mergedFileTitle, String creator, String subject)
            throws IOException {
        String title = mergedFileTitle;

        COSStream cosStream = null;
        try {

            cosStream = new COSStream();

            PDFMergerUtility pdfMerger = createPDFMergerUtility(sources,
                    outputStream);
            PDDocumentInformation pdfDocumentInfo = createPDFDocumentInfo(
                    title, creator, subject);
            PDMetadata xmpMetadata = createXMPMetadata(cosStream, title,
                    creator, subject);
            pdfMerger.setDestinationDocumentInformation(pdfDocumentInfo);
            pdfMerger.setDestinationMetadata(xmpMetadata);

            BaseLoggers.flowLogger.info("Merging " + sources.size()
                    + " source documents into one PDF");
            pdfMerger.mergeDocuments(MemoryUsageSetting.setupMixed(
                    maxMainMemoryBytes, maxStorageBytes));

            BaseLoggers.flowLogger.info("PDF merge successful");

        } catch (Exception e) {
            Message message = new Message(
                    CommunicationGeneratorConstants.COMMUNICATION_PDF_MERGER_ERROR,
                    Message.MessageType.ERROR);
            throw ExceptionBuilder
                    .getInstance(BusinessException.class)
                    .setMessage(message)
                    .setOriginalException(e)
                    .setSeverity(
                            ExceptionSeverityEnum.SEVERITY_MEDIUM
                                    .getEnumValue()).build();
        }finally {
            for (InputStream source : sources) {
                IOUtils.closeQuietly(source);
            }
            IOUtils.closeQuietly(cosStream);
            IOUtils.closeQuietly(outputStream);
        }

    }

    private PDFMergerUtility createPDFMergerUtility(List<InputStream> sources,
            OutputStream mergedPDFOutputStream) {
        BaseLoggers.flowLogger.info("Initialising PDF merge utility");
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.addSources(sources);
        pdfMerger.setDestinationStream(mergedPDFOutputStream);
        return pdfMerger;
    }

    private PDDocumentInformation createPDFDocumentInfo(String title,
            String creator, String subject) {
        BaseLoggers.flowLogger
                .info("Setting document info (title, author, subject) for merged PDF");
        PDDocumentInformation documentInformation = new PDDocumentInformation();
        documentInformation.setTitle(title);
        documentInformation.setCreator(creator);
        documentInformation.setSubject(subject);
        return documentInformation;
    }

    private PDMetadata createXMPMetadata(COSStream cosStream, String title,
            String creator, String subject) throws BadFieldValueException,
            TransformerException, IOException {
        BaseLoggers.flowLogger
                .info("Setting XMP metadata (title, author, subject) for merged PDF");
        XMPMetadata xmpMetadata = XMPMetadata.createXMPMetadata();

        // PDF/A-1b properties
        PDFAIdentificationSchema pdfaSchema = xmpMetadata
                .createAndAddPFAIdentificationSchema();
        pdfaSchema.setPart(1);
        pdfaSchema.setConformance("B");

        // Dublin Core properties
        DublinCoreSchema dublinCoreSchema = xmpMetadata
                .createAndAddDublinCoreSchema();
        dublinCoreSchema.setTitle(title);
        dublinCoreSchema.addCreator(creator);
        dublinCoreSchema.setDescription(subject);

        // XMP Basic properties
        XMPBasicSchema basicSchema = xmpMetadata.createAndAddXMPBasicSchema();
        Calendar creationDate = Calendar.getInstance();
        basicSchema.setCreateDate(creationDate);
        basicSchema.setModifyDate(creationDate);
        basicSchema.setMetadataDate(creationDate);
        basicSchema.setCreatorTool(creator);

        // Create and return XMP data structure in XML format
        ByteArrayOutputStream xmpOutputStream = null;
        OutputStream cosXMPStream = null;
        try {
            xmpOutputStream = new ByteArrayOutputStream();
            cosXMPStream = cosStream.createOutputStream();
            new XmpSerializer().serialize(xmpMetadata, xmpOutputStream, true);
            cosXMPStream.write(xmpOutputStream.toByteArray());
            return new PDMetadata(cosStream);
        } finally {
            IOUtils.closeQuietly(xmpOutputStream);
            IOUtils.closeQuietly(cosXMPStream);
        }
    }

}
