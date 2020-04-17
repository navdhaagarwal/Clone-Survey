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
@Synonym(grant="SELECT")
@Table(indexes={@Index(name="RAIM_PERF_45_4058",columnList="REASON_ACT_INACT_MAP")})
public class CustomerCategory extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;
    /**
     * To define the code of customer category for which details are being maintained. This code will be unique for each customer category being defined.
     */
    private String            customerCategoryCode;
    /**
     * To define the description of the customer category being defined. This description will be unique for each customer category being defined.
     */
    @Sortable
    private String            customerCategoryDescription;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    public String getCustomerCategoryCode() {
        return customerCategoryCode;
    }

    public void setCustomerCategoryCode(String customerCategoryCode) {
        this.customerCategoryCode = customerCategoryCode;
    }

    public String getCustomerCategoryDescription() {
        return customerCategoryDescription;
    }

    public void setCustomerCategoryDescription(String customerCategoryDescription) {
        this.customerCategoryDescription = customerCategoryDescription;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    public String toString() {
        return "CustomerCategory [customerCategoryCode=" + customerCategoryCode + ", customerCategoryDescription="
                + customerCategoryDescription + "]";
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        CustomerCategory customerCategory = (CustomerCategory) baseEntity;
        super.populate(customerCategory, cloneOptions);
        customerCategory.setCustomerCategoryCode(customerCategoryCode);
        customerCategory.setCustomerCategoryDescription(customerCategoryDescription);
        if (reasonActInactMap != null) {
            customerCategory.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        CustomerCategory customerCategory = (CustomerCategory) baseEntity;
        super.populateFrom(customerCategory, cloneOptions);
        this.setCustomerCategoryCode(customerCategory.getCustomerCategoryCode());
        this.setCustomerCategoryDescription(customerCategory.getCustomerCategoryDescription());
        if (customerCategory.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) customerCategory.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    @Override
    public String getDisplayName() {
        return getCustomerCategoryCode();
    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("Customer Category Master Object received to be saved ------------>");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Customer Category Code :" + customerCategoryCode);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Customer Category Description :" + customerCategoryDescription);
        log = stf.toString();

        return log;
    }
}
