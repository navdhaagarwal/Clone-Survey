/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.initialization;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;

/**
 * Core class to initialize product information as contained in product-info.properties file on classpath.
 * @author Nucleus Software Exports Limited
 */
public class ProductInformationLoader {

    public static final String                 PRODUCT_INFO_FILE_NAME            = "product-info.properties";
    private static final Resource              PRODUCT_INFO_RESOURCE             = new ClassPathResource(
                                                                                         PRODUCT_INFO_FILE_NAME);

    private static Boolean                     PRODUCT_INFO_RESOURCE_FILE_EXISTS = null;

    protected static final Map<String, String> PRODUCT_INFO                      = new LinkedHashMap<String, String>();

    private static final String                PRODUCT_NAME                      = "product.name";
    private static final String                PRODUCT_CODE                    = "product.code";

    private static final List<String>          ALL_PROPERTIES                    = Arrays.asList(new String[] { PRODUCT_NAME , PRODUCT_CODE });

    static {
        if (productInfoExists()) {
            try {
                Properties properties = new Properties();
                properties.load(PRODUCT_INFO_RESOURCE.getInputStream());
                for (Entry<Object, Object> entry : properties.entrySet()) {
                    String value = (String) entry.getValue();
                    if (StringUtils.isBlank(value)) {
                        throw new SystemException("Empty Property found in product-info.properties: " + value);
                    }
                    PRODUCT_INFO.put((String) entry.getKey(), value);
                }
                if (!PRODUCT_INFO.keySet().containsAll(ALL_PROPERTIES)) {
                    throw new SystemException(String.format("'%s' doesn't contain all required properties : "
                            + ALL_PROPERTIES, PRODUCT_INFO_FILE_NAME));
                }
                logProductInfo();
            } catch (Exception e) {
                throw new SystemException("Exception while reading product-info.properties file."+e);
            }
        } else {
            BaseLoggers.productInfoLogger.warn(String.format("'%s' file not found on class path", PRODUCT_INFO_FILE_NAME));
        }
    }

    protected static void logProductInfo() {
        StringBuilder sb = new StringBuilder();
        String straightLine = RandomStringUtils.random(100, "-");
        String newLine = System.getProperty("line.separator");
        sb.append(newLine).append(straightLine).append(newLine);
        sb.append(String.format("Following are the properties which are specified in '%s' file :: ", PRODUCT_INFO_FILE_NAME))
                .append(newLine);
        sb.append(straightLine).append(newLine);
        for (Entry<String, String> entry : PRODUCT_INFO.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(newLine);
        }
        sb.append(straightLine);
        BaseLoggers.productInfoLogger.info(sb.toString());
    }

    public static String getProductCode() {
        if (PRODUCT_INFO.isEmpty()) {
            return "";
        }
        return PRODUCT_INFO.get(PRODUCT_CODE);
    }
    public static String getProductName() {
        if (PRODUCT_INFO.isEmpty()) {
            return "";
        }
        return PRODUCT_INFO.get(PRODUCT_NAME);
    }
    public static String getProductInfoProperty(String key) {
        if (PRODUCT_INFO.isEmpty()) {
            return null;
        }
        return PRODUCT_INFO.get(key);
    }

	public static String getProductVersion() {
		Class cls;
		try {
			if ("FW".equals(getProductCode())) {
				cls = Class.forName("com.nucleus.fw.version.VersionInfo");

			} else {
				cls = Class.forName("com.nucleus.version.VersionInfo");

			}
			Method versionMethod = cls.getDeclaredMethod("getVersion");
			return (String) versionMethod.invoke(null);

		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {

			BaseLoggers.exceptionLogger.error(e.getClass() + " occured while getting product version information",e);

		}
		return null;
	}
  
    public static boolean productInfoExists() {
        // Leveraging the static boolean variable so that we do not repeatedly check for file existence and cache the result.
        if (PRODUCT_INFO_RESOURCE_FILE_EXISTS == null) {
            PRODUCT_INFO_RESOURCE_FILE_EXISTS = PRODUCT_INFO_RESOURCE.exists();
        }
        return PRODUCT_INFO_RESOURCE_FILE_EXISTS;
    }
}
