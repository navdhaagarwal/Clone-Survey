package com.nucleus.web.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import com.nucleus.html.util.HtmlUtils;

import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.web.common.controller.CASValidationUtils;

@Named("renderImageUtility")
public class RenderImageUtility {

	@Inject
	@Named("couchDataStoreDocumentService")
	private DatastorageService docService2;

	@Inject
	@Named("tika")
	private Tika tika;

	public void renderImage(String imageId, HttpServletResponse response) throws IOException {
		java.io.InputStream inStream = null;
		try {
			if (CASValidationUtils.isAlphaNumeric(imageId)) {
				File imageFile = docService2.retriveDocument(imageId);
				if (imageFile != null) {
					inStream = new java.io.FileInputStream(imageFile);
					byte[] fileContents = IOUtils.toByteArray(inStream);
					InputStream originalInputStream = new ByteArrayInputStream(fileContents);
					String fileType = tika.detect(originalInputStream);
					// to prevent caching of image at proxy server
					response.setHeader("Expires", "Mon, 01 Jan 1900 16:00:00 GMT");
					response.setContentType(fileType);
					if (fileType.contains("text")) {
						String escapedFileContent = HtmlUtils.htmlEscape(new String(fileContents, "UTF-8"));
						IOUtils.copy(new ByteArrayInputStream(escapedFileContent.getBytes()),
								response.getOutputStream());
					} else {
						IOUtils.copy(new ByteArrayInputStream(fileContents), response.getOutputStream());
					}

				}
			}
		} finally {
			if (inStream != null) {
				inStream.close();
			}
		}
	}

}
