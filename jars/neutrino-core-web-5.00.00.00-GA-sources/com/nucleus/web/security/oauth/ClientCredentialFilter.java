package com.nucleus.web.security.oauth;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.jwt.util.JotUtil;
import com.nucleus.core.misc.util.PasswordEncryptorUtil;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.oauth.TrustedSourceInfo;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.security.oauth.service.TrustedSourceService;
import com.nucleus.web.security.AesUtil;
import com.nucleus.web.util.CustomMatcherGenericFilter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gajendra.jatav on 4/18/2019.
 */
public class ClientCredentialFilter extends CustomMatcherGenericFilter {

    private Boolean isApiManagerEnabled = null;

    @Inject
    @Named("clientDetails")
    private TrustedSourceService trustedSourceService;

    @PostConstruct
    public void init(){
        CoreUtility coreUtility = (NeutrinoSpringAppContextUtil.getBeanByName("coreUtility", CoreUtility.class));
        if (coreUtility == null) {
            throw new SystemException("Bean Not Found for CORE UTILITY");
        }
        this.isApiManagerEnabled = coreUtility.isApiManagerEnabled();
    }

    @Override
    public void filterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean sendError = false;
        try {
            String headerVal = ((HttpServletRequest) request).getHeader("payload");
            sendError = headerVal == null;
            Map headerMap = null;
            if (!sendError) {
                Map<String, String> encryptedHeaders = new HashMap<>();
                encryptedHeaders.put("payload", headerVal);
                headerMap = JotUtil.decrypt("encryptedHeaders", encryptedHeaders, Map.class);
                sendError = headerMap == null;
                if (!sendError) {
                    extractAndSetAllAttributesInRequest(request, headerMap);
                    chain.doFilter(request, response);
                }
            }
        } catch (Exception e) {
            BaseLoggers.flowLogger.debug("Error while calling service secured with client id and secret ",e);
            sendError = true;
        }

        if(sendError){
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.write("{ \"status\": \"UNAUTHORIZED\", \"message\": \"Invalid client id or client secret.\" }");
        }
    }

    private void extractAndSetAllAttributesInRequest(ServletRequest request, Map headerMap){
        setAttributeInRequest(request, headerMap, "username");
        setAttributeInRequest(request, headerMap, "otp");
        setAttributeInRequest(request, headerMap, "question1");
        setAttributeInRequest(request, headerMap, "question2");
        setAttributeInRequest(request, headerMap, "answer1");
        setAttributeInRequest(request, headerMap, "answer2");
        setAttributeInRequest(request, headerMap, "newPassword");
        setAttributeInRequest(request, headerMap, "resetToken");
        setAttributeInRequest(request, headerMap, "passPhrase");
    }

    private void setAttributeInRequest(ServletRequest request, Map headerMap, String attributeName){
        Object attributeValue = headerMap.get(attributeName);
        if (attributeValue != null) {
            request.setAttribute(attributeName, attributeValue);
        }
    }
}
