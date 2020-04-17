package com.nucleus.regional.config.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.core.misc.util.ExceptionUtility;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.regional.RegionalData;
import com.nucleus.regional.RegionalEnabled;
import com.nucleus.regional.config.constant.RegionalFieldsAttributes;
import com.nucleus.regional.config.util.RegionalXMLReader;
import com.nucleus.regional.config.validator.IRegionalFieldValueValidator;
import com.nucleus.regional.metadata.RegionalMetaData;
import com.nucleus.regional.metadata.service.IRegionalMetaDataService;

@Named("regionalRequestResponseService")
public class RegionalRequestResponseService {

    @Inject
    @Named("regionalXMLReader")
    private RegionalXMLReader regionalXMLReader;

    @Inject
    @Named("regionalMetaDataService")
    private IRegionalMetaDataService regionalMetaDataService;

    @Inject
    private BeanAccessHelper beanAccessHelper;

    @Value(value = "${regionalDataFQCN}")
    private String regionalDataFQCN;

    private Class regionalDataClass = null;

    public static final String FIELD_KEY = "key";
    public static final String FIELD_VALUE = "value";
    public static final String FIELD_NAME = "name";
    public static final String METHOD_SETKEY = "setKey";
    public static final String METHOD_GETFIELD = "getField";
    private static final String DEFAULT_REGIONAL_FIELD_VALUE_VALIDATOR = "defaultRegionalFieldValueValidator";

    public static final String EXCEPTION_MSG = "Exception occurred ";

    private boolean isInstanceOfRegionalData(Object regionalDataObject) {
        if (StringUtils.isNotEmpty(regionalDataFQCN)
                && isNull(regionalDataClass)) {
            try {
                regionalDataClass = Class.forName(regionalDataFQCN);
            } catch (ClassNotFoundException e) {
                ExceptionUtility.rethrowSystemException(e);
            }
        }

        if (regionalDataClass.isAssignableFrom(regionalDataObject.getClass())) {
            return true;
        }

        return false;
    }

    public Map<String, String> getLogicalNameAndRegionalFieldMap(
            RegionalEnabled regionalEnabled) {

        String sourceEntityName = regionalEnabled.getClass().getName();

        Map<String, String> logicalNameAndRegionalFieldMap = new HashedMap();

        Map<String, Object> map = regionalXMLReader
                .getAllRegionalResponseFieldMap();
        if (notNull(map)) {
            List<RegionalFieldsAttributes> regionalFieldsAttributeList = (List<RegionalFieldsAttributes>) map
                    .get(sourceEntityName);
            for (RegionalFieldsAttributes regionalFieldsAttribute : regionalFieldsAttributeList) {
                logicalNameAndRegionalFieldMap.put(
                        regionalFieldsAttribute.getLogicalName(),
                        regionalFieldsAttribute.getFieldName());
            }
        }

        return logicalNameAndRegionalFieldMap;
    }

    public void saveAppDataToREField(Object regionalWSRequestData,
            RegionalEnabled regionalEnabled) {

        try {
            if (isInstanceOfRegionalData(regionalWSRequestData)) {
                List fields = (List) PropertyUtils.getProperty(
                        regionalWSRequestData, "field");
                if (isNull(regionalEnabled.getRegionalData())) {
                    regionalEnabled.setRegionalData(new RegionalData());
                }
                Map<String, String> logicalAndRegionalFieldMap = getLogicalNameAndRegionalFieldMap(regionalEnabled);
                String sourceEntityName = HibernateProxyHelper
                        .getClassWithoutInitializingProxy(regionalEnabled)
                        .getName();
                Map<String, RegionalMetaData> logicalNameRegionalMetaDataMap = regionalMetaDataService
                        .getLogicalNameAndRegionalMetaDataMap(sourceEntityName);
                for (Object field : fields) {
                    setReginalDataValue(field, logicalAndRegionalFieldMap,
                            regionalEnabled, logicalNameRegionalMetaDataMap,
                            fields);
                }
            }

        } catch (IllegalAccessException e) {
            ExceptionUtility.rethrowSystemException(e);
        } catch (InvocationTargetException e) {
            ExceptionUtility.rethrowSystemException(e);
        } catch (NoSuchMethodException e) {
            ExceptionUtility.rethrowSystemException(e);
        }

    }

