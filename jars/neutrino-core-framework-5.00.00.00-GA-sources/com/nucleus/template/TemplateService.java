package com.nucleus.template;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * The Interface TemplateService.
 */
public interface TemplateService {

    /**
     * Gets the resolved string from event template.
     * 
     * @param messageResourceKey
     *            the messageResourceKey
     * @param locale
     *            the locale
     * @param map
     *            the map
     * @return the resolved string from event template
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getResolvedStringFromResourceBundle(String messageResourceKey, Locale locale, Map<String, String> map)
            throws IOException;

    /**
     * Gets the resolved string from template.
     * 
     * @param templateKey
     *            the template key
     * @param map
     *            the map
     * @return the resolved string from template
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getResolvedStringFromTemplate(String templateKey, String templateValue, Map<String, String> map)
            throws IOException;

    public String getResolvedStringFromFTL(String templateName, Map<String, Object> map) throws IOException;

	public String getStringFromTemplateString(String templateKey, String templateValue, Map<String, Object> map) throws IOException;
}
