/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.hibernate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module.Feature;
/*import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module.Feature;*/
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.money.entity.Money;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.formatter.MoneyFormatter;

/**
 * @author Nucleus Software Exports Limited
 *
 * This class should be used to serialize entities to JSON, it specifically handles the entities with instances of type Money.class
 */
public class JsonUtilsWithMoneyHandle {

    private static final ObjectMapper OBJECT_MAPPER_FULL_INIT_MONEY = new ObjectMapper();

    static {

        Hibernate5Module hibernate5Module = new Hibernate5Module();
        hibernate5Module.enable(Feature.FORCE_LAZY_LOADING);
        
        OBJECT_MAPPER_FULL_INIT_MONEY.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER_FULL_INIT_MONEY.registerModule(new JodaModule());
        hibernate5Module.enable(Feature.FORCE_LAZY_LOADING);
        OBJECT_MAPPER_FULL_INIT_MONEY.registerModule(hibernate5Module);
    }
    
    public static String serializeWithLazyInitializationAndHandleMoney(Object object, Locale locale) {

        StringWriter stringWriter = new StringWriter();
        try {
            OBJECT_MAPPER_FULL_INIT_MONEY.registerModule(new SimpleModule().addSerializer(Money.class, new MoneySerializer(locale)));
            OBJECT_MAPPER_FULL_INIT_MONEY.writeValue(stringWriter, object);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in serializing object of class:" + object.getClass().getName(), e);
            throw new SystemException("Error in serializing object of class" + object.getClass(), e);
        }
        return stringWriter.toString();
    }

    public static <T> T deserialize(String jsonString, Class<T> targetClass) {
        T finalObject = null;
        try {
            finalObject = OBJECT_MAPPER_FULL_INIT_MONEY.readValue(jsonString.getBytes(), targetClass);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in dserializing object of class:" + targetClass.getName(), e);
            throw new SystemException("Error in deserializing object of class" + targetClass, e);
        }
        return finalObject;
    }
}

class MoneySerializer extends JsonSerializer<Money> {
    
    Locale locale;
    
    public MoneySerializer(Locale locale) {
        super();
        this.locale = locale;
    }

    @Override
    public void serialize(Money value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
            JsonProcessingException {
        MoneyFormatter moneyFormatter = NeutrinoSpringAppContextUtil.getBeanByName("moneyFormatter", MoneyFormatter.class);
        jgen.writeString(moneyFormatter.print(value, locale));
    }
}
