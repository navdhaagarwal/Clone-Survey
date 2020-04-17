package com.nucleus.core.dynamicform.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.dynamicform.exception.InvalidDynamicFormDataException;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.base.validation.domainobject.ValidationRuleResult;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.master.CriteriaMapVO;
import org.apache.commons.beanutils.PropertyUtils;
import com.nucleus.core.formsConfiguration.fieldcomponent.LOVFieldVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.formsConfiguration.DynamicForm;
import com.nucleus.core.formsConfiguration.FieldDataType;
import com.nucleus.core.formsConfiguration.FieldDefinition;
import com.nucleus.core.formsConfiguration.FieldMetaData;
import com.nucleus.core.formsConfiguration.FormContainerType;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.PanelDefinition;
import com.nucleus.core.formsConfiguration.PersistentFormData;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.core.formsConfiguration.fieldcomponent.EmailInfoVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;
import com.nucleus.core.money.builder.MoneyBuilder;
import com.nucleus.core.money.entity.Money;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.money.MoneyService;
import com.nucleus.service.BaseFormServiceImpl;

@Named(value = "jsonMapConverter")
public class JsonMapConverter extends BaseFormServiceImpl {

    private static final String NO_VALUE_EXISTS_INTO_SYSTEM = "";
    private static final String INVALID_VALUE_CASCADE_SELECT = "";

    @Inject
    @Named("moneyService")
    protected MoneyService moneyService;

    @Inject
    @Named("formConfigService")
    protected FormService  formService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    @Inject
    @Named("dynamicFormValidator")
    private DynamicFormValidator dynamicFormValidator;
    
    private static final String UNABLE_TO_CONVERT_VALUE_TO_DATE_OBJECT="Unable to convert parse value to Date";
    
    public Map<String, Object> prepareDynamicFormDataJsonMap(PersistentFormData persistentFormData) {
       Map<String, Object> jsonMap = new HashMap<String, Object>();
        Map<String, Object> contextMap = persistentFormData.getFieldValuePariring();
        EntityId entityId=EntityId.fromUri(persistentFormData.getFormUri());
        UIMetaData uiMetaDataObj = entityDao.find(UIMetaData.class,entityId.getLocalId());
        if(notNull(uiMetaDataObj) && hasElements(uiMetaDataObj.getPanelDefinitionList())){
        	for(PanelDefinition panelDefObj:uiMetaDataObj.getPanelDefinitionList()){
        			if(notNull(panelDefObj)){
        				if(notNull(panelDefObj.getPanelType()) && panelDefObj.getPanelType()==FormContainerType.FIELD_TYPE_PANEL){
        					updateValueInJsonMapForDefinitionTypePanel(panelDefObj,contextMap,jsonMap);   
        				}
        				else if(notNull(panelDefObj.getPanelType()) && panelDefObj.getPanelType()==FormContainerType.FIELD_TYPE_VIRTUAL){
        					updateValueInJsonMapForDefinitionTypeVirtual(contextMap,jsonMap,panelDefObj);
        				}
        				else if(notNull(panelDefObj.getPanelType()) && panelDefObj.getPanelType()==FormContainerType.FIELD_TYPE_TABLE){
        					updateValueInJsonMapForDefinitionTypeTable(panelDefObj,contextMap,jsonMap);                       
        				}
        			}
        	   }	
        	}
    return jsonMap;   
    
    }

    private void updateValueInJsonMapForDefinitionTypePanel(
			PanelDefinition panelDefObj, Map<String, Object> contextMap,
			Map<String, Object> jsonMap) {
        Map<String,Object> actualPanelMapData = new HashMap<String, Object>();
         if(CollectionUtils.isNotEmpty(panelDefObj.getFieldDefinitionList())){
         	for(Entry<String, Object> mapEntry : contextMap.entrySet()){
	            Object value = mapEntry.getValue();
	            Map<String,Object> panelValuesMap=(Map<String,Object>)value;
	            if(MapUtils.isNotEmpty(panelValuesMap)){
	            	for(Entry<String,Object> entry:panelValuesMap.entrySet()){
	            		String mapKey = entry.getKey();
		                Object mapValue = entry.getValue();
		                convertTableValuesToObject(mapKey,mapValue,panelDefObj.getFieldDefinitionList(),actualPanelMapData);
	            	}
	            }
         		
         	}                           
         }
         jsonMap.put(panelDefObj.getPanelKey(), actualPanelMapData);
	}

	private void updateValueInJsonMapForDefinitionTypeTable(PanelDefinition panelDefObj,Map<String, Object> contextMap,Map<String, Object> jsonMap) {
		String key = null;
        Object value = null;
        Map<String, Object> tableContextMap=new HashMap<String, Object>();
		List<Map<String, Object>> tablePanelDataMapList = new ArrayList<Map<String, Object>>();
        List<FieldDefinition> fieldDefinitionList = panelDefObj.getFieldDefinitionList();
		if (MapUtils.isNotEmpty(contextMap)) {
			tableContextMap.put(panelDefObj.getPanelKey(),contextMap.get(panelDefObj.getPanelKey()));
            for (Entry<String, Object> entry : tableContextMap.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();
                List<Object> tableValuesList=(List<Object>)value;
                if(hasElements(tableValuesList)){
                	for(Object tableRowObj:tableValuesList){
                		Map<String,Object> tableRowObjValueMap=(Map<String,Object>)tableRowObj;
                		Map<String,Object> tableValuesMap=new HashMap<String, Object>();
		                for(Entry<String, Object> mapEntry : tableRowObjValueMap.entrySet()){
		                	String mapKey = mapEntry.getKey();
			                Object mapValue = mapEntry.getValue();
			                convertTableValuesToObject(mapKey,mapValue,fieldDefinitionList,tableValuesMap);
		                }
		                tablePanelDataMapList.add(tableValuesMap);
		               
                	}
                	 jsonMap.put(key, tablePanelDataMapList);
                }
                
                
                
               
            }
		} 
	}

