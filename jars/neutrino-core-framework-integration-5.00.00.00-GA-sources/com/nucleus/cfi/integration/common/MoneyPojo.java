/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.cfi.integration.common;

import java.io.Serializable;
import java.math.BigDecimal;

import com.nucleus.core.validation.util.NeutrinoValidator;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class MoneyPojo implements Serializable {

    private static final long serialVersionUID = -7506601661763391344L;

    private BigDecimal        amount;
    private String            currencyCode;

    public MoneyPojo(BigDecimal amount, String currencyCode) {

        NeutrinoValidator.notNull(amount, "Can not construct MoneyPojo with null amount");
        NeutrinoValidator.notNull(amount, "Can not construct MoneyPojo with null currencyCode");
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

}
