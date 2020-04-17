package com.nucleus.address;

import java.util.List;

import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.persistence.HibernateUtils;

@Transactional
@Named("intraCountryRegionInitializer")
public class IntraCountryRegionInitializer {

	public enum IntraCountryRegionLazyAttributes {
		ALL, COUNTRY
	}

	public void initialize(List<IntraCountryRegion> regions, IntraCountryRegionLazyAttributes... attributes) {
		if (regions != null && attributes != null) {
			for (IntraCountryRegion region : regions) {
				this.initialize(region, attributes);
			}
		}
	}

	public void initialize(IntraCountryRegion region, IntraCountryRegionLazyAttributes... attributes) {
		if (region == null || attributes == null) {
			return;
		}
		for (IntraCountryRegionLazyAttributes attr : attributes) {
			if (attr == IntraCountryRegionLazyAttributes.ALL || attr == IntraCountryRegionLazyAttributes.COUNTRY) {
				HibernateUtils.initializeAndUnproxy(region.getCountry());
			}
		}
	}

}
