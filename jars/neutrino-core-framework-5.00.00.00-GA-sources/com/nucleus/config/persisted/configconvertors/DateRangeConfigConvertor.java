package com.nucleus.config.persisted.configconvertors;

import java.util.Date;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.vo.ConfigurationVO;

public class DateRangeConfigConvertor extends AbstractConfigConvertor {

    /** The Constant RANGE_SEPARATOR. */
    static final String RANGE_SEPARATOR = "-";

    @Override
    protected void transferValueFromConfigurationToVO(Configuration configuration, ConfigurationVO configurationVO) {
        String[] dateOrTimeRangeArr = ConfigConvertorUtils.splitValue(configuration.getPropertyValue(), RANGE_SEPARATOR);
        Date fromDate = new Date(Long.valueOf(dateOrTimeRangeArr[0]));
        Date toDate = new Date(Long.valueOf(dateOrTimeRangeArr[1]));
        configurationVO.setFromDate(fromDate);
        configurationVO.setToDate(toDate);
    }

    @Override
    public boolean isConfigurationChanged(ConfigurationVO configurationVO) {
        String newValue[] = ConfigConvertorUtils.splitValue(getCurrentPropertyValueAsString(configurationVO),
                RANGE_SEPARATOR);
        String oldValue[] = ConfigConvertorUtils.splitValue(configurationVO.getPropertyValue(), RANGE_SEPARATOR);
        String fromOldDate = oldValue[0];
        String toOldDate = oldValue[1];

        String fromDate = newValue[0];
        String toDate = newValue[1];
        if (fromOldDate.equalsIgnoreCase(fromDate) && toOldDate.equalsIgnoreCase(toDate)) {
            return false;
        }
        return true;
    }

    @Override
    protected String getCurrentPropertyValueAsString(ConfigurationVO configurationVO) {
        String dateRange = "";
        if (configurationVO.getFromDate() != null && configurationVO.getToDate() != null) {
            String fromDate = String.valueOf(configurationVO.getFromDate().getTime());
            String toDate = String.valueOf(configurationVO.getToDate().getTime());
            return fromDate + RANGE_SEPARATOR + toDate;
        }

        return dateRange;
    }

}
