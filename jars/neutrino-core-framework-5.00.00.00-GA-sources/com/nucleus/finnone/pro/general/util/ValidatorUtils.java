package com.nucleus.finnone.pro.general.util;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import java.util.Collection;
import java.util.Map;

public class ValidatorUtils {

	private static boolean noNullElement(Object... targets) {
		boolean result = true;
		for (Object target : targets) {
			if (!notNull(target)) {
				result = false;
				break;
			}
		}
		return result;
	}

	private static boolean allNullElement(Object... targets) {
		boolean result = true;
		for (Object target : targets) {
			if (notNull(target)) {
				result = false;
				break;
			}
		}
		return result;
	}

	public static boolean noNullElements(Object... targets) {
		return notNull(targets) ? noNullElement(targets) : false;
	}

	public static boolean notNull(Object target) {
		return target != null ? true : false;
	}

	public static boolean isNull(Object target) {
		return target == null ? true : false;
	}

	public static final <T> boolean hasElements(Collection<T> collection) {
		return isNotEmpty(collection);
	}

	public static final <T> boolean hasNoElements(Collection<T> collection) {
		return isEmpty(collection);
	}

	public static boolean allNullElements(Object... targets) {
		return notNull(targets) ? allNullElement(targets) : true;
	}

	public static boolean hasAnyEntry(Map<?, ?> map) {
		return isNotEmpty(map);
	}


	public static boolean hasNoEntry(Map<?, ?> map) {
		return isEmpty(map);
	}

}