	private void convertTableValuesToObject(String mapKey, Object mapValue,
			List<FieldDefinition> fieldDefinitionList, Map<String,Object> tableValuesMap) {
    	
		for(FieldDefinition fieldDef:fieldDefinitionList){
			if(notNull(fieldDef.getFieldKey()) && fieldDef.getFieldKey().equals(mapKey)){
				int fieldDataType=fieldDef.getFieldDataType();
				if (fieldDataType != 0 && fieldDataType == FieldDataType.DATA_TYPE_TEXT_REFERENCE) {
	        		updateValueInJsonMapForDataTypeReference(mapValue,tableValuesMap,mapKey);
	        	}
	        	else if(fieldDataType != 0 && fieldDataType==FieldDataType.DATA_TYPE_DATE){
	        		updateValueInJsonMapForDataTypeDate(mapValue,tableValuesMap,mapKey);
	        	}
	        	else if(fieldDataType != 0 && fieldDataType==FieldDataType.DATA_TYPE_MONEY){
	        		updateValueInJsonMapForDataTypeMoney(mapValue,tableValuesMap,mapKey);
	        	}else if(fieldDataType !=0 && fieldDataType==FieldDataType.DATA_TYPE_PHONE){
	        		updateValueInJsonMapForDataTypePhone(mapValue,tableValuesMap,mapKey);
                }else if(fieldDataType !=0 && fieldDataType==FieldDataType.DATA_TYPE_LOV){
                    updateValueInJsonMapForDataTypeLOV(mapValue,tableValuesMap,mapKey);
	        	}else if (fieldDataType == FieldDataType.DATA_TYPE_TEXT_BOOLEAN) {
	                tableValuesMap.put(mapKey, mapValue);
	            }else if (fieldDataType == FieldDataType.DATA_TYPE_TEXT) {
	            	tableValuesMap.put(mapKey, mapValue);

	            }else if (fieldDataType == FieldDataType.DATA_TYPE_NUMBER || fieldDataType == FieldDataType.DATA_TYPE_INTEGER) {
	            	Object object =null;
	                if (null != mapValue && !"".equals(mapValue)) {
	                	object = new BigDecimal((String)mapValue);
	                } else {
	                	object = new BigDecimal(0);
	                }
	                tableValuesMap.put(mapKey, object);
	            } 
			}			
		}
	}

	private void updateValueInJsonMapForDefinitionTypeVirtual(Map<String,Object> contextMap,Map<String, Object> jsonMap,PanelDefinition panelDefObj) {
    	String key = null;
        Object value = null;
        
        List<FieldDefinition> fieldDefinitionList = panelDefObj.getFieldDefinitionList();
     		if (MapUtils.isNotEmpty(contextMap)) {
                 for (Entry<String, Object> entry : contextMap.entrySet()) {
                     key = entry.getKey();
                     value = entry.getValue();
                     convertTableValuesToObject(key,value,fieldDefinitionList,jsonMap);
                 }
     		}
	}

	private void updateValueInJsonMapForDataTypePhone(Object value,
			Map<String, Object> jsonMap, String key) {
    	
            JSONObject phoneJsonObj = new JSONObject((Map<String,Object>)value) ;
            PhoneNumberVO phoneNumberVO=null;
			try {
				phoneNumberVO = new ObjectMapper().readValue(phoneJsonObj.toString(), PhoneNumberVO.class);
			} catch (JsonParseException e) {
                BaseLoggers.exceptionLogger.error(e.getMessage(), e);
			} catch (JsonMappingException e) {
                BaseLoggers.exceptionLogger.error(e.getMessage(), e);
			} catch (IOException e) {
                BaseLoggers.exceptionLogger.error(e.getMessage(), e);
			}
            jsonMap.put(key, phoneNumberVO);	
	}

