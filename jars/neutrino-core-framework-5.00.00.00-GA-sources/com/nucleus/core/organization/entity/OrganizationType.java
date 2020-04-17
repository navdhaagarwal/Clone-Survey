/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.organization.entity;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(indexes={@Index(name="levelInHierarchy_index",columnList="levelInHierarchy")})
public class OrganizationType extends GenericParameter {

    public static final String ORGANIZATION_TYPE_BRANCH_SB = "subBranch";
    public static final String ORGANIZATION_TYPE_BRANCH    = "branch";
    public static final String ORGANIZATION_TYPE_BRANCH_RO = "regionalOffice";
    public static final String ORGANIZATION_TYPE_BRANCH_ZO = "zonalOffice";
    public static final String ORGANIZATION_TYPE_BRANCH_HO = "headOffice";

    private static final long  serialVersionUID            = 6118406381780128374L;

    private int                levelInHierarchy;

    public int getLevelInHierarchy() {
        return levelInHierarchy;
    }

    public void setLevelInHierarchy(int level) {
        this.levelInHierarchy = level;
    }

}
