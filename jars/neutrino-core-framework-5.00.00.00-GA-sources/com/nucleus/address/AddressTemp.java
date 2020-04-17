package com.nucleus.address;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Latitude;
import org.hibernate.search.annotations.Longitude;
import org.hibernate.search.annotations.Spatial;

import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.internetchannel.AccomodationType;
import com.nucleus.internetchannel.ResidenceType;
import com.nucleus.tehsil.entity.Tehsil;
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class AddressTemp extends BaseEntity {

    public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	private static final long  serialVersionUID = 1L;
    private String cifNumber;
    private String loanAccountNumber;
    private String            recordId;
    
   
	private String             approvalStatus;//M-Maker Stage,H-Approved and moved to History

    @ManyToOne(fetch=FetchType.LAZY)
    private AddressType        addressType;

    @ManyToOne(fetch=FetchType.LAZY)
    private AccomodationType   accomodationType;

    private String             sameAsAddress;

    private String             addressLine1;

    private String             addressLine2;

    private String             addressLine3;

    private String             addressLine4;

    @ManyToOne(fetch=FetchType.LAZY)
    private Country            country;

    @ManyToOne(fetch=FetchType.LAZY)
    private State              state;

    @ManyToOne(fetch=FetchType.LAZY)
    private City               city;

    private String             village;

    @ManyToOne(fetch=FetchType.LAZY)
    private District           district;

    @ManyToOne(fetch=FetchType.LAZY)
    private ZipCode            zipcode;

    @ManyToOne(fetch=FetchType.LAZY)
    private IntraCountryRegion region;

    @ManyToOne(fetch=FetchType.LAZY)
    private Area               area;

    private String             landMark;

    private String             taluka;

    private boolean            activeAddress    = true;

    private Integer            numberOfYearsAtAddress;

    private Integer            numberOfMonthsAtAddress;

    private Integer            yearsInCurrentCity;

    private Integer            monthsInCurrentCity;

    @Column
    @Temporal(TemporalType.DATE)
    private Date               occupancyStartDate;

    @Column
    @Temporal(TemporalType.DATE)
    private Date               occupancyEndDate;

    private boolean            primaryAddress;

    private boolean            sendParcel;

    private String             additionalInfo;

    private String             expressionId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_tmp_fk")
    private List<PhoneNumber>  phoneNumberList;

    @Longitude
    private Double             longitude;

    @Latitude
    private Double             latitude;

    @Transient
    private String             fullAddress;

    @ManyToOne(fetch=FetchType.LAZY)
    private ResidenceType      residenceType;

    private String             otherResidenceType;

    @ManyToOne(fetch=FetchType.LAZY)
    private VillageMaster villageMaster;

    @ManyToOne(fetch=FetchType.LAZY)
    private Tehsil tehsil;
    
    @Transient
    private boolean tempRecordExists ;

    public boolean isTempRecordExists() {
		return tempRecordExists;
	}

	public void setTempRecordExists(boolean tempRecordExists) {
		this.tempRecordExists = tempRecordExists;
	}

	public String getFullAddress() {
        if (city != null && state != null && country != null) {
            validateAndUpdateFullAddress();
        }
        return fullAddress;
    }


    public String getLogInfo() {
        return this.getFullAddress();
    }

    private void validateAndUpdateFullAddress() {
        StringBuilder baseAddress = new StringBuilder().append(city.getCityName()).append(" ,")
                .append(state.getStateName()).append(" ,").append(country.getCountryName());
        String tempFullAddress = null;

        if (addressLine4 != null && addressLine1 != null && addressLine2 != null && addressLine3 != null) {
            tempFullAddress = new StringBuilder(addressLine1).append(" ,").append(addressLine2).append(" ,")
                    .append(addressLine3).append(" ,").append(addressLine4).append(" ,").append(baseAddress).toString();
        } else if (addressLine3 != null && addressLine1 != null && addressLine2 != null) {
            tempFullAddress = new StringBuilder(addressLine1).append(" ,").append(addressLine2).append(" ,")
                    .append(addressLine3).append(" ,").append(baseAddress).toString();
        } else if (addressLine2 != null && addressLine1 != null) {
            tempFullAddress = new StringBuilder(addressLine1).append(" ,").append(addressLine2).append(" ,")
                    .append(baseAddress).toString();
        } else if (addressLine1 != null) {
            tempFullAddress = new StringBuilder(addressLine1).append(" ,").append(baseAddress).toString();
        }
        if (tempFullAddress != null && !tempFullAddress.equals(fullAddress)) {
            fullAddress = tempFullAddress;
        }
    }

    public String getCifNumber() {
		return cifNumber;
	}

	public void setCifNumber(String cifNumber) {
		this.cifNumber = cifNumber;
	}

	public String getLoanAccountNumber() {
		return loanAccountNumber;
	}

	public void setLoanAccountNumber(String loanAccountNumber) {
		this.loanAccountNumber = loanAccountNumber;
	}

    /**
    * @return the sameAsAddress
    */
    public String getSameAsAddress() {
        return sameAsAddress;
    }

    /**
    * @param sameAsAddress
    *            the sameAsAddress to set
    */
    public void setSameAsAddress(String sameAsAddress) {
        this.sameAsAddress = sameAsAddress;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public Integer getNumberOfMonthsAtAddress() {
        return numberOfMonthsAtAddress;
    }

    public void setNumberOfMonthsAtAddress(Integer numberOfMonthsAtAddress) {
        this.numberOfMonthsAtAddress = numberOfMonthsAtAddress;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public Integer getMonthsInCurrentCity() {
        return monthsInCurrentCity;
    }

    public void setMonthsInCurrentCity(Integer monthsInCurrentCity) {
        this.monthsInCurrentCity = monthsInCurrentCity;
    }

    public Integer getNumberOfYearsAtAddress() {
        return numberOfYearsAtAddress;
    }

    public void setNumberOfYearsAtAddress(Integer numberOfYearsAtAddress) {
        this.numberOfYearsAtAddress = numberOfYearsAtAddress;
    }

    public String getAddressLine4() {
        return addressLine4;
    }

    public void setAddressLine4(String addressLine4) {
        this.addressLine4 = addressLine4;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public Integer getYearsInCurrentCity() {
        return yearsInCurrentCity;
    }

    public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public void setYearsInCurrentCity(Integer yearsInCurrentCity) {
        this.yearsInCurrentCity = yearsInCurrentCity;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public ZipCode getZipcode() {
        return zipcode;
    }

    public void setZipcode(ZipCode zipcode) {
        this.zipcode = zipcode;
    }

    public IntraCountryRegion getRegion() {
        return region;
    }

    public void setRegion(IntraCountryRegion region) {
        this.region = region;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getLandMark() {
        return landMark;
    }

    /**
    * @return the phoneNumberList
    */
    public List<PhoneNumber> getPhoneNumberList() {
        return phoneNumberList;
    }

    /**
    * @param phoneNumberList
    *            the phoneNumberList to set
    */
    public void setPhoneNumberList(List<PhoneNumber> phoneNumberList) {
        this.phoneNumberList = phoneNumberList;
    }

    public void setLandMark(String landMark) {
        this.landMark = landMark;
    }

    public boolean isActiveAddress() {
        return activeAddress;
    }

    /**
    * Denotes if this address is active or is just for information
    */
    public void setActiveAddress(boolean active) {
        this.activeAddress = active;
    }

    public Date getOccupancyStartDate() {
        return occupancyStartDate;
    }

    /**
    * Sets the start date for occupancy on this address
    */
    public void setOccupancyStartDate(Date occupancyStartDate) {
        this.occupancyStartDate = occupancyStartDate;
    }

    public Date getOccupancyEndDate() {
        return occupancyEndDate;
    }

    /**
    * Sets the end date for occupancy on this address
    */
    public void setOccupancyEndDate(Date occupancyEndDate) {
        this.occupancyEndDate = occupancyEndDate;
    }

    public boolean isSendParcel() {
        return sendParcel;
    }

    /**
    * Denotes if parcel or any physical document can be sent on this address
    */
    public void setSendParcel(boolean sendParcel) {
        this.sendParcel = sendParcel;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
    * Add some additional information related to this address.
    */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public boolean isPrimaryAddress() {
        return primaryAddress;
    }

    /**
    * Denotes if the address is primary
    */
    public void setPrimaryAddress(boolean primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    /**
    * @return the expressionId
    */
    public String getExpressionId() {
        return expressionId;
    }

    /**
    * @param expressionId
    *            the expressionId to set
    */
    public void setExpressionId(String expressionId) {
        this.expressionId = expressionId;
    }

    @Override
    public String toString() {
        StringBuffer strBuf = new StringBuffer(addressLine1 + "\n" + addressLine2 + "\n" + addressLine3 + "\n"
                + addressLine4 + "\n");
        if (city != null)
            strBuf.append(city.getCityName() + "\n");
        if (state != null)
            strBuf.append(state.getStateName() + "/n");
        if (country != null)
            strBuf.append(country.getCountryName());
        return strBuf.toString();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Address address = (Address) baseEntity;
        super.populate(address, cloneOptions);
        address.setActiveAddress(activeAddress);
        address.setAdditionalInfo(additionalInfo);
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setAddressLine3(addressLine3);
        address.setAddressLine4(addressLine4);
        address.setAddressType(addressType);
        address.setAccomodationType(accomodationType);
        address.setArea(area);
        address.setCity(city);
        address.setCountry(country);
        address.setDistrict(district);
        address.setExpressionId(expressionId);
        address.setLandMark(landMark);
        address.setNumberOfYearsAtAddress(numberOfYearsAtAddress);
        address.setNumberOfMonthsAtAddress(numberOfMonthsAtAddress);
        address.setOccupancyEndDate(occupancyEndDate);
        address.setOccupancyStartDate(occupancyStartDate);
        if (phoneNumberList != null && phoneNumberList.size() > 0) {
            List<PhoneNumber> clonePhoneNumberList = new ArrayList<PhoneNumber>();
            for (PhoneNumber phoneNumber : phoneNumberList) {
                clonePhoneNumberList.add((PhoneNumber) phoneNumber.cloneYourself(cloneOptions));
            }
            address.setPhoneNumberList(clonePhoneNumberList);
        }
        address.setPrimaryAddress(primaryAddress);
        address.setRegion(region);
        address.setSameAsAddress(sameAsAddress);
        address.setSendParcel(sendParcel);
        address.setState(state);
        address.setVillage(village);
        address.setTaluka(taluka);
        address.setYearsInCurrentCity(yearsInCurrentCity);
        address.setMonthsInCurrentCity(monthsInCurrentCity);
        address.setZipcode(zipcode);
        address.setOtherResidenceType(otherResidenceType);
        address.setResidenceType(residenceType);
        address.setVillageMaster(villageMaster);
        address.setTehsil(tehsil);
    }

    public AccomodationType getAccomodationType() {
        return accomodationType;
    }

    public void setAccomodationType(AccomodationType accomodationType) {
        this.accomodationType = accomodationType;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public ResidenceType getResidenceType() {
        return residenceType;
    }

    public void setResidenceType(ResidenceType residenceType) {
        this.residenceType = residenceType;
    }

    public String getOtherResidenceType() {
        return otherResidenceType;
    }

    public void setOtherResidenceType(String otherResidenceType) {
        this.otherResidenceType = otherResidenceType;
    }

    public final String getTaluka() {
        return taluka;
    }

    public final void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public VillageMaster getVillageMaster() {
        return villageMaster;
    }

    public void setVillageMaster(VillageMaster villageMaster) {
        this.villageMaster = villageMaster;
    }

    public Tehsil getTehsil() {
        return tehsil;
    }

    public void setTehsil(Tehsil tehsil) {
        this.tehsil = tehsil;
    }
}
