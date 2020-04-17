package com.nucleus.core.security.filesanitizer;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.nucleus.core.datastore.util.FileSanitizer;
import com.nucleus.logging.BaseLoggers;

@Named("xmlSanitizer")
public class XMLSanitizer implements FileSanitizer {

	private static final Set<String> sanitizableMimeTypes = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("application/xml");
			add("text/xml");
		}
	});

	@Override
	public boolean canSanitize(String mimeType, String extensionType) {
		if (sanitizableMimeTypes.contains(mimeType)) {
			return true;
		}
		return false;
	}

	@Override
	public void checkSanity(InputStream stream) {

		// TODO Auto-generated method stub
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		String FEATURE = null;
		try {
			// Prevent DTDs (doctypes) are disallowed
			FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
			dbf.setFeature(FEATURE, true);

			// Disallow external-general-entities
			FEATURE = "http://xml.org/sax/features/external-general-entities";
			dbf.setFeature(FEATURE, false);

			// Disallow external-parameter-entities
			FEATURE = "http://xml.org/sax/features/external-parameter-entities";
			dbf.setFeature(FEATURE, false);

			// Disable external DTDs as well
			FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
			dbf.setFeature(FEATURE, false);

			// DTD, and Entity Attacks"
			dbf.setXIncludeAware(false);
			dbf.setExpandEntityReferences(false);


			DocumentBuilder safebuilder = dbf.newDocumentBuilder();
			safebuilder.parse(stream);

		} catch (Exception e) {
			BaseLoggers.exceptionLogger.info(
					"ParserConfigurationException was thrown. The feature {}  is probably not supported by your XML processor., {}",
					FEATURE,e);
			throw new XxeException("ParserConfigurationException was thrown. The feature '" + FEATURE
					+ "' is probably not supported by your XML processor.",e);
		}

	}

}
