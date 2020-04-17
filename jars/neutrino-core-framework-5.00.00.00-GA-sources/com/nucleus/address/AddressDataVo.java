package com.nucleus.address;


import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class AddressDataVo implements Serializable {

    private String accomodationType;

    private String addressTypeAED;

    private String customPincodeValue;

    private String ownershipStatus;

    private String street;

    private String poBox;

    private String additionalAddressPurpose;

    private List<Long> GSTINDetails;

    private String addressType;

    private String addressTypeAgriculture;

    private String sameAsAddress;

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private Boolean customPincodeFlag;

    private String addressLine4;

    private Long country;

    private Long state;

    private Long city;

    private String village;

    private Long villageMaster;

    private Long tehsil;

    private Long district;

    private Long zipcode;

    private Long region;

    private Long area;

    private String landMark;

    private String taluka;

    private boolean activeAddress = true;

    private Integer numberOfYearsAtAddress;

    private Integer numberOfMonthsAtAddress;

    private Integer yearsInCurrentCity;

    private Integer monthsInCurrentCity;

    private Date occupancyStartDate;

    private Date occupancyEndDate;

    private boolean primaryAddress;

    private boolean sendParcel;

    private String additionalInfo;

    private String expressionId;

    private List<String> phoneNumberList;

    private String longitude;

    private String latitude;

    private String fullAddress;

    private String residenceType;

    private String otherResidenceType;

    private String additionalField1;

    private String additionalField2;

    private String additionalField3;

    private String additionalField4;

    private String additionalField5;

    private String genericYesNo;

    private Boolean isMappedToMultipleAddrTypes;

    private List<String> otherAddressTypeList;

    private List<String> otherAsAddressTypeListIds;

    private String isCopiedAddress;

    private String gcdId;

    private String gstIn;

    private String completeAddress;

    private Boolean verified;

    private Long streetType;

    private String additionalDropdownField1;

    private String additionalDropdownField2;

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

    public String getAccomodationType() {
        return accomodationType;
    }

    public void setAccomodationType(String accomodationType) {
        this.accomodationType = accomodationType;
    }

    public String getAddressTypeAED() {
        return addressTypeAED;
    }

    public void setAddressTypeAED(String addressTypeAED) {
        this.addressTypeAED = addressTypeAED;
    }

    public String getCustomPincodeValue() {
        return customPincodeValue;
    }

    public void setCustomPincodeValue(String customPincodeValue) {
        this.customPincodeValue = customPincodeValue;
    }

    public String getOwnershipStatus() {
        return ownershipStatus;
    }

    public void setOwnershipStatus(String ownershipStatus) {
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

    public String getAdditionalAddressPurpose() {
        return additionalAddressPurpose;
    }

    public void setAdditionalAddressPurpose(String additionalAddressPurpose) {
        this.additionalAddressPurpose = additionalAddressPurpose;
    }

    public List<Long> getGSTINDetails() {
        return GSTINDetails;
    }

    public void setGSTINDetails(List<Long> GSTINDetails) {
        this.GSTINDetails = GSTINDetails;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getAddressTypeAgriculture() {
        return addressTypeAgriculture;
    }

    public void setAddressTypeAgriculture(String addressTypeAgriculture) {
        this.addressTypeAgriculture = addressTypeAgriculture;
    }

    public String getSameAsAddress() {
        return sameAsAddress;
    }

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

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public Boolean getCustomPincodeFlag() {
        return customPincodeFlag;
    }

    public void setCustomPincodeFlag(Boolean customPincodeFlag) {
        this.customPincodeFlag = customPincodeFlag;
    }

    public String getAddressLine4() {
        return addressLine4;
    }

    public void setAddressLine4(String addressLine4) {
        this.addressLine4 = addressLine4;
    }

    public Long getCountry() {
        return country;
    }

    public void setCountry(Long country) {
        this.country = country;
    }

    public Long getState() {
        return state;
    }

    public void setState(Long state) {
        this.state = state;
    }

    public Long getCity() {
        return city;
    }

    public void setCity(Long city) {
        this.city = city;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public Long getVillageMaster() {
        return villageMaster;
    }

    public void setVillageMaster(Long villageMaster) {
        this.villageMaster = villageMaster;
    }

    public Long getTehsil() {
        return tehsil;
    }

    public void setTehsil(Long tehsil) {
        this.tehsil = tehsil;
    }

    public Long getDistrict() {
        return district;
    }

    public void setDistrict(Long district) {
        this.district = district;
    }

    public Long getZipcode() {
        return zipcode;
    }

    public void setZipcode(Long zipcode) {
        this.zipcode = zipcode;
    }

    public Long getRegion() {
        return region;
    }

    public void setRegion(Long region) {
        this.region = region;
    }

    public Long getArea() {
        return area;
    }

    public void setArea(Long area) {
        this.area = area;
    }

    public String getLandMark() {
        return landMark;
    }

    public void setLandMark(String landMark) {
        this.landMark = landMark;
    }

    public String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public boolean isActiveAddress() {
        return activeAddress;
    }

    public void setActiveAddress(boolean activeAddress) {
        this.activeAddress = activeAddress;
    }

    public Integer getNumberOfYearsAtAddress() {
        return numberOfYearsAtAddress;
    }

    public void setNumberOfYearsAtAddress(Integer numberOfYearsAtAddress) {
        this.numberOfYearsAtAddress = numberOfYearsAtAddress;
    }

    public Integer getNumberOfMonthsAtAddress() {
        return numberOfMonthsAtAddress;
    }

    public void setNumberOfMonthsAtAddress(Integer numberOfMonthsAtAddress) {
        this.numberOfMonthsAtAddress = numberOfMonthsAtAddress;
    }

    public Integer getYearsInCurrentCity() {
        return yearsInCurrentCity;
    }

    public void setYearsInCurrentCity(Integer yearsInCurrentCity) {
        this.yearsInCurrentCity = yearsInCurrentCity;
    }

    public Integer getMonthsInCurrentCity() {
        return monthsInCurrentCity;
    }

    public void setMonthsInCurrentCity(Integer monthsInCurrentCity) {
        this.monthsInCurrentCity = monthsInCurrentCity;
    }

    public Date getOccupancyStartDate() {
        return occupancyStartDate;
    }

    public void setOccupancyStartDate(Date occupancyStartDate) {
        this.occupancyStartDate = occupancyStartDate;
    }

    public Date getOccupancyEndDate() {
        return occupancyEndDate;
    }

    public void setOccupancyEndDate(Date occupancyEndDate) {
        this.occupancyEndDate = occupancyEndDate;
    }

    public boolean isPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(boolean primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public boolean isSendParcel() {
        return sendParcel;
    }

    public void setSendParcel(boolean sendParcel) {
        this.sendParcel = sendParcel;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getExpressionId() {
        return expressionId;
    }

    public void setExpressionId(String expressionId) {
        this.expressionId = expressionId;
    }

    public List<String> getPhoneNumberList() {
        return phoneNumberList;
    }

    public void setPhoneNumberList(List<String> phoneNumberList) {
        this.phoneNumberList = phoneNumberList;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getResidenceType() {
        return residenceType;
    }

    public void setResidenceType(String residenceType) {
        this.residenceType = residenceType;
    }

    public String getOtherResidenceType() {
        return otherResidenceType;
    }

    public void setOtherResidenceType(String otherResidenceType) {
        this.otherResidenceType = otherResidenceType;
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

    public String getGenericYesNo() {
        return genericYesNo;
    }

    public void setGenericYesNo(String genericYesNo) {
        this.genericYesNo = genericYesNo;
    }

    public Boolean getMappedToMultipleAddrTypes() {
        return isMappedToMultipleAddrTypes;
    }

    public void setMappedToMultipleAddrTypes(Boolean mappedToMultipleAddrTypes) {
        isMappedToMultipleAddrTypes = mappedToMultipleAddrTypes;
    }

    public List<String> getOtherAddressTypeList() {
        return otherAddressTypeList;
    }

    public void setOtherAddressTypeList(List<String> otherAddressTypeList) {
        this.otherAddressTypeList = otherAddressTypeList;
    }

    public List<String> getOtherAsAddressTypeListIds() {
        return otherAsAddressTypeListIds;
    }

    public void setOtherAsAddressTypeListIds(List<String> otherAsAddressTypeListIds) {
        this.otherAsAddressTypeListIds = otherAsAddressTypeListIds;
    }

    public String getIsCopiedAddress() {
        return isCopiedAddress;
    }

    public void setIsCopiedAddress(String isCopiedAddress) {
        this.isCopiedAddress = isCopiedAddress;
    }

    public String getGcdId() {
        return gcdId;
    }

    public void setGcdId(String gcdId) {
        this.gcdId = gcdId;
    }

    public String getGstIn() {
        return gstIn;
    }

    public void setGstIn(String gstIn) {
        this.gstIn = gstIn;
    }

    public Long getStreetType() {
        return streetType;
    }

    public void setStreetType(Long streetType) {
        this.streetType = streetType;
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

    public AddressDataVo createAddressVoFromObject(Address address) {
        AddressDataVo addressDataVo = new AddressDataVo();
        if (Objects.isNull(address)) {
            return addressDataVo;
        }
        if (Objects.nonNull(address.getAccomodationType())) {
            addressDataVo.setAccomodationType(address.getAccomodationType().getCode());
        }
        if (Objects.nonNull(address.getAddressTypeAED())) {
            addressDataVo.setAddressTypeAED(address.getAddressTypeAED().getCode());
        }

        addressDataVo.setCustomPincodeValue(address.getCustomPincodeValue());

        if (Objects.nonNull(address.getOwnershipStatus())) {
            addressDataVo.setOwnershipStatus(address.getOwnershipStatus().getCode());

        }

        addressDataVo.setStreet(address.getStreet());
        addressDataVo.setPoBox(address.getPoBox());


        if (Objects.nonNull(address.getAdditionalAddressPurpose())) {
            addressDataVo.setAdditionalAddressPurpose(address.getAdditionalAddressPurpose().getCode());

        }
        /*if (Objects.nonNull(address.getGSTINDetails()) && CollectionUtils.isNotEmpty(address.getGSTINDetails())) {
            List<Long> gstinIDList = new ArrayList<>();
            address.getGSTINDetails().forEach(gstinDetails -> {
                if (gstinDetails.getId() != null) {
                    gstinIDList.add(gstinDetails.getId());
                }
            });
            addressDataVo.setGSTINDetails(gstinIDList);

        }*/
        if (Objects.nonNull(address.getAddressType())) {
            addressDataVo.setAddressType(address.getAddressType().getCode());

        }
        if (Objects.nonNull(address.getAddressTypeAgriculture())) {
            addressDataVo.setAddressTypeAgriculture(address.getAddressTypeAgriculture().getCode());

        }

        addressDataVo.setSameAsAddress(address.getSameAsAddress());
        addressDataVo.setAddressLine1(address.getAddressLine1());
        addressDataVo.setAddressLine2(address.getAddressLine2());
        addressDataVo.setAddressLine3(address.getAddressLine3());
        addressDataVo.setAddressLine4(address.getAddressLine4());
        addressDataVo.setCompleteAddress(address.getCompleteAddress());
        addressDataVo.setVerified(address.getVerified());
        addressDataVo.setCustomPincodeFlag(address.getCustomPincodeFlag());

        if (Objects.nonNull(address.getCountry())) {
            addressDataVo.setCountry(address.getCountry().getId());
        }

        if (Objects.nonNull(address.getState())) {
            addressDataVo.setState(address.getState().getId());

        }
        if (Objects.nonNull(address.getCity())) {
            addressDataVo.setCity(address.getCity().getId());

        }
        addressDataVo.setVillage(address.getVillage());

        if (Objects.nonNull(address.getVillageMaster())) {
            addressDataVo.setVillageMaster(address.getVillageMaster().getId());

        }
        if (Objects.nonNull(address.getTehsil())) {
            addressDataVo.setTehsil(address.getTehsil().getId());

        }
        if (Objects.nonNull(address.getDistrict())) {
            addressDataVo.setDistrict(address.getDistrict().getId());

        }
        if (Objects.nonNull(address.getZipcode())) {
            addressDataVo.setZipcode(address.getZipcode().getId());

        }
        if (Objects.nonNull(address.getRegion())) {
            addressDataVo.setRegion(address.getRegion().getId());
        }
        if (Objects.nonNull(address.getArea())) {

            addressDataVo.setArea(address.getArea().getId());
        }

        addressDataVo.setLandMark(address.getLandMark());


        addressDataVo.setTaluka(address.getTaluka());

        if (Objects.nonNull(address.isActiveAddress())) {
            addressDataVo.setActiveAddress(address.isActiveAddress());

        }
        if (Objects.nonNull(address.getStreetMaster())) {
            addressDataVo.setStreetType(address.getStreetMaster().getId());

        }
        addressDataVo.setNumberOfYearsAtAddress(address.getNumberOfYearsAtAddress());
        addressDataVo.setNumberOfMonthsAtAddress(address.getNumberOfMonthsAtAddress());
        addressDataVo.setYearsInCurrentCity(address.getYearsInCurrentCity());
        addressDataVo.setMonthsInCurrentCity(address.getMonthsInCurrentCity());
        addressDataVo.setOccupancyStartDate(address.getOccupancyStartDate());
        addressDataVo.setOccupancyEndDate(address.getOccupancyEndDate());
        addressDataVo.setPrimaryAddress(address.isPrimaryAddress());
        addressDataVo.setSendParcel(address.isSendParcel());
        addressDataVo.setAdditionalInfo(address.getAdditionalInfo());
        addressDataVo.setExpressionId(address.getExpressionId());
        /*if(Objects.nonNull(address.getPhoneNumberList()) && CollectionUtils.isNotEmpty(address.getPhoneNumberList())){
            addressDataVo.setPhoneNumberList();

        }*/
/*
        if(Objects.nonNull(address.getLatitude())){
            addressDataVo.setLongitude(address.getLatitude());

        }
        if(Objects.nonNull()){
            addressDataVo.setLatitude();

        }
        if(Objects.nonNull()){
            addressDataVo.setFullAddress();

        }
*/
        if (Objects.nonNull(address.getResidenceType())) {
            addressDataVo.setResidenceType(address.getResidenceType().getCode());

        }
        addressDataVo.setOtherResidenceType(address.getOtherResidenceType());

        addressDataVo.setAdditionalField1(address.getAdditionalField1());
        addressDataVo.setAdditionalField2(address.getAdditionalField2());
        addressDataVo.setAdditionalField3(address.getAdditionalField3());
        addressDataVo.setAdditionalField4(address.getAdditionalField4());
        addressDataVo.setAdditionalField5(address.getAdditionalField5());

        if (Objects.nonNull(address.getGenericYesNo())) {
            addressDataVo.setGenericYesNo(address.getGenericYesNo().getCode());
        }

        if (Objects.nonNull(address.getIsMappedToMultipleAddrTypes())) {
            addressDataVo.setMappedToMultipleAddrTypes(address.getIsMappedToMultipleAddrTypes());

        }

        addressDataVo.setAdditionalDropdownField1(address.getAdditionalDropdownField1());
        addressDataVo.setAdditionalDropdownField2(address.getAdditionalDropdownField2());

        /*if(Objects.nonNull()){
            addressDataVo.setOtherAddressTypeList();

        }
        if(Objects.nonNull()){
            addressDataVo.setOtherAsAddressTypeListIds();

        }
        if(Objects.nonNull()){

            addressDataVo.setIsCopiedAddress();
        }
        if(Objects.nonNull()){
            addressDataVo.setGcdId();

        }
        if(Objects.nonNull()){
            addressDataVo.setGstIn();

        }*/

        return addressDataVo;
    }

    /*public AddressDataVo createAddressVoFromObject(Address address){
        AddressDataVo addressVo = new AddressDataVo();
        if(Objects.isNull(address)){
            return addressVo;
        }
        if(Objects.nonNull(address.getAccomodationType())) {
            addressVo.setAccomodationType(address.getAccomodationType().getCode());
        }
        if(Objects.nonNull(address.getAddressTypeAED())) {
            addressVo.setAddressTypeAED(address.getAddressTypeAED().getCode());
        }
        if(Objects.nonNull()){
            addressVo.setCustomPincodeValue();

        }
        if(Objects.nonNull()){
            addressVo.setOwnershipStatus();

        }
        if(Objects.nonNull()){
            addressVo.setStreet();

        }
        if(Objects.nonNull()){
            addressVo.setPoBox();

        }
        if(Objects.nonNull()){
            addressVo.setAdditionalAddressPurpose();

        }
        if(Objects.nonNull()){
            addressVo.setGSTINDetails();

        }
        if(Objects.nonNull()){
            addressVo.setAddressType();

        }
        if(Objects.nonNull()){
            addressVo.setAddressTypeAgriculture();

        }
        if(Objects.nonNull()){
            addressVo.setSameAsAddress();

        }
        if(Objects.nonNull()){
            addressVo.setAddressLine1();

        }
        if(Objects.nonNull()){
            addressVo.setAddressLine2();

        }
        if(Objects.nonNull()){

            addressVo.setAddressLine3();
        }
        if(Objects.nonNull()){
            addressVo.setCustomPincodeFlag();

        }
        if(Objects.nonNull()){
            addressVo.setAddressLine4();

        }
        if(Objects.nonNull()){
            addressVo.setCountry();

        }
        if(Objects.nonNull()){
            addressVo.setState();

        }
        if(Objects.nonNull()){
            addressVo.setCity();

        }
        if(Objects.nonNull()){
            addressVo.setVillage();

        }
        if(Objects.nonNull()){
            addressVo.setVillageMaster();

        }
        if(Objects.nonNull()){
            addressVo.setTehsil();

        }
        if(Objects.nonNull()){
            addressVo.setDistrict();

        }
        if(Objects.nonNull()){
            addressVo.setZipcode();

        }
        if(Objects.nonNull()){

            addressVo.setRegion();
        }
        if(Objects.nonNull()){

            addressVo.setArea();
        }
        if(Objects.nonNull()){
            addressVo.setLandMark();

        }
        if(Objects.nonNull()){
            addressVo.setTaluka();

        }
        if(Objects.nonNull()){
            addressVo.setActiveAddress();

        }
        if(Objects.nonNull()){
            addressVo.setNumberOfYearsAtAddress();

        }
        if(Objects.nonNull()){
            addressVo.setNumberOfMonthsAtAddress();

        }
        if(Objects.nonNull()){
            addressVo.setYearsInCurrentCity();

        }
        if(Objects.nonNull()){
            addressVo.setMonthsInCurrentCity();

        }
        if(Objects.nonNull()){
            addressVo.setOccupancyStartDate();

        }
        if(Objects.nonNull()){
            addressVo.setOccupancyEndDate();

        }
        if(Objects.nonNull()){

            addressVo.setPrimaryAddress();
        }
        if(Objects.nonNull()){
            addressVo.setSendParcel();

        }
        if(Objects.nonNull()){
            addressVo.setAdditionalInfo();

        }
        if(Objects.nonNull()){
            addressVo.setExpressionId();

        }
        if(Objects.nonNull()){
            addressVo.setPhoneNumberList();

        }
        if(Objects.nonNull()){
            addressVo.setLongitude();

        }
        if(Objects.nonNull()){
            addressVo.setLatitude();

        }
        if(Objects.nonNull()){
            addressVo.setFullAddress();

        }
        if(Objects.nonNull()){
            addressVo.setResidenceType();

        }
        if(Objects.nonNull()){
            addressVo.setOtherResidenceType();

        }
        if(Objects.nonNull()){
            addressVo.setAdditionalField1();

        }
        if(Objects.nonNull()){
            addressVo.setAdditionalField2();

        }
        if(Objects.nonNull()){
            addressVo.setAdditionalField3();

        }
        if(Objects.nonNull()){
            addressVo.setAdditionalField4();

        }
        if(Objects.nonNull()){
            addressVo.setAdditionalField5();

        }
        if(Objects.nonNull()){
            addressVo.setGenericYesNo();

        }
        if(Objects.nonNull()){
            addressVo.setIsMappedToMultipleAddrTypes();

        }
        if(Objects.nonNull()){
            addressVo.setOtherAddressTypeList();

        }
        if(Objects.nonNull()){
            addressVo.setOtherAsAddressTypeListIds();

        }
        if(Objects.nonNull()){

            addressVo.setIsCopiedAddress();
        }
        if(Objects.nonNull()){
            addressVo.setGcdId();

        }
        if(Objects.nonNull()){
            addressVo.setGstIn();

        }

        return addressVo;
    }*/
}