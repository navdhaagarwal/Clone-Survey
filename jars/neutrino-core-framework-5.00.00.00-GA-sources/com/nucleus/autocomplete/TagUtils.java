package com.nucleus.autocomplete;

import java.util.Collection;

import com.nucleus.logging.BaseLoggers;

import flexjson.JSONSerializer;

public class TagUtils {

	public static String convertListToJsonString(Collection<Object> objectList, String... includedAttributes) {
		String selectedItems = "";
		try {
			selectedItems = new JSONSerializer().include(includedAttributes).exclude("*")
					.serialize(objectList);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in JSON Serialization" + e);
		}
		return selectedItems;
	}
}
