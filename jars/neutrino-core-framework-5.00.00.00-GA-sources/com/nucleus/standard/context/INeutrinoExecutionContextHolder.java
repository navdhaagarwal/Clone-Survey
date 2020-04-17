package com.nucleus.standard.context;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.nucleus.currency.Currency;
import com.nucleus.entity.BaseTenant;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

public interface INeutrinoExecutionContextHolder extends
		IExecutionContextHolder {
	final static String BUSINESS_DATE = "businessDate";
	final static String USERINFO = "userInfo";
	final static String TENANT = "tenant";
	final static String CURRENCY = "baseCurrency";
	final static String BASE_CURRENCY_EXCEPTION = "base.currency.not.configured";

	Locale getDefaultLocale();

	User getUser();

	Date getSystemDateWithSystemTime();

	java.util.Date getBusinessDate();

	Date getBusinessDateWithSystemTime();

	UserInfo getLoggedInUserDetails();

	Long getTenantId();

	BaseTenant getTenant();

	Currency getBaseCurrency();

	Long getLoggedInBranchId();

	

}
