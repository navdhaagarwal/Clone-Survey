package com.nucleus.finnone.pro.lov;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.nucleus.core.json.util.DateTimeModule;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.jackson.custom.CustomMapEntrySerializer;
import com.nucleus.logging.BaseLoggers;

public class LOVJsonUtil {

	private static final Map<String, ObjectMapper> DATE_FORMAT_WISE_OBJECT_MAPPER = new ConcurrentHashMap<>();

	public static final String NO_FORMAT = "NO_FORMAT";

	public static <T, K> K parseJsonToObject(String jsonString, Class<K> returnType, Class<T> objectType,
			String dateFormat) {
		ObjectMapper mapper = getObjectMapper(dateFormat);
		try {
			JavaType type = mapper.getTypeFactory().constructParametricType(returnType, objectType);
			return mapper.readValue(jsonString, type);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Exception: " + e.getMessage(), e);
		}
		return null;
	}

	private static ObjectMapper getObjectMapper(String dateFormat) {
		ObjectMapper objectMapper = DATE_FORMAT_WISE_OBJECT_MAPPER.get(dateFormat);
		if (ValidatorUtils.isNull(objectMapper)) {
			objectMapper = new ObjectMapper();
			objectMapper.setAnnotationIntrospector(new IgnoranceIntrospector());
			if (!NO_FORMAT.equals(dateFormat))
				objectMapper.setDateFormat(new SimpleDateFormat(dateFormat));
			objectMapper.registerModule(new AfterburnerModule());
			SimpleModule simpleModule = new SimpleModule();
			simpleModule.addSerializer(new CustomMapEntrySerializer());
			objectMapper.registerModule(simpleModule);
			objectMapper.registerModule(new JodaModule());
			objectMapper.registerModule(new DateTimeModule());
			DATE_FORMAT_WISE_OBJECT_MAPPER.put(dateFormat, objectMapper);
		}
		return objectMapper;
	}
}

@SuppressWarnings("serial")
class IgnoranceIntrospector extends JacksonAnnotationIntrospector {
	@Override
	public boolean hasIgnoreMarker(AnnotatedMember m) {
		return super.hasIgnoreMarker(m) || m.hasAnnotation(JsonDeserialize.class)
		&& (m.getAnnotation(JsonDeserialize.class).using().getSimpleName().equals("LMSJsonDateDeSerializer")
		|| m.getAnnotation(JsonDeserialize.class).contentUsing().getSimpleName().equals("LMSJsonDateDeSerializer"));
	}
}
