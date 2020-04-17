package com.nucleus.core.datetime.entity;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.persistence.EntityDao;

/**
 * 
 * To persist the database time zone
 * 
 * @since GA 2.5
 * @author prateek.chachra
 *
 */
@Named("timeZoneService")
public class TimeZoneServiceImpl implements TimeZoneService {

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Override
	@Transactional
	public void persist(TimeZoneDetails timeZoneDetails) {
		entityDao.persist(timeZoneDetails);

	}

	@Override
	public TimeZoneDetails getExistingTimeZone() {

		TimeZoneDetails dbTimeZoneDetails=null;
		List<TimeZoneDetails> timeZoneDetails = (List<TimeZoneDetails>) entityDao.findAll(TimeZoneDetails.class);
		
		if (ValidatorUtils.hasElements(timeZoneDetails) ) {
			dbTimeZoneDetails = timeZoneDetails.get(0);
		}
		return dbTimeZoneDetails;
	}

}
