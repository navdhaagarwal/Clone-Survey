package com.nucleus.core.datastore.util;

import java.io.InputStream;

public interface FileSanitizer 
{	
	boolean canSanitize(String mimeType,String extensionType);
	void checkSanity(InputStream stream) ;
}


