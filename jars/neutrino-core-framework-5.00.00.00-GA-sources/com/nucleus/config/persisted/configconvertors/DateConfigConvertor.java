package com.nucleus.config.persisted.configconvertors;

import java.util.Date;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.vo.ConfigurationVO;

public class DateConfigConvertor extends AbstractConfigConvertor {

    @Override
    protected void transferValueFromConfigurationToVO(Configuration configuration, ConfigurationVO configurationVO) {
        Date date = new Date(Long.valueOf(configuration.getPropertyValue()));
        configurationVO.setDate(date);
    }

    @Override
    protected String getCurrentPropertyValueAsString(ConfigurationVO configurationVO) {
        String dateString = "";
        if (configurationVO.getDate() != null) {
            return String.valueOf(configurationVO.getDate().getTime());
        }
        return dateString;

    }

}
