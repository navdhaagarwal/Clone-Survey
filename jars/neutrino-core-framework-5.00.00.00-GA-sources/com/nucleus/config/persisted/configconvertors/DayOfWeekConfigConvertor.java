package com.nucleus.config.persisted.configconvertors;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.vo.ConfigurationVO;

public class DayOfWeekConfigConvertor extends AbstractConfigConvertor {

    @Override
    protected void transferValueFromConfigurationToVO(Configuration configuration, ConfigurationVO configurationVO) {
        configurationVO.setDay(configuration.getPropertyValue());

    }

    @Override
    protected String getCurrentPropertyValueAsString(ConfigurationVO configurationVO) {
        String dayWeek = "";
        if (configurationVO.getDay() != null) {
            return configurationVO.getDay();
        }
        return dayWeek;
    }

}
