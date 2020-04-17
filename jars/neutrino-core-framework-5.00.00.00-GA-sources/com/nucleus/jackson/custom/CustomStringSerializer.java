package com.nucleus.jackson.custom;


import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.std.NonTypedScalarSerializerBase;

/**
 * 
 * @author gajendra.jatav
 * 
 * refer PDDEV-6663
 * 
 *
 */
public final class CustomStringSerializer
// NOTE: generic parameter changed from String to Object in 2.6, to avoid
//   use of bridge methods
    extends NonTypedScalarSerializerBase<Object>
{
    private static final long serialVersionUID = 1L;

    public CustomStringSerializer() { super(String.class, false); }

    /**
     * For Strings, both null and Empty String qualify for emptiness.
     */
    @Override
    @Deprecated
    public boolean isEmpty(Object value) {
        String str = (String) value;
        return (str == null) || (str.length() == 0);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Object value) {
        String str = (String) value;
        return (str == null) || (str.length() == 0);
    }

    /**
     * 
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    	
    	if(value==null){
    		gen.writeString((String) value);
    		return;
    	}
    	Class valueClass=value.getClass();
    	if(CharSequence.class.isAssignableFrom(valueClass))
    	{
    		gen.writeString((String) value);
    		return;
    	}
    	if(Long.class.isAssignableFrom(valueClass))
    	{
    		gen.writeNumber((Long)value);
    		return;
    	}
    	if(Double.class.isAssignableFrom(valueClass))
    	{
    		gen.writeNumber((Double)value);
    		return;
    	}
    	if(Short.class.isAssignableFrom(valueClass))
    	{
    		gen.writeNumber((Short)value);
    		return;
    	}
    	if(Integer.class.isAssignableFrom(valueClass))
    	{
    		gen.writeNumber((Integer)value);
    		return;
    	}
    	if(Float.class.isAssignableFrom(valueClass))
    	{
    		gen.writeNumber((Float)value);
    		return;
    	}
    	if(BigDecimal.class.isAssignableFrom(valueClass))
    	{
    		gen.writeNumber((BigDecimal)value);
    		return;
    	}
    	if(BigInteger.class.isAssignableFrom(valueClass))
    	{
    		gen.writeNumber((BigInteger)value);
    		return;
    	}
    	gen.writeString((String) value);
    	
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return createSchemaNode("string", true);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        visitStringFormat(visitor, typeHint);
    }
}
