/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights
 * reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.money.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.money.builder.MoneyBuilder;
import com.nucleus.core.money.utils.MoneyUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.entity.CloneOptions;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.EntityLifeCycleData;
import com.nucleus.entity.EntityLifeCycleDataBuilder;

/**
 * Embeddable money class which can be modelled as {@link Embedded} reference in entities.
 * Each instance of Money contains two mutable child {@link Amount} instances. One {@link Amount} instance store money value 
 * for to base currency. The other Amount instance stores value of Non Base currency money value. 
 * @author Nucleus Software Exports Limited
 */
@Embeddable
public final class Money implements  Serializable, Comparable<Money> {

    private static final long serialVersionUID  = 2924666857260788281L;


    /*
     * Package level static fields
     */
    static Currency           BASE_CURRENCY;
    static int                DECIMAL_PRECISION = 7;

    private static boolean    initialized       = false;

    @Embedded
    private BaseAmount        baseAmount;
    @Embedded
    private NonBaseAmount     nonBaseAmount;

    /**
     * Money constructor to get a new Money instance
     */
    public Money() {
        if (!initialized) {
            throw new SystemException(
                    "Money object cannot be created before the default currency of the system is correctly initialized.");
        }
    }

    /**
     * Initialization method to initialize Money class with System's default ISO code and decimal precision.
     * Please note that this class can be only initialized once. Repeated calls to this method has no effect and only first
     * invocation value is considered final.
     * @param isoCode The default ISO code for system's money.
     * @param decimalStoragePrecision denotes the number of decimal precision to store with each amount
     */
    public static void initializeWithDefaultValues(String currencyCode, int decimalStoragePrecision) {
        if (initialized) {
            return;
        }
        Currency currency = null;
        try {
            currency = Currency.getInstance(currencyCode.toUpperCase());
        } catch (Exception e) {
            throw new SystemException("Invalid ISO code : " + currencyCode + " passed for system initialization.");
        }
        if (decimalStoragePrecision <= 0 || decimalStoragePrecision > 10) {
            throw new SystemException("Invalid precison specification: " + decimalStoragePrecision);
        }
        BASE_CURRENCY = currency;
        DECIMAL_PRECISION = decimalStoragePrecision;
        initialized = true;
    }

    /**
     * Gets the {@link Amount} instance associated with Base currency amount 
     */
    public Amount getBaseAmount() {
        if (baseAmount == null) {
            baseAmount = new BaseAmount();
        }
        return baseAmount;
    }

    /**
     * Gets the {@link Amount} instance associated with Non Base currency amount 
     */
    public Amount getNonBaseAmount() {
        if (nonBaseAmount == null) {
            nonBaseAmount = new NonBaseAmount();
        }
        return nonBaseAmount;
    }

    /**
     * Gets system's base currency.
     */
    public static Currency getBaseCurrency() {
        return BASE_CURRENCY;
    }

    public static int getDecimalPrecision() {
        return DECIMAL_PRECISION;
    }


    
    public Money cloneYourself(CloneOptions cloneOptions) {
     /*   Money cloned = (Money) SerializationUtils.deserialize(SerializationUtils.serialize(this));
        if (cloneOptions != null && cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.COPY_ID_KEY)) {
            cloned.id = this.id;
        } else {
            cloned.id = null;
        }
        return cloned;*/
    	
    	Money moneyClone = new MoneyBuilder().setBaseAmountvalue(this.getBaseAmount().getValue().toString()).getMoney();
    	
         populate(moneyClone, cloneOptions);
         return moneyClone;
    }
    
   /* private Money createEmptyClone() {
        try {
            return (Money) Hibernate.getClass(this).newInstance();
        } catch (Exception e) {
            throw new SystemException("Exception occured in clone for Money cloning", e);
        }
    }
    */
    
