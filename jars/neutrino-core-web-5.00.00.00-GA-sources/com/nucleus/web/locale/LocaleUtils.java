package com.nucleus.web.locale;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class LocaleUtils {
	public static boolean isValidLocale(String uLocale) {
        if ( StringUtils.isBlank(uLocale)) {
        	return false;
        }
        
		Locale[] locales = Locale.getAvailableLocales();

		for (Locale locale : locales) {
			if (uLocale.equalsIgnoreCase(locale.toString()) && ( StringUtils.isNotBlank(locale.getCountry()) ) ) {
				return true;
		    }
		}
		return false;
	}
}
