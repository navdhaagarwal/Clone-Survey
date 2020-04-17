package com.nucleus.standard.context;

import com.nucleus.currency.Currency;
import com.nucleus.entity.BaseTenant;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.finnone.pro.lmsbase.domainobject.Tenant;
import com.nucleus.finnone.pro.lmsbase.serviceinterface.ITenantService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

@Named("neutrinoExecutionContextHolder")
public class NeutrinoExecutionContextHolder extends ExecutionContextHolder
		implements INeutrinoExecutionContextHolder {
	
	@Inject
	@Named("tenantService")
	private ITenantService tenantService;

	@Override
	public Date getSystemDateWithSystemTime() {
		return new Date();
	}

	@Override
	public java.util.Date getBusinessDate() {
		Date businessDate = (Date) NeutrinoExecutionContextSupport
				.getFromGlobalContext(NeutrinoExecutionContextHolder.BUSINESS_DATE);
		if (businessDate == null) {
			Tenant tenant = tenantService.getDefaultTenant();
			if (tenant != null && tenant.getBusinessDate() != null) {
				businessDate = tenant.getBusinessDate();
				addToGlobalContext(BUSINESS_DATE, businessDate);
			} else {
				businessDate = new Date();
			}
		}

		return businessDate;
	}

	@Override
	public Date getBusinessDateWithSystemTime() {

		Date businessDate = getBusinessDate();
		Calendar businessCalendar = Calendar.getInstance();
		businessCalendar.setTimeInMillis(businessDate.getTime());
		Calendar calendar = Calendar.getInstance();
		calendar.set(businessCalendar.get(Calendar.YEAR),
				businessCalendar.get(Calendar.MONTH),
				businessCalendar.get(Calendar.DATE));
		return calendar.getTime();
	}

	@Override
	public UserInfo getLoggedInUserDetails() {
		UserInfo userInfo = (UserInfo) NeutrinoExecutionContextSupport
				.getFromGlobalContext(NeutrinoExecutionContextHolder.USERINFO);
		return userInfo;

	}

	@Override
	public Long getLoggedInBranchId() {
		UserInfo userInfo = getLoggedInUserDetails();
		if (notNull(userInfo)) {
			return userInfo.getLoggedInBranch().getId();
		}
		return null;
	}

	@Override
	public Long getTenantId() {
		BaseTenant baseTenant = getTenant();
		if (notNull(baseTenant)) {
			return baseTenant.getId();
		}
		return null;
	}

	@Override
	public User getUser() {
		User user = null;
		UserInfo userInfo = getLoggedInUserDetails();
		if (notNull(userInfo)) {
			user = userInfo.getUserReference();
		}

		return user;
	}

	@Override
	public Locale getDefaultLocale() {
		return new Locale(getTenant().getLocale());
	}

	@Override
	public BaseTenant getTenant() {
		BaseTenant baseTenant = (BaseTenant) NeutrinoExecutionContextSupport
				.getFromGlobalContext(NeutrinoExecutionContextHolder.TENANT);
		if(ValidatorUtils.isNull(baseTenant)){
			baseTenant=tenantService.getDefaultTenant();
			addToGlobalContext(NeutrinoExecutionContextHolder.TENANT, baseTenant);
		}
		return baseTenant;
	}

	@Override
	public Currency getBaseCurrency() {
		Currency currency = (Currency) NeutrinoExecutionContextSupport
				.getFromGlobalContext(NeutrinoExecutionContextHolder.CURRENCY);
		if (currency == null) {
			throw ExceptionBuilder
					.getInstance(SystemException.class,
							BASE_CURRENCY_EXCEPTION,
							"Base Currency is not available in system.")
					.setSeverity(
							ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue())
					.build();
		}
		return currency;
	}

}
