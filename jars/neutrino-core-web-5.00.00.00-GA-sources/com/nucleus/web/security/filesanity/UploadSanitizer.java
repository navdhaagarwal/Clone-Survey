package com.nucleus.web.security.filesanity;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author gajendra.jatav
 *
 */
public interface UploadSanitizer {
	
	public void sanitize(InputStream inputStream) throws IOException;
	
}
