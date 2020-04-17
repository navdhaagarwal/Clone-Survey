package com.nucleus.finnone.pro.general.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapToList {
	public static List<Entry<String, String>> convertToList(
			Map<String, String> map) {
		if (!map.isEmpty() && map.size() > 0) {
			Set<Entry<String, String>> set = map.entrySet();
			List<Entry<String, String>> list = new ArrayList<Entry<String, String>>(
					set);
			Collections.sort(list, new Comparator<Entry>() {
				@Override
				public int compare(Entry s1, Entry s2) {
					return s1.getValue().toString()
							.compareToIgnoreCase(s2.getValue().toString());
				}
			});

			return list;
		}
		return null;

	}
}
