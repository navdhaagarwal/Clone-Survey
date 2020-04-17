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
package com.nucleus.core.xml.util;

import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import com.nucleus.core.exceptions.InvalidDataException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentCollectionConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedSetConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.nucleus.logging.BaseLoggers;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class XmlUtils {

	private static final TransformerFactory TANSFORMER_FACTORY = TransformerFactory
			.newInstance();

	private static final XStream xstream = new XStream();

	private static final XStream xStreamWithDateTimeToISO = new XStream() {
		protected MapperWrapper wrapMapper(final MapperWrapper next) {
			return new HibernateMapper(next);
		}
	};

	static {
		xStreamWithDateTimeToISO
				.registerConverter(new HibernateProxyConverter());
		xStreamWithDateTimeToISO
				.registerConverter(new HibernatePersistentCollectionConverter(
						xStreamWithDateTimeToISO.getMapper()));
		xStreamWithDateTimeToISO
				.registerConverter(new HibernatePersistentMapConverter(
						xStreamWithDateTimeToISO.getMapper()));
		xStreamWithDateTimeToISO
				.registerConverter(new HibernatePersistentSortedMapConverter(
						xStreamWithDateTimeToISO.getMapper()));
		xStreamWithDateTimeToISO
				.registerConverter(new HibernatePersistentSortedSetConverter(
						xStreamWithDateTimeToISO.getMapper()));
		xStreamWithDateTimeToISO.registerConverter(new Converter() {
			DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

			@Override
			public boolean canConvert(Class type) {
				return (DateTime.class.isAssignableFrom(type));
			}

			@Override
			public Object unmarshal(HierarchicalStreamReader reader,
					UnmarshallingContext context) {
				String value = reader.getValue();
				return StringUtils.isBlank(value) ? null : formatter
						.parseDateTime(value);
			}

			@Override
			public void marshal(Object source, HierarchicalStreamWriter writer,
					MarshallingContext context) {

				DateTime dateTime = (DateTime) source;
				// formats and sets the value
				writer.setValue(formatter.print(dateTime));
			}
		});


	}

	public static <T> T readFromXml(String xmlContent,
			Class<T> targetObjectClass) {
		return targetObjectClass.cast(xstream.fromXML(xmlContent));
	}

	public static String writeToXml(Object objectToWrite) {
		return xstream.toXML(objectToWrite);
	}

	public static String writeToXml(Document document)
			throws InvalidDataException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			TANSFORMER_FACTORY.newTransformer().transform(
					new DOMSource(document), new StreamResult(stream));
		} catch (Exception e) {
			throw new InvalidDataException(
					"Exception occured while converting DOM into XML", e);
		}
		return stream.toString();
	}

	public static Document readFromXml(String input)
			throws InvalidDataException {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(IOUtils.toInputStream(input));
		} catch (Exception e) {
			throw new InvalidDataException(
					"Exception occured while converting input String into document",
					e);
		}
	}

	public static String initializeAndWriteToXml(Object objectToWrite) {
		final XStream xstream = new XStream() {
			@Override
			protected MapperWrapper wrapMapper(final MapperWrapper next) {
				return new HibernateMapper(next);
			}
		};
		xstream.registerConverter(new HibernateProxyConverter());
		xstream.registerConverter(new HibernatePersistentCollectionConverter(
				xstream.getMapper()));
		xstream.registerConverter(new HibernatePersistentMapConverter(xstream
				.getMapper()));
		xstream.registerConverter(new HibernatePersistentSortedMapConverter(
				xstream.getMapper()));
		xstream.registerConverter(new HibernatePersistentSortedSetConverter(
				xstream.getMapper()));

		return xstream.toXML(objectToWrite);
	}
	
	public static String initializeAndWriteToXmlWithDateTimeToISO(Object objectToWrite) {
        String string = xStreamWithDateTimeToISO.toXML(objectToWrite);
        BaseLoggers.flowLogger.debug(string);
        return string;
    }

    public static <T> T readFromXmlWithISOToDateTime(String xmlContent, Class<T> targetObjectClass) {
        return targetObjectClass.cast(xStreamWithDateTimeToISO.fromXML(xmlContent));
    }

}
