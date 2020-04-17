package com.nucleus.cas.sequence;

import javax.inject.Named;

import org.joda.time.DateTime;

import com.nucleus.core.misc.util.DateUtils;

@Named("regionalSequenceService")
public class RegionalSequenceServiceImpl extends CasSequenceServiceImpl {

	@Override
	public String generateNextApplicationNumber() {
		DateTime today = DateUtils.getCurrentUTCTime();
		Integer year = today.getYear();
		Integer month = today.getMonthOfYear();
		Integer day = today.getDayOfMonth();
		String resultJulian = "";
		String yearDigit = ((Integer) today.getYear()).toString().substring(2,
				4);
		Integer[] monthValues = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
				31 };
		if (year % 4 == 0) {
			monthValues[1] = 29;
		}

		Integer julianDays = 0;
		for (Integer i = 0; i < month - 1; i++) {
			julianDays += monthValues[i];
		}
		julianDays += day;

		resultJulian = yearDigit + pad(julianDays, 3);

		return "LOS" + resultJulian
				+ pad(entityDao.getNextValue("application_sequence"), 5);
	}

	@Override
	public String[] generateNextApplicationNumbersRange(int incrementBy) {
		/*
		 * DateTime today = DateUtils.getCurrentUTCTime(); String yearDigit =
		 * ((Integer) today.getYear()).toString().substring(2, 4); String
		 * monthDigit = pad((Integer) today.getMonthOfYear(), 2); String
		 * dayDigit = pad((Integer) today.getDayOfMonth(), 2);
		 */
		DateTime today = DateUtils.getCurrentUTCTime();
		Integer year = today.getYear();
		Integer month = today.getMonthOfYear();
		Integer day = today.getDayOfMonth();
		String resultJulian = "";
		String yearDigit = ((Integer) today.getYear()).toString().substring(2,
				4);
		Integer[] monthValues = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
				31 };
		if (year % 4 == 0) {
			monthValues[1] = 29;
		}
		Integer julianDays = 0;
		for (Integer i = 0; i < month - 1; i++) {
			julianDays += monthValues[i];
		}
		julianDays += day;

		resultJulian = yearDigit + pad(julianDays, 3);

		String[] appNumbers = new String[2];
		Long startnumber = entityDao.getNextValue("application_sequence",
				incrementBy);

		appNumbers[0] = "LOS" + resultJulian + pad(startnumber, 5);
		appNumbers[1] = "LOS" + resultJulian
				+ pad(startnumber + incrementBy, 5);
		return appNumbers;
	}
}
