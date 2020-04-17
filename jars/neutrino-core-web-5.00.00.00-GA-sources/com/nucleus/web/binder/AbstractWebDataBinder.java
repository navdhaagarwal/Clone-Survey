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
package com.nucleus.web.binder;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * @author Nucleus Software Exports Limited
 */
@Transactional
public abstract class AbstractWebDataBinder<T> implements WebDataBinder<T> {

    public WebApplicationContext getWebApplicationContext() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = sra.getRequest();
        return RequestContextUtils.findWebApplicationContext(req);
    }

    public Object getBean(String beanName) {
        return getWebApplicationContext().getBean(beanName);
    }

    public T getData(Object obj)
    {
        return null;
    }
    
    /*public DateTime getCurrentDate() {
        BusinessDateService businessDateService = (BusinessDateService) getWebApplicationContext().getBean(
                "businessDateService");
        if (null != businessDateService) {
            BusinessDate businessDate = businessDateService.findBusinessDate();
            DateTime currentDate = null;
            if (null != businessDate && null != businessDate.getSelectedBusinessDate()) {
                currentDate = businessDate.getSelectedBusinessDate();
            } else {
                currentDate = DateUtils.getCurrentUTCTime();
            }
            return currentDate;
        } else
            return DateUtils.getCurrentUTCTime();

    }*/
    
}
