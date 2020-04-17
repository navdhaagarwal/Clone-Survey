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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;

/**
 * Amount class to represent amount and its associated currency.
 * @author Nucleus Software Exports Limited
 */
public abstract class Amount implements Serializable {

    private static final long serialVersionUID = -6628227938362178772L;
    private static final String CURRENCY_ISO_CODE_INVALID="Unable to create Currency Instance. Currency ISO Code is invalid.";
    /*
     * As per several articles on internet this mode is best suited for finance applications. We can explore more in future
     * if required.
     */
    private static int        ROUNDING_MODE    = BigDecimal.ROUND_HALF_EVEN;

    /*
     * Default constructor 
     */
    Amount() {
    }

    BigDecimal toBigDecimal(String string) {
        BigDecimal bd = new BigDecimal(string);
        bd = bd.setScale(Money.DECIMAL_PRECISION, ROUNDING_MODE);
        return bd;
    }

    public abstract BigDecimal getValue();

    /**
     * Gets the value of currency for this Amount.
     */
    public abstract Currency getCurrency();

    /**
     * Gets the value of currency code for this Amount.
     */
    public String getCurrencyCode() {
        return getCurrency().getCurrencyCode();
    }

    public void setValue(String value) {
        try {
            setNumber(toBigDecimal(value));
        } catch (Exception e) {
            throw new InvalidDataException("Invalid value: " + value);
        }
    }

    public void setNumber(Number value) {
        if (value != null) {
            if (value instanceof BigDecimal) {
                setBigDecimalValue((BigDecimal) value);
            } else if (value instanceof Integer) {
                setBigDecimalValue(new BigDecimal((Integer) value));
            } else if (value instanceof Long) {
                setBigDecimalValue(new BigDecimal((Long) value));
            } else if (value instanceof Float) {
                setBigDecimalValue(new BigDecimal((Float) value));
            } else if (value instanceof Double) {
                setBigDecimalValue(new BigDecimal((Double) value));
            } else if (value instanceof Byte) {
                setBigDecimalValue(new BigDecimal((Byte) value));
            } else if (value instanceof Short) {
                setBigDecimalValue(new BigDecimal((Short) value));
            } else {
                setBigDecimalValue(new BigDecimal(value.toString()));
            }
        }

    }

    abstract void setBigDecimalValue(BigDecimal value);

    /**
     * Attempts to convert the value String into {@link Currency} and stores the value.
     * @throws InvalidDataException if {@link Currency} conversion fails due to invalid currency code
     * @throws InvalidDataException if the current Amount corresponds to Base Amount and currency code is not same as
     * base currency see ({@link #getBaseCurrency()}
     */
    public void setCurrency(String currencyCode) {
        if (currencyCode != null) {
        	try{
        		Currency currencyObj=Currency.getInstance(currencyCode.toUpperCase());
        		setCurrency(currencyObj);
        	}catch(Exception e){
        		  throw ExceptionBuilder.getInstance(SystemException.class,CURRENCY_ISO_CODE_INVALID,CURRENCY_ISO_CODE_INVALID).setOriginalException(e)
                  .setMessage(CURRENCY_ISO_CODE_INVALID).build();
        	}            
        }
    }

    public void setCurrencyCode(String currencyCode) {
        setCurrency(currencyCode);
    }


    public abstract void setCurrency(Currency currency);

    /**
     * Gets system's base currency.
     */
    public static Currency getBaseCurrency() {
        return Money.getBaseCurrency();
    }

    public static int getDecimalPrecision() {
        return Money.getDecimalPrecision();
    }

    abstract Amount newInstance();

    Amount getCloned() {
        Amount cloned = newInstance();
        cloned.setCurrency(getCurrencyCode());
        cloned.setBigDecimalValue(getValue());
        return cloned;
    }

    public void subtract(Amount amount) {
        if (getValue() == null) {
            setNumber(amount.getValue());
        } else {
            setNumber(amount.getValue().subtract(getValue()));
        }
        setCurrency(amount.getCurrency());
    }

    public void add(Amount amount) {
        if (getValue() == null) {
            setNumber(amount.getValue());
        } else {
            setNumber(amount.getValue().add(getValue()));
        }
        setCurrency(amount.getCurrency());
    }

    public void divide(Amount amount) {
        if (getValue() == null) {
            setNumber(amount.getValue());
        } else {
            // setNumber(amount.getValue().divide(getValue(), ROUNDING_MODE));
            // dividend and divisor were interchanged initially.
            setNumber(getValue().divide(amount.getValue(), 7, ROUNDING_MODE));
        }
        setCurrency(amount.getCurrency());
    }

    public void multiply(Amount amount) {
        if (getValue() == null) {
            setNumber(amount.getValue());
        } else {
            setNumber(amount.getValue().multiply(getValue()));
        }
        setCurrency(amount.getCurrency());
    }

    public void addAmountBy(Number amount) {
        setNumber(getValue().add(new BigDecimal(amount.toString())));
    }

    public void subtractAmountBy(Number amount) {
        setNumber(getValue().subtract(new BigDecimal(amount.toString())));
    }

    public void multiplyAmountBy(Number amount) {
        setNumber(getValue().multiply(new BigDecimal(amount.toString())));
    }

    public void divideAmountBy(Number amount) {
        setNumber(getValue().divide(new BigDecimal(amount.toString())));
    }

}