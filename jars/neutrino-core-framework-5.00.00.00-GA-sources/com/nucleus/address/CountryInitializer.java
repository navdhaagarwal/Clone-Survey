package com.nucleus.address;

import java.util.List;

import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.persistence.HibernateUtils;

@Transactional
@Named("countryInitializer")
public class CountryInitializer {

	public enum CountryLazyAttributes {
		ALL, COUNTRY_GROUP, REGION
	}

	public void initialize(List<Country> countries, CountryLazyAttributes... attributes) {
		if (countries != null && attributes != null) {
			for (Country country : countries) {
				this.initialize(country, attributes);
			}
		}
	}

	public void initialize(Country country, CountryLazyAttributes... attributes) {
		if (country == null || attributes == null) {
			return;
		}
		for (CountryLazyAttributes attr : attributes) {
			if (attr == CountryLazyAttributes.ALL || attr == CountryLazyAttributes.COUNTRY_GROUP) {
				HibernateUtils.initializeAndUnproxy(country.getCountryGroup());
			}
			if (attr == CountryLazyAttributes.ALL || attr == CountryLazyAttributes.REGION) {
				HibernateUtils.initializeAndUnproxy(country.getRegion());
			}
		}

	}

}
