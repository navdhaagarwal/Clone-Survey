package com.nucleus.jackson.custom;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
/**
 * 
 * @author gajendra.jatav
 *
 * Json representation for Map.Entry changed in Jackson 2.5.5 so while upgrading from 2.2.2 to 2.7.7 we are adding custom serializer
 * Refer PDDEV-6663
 *  
 *
 */
public class CustomMapEntrySerializer extends JsonSerializer<Map.Entry>{

	

	@Override
	public void serialize(Map.Entry value, JsonGenerator gen,
			SerializerProvider serializers) throws IOException,
			JsonProcessingException {
		
		
		if(null==value)
			return;
		gen.writeStartObject();
		Object key=value.getKey();
		Object val=value.getValue();
		if(key==null)
		{
			key=StringUtils.EMPTY;
			
		}
		if(val==null)
		{
			val=StringUtils.EMPTY;
		}
		if(BeanUtils.isSimpleProperty(key.getClass()))
		{
			gen.writeStringField("key", key.toString());
		}
		else
		{
			gen.writeObjectField("key", key);
		}
		if(BeanUtils.isSimpleProperty(val.getClass()))
		{
			gen.writeStringField("value", val.toString());
		}
		else
		{
			gen.writeObjectField("value", value);
		}
        gen.writeEndObject();
		
	}
	@Override
	public Class<Map.Entry> handledType() {
		
		return  Map.Entry.class;
	}

}
