package com.nucleus.master.audit.service.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;

import flexjson.JSONDeserializer;

public interface MasterChangeFieldDiffCalculator {

	public void calculateDiff(Object newValue, Object oldValue, StringBuilder message, Object oldEntity,
			Object newEntity,
			Stack<MasterChangeTuple2<MasterChangeGetterMethodMeta, MasterChangeSetterMethodMeta>> getSetStack)
					throws Exception;

	public default Diff compareJSONwithoutNesting(String oldJSON, String newJSON) {
		JSONDeserializer deserializer = new JSONDeserializer<>();
		Map<String, Object> oldValueMap = new HashMap<String, Object>();
		Map<String, Object> newValueMap = new HashMap<String, Object>();

		// convert JSON string to Map

		oldValueMap = (Map<String, Object>) deserializer.deserialize(oldJSON);
		newValueMap = (Map<String, Object>) deserializer.deserialize(newJSON);
		Javers javer = JaversBuilder.javers().build();
		return javer.compare(oldValueMap, newValueMap);

	}

}
