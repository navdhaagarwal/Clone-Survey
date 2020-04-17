package com.nucleus.core.income;

import com.nucleus.core.money.entity.Money;
import com.nucleus.entity.BaseEntity;

/**
 * @author Nucleus Software Exports Limited
 */

public class IncomeDetails extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Money             totalIncome;

    private Money             totalObligation;

    private Money             grossMonthlyIncome;

    private Money             netAmountIncome;

    public Money getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(Money totalIncome) {
        this.totalIncome = totalIncome;
    }

    public Money getTotalObligation() {
        return totalObligation;
    }

    public void setTotalObligation(Money totalObligation) {
        this.totalObligation = totalObligation;
    }

    public Money getGrossMonthlyIncome() {
        return grossMonthlyIncome;
    }

    public void setGrossMonthlyIncome(Money grossMonthlyIncome) {
        this.grossMonthlyIncome = grossMonthlyIncome;
    }

    public Money getNetAmountIncome() {
        return netAmountIncome;
    }

    public void setNetAmountIncome(Money netAmountIncome) {
        this.netAmountIncome = netAmountIncome;
    }

}
