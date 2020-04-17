package com.nucleus.money.dao;

import java.util.Date;

import com.nucleus.currency.ConversionRate;

public interface MoneyDao {

	ConversionRate getConversionRateBasedOnEffectiveDate(Long currencyId,Date effectiveDate);
}
