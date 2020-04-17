package com.nucleus.config.persisted.configconvertors;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.validation.util.NeutrinoValidator;

public abstract class AbstractConfigConvertor implements IConfigConvertor {

    @Override
    public Configuration toConfiguration(ConfigurationVO configurationVO) {
        NeutrinoValidator.isTrue(configurationVO.getValueType() != null, "Property Value type cannot be blank");
        Configuration configuration = new Configuration();
        configuration.setPropertyKey(configurationVO.getPropertyKey());
        configuration.setValueType(configurationVO.getValueType());
        configuration.setPropertyValue(getCurrentPropertyValueAsString(configurationVO));
        configuration.setUserModifiable(configurationVO.isUserModifiable());
        return configuration;
    }

    @Override
    public ConfigurationVO fromConfiguration(Configuration configuration) {
        if (configuration == null) {
            return null;
        }
        ConfigurationVO configurationVO = new ConfigurationVO();
        configurationVO.setId(configuration.getId());
        configurationVO.setPropertyKey(configuration.getPropertyKey());
        configurationVO.setPropertyValue(configuration.getPropertyValue());
        configurationVO.setValueType(configuration.getValueType());
        configurationVO.setUserModifiable(configuration.isUserModifiable());
        transferValueFromConfigurationToVO(configuration, configurationVO);
        return configurationVO;
    }

    @Override
    public boolean isConfigurationChanged(ConfigurationVO configurationVO) {
        String str = "";
        if (configurationVO.getPropertyValue() != null) {
            str = configurationVO.getPropertyValue();
        }
        if (getCurrentPropertyValueAsString(configurationVO).equalsIgnoreCase(str)) {
            return false;
        }
        return true;
    }

    abstract protected String getCurrentPropertyValueAsString(ConfigurationVO configurationVO);

    abstract protected void transferValueFromConfigurationToVO(Configuration configuration, ConfigurationVO configurationVO);

}
