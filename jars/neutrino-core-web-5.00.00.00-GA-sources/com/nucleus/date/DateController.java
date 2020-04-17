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
package com.nucleus.date;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.date.service.DateService;
import com.nucleus.era.Era;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.common.controller.BaseController;
import java.util.Date;
import com.nucleus.hijri.dao.HijriGregorianMappingDAO;

/**
 * The controller is used to handle various operations related to handling
 * locale specific dates
 * 
 * @author sandeep.grover
 * 
 */

@Transactional
@Controller
@RequestMapping(value = "/Date")
public class DateController extends BaseController {

	@Inject
	@Named("dateService")
	private DateService dateService;

	@Inject
	@Named("hijriGregorianMappingDAO")
	private HijriGregorianMappingDAO hijriGregorianMappingDAO;

	private static final String JAP_DATE_DELIMITER = "-";

	/**
	 * 
	 * @param days
	 * @param months
	 * @param year
	 * @return the corresponding japanese date
	 */
	@RequestMapping(value = "/getJapDate")
	@ResponseBody
	public String getJapDate(@RequestParam("days") String days,
			@RequestParam("months") String months,
			@RequestParam("year") Integer year) {
		StringBuffer japDate = new StringBuffer("");
		Era era = dateService.getEraBasedOnYear(year);
		if (era != null) {
			japDate.append(era.getEraSymbol());
			japDate.append(year - era.getStartYear() + 1);
			japDate.append(JAP_DATE_DELIMITER);
			japDate.append(months);
			japDate.append(JAP_DATE_DELIMITER);
			japDate.append(days);
		}
		return japDate.toString();
	}

	/**
	 * 
	 * @param yearOfKing
	 * @return the English Year corresponding to the year of the king
	 */
	@RequestMapping(value = "/getEngYear")
	@ResponseBody
	public Integer getEngDate(
			@RequestParam("yearOfKing") StringBuffer yearOfKing) {
		if (yearOfKing != null) {
			Integer startYear = dateService.getEraBasedOnYearOfKing(yearOfKing
					.charAt(0));
			Integer limitYear = dateService
					.getEraLimitBasedOnYearOfKing(yearOfKing.charAt(0));
			Integer maxStartYear = dateService.getMaxStartYear();
			yearOfKing.deleteCharAt(0);
			Integer yearTobeAdded = null;
			Integer engYear = null;
			try {
				yearTobeAdded = new Integer(yearOfKing.toString());
			} catch (NumberFormatException e) {
				BaseLoggers.flowLogger.error(e.getMessage());
				return -1;
			}
			if (yearTobeAdded > 0) {
				if (startYear != null) {
					engYear = new Integer(startYear + yearTobeAdded - 1);

					if (limitYear == null && startYear.equals(maxStartYear)) {
						return engYear;
					} else if (engYear < limitYear)
						return engYear;

					else {
						return -1;
					}
				}
			}
		}
		return -1;
	}

	@RequestMapping(value = "/getHijriDateFromDb")
	@ResponseBody
	public boolean getHijriDate (
			@RequestParam("hijriDate") String date) {

		Date gregorianDateFromHijri=hijriGregorianMappingDAO.getGregorianDateFromHijri(date);

		if(gregorianDateFromHijri!=null){
			return true;
		}
		else {
			return false;
		}


	}

}
