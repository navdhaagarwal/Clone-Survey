package com.nucleus.address;

import java.util.List;

import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.persistence.HibernateUtils;

@Transactional
@Named("areaInitializer")
public class AreaInitializer {

	public enum AreaLazyAttributes {
		ALL, CITY, ZIPCODE, AREA_CATEGORIZATION
	}

	public void initialize(List<Area> areas, AreaLazyAttributes... attributes) {
		if (areas != null && attributes != null) {
			for (Area area : areas) {
				this.initialize(area, attributes);
			}
		}
	}

	public void initialize(Area area, AreaLazyAttributes... attributes) {
		if (area == null || attributes == null) {
			return;
		}
		for (AreaLazyAttributes attr : attributes) {
			if (attr == AreaLazyAttributes.ALL || attr == AreaLazyAttributes.CITY) {
				HibernateUtils.initializeAndUnproxy(area.getCity());
			}
			if (attr == AreaLazyAttributes.ALL || attr == AreaLazyAttributes.ZIPCODE) {
				HibernateUtils.initializeAndUnproxy(area.getZipcode());
			}
			if (attr == AreaLazyAttributes.ALL || attr == AreaLazyAttributes.AREA_CATEGORIZATION) {
				HibernateUtils.initializeAndUnproxy(area.getAreaCategorization());
			}
		}

	}

}
