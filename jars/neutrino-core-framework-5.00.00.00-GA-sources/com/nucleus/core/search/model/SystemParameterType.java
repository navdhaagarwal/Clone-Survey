package com.nucleus.core.search.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for Parameter Data type
 */
public interface SystemParameterType {

	public static final int SYSTEM_PARAMETER_TYPE_CURRENT_USER = 0;
	public static final int SYSTEM_PARAMETER_TYPE_CURRENT_DATE = 1;

	public static final List<Integer> ALL_STATUSES = Collections
			.unmodifiableList(Arrays.asList(SYSTEM_PARAMETER_TYPE_CURRENT_USER,
					SYSTEM_PARAMETER_TYPE_CURRENT_DATE));
}
