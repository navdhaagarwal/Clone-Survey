package com.nucleus.security.core.session;

import com.nucleus.security.core.session.SessionModuleMapping;
import com.nucleus.user.UserInfo;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

public interface SessionModuleService {

    public Integer getActiveSessionCountForModule(String module);

    public boolean isAllowLoginForConcurrencyMode(UserInfo userInfo);

    public boolean isAllowLoginForConcurrencyMode(Authentication auth);

    public boolean checkForLoginFeasibility(UserInfo userInfo, String module);

    public void createSessionModuleMapping(AuthenticationSuccessEvent authenticationSuccessEvent);

    public void createSessionModuleMapping(String sessionId);

    public void deleteSessionModuleMapping(String sessionId);

    public void deleteAllSessionModuleMapping(String module);

    public Boolean isConcurrencySwichingEnabled();

    public Boolean isSsoEnabled();
}