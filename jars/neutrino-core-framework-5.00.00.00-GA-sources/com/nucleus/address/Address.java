package com.nucleus.address;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.additionalEmployerDetails.entity.AddressTypeAED;
import com.nucleus.core.additionalEmployerDetails.entity.OwnershipStatus;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.internetchannel.AccomodationType;
import com.nucleus.internetchannel.ResidenceType;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.tehsil.entity.Tehsil;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
@Table(indexes ={@Index(name="contact_info_fk_index_address",columnList="contact_info_fk"),
        @Index(name="ADDRESS_ADD_LINE1_INDX",columnList="addressLine1"),
        @Index(name="ADDRESS_ADD_LINE2_INDX",columnList="addressLine2"),
        @Index(name="ADDRESS_ADD_LINE3_INDX",columnList="addressLine3"),
        @Index(name="ADDRESS_ADD_LINE4_INDX",columnList="addressLine4")
})
@DynamicInsert
@DynamicUpdate
public class Address extends BaseEntity {

    private static final long  serialVersionUID = 1L;

   /* for additional employerdetails only begins*/
    
    @Transient
    private Boolean customPincodeFlag;
    
    private String customPincodeValue;

    @ManyToOne(fetch = FetchType.LAZY)
    private AddressTypeAED        addressTypeAED;
    @ManyToOne(fetch = FetchType.LAZY)
    private OwnershipStatus ownershipStatus;
    private String street;

