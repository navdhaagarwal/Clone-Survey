package com.nucleus.web.common.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.money.utils.MoneyUtils;
import com.nucleus.currency.Currency;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.finnone.pro.general.vo.CurrencyVO;
import com.nucleus.money.MoneyService;

@Transactional
@Controller
@SessionAttributes("money")
@RequestMapping(value = "/money")
public class MoneyController extends BaseController {

    @Inject
    @Named("moneyService")
    MoneyService                   moneyService;

    @Inject
    @Named("configurationService")
    protected ConfigurationService configurationService;

    /**
     * This method is being used for actual formatting of the amount entered for the currency fields.
     * @param currencyId
     * @param previousElementId
     * @param amount
     * @return
     */
    @RequestMapping(value="/formatCurrency")
    public @ResponseBody
    String formatMoney(@RequestParam("amount") String amount,Locale locale) {
        String currencyOut = "";

        // Checking if money value is not empty
        if ( StringUtils.isBlank(amount)) {
        	return currencyOut;
        }
        String[] amountVar = amount.trim().split(MoneyUtils.MONEY_DELIMITER);

        // Proceed if money has currency as well as value
        if (amountVar.length == 2) {
             currencyOut = moneyService.formatMoneyAmount(amountVar[1], null, amountVar[0]);
        }
        return currencyOut;
    }
    
	@RequestMapping(value = "/getCurrencyDetails")
	public @ResponseBody Map<String, CurrencyVO> getCurrencyDetails() {
		Map<String, CurrencyVO> currencyMap = new HashMap<>();
		List<Currency> currencyList = moneyService.retrieveAllActiveCurrencies();
		if (ValidatorUtils.hasElements(currencyList)) {
			for (Currency currency : currencyList) {
				CurrencyVO currencyVO = new CurrencyVO();
				currencyVO.populate(currency);
				currencyMap.put(currencyVO.getIsoCode(), currencyVO);
				if (ValidatorUtils.notNull(currency.getIsBaseCurrency()) && currency.getIsBaseCurrency()) {
					currencyMap.put("tenant_currency", currencyVO);
				}
			}
		}
		return currencyMap;
	}
}
