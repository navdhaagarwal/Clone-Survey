package com.nucleus.money.dao;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.currency.ConversionRate;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.persistence.EntityDao;

@Named("moneyDao")
public class MoneyDaoImpl implements MoneyDao{
	
	 @Inject
	 @Named("entityDao")
	 protected EntityDao         entityDao;

	@Override
	public ConversionRate getConversionRateBasedOnEffectiveDate(Long currencyId,Date effectiveDate){
        NamedQueryExecutor<ConversionRate> executor = new NamedQueryExecutor<ConversionRate>("Currency.getConversionRateBasedOnEffectiveDate")
                .addParameter("effectiveFromDate", effectiveDate).addParameter("currencyId", currencyId);
        return entityDao.executeQueryForSingleValue(executor);
    }
}