    private String poBox;
    @ManyToOne(fetch = FetchType.LAZY)
    private AdditionalAddressPurpose additionalAddressPurpose;
    
    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "address_fk")
    private List<GSTINDetails> GSTINDetails;

    public AdditionalAddressPurpose getAdditionalAddressPurpose() {
        return additionalAddressPurpose;
    }

    public void setAdditionalAddressPurpose(
            AdditionalAddressPurpose additionalAddressPurpose) {
        this.additionalAddressPurpose = additionalAddressPurpose;
    }

    public AddressTypeAED getAddressTypeAED() {
        return addressTypeAED;
    }

    public void setAddressTypeAED(AddressTypeAED addressTypeAED) {
        this.addressTypeAED = addressTypeAED;
    }

    public OwnershipStatus getOwnershipStatus() {
        return ownershipStatus;
    }

    public void setOwnershipStatus(OwnershipStatus ownershipStatus) {
        this.ownershipStatus = ownershipStatus;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

	
	 /*ends here*/



    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference
    private AddressType        addressType;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference
    private AddressTypeAgriculture        addressTypeAgriculture;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference
    private AccomodationType   accomodationType;

    private String             sameAsAddress;

    @EmbedInAuditAsValue
    private String             addressLine1;

    @EmbedInAuditAsValue
    private String             addressLine2;

    @EmbedInAuditAsValue
    private String             addressLine3;

    @EmbedInAuditAsValue
    private String             addressLine4;

    @ManyToOne(fetch = FetchType.LAZY)	
    @EmbedInAuditAsReference(columnToDisplay="countryName")
    private Country            country;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(columnToDisplay="stateCode")
    private State              state;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(columnToDisplay="cityCode")
    private City               city;

    private String             village;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference
    private VillageMaster villageMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference
    private Tehsil             tehsil;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(columnToDisplay="districtCode")
    private District           district;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(columnToDisplay="zipCode")
    private ZipCode            zipcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(columnToDisplay="intraRegionCode")
    private IntraCountryRegion region;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(columnToDisplay="areaCode")
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
    private Date           	   occupancyStartDate;

    @Column
    @Temporal(TemporalType.DATE)
    private Date               occupancyEndDate;

    private boolean            primaryAddress;

    private boolean            sendParcel;

    private String             additionalInfo;

    private String             expressionId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_fk")
    private List<PhoneNumber>  phoneNumberList;

    private Double             longitude;

    private Double             latitude;


    @Transient
    private String             fullAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference
    private ResidenceType      residenceType;

    private String             otherResidenceType;


    private String additionalField1;
    private String additionalField2;
    private String additionalField3;
    private String additionalField4;
    private String additionalField5;

    @ManyToOne(fetch = FetchType.LAZY)
    private AddressGeneric genericYesNo;

	public Boolean getIsCopiedAddress() {
		return isCopiedAddress;
	}

	public void setIsCopiedAddress(Boolean isCopiedAddress) {
		this.isCopiedAddress = isCopiedAddress;
	}

	private Boolean isMappedToMultipleAddrTypes;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "ADDR_OTHER_ADDRESS_TYPE_LIST", joinColumns = { @JoinColumn(name = "ADDRESS", referencedColumnName = "ID") })
	private List<AddressType> otherAddressTypeList;

	@Transient
	private Long[] otherAsAddressTypeListIds;

	private Boolean isCopiedAddress;
	
	private String gcdId;
	
	private String gstIn;

    private Boolean verified = Boolean.FALSE;

    @Column(name = "complete_address", length = 2000)
    private String completeAddress;

	@ManyToOne(fetch=FetchType.LAZY)
	private Street streetMaster;

    @Transient
    private String auditTrailIdentifier;

	private String additionalDropdownField1;

	private String additionalDropdownField2;

	public Boolean getIsMappedToMultipleAddrTypes() {
		return isMappedToMultipleAddrTypes;
	}

	public void setIsMappedToMultipleAddrTypes(Boolean isMappedToMultipleAddrTypes) {
		this.isMappedToMultipleAddrTypes = isMappedToMultipleAddrTypes;
	}

    public String getCompleteAddress() {
        return completeAddress;
    }

    public void setCompleteAddress(String completeAddress) {
        this.completeAddress = completeAddress;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }


	public List<AddressType> getOtherAddressTypeList() {
		return otherAddressTypeList;
	}

	public void setOtherAddressTypeList(List<AddressType> otherAddressTypeList) {
		this.otherAddressTypeList = otherAddressTypeList;
	}

	public Long[] getOtherAsAddressTypeListIds() {
		return otherAsAddressTypeListIds;
	}

	public void setOtherAsAddressTypeListIds(Long[] otherAsAddressTypeListIds) {
		this.otherAsAddressTypeListIds = otherAsAddressTypeListIds;
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
        if(streetMaster!=null){
            baseAddress = baseAddress.append(" ,").append(streetMaster.getStreetName());
        }
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


    public Boolean getCustomPincodeFlag() {

        if(null != zipcode){
            if(null != zipcode.getId() && (String.valueOf(zipcode.getId())).equals(customPincodeValue)){
                return false;
            }
        }

        return customPincodeFlag;
    }

    public void setCustomPincodeFlag(Boolean customPincodeFlag) {
        this.customPincodeFlag = customPincodeFlag;
    }

    public String getCustomPincodeValue() {
        return customPincodeValue;
    }

    public void setCustomPincodeValue(String customPincodeValue) {
        this.customPincodeValue = customPincodeValue;
    }

    public String getGstIn() {
		return gstIn;
	}

	public void setGstIn(String gstIn) {
		this.gstIn = gstIn;
	}

	@Override
    public String toString() {
        StringBuilder strBuf = new StringBuilder(addressLine1 + "\n" + addressLine2 + "\n" + addressLine3 + "\n"
                + addressLine4 + "\n");
        if (city != null)
            strBuf.append(city.getCityName() + "\n");
        if (state != null)
            strBuf.append(state.getStateName() + "\n");
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
        if (phoneNumberList != null && !phoneNumberList.isEmpty()) {
            List<PhoneNumber> clonePhoneNumberList = new ArrayList<>();
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
        address.setAdditionalAddressPurpose(additionalAddressPurpose);
       /* for aed*/
        address.setAddressTypeAED(addressTypeAED);
        address.setStreet(street);
        address.setPoBox(poBox);
        address.setOwnershipStatus(ownershipStatus);
        address.setVillageMaster(villageMaster);
        address.setTehsil(tehsil);
        address.setIsMappedToMultipleAddrTypes(isMappedToMultipleAddrTypes);
        address.setOtherAsAddressTypeListIds(otherAsAddressTypeListIds);
        address.setIsCopiedAddress(isCopiedAddress);
        if(CollectionUtils.isNotEmpty(otherAddressTypeList)){
            address.setOtherAddressTypeList(new ArrayList<AddressType>(otherAddressTypeList));
            }
		
		address.setCustomPincodeValue(customPincodeValue);
		address.setGstIn(gstIn);
		if(GSTINDetails!=null && GSTINDetails.size()>0){
            List<GSTINDetails> gstInAddressList=new ArrayList<GSTINDetails>();
            for(GSTINDetails gstInAddress:GSTINDetails){
                gstInAddressList.add((GSTINDetails)gstInAddress.cloneYourself(cloneOptions));
            }
            address.setGSTINDetails(gstInAddressList);
        }

        address.setStreetMaster(streetMaster);
		address.setVerified(verified);
		address.setCompleteAddress(completeAddress);
		address.setAdditionalDropdownField1(additionalDropdownField1);
        address.setAdditionalDropdownField2(additionalDropdownField2);
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

    public  String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public AddressTypeAgriculture getAddressTypeAgriculture() {
        return addressTypeAgriculture;
    }

    public void setAddressTypeAgriculture(AddressTypeAgriculture addressTypeAgriculture) {
        this.addressTypeAgriculture = addressTypeAgriculture;
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

    public String getAdditionalField1() {
        return additionalField1;
    }

    public void setAdditionalField1(String additionalField1) {
        this.additionalField1 = additionalField1;
    }

    public String getAdditionalField2() {
        return additionalField2;
    }

    public void setAdditionalField2(String additionalField2) {
        this.additionalField2 = additionalField2;
    }

    public String getAdditionalField3() {
        return additionalField3;
    }

    public void setAdditionalField3(String additionalField3) {
        this.additionalField3 = additionalField3;
    }

    public String getAdditionalField4() {
        return additionalField4;
    }

    public void setAdditionalField4(String additionalField4) {
        this.additionalField4 = additionalField4;
    }

    public String getAdditionalField5() {
        return additionalField5;
    }

    public void setAdditionalField5(String additionalField5) {
        this.additionalField5 = additionalField5;
    }

    public AddressGeneric getGenericYesNo() {
        return genericYesNo;
    }

    public void setGenericYesNo(AddressGeneric genericYesNo) {
        this.genericYesNo = genericYesNo;
    }

	public String getGcdId() {
		return gcdId;
	}

	public void setGcdId(String gcdId) {
		this.gcdId = gcdId;
	}

    public List<GSTINDetails> getGSTINDetails() {
        return GSTINDetails;
    }

    public void setGSTINDetails(List<GSTINDetails> gSTINDetails) {
        GSTINDetails = gSTINDetails;
    }

    public Street getStreetMaster() {
        return streetMaster;
    }

    public void setStreetMaster(Street streetMaster) {
        this.streetMaster = streetMaster;
    }
    public String getAuditTrailIdentifier() {
        return auditTrailIdentifier;
    }

    public void setAuditTrailIdentifier(String auditTrailIdentifier) {
        this.auditTrailIdentifier = auditTrailIdentifier;
    }
	 public String getAdditionalDropdownField1() {
        return additionalDropdownField1;
    }

    public void setAdditionalDropdownField1(String additionalDropdownField1) {
        this.additionalDropdownField1 = additionalDropdownField1;
    }

    public String getAdditionalDropdownField2() {
        return additionalDropdownField2;
    }

    public void setAdditionalDropdownField2(String additionalDropdownField2) {
        this.additionalDropdownField2 = additionalDropdownField2;
    }
}