    protected void populate(Money clonedMoney, CloneOptions cloneOptions) {
        
      clonedMoney.baseAmount = this.baseAmount;
      clonedMoney.nonBaseAmount = this.nonBaseAmount;
      clonedMoney.BASE_CURRENCY = this.BASE_CURRENCY;
    }

    private void validateCurrency(Money other) {
        if (!this.getBaseAmount().getCurrencyCode().equals(other.getBaseAmount().getCurrencyCode())
                || (this.getNonBaseAmount().getCurrency()!=null
                && other.getNonBaseAmount().getCurrency()!=null
                && !this.getNonBaseAmount().getCurrencyCode().equals(other.getNonBaseAmount().getCurrencyCode()))){
            throw new UnsupportedOperationException("Operation can't be performed with amount having different currencies");
        }
    }


    public Money add(Money other) {
        validateCurrency(other);
        Money newMoney = new Money();
        newMoney.getBaseAmount().add(this.getBaseAmount());
        newMoney.getBaseAmount().add(other.getBaseAmount());
        newMoney.getNonBaseAmount().add(this.getNonBaseAmount());
        newMoney.getNonBaseAmount().add(other.getNonBaseAmount());
        return newMoney;
    }

    public Money subtract(Money other) {
        validateCurrency(other);
        Money newMoney = new Money();
        newMoney.getBaseAmount().add(this.getBaseAmount());
        newMoney.getBaseAmount().subtract(other.getBaseAmount());
        newMoney.getNonBaseAmount().add(this.getNonBaseAmount());
        newMoney.getNonBaseAmount().subtract(other.getNonBaseAmount());
        return newMoney;
    }

    public Money multiply(Money other) {
        validateCurrency(other);
        Money newMoney = new Money();
        newMoney.getBaseAmount().add(this.getBaseAmount());
        newMoney.getBaseAmount().multiply(other.getBaseAmount());
        newMoney.getNonBaseAmount().add(this.getNonBaseAmount());
        newMoney.getNonBaseAmount().multiply(other.getNonBaseAmount());
        return newMoney;
    }

    public Money divide(Money other) {
        validateCurrency(other);
        Money newMoney = new Money();
        newMoney.getBaseAmount().add(this.getBaseAmount());
        newMoney.getBaseAmount().divide(other.getBaseAmount());
        newMoney.getNonBaseAmount().add(this.getNonBaseAmount());
        newMoney.getNonBaseAmount().divide(other.getNonBaseAmount());
        return newMoney;
    }

    public Money divide(Number divisor) {
        Money newMoney = new Money();
        BaseAmount divisor1 = new BaseAmount();
        divisor1.setNumber(divisor);
        newMoney.getBaseAmount().add(this.getBaseAmount());
        newMoney.getBaseAmount().divide(divisor1);
        newMoney.getNonBaseAmount().add(this.getNonBaseAmount());
        newMoney.getNonBaseAmount().divide(divisor1);
        return newMoney;
    }
    
    public String getFormattedAmount(){
        return baseAmount.getCurrencyCode() + MoneyUtils.MONEY_DELIMITER + baseAmount.getValue();
   }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseAmount == null) ? 0 : baseAmount.hashCode());
        result = prime * result + ((nonBaseAmount == null) ? 0 : nonBaseAmount.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Money other = (Money) obj;
        if (baseAmount == null) {
            if (other.baseAmount != null)
                return false;
        } else if (!baseAmount.equals(other.baseAmount))
            return false;
        if (nonBaseAmount == null) {
            if (other.nonBaseAmount != null)
                return false;
        } else if (!nonBaseAmount.equals(other.nonBaseAmount))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (nonBaseAmount != null) {
            return nonBaseAmount.getCurrencyCode() + MoneyUtils.MONEY_DELIMITER + nonBaseAmount.getValue();
        } else {
            return "";
        }
    }

    @Override
    public int compareTo(Money money) {
        return baseAmount.getValue().compareTo(money.getBaseAmount().getValue());
    }

}
