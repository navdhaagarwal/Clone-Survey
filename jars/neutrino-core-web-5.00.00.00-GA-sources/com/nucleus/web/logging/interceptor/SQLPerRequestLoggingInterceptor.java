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
package com.nucleus.web.logging.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.nucleus.core.datasource.logging.DatasourceLoggingUtils;
import com.nucleus.core.datasource.logging.QueryInfoPerRequestHolder;
import com.nucleus.logging.BaseLoggers;

public class SQLPerRequestLoggingInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        QueryInfoPerRequestHolder.setHttpRequestActive();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

        try {
            DatasourceLoggingUtils.logQueriesForRequestAndClearContext(request.getRequestURI(), true);
        } catch (Exception e) {
            // don't let it to break business logic
            BaseLoggers.exceptionLogger.error("Error in logging sql queries {}", e.getMessage());
        }

    }
}
