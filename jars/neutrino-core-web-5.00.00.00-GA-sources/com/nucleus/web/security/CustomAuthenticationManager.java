package com.nucleus.web.security;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;

import com.nucleus.core.validation.util.NeutrinoValidator;

@SuppressWarnings("deprecation")
public class CustomAuthenticationManager extends ProviderManager implements ApplicationContextAware {

    private final String           DB_AUTHENTICATION   = "db";
    private final String           LDAP_AUTHENTICATION = "ldap";
    private final String           DUAL_AUTHENTICATION = "dual";

    private AuthenticationProvider dbAuthenticationProvider;
    private AuthenticationProvider ldapAuthenticationProvider;
    private AuthenticationProvider tokenBasedAuthenticationProvider;
    private String                 authenticationMode  = DUAL_AUTHENTICATION;
    
    /**
     * to enable/diable token based authentication 
     */
    private Boolean tokenBasedAuthenticationEnable = false;
    
    @Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil        systemSetupUtil;

    private ApplicationContext     applicationContext;

    public CustomAuthenticationManager(List<AuthenticationProvider> providers){
        super(providers);
    }
    
    @Override
    public List<AuthenticationProvider> getProviders() {
        List<AuthenticationProvider> providers = getDerivedProviders();
        //setProviders(providers);
        return providers;
    }

    private List<AuthenticationProvider> getDerivedProviders() {
        List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();

        if (systemSetupUtil.isSystemSetup()) {
            populateAuthenticationProviders(authenticationMode, providers);
        } else {
            providers.add(dbAuthenticationProvider);
        }
        return providers;
    }

    private void populateAuthenticationProviders(String authenticationMode, List<AuthenticationProvider> providers) {
        if (DB_AUTHENTICATION.equals(authenticationMode)) {
            providers.add(dbAuthenticationProvider);          
        } else if (LDAP_AUTHENTICATION.equals(authenticationMode)) {
            providers.add(ldapAuthenticationProvider);
        } else if (DUAL_AUTHENTICATION.equals(authenticationMode)) {
            providers.add(ldapAuthenticationProvider);
            providers.add(dbAuthenticationProvider);
        }
        if(tokenBasedAuthenticationEnable){
            NeutrinoValidator.notNull(tokenBasedAuthenticationProvider);
            providers.add(tokenBasedAuthenticationProvider);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //List<AuthenticationProvider> providers = getDerivedProviders();
        //setProviders(providers);
        super.afterPropertiesSet();
        setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(applicationContext));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setDbAuthenticationProvider(AuthenticationProvider dbAuthenticationProvider) {
        this.dbAuthenticationProvider = dbAuthenticationProvider;
    }

    public void setLdapAuthenticationProvider(AuthenticationProvider ldapAuthenticationProvider) {
        this.ldapAuthenticationProvider = ldapAuthenticationProvider;
    }

    public void setSystemSetupUtil(SystemSetupUtil systemSetupUtil) {
        this.systemSetupUtil = systemSetupUtil;
    }

    /**
     * @return the tokenBasedAuthenticationProvider
     */
    public AuthenticationProvider getTokenBasedAuthenticationProvider() {
        return tokenBasedAuthenticationProvider;
    }

    /**
     * @param tokenBasedAuthenticationProvider the tokenBasedAuthenticationProvider to set
     */
    public void setTokenBasedAuthenticationProvider(AuthenticationProvider tokenBasedAuthenticationProvider) {
        this.tokenBasedAuthenticationProvider = tokenBasedAuthenticationProvider;
    }


    public void setAuthenticationMode(String authenticationMode) {
        if (StringUtils.isNotBlank(authenticationMode)) {
            this.authenticationMode = authenticationMode;
        }
    }

    public Boolean getTokenBasedAuthenticationEnable() {
        return tokenBasedAuthenticationEnable;
    }

    public void setTokenBasedAuthenticationEnable(Boolean tokenBasedAuthenticationEnable) {
        this.tokenBasedAuthenticationEnable = tokenBasedAuthenticationEnable;
    }

}