    private void updateValueInJsonMapForDataTypeLOV(Object value,
                                                      Map<String, Object> jsonMap, String key) {

        JSONObject lovFieldJsonObj = new JSONObject((Map<String,Object>)value) ;
        LOVFieldVO lovFieldVO=null;
        try {
            lovFieldVO = new ObjectMapper().readValue(lovFieldJsonObj.toString(), LOVFieldVO.class);
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        jsonMap.put(key, lovFieldVO);
    }

	private void updateValueInJsonMapForDataTypeMoney(Object value,
			Map<String, Object> jsonMap, String key) {
    	if(value instanceof java.util.Map){
			 Map<String,Object> fieldKeyValueMap=new HashMap<String, Object>();
			 fieldKeyValueMap=(Map<String, Object>)value;                     	
   		 jsonMap.put(key,fieldKeyValueMap.get("formattedAmount"));
		}
		
	}


	private void updateValueInJsonMapForDataTypeDate(Object value,Map<String,Object> jsonMap,String key) {
		Object dateObj=null;
        String defaultDateFormat = getUserPreferredDateFormat();
        DateFormat formatter = new SimpleDateFormat(defaultDateFormat);
        Date date = null;
        if (null != value && !"".equals(value)) {
            try {
                date = formatter.parse((String)value);
            } catch (ParseException e) {
            	throw ExceptionBuilder.getInstance(BaseException.class,UNABLE_TO_CONVERT_VALUE_TO_DATE_OBJECT,UNABLE_TO_CONVERT_VALUE_TO_DATE_OBJECT).setOriginalException(e)
                .setMessage(UNABLE_TO_CONVERT_VALUE_TO_DATE_OBJECT).build();
            }
            if (date != null) {
                DateTime calendar = new DateTime(date);
                dateObj = calendar;
            }
            dateObj = ((DateTime) dateObj).getMillis();
        }
        jsonMap.put(key,dateObj);
	
	}

	private void updateValueInJsonMapForDataTypeReference(Object value,Map<String,Object> jsonMap,String key) {
		if(value instanceof java.util.Map){
			 Map<String,Object> fieldKeyValueMap=new HashMap<String, Object>();
			 fieldKeyValueMap=(Map<String, Object>)value;                     	
    		 jsonMap.put(key,fieldKeyValueMap.get("uri"));
		}else if (value instanceof java.util.List) {
		    List<Object> objectList = (List<Object>) value;
            if (null != objectList && objectList.size() > 0) {                        	
                if (objectList.get(0) instanceof java.util.Map) {
                	Map<String,Object> myMap=new HashMap<String, Object>();
                	myMap=(Map<String, Object>) objectList.get(0);
                    value=myMap.get("uri");                      	
                    jsonMap.put(key,value);
                }
            }
		}    
	}
	
	private void updateAndCallBackForDataTypeReferenceConversion(FieldDefinition fieldDefinition,Object value,Map<String,Object> jsonMap,String key,boolean doValidate)
    {
		//Handle third party data
		if(value instanceof java.util.Map){
			 Map<String,Object> fieldKeyValueMap=(Map<String, Object>)value;
			if(fieldKeyValueMap.containsKey("uri"))
			{
				jsonMap.put(key,fieldKeyValueMap.get("uri"));
			}
		}else if (value instanceof java.util.List) {
			List<Object>   objectList = (List<Object>) value;
            if (null != objectList && objectList.size() > 0) {                        	
                if (objectList.get(0) instanceof java.util.Map) {
                	Map<String,Object> myMap=(Map<String,Object>) objectList.get(0);
                    value=myMap.get("uri");                      	
                    jsonMap.put(key,value);
                }
            }
		}    
	}

    /**
     * 
     * Method to get Json Map from COntext MAp
     *
     * @return
     */
    public Map<String, Object> saveFormPersistentDataMap(Map<String, Object> dataMap,List<FieldMetaData> fields) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        Map<String, Object> contextMap = dataMap;

        String key = null;
        Object value = null;

        String key1 = null;
        Object value1 = null;

        List<Map<String, Object>> tablePanelDataMapList = null;
        List<Map<String, Object>> newTablePanelDataMapList = null;

        Map<String, Object> actualPanelMapData = null;
        Map<String, Object> newActualPanelMapData = null;

        List<Object> tableFieldDataMapList = null;

        if (null != contextMap) {
            for (Entry<String, Object> entry : contextMap.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();

                if (value instanceof java.util.Map) {
                    // It is actual panel
                    actualPanelMapData = (Map<String, Object>) value;
                    newActualPanelMapData = new HashMap<String, Object>();

                    for (Entry<String, Object> entryPanelKeys : actualPanelMapData.entrySet()) {
                        key1 = entryPanelKeys.getKey();
                        value1 = entryPanelKeys.getValue();
                        newActualPanelMapData.put(key1,
                                createJsonObjectVal(key1, value1, fields));

                        jsonMap.put(key, newActualPanelMapData);
                    }

                } else if (value instanceof java.util.List) {
                    // if tabular panel
                    // List of Maps

                    List<Object> objectList = (List<Object>) value;
                    if (null != objectList && objectList.size() > 0) {

                        if (objectList.get(0) instanceof java.util.Map) {

                            tablePanelDataMapList = (List<Map<String, Object>>) value;

                            newTablePanelDataMapList = new ArrayList<Map<String, Object>>();

                            for (Map<String, Object> tablePanelMap : tablePanelDataMapList) {

                                newActualPanelMapData = new HashMap<String, Object>();

                                for (Entry<String, Object> entryPanelKeys : tablePanelMap.entrySet()) {

                                    key1 = entryPanelKeys.getKey();
                                    value1 = entryPanelKeys.getValue();
                                    newActualPanelMapData.put(
                                            key1,
                                            createJsonObjectVal(key1, value1, fields));
                                }
                                newTablePanelDataMapList.add(newActualPanelMapData);
                            }
                            jsonMap.put(key, newTablePanelDataMapList);

                        } else {
                            // if list of objects

                            tableFieldDataMapList = (List<Object>) value;

                            jsonMap.put(
                                    key,
                                    createJsonObjectVal(key, tableFieldDataMapList,fields));
                        }
                    }

                } else {
                    jsonMap.put(key, createJsonObjectVal(key, value, fields));
                }
            }
        }
        return jsonMap;
    }

    /**
     * 
     * Return the Map with actual Object Values
     * This will be in sync to display selected values in UI 
     * @param persistentFormData
     * @return
     */

    public Map<String, Object> loadFormPersistentDataMap(PersistentFormData persistentFormData, boolean ruleExecution) {
        Map<String, Object> contextMap = new HashMap<String, Object>();
        JSONObject jsonObject = null;

        try {
            if (null != persistentFormData) {
                if (persistentFormData.getFieldValueData() != null) {
                    jsonObject = new JSONObject(persistentFormData.getFieldValueData());
                    return getMap(jsonObject, persistentFormData, ruleExecution);
                }
            }
        } catch (Exception e) {
            throw new SystemException("Unable to read JSOn Object", e);
            // TODO : Handle Exception
        }
        return contextMap;
    }

