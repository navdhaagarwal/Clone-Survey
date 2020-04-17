package com.nucleus.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.ProductInformationLoader;

/*
 * As @PostConstruct Doesn't work with FactoryBean, we are using InitializingBean's afterPropertiesSet()
 */
public class EnvironmentConfigurationFallbackFactoryBean implements FactoryBean<Object>, InitializingBean {

    private String  fallbackProperty = null;
    private String  defaultValue     = null;
    private boolean propertySet      = false;

    @Override
    public Object getObject() throws Exception {
        return fallbackProperty;
    }

    @Override
    public Class<Object> getObjectType() {
        return Object.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setFallbackProperty(String fallbackProperty) {
        this.fallbackProperty = fallbackProperty;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (defaultValue == null) {
            throw new SystemException("'defaultValue' should be defined.");
        }
        if (ProductInformationLoader.productInfoExists()) {
            String productName;
            productName = ProductInformationLoader.getProductName();
            if (System.getProperty(productName + "." + fallbackProperty) != null) {
                this.fallbackProperty = System.getProperty(productName + "." + fallbackProperty);
                propertySet = true;
            }
        }
        if (!propertySet) {
            if (System.getProperty(fallbackProperty) != null) {
                this.fallbackProperty = System.getProperty(fallbackProperty);
            } else if (System.getenv(fallbackProperty) != null) {
                this.fallbackProperty = System.getenv(fallbackProperty);
            } else
                this.fallbackProperty = defaultValue;
        }

    }

}
