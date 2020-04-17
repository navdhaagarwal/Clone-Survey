
package com.nucleus.accountType;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;


@Entity
@DynamicUpdate
@DynamicInsert
public class AccountTypeForCKYC extends GenericParameter{
    
    public static final String NORMAL="Normal";
    public static final String SIMPLIFIED_FOR_LOW_RISK_CUSTOMERS="Simplified (for low risk customers)";
    public static final String SMALL="Small";
    
    

}