    /**
     * Get configuration map from Json String
     * stored in db 
     * @param object
     * @param persistentFormData
     * @return
     * @throws Exception
     */
    private Map getMap(JSONObject object, PersistentFormData persistentFormData, boolean ruleExecution) {
        Map<String, Object> map = new HashMap<String, Object>();

        Object jsonObject = null;
        String key = null;
        Object value = null;

        try {
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {

                key = null;
                value = null;

                key = keys.next();

                if (null != key && !object.isNull(key)) {
                    value = object.get(key);
                }

                if (value instanceof JSONObject) {
                    Object obj = convertFromJsonToActual(key, value, persistentFormData.getModelMetaData().getFields(), map);

                    if (!(obj instanceof Boolean)) {
                        map.put(key, getMap((JSONObject) value, persistentFormData, ruleExecution));
                    }
                    continue;
                }

                if (value instanceof JSONArray) {
                    JSONArray array = ((JSONArray) value);
                    List list = new ArrayList();
                    for (int i = 0 ; i < array.length() ; i++) {
                        jsonObject = array.get(i);
                        if (jsonObject instanceof JSONObject) {
                            list.add(getMap((JSONObject) jsonObject, persistentFormData, ruleExecution));
                        } else {
                            if (persistentFormData.getModelMetaData() != null
                                    && persistentFormData.getModelMetaData().getFields() != null) {

                                if (!ruleExecution) {
                                    list.add(convertFromJsonToActual(key, jsonObject, persistentFormData.getModelMetaData()
                                            .getFields(), map));
                                } else {
                                    list.add(convertFromJsonToActualForRule(key, jsonObject, persistentFormData
                                            .getModelMetaData().getFields()));
                                }
                            }
                        }
                    }
                    map.put(key, list);
                    continue;
                }

                if (persistentFormData.getModelMetaData() != null
                        && persistentFormData.getModelMetaData().getFields() != null && null != value) {

                    if (!ruleExecution) {
                        map.put(key,
                                convertFromJsonToActual(key, value, persistentFormData.getModelMetaData().getFields(), map));
                    } else {
                        map.put(key,
                                convertFromJsonToActualForRule(key, value, persistentFormData.getModelMetaData().getFields()));
                    }
                }
            }
        } catch (Exception e) {
            BaseLoggers.flowLogger.debug("Exception occured while reading Json Object :: for Key :: " + key
                    + " :: and value :: " + value);
            BaseLoggers.flowLogger.info("Exception occured while reading Json Object :: for Key :: " + key
                    + " :: and value :: " + value);

            BaseLoggers.flowLogger.info(e.getMessage());
            BaseLoggers.flowLogger.debug(e.getMessage());
        }
        return map;
    }

    /**
     * 
     * Function to get the actual value
     * @param key
     * @param value
     * @return
     */
    private Object convertFromJsonToActual(String key, Object value, List<FieldMetaData> fields, Map<String, Object> map) {
        Object obj = value;

        try {
            FieldMetaData fieldMetaData = getFieldDataType(key, fields);
            int dataType = null != fieldMetaData ? fieldMetaData.getDataType() : 0;

            if (dataType == FieldDataType.DATA_TYPE_DATE) {

                Long currentDateTime=Long.valueOf(value.toString());

                String defaultDateFormat = getUserPreferredDateFormat();

                DateFormat formatter = new SimpleDateFormat(defaultDateFormat);

                // Converting milliseconds to Date using Calendar
                DateTime cal = new DateTime(currentDateTime);
                obj = formatter.format(cal.toDate());

            } else if (dataType == FieldDataType.DATA_TYPE_TEXT_REFERENCE) {
                if (null != value && !"".equals(value)) {
                    obj = Long.parseLong(((String) value).split(":")[1].split("}")[0]);
                }

            } else if (dataType == FieldDataType.DATA_TYPE_PHONE) {
                JSONObject getPhoneJSONObject = (JSONObject) value;
                PhoneNumberVO phoneNumberVO = new ObjectMapper().readValue(getPhoneJSONObject.toString(),
                        PhoneNumberVO.class);
                map.put(key, phoneNumberVO);
                return true;
            }else if(dataType == FieldDataType.DATA_TYPE_EMAIL){
                JSONObject getEmailJSONObject = (JSONObject) value;
                EmailInfoVO emailInfoVO = new ObjectMapper().readValue(getEmailJSONObject.toString(), EmailInfoVO.class);
                map.put(key, emailInfoVO);
                return true;
            }else if(dataType == FieldDataType.DATA_TYPE_LOV){
                JSONObject getLOVFieldJSONObject = (JSONObject) value;
                LOVFieldVO lovFieldVO = new ObjectMapper().readValue(getLOVFieldJSONObject.toString(), LOVFieldVO.class);
                map.put(key, lovFieldVO);
                return true;
            }

        } catch (Exception e) {
            BaseLoggers.flowLogger.debug("Exception occured while reading Json Object :: for Key :: " + key
                    + " :: and value :: " + value);
            BaseLoggers.flowLogger.info("Exception occured while reading Json Object :: for Key :: " + key
                    + " :: and value :: " + value);

            BaseLoggers.flowLogger.info(e.getMessage());
            BaseLoggers.flowLogger.debug(e.getMessage());
        }
        return obj;
    }

