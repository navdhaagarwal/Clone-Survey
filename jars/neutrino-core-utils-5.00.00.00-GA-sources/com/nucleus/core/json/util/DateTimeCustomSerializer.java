package com.nucleus.core.json.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.joda.cfg.FormatConfig;
import com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat;
import com.fasterxml.jackson.datatype.joda.ser.JodaDateSerializerBase;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateTimeCustomSerializer extends JodaDateSerializerBase<DateTime> {

	private static final long serialVersionUID = 2L;

	public DateTimeCustomSerializer() {
		this(FormatConfig.DEFAULT_DATETIME_PRINTER);
	}

	public DateTimeCustomSerializer(JacksonJodaDateFormat format) {
		// false -> no arrays (numbers)
		super(DateTime.class, format,
				SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,2,0);
	}

	public DateTimeCustomSerializer(JacksonJodaDateFormat format,int shapeOverride) {
		super(DateTime.class, format,
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, FORMAT_TIMESTAMP,
                shapeOverride);
	}

	

	@Override
	public void serialize(DateTime dateTime, JsonGenerator jsonGenerator,
			SerializerProvider provider) throws IOException,
			JsonGenerationException {

		if (_serializationShape(provider)==JodaDateSerializerBase.FORMAT_TIMESTAMP) {
			jsonGenerator.writeNumber(dateTime.getMillis());
		} else {
			DateFormat dateFormat = provider.getConfig().getDateFormat();
			SimpleDateFormat simpleDateFormat;
			org.joda.time.format.DateTimeFormatter formatter = null;
			if (dateFormat != null
					&& SimpleDateFormat.class.isAssignableFrom(dateFormat
							.getClass())) {
				simpleDateFormat = (SimpleDateFormat) provider.getConfig()
						.getDateFormat();
				formatter = DateTimeFormat
						.forPattern(simpleDateFormat.toPattern())
						.withLocale(Locale.ROOT)
						.withChronology(dateTime.getChronology());
			} else {
				formatter = DateTimeFormat.fullDateTime();
			}

			jsonGenerator.writeString(dateTime.toString(formatter));
		}
	}

	@Override
	public JodaDateSerializerBase<DateTime> withFormat(JacksonJodaDateFormat format, int shapeOverride) {
		return (_format == format && _shapeOverride==shapeOverride) ?this :new DateTimeCustomSerializer(format,shapeOverride);
	}
}