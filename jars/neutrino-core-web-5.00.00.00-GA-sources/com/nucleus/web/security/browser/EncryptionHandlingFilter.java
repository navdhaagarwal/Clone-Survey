package com.nucleus.web.security.browser;

import com.nucleus.web.security.NeutrinoRequestParamHolder;
import com.nucleus.web.security.NeutrinoUrlValidatorFilter;
import com.nucleus.web.security.NeutrinoUrlValidatorFilterConfig;
import com.nucleus.web.security.servlet.api.UrlExcusionUtil;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by gajendra.jatav on 2/7/2020.
 */
public class EncryptionHandlingFilter extends OncePerRequestFilter {

    @Inject
    @Named("neutrinoUrlValidatorFilterConfig")
    private NeutrinoUrlValidatorFilterConfig neutrinoUrlValidatorFilterConfig;

    private boolean isParamEncryptionEnabled = true;

    private List<AntPathRequestMatcher> excludedPatterns;

    @PostConstruct
    public void init() {
        this.isParamEncryptionEnabled = neutrinoUrlValidatorFilterConfig.getParamEncryptionEnabled();
        excludedPatterns = UrlExcusionUtil.parseAndCreateRegex("", NeutrinoUrlValidatorFilter.class.getSimpleName());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isDecryptionRequired(request)
                && (isSecureUri(request) || isEncrypted(request))) {
                request = new NeutrinoRequestParamHolder(request);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isEncrypted(HttpServletRequest request) {
        return request.getParameter(NeutrinoRequestParamHolder.REQUEST_DATA_PARAM) != null
                || request.getHeader(NeutrinoRequestParamHolder.ENCODED_JSON_HEADER)!=null;
    }

    private boolean isSecureUri(HttpServletRequest request) {
        return !UrlExcusionUtil.isExcludedUri(request, excludedPatterns);
    }

    private boolean isDecryptionRequired(HttpServletRequest request) {
        return isParamEncryptionEnabled &&
                "POST".equalsIgnoreCase(request.getMethod());
    }
}
