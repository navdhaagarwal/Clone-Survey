package com.nucleus.core.formsConfiguration;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.nucleus.core.formsConfiguration.fieldcomponent.LOVFieldVO;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.dynamicform.service.FormService;
import com.nucleus.core.formsConfiguration.fieldcomponent.EmailInfoVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;
import com.nucleus.core.money.builder.MoneyBuilder;
import com.nucleus.core.money.entity.Money;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.money.MoneyService;
import com.nucleus.persistence.EntityDao;

import io.swagger.annotations.ApiModelProperty;

@Embeddable
public class DynamicForm implements Serializable {

    private static final long serialVersionUID = 1L;

	@ApiModelProperty(hidden=true)
    @Transient
    private Map<String, Object> dataMapForDisplay;

	@ApiModelProperty(hidden=true)
    @Transient
    private Map<String, Object> dataMapWithActualType;

    @Column(length = 4000)
    private String dataJsonString;

    private Long placeholderId;
    
    private Long modelMetaDataId;

    
    private Long uiMetaDataId;
    
	@ApiModelProperty(hidden=true)
    @Transient
    private ModelMetaData modelMetaData;

	@ApiModelProperty(hidden=true)
    @Transient
    private UIMetaData uiMetaData;

    public String getDataJsonString() {
        return dataJsonString;
    }

    public void setDataJsonString(String dataJsonString) {
        this.dataJsonString = dataJsonString;
    }

    public Long getModelMetaDataId() {
        return modelMetaDataId;
    }

    public void setModelMetaDataId(Long modelMetaDataId) {
        this.modelMetaDataId = modelMetaDataId;
    }

    public Long getUiMetaDataId() {
        return uiMetaDataId;
    }

    public void setUiMetaDataId(Long uiMetaDataId) {
        this.uiMetaDataId = uiMetaDataId;
    }

    public Map<String, Object> getDataMapForDisplay() {
        return dataMapForDisplay;
    }

    public void setDataMapForDisplay(Map<String, Object> dataMapForDisplay) {
        this.dataMapForDisplay = dataMapForDisplay;
    }

    public Map<String, Object> getDataMapWithActualType() {
        return dataMapWithActualType;
    }

    public void setDataMapWithActualType(Map<String, Object> dataMapWithActualType) {
        this.dataMapWithActualType = dataMapWithActualType;
    }
    
    public ModelMetaData getModelMetaData(){
    	if(ValidatorUtils.notNull(this.modelMetaData)){
    		return this.modelMetaData;
    	}else{
        EntityDao entityDao = NeutrinoSpringAppContextUtil.getBeanByName(
                "entityDao", EntityDao.class);
        	this.modelMetaData = entityDao.find(ModelMetaData.class, this.modelMetaDataId);
        	return this.modelMetaData;
    	}
    }
    
    public UIMetaData getUiMetaData(){
    	if(ValidatorUtils.notNull(this.uiMetaData)){
    		return this.uiMetaData;
    	}else{
        EntityDao entityDao = NeutrinoSpringAppContextUtil.getBeanByName(
                "entityDao", EntityDao.class);
        	this.uiMetaData = entityDao.find(UIMetaData.class, this.uiMetaDataId);
        	return this.uiMetaData;
    	}
    }
    
     /**
     * 
     * @return
     */
    public Map<String, Object> getFieldValuePariringForDisplay() {

        if (dataMapForDisplay == null) {
            dataMapForDisplay = transFormJSONAsMapAsperMetaData(
                    this.dataJsonString, getModelMetaData(), false);
        }
        return dataMapForDisplay;
    }

    /**
     * 
     * @return
     */
    public Map<String, Object> getFieldValuePariringWithActualType() {

        if (dataMapWithActualType == null && !isEmpty(dataJsonString)) {
            dataMapWithActualType = transFormJSONAsMapAsperMetaData(
                    this.dataJsonString, getModelMetaData(), true);
        }
        return dataMapWithActualType;
    }

    /**
     * 
     * @param jsonString
     * @param modelMetaData
     * @param actualDataType
     * @return
     */
    public static Map<String, Object> transFormJSONAsMapAsperMetaData(
            String jsonString, ModelMetaData modelMetaData,
            boolean actualDataType) {
        Map<String, Object> transformedMap = null;
        JSONObject sourceJsonObject = null;
        try {
            sourceJsonObject = new JSONObject(jsonString);
            transformedMap = convertJsonObjectToMap(sourceJsonObject,
                    modelMetaData, actualDataType);
        } catch (JSONException e) {
            throw ExceptionBuilder
                    .getInstance(ServiceInputException.class)
                    .setExceptionCode("invalid.json.data")
                    .setMessage(
                            new Message("invalid.json.data", MessageType.ERROR))
                    .setLogMessage("Invalid JSON String was supplied.").build();

        }
        return transformedMap;
    }

