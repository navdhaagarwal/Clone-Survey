package com.nucleus.address;

import java.util.List;

import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.persistence.HibernateUtils;

@Transactional
@Named("addressInitializer")
public class AddressInitializer {

	public enum AddressLazyAttributes {
		ALL, ADDRESS_TYPE_AED, OWNERSHIP_STATUS, ADDITIONAL_ADDRESS_PURPOSE, ADDRESS_TYPE, ADDRESS_TYPE_AGRICULTURE, ACCOMODATION_TYPE, COUNTRY, STATE, CITY, VILLAGE_MASTER, TEHSIL, DISTRICT, ZIPCODE, REGION, AREA, RESIDENCE_TYPE, GENERIC_YES_OR_NO,STREET_MASTER;
	}

	public void initialize(List<Address> addresses, AddressLazyAttributes... attributes) {
		if (addresses != null && attributes != null) {
			for (Address address : addresses) {
				this.initialize(address, attributes);
			}
		}
	}

	public void initialize(Address address, AddressLazyAttributes... attributes) {
		if (address == null || attributes == null) {
			return;
		}

		for (AddressLazyAttributes attr : attributes) {
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ADDRESS_TYPE_AED) {
				HibernateUtils.initializeAndUnproxy(address.getAddressTypeAED());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.OWNERSHIP_STATUS) {
				HibernateUtils.initializeAndUnproxy(address.getOwnershipStatus());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ADDITIONAL_ADDRESS_PURPOSE) {
				HibernateUtils.initializeAndUnproxy(address.getAdditionalAddressPurpose());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ADDRESS_TYPE) {
				HibernateUtils.initializeAndUnproxy(address.getAddressType());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ADDRESS_TYPE_AGRICULTURE) {
				HibernateUtils.initializeAndUnproxy(address.getAddressTypeAgriculture());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ACCOMODATION_TYPE) {
				HibernateUtils.initializeAndUnproxy(address.getAccomodationType());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.COUNTRY) {
				HibernateUtils.initializeAndUnproxy(address.getCountry());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.STATE) {
				HibernateUtils.initializeAndUnproxy(address.getState());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.CITY) {
				HibernateUtils.initializeAndUnproxy(address.getCity());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.VILLAGE_MASTER) {
				HibernateUtils.initializeAndUnproxy(address.getVillageMaster());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.TEHSIL) {
				HibernateUtils.initializeAndUnproxy(address.getTehsil());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.DISTRICT) {
				HibernateUtils.initializeAndUnproxy(address.getDistrict());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ZIPCODE) {
				HibernateUtils.initializeAndUnproxy(address.getZipcode());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.REGION) {
				HibernateUtils.initializeAndUnproxy(address.getRegion());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.AREA) {
				HibernateUtils.initializeAndUnproxy(address.getArea());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.RESIDENCE_TYPE) {
				HibernateUtils.initializeAndUnproxy(address.getResidenceType());
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.GENERIC_YES_OR_NO) {
				HibernateUtils.initializeAndUnproxy(address.getGenericYesNo());
			}
			if(attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.STREET_MASTER){
				HibernateUtils.initializeAndUnproxy(address.getStreetMaster());
			}
		}
	}


	public void initializeAndUnproxy(Address address, AddressLazyAttributes... attributes) {
		if (address == null || attributes == null) {
			return;
		}

		for (AddressLazyAttributes attr : attributes) {
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ADDRESS_TYPE_AED) {
				address.setAddressTypeAED(HibernateUtils.initializeAndUnproxy(address.getAddressTypeAED()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.OWNERSHIP_STATUS) {
				address.setOwnershipStatus(HibernateUtils.initializeAndUnproxy(address.getOwnershipStatus()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ADDITIONAL_ADDRESS_PURPOSE) {
				address.setAdditionalAddressPurpose(HibernateUtils.initializeAndUnproxy(address.getAdditionalAddressPurpose()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ADDRESS_TYPE) {
				address.setAddressType(HibernateUtils.initializeAndUnproxy(address.getAddressType()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ADDRESS_TYPE_AGRICULTURE) {
				address.setAddressTypeAgriculture(HibernateUtils.initializeAndUnproxy(address.getAddressTypeAgriculture()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ACCOMODATION_TYPE) {
				address.setAccomodationType(HibernateUtils.initializeAndUnproxy(address.getAccomodationType()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.COUNTRY) {
				address.setCountry(HibernateUtils.initializeAndUnproxy(address.getCountry()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.STATE) {
				address.setState(HibernateUtils.initializeAndUnproxy(address.getState()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.CITY) {
				address.setCity(HibernateUtils.initializeAndUnproxy(address.getCity()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.VILLAGE_MASTER) {
				address.setVillageMaster(HibernateUtils.initializeAndUnproxy(address.getVillageMaster()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.TEHSIL) {
				address.setTehsil(HibernateUtils.initializeAndUnproxy(address.getTehsil()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.DISTRICT) {
				address.setDistrict(HibernateUtils.initializeAndUnproxy(address.getDistrict()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.ZIPCODE) {
				address.setZipcode(HibernateUtils.initializeAndUnproxy(address.getZipcode()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.REGION) {
				address.setRegion(HibernateUtils.initializeAndUnproxy(address.getRegion()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.AREA) {
				address.setArea(HibernateUtils.initializeAndUnproxy(address.getArea()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.RESIDENCE_TYPE) {
				address.setResidenceType(HibernateUtils.initializeAndUnproxy(address.getResidenceType()));
			}
			if (attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.GENERIC_YES_OR_NO) {
				address.setGenericYesNo(HibernateUtils.initializeAndUnproxy(address.getGenericYesNo()));
			}
			if(attr == AddressLazyAttributes.ALL || attr == AddressLazyAttributes.STREET_MASTER){
				address.setStreetMaster(HibernateUtils.initializeAndUnproxy(address.getStreetMaster()));
			}
		}
	}

}