    /**
     * 
     * Function to get the actual value
     * @param key
     * @param value
     * @return
     * This method will actually load full objects
     * on hich rule can be executed
     */
    private Object convertFromJsonToActualForRule(String key, Object value, List<FieldMetaData> fields) {
        Object obj = value;

        try {
            FieldMetaData fieldMetaData = getFieldDataType(key, fields);
            int dataType = null != fieldMetaData ? fieldMetaData.getDataType() : 0;

            if (dataType == FieldDataType.DATA_TYPE_DATE) {

                Long currentDateTime = (Long) value;

                String defaultDateFormat = getUserPreferredDateFormat();

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

            } else if (dataType == FieldDataType.DATA_TYPE_NUMBER || dataType == FieldDataType.DATA_TYPE_INTEGER) {
                if (null != value && !"".equals(value)) {

                    if (value instanceof Integer) {
                        obj = new BigDecimal((Integer) value);

                    } else if (value instanceof Long) {
                        obj = new BigDecimal((Long) value);

                    } else if (value instanceof Number) {
                        obj = BigDecimal.valueOf(((Number) value).doubleValue()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
                    }
                } else {
                    obj = new BigDecimal(0);
                }

            } else if (dataType == FieldDataType.DATA_TYPE_MONEY) {
                if (null != value && !"".equals(value)) {
                    obj = moneyService.parseMoney((String) value, null);
                } else {
                    Money money = new MoneyBuilder().setNonBaseAmountvalue(BigDecimal.ZERO.toString()).getMoney();
                    money.getBaseAmount().setCurrency(Money.getBaseCurrency().getCurrencyCode());
                    obj = money;
                }

            } else if (dataType == FieldDataType.DATA_TYPE_TEXT_REFERENCE) {
                if (null != value && !"".equals(value)) {
                    String val = (String) value;
                    obj = formService.loadReference(EntityId.fromUri((val).substring(val.indexOf("{") + 1,
                            val.lastIndexOf("}"))));
                }

            } else if (dataType == FieldDataType.DATA_TYPE_PHONE) {
                JSONObject getPhoneJSONObject = (JSONObject) value;
                getPhoneJSONObject.remove("class");
                PhoneNumberVO phoneNumberVO = new ObjectMapper().readValue(getPhoneJSONObject.toString(),
                        PhoneNumberVO.class);
                return phoneNumberVO;
            } else if (dataType == FieldDataType.DATA_TYPE_LOV) {
                JSONObject getLOVFieldJSONObject = (JSONObject) value;
                getLOVFieldJSONObject.remove("class");
                LOVFieldVO lovFieldVO = new ObjectMapper().readValue(getLOVFieldJSONObject.toString(),
                        LOVFieldVO.class);
                return formService.loadLOVFieldValueOrReference(lovFieldVO);
            }

        } catch (Exception e) {
            BaseLoggers.flowLogger.debug("Exception occured while reading Json Object :: for Key :: " + key
                    + " :: and value :: " + value);
            BaseLoggers.flowLogger.info("Exception occured while reading Json Object :: for Key :: " + key
                    + " :: and value :: " + value);

            BaseLoggers.flowLogger.info(e.getMessage());
            BaseLoggers.flowLogger.debug(e.getMessage());
        }

        return obj;
    }

    /**
     * 
     * get specific Object value
     * These will be later serialized
     * @param fieldKey
     * @param value
     * @return
     */
    private Object createJsonObjectVal(String fieldKey, Object value, List<FieldMetaData> fields) {
        List<Object> oldValuesList = null;

        if (value instanceof java.util.List) {
            oldValuesList = (List<Object>) value;
        } else {
            oldValuesList = new ArrayList<>();
            oldValuesList.add(value);
        }

        List<Object> newValuesList = new ArrayList<>();

        Object obj = null;

        FieldMetaData fieldMetaData = getFieldDataType(fieldKey, fields);
        int dataType = null != fieldMetaData ? fieldMetaData.getDataType() : 0;

        if (dataType != 0) {


                for (Object object : oldValuesList) {

                    if (null != object) {
                   obj=convertObjectValToSave(dataType,object);
                     

                        newValuesList.add(obj);
                    }
                }
            

        }
        if (value instanceof java.util.List) {
            return newValuesList;
        }

        return obj;
    }
    
 

	private Object convertObjectValToSave(int dataType, Object object) {
    	Object obj;
    	if (dataType == FieldDataType.DATA_TYPE_DATE) {

            obj = ((DateTime) object).getMillis();

        } else if (dataType == FieldDataType.DATA_TYPE_TEXT_REFERENCE) {
            obj = "{" + ((Entity) object).getUri() + "}";

        } else {
            obj = object;
        }
		return  obj;
	}

	private FieldMetaData getFieldDataType(String fieldKey, List<FieldMetaData> fields) {

        for (FieldMetaData fieldMetaData : fields) {
            if (fieldKey.equals(fieldMetaData.getFieldKey())) {
                return fieldMetaData;
            }
        }
        return null;
    }

    /**
     * Not used 
     */

    /**
     * 
     * Method to get the data type of the field.
     * @param fieldKey
     * @param id
     * @return
     */
    private int getFieldDataType(String fieldKey, Long id) {
        int dataType = 0;

        NamedQueryExecutor<ModelMetaData> modelMetaExecutor = new NamedQueryExecutor<ModelMetaData>(
                "dynamicForm.loadFieldMetaaDataByFieldKey").addParameter("id", id).addParameter("fieldKey", fieldKey);

        List<ModelMetaData> modelMetaDataList = entityDao.executeQuery(modelMetaExecutor);

        if (null != modelMetaDataList && modelMetaDataList.size() > 0) {

            ModelMetaData modelMetaData = modelMetaDataList.get(0);
            if (null != modelMetaData.getFields() && modelMetaData.getFields().size() > 0) {

                List<FieldMetaData> fieldMetaDataList = modelMetaData.getFields();

                for (FieldMetaData fieldMetaData : fieldMetaDataList) {
                    if (fieldMetaData.getFieldKey().equals(fieldKey)) {
                        return fieldMetaData.getDataType();
                    }
                }
            }
        }

        return dataType;
    }
    
    public Map<String, Object> loadDynamicFormDataMap(DynamicForm dynamicFormObj, boolean ruleExecution) {
        Map<String, Object> contextMap = new HashMap<String, Object>();
        JSONObject jsonObject = null;

        try {
            if (notNull(dynamicFormObj) && notNull(dynamicFormObj.getDataJsonString())) {
                    jsonObject = new JSONObject(dynamicFormObj.getDataJsonString());
                    return getDynamicFormMap(jsonObject, dynamicFormObj, ruleExecution);
            
            }
        } catch (Exception e) {
            throw new SystemException("Unable to read JSOn Object", e);
            // TODO : Handle Exception
        }
        return contextMap;
    }
    
    private Map getDynamicFormMap(JSONObject object, DynamicForm dynamicFormobj, boolean ruleExecution) {
        Map<String, Object> map = new HashMap<String, Object>();

        Object jsonObject = null;
        String key = null;
        Object value = null;

        try {
        	ModelMetaData modelMetaData = dynamicFormobj.getModelMetaData();
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {

                key = null;
                value = null;

                key = keys.next();

                if (null != key && !object.isNull(key)) {
                    value = object.get(key);
                }

                if (value instanceof JSONObject) {
                    Object obj = convertFromJsonToActual(key, value, modelMetaData.getFields(), map);

                    if (!(obj instanceof Boolean)) {
                        map.put(key, getDynamicFormMap((JSONObject) value, dynamicFormobj, ruleExecution));
                    }
                    continue;
                }

                if (value instanceof JSONArray) {
                    JSONArray array = ((JSONArray) value);
                    List list = new ArrayList();
                    for (int i = 0 ; i < array.length() ; i++) {
                        jsonObject = array.get(i);
                        if (jsonObject instanceof JSONObject) {
                            list.add(getDynamicFormMap((JSONObject) jsonObject, dynamicFormobj, ruleExecution));
                        } else {
                            if (dynamicFormobj.getModelMetaData() != null
                                    && modelMetaData.getFields() != null) {

                                if (!ruleExecution) {
                                    list.add(convertFromJsonToActual(key, jsonObject, modelMetaData.getFields(), map));
                                } else {
                                    list.add(convertFromJsonToActualForRule(key, jsonObject, modelMetaData.getFields()));
                                }
                            }
                        }
                    }
                    map.put(key, list);
                    continue;
                }

                if (dynamicFormobj.getModelMetaData() != null
                        && modelMetaData.getFields() != null && null != value) {

                    if (!ruleExecution) {
                        map.put(key,
                                convertFromJsonToActual(key, value, modelMetaData.getFields(), map));
                    } else {
                        map.put(key,
                                convertFromJsonToActualForRule(key, value, modelMetaData.getFields()));
                    }
                }
            }
        } catch (Exception e) {
            BaseLoggers.flowLogger.debug("Exception occured while reading Json Object :: for Key :: " + key
                    + " :: and value :: " + value);
            BaseLoggers.flowLogger.info("Exception occured while reading Json Object :: for Key :: " + key
                    + " :: and value :: " + value);

            BaseLoggers.flowLogger.info(e.getMessage());
            BaseLoggers.flowLogger.debug(e.getMessage());
        }
        return map;
    }
    
    public Map<String, Object> saveDynamicFormDataMap(Map<String, Object> contextMap,ModelMetaData modelMetaDataObj) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
       
       /* ModelMetaData modelMetaDataObj=null;
        	String modelUri=uiMetaDataVo.getModelUri();
        	if(isNotEmpty(modelUri)){
    			Object object = entityDao.get(EntityId.fromUri(modelUri));
    			if(object instanceof ModelMetaData){
    				modelMetaDataObj=(ModelMetaData)object;
    			}
        	}*/

        String key = null;
        Object value = null;

        String key1 = null;
        Object value1 = null;

        List<Map<String, Object>> tablePanelDataMapList = null;
        List<Map<String, Object>> newTablePanelDataMapList = null;

        Map<String, Object> actualPanelMapData = null;
        Map<String, Object> newActualPanelMapData = null;

        List<Object> tableFieldDataMapList = null;

        if (null != contextMap) {
            for (Entry<String, Object> entry : contextMap.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();

                if (value instanceof java.util.Map) {
                    // It is actual panel
                    actualPanelMapData = (Map<String, Object>) value;
                    newActualPanelMapData = new HashMap<String, Object>();

                    for (Entry<String, Object> entryPanelKeys : actualPanelMapData.entrySet()) {
                        key1 = entryPanelKeys.getKey();
                        value1 = entryPanelKeys.getValue();
                        newActualPanelMapData.put(key1,
                                createJsonObjectVal(key1, value1,modelMetaDataObj.getFields()));

                        jsonMap.put(key, newActualPanelMapData);
                    }

                } else if (value instanceof java.util.List) {
                    // if tabular panel
                    // List of Maps

                    List<Object> objectList = (List<Object>) value;
                    if (null != objectList && objectList.size() > 0) {

                        if (objectList.get(0) instanceof java.util.Map) {

                            tablePanelDataMapList = (List<Map<String, Object>>) value;

                            newTablePanelDataMapList = new ArrayList<Map<String, Object>>();

                            for (Map<String, Object> tablePanelMap : tablePanelDataMapList) {

                                newActualPanelMapData = new HashMap<String, Object>();

                                for (Entry<String, Object> entryPanelKeys : tablePanelMap.entrySet()) {

                                    key1 = entryPanelKeys.getKey();
                                    value1 = entryPanelKeys.getValue();
                                    newActualPanelMapData.put(
                                            key1,
                                            createJsonObjectVal(key1, value1, modelMetaDataObj
                                                    .getFields()));
                                }
                                newTablePanelDataMapList.add(newActualPanelMapData);
                            }
                            jsonMap.put(key, newTablePanelDataMapList);

                        } else {
                            // if list of objects

                            tableFieldDataMapList = (List<Object>) value;

                            jsonMap.put(
                                    key,
                                    createJsonObjectVal(key, tableFieldDataMapList, modelMetaDataObj
                                            .getFields()));
                        }
                    }

                } else {
                    jsonMap.put(key, createJsonObjectVal(key, value, modelMetaDataObj.getFields()));
                }
            }
        }
        return jsonMap;
    }

