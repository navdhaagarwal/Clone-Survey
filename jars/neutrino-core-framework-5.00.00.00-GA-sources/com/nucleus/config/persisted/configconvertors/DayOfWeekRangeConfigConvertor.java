package com.nucleus.config.persisted.configconvertors;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.vo.ConfigurationVO;

public class DayOfWeekRangeConfigConvertor extends AbstractConfigConvertor {

    /** The Constant RANGE_SEPARATOR. */
    static final String RANGE_SEPARATOR = "-";

    @Override
    protected void transferValueFromConfigurationToVO(Configuration configuration, ConfigurationVO configurationVO) {
        String[] valueArr = ConfigConvertorUtils.splitValue(configuration.getPropertyValue(), RANGE_SEPARATOR);
        String fromDay = valueArr[0];
        String toDay = valueArr[1];
        configurationVO.setFromDay(fromDay);
        configurationVO.setToDay(toDay);
    }

    @Override
    public boolean isConfigurationChanged(ConfigurationVO configurationVO) {
        String[] valueArr = ConfigConvertorUtils.splitValue(configurationVO.getPropertyValue(), RANGE_SEPARATOR);
        String fromOldDay = valueArr[0];
        String toOldDay = valueArr[1];

        String newValue[] = ConfigConvertorUtils.splitValue(getCurrentPropertyValueAsString(configurationVO),
                RANGE_SEPARATOR);

        String fromDay = newValue[0];
        String toDay = newValue[1];
        if (fromOldDay.equalsIgnoreCase(fromDay) && toOldDay.equalsIgnoreCase(toDay)) {
            return false;
        }
        return true;
    }

    @Override
    protected String getCurrentPropertyValueAsString(ConfigurationVO configurationVO) {
        String dayRange = "";
        if (configurationVO.getFromDay() != null && configurationVO.getToDay() != null) {
            String fromDay = configurationVO.getFromDay();
            String toDay = configurationVO.getToDay();
            return fromDay + RANGE_SEPARATOR + toDay;
        }

        return dayRange;
    }

}
