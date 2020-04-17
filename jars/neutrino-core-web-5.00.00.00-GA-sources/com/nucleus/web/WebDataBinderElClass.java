package com.nucleus.web;

import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

public class WebDataBinderElClass {

    public static Object getWebDataBinderData(String key) {
        Object binderData = null;
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = sra.getRequest();
       
        Object sessionBinderData = req.getAttribute(key + "_neutrino_web_data_binder");
        ServletContext servletContext = RequestContextUtils.findWebApplicationContext(req).getServletContext();

        if (sessionBinderData == null) {
            binderData = ((java.util.Map<String, com.nucleus.web.binder.WebDataBinder<java.util.List<?>>>) servletContext
                    .getAttribute("neutrinoDataBinder")).get(key).getData();
            req.setAttribute(key + "_neutrino_web_data_binder", binderData);
            return binderData;
        }

        return sessionBinderData;

    }
    
    public static Object getWebDataBinderDataForId(String key, String id) {
        if (StringUtils.isEmpty(id)) {
            return Collections.emptyList();
        }
        Object binderData = null;
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = sra.getRequest();
       
        Object sessionBinderData = null;
        ServletContext servletContext = RequestContextUtils.findWebApplicationContext(req).getServletContext();

        if (sessionBinderData == null) {
            binderData = ((java.util.Map<String, com.nucleus.web.binder.AbstractWebDataBinder<java.util.List<?>>>) servletContext
                    .getAttribute("neutrinoDataBinder")).get(key).getData(Long.valueOf(id));
            req.setAttribute(key + "_neutrino_web_data_binder", binderData);
            return binderData;
        }

        return sessionBinderData;

    }
    public static Object getWebDataBinderDataForType(String key, String type) {
        if (StringUtils.isEmpty(type)) {
            return Collections.emptyList();
        }
        Object binderData = null;
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = sra.getRequest();
       
        Object sessionBinderData = null;
        ServletContext servletContext = RequestContextUtils.findWebApplicationContext(req).getServletContext();

        if (sessionBinderData == null) {
            binderData = ((java.util.Map<String, com.nucleus.web.binder.AbstractWebDataBinder<java.util.List<?>>>) servletContext
                    .getAttribute("neutrinoDataBinder")).get(key).getData(type);
            req.setAttribute(key + "_neutrino_web_data_binder", binderData);
            return binderData;
        }

        return sessionBinderData;

    }
}
