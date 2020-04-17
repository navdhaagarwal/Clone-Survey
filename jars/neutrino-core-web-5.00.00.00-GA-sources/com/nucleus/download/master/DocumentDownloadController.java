package com.nucleus.download.master;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.datastore.service.DocumentMetaData;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.communicationgenerator.service.CommunicationTemplateService;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.web.common.controller.BaseController;

@Controller
public class DocumentDownloadController extends BaseController {

	private static final String IO_ERROR_WHILE_DOWNLOADING_DOCUMENT = "IO error while downloading document.";

	@Inject
	@Named("couchDataStoreDocumentService")
	private DatastorageService docService2;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Inject
	@Named("communicationTemplateService")
	private CommunicationTemplateService communicationTemplateService;

	@RequestMapping(value = "/documentDownload")
	@ResponseBody
	public HttpEntity<byte[]> getDocumentFromId(ModelMap map,
			@RequestParam(value = "documentId", required = false) String documentId,
			@RequestParam(value = "fileName", required = false) String fileName,
			@RequestParam(value = "fileLocation", required = false) String location) {
		String documentName = null;
		String contentType = null;
		byte[] fileContent = null;
		HttpEntity<byte[]> fileEntity = null;
		if (StringUtils.isNotEmpty(documentId)) {
			DocumentMetaData docMetaData = docService2.retrieveDocumentWithMetaData(documentId);
			if (docMetaData != null) {
				documentName = (StringUtils.isNoneEmpty(docMetaData.getFileName())) ? docMetaData.getFileName()
						: "tmpdocumnent01";
				contentType = docMetaData.getMimeType();
				fileContent = docMetaData.getContent();
			}
		} else if (StringUtils.isNotEmpty(fileName) && StringUtils.isNotEmpty(location)) {
			File file = fetchFileFromLocation(fileName, location);
			if (file.exists()) {
				try {
					contentType = communicationTemplateService.findContentType(file);
					fileContent = FileUtils.readFileToByteArray(file);
				} catch (IOException e) {
					BaseLoggers.exceptionLogger.error(e.getMessage(), e);
					throw ExceptionBuilder
							.getInstance(BusinessException.class, IO_ERROR_WHILE_DOWNLOADING_DOCUMENT,
									IO_ERROR_WHILE_DOWNLOADING_DOCUMENT)
							.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
				}
				documentName = fileName;
			}
		}
		if (fileContent != null) {
			MediaType mediaType = (StringUtils.isNoneEmpty(contentType)) ? MediaType.parseMediaType(contentType)
					: MediaType.APPLICATION_OCTET_STREAM;
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentDispositionFormData("attachment", documentName);
			responseHeaders.setContentType(mediaType);
			fileEntity = new HttpEntity<byte[]>(fileContent, responseHeaders);
		}
		return fileEntity;
	}

	@RequestMapping(value = "/deleteFile", method = RequestMethod.POST)
	public void cleanCommunicationPreviewPath(ModelMap map, @RequestParam(value = "fileNames") String[] fileNames,
			@RequestParam(value = "fileLocation") String location) {
		if (fileNames != null) {
			for (String fileName : fileNames) {
				if (StringUtils.isNotEmpty(fileName) && StringUtils.isNotEmpty(location)) {
					deleteFile(location, fileName);
				}
			}
		}

	}

	private void deleteFile(String location, String fileName) {
		String filePath = StringUtils.appendIfMissing(location, "/", "\\") + fileName;
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
	}

	@RequestMapping(value = "/viewAttachedDocument")
	public void getFile(ModelMap map, @RequestParam(value = "documentId", required = false) String documentId,
			@RequestParam(value = "fileName", required = false) String fileName,
			@RequestParam(value = "fileLocation", required = false) String location, HttpServletResponse response)
			throws IOException {
		String contentType = null;
		byte[] fileContent = null;
		if (StringUtils.isNotEmpty(documentId)) {
			DocumentMetaData docMetaData = docService2.retrieveDocumentWithMetaData(documentId);
			if (docMetaData != null) {
				contentType = docMetaData.getMimeType();
				fileContent = docMetaData.getContent();
			}
		} else if (StringUtils.isNotEmpty(fileName) && StringUtils.isNotEmpty(location)) {
			File file = fetchFileFromLocation(fileName, location);
			if (file.exists()) {
				try {
					contentType = communicationTemplateService.findContentType(file);
					fileContent = FileUtils.readFileToByteArray(file);
				} catch (IOException e) {
					BaseLoggers.exceptionLogger.error(e.getMessage(), e);
					throw ExceptionBuilder
							.getInstance(BusinessException.class, IO_ERROR_WHILE_DOWNLOADING_DOCUMENT,
									IO_ERROR_WHILE_DOWNLOADING_DOCUMENT)
							.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
				}
			}
		}
		if (fileContent != null) {
	        InputStream targetStream = new ByteArrayInputStream(fileContent);
	        BufferedInputStream bufferedInputStream = new BufferedInputStream(targetStream);
	        ServletOutputStream bufferedOutputStream = response.getOutputStream();
	        bufferedInputStream.read(fileContent);
	        
	        response.setContentType(contentType.toString());
	        response.setContentLength(fileContent.length);
	        response.setHeader("Content-Disposition", "inline; filename=" + fileName);
	        response.setHeader("Cache-Control", "cache, must-revalidate");
	        response.setHeader("Pragma", "public");
	        bufferedOutputStream.write(fileContent);
		}
		
	}

	private File fetchFileFromLocation(String fileName, String location) {
		String filePath = StringUtils.appendIfMissing(location, "/", "\\") + fileName;
		File file = new File(filePath);
		return file;
	}
}
