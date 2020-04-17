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
package com.nucleus.core.money.builder;

import java.util.Currency;

import com.nucleus.core.money.entity.Money;
import com.nucleus.core.validation.util.NeutrinoValidator;

/**
 * Builder class to quickly build money instance through fluent api.
 * @author Nucleus Software Exports Limited
 */
public class MoneyBuilder {

    private Money money;

    public MoneyBuilder() {
        this.money = new Money();
    }

    public MoneyBuilder(Money money) {
        NeutrinoValidator.notNull(money, "Money cannot be null");
        this.money = money;
    }

    public Money getMoney() {
        return money;
    }

    public MoneyBuilder setNonBaseAmountCurrency(Currency currency) {
        money.getNonBaseAmount().setCurrency(currency);
        return this;
    }

    public MoneyBuilder setNonBaseAmountCurrency(String currency) {
        money.getNonBaseAmount().setCurrency(currency);
        return this;
    }

    public MoneyBuilder setBaseAmountvalue(String amount) {
        money.getBaseAmount().setValue(amount);
        return this;
    }

    public MoneyBuilder setNonBaseAmountvalue(String amount) {
        money.getNonBaseAmount().setValue(amount);
        return this;
    }

}
