package com.nucleus.systemSetup.entity;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.contact.EMailInfo;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.misc.util.PasswordEncryptorUtil;
import com.nucleus.entity.BaseEntity;
import com.nucleus.user.UserProfile;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class CompanyLicenseInfo extends BaseEntity {

    private static final long         serialVersionUID = 1196733183588786010L;

    private String                    companyName;

    @OneToOne(cascade = CascadeType.ALL)
    private EMailInfo                 corporateEmail;

    private String                    companyWebsite;

    private String                    companyDomain;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "company_license_info")
    private CompanyAddress            companyAddress;

    private String                    smtpIPAddress;

    private String                    smtpPort;

    private String                    smtpServer;

    private String                    smtpUsername;

    private String                    smtpPassword;

    private String                    smsIPAddress;

    private String                    smsPort;

    private String                    smsUsername;

    private String                    smsPassword;

    private String                    smsGateway;

    @Column(updatable = false)
    private String                    hashKey;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "company_license_info")
    private List<CountryConfig>       countryConfigs;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "company_license_info")
    private List<ProductConfig>       productConfigs;

    @OneToOne(cascade = CascadeType.ALL)
    private UserProfile               userProfile;

    private Integer                   numOfUsers;

    private Integer                   usersGrowth;

    private Integer                   numOfLoans;

    private Integer                   transactionGrowth;

    private Integer                   numOfBranches;

    private Integer                   branchesGrowth;

    @Lob
    private String                    licenseKey;

    @Lob
    private String                    licenseText;

    @OneToMany(cascade = CascadeType.ALL)
    private List<ApplicationFeatures> applicationFeatures;

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getLicenseText() {
        return licenseText;
    }

    public void setLicenseText(String licenseText) {
        this.licenseText = licenseText;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getCompanyDomain() {
        return companyDomain;
    }

    public void setCompanyDomain(String companyDomain) {
        this.companyDomain = companyDomain;
    }

    public String getSmtpIPAddress() {
        return smtpIPAddress;
    }

    public void setSmtpIPAddress(String smtpIPAddress) {
        this.smtpIPAddress = smtpIPAddress;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public String getSmsIPAddress() {
        return smsIPAddress;
    }

    public void setSmsIPAddress(String smsIPAddress) {
        this.smsIPAddress = smsIPAddress;
    }

    public String getSmsPort() {
        return smsPort;
    }

    public void setSmsPort(String smsPort) {
        this.smsPort = smsPort;
    }

    public String getSmsUsername() {
        return smsUsername;
    }

    public void setSmsUsername(String smsUsername) {
        this.smsUsername = smsUsername;
    }

    public String getSmsPassword() {
        return smsPassword;
    }

    public void setSmsPassword(String smsPassword) {
        this.smsPassword = smsPassword;
    }

    /**
     * @return the companyName
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * @param companyName the companyName to set
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * @return the companyAddress
     */
    public CompanyAddress getCompanyAddress() {
        return companyAddress;
    }

    /**
     * @param companyAddress the companyAddress to set
     */
    public void setCompanyAddress(CompanyAddress companyAddress) {
        this.companyAddress = companyAddress;
    }

    /**
     * @return the corporateEmail
     */
    public EMailInfo getCorporateEmail() {
        return corporateEmail;
    }

    /**
     * @param corporateEmail the companyEmail to set
     */
    public void setCorporateEmail(EMailInfo corporateEmail) {
        this.corporateEmail = corporateEmail;
    }

    /**
     * @return the smtpServer
     */
    public String getSmtpServer() {
        return smtpServer;
    }

    /**
     * @param smtpServer the smtpServer to set
     */
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    /**
     * @return the smsGateway
     */
    public String getSmsGateway() {
        return smsGateway;
    }

    /**
     * @param smsGateway the smsGateway to set
     */
    public void setSmsGateway(String smsGateway) {
        this.smsGateway = smsGateway;
    }

    /**
     * @return the countryConfigs
     */
    public List<CountryConfig> getCountryConfigs() {
        return countryConfigs;
    }

    /**
     * @param countryConfigs the countryConfigs to set
     */
    public void setCountryConfigs(List<CountryConfig> countryConfigs) {
        this.countryConfigs = countryConfigs;
    }

    /**
     * @return the productConfigs
     */
    public List<ProductConfig> getProductConfigs() {
        return productConfigs;
    }

    /**
     * @param productConfigs the productConfigs to set
     */
    public void setProductConfigs(List<ProductConfig> productConfigs) {
        this.productConfigs = productConfigs;
    }

    /**
     * @return the userProfile
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }

    /**
     * @param userProfile the userProfile to set
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    /**
     * @return the numOfUsers
     */
    public Integer getNumOfUsers() {
        return numOfUsers;
    }

    /**
     * @param numOfUsers the numOfUsers to set
     */
    public void setNumOfUsers(Integer numOfUsers) {
        this.numOfUsers = numOfUsers;
    }

    /**
     * @return the usersGrowth
     */
    public Integer getUsersGrowth() {
        return usersGrowth;
    }

    /**
     * @param usersGrowth the usersGrowth to set
     */
    public void setUsersGrowth(Integer usersGrowth) {
        this.usersGrowth = usersGrowth;
    }

    /**
     * @return the numOfLoans
     */
    public Integer getNumOfLoans() {
        return numOfLoans;
    }

    /**
     * @param numOfLoans the numOfLoans to set
     */
    public void setNumOfLoans(Integer numOfLoans) {
        this.numOfLoans = numOfLoans;
    }

    /**
     * @return the transactionGrowth
     */
    public Integer getTransactionGrowth() {
        return transactionGrowth;
    }

    /**
     * @param transactionGrowth the transactionGrowth to set
     */
    public void setTransactionGrowth(Integer transactionGrowth) {
        this.transactionGrowth = transactionGrowth;
    }

    /**
     * @return the numOfBranches
     */
    public Integer getNumOfBranches() {
        return numOfBranches;
    }

    /**
     * @param numOfBranches the numOfBranches to set
     */
    public void setNumOfBranches(Integer numOfBranches) {
        this.numOfBranches = numOfBranches;
    }

    /**
     * @return the branchesGrowth
     */
    public Integer getBranchesGrowth() {
        return branchesGrowth;
    }

    /**
     * @param branchesGrowth the branchesGrowth to set
     */
    public void setBranchesGrowth(Integer branchesGrowth) {
        this.branchesGrowth = branchesGrowth;
    }

    /**
     * @return the applicationFeatures
     */
    public List<ApplicationFeatures> getApplicationFeatures() {
        return applicationFeatures;
    }

    /**
     * @param applicationFeatures the applicationFeatures to set
     */
    public void setApplicationFeatures(List<ApplicationFeatures> applicationFeatures) {
        this.applicationFeatures = applicationFeatures;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHaskKey(String hashKey) {
        this.hashKey = hashKey;
    }

    @PrePersist
    void createHashKey() {

        if (getEntityLifeCycleData() != null && getEntityLifeCycleData().getCreationTimeStamp() != null) {
            this.hashKey = Long.valueOf(getEntityLifeCycleData().getCreationTimeStamp().getMillis()).toString();
        } else if (this.hashKey == null) {
            this.hashKey = String.valueOf(System.currentTimeMillis());
        }
        try {
            this.smtpPassword = PasswordEncryptorUtil.encryptPassword(smtpPassword, hashKey);
            this.smsPassword = PasswordEncryptorUtil.encryptPassword(smsPassword, hashKey);
        } catch (NoSuchAlgorithmException e) {
            throw new SystemException(e);
        }
    }

}
