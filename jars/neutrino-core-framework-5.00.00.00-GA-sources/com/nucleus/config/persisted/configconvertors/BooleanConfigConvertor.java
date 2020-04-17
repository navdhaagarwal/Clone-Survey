package com.nucleus.config.persisted.configconvertors;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.vo.ConfigurationVO;

public class BooleanConfigConvertor extends AbstractConfigConvertor {

    @Override
    protected String getCurrentPropertyValueAsString(ConfigurationVO configurationVO) {
        if(configurationVO.isConfigurable()==null){
            return "false"; 
        }
        boolean configurable = configurationVO.isConfigurable();
        if (configurable) {
            return "true";
        }
        return "false";
    }

    @Override
    protected void transferValueFromConfigurationToVO(Configuration configuration, ConfigurationVO configurationVO) {
        String configureValue = configuration.getPropertyValue();
        boolean configurable = false;
        if (configureValue.equalsIgnoreCase("true")) {
            configurable = true;
        }
        configurationVO.setConfigurable(configurable);
    }

}