    public void mergeDynamicFormDataMap(Map<String, Object> formJsonDataMap,
			Map<String, Object> mergedFormData, UIMetaData uiMetaData, ModelMetaData modelMetaData,
			Set<String> allowedKeys,boolean doValidate)
    {
    	
    	List<PanelDefinition> panelDefinitions=uiMetaData.getPanelDefinitionList();
    	for(PanelDefinition panelDefinition: panelDefinitions)
    	{
    		mergePanelFieldData(panelDefinition,formJsonDataMap,mergedFormData,modelMetaData,allowedKeys,doValidate);
    	}
		/*  String key = null;
	        Object value = null;

	        String key1 = null;
	        Object value1 = null;

	        List<Map<String, Object>> tablePanelDataMapList = null;
	        List<Map<String, Object>> newTablePanelDataMapList = null;

	        Map<String, Object> actualPanelMapData = null;
	        Map<String, Object> newActualPanelMapData = null;
	        List<Object> tableFieldDataMapList = null;*/

	      /*  if (null != contextMap) {
	            for (Entry<String, Object> entry : contextMap.entrySet()) {
	                key = entry.getKey();
	                value = entry.getValue();

	                if (value instanceof java.util.Map) {
	                    // It is actual panel
	                    actualPanelMapData = (Map<String, Object>) value;
	                    newActualPanelMapData = new HashMap<String, Object>();
	                    boolean isPanel=true==allowedKeysPanMap.get(key)?true:false;
	                    for (Entry<String, Object> entryPanelKeys : actualPanelMapData.entrySet()) {
	                        key1 = entryPanelKeys.getKey();
	                        value1 = entryPanelKeys.getValue();
	                        addToMap(newActualPanelMapData,key1,createJsonObjectVal(key1, value1,modelMetaDataObj.getFields()),allowedKeysPanMap,!isPanel);
	                    }
	                    addToMap(jsonMap,key,newActualPanelMapData,allowedKeysPanMap,false);

	                } else if (value instanceof java.util.List) {
	                    // if tabular panel
	                    // List of Maps

	                    List<Object> objectList = (List<Object>) value;
	                    if (null != objectList && objectList.size() > 0) {
	                    
	                        if (objectList.get(0) instanceof java.util.Map) {
	                        	//Table Data
	                            tablePanelDataMapList = (List<Map<String, Object>>) value;

	                            newTablePanelDataMapList = new ArrayList<Map<String, Object>>();

	                            for (Map<String, Object> tablePanelMap : tablePanelDataMapList) {

	                                newActualPanelMapData = new HashMap<String, Object>();

	                                for (Entry<String, Object> entryPanelKeys : tablePanelMap.entrySet()) {

	                                    key1 = entryPanelKeys.getKey();
	                                    value1 = entryPanelKeys.getValue();
	                                    addToMap(newActualPanelMapData,key1,createJsonObjectVal(key1, value1, modelMetaDataObj
                                                .getFields()),allowedKeysPanMap,false);  
	                                }
	                                newTablePanelDataMapList.add(newActualPanelMapData);
	                            }
	                            jsonMap.put(key, newTablePanelDataMapList);

	                        } else {
	                            // if list of objects

	                            tableFieldDataMapList = (List<Object>) value;
	                            addToMap(jsonMap,key,createJsonObjectVal(key, tableFieldDataMapList, modelMetaDataObj
                                        .getFields()),allowedKeysPanMap,false);
	                        }
	                    }

	                } else {
	                   
	                    addToMap(jsonMap,key,createJsonObjectVal(key, value, modelMetaDataObj.getFields()),allowedKeysPanMap,false);
	                }
	            }
	        }*/
	      
		
	}
	private void mergePanelFieldData(PanelDefinition panelDefinition, Map<String, Object> formJsonDataMap, Map<String, Object> mergedFormData,
			ModelMetaData modelMetaData, Set<String> allowedKeys,boolean doValidate) {
		
		
		if(notNull(panelDefinition.getPanelType()) && panelDefinition.getPanelType()==FormContainerType.FIELD_TYPE_PANEL){

			String panelKey=panelDefinition.getPanelKey();
			List<FieldDefinition> fieldDefinitions=panelDefinition.getFieldDefinitionList();
			Map<String, Object> panelFieldsMap=(Map<String, Object>) formJsonDataMap.get(panelKey);
			if(panelFieldsMap==null)
			{
				panelFieldsMap=new HashMap<String, Object>();
				formJsonDataMap.put(panelKey, panelFieldsMap);
			}
			if(ValidatorUtils.hasElements(fieldDefinitions))
			{
			    Map<String,FieldDefinition> fieldDefinitionMap = new HashMap<>();
                fieldDefinitions.forEach(fieldDefinition -> fieldDefinitionMap.put(fieldDefinition.getFieldKey(),fieldDefinition));
				for(FieldDefinition fieldDefinition:fieldDefinitions)
				{
					updateFieldData(fieldDefinition,panelFieldsMap,mergedFormData,allowedKeys,fieldDefinitionMap,doValidate);
				}
			} 
		}
		else if(notNull(panelDefinition.getPanelType()) && panelDefinition.getPanelType()==FormContainerType.FIELD_TYPE_VIRTUAL){
			updateJsonMapForDefinitionTypeVirtual(panelDefinition,mergedFormData,formJsonDataMap,allowedKeys,doValidate);
		}
		else if(notNull(panelDefinition.getPanelType()) && panelDefinition.getPanelType()==FormContainerType.FIELD_TYPE_TABLE){
			updateJsonMapForDefinitionTypeTable(panelDefinition,mergedFormData,formJsonDataMap,allowedKeys,doValidate);
		}
		
		
		
	}

