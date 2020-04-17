/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.searchframework.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class SearchAttributeDataType extends GenericParameter {
    // ~ Static fields/initializers =================================================================

    private static final long         serialVersionUID              = -5265654164895690972L;

    // ~ Instance fields ============================================================================

    public static final int           PARAMETER_DATA_TYPE_STRING    = 1;
    public static final int           PARAMETER_DATA_TYPE_INTEGER   = 2;
    public static final int           PARAMETER_DATA_TYPE_LONG      = 3;
    public static final int           PARAMETER_DATA_TYPE_NUMBER    = 4;
    public static final int           PARAMETER_DATA_TYPE_BOOLEAN   = 5;
    public static final int           PARAMETER_DATA_TYPE_DATE      = 6;
    public static final int           PARAMETER_DATA_TYPE_YEARS     = 7;
    public static final int           PARAMETER_DATA_TYPE_REFERENCE = 8;

    public static final List<Integer> ALL_STATUSES                  = Collections.unmodifiableList(Arrays.asList(
                                                                            PARAMETER_DATA_TYPE_STRING,
                                                                            PARAMETER_DATA_TYPE_INTEGER,
                                                                            PARAMETER_DATA_TYPE_NUMBER,
                                                                            PARAMETER_DATA_TYPE_LONG,
                                                                            PARAMETER_DATA_TYPE_BOOLEAN,
                                                                            PARAMETER_DATA_TYPE_DATE,
                                                                            PARAMETER_DATA_TYPE_YEARS,
                                                                            PARAMETER_DATA_TYPE_REFERENCE));

    public static final String        DATE_PATTERN                  = "dd/MM/yyyy HH:mm:ss";
}
