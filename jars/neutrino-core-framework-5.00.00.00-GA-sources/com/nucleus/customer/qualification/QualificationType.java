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
package com.nucleus.customer.qualification;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class QualificationType extends GenericParameter {

    private static final long  serialVersionUID                       = -5382408696265814695L;

    public static final String QUALIFICATION_TYPE_HSC                 = "HSC";
    public static final String QUALIFICATION_TYPE_SSC                 = "SSC";
    public static final String QUALIFICATION_TYPE_GRADUATION          = "GRADUATION";
    public static final String QUALIFICATION_TYPE_POST_GRADUATION     = "POST-GRADUATION";
    public static final String QUALIFICATION_TYPE_PROFESSIONAL_DEGREE = "P_Degree";
}
