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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import com.nucleus.logging.BaseLoggers;

/**
 * Registry bean that adds {@link WebDataBinder} map to application context. It also allows new data binders to be added into registry
 * @author Nucleus Software Exports Limited
 */
public class WebDataBinderRegistry implements ServletContextAware {

    private Map<String, WebDataBinder<?>> webDataBinderContainer = new LinkedHashMap<String, WebDataBinder<?>>();

    @Override
    public void setServletContext(ServletContext servletContext) {
        BaseLoggers.webLogger
                .info("Added WebDataBinderContainer into ServletContext attribute against key: neutrinoDataBinder");
        servletContext.setAttribute("neutrinoDataBinder", webDataBinderContainer);
    }

    public void registerBinder(String key, WebDataBinder<?> webDataBinder) {
        WebDataBinder<?> existingBinder = webDataBinderContainer.get(key);
        if (existingBinder != null) {
            BaseLoggers.webLogger.info("Overriding already existing WebDataBinder with key: " + key + ".. Old Object: "
                    + existingBinder + " New Object: " + webDataBinder);
        } else {
            BaseLoggers.webLogger.info("Adding WebDataBinder with key :" + key + " and object: " + webDataBinder);
        }
        webDataBinderContainer.put(key, webDataBinder);
    }

}