	private void updateJsonMapForDefinitionTypeVirtual(PanelDefinition panelDefinition,
			Map<String, Object> mergedFormData, Map<String, Object> formJsonDataMap, Set<String> allowedKeys,boolean doValidate)
     {
	
		
		List<FieldDefinition> fieldDefinitions=panelDefinition.getFieldDefinitionList();
		
		if(ValidatorUtils.hasElements(fieldDefinitions))
		{
            Map<String,FieldDefinition> fieldDefinitionMap = new HashMap<>();
            fieldDefinitions.forEach(fieldDefinition -> fieldDefinitionMap.put(fieldDefinition.getFieldKey(),fieldDefinition));
            for(FieldDefinition fieldDefinition:fieldDefinitions)
			{
				updateFieldData(fieldDefinition,formJsonDataMap,mergedFormData,allowedKeys,fieldDefinitionMap,doValidate);
			}
		} 
  
		
	}

	private void updateJsonMapForDefinitionTypeTable(PanelDefinition panelDefObj,
			Map<String, Object> mergedFormData, Map<String, Object> formJsonDataMap, Set<String> allowedKeys,boolean doValidate)
    {
		
		
        String panelKey=panelDefObj.getPanelKey();
        if(!mergedFormData.containsKey(panelKey))
        {
        	return;
        }
        Object tableObj=mergedFormData.get(panelKey);
        if(!(tableObj  instanceof List))
        {
        	BaseLoggers.flowLogger.info("Expected Table data but for "+panelKey);
        	return ;
        }
        List<Object> tableData=(List<Object>)mergedFormData.get(panelKey);
        List<Map<String, Object>> tablePanelDataMapList=(List<Map<String, Object>>) formJsonDataMap.get(panelKey);
        if(tablePanelDataMapList==null)
        {
        	tablePanelDataMapList = new ArrayList<Map<String, Object>>();
        	formJsonDataMap.put(panelKey, tablePanelDataMapList);
        }
		
        List<FieldDefinition> fieldDefinitionList = panelDefObj.getFieldDefinitionList();

        for(Object tableRowObject:tableData)
        {
        	Map<String, Object> tableRowSourceMap=(Map<String, Object>) tableRowObject;
        	Map<String, Object> tableFieldRowMap=new HashMap<String, Object>();
        	for(FieldDefinition fieldDefinition:fieldDefinitionList)
    		{
    			updateFieldData(fieldDefinition,tableFieldRowMap,tableRowSourceMap,allowedKeys,null,doValidate);
    		}
        	tablePanelDataMapList.add(tableFieldRowMap);
      
        } 
        
	}

