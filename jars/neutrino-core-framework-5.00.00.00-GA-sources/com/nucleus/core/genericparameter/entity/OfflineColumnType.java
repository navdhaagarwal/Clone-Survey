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
package com.nucleus.core.genericparameter.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
public class OfflineColumnType extends GenericParameter {

  private static final long serialVersionUID = 101418580006763850L;
    public static final String OWNERSHIP_TYPE = "OwnershipType";
    public static final String APPLICATION_TYPE = "ApplicationType";
    public static final String LOAN_PURPOSE = "LoanPurpose";
    public static final String RELATIONSHIP = "Relationship";
    public static final String SALUTATION = "Salutation";
    public static final String OCCUPATION_TYPE = "OccupationType";
    public static final String EMPLOYEE_TYPE = "EmployeeType";
    public static final String JOB_TITLE = "JobTitle";
    public static final String NATURE_OF_PROFESSION = "NatureOfProfession";
    public static final String ADDRESS_TYPE = "AddressType";
    public static final String PREFERRED_LANGUAGE = "PreferredLanguage";
    public static final String PRODUCT_TYPE = "ProductType";
    public static final String IDENTIFICATION_TYPE = "IdentificationType";
    public static final String PROMO_CODE = "Promocode";
    public static final String APPLICANT_TYPE = "ApplicantType";
    public static final String NATIONALITY = "Nationality";
    public static final String EMPLOYEMENT_TYPE = "EmploymentType";
    
    
    

}
