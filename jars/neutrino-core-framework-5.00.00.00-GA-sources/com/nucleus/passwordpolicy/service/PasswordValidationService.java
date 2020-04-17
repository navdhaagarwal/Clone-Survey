package com.nucleus.passwordpolicy.service;


import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.passwordpolicy.PasswordCreationPolicy;

import java.util.List;
import java.util.Locale;

public interface PasswordValidationService {

    public Locale getLocale();
    public String getMessageDescription(Message message, Locale locale);
    public PasswordCreationPolicy getPasswordPolicyByName(String name);
    public Configuration getConfigurationFromPropertyKey(String propertykey);
    public List<PasswordCreationPolicy> getEnabledPasswordPolicy();
}
