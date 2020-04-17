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
package com.nucleus.web.formatter;

import java.text.ParseException;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.format.Formatter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nucleus.core.money.entity.Money;
import com.nucleus.core.money.utils.MoneyUtils;
import com.nucleus.money.MoneyService;
import com.nucleus.user.UserService;

/**
 * @author Nucleus Software Exports Limited
 * Formatter class to convert between String and Money type.
 */
@Named("moneyFormatter")
public class MoneyFormatter implements Formatter<Money> {

    @Inject
    @Named("userService")
    protected UserService  userService;
    @Inject
    @Named("moneyService")
    protected MoneyService moneyService;

    @Override
    public String print(Money money, Locale locale) {

        Locale userLocale = userService.getUserLocale();

        return MoneyUtils.formatMoneyByLocale(money, userLocale);

    }

    @Override
    public Money parse(String text, Locale locale) throws ParseException {
    	
    	ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    	HttpServletRequest req = sra.getRequest();
    	
    	if(req.getParameter("money_appId") != null && !req.getParameter("money_appId").isEmpty()){
    		String moneyAppIdStr = req.getParameter("money_appId");
    		if(moneyAppIdStr.indexOf(",")!=-1){
    			moneyAppIdStr = moneyAppIdStr.split(",")[0];
    		}
    		return moneyService.parseMoney(moneyAppIdStr+"~"+text, locale);
    	}else if(req.getParameter("appId") != null){
    		return moneyService.parseMoney((String)req.getParameter("appId")+"~"+text, locale);
    	}else{	
    		return moneyService.parseMoney(text, locale);
    	}

        
    }

}