    private static Map<String, Object> convertJsonObjectToMap(
            JSONObject sourceJSONObject, ModelMetaData modelMetaData,
            boolean actualDataType) {
        Map<String, Object> preparedMap = new HashMap<String, Object>();

        Object jsonObject = null;
        String key = null;
        Object value = null;

        try {
            Iterator<String> keys = sourceJSONObject.keys();
            while (keys.hasNext()) {
                value = null;
                key = keys.next();

                if (null != key && !sourceJSONObject.isNull(key)) {
                    value = sourceJSONObject.get(key);
                }

                if (value instanceof JSONObject) {
                    Object obj = convertJSONObjectToDisplayAndUpdateMap(key,
                            value, modelMetaData.getFields(), preparedMap);

                    if (!(obj instanceof Boolean)) {
                        preparedMap.put(
                                key,
                                convertJsonObjectToMap((JSONObject) value,
                                        modelMetaData, actualDataType));
                    }
                    continue;
                }

                if (value instanceof JSONArray) {
                    JSONArray array = ((JSONArray) value);
                    List list = new ArrayList();
                    for (int i = 0; i < array.length(); i++) {
                        jsonObject = array.get(i);
                        if (jsonObject instanceof JSONObject) {
                            list.add(convertJsonObjectToMap(
                                    (JSONObject) jsonObject, modelMetaData,
                                    actualDataType));
                        } else {
                            if (modelMetaData != null
                                    && modelMetaData.getFields() != null) {

                                if (!actualDataType) {
                                    list.add(convertJSONObjectToDisplayAndUpdateMap(
                                            key, jsonObject,
                                            modelMetaData.getFields(),
                                            preparedMap));
                                } else {
                                    list.add(convertJSONObjectToActualTypeAndUpdateMap(
                                            key, jsonObject,
                                            modelMetaData.getFields()));
                                }
                            }
                        }
                    }
                    preparedMap.put(key, list);
                    continue;
                }

                if (modelMetaData != null && modelMetaData.getFields() != null
                        && null != value) {

                    if (!actualDataType) {
                        preparedMap.put(
                                key,
                                convertJSONObjectToDisplayAndUpdateMap(key,
                                        value, modelMetaData.getFields(),
                                        preparedMap));
                    } else {
                        preparedMap.put(
                                key,
                                convertJSONObjectToActualTypeAndUpdateMap(key,
                                        value, modelMetaData.getFields()));
                    }
                }else if (ValidatorUtils.isNull(value)){
                	preparedMap.put(
                            key,"");
                }
            }
        } catch (Exception e) {
            BaseLoggers.flowLogger
                    .debug("Exception occured while reading Json Object :: for Key :: "
                            + key + " :: and value :: " + value);
            BaseLoggers.flowLogger
                    .info("Exception occured while reading Json Object :: for Key :: "
                            + key + " :: and value :: " + value);

            BaseLoggers.flowLogger.info(e.getMessage());
            BaseLoggers.flowLogger.debug(e.getMessage());
        }
        return preparedMap;
    }

    /**
     * 
     * Function to get the actual value
     * 
     * @param key
     * @param value
     * @return
     */
    private static Object convertJSONObjectToDisplayAndUpdateMap(String key,
            Object value, List<FieldMetaData> fields,
            Map<String, Object> preparedMap) {
        Object obj = value;
        FormService formService = NeutrinoSpringAppContextUtil.getBeanByName(
                "formConfigService", FormService.class);

        try {
            FieldMetaData fieldMetaData = getFieldDataType(key, fields);
            int dataType = null != fieldMetaData ? fieldMetaData.getDataType()
                    : 0;

            if (dataType == FieldDataType.DATA_TYPE_DATE) {

                Long currentDateTime = (Long) value;

                String defaultDateFormat = formService
                        .getUserPreferredDateFormat();

                DateFormat formatter = new SimpleDateFormat(defaultDateFormat);

                // Converting milliseconds to Date using Calendar
                DateTime cal = new DateTime(currentDateTime);
                obj = formatter.format(cal.toDate());

            } else if (dataType == FieldDataType.DATA_TYPE_TEXT_REFERENCE) {
                if (null != value && !"".equals(value)) {
                    obj = Long.parseLong(((String) value).split(":")[1]
                            .split("}")[0]);
                }

            } else if (dataType == FieldDataType.DATA_TYPE_PHONE) {
                JSONObject getPhoneJSONObject = (JSONObject) value;
                PhoneNumberVO phoneNumberVO = new ObjectMapper().readValue(
                        getPhoneJSONObject.toString(), PhoneNumberVO.class);
                preparedMap.put(key, phoneNumberVO);
                return true;
            } else if (dataType == FieldDataType.DATA_TYPE_EMAIL) {
                JSONObject getEmailJSONObject = (JSONObject) value;
                EmailInfoVO emailInfoVO = new ObjectMapper().readValue(
                        getEmailJSONObject.toString(), EmailInfoVO.class);
                preparedMap.put(key, emailInfoVO);
                return true;
            } else if (dataType == FieldDataType.DATA_TYPE_LOV) {
                JSONObject getLOVFieldJSONObject = (JSONObject) value;
                LOVFieldVO lovFieldVO = new ObjectMapper().readValue(
                        getLOVFieldJSONObject.toString(), LOVFieldVO.class);
                preparedMap.put(key, lovFieldVO);
                return true;
            }

        } catch (Exception e) {
            BaseLoggers.flowLogger
                    .debug("Exception occured while reading Json Object :: for Key :: "
                            + key + " :: and value :: " + value);
            BaseLoggers.flowLogger
                    .info("Exception occured while reading Json Object :: for Key :: "
                            + key + " :: and value :: " + value);

            BaseLoggers.flowLogger.info(e.getMessage());
            BaseLoggers.flowLogger.debug(e.getMessage());
        }
        return obj;
    }

