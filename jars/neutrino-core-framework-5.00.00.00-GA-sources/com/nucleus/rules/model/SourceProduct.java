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
package com.nucleus.rules.model;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * Entity for the SourceProduct
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class SourceProduct extends GenericParameter {

    private static final long  serialVersionUID        = -119243391423862264L;

    public static final String SOURCE_PRODUCT_TYPE_CAS = "CAS";

    public static final String SOURCE_PRODUCT_TYPE_LMS = "LMS";
    public static final String SOURCE_PRODUCT_TYPE_LICENSE = "LICENSE";
    public static final String SOURCE_PRODUCT_NEUTRINO_FW = "FW";
    public static final String SOURCE_PRODUCT_NEUTRINO_COMMON = "COMMON";
    
    public static final String SOURCE_PRODUCT_NEUTRINO_BIZVIEW = "BIZVIEW";
    
    public static final String SOURCE_PRODUCT_TYPE_CMS = "CMS";

}
