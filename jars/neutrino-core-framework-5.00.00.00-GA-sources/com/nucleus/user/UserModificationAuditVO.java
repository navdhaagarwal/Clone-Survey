package com.nucleus.user;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Transient;

public class UserModificationAuditVO {

    private String       userName;
    private String       password;
    private String       passwordExpiration;
    private String       emailId;
    private String       salutation;
    private String       firstName;
    private String       middleName;
    private String       lastName;
    private String       fourthName;
    private Boolean      isBusinessPartner;
    private String       aliasName;
    private Boolean      isSupervisor;
    private Boolean      isRelationshipOfficer;
    private String       passwordHintQuestion;
    private String       passwordHintAnswer;
    private String       securityHintQuestion0;
    private String       securityHintAnswer0;
    private String       securityHintQuestion1;
    private String       securityHintAnswer1;
    private String       deviationLevel;
    private String       ipAddress;
    private String       fromIpAddress;
    private String       toIpAddress;
    private BigDecimal   sanctionedAmount;
    private Boolean      isTeamLead;
    private String       country;
    private String       flatNumber;
    private String       addressLine2;
    private String       addressLine3;
    private String       region;
    private String       state;
    private String       city;
    private String       district;
    private Long         pincode;
    private String       area;
    private String       taluka;
    private String       village;
    @Transient
    private List<String> roleNames;
    private String       moduleName;
    @Transient
    private List<String> branchCode;
    private String       defaultBranch;
    private Character    accessToAllBranches;
    @Transient
    private List<String> branchAdminCode;
    @Transient
    private List<String> productCode;
    private Character    accessToAllProducts;
    private Integer      noOfTeamsRepresentedBy;
    private Integer      noOfTeamsLedBy;
    @Transient
    private List<String> teamNames;
    private Boolean      mobilityEnabled;
    private Boolean      challengeEnabled;
    private Integer      challenge;
    private String       phoneNumber;
    private String       mobileNumber;
    private String       comminicationEmail;
    private String       associatedBusinessPartner;
    private Boolean      isSuperAdmin;
    
    private Boolean      isDeviceAuthEnabled;
    private List<UserDeviceMapping> registeredDeviceList ;
    private String userCategory;
    private String userClassification;
    private String userDepartment;

    public Boolean getIsSuperAdmin() {
        return isSuperAdmin;
    }

    public void setIsSuperAdmin(Boolean isSuperAdmin) {
        this.isSuperAdmin = isSuperAdmin;
    }

    public String getAssociatedBusinessPartner() {
        return associatedBusinessPartner;
    }

    public void setAssociatedBusinessPartner(String associatedBusinessPartner) {
        this.associatedBusinessPartner = associatedBusinessPartner;
    }

    public String getFromIpAddress() {
        return fromIpAddress;
    }

    public void setFromIpAddress(String fromIpAddress) {
        this.fromIpAddress = fromIpAddress;
    }

    public String getToIpAddress() {
        return toIpAddress;
    }

    public void setToIpAddress(String toIpAddress) {
        this.toIpAddress = toIpAddress;
    }

    public Boolean getIsBusinessPartner() {
        return isBusinessPartner;
    }

    public void setIsBusinessPartner(Boolean isBusinessPartner) {
        this.isBusinessPartner = isBusinessPartner;
    }

    public Boolean getIsSupervisor() {
        return isSupervisor;
    }

    public void setIsSupervisor(Boolean isSupervisor) {
        this.isSupervisor = isSupervisor;
    }

    public Boolean getIsRelationshipOfficer() {
        return isRelationshipOfficer;
    }

    public void setIsRelationshipOfficer(Boolean isRelationshipOfficer) {
        this.isRelationshipOfficer = isRelationshipOfficer;
    }

    public Boolean getIsTeamLead() {
        return isTeamLead;
    }

    public void setIsTeamLead(Boolean isTeamLead) {
        this.isTeamLead = isTeamLead;
    }

    public Boolean getMobilityEnabled() {
        return mobilityEnabled;
    }

    public void setMobilityEnabled(Boolean mobilityEnabled) {
        this.mobilityEnabled = mobilityEnabled;
    }

    public Boolean getChallengeEnabled() {
        return challengeEnabled;
    }

