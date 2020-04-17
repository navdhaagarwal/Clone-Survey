package com.nucleus.core.datastore.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.ektorp.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.geo.Box;
import org.springframework.data.mongodb.core.geo.Circle;
import org.springframework.data.mongodb.core.geo.Distance;
import org.springframework.data.mongodb.core.geo.Point;

import com.nucleus.address.Address;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.datastore.util.FileSanitizer;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;

public class OracleDatastoreServiceImpl extends BaseServiceImpl implements
		DatastorageService {
	public static final String FILE_NAME_MIDDLE_SEPARATOR = "_NTR_";
	
    @Value(value = "${block.useruploaded.maliciouscontent}")
    private boolean  blockUserUploadedMaliciouscontent;
	private static final Map<String, String> image = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;

				{
					put("JPG", "JPG");
					put("JPEG", "JPEG");
					put("GIF", "GIF");
					put("BMP", "BMP");
					put("PNG", "PNG");
					put("TIFF", "TIFF");
				}
			});
	
	private static final Map<String, String> msoffice = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;

				{
					put("XLS", "XLS");
					put("XLSX", "XLSX");
					put("DOC", "DOC");
					put("DOCX", "DOCX");
					put("PDF", "PDF");
					put("MSG", "MSG");
					put("OFT", "OFT");
				}
			});
	private static final Map<String, String> ods = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;

				{
					put("ODS", "ODS");
				}
			});
	
	private static final Map<String, String> text = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;

				{
					put("TXT", "TXT");
					put("FTL", "FTL");
					put("HTML", "HTML");
					put("CSV", "CSV");
                    put("XML", "XML");
                    put("VM", "VM");
				}
			});
	private static final Map<String, String> xml = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;

				{
					put("XML", "XML");

				}
			});
	private static final Map<String, String> odt = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;

				{
					put("ODT", "ODT");
				}
			});
	
	private static final Map<String, String> msg = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;

				{
					
					put("MHTML", "MHTML");
					
				}
			});

	private static final Map<String, String> font = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;

				{
					put("TTF", "TTF");
					put("OTF", "OTF");
				}
			});

	private static final Map<String, Map<String, String>> supportedFileTypes = Collections
			.unmodifiableMap(new HashMap<String, Map<String, String>>() {
				private static final long serialVersionUID = 1L;

				{

					put("image/jpeg", image);
					put("image/pjpeg", image);
					put("image/jpg", image);
					put("image/x-xbitmap", image);
					put("image/jp_", image);
					put("application/jpg", image);
					put("application/x-jpg", image);
					put("image/pipeg", image);
					put("image/vnd.swiftview-jpeg", image);
					put("image/jpeg", image);
					put("image/jpg", image);
					put("image/jpe_", image);
					put("image/pjpeg", image);
					put("image/vnd.swiftview-jpeg", image);
					put("image/gif", image);
					put("image/x-xbitmap", image);
					put("image/gi_", image);
					put("image/bmp", image);
					put("image/x-windows-bmp", image);
					put("image/x-bmp", image);
					put("image/x-bitmap", image);
					put("image/x-xbitmap", image);
					put("image/x-win-bitmap", image);
					put("image/x-windows-bmp", image);
					put("image/ms-bmp", image);
					put("image/x-ms-bmp", image);
					put("application/bmp", image);
					put("application/x-bmp", image);
					put("application/x-win-bitmap ", image);
					put("image/png", image);
					put("application/png", image);
					put("application/x-png", image);
					put("application/msword", msoffice);
					put("application/doc", msoffice);
					put("appl/text", msoffice);
					put("application/vnd.msword", msoffice);
					put("application/vnd.ms-word", msoffice);
					put("application/winword", msoffice);
					put("application/word", msoffice);
					put("application/x-msw6", msoffice);
					put("application/x-msword", msoffice);
					put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
							msoffice);
					put("application/zip", msoffice);
					put("application/x-tika-ooxml", msoffice);
					put("application/vnd.msexcel", msoffice);
					put("application/vnd.ms-excel", msoffice);
					put("application/excel", msoffice);
					put("application/msexcel", msoffice);
					put("application/x-msexcel", msoffice);
					put("application/x-ms-excel", msoffice);
					put("application/vnd.ms-excel", msoffice);
					put("application/x-excel", msoffice);
					put("application/x-dos_ms_excel", msoffice);
					put("application/xls", msoffice);
					put("application/x-tika-msoffice", msoffice);
					put("application/vnd.oasis.opendocument.spreadsheet", ods);
					put("application/x-vnd.oasis.opendocument.spreadsheet", ods);
					put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
							msoffice);
					put("application/pdf", msoffice);
					put("application/x-pdf", msoffice);
					put("application/acrobat", msoffice);
					put("applications/vnd.pdf", msoffice);
					put("text/pdf", msoffice);
					put("text/x-pdf", msoffice);
					put("text/comma-separated-values", text);
					put("text/csv", text);
					put("application/csv", text);
					// put("text/anytext","CSV");
					put("text/plain", text);
					put("application/txt", text);
					// put("browser/internal","TXT");
					put("text/anytext", text);
					put("widetext/plain", text);
					put("widetext/paragraph", text);
					put("text/xml", xml);
					put("application/xml", xml);
					put("application/x-xml", xml);
					put("application/xhtml+xml",xml);
					put("text/anytext", text);
					put("text/html", text);
					put("text/plain", text);
					put("application/vnd.oasis.opendocument.text", odt);
					put("application/x-vnd.oasis.opendocument.text", odt);
					put("image/tiff", image);							
					put("message/rfc822", msg);
					put("application/x-font-ttf", font);
					put("application/x-font-TrueType", font);
					put("application/x-font-truetype", font);
					put("application/octet-stream", font);
					put("font/otf", font);
					put("application/font-otf", font);
					put("application/font", font);
					put("application/otf", font);
					put("application/x-font-otf", font);
					put("application/x-font-opentype", font);
					put("font/opentype", font);
					
				}
			});

	private static final Map<String, String> supportedExtensionTypes = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				private static final long serialVersionUID = 1L;
				{
					put("JPG", "JPG");
					put("JPEG", "JPEG");
					put("GIF", "GIF");
					put("BMP", "BMP");
					put("PNG", "PNG");
					put("DOC", "DOC");
					put("DOCX", "DOCX");
					put("XLS", "XLS");
					put("ODS", "ODS");
					put("XLSX", "XLSX");
					put("PDF", "PDF");
					put("CSV", "CSV");
					put("TXT", "TXT");
					put("XML", "XML");
					put("FTL", "FTL");
					put("HTML", "HTML");
					put("ODT", "ODT");
					put("TIF", "TIFF");
					put("MSG", "MSG");
					put("OFT", "OFT");
					put("HTM", "HTML");
					put("MHT", "MHTML");
					put("VM", "VM");
					put("OTF", "OTF");
					put("TTF", "TTF");
				}
			});

	private static Pattern[] patterns = {
			Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("src[\r\n]*=[\r\n]*\\'(.*?)\\'", Pattern.DOTALL),
			Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.DOTALL),
			Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<script(.*?)>", Pattern.DOTALL),
			Pattern.compile("(.*?)script(.*?)", Pattern.DOTALL),
			Pattern.compile("eval\\((.*?)\\)", Pattern.DOTALL),
			Pattern.compile("expression\\((.*?)\\)", Pattern.DOTALL),
			Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
			Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
			Pattern.compile("onload(.*?)=", Pattern.DOTALL),
			Pattern.compile("<", Pattern.DOTALL),
			Pattern.compile(">", Pattern.DOTALL),
			Pattern.compile("/>", Pattern.DOTALL),
			Pattern.compile("\\*", Pattern.DOTALL),
			Pattern.compile("\\=", Pattern.DOTALL),
			Pattern.compile("`", Pattern.DOTALL),
			Pattern.compile("\\%", Pattern.DOTALL),
			Pattern.compile("\\^", Pattern.DOTALL),
			Pattern.compile(";", Pattern.DOTALL),
			Pattern.compile("]", Pattern.DOTALL),
			Pattern.compile("}", Pattern.DOTALL),
			Pattern.compile("\\(", Pattern.DOTALL),
			Pattern.compile("\\)", Pattern.DOTALL),
			Pattern.compile("\\-", Pattern.DOTALL),
			Pattern.compile("\\+", Pattern.DOTALL),
			Pattern.compile("'", Pattern.DOTALL),
			Pattern.compile("\\[", Pattern.DOTALL),
			Pattern.compile("\\{", Pattern.DOTALL),
			Pattern.compile("!", Pattern.DOTALL),
			Pattern.compile("alert", Pattern.DOTALL),
			Pattern.compile("confirm", Pattern.DOTALL),
			Pattern.compile("prompt", Pattern.DOTALL) };

	private Matcher matcher;

	@Inject
	@Named("documentDataStoreDao")
	private DocumentDataStoreDao documentDataStoreDao;

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	@Inject
	@Named("tika")
	private Tika tika;

	@Override
	public void saveObject(Object objectToSave) {
		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public void removeObject(Object objectToRemove) {
		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public <T> T findObjectId(String id, Class<T> entityClass) {
		return documentDataStoreDao.findObjectId(id, entityClass);
	}

	@Override
	public <T> List<T> findObjectsByMultipleIds(Class<T> entityClass,
			List<String> documentIdList) {
		throw new UnsupportedOperationException("Operation not supported");
	}
	
	@Override
	public String saveDocument(InputStream stream, String attachmentName, String contentType, Map<String, String> metadata){
		//metadata will be ignored in oracle implementation of datastore
		return this.saveDocument(stream, attachmentName, contentType);
	}

	@Override
	public String saveDocument(InputStream stream, String attachmentName,
			String contentType) {

		String extensionType = supportedExtensionTypes.get(contentType
				.toUpperCase());
		if (checkNamePattern(attachmentName)) {
			throw new SystemException("File Name not supported");
		}

		if (StringUtils.isEmpty(extensionType)) {
			BaseLoggers.exceptionLogger.error("******content type is ***** "
					+ contentType);
			throw new SystemException("File Format not supported");
		}
		String mimeType;
		ByteArrayInputStream bais = getInputStreamFromBytes(getBytesFromInputStream(stream));
		try {
			mimeType = tika.detect(bais);
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
		if(blockUserUploadedMaliciouscontent){
			Map<String, FileSanitizer> sanitizers = NeutrinoSpringAppContextUtil
					.getBeansOfType(FileSanitizer.class);
	
			for (Map.Entry<String, FileSanitizer> sanitizerEntry : sanitizers
					.entrySet()) {
				bais.reset();
				FileSanitizer fileSanitizer = sanitizerEntry.getValue();
				if (fileSanitizer.canSanitize(mimeType,extensionType)) {
						fileSanitizer.checkSanity(bais);
					}
				}
		}
		bais.reset();
		return documentDataStoreDao.saveDocument(bais, attachmentName,
				contentType);

	}
	
	@Override
	public String saveDocumentBase64(String contentInBase64Encoding, String attachmentName, String contentType, Map<String, String> metadata){
		//metadata will be ignored in oracle implementation of datastore
		return saveDocumentBase64(contentInBase64Encoding, attachmentName, contentType);
	}
	

	@Override
	public String saveDocumentBase64(String contentInBase64Encoding,
			String attachmentName, String contentType) {
		String documentId = null;
		try {
			byte[] bytes = Base64.decode(contentInBase64Encoding);
			ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
			baos.write(bytes, 0, bytes.length);
			documentId = documentDataStoreDao.saveDocument(baos,
					attachmentName, contentType);
		} catch (IOException e) {
			BaseLoggers.exceptionLogger
					.error("Exception occured while storing document in database:"
							+ e.getMessage());
			throw new SystemException(
					"Execption occured while storing document in database:", e);
		}

		return documentId;
	}

	@Override
	public <T> List<T> getObjectIds(Class<T> entityClass) {
		return null;
	}

	@Override
	public File retriveDocument(String documentId) {

		OracleDocumentEntity oracleDocumentEntity = documentDataStoreDao
				.findObjectId(documentId, OracleDocumentEntity.class);
		if (oracleDocumentEntity == null) {
			BaseLoggers.exceptionLogger.error(
					"No document found in OracleDatastore for id {}",
					documentId);
			throw new SystemException(
					"Excption occured while retreving document in database with id:"
							+ documentId);
		}

		String PREFIX = documentId;

        if(!StringUtils.isEmpty(oracleDocumentEntity.getFilename())) {

               PREFIX = PREFIX + "_"+oracleDocumentEntity.getFilename();

        }
		String SUFFIX = "." + oracleDocumentEntity.getContentType();
		PREFIX = PREFIX.concat(FILE_NAME_MIDDLE_SEPARATOR);
		File file = null;
		FileOutputStream out = null;
		try {
			file = File.createTempFile(PREFIX, SUFFIX);
			out = new FileOutputStream(file);
			IOUtils.copy(oracleDocumentEntity.getContent().getBinaryStream(),
					out);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger
					.error("Excption occured while retreving document in database:"
							+ e.getMessage());
			throw new SystemException(
					"Excption occured while retreving document in database:", e);
		} finally {
            IOUtils.closeQuietly(out);
        }
		return file;
	}
	
	@Override
	public DataStoreDocument retrieveDocument(String documentId) {
		OracleDocumentEntity oracleDocumentEntity = documentDataStoreDao .findObjectId(documentId, OracleDocumentEntity.class);
		if (oracleDocumentEntity == null) {
			BaseLoggers.exceptionLogger.error( "No document found in OracleDatastore for id {}", documentId);
			throw new SystemException( "No document found in OracleDatastore for id:" + documentId);
		}
		try{
			return new DataStoreDocument(IOUtils.toByteArray(oracleDocumentEntity.getContent().getBinaryStream()), oracleDocumentEntity.getFilename(), oracleDocumentEntity.getContentType());
		}catch(Exception e){
			BaseLoggers.exceptionLogger.error( "Exception while converting Oracle Datastore document (id = {"+documentId+"}) into data object ", e);
			throw new SystemException( "Exception while converting Oracle Datastore document (id = {"+documentId+"}) into data object ", e);
		}
	}
	
	@Override
	public String saveDocument(DataStoreDocument document) {
		return saveDocument(new ByteArrayInputStream(document.getContent()), document.getFileName(), document.getContentType());
	}

	@Override
	public void upDateDocument(String documentId, InputStream stream,
			String AttachmentName, String contentType) {
		NeutrinoValidator.notNull(documentId,
				"Document id cannot be null while retrieving document.");
		NeutrinoValidator.notNull(stream,
				"InputStream cannot be null while Saving document.");
		NeutrinoValidator.notNull(AttachmentName,
				"AttachmentName cannot be null while Saving document.");
		NeutrinoValidator
				.notNull(contentType,
						"Content-Type of the attachment cannot be null while Saving Attachment.");

		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public void removeDocument(String documentId) {
		NeutrinoValidator.notNull(documentId,
				"Document id cannot be null while retrieving document.");
		documentDataStoreDao.removeDocument(documentId);
	}

	@Override
	public <T> void remove(String id, Class<T> entityClass) {
	}

	@Override
	public <T> T findObjectBySingleCriteria(String key, String value,
			Class<T> entityClass) {
		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public <T> void updateDataDocument(String keyToData, String newData,
			Class<T> entityClass) {
		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public void updateDataDocument(String keyToData, String newData,
			String collectionName) {
	}

	@Override
	public <T> List<T> findNearByArea(Point p, Distance d, Class<T> entityClass) {
		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public <T> List<T> findByPositionWithinCircle(Circle c, Class<T> entityClass) {
		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public <T> List<T> findByPositionWithinBox(Box b, Class<T> entityClass) {
		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public <T> void updateAddressLocation(String keyToData,
			double[] updatedLocation, Class<T> entityClass) {
		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public void updateAddressLocation(String keyToData,
			double[] updatedLocation, String collectionName) {
		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public void createAddress(Address address) {
		throw new UnsupportedOperationException("Operation not supported");
	}

	@Override
	public String saveDocumentWithDescription(InputStream stream,
			String contentType, String fileName, String bucket) {
		return null;
	}

	@Override
	public byte[] retriveDocumentAsByteArray(String documentId) {
		OracleDocumentEntity oracleDocumentEntity = documentDataStoreDao
				.findObjectId(documentId, OracleDocumentEntity.class);
		try {
			return IOUtils.toByteArray(oracleDocumentEntity.getContent()
					.getBinaryStream());
		} catch (Exception e) {
			BaseLoggers.exceptionLogger
					.error("Excption occured while retreving document in database:",
							e);
			throw new SystemException(
					"Excption occured while retreving document in database:", e);
		}
	}

	public void setDocumentDataStoreDao(
			DocumentDataStoreDao documentDataStoreDao) {
		this.documentDataStoreDao = documentDataStoreDao;
	}

	public void setConfigurationService(
			ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public void setTika(Tika tika) {
		this.tika = tika;
	}

	// TODO change to matches
	private boolean checkNamePattern(String fileName) {
		Boolean result = false;

		for (Pattern scriptPattern : patterns) {
			matcher = scriptPattern.matcher(fileName);
			result = matcher.lookingAt();
			if (result) {
				break;
			}

		}
		return result;

	}
	
	private byte[] getBytesFromInputStream(InputStream inputStream){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			IOUtils.copy(inputStream, baos);
		} catch (IOException e) {
			throw ExceptionBuilder.getInstance(ServiceInputException.class, "inputstream.could.not.be.read", "InputStream could not be read").build();
		}
		return baos.toByteArray();
	}
	
	private ByteArrayInputStream getInputStreamFromBytes(byte[] bytes){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);	
		return bais;
	}
	
	@Override
	public DocumentMetaData retrieveDocumentWithMetaData(String documentId){
		OracleDocumentEntity oracleDocumentEntity = documentDataStoreDao
				.findObjectId(documentId, OracleDocumentEntity.class);	
		String fileName = oracleDocumentEntity.getFilename();
		try{
		
			String mimeType = tika.detect(oracleDocumentEntity.getContent().getBinaryStream());
			return new DocumentMetaData(IOUtils.toByteArray(oracleDocumentEntity.getContent()
				.getBinaryStream()),oracleDocumentEntity.getContentType(),mimeType,fileName);
		}
		
		catch (Exception e) {
			BaseLoggers.exceptionLogger
					.error("Exception occured while preparing DocumentMetaData:",
							e);
			throw new SystemException(
					"Exception occured while preparing DocumentMetaData:", e);
		}
	}


}
