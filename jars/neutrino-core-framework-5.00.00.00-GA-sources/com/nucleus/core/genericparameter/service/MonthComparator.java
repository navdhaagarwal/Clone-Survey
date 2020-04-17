package com.nucleus.core.genericparameter.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.entity.GenericParameter;

public class MonthComparator implements GenericParameterComparator<GenericParameter> {

	private static final String DATE_FORMAT_MMMM = "MMMM";

	@Override
	public int compare(GenericParameter genParam0, GenericParameter genParam1) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_MMMM);
		Date date1 = null;
		Date date2 = null;
		try {
			if(genParam0 != null && genParam0.getName() != null) {
				date1 = sdf.parse(genParam0.getName());
			}
			if(genParam1 != null && genParam1.getName() != null) {
				date2 = sdf.parse(genParam1.getName());
			}
		} catch (ParseException e) {
			throw new SystemException("Date Parse Exception while sorting Months : ", e);
		}
		if (date1 != null && date2 != null) {
			return date1.compareTo(date2);
		}
		return 0;

	}
}
