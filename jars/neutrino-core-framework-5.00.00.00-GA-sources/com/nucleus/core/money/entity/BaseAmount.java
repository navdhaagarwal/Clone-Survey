/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.money.entity;

import java.math.BigDecimal;
import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.nucleus.core.exceptions.InvalidDataException;

/**
 * @author Nucleus Software Exports Limited
 */
@Embeddable
public class BaseAmount extends Amount {

    private static final long serialVersionUID = -6372038668732439091L;

    @Column(name="base_value",precision = 25, scale = 7)
    private BigDecimal        baseValue;
    @Column(name="base_currency_code")
    private String            baseCurrencyCode;

    public BaseAmount() {
        setCurrency(Money.getBaseCurrency());
    }

    /**
     * Gets the value of stored monetary value as BigDecimal
     */
    @Override
    public BigDecimal getValue() {
        return baseValue;
    }

    @Override
    public Currency getCurrency() {
        return baseCurrencyCode == null ? null : Currency.getInstance(baseCurrencyCode);
    }

    @Override
    public void setCurrency(Currency currency) {
        if (!currency.equals(Money.BASE_CURRENCY)) {
            throw new InvalidDataException(String.format("Expected base currency %s but got %s", Money.BASE_CURRENCY,
                    currency.getCurrencyCode()));
        }
        this.baseCurrencyCode = currency.getCurrencyCode();
    }

    @Override
    Amount newInstance() {
        return new BaseAmount();
    }

    @Override
    void setBigDecimalValue(BigDecimal value) {
        this.baseValue = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseCurrencyCode == null) ? 0 : baseCurrencyCode.hashCode());
        result = prime * result + ((baseValue == null) ? 0 : baseValue.hashCode());
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
        BaseAmount other = (BaseAmount) obj;
        if (baseCurrencyCode == null) {
            if (other.baseCurrencyCode != null)
                return false;
        } else if (!baseCurrencyCode.equals(other.baseCurrencyCode))
            return false;
        if (baseValue == null) {
            if (other.baseValue != null)
                return false;
        } else if (!baseValue.equals(other.baseValue))
            return false;
        return true;
    }

}
