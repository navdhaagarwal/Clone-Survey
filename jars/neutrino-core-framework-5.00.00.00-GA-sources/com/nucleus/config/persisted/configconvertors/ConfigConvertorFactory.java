package com.nucleus.config.persisted.configconvertors;

import java.util.HashMap;
import java.util.Map;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.config.persisted.vo.ValueType;
import com.nucleus.core.exceptions.InvalidDataException;

public class ConfigConvertorFactory {

    static Map<ValueType, Object> modelMap = new HashMap<ValueType, Object>();

    /**
     * Provide the <b>IConfigConvertor</b> from <b>ConfigurationVO</b>.
     *
     * @param configurationVO the configurationVO
     * @return the IConfigConvertor 
     */
    public static IConfigConvertor getConvertorFromVO(ConfigurationVO configurationVO) {
        ValueType valueType = configurationVO.getValueType();
        if (modelMap.get(valueType) == null) {
            getModelMap(valueType);
        }
        return (IConfigConvertor) modelMap.get(valueType);
    }

    /**
     * Provide the <b>IConfigConvertor</b> from <b>Configuration</b>.
     *
     * @param configuration the configuration
     * @return the IConfigConvertor 
     */

    public static IConfigConvertor getConvertorFromConfiguration(Configuration configuration) {
        ValueType valueType = configuration.getValueType();
        if (modelMap.get(valueType) == null) {
            getModelMap(valueType);
        }
        return (IConfigConvertor) modelMap.get(valueType);
    }

    /**
     * Creates the model map on the basis of value type 
     *
     * @param valueType the ValueType
     */
    public static void getModelMap(ValueType valueType) {
        switch (valueType) {
            case NORMAL_TEXT:
                modelMap.put(valueType, new StringConfigConvertor());
                break;

            case DATE:
            case TIME:
                modelMap.put(valueType, new DateConfigConvertor());
                break;

            case DATE_RANGE:
            case TIME_RANGE:
                modelMap.put(valueType, new DateRangeConfigConvertor());
                break;

            case DAY_OF_WEEK:
                modelMap.put(valueType, new DayOfWeekConfigConvertor());
                break;

            case DAYS_OF_WEEK_RANGE:
                modelMap.put(valueType, new DayOfWeekRangeConfigConvertor());
                break;
                
            case BOOLEAN_VALUE:
                modelMap.put(valueType, new BooleanConfigConvertor());
                break;
                
            case DASHBOARD:
                modelMap.put(valueType, new DashboardConfigConvertor());
                break;    

            default:
                throw new InvalidDataException("No matching value type defined");
        }
    }
}
