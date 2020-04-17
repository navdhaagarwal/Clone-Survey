package com.nucleus.security.upload;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.datastore.util.FileSanitizer;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by gajendra.jatav on 10/18/2019.
 */
@Named("mimeTypeSanitizer")
public class MimeTypeSanitizerImpl implements MimeTypeSanitizer {

    private static final Map<String, String> image = new HashMap<>();

    private static final Map<String, String> msoffice = new HashMap<>();

    private static final Map<String, String> ods = new HashMap<>();

    private static final Map<String, String> text = new HashMap<>();

    private static final Map<String, String> xml = new HashMap<>();

    private static final Map<String, String> odt = new HashMap<>();

    private static final String MHTML = "MHTML";

    private static final Map<String, String> msg = new HashMap<>();

    private static final String XBITMAP = "image/x-xbitmap";

    private static final Map<String, String> font = Collections
            .unmodifiableMap(new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;

                {
                    put("TTF", "TTF");
                    put("OTF", "OTF");
                }
            });

    private static final Map<String, Map<String, String>> supportedFileTypes = new HashMap<>();

    private static final Map<String, String> supportedExtensionTypes = new HashMap<>();

    static {

        image.put("JPG", "JPG");
        image.put("JPEG", "JPEG");
        image.put("GIF", "GIF");
        image.put("BMP", "BMP");
        image.put("PNG", "PNG");
        image.put("TIFF", "TIFF");


        msoffice.put("XLS", "XLS");
        msoffice.put("XLSX", "XLSX");
        msoffice.put("DOC", "DOC");
        msoffice.put("DOCX", "DOCX");
        msoffice.put("PDF", "PDF");
        msoffice.put("MSG", "MSG");
        msoffice.put("OFT", "OFT");

        ods.put("ODS", "ODS");

        text.put("TXT", "TXT");
        text.put("FTL", "FTL");
        text.put("HTML", "HTML");
        text.put("CSV", "CSV");
        text.put("XML", "XML");
        text.put("VM", "VM");

        xml.put("XML", "XML");

        odt.put("ODT", "ODT");

        msg.put(MHTML, MHTML);

        supportedFileTypes.put("image/jpeg", image);
        supportedFileTypes.put("image/pjpeg", image);
        supportedFileTypes.put("image/jpg", image);
        supportedFileTypes.put(XBITMAP, image);
        supportedFileTypes.put("image/jp_", image);
        supportedFileTypes.put("application/jpg", image);
        supportedFileTypes.put("application/x-jpg", image);
        supportedFileTypes.put("image/pipeg", image);
        supportedFileTypes.put("image/vnd.swiftview-jpeg", image);
        supportedFileTypes.put("image/jpeg", image);
        supportedFileTypes.put("image/jpg", image);
        supportedFileTypes.put("image/jpe_", image);
        supportedFileTypes.put("image/pjpeg", image);
        supportedFileTypes.put("image/vnd.swiftview-jpeg", image);
        supportedFileTypes.put("image/gif", image);
        supportedFileTypes.put(XBITMAP, image);
        supportedFileTypes.put("image/gi_", image);
        supportedFileTypes.put("image/bmp", image);
        supportedFileTypes.put("image/x-windows-bmp", image);
        supportedFileTypes.put("image/x-bmp", image);
        supportedFileTypes.put("image/x-bitmap", image);
        supportedFileTypes.put(XBITMAP, image);
        supportedFileTypes.put("image/x-win-bitmap", image);
        supportedFileTypes.put("image/x-windows-bmp", image);
        supportedFileTypes.put("image/ms-bmp", image);
        supportedFileTypes.put("image/x-ms-bmp", image);
        supportedFileTypes.put("application/bmp", image);
        supportedFileTypes.put("application/x-bmp", image);
        supportedFileTypes.put("application/x-win-bitmap ", image);
        supportedFileTypes.put("image/png", image);
        supportedFileTypes.put("application/png", image);
        supportedFileTypes.put("application/x-png", image);
        supportedFileTypes.put("application/msword", msoffice);
        supportedFileTypes.put("application/doc", msoffice);
        supportedFileTypes.put("appl/text", msoffice);
        supportedFileTypes.put("application/vnd.msword", msoffice);
        supportedFileTypes.put("application/vnd.ms-word", msoffice);
        supportedFileTypes.put("application/winword", msoffice);
        supportedFileTypes.put("application/word", msoffice);
        supportedFileTypes.put("application/x-msw6", msoffice);
        supportedFileTypes.put("application/x-msword", msoffice);
        supportedFileTypes.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                msoffice);
        supportedFileTypes.put("application/zip", msoffice);
        supportedFileTypes.put("application/x-tika-ooxml", msoffice);
        supportedFileTypes.put("application/vnd.msexcel", msoffice);
        supportedFileTypes.put("application/vnd.ms-excel", msoffice);
        supportedFileTypes.put("application/excel", msoffice);
        supportedFileTypes.put("application/msexcel", msoffice);
        supportedFileTypes.put("application/x-msexcel", msoffice);
        supportedFileTypes.put("application/x-ms-excel", msoffice);
        supportedFileTypes.put("application/vnd.ms-excel", msoffice);
        supportedFileTypes.put("application/x-excel", msoffice);
        supportedFileTypes.put("application/x-dos_ms_excel", msoffice);
        supportedFileTypes.put("application/xls", msoffice);
        supportedFileTypes.put("application/x-tika-msoffice", msoffice);
        supportedFileTypes.put("application/vnd.oasis.opendocument.spreadsheet", ods);
        supportedFileTypes.put("application/x-vnd.oasis.opendocument.spreadsheet", ods);
        supportedFileTypes.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                msoffice);
        supportedFileTypes.put("application/pdf", msoffice);
        supportedFileTypes.put("application/x-pdf", msoffice);
        supportedFileTypes.put("application/acrobat", msoffice);
        supportedFileTypes.put("applications/vnd.pdf", msoffice);
        supportedFileTypes.put("text/pdf", msoffice);
        supportedFileTypes.put("text/x-pdf", msoffice);
        supportedFileTypes.put("text/comma-separated-values", text);
        supportedFileTypes.put("text/csv", text);
        supportedFileTypes.put("application/csv", text);
        supportedFileTypes.put("text/plain", text);
        supportedFileTypes.put("application/txt", text);
        supportedFileTypes.put("text/anytext", text);
        supportedFileTypes.put("widetext/plain", text);
        supportedFileTypes.put("widetext/paragraph", text);
        supportedFileTypes.put("text/xml", xml);
        supportedFileTypes.put("application/xml", xml);
        supportedFileTypes.put("application/x-xml", xml);
        supportedFileTypes.put("application/xhtml+xml", xml);
        supportedFileTypes.put("text/anytext", text);
        supportedFileTypes.put("text/html", text);
        supportedFileTypes.put("text/plain", text);
        supportedFileTypes.put("application/vnd.oasis.opendocument.text", odt);
        supportedFileTypes.put("application/x-vnd.oasis.opendocument.text", odt);
        supportedFileTypes.put("image/tiff", image);
        supportedFileTypes.put("message/rfc822", msg);
        supportedFileTypes.put("application/x-font-ttf", font);
        supportedFileTypes.put("application/x-font-TrueType", font);
        supportedFileTypes.put("application/x-font-truetype", font);
        supportedFileTypes.put("application/octet-stream", font);
        supportedFileTypes.put("font/otf", font);
        supportedFileTypes.put("application/font-otf", font);
        supportedFileTypes.put("application/font", font);
        supportedFileTypes.put("application/otf", font);
        supportedFileTypes.put("application/x-font-otf", font);
        supportedFileTypes.put("application/x-font-opentype", font);
        supportedFileTypes.put("font/opentype", font);


        // adding supportedExtensionTypes
        supportedExtensionTypes.put("JPG", "JPG");
        supportedExtensionTypes.put("JPEG", "JPEG");
        supportedExtensionTypes.put("GIF", "GIF");
        supportedExtensionTypes.put("BMP", "BMP");
        supportedExtensionTypes.put("PNG", "PNG");
        supportedExtensionTypes.put("DOC", "DOC");
        supportedExtensionTypes.put("DOCX", "DOCX");
        supportedExtensionTypes.put("XLS", "XLS");
        supportedExtensionTypes.put("ODS", "ODS");
        supportedExtensionTypes.put("XLSX", "XLSX");
        supportedExtensionTypes.put("PDF", "PDF");
        supportedExtensionTypes.put("CSV", "CSV");
        supportedExtensionTypes.put("TXT", "TXT");
        supportedExtensionTypes.put("KEY", "TXT");
        supportedExtensionTypes.put("XML", "XML");
        supportedExtensionTypes.put("JRXML", "XML");
        supportedExtensionTypes.put("FTL", "FTL");
        supportedExtensionTypes.put("HTML", "HTML");
        supportedExtensionTypes.put("ODT", "ODT");
        supportedExtensionTypes.put("TIF", "TIFF");
        supportedExtensionTypes.put("MSG", "MSG");
        supportedExtensionTypes.put("OFT", "OFT");
        supportedExtensionTypes.put("HTM", "HTML");
        supportedExtensionTypes.put("MHT", MHTML);
        supportedExtensionTypes.put("VM", "VM");
        supportedExtensionTypes.put("OTF", "OTF");
        supportedExtensionTypes.put("TTF", "TTF");

    }

    @Inject
    @Named("tika")
    private Tika tika;

    private boolean mimeSanitizerEnabled = true;

    @Value("${additional.mime.extension.mapping}")
    private String additionalMimeMappingStr;

    @PostConstruct
    public void init(){
        Map<String, List<String>> additionalMimeMapping = parse(additionalMimeMappingStr);
        if(additionalMimeMapping!=null && !additionalMimeMapping.isEmpty()){
            additionalMimeMapping.forEach((mimeType,extensions)->{
                supportedFileTypes.putIfAbsent(mimeType, new HashMap<>());
                Map<String, String> extensionMap = extensions.stream().collect(
                    Collectors.toMap(element->element.toUpperCase(), element->element.toUpperCase()));
                supportedExtensionTypes.putAll(extensionMap);
                supportedFileTypes.get(mimeType).putAll(extensionMap);
            });
        }
    }

    private Map<String,List<String>> parse(String additionalMimeMappingStr) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(additionalMimeMappingStr,Map.class);
        } catch (IOException e) {
            BaseLoggers.flowLogger.error("invalid json in additional.mime.extension.mapping");
            return null;
        }
    }

    @Value(value = "#{'${security.mime.sanitizer.enabled}'}")
    public void setMimeSanitizerEnabled(String mimeSanitizerEnabled) {
        if (StringUtils.isEmpty(mimeSanitizerEnabled)
                || "${security.mime.sanitizer.enabled}".equalsIgnoreCase(mimeSanitizerEnabled)) {
            this.mimeSanitizerEnabled = true;
            return;
        }
        this.mimeSanitizerEnabled = Boolean.parseBoolean(mimeSanitizerEnabled);
    }



    @Override
    public void sanitize(CommonsMultipartFile multipartFile) {
        if (!mimeSanitizerEnabled){
            BaseLoggers.exceptionLogger.debug("mimeSanitizer not enabled ");
            return;
        }
        String fileName = multipartFile.getOriginalFilename();
        String contentType = fileName.substring(fileName.lastIndexOf('.') + 1);
        String extensionType = supportedExtensionTypes.get(contentType
                .toUpperCase());

        if (StringUtils.isEmpty(extensionType)) {
            BaseLoggers.exceptionLogger.error("******content type is ***** {} ", contentType);
            throw new SystemException("File Format not supported");
        }
        String mimeType;
        ByteArrayInputStream byteArrayInputStream = getInputStreamFromBytes(multipartFile.getBytes());
        try {
            mimeType = tika.detect(byteArrayInputStream);
            Map<String, String> fileTypeMap = supportedFileTypes.get(mimeType);
            if (fileTypeMap == null
                    || StringUtils.isEmpty(fileTypeMap.get(extensionType))) {

                throw new SystemException(
                        "File Type not supported and its not matching with extension ");
            }
        } catch (Exception e) {
            BaseLoggers.exceptionLogger
                    .error("Error occured while matching the fileType and extension type",
                            e.fillInStackTrace());
            throw new SystemException(
                    "File Type not supported and its not matching with extension");
        }

        sanitizeContent(mimeType, extensionType, byteArrayInputStream);
    }

    private void sanitizeContent(String mimeType, String extensionType, ByteArrayInputStream byteArrayInputStream) {
        Map<String, FileSanitizer> sanitizers = NeutrinoSpringAppContextUtil
                .getBeansOfType(FileSanitizer.class);
        for (Map.Entry<String, FileSanitizer> sanitizerEntry : sanitizers
                .entrySet()) {
            FileSanitizer fileSanitizer = sanitizerEntry.getValue();
            if (fileSanitizer.canSanitize(mimeType, extensionType)) {
                byteArrayInputStream.reset();
                fileSanitizer.checkSanity(byteArrayInputStream);
            }
        }
    }

    private ByteArrayInputStream getInputStreamFromBytes(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

}
