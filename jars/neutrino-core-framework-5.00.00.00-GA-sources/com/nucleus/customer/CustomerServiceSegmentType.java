package com.nucleus.customer;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class CustomerServiceSegmentType extends GenericParameter {

    private static final long  serialVersionUID                                    = -4971965167228571958L;

    public static final String CUSTOMER_INCOME_TYPE_GLOBALPRIVATECLIENTS           = "GlobalPrivateClients";
    public static final String CUSTOMER_INCOME_TYPE_CLUBELITECORPORATECUSTOMERS    = "ClubEliteCorporateCustomers";
    public static final String CUSTOMER_INCOME_TYPE_PRIVATEBANKINGTITANIUMSTANDARD = "PrivateBankingTitaniumStandard";
    public static final String CUSTOMER_INCOME_TYPE_WEALTHMANAGEMENT               = "WealthManagement";
    public static final String CUSTOMER_INCOME_TYPE_PRIVATEBANKINGGOLDSTANDARD     = "PrivateBankingGoldStandard";
    public static final String CUSTOMER_INCOME_TYPE_NONRESIDENTINDIAN              = "NonResidentIndian";
    public static final String CUSTOMER_INCOME_TYPE_GENERALBANKING                 = "GeneralBanking";
    public static final String CUSTOMER_INCOME_TYPE_DEFAULTUNSEGMENTED             = "DefaultUnsegmented";

}
