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
package com.nucleus.core.organization.entity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.exceptions.SystemException;

/**
 * Entity to represent the topmost bank in the system through which everything will get driven.
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
public class RootOrganization extends Organization {

    private static final long serialVersionUID = -7164368419151742508L;

    @OneToOne
    public OrganizationBranch headOffice;

    @Override
    public void setName(String name) {
        throw new SystemException("Cannot change name of the Root Organization");
    }

    @Override
    public void setDescription(String name) {
        throw new SystemException("Cannot change description of the Root Organization");
    }

    @Override
    public void setPersistenceStatus(int status) {
        throw new SystemException("Cannot change persistence status of the Root Organization");
    }

    public OrganizationBranch getHeadOffice() {
        return headOffice;
    }

    public void setHeadOffice(OrganizationBranch headOffice) {
        this.headOffice = headOffice;
    }
}