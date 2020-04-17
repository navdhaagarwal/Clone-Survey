package com.nucleus.web.security.filesanity;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.tika.Tika;

import com.nucleus.core.security.filesanitizer.XMLSanitizer;

/**
 * 
 * @author gajendra.jatav
 *
 */
@Named("uploadSanitizer")
public class UploadSanitizerImpl implements UploadSanitizer{

	@Named("xmlSanitizer")
	@Inject
	private  XMLSanitizer xmlSanitizer;
	
	@Inject
	@Named("tika")
	private Tika tika;
	
	@Override
	public void sanitize(InputStream inputStream) throws IOException {
		if(xmlSanitizer.canSanitize(tika.detect(inputStream),"")){
			xmlSanitizer.checkSanity(inputStream);
		}
	}

}