	private void updateFieldData(FieldDefinition fieldDef, Map<String, Object> panelFieldsMap,
			Map<String, Object> mergedFormData, Set<String> allowedKeys,Map<String, FieldDefinition> fieldDefinitionMap,  boolean doValidate)  {
		
			String fieldKey=fieldDef.getFieldKey();
			if(ValidatorUtils.hasElements(allowedKeys) && !allowedKeys.contains(fieldKey))
			{//Filtering fields as per filter
				return;
			}
			if(!mergedFormData.containsKey(fieldKey) && !doValidate)
			{
				return ;
			}
			Object mapValue= mergedFormData.get(fieldKey);
	
			if(notNull(fieldKey) ){
			    if(doValidate){
			        dynamicFormValidator.validateAndUpdateField(mapValue,fieldDef,panelFieldsMap,fieldDefinitionMap,mergedFormData);
			        return;
                }
				int fieldDataType=fieldDef.getFieldDataType();
				if (fieldDataType != 0 && fieldDataType == FieldDataType.DATA_TYPE_TEXT_REFERENCE) {
					updateAndCallBackForDataTypeReferenceConversion(fieldDef,mapValue,panelFieldsMap,fieldKey,doValidate);
	        	}
	        	else if(fieldDataType != 0 && fieldDataType==FieldDataType.DATA_TYPE_DATE){
	        		updateValueInJsonMapForDataTypeDate(mapValue,panelFieldsMap,fieldKey);
	        	}
	        	else if(fieldDataType != 0 && fieldDataType==FieldDataType.DATA_TYPE_MONEY){
	        		updateValueInJsonMapForDataTypeMoney(mapValue,panelFieldsMap,fieldKey);
	        	}else if(fieldDataType !=0 && fieldDataType==FieldDataType.DATA_TYPE_PHONE){
	        		updateValueInJsonMapForDataTypePhone(mapValue,panelFieldsMap,fieldKey);
                }else if(fieldDataType !=0 && fieldDataType==FieldDataType.DATA_TYPE_LOV){
                    updateValueInJsonMapForDataTypeLOV(mapValue,panelFieldsMap,fieldKey);
	        	}else if (fieldDataType == FieldDataType.DATA_TYPE_TEXT_BOOLEAN) {
	        		panelFieldsMap.put(fieldKey, mapValue);
	            }else if (fieldDataType == FieldDataType.DATA_TYPE_TEXT) {
	            	panelFieldsMap.put(fieldKey, mapValue);

	            }else if (fieldDataType == FieldDataType.DATA_TYPE_NUMBER || fieldDataType == FieldDataType.DATA_TYPE_INTEGER) {
	            	Object object =null;
	            	
	                if (null != mapValue ) {
	                	if((mapValue instanceof String) && StringUtils.isNotBlank( (String)mapValue))
	                	{
	                		object = new BigDecimal((String)mapValue);
	                	}
	                	else if(mapValue instanceof Number) 
	                	{
	                		object = new BigDecimal(String.valueOf(mapValue));
	                	}

	                } else {
	                	object = new BigDecimal(0);
	                }
	                panelFieldsMap.put(fieldKey, object);
	            } 
			}			
			
	}

	
	private void addToMap(Map<String, Object> jsonMap,String key,Object value, Map<String,Boolean> allowedKeysPanMap,boolean noFilter)
	{
		if(noFilter)
		{
			jsonMap.put(key, value);
			return;
		}
		if(ValidatorUtils.hasAnyEntry(allowedKeysPanMap) && allowedKeysPanMap.containsKey(key))
		{
			jsonMap.put(key, value);
		}
	}

	
}
