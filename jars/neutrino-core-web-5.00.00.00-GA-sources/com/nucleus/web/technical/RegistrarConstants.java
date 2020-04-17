package com.nucleus.web.technical;

public class RegistrarConstants {

    public static final String COUNTRY_GROUP                                  = "countryGroup";
    public static final String ID                                             = "id";
    public static final String NAME                                           = "name";
    public static final String CODE                                           = "code";
    public static final String REGION_NAME                                    = "regionName";
    public static final String INTRA_REGION_NAME                              = "intraRegionName";
    public static final String AREA_NAME                                      = "areaName";
    public static final String EMPLOYER_NAME                                  = "employerName";
    public static final String DISTRICT_NAME                                  = "districtName";
    public static final String CITY_NAME                                      = "cityName";
    public static final String STATE_NAME                                     = "stateName";
    public static final String BRANCH_NAME                                    = "branchName";
    public static final String ZIP_CODE                                       = "zipCode";
    public static final String CONSTITUTION_DESC                              = "constitutionDescription";
    public static final String CUSTOMER_CATEGORY_DESC                         = "customerCategoryDescription";
    public static final String BANK_NAME                                      = "bankName";
    public static final String IDENTIFICATION_TYPE_NAME                       = "identificationTypeName";
    public static final String DESCRIPTION                                    = "description";
    public static final String USER_NAME                                      = "username";
    public static final String COUNTRY_ISO_CODE                               = "countryISOCode";
    public static final String COUNTRY_NAME                                   = "countryName";
    public static final String NATIONALITY                                    = "nationality";
    public static final String CONSTITUTION_CODE                              = "constitutionCode";
    public static final String PRODUCT_NAME                                   = "productName";
    public static final String PRODUCT_CODE                                   = "productCode";
    public static final String CURRENCY_NAME                                  = "currencyName";
    public static final String CLASS_NAME                                     = "className";
    public static final String DISPLAY_ENTITY_NAME                            = "displayEntityName";
    public static final String SHORT_NAME                                     = "shortName";
    public static final String PROPERTY_TYPE                                  = "propertyType";
    public static final String EMPLOYER_CODE                                  = "employerCode";
    public static final String PARENT_CODE                                    = "parentCode";
    public static final String SCHEME_NAME                                    = "schemeName";
    public static final String SPECIALIZATION_NAME                            = "specializationName";
    public static final String NOTIFICATION_NAME                              = "notificationName";
    public static final String ACCESSORY_NAME                                 = "accessoryName";
    public static final String ACCESSORY_ID                                   = "id";
    public static final String ACCESSORY_DESC                                 = "accessoryDesc";
    public static final String ACCESSORY_COST                                 = "accessoryCost";
    public static final String NOTE_CODE                                      = "noteCode";
    public static final String LOAN_SCHEME_CODE                               = "schemeCode";
    public static final String LOAN_SCHEME_START_DATE                         = "startDate";
    public static final String FULL_NAME                                      = "fullName";
    // added by taru
    public static final String OTHER_NATURE_OF_BUISNESS                       = "otrNtrofBsnsName";
    public static final String OTHER_NATURE_OF_PROFESSION                     = "otrNtrofPfnName";
    public static final String EMPLOYEE_NUMBER                                = "employeeNumber";
    public static final String EMPLOYEE_NAME                                  = "employeeName";
    public static final String APPROVED                                       = "Approved";
    public static final String DELOGGED                                       = "Delogged";
    public static final String REJECTED                                       = "Rejected";
    public static final String PENDING                                        = "Pending";
    public static final String DISBURSED                                      = "Disbursed";
    
    public static final String DYNAMIC_PARAMETER_FIELD                        = "dynamicParameterName";

    public static String[]     EMPLOYEE_DETAILS                               = { EMPLOYEE_NUMBER, EMPLOYEE_NAME };

    public static String[]     AVAILABLE_COLUMN_NAMES_FOR_OCCUPATION          = { OTHER_NATURE_OF_BUISNESS,
            OTHER_NATURE_OF_PROFESSION                                       };

    // added by taru ends here

    public static String[]     city_with_state                                = { "cityName", "state" , "id" };

    public static String[]     AVAILABLE_COLUMN_NAMES_FOR_COUNTRY             = { COUNTRY_ISO_CODE, COUNTRY_NAME,
            NATIONALITY                                                      };

    public static String[]     AVAILABLE_COLUMN_NAMES_FOR_CONSTITUTION        = { CONSTITUTION_CODE, CONSTITUTION_DESC };

    public static String[]     AVAILABLE_COLUMN_NAMES_FOR_FAV                 = { NAME, DESCRIPTION };

    public static String[]     AVAILABLE_COLUMN_NAMES_FOR_FORM_CONFIG         = { NAME, CODE };

    public static String[]     AVAILABLE_COLUMN_NAMES_FOR_ASSET_CATEGORY      = { NAME, CODE, DESCRIPTION };

    public static String[]     AVAILABLE_COLUMN_NAMES_NAME_DESCRIPTION        = { NAME, DESCRIPTION };

    public static String[]     AVAILABLE_COLUMN_NAMES_NAME_CODE               = { NAME, CODE };

    public static String[]     AVAILABLE_COLUMN_NAMES_NAME_CODE_DESCRIPTION   = { NAME, CODE, DESCRIPTION };

    public static String[]     AVAILABLE_COLUMN_NAMES_CLASS_DISPLAY_ENTITY    = { CLASS_NAME, DISPLAY_ENTITY_NAME };

    public static String[]     AVAILABLE_COLUMN_NAMES_SHORT_NAME_DESCRIPTION  = { SHORT_NAME, DESCRIPTION };

    public static String[]     AVAILABLE_COLUMN_NAMES_EMPLOYER_NAME_CODE      = { EMPLOYER_NAME, EMPLOYER_CODE };

    public static String[]     AVAILABLE_COLUMN_NAMES_NAME_CODE_PARENT_CODE   = { NAME, CODE, PARENT_CODE };

    public static String[]     AVAILABLE_COLUMN_NAMES_DESCRIPTION_PARENT_CODE = { DESCRIPTION, PARENT_CODE };

    public static String[]     AVAILABLE_COLUMN_NAMES_CODE_DESCRIPTION        = { CODE, DESCRIPTION };

    public static String[]     AVAILABLE_COLUMN_NAMES_FOR_ACCESSORY           = { ACCESSORY_NAME, ACCESSORY_ID,
            ACCESSORY_DESC, ACCESSORY_COST                                   };
    public static final String SPECIAL_TABLE                                      = "Special";
}
