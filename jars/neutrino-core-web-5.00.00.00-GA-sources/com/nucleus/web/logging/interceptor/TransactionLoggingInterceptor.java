package com.nucleus.web.logging.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import com.nucleus.core.web.conversation.ConversationalSessionAttributeStore;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.csrf.CSRFRequestDataValueProcessor;

/**
* @author Nucleus Software Exports Limited
*
* Feature--I:
*
* This Intercepter is processed after all Spring Security Filters(As Written in Web.xml)
* This Intercepter will run only once per every request going to the server
* This Intercepter adds(During PREHANDLE) a UniqueIdentification number(UUID.hashcode) for every request ,which is propagated throughout the application Implicitly,Using MDC
* This Intercepter also adds LOGGEDIN_USER_NAME to MDC map so that it can be used where ever Required
* This UUID can be used for tracking what user is doing,This gets printed in Every log ,so we can track what user is doing by doing a grep on UUID
*
* Feature--II:
* This Intercepter also logs UserName and what links he is clicking....i.e tracking what the present user is doing so that this can be further analysed for improvement
*
* This clears memory items which are inserted in MDC while POST_HANDLE
*/

public class TransactionLoggingInterceptor implements HandlerInterceptor {

    public static final String UUID_IDENTIFIER           = "UUID";
    public static final int    UUID_IDENTIFIER_LENGTH    = 15;
    public static final String LOGGED_IN_USER_IDENTIFIER = "LOGGEDIN_USER_NAME";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        insertIntoMDC(request);

        // check if conversational id is present in each request.
        String conversationId = CSRFRequestDataValueProcessor.getConversationId(request);
        if (StringUtils.isBlank(conversationId)) {
            BaseLoggers.conversationalLogger.info("No conversationId found in request [{}].", request.getRequestURL());
        } else {
            BaseLoggers.conversationalLogger.debug("Conversation Id [{}] found in request [{}]", conversationId,
                    request.getRequestURL());
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        // To Remove inserted variables form memory after completion of requirement so that no memory leak occurs
        clearMDC();
        // add conversation id to all redirects(eg.-return "redirect:app/......")
        if(modelAndView==null)
        {
        	return;
        }
        if ((modelAndView.getView() != null && modelAndView.getView() instanceof RedirectView)
                || (modelAndView.getViewName() != null && modelAndView.getViewName().startsWith(
                        UrlBasedViewResolver.REDIRECT_URL_PREFIX))) {
			String conversationId = CSRFRequestDataValueProcessor.getConversationId(request);
            if (conversationId != null) {
                modelAndView.addObject(ConversationalSessionAttributeStore.CID_FIELD, conversationId);
            }
        }

    }

    @Override
   public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // To Remove inserted variables form memory after completion of requirement so that no memory leak occurs
        clearMDC();
    }

    private void clearMDC() {
        // Clearing Memory So as to Prevent Memory Leaks
        MDC.remove(UUID_IDENTIFIER);
        MDC.remove(LOGGED_IN_USER_IDENTIFIER);
    }

    private void insertIntoMDC(HttpServletRequest httpServletRequest) {
        String transactionLoggingUUID = "TXN-" + RandomStringUtils.randomNumeric(UUID_IDENTIFIER_LENGTH);
        // Inserting the Unique Id into MDC map,MDC takes care of thread safety and child thread inheritance
        MDC.put(UUID_IDENTIFIER, transactionLoggingUUID);
        String loggedInUser = null;
        // For Extracting UserName out of the request(Same code as getUserName() Function in BaseController
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
                                                                && SecurityContextHolder.getContext().getAuthentication().getName() != null) {
                    loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
                    BaseLoggers.accessLogger.info(MDC.get(UUID_IDENTIFIER) + "    "
                            + SecurityContextHolder.getContext().getAuthentication().getName() + " is accessing "
                            + httpServletRequest.getRequestURI());
        }

        // Inserts LOGGEDIN_USER_NAME,so that it can be used any where required
        MDC.put(LOGGED_IN_USER_IDENTIFIER, loggedInUser);
    }

}
