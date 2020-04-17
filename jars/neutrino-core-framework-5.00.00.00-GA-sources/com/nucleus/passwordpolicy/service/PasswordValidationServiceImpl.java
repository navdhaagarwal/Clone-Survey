package com.nucleus.passwordpolicy.service;


import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.passwordpolicy.PasswordCreationPolicy;
import com.nucleus.service.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Locale;

@Named("passwordValidationService")
public class PasswordValidationServiceImpl extends BaseServiceImpl implements PasswordValidationService{

    @Autowired
    protected ApplicationContext applicationContext;

    @Inject
    @Named("messageSource")
    protected MessageSource messageSource;

    @Inject
    @Named("configurationService")
    protected ConfigurationService configurationService;




    public Locale getLocale() {
        return configurationService.getSystemLocale();
    }

    public String getMessageDescription(Message message, Locale locale) {
        Locale localeupdated = null;
        if (locale == null) {
            localeupdated = configurationService.getSystemLocale();
        } else {
            localeupdated = locale;
        }
        return messageSource.getMessage(message.getI18nCode(), message.getMessageArguments(), message.getI18nCode(), localeupdated);

    }




    @Override
    public PasswordCreationPolicy getPasswordPolicyByName(String name) {
        NamedQueryExecutor<PasswordCreationPolicy> passwdPolicyFromName = new NamedQueryExecutor<PasswordCreationPolicy>(
                "PasswordCreationPolicy.getPasswordPolicyByName").addParameter("name", name);
        return entityDao.executeQueryForSingleValue(passwdPolicyFromName);
    }

    @Override
    public Configuration getConfigurationFromPropertyKey(String propertykey){
        NamedQueryExecutor<Configuration> conf = new NamedQueryExecutor<Configuration>(
                "PasswordCreationPolicy.getConfigurationFromPropertyKey").addParameter("key", propertykey);
        return entityDao.executeQueryForSingleValue(conf);
    }

    @Override
    public List<PasswordCreationPolicy> getEnabledPasswordPolicy(){
        NamedQueryExecutor<PasswordCreationPolicy> passwdPolicy = new NamedQueryExecutor<PasswordCreationPolicy>(
                "PasswordCreationPolicy.getEnabledPasswordPolicy").addParameter("isEnabled",Boolean.TRUE);
        return entityDao.executeQuery(passwdPolicy);
    }
}
