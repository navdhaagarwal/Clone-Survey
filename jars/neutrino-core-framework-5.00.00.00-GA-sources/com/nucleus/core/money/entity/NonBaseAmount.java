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

/**
 * @author Nucleus Software Exports Limited
 */
@Embeddable
public class NonBaseAmount extends Amount {

    private static final long serialVersionUID = -7994817409678050468L;

    @Column(name="non_base_value",precision = 25, scale = 7)
    private BigDecimal        nonBaseValue;
    
    @Column(name="non_base_curr_code")
    private String            nonBasecurrencyCode;

    @Override
    public BigDecimal getValue() {
        return nonBaseValue;
    }

    @Override
    public Currency getCurrency() {
        return nonBasecurrencyCode == null ? null : Currency.getInstance(nonBasecurrencyCode);
    }

    @Override
    public void setCurrency(Currency currency) {
        if (currency != null) {
            this.nonBasecurrencyCode = currency.getCurrencyCode();
        }
    }

    @Override
    Amount newInstance() {
        return new NonBaseAmount();
    }

    @Override
    public void setBigDecimalValue(BigDecimal value) {
        this.nonBaseValue = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nonBaseValue == null) ? 0 : nonBaseValue.hashCode());
        result = prime * result + ((nonBasecurrencyCode == null) ? 0 : nonBasecurrencyCode.hashCode());
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
        NonBaseAmount other = (NonBaseAmount) obj;
        if (nonBaseValue == null) {
            if (other.nonBaseValue != null)
                return false;
        } else if (!nonBaseValue.equals(other.nonBaseValue))
            return false;
        if (nonBasecurrencyCode == null) {
            if (other.nonBasecurrencyCode != null)
                return false;
        } else if (!nonBasecurrencyCode.equals(other.nonBasecurrencyCode))
            return false;
        return true;
    }

}