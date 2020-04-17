/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.systemSetup.entity;

import java.io.Serializable;

import com.nucleus.user.UserProfile;

/**
 * The Class SeedConfiguration.
 *
 * @author Nucleus Software Exports Limited
 */
public class SeedConfiguration implements Serializable {

    private static final long  serialVersionUID = 6873335771783856397L;

    private CompanyLicenseInfo companyLicenseInfo;

    private Long[]             countryConfigs;

    private Long[]             productConfigs;

    private UserProfile        userProfile;

    /**
     * @return the companyLicenseInfo
     */
    public CompanyLicenseInfo getCompanyLicenseInfo() {
        return companyLicenseInfo;
    }

    /**
     * @param companyLicenseInfo the companyLicenseInfo to set
     */
    public void setCompanyLicenseInfo(CompanyLicenseInfo companyLicenseInfo) {
        this.companyLicenseInfo = companyLicenseInfo;
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

    public Long[] getCountryConfigs() {
        return countryConfigs;
    }

    public void setCountryConfigs(Long[] countryConfigs) {
        this.countryConfigs = countryConfigs;
    }

    public Long[] getProductConfigs() {
        return productConfigs;
    }

    public void setProductConfigs(Long[] productConfigs) {
        this.productConfigs = productConfigs;
    }
}
