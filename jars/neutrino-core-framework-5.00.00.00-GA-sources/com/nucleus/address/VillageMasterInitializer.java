package com.nucleus.address;

import java.util.List;

import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.persistence.HibernateUtils;

@Transactional
@Named("villageMasterInitializer")
public class VillageMasterInitializer {

	public enum VillageMasterLazyAttributes {
		ALL, DISTRICT, TEHSIL
	}

	public void initialize(List<VillageMaster> villageMasters, VillageMasterLazyAttributes... attributes) {
		if (villageMasters != null && attributes != null) {
			for (VillageMaster villageMaster : villageMasters) {
				this.initialize(villageMaster, attributes);
			}
		}
	}

	public void initialize(VillageMaster villageMaster, VillageMasterLazyAttributes... attributes) {
		if (villageMaster == null || attributes == null) {
			return;
		}
		for (VillageMasterLazyAttributes attr : attributes) {
			if (attr == VillageMasterLazyAttributes.ALL || attr == VillageMasterLazyAttributes.DISTRICT) {
				HibernateUtils.initializeAndUnproxy(villageMaster.getDistrict());
			}
			if (attr == VillageMasterLazyAttributes.ALL || attr == VillageMasterLazyAttributes.TEHSIL) {
				HibernateUtils.initializeAndUnproxy(villageMaster.getTehsil());
			}
		}
	}
}
