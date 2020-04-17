package com.nucleus.config.persisted.configconvertors;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.vo.ConfigurationVO;

public class StringConfigConvertor extends AbstractConfigConvertor {

    @Override
    protected void transferValueFromConfigurationToVO(Configuration configuration, ConfigurationVO configurationVO) {
        configurationVO.setText(configuration.getPropertyValue());
    }

    @Override
    protected String getCurrentPropertyValueAsString(ConfigurationVO configurationVO) {
        String str = "";
        if (configurationVO.getText() != null) {
            return configurationVO.getText();
        }

        return str;
    }

}
