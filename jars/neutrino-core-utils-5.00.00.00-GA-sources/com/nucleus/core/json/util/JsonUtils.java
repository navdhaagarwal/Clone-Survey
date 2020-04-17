/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.json.util;

import java.io.StringWriter;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module.Feature;
/*import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module.Feature;*/
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER_NO_INIT    = new ObjectMapper();
    private static final ObjectMapper OBJECT_MAPPER_FULL_INIT  = new ObjectMapper();
    private static final String       DEFAULT_DATE_TIME_FORMAT = "MM/dd/yyyy hh:mm:ss a";

    static {
        OBJECT_MAPPER_NO_INIT.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER_NO_INIT.registerModule(new JodaModule());
        OBJECT_MAPPER_NO_INIT.registerModule(new DateTimeModule());
        /*Hibernate4Module hibernate4Module = new Hibernate4Module();
        hibernate4Module.disable(Feature.FORCE_LAZY_LOADING);*/
        Hibernate5Module hibernate5Module = new Hibernate5Module();
        hibernate5Module.disable(Feature.FORCE_LAZY_LOADING);
        
        OBJECT_MAPPER_NO_INIT.registerModule(hibernate5Module);

        OBJECT_MAPPER_FULL_INIT.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER_FULL_INIT.registerModule(new JodaModule());
        OBJECT_MAPPER_FULL_INIT.registerModule(new DateTimeModule());
        /*Hibernate4Module hibernate4Module2 = new Hibernate4Module();
        hibernate4Module2.enable(Feature.FORCE_LAZY_LOADING);
        OBJECT_MAPPER_FULL_INIT.registerModule(hibernate4Module);*/
        Hibernate5Module hibernate5Module2 = new Hibernate5Module();
        hibernate5Module2.enable(Feature.FORCE_LAZY_LOADING);
        OBJECT_MAPPER_FULL_INIT.registerModule(hibernate5Module2);
    }

    public static String serializeWithLazyInitialization(Object object) {

        StringWriter stringWriter = new StringWriter();
        try {
        	 ObjectWriter objectWriter = OBJECT_MAPPER_FULL_INIT.writer(new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT));
             objectWriter.writeValue(stringWriter, object);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in serializing object of class:" + object.getClass().getName(), e);
            throw new SystemException("Error in serializing object of class" + object.getClass(), e);
        }
        return stringWriter.toString();
    }

    public static String serializeWithoutLazyInitialization(Object object) {

        StringWriter stringWriter = new StringWriter();
        try {
             ObjectWriter objectWriter = OBJECT_MAPPER_NO_INIT.writer(new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT));
             objectWriter.writeValue(stringWriter, object);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in serializing object of class:" + object.getClass().getName(), e);
            throw new SystemException("Error in serializing object of class:" + object.getClass().getName(), e);
        }
        return stringWriter.toString();
    }

    public static String serializeWithoutLazyInitialization(Object object, String dateFormat) {

        StringWriter stringWriter = new StringWriter();
        try {
             ObjectWriter objectWriter = OBJECT_MAPPER_NO_INIT.writer(new SimpleDateFormat(StringUtils.isEmpty(dateFormat) ? DEFAULT_DATE_TIME_FORMAT
                    : dateFormat));
             objectWriter.writeValue(stringWriter, object);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in serializing object of class:" + object.getClass().getName(), e);
            throw new SystemException("Error in serializing object of class:" + object.getClass().getName(), e);
        }
        return stringWriter.toString();
    }

    public static <T> T deserialize(String jsonString, Class<T> targetClass) {
        T finalObject = null;
        try {
            finalObject = OBJECT_MAPPER_NO_INIT.readValue(jsonString.getBytes(), targetClass);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in dserializing object of class:" + targetClass.getName(), e);
            throw new SystemException("Error in deserializing object of class" + targetClass, e);
        }
        return finalObject;
    }
}