    private void setReginalDataValue(Object field,
            Map<String, String> logicalAndRegionalFieldNameMap,
            RegionalEnabled regionalEnabled,
            Map<String, RegionalMetaData> logicalNameRegionalMetaDataMap,
            List fields) {

        try {
            RegionalData regionalData = regionalEnabled.getRegionalData();
            Enum enumData = (Enum) PropertyUtils.getProperty(field, FIELD_KEY);
            Method method = enumData.getClass().getMethod(FIELD_VALUE);
            String logicalName = (String) method.invoke(enumData);
            String fieldValue = (String) PropertyUtils.getProperty(field,
                    FIELD_VALUE);
            if (notNull(logicalAndRegionalFieldNameMap)
                    && logicalAndRegionalFieldNameMap.containsKey(logicalName)) {
                String fieldName = logicalAndRegionalFieldNameMap
                        .get(logicalName);
                Object targetValue = prepareTargetValueForRegionalFieldBasedOnReferenceEntityAndBean(
                        fieldValue,
                        getRegionalMetaDataBasedOnLogicalName(logicalName,
                                logicalNameRegionalMetaDataMap), fields);
                BeanUtils.setProperty(regionalData, fieldName, targetValue);
            }
        } catch (IllegalAccessException e) {
            BaseLoggers.flowLogger.error(
                    new StringBuilder().append("Exception occurred ")
                            .toString(), e);
            ExceptionUtility.rethrowSystemException(e);

        } catch (NoSuchMethodException e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);

        } catch (SecurityException e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);

        } catch (IllegalArgumentException e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);

        } catch (InvocationTargetException e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);

        }

        catch (Exception e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);

        }

    }

    private Object prepareTargetValueForRegionalFieldBasedOnReferenceEntityAndBean(
            String fieldValue, RegionalMetaData regionalMetaDataObj, List fields) {
        Object targetValue = fieldValue;
        Object beanObject=null;
        if (notNull(regionalMetaDataObj)
                && isNotBlank(regionalMetaDataObj.getReferencedEntityName())
                && isNotBlank(regionalMetaDataObj.getValidationQuery())
                && isBlank(regionalMetaDataObj.getValidatorBeanName())) {
            beanObject = beanAccessHelper.getBean(
                    DEFAULT_REGIONAL_FIELD_VALUE_VALIDATOR,
                    IRegionalFieldValueValidator.class);     
            targetValue = prepareTargetValueForRegionalField(fieldValue,
                    regionalMetaDataObj, fields,beanObject);        
        }else if(notNull(regionalMetaDataObj) && isNotBlank(regionalMetaDataObj.getValidatorBeanName())){
            beanObject = beanAccessHelper.getBean(
                    regionalMetaDataObj.getValidatorBeanName(),
                    IRegionalFieldValueValidator.class);
            targetValue = prepareTargetValueForRegionalField(fieldValue,
                    regionalMetaDataObj, fields,beanObject);        
        }        
      
        return targetValue;
    }

    private RegionalMetaData getRegionalMetaDataBasedOnLogicalName(
            String logicalName,
            Map<String, RegionalMetaData> logicalNameRegionalMetaDataMap) {
        RegionalMetaData regionalMetaDataObj = null;
        if (logicalNameRegionalMetaDataMap.containsKey(logicalName)) {
            regionalMetaDataObj = logicalNameRegionalMetaDataMap
                    .get(logicalName);
        }
        return regionalMetaDataObj;
    }

    private Object prepareTargetValueForRegionalField(String fieldValue,
            RegionalMetaData regionalMetaDataObj, List fields,Object beanObject) {
        Object targetValue = fieldValue;
        if (notNull(beanObject)) {
                targetValue=((IRegionalFieldValueValidator) beanObject).getTargetValueForRegionalField(fieldValue,
                        regionalMetaDataObj, fields);
        }
        return targetValue;

    }

    public void getRegionalResponseData(Object responseAppData,
            RegionalEnabled regionalEnabled) {
        try {
            Method fieldListMethod = responseAppData.getClass().getMethod(
                    METHOD_GETFIELD);
            List fieldList = (List) fieldListMethod.invoke(responseAppData);
            Map<String, String> logicalNameAndRegionalFieldMap = getLogicalNameAndRegionalFieldMap(regionalEnabled);

            if (notNull(logicalNameAndRegionalFieldMap)) {

                for (String logicalName : logicalNameAndRegionalFieldMap
                        .keySet())

                {

                    setFieldValue(responseAppData, logicalName,
                            logicalNameAndRegionalFieldMap, regionalEnabled,
                            fieldList);
                }
            }
        } catch (IllegalAccessException e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);
        } catch (NoSuchMethodException e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);
        } catch (IllegalArgumentException e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);
        } catch (InvocationTargetException e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);
        } catch (InstantiationException e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);
        } catch (NoSuchFieldException e) {
            BaseLoggers.flowLogger.error(EXCEPTION_MSG, e);
            ExceptionUtility.rethrowSystemException(e);
        }

    }

    private void setFieldValue(Object responseAppData, String logicalName,
            Map<String, String> logicalNameAndRegionalFieldMap,
            RegionalEnabled regionalEnabled, List fieldList)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, NoSuchFieldException {
        if (notNull(logicalNameAndRegionalFieldMap.get(logicalName))
                && isInstanceOfRegionalData(responseAppData)) {
            final Class[] innerClasses = responseAppData.getClass()
                    .getClasses();
            String fieldName;

            Object field = innerClasses[0].newInstance();

            fieldName = logicalNameAndRegionalFieldMap.get(logicalName);
            BaseLoggers.flowLogger.info(new StringBuilder()
                    .append("Fieldname for the key").append(logicalName)
                    .append(" is").append(fieldName).toString());

            Field key = field.getClass().getDeclaredField(FIELD_KEY);

            Object[] constantList = key.getType().getEnumConstants();
            for (Object constant : constantList) {
                Method valueMethod = constant.getClass().getMethod(FIELD_VALUE);
                String enumVal = (String) valueMethod.invoke(constant);

                if (logicalName.equals(enumVal)) {
                    Method keySetterMethod = field.getClass().getMethod(
                            METHOD_SETKEY, constant.getClass());
                    keySetterMethod.invoke(field, constant);

                }

            }

            BeanUtils.setProperty(
                    field,
                    FIELD_VALUE,
                    (String) PropertyUtils.getProperty(
                            regionalEnabled.getRegionalData(), fieldName));
            fieldList.add(field);
        }

    }

}
