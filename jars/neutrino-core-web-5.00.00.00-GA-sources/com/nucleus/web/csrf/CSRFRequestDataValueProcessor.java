package com.nucleus.web.csrf;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.RequestDataValueProcessor;

import com.nucleus.core.web.conversation.ConversationalSessionAttributeStore;

/**
* A <code>RequestDataValueProcessor</code> that pushes a hidden field with a CSRF token into forms.
* This process implements the {@link #getExtraHiddenFields(HttpServletRequest)} method to push the
* CSRF token obtained from {@link CSRFTokenManager}. To register this processor to automatically process all
* Spring based forms register it as a Spring bean named 'requestDataValueProcessor' as shown below:
* <pre>
* &lt;bean name="requestDataValueProcessor" class="com.nucleus.web.csrf.CSRFRequestDataValueProcessor"/&gt;
* </pre>
* @author Nucleus Software.
* Code adapted from Eyallupu's blog suggestion to handle csrf.
*/
@Named("requestDataValueProcessor")
public class CSRFRequestDataValueProcessor implements RequestDataValueProcessor {

    @Override
    public String processAction(HttpServletRequest request, String action, String httpMethod) {
        return action;
    }

    @Override
    public String processFormFieldValue(HttpServletRequest request, String name, String value, String type) {
        return value;
    }

    @Override
    public Map<String, String> getExtraHiddenFields(HttpServletRequest request) {
        Map<String, String> hiddenFields = new HashMap<String, String>();
        hiddenFields.put(CSRFTokenManager.CSRF_PARAM_NAME, CSRFTokenManager.getTokenForSession(request));

        String conversationId = getConversationId(request);
        if (StringUtils.isNoneBlank(conversationId)) {
        	conversationId = conversationId.replaceAll("[^\\w\\s\\-_]", "");       
            hiddenFields.put(ConversationalSessionAttributeStore.CID_FIELD, conversationId);
        }

        return hiddenFields;
    }

    @Override
    public String processUrl(HttpServletRequest request, String url) {
        return url;
    }

    public static String getConversationId(HttpServletRequest request) {

        String cid = request.getParameter(ConversationalSessionAttributeStore.CID_FIELD);
        if (StringUtils.isBlank(cid)) {
            cid = (String) request.getHeader(ConversationalSessionAttributeStore.CID_FIELD);
        }
        if (StringUtils.isBlank(cid)) {
            cid = (String) request.getAttribute(ConversationalSessionAttributeStore.CID_FIELD);
        }
        return cid;
    }

}
