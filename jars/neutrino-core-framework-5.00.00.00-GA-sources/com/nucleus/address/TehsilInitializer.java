package com.nucleus.address;

import java.util.List;

import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.persistence.HibernateUtils;
import com.nucleus.tehsil.entity.Tehsil;

@Transactional
@Named("tehsilInitializer")
public class TehsilInitializer {

	public enum TehsilLazyAttributes {
		ALL, DISTRICT
	}

	public void initialize(List<Tehsil> tehsils, TehsilLazyAttributes... attributes) {
		if (tehsils != null && attributes != null) {
			for (Tehsil tehsil : tehsils) {
				this.initialize(tehsil, attributes);
			}
		}
	}

	public void initialize(Tehsil tehsil, TehsilLazyAttributes... attributes) {
		if (tehsil == null || attributes == null) {
			return;
		}
		for (TehsilLazyAttributes attr : attributes) {
			if (attr == TehsilLazyAttributes.ALL || attr == TehsilLazyAttributes.DISTRICT) {
				HibernateUtils.initializeAndUnproxy(tehsil.getDistrict());
			}
		}
	}
}
