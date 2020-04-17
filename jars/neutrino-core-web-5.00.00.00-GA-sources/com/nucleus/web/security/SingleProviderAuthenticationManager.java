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

/**
 * 
 * @author Nucleus Software Exports Limited
 * TODO -> anmol.agarwal Add documentation to class
 * Authentication manager that will have only one authenticationManager.
 * tokenBasedAuthenticationProvider is added to support the token based authentication functionality in this Manager.
 */
@SuppressWarnings("deprecation")
public class SingleProviderAuthenticationManager extends ProviderManager implements ApplicationContextAware {

        private AuthenticationProvider authenticationProvider;
    private AuthenticationProvider tokenBasedAuthenticationProvider;
    
    /**
     * to enable/diable token based authentication 
     */
    private Boolean tokenBasedAuthenticationEnable = false;
    
    @Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil        systemSetupUtil;

    private ApplicationContext     applicationContext;

    public SingleProviderAuthenticationManager(List<AuthenticationProvider> providers){
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
        populateAuthenticationProviders(providers);
        return providers;
    }

    private void populateAuthenticationProviders(List<AuthenticationProvider> providers) {
            providers.add(getAuthenticationProvider());          
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

    public Boolean getTokenBasedAuthenticationEnable() {
        return tokenBasedAuthenticationEnable;
    }

    public void setTokenBasedAuthenticationEnable(Boolean tokenBasedAuthenticationEnable) {
        this.tokenBasedAuthenticationEnable = tokenBasedAuthenticationEnable;
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

}
