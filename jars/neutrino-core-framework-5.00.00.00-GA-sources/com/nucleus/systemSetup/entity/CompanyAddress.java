/**
 * The Class CompanyAddress is for persisting Comanpany's Address. Its different from Address Entity in a way that every
 * field here is a string. So that it won't conflict with the Entities like City, State etc.
 * 
 * @author Nucleus Software Exports Limited
 */

package com.nucleus.systemSetup.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class CompanyAddress extends BaseEntity {

    private static final long serialVersionUID = 7603109537549127732L;

    private String            addressLine1;
    private String            addressLine2;
    private String            addressLine3;
    private String            addressLine4;
    private String            country;
    private String            state;
    private String            city;
    private String            zipcode;

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

    public String getAddressLine4() {
        return addressLine4;
    }

    public void setAddressLine4(String addressLine4) {
        this.addressLine4 = addressLine4;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

}
