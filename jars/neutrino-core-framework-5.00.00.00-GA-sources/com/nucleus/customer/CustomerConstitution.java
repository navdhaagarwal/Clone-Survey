/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.customer;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="SELECT,REFERENCES")
@Table(indexes={@Index(name="RAIM_PERF_45_4280",columnList="REASON_ACT_INACT_MAP")})
public class CustomerConstitution extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;
    /**
     * To define the code of customer constitution for which details are being maintained. This code will be unique for each constitution being defined.

     */
    private String            constitutionCode;
    /**
     * To define the description of the customer constitution being defined. This description will be unique for each constitution being defined.

     */
    @Sortable
    private String            constitutionDescription;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    private String            customerGroup;

    public String getConstitutionCode() {
        return constitutionCode;
    }

    public void setConstitutionCode(String constitutionCode) {
        this.constitutionCode = constitutionCode;
    }

    public String getConstitutionDescription() {
        return constitutionDescription;
    }

    public void setConstitutionDescription(String constitutionDescription) {
        this.constitutionDescription = constitutionDescription;
    }
    public String getCustomerGroup() {
        return customerGroup;
    }
    public void setCustomerGroup(String customerGroup) {
        this.customerGroup = customerGroup;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    public String toString() {
        return "CustomerConstitution [constitutionCode=" + constitutionCode + ", constitutionDescription="
                + constitutionDescription + "]";
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        CustomerConstitution customerConstitution = (CustomerConstitution) baseEntity;
        super.populate(customerConstitution, cloneOptions);
        customerConstitution.setConstitutionCode(constitutionCode);
        customerConstitution.setConstitutionDescription(constitutionDescription);
        customerConstitution.setCustomerGroup(customerGroup);
        if (reasonActInactMap != null) {
            customerConstitution.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        CustomerConstitution customerConstitution = (CustomerConstitution) baseEntity;
        super.populateFrom(customerConstitution, cloneOptions);
        this.setConstitutionCode(customerConstitution.getConstitutionCode());
        this.setConstitutionDescription(customerConstitution.getConstitutionDescription());
        this.setCustomerGroup(customerConstitution.getCustomerGroup());
        if (customerConstitution.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) customerConstitution.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    @Override
    public String getDisplayName() {
        return getConstitutionCode();
    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("Customer Constitution Master Object received to be saved ------------>");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Customer Constitution Code :" + constitutionCode);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Customer Constitution Description :" + constitutionDescription);
        log = stf.toString();
        return log;
    }
}
