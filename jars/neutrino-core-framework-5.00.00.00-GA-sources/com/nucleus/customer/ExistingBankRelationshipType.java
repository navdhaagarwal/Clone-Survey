package com.nucleus.customer;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class ExistingBankRelationshipType extends GenericParameter {

    private static final long serialVersionUID = -3783967402947663241L;

    public static final String HOME_LOAN             = "HomeLoan";
    public static final String CAR_LOAN              = "CarLoan";
    public static final String PERSONAL_LOAN         = "PersonalLoan";
    public static final String CREDIT_CARD           = "CreditCard";
    public static final String SAVING_ACCOUNT        = "SavingsAccount";
    public static final String SALARY_ACCOUNT        = "SalaryAccount";
    public static final String CURRENT_ACCOUNT       = "CurrentAccount";
}
