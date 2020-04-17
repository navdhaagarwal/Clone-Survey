package com.nucleus.demographics;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class AccountType extends GenericParameter {

    private static final long  serialVersionUID                  = 6737776579385415892L;

    public static final String ACCOUNT_TYPE_SAVINGS_BANK_ACCOUNT = "SavingAccount";

    public static final String ACCOUNT_TYPE_CURRENT_ACCOUNT      = "CurrentAccount";

    public static final String ACCOUNT_TYPE_NRE_ACCOUNT          = "NRE_ACCOUNT";

    public static final String ACCOUNT_TYPE_NRO_ACCOUNT          = "NRO_ACCOUNT";

    public static final String ACCOUNT_TYPE_FCRM_ACCOUNT         = "FCRM_ACCOUNT";

    public static final String ACCOUNT_TYPE_KCC_ACCOUNT          = "KCCAccount";

}
