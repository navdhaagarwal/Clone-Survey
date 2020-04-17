package com.nucleus.address;

import java.util.List;

import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.persistence.HibernateUtils;

@Transactional
@Named("cityInitializer")
public class CityInitializer {

	public enum CityLazyAttributes {
		ALL, COUNTRY, STATE, LOCATION_TYPE, CITY_CATEGORIZATION,RISK
	}

	public void initialize(List<City> cities, CityLazyAttributes... attributes) {
		if (cities != null && attributes != null) {
			for (City city : cities) {
				this.initialize(city, attributes);
			}
		}
	}

	public void initialize(City city, CityLazyAttributes... attributes) {
		if (city == null || attributes == null) {
			return;
		}
		for (CityLazyAttributes attr : attributes) {
			if (attr == CityLazyAttributes.ALL || attr == CityLazyAttributes.COUNTRY) {
				HibernateUtils.initializeAndUnproxy(city.getCountry());
			}
			if (attr == CityLazyAttributes.ALL || attr == CityLazyAttributes.STATE) {
				HibernateUtils.initializeAndUnproxy(city.getState());
			}
			if (attr == CityLazyAttributes.ALL || attr == CityLazyAttributes.LOCATION_TYPE) {
				HibernateUtils.initializeAndUnproxy(city.getLocationType());
			}
			if (attr == CityLazyAttributes.ALL || attr == CityLazyAttributes.CITY_CATEGORIZATION) {
				HibernateUtils.initializeAndUnproxy(city.getCityCategorization());
			}
			if (attr == CityLazyAttributes.ALL || attr == CityLazyAttributes.RISK) {
				HibernateUtils.initializeAndUnproxy(city.getCityCategorization());
			}
		}

	}

}