    public void setChallengeEnabled(Boolean challengeEnabled) {
        this.challengeEnabled = challengeEnabled;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getComminicationEmail() {
        return comminicationEmail;
    }

    public void setComminicationEmail(String comminicationEmail) {
        this.comminicationEmail = comminicationEmail;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getSecurityHintQuestion0() {
        return securityHintQuestion0;
    }

    public void setSecurityHintQuestion0(String securityHintQuestion0) {
        this.securityHintQuestion0 = securityHintQuestion0;
    }

    public String getSecurityHintAnswer0() {
        return securityHintAnswer0;
    }

    public void setSecurityHintAnswer0(String securityHintAnswer0) {
        this.securityHintAnswer0 = securityHintAnswer0;
    }

    public String getSecurityHintQuestion1() {
        return securityHintQuestion1;
    }

    public void setSecurityHintQuestion1(String securityHintQuestion1) {
        this.securityHintQuestion1 = securityHintQuestion1;
    }

    public String getSecurityHintAnswer1() {
        return securityHintAnswer1;
    }

    public void setSecurityHintAnswer1(String securityHintAnswer1) {
        this.securityHintAnswer1 = securityHintAnswer1;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordExpiration() {
        return passwordExpiration;
    }

    public void setPasswordExpiration(String passwordExpiration) {
        this.passwordExpiration = passwordExpiration;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFourthName() {
        return fourthName;
    }

    public void setFourthName(String fourthName) {
        this.fourthName = fourthName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getPasswordHintQuestion() {
        return passwordHintQuestion;
    }

    public void setPasswordHintQuestion(String passwordHintQuestion) {
        this.passwordHintQuestion = passwordHintQuestion;
    }

    public String getPasswordHintAnswer() {
        return passwordHintAnswer;
    }

    public void setPasswordHintAnswer(String passwordHintAnswer) {
        this.passwordHintAnswer = passwordHintAnswer;
    }

    public String getDeviationLevel() {
        return deviationLevel;
    }

    public void setDeviationLevel(String deviationLevel) {
        this.deviationLevel = deviationLevel;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public BigDecimal getSanctionedAmount() {
        return sanctionedAmount;
    }

    public void setSanctionedAmount(BigDecimal sanctionedAmount) {
        this.sanctionedAmount = sanctionedAmount;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Long getPincode() {
        return pincode;
    }

    public void setPincode(Long pincode) {
        this.pincode = pincode;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public Character getAccessToAllBranches() {
        return accessToAllBranches;
    }

    public void setAccessToAllBranches(Character accessToAllBranches) {
        this.accessToAllBranches = accessToAllBranches;
    }

    public Character getAccessToAllProducts() {
        return accessToAllProducts;
    }

    public void setAccessToAllProducts(Character accessToAllProducts) {
        this.accessToAllProducts = accessToAllProducts;
    }

    public Integer getNoOfTeamsRepresentedBy() {
        return noOfTeamsRepresentedBy;
    }

    public void setNoOfTeamsRepresentedBy(Integer noOfTeamsRepresentedBy) {
        this.noOfTeamsRepresentedBy = noOfTeamsRepresentedBy;
    }

    public Integer getNoOfTeamsLedBy() {
        return noOfTeamsLedBy;
    }

    public void setNoOfTeamsLedBy(Integer noOfTeamsLedBy) {
        this.noOfTeamsLedBy = noOfTeamsLedBy;
    }

    public Integer getChallenge() {
        return challenge;
    }

    public void setChallenge(Integer challenge) {
        this.challenge = challenge;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }

    public List<String> getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(List<String> branchCode) {
        this.branchCode = branchCode;
    }

    public List<String> getBranchAdminCode() {
        return branchAdminCode;
    }

    public void setBranchAdminCode(List<String> branchAdminCode) {
        this.branchAdminCode = branchAdminCode;
    }

    public List<String> getProductCode() {
        return productCode;
    }

    public void setProductCode(List<String> productCode) {
        this.productCode = productCode;
    }

    public List<String> getTeamNames() {
        return teamNames;
    }

    public void setTeamNames(List<String> teamNames) {
        this.teamNames = teamNames;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

	public Boolean getIsDeviceAuthEnabled() {
		return isDeviceAuthEnabled;
	}

	public void setIsDeviceAuthEnabled(Boolean isDeviceAuthEnabled) {
		this.isDeviceAuthEnabled = isDeviceAuthEnabled;
	}

	public List<UserDeviceMapping> getRegisteredDeviceList() {
		return registeredDeviceList;
	}

	public void setRegisteredDeviceList(List<UserDeviceMapping> registeredDeviceList) {
		this.registeredDeviceList = registeredDeviceList;
	}

	public String getUserCategory() {
		return userCategory;
	}

	public void setUserCategory(String userCategory) {
		this.userCategory = userCategory;
	}

	public String getUserClassification() {
		return userClassification;
	}

	public void setUserClassification(String userClassification) {
		this.userClassification = userClassification;
	}

	public String getUserDepartment() {
		return userDepartment;
	}

	public void setUserDepartment(String userDepartment) {
		this.userDepartment = userDepartment;
	}

}
