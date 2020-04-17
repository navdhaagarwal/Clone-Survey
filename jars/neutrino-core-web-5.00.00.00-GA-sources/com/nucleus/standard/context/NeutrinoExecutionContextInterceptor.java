package com.nucleus.standard.context;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static com.nucleus.web.security.AesUtil.PASS_PHRASE;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.nucleus.finnone.pro.lmsbase.domainobject.Tenant;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.currency.Currency;
import com.nucleus.finnone.pro.general.vo.CurrencyVO;
import com.nucleus.finnone.pro.lmsbase.serviceinterface.ITenantService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.money.MoneyService;
import com.nucleus.user.UserInfo;



public class NeutrinoExecutionContextInterceptor extends HandlerInterceptorAdapter {
	@Inject
	@Named("neutrinoExecutionContextInitializationHelper")
	NeutrinoExecutionContextInitializationHelper neutrinoExecutionContextInitializationHelper;
	
	@Inject
	@Named("neutrinoExecutionContextHolder")
	INeutrinoExecutionContextHolder neutrinoExecutionContextHolder;
	
	@Inject
	@Named("tenantService")
	ITenantService tenantService;
	
	@Inject
	@Named("moneyService")
	private MoneyService moneyService;
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		try {
			
			neutrinoExecutionContextHolder.clearGlobalContext();
			neutrinoExecutionContextHolder.clearLocalContext();
			neutrinoExecutionContextInitializationHelper.initializeContextOnDemand();
			
			String trustedSourceName = (String) request.getAttribute("trustedSourceName");
 			if(null!=trustedSourceName) {
			neutrinoExecutionContextHolder.addToLocalContext("trustedSourceName",
					trustedSourceName);
			}
			
			setPassPhrase(request);
			
	    	HttpSession session = request.getSession();
	    	BaseLoggers.flowLogger.debug("Path Info : " + request.getPathInfo());
					
			if(SecurityContextHolder.getContext().getAuthentication() != null &&getUserDetails()!=null){		
				
				if (session.getAttribute("user_profile") == null || session.getAttribute("user_tenant") == null){
					Currency baseCurrency = null;
					Map<String, CurrencyVO> currencyMap = new HashMap<>();
					baseCurrency = getBaseCurrencyAndPopulateCurrencyMap(currencyMap);
					int currencyPrecision = baseCurrency==null ? -1 : baseCurrency.getDecimalPlaces();

					session.setAttribute("amountFormatWithoutPrecision",tenantService.getAmountFormatWithoutPrecision());
					session.setAttribute("currencyMap",currencyMap);
					session.setAttribute("baseCurrency",baseCurrency);
					session.setAttribute("curr_precision",currencyPrecision);
					session.setAttribute("user_date_format", getUserDateFormat());
				}
	    					
	    	}
			return true;
		}

		catch (Exception e) {
			BaseLoggers.exceptionLogger.error(e.getMessage(), e);
			return false;
		}

	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}
	private void setPassPhrase(HttpServletRequest request)
	{
		 if (request.getSession().getAttribute(PASS_PHRASE) == null) {
	          request.getSession().setAttribute(PASS_PHRASE, RandomStringUtils.randomNumeric(8));
	        }
	}
	
	private UserInfo getUserDetails() {
		UserInfo userInfo = null;
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext != null) {
			Object principal = securityContext.getAuthentication().getPrincipal();
			if (UserInfo.class.isAssignableFrom(principal.getClass()))
				userInfo = (UserInfo) principal;
		}

		return userInfo;
	}

	private Currency getBaseCurrencyAndPopulateCurrencyMap(Map<String, CurrencyVO> currencyMap){
		Currency baseCurrency = null;
		List<Currency> currencyList = moneyService.retrieveAllActiveCurrencies();
		//int currencyPrecision = -1;
		if (hasElements(currencyList)) {
			for (Currency currency : currencyList) {
				CurrencyVO currencyVO = new CurrencyVO();
				currencyVO.populate(currency);
				currencyMap.put(currencyVO.getIsoCode(), currencyVO);

				if (notNull(currency.getIsBaseCurrency()) && currency.getIsBaseCurrency()) {
					baseCurrency = currency;
					currencyMap.put("tenant_currency", currencyVO);
					//currencyPrecision = currencyVO.getDecimalPlaces();
				}

			}
		}
		return baseCurrency;
	}
	
	private String getUserDateFormat() {
		Map<String, ConfigurationVO> preferences = getUserDetails().getUserPreferences();
		String userDateFormat = null;

		/*
		 * Get user preferred date format .. it is assumed that only day of the
		 * month, month and year component will be provided
		 * Preference will be given to user's preference .. else tenant level
		 * settings will be used
		 */
		ConfigurationVO preferredDateFormat = preferences.get("config.date.formats");
		if (preferredDateFormat != null && preferredDateFormat.getPropertyValue() != null && !"".equals(preferredDateFormat.getPropertyValue())) {
			userDateFormat = preferredDateFormat.getPropertyValue();
		} else {
			userDateFormat = tenantService.getDefaultTenant().getDateFormat();
		}
		
		return userDateFormat;
	}
}