    /**
     * 
     * Function to get the actual value
     * 
     * @param key
     * @param value
     * @return This method will actually load full objects on hich rule can be
     *         executed
     */
    private static Object convertJSONObjectToActualTypeAndUpdateMap(String key,
            Object value, List<FieldMetaData> fields) {

        FormService formService = NeutrinoSpringAppContextUtil.getBeanByName(
                "formConfigService", FormService.class);
        MoneyService moneyService = NeutrinoSpringAppContextUtil.getBeanByName(
                "moneyService", MoneyService.class);
        Object obj = value;

        try {
            FieldMetaData fieldMetaData = getFieldDataType(key, fields);
            int dataType = null != fieldMetaData ? fieldMetaData.getDataType()
                    : 0;

            if (dataType == FieldDataType.DATA_TYPE_DATE) {

                Long currentDateTime = (Long) value;

                String defaultDateFormat = formService
                        .getUserPreferredDateFormat();

                DateFormat formatter = new SimpleDateFormat(defaultDateFormat);

                // Converting milliseconds to Date using Calendar
                DateTime cal = new DateTime(currentDateTime);
                obj = formatter.format(cal.toDate());

            } else if (dataType == FieldDataType.DATA_TYPE_TEXT_BOOLEAN) {
                if (null != value && !"".equals(value)) {
                    obj = Boolean.parseBoolean((String) value);
                }

            } else if (dataType == FieldDataType.DATA_TYPE_TEXT) {
                obj = value;

            } else if (dataType == FieldDataType.DATA_TYPE_NUMBER
                    || dataType == FieldDataType.DATA_TYPE_INTEGER) {
                if (null != value && !"".equals(value)) {

                    if (value instanceof Integer) {
                        obj = new BigDecimal((Integer) value);

                    } else if (value instanceof Long) {
                        obj = new BigDecimal((Long) value);

                    } else if (value instanceof Number) {
                        obj = BigDecimal
                                .valueOf(((Number) value).doubleValue())
                                .setScale(4, BigDecimal.ROUND_HALF_EVEN);
                    }
                } else {
                    obj = new BigDecimal(0);
                }

            } else if (dataType == FieldDataType.DATA_TYPE_MONEY) {
                if (null != value && !"".equals(value)) {
                    obj = moneyService.parseMoney((String) value, null);
                } else {
                    Money money = new MoneyBuilder().setNonBaseAmountvalue(
                            BigDecimal.ZERO.toString()).getMoney();
                    money.getBaseAmount().setCurrency(
                            Money.getBaseCurrency().getCurrencyCode());
                    obj = money;
                }

            } else if (dataType == FieldDataType.DATA_TYPE_TEXT_REFERENCE) {
                if (null != value && !"".equals(value)) {
                    String val = (String) value;
                    obj = formService.loadReference(EntityId.fromUri((val)
                            .substring(val.indexOf("{") + 1,
                                    val.lastIndexOf("}"))));
                }

            } else if (dataType == FieldDataType.DATA_TYPE_PHONE) {
                JSONObject getPhoneJSONObject = (JSONObject) value;
                getPhoneJSONObject.remove("class");
                PhoneNumberVO phoneNumberVO = new ObjectMapper().readValue(
                        getPhoneJSONObject.toString(), PhoneNumberVO.class);
                return phoneNumberVO;
            } else if (dataType == FieldDataType.DATA_TYPE_LOV) {
                JSONObject getLOVFieldJSONObject = (JSONObject) value;
                getLOVFieldJSONObject.remove("class");
                LOVFieldVO lovFieldVO = new ObjectMapper().readValue(
                        getLOVFieldJSONObject.toString(), LOVFieldVO.class);
                return formService.loadLOVFieldValueOrReference(lovFieldVO);
            }

        } catch (Exception e) {
            BaseLoggers.flowLogger
                    .debug("Exception occured while reading Json Object :: for Key :: "
                            + key + " :: and value :: " + value);
            BaseLoggers.flowLogger
                    .info("Exception occured while reading Json Object :: for Key :: "
                            + key + " :: and value :: " + value);

            BaseLoggers.flowLogger.info(e.getMessage());
            BaseLoggers.flowLogger.debug(e.getMessage());
        }

        return obj;
    }

    private static FieldMetaData getFieldDataType(String fieldKey,
            List<FieldMetaData> fields) {

        for (FieldMetaData fieldMetaData : fields) {
            if (fieldKey.equals(fieldMetaData.getFieldKey())) {
                return fieldMetaData;
            }
        }
        return null;
    }

	public Long getPlaceholderId() {
		return placeholderId;
	}

	public void setPlaceholderId(Long placeholderId) {
		this.placeholderId = placeholderId;
	}

}


   

