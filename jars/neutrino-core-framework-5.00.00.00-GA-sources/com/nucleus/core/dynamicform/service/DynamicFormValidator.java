package com.nucleus.core.dynamicform.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.ibm.icu.math.BigDecimal;
import com.nucleus.core.dynamicform.exception.InvalidDynamicFormDataException;
import com.nucleus.core.formsConfiguration.FieldCustomOptions;
import com.nucleus.core.formsConfiguration.FieldDataType;
import com.nucleus.core.formsConfiguration.FieldDefinition;
import com.nucleus.core.formsConfiguration.FormComponentType;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.master.CriteriaMapVO;
import com.nucleus.money.MoneyService;
import com.nucleus.service.BaseServiceImpl;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Named;
import java.beans.PropertyDescriptor;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.nucleus.core.dynamicform.exception.InvalidDynamicFormDataException.ERROR_CODE;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;


/**
 * Created by gajendra.jatav on 8/30/2019.
 */
@Named("dynamicFormValidator")
public class DynamicFormValidator extends BaseServiceImpl {

    private static final String PHONE_NUMBER_DELIMETER = " ";

    @Named("dynamicFormDateFormatProvider")
    @Inject
    private DynamicFormDateFormatProvider dynamicFormDateFormatProvider;

    public static void throwValidationException(InvalidDynamicFormDataException.ERROR_CODE errorCode, String message, boolean doValidate,String fieldKey){
        if (doValidate) {
            throw ExceptionBuilder.getInstance(InvalidDynamicFormDataException.class, errorCode.getMessageCode(), message)
                    .setMessage(CoreUtility.prepareMessage(errorCode.getMessageCode(),fieldKey))
                    .setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
        }
    }

    public static void throwValidationException(InvalidDynamicFormDataException.ERROR_CODE errorCode, String message, Exception e, boolean doValidate,String fieldKey){
        if (doValidate) {
            throw ExceptionBuilder.getInstance(InvalidDynamicFormDataException.class, errorCode.getMessageCode(), message)
                    .setOriginalException(e)
                    .setMessage(CoreUtility.prepareMessage(errorCode.getMessageCode(),fieldKey))
                    .setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
        }
    }


    public static final String FALSE = "false";
    public static final String TRUE = "true";
    public static final String ZERO = "0";

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    @Inject
    @Named("nonTransactionalMoneyService")
    private MoneyService nonTransactionalMoneyService;

    public void validateAndUpdateField(Object value, FieldDefinition fieldDefinition,
                                       Map<String, Object> targetDynamicFieldValueMap,
                                       Map<String, FieldDefinition>
                                               fieldDefinitionMap,
                                       Map<String, Object> fieldKeyValueMap)
            {

        if (fieldDefinition.getDisable()!=null && fieldDefinition.getDisable()){
            return;
        }
        this.basicValidationsForDynamicFormFields(fieldDefinition, value);
        if (null != value) {

            String fieldType = fieldDefinition.getFieldType();

            switch (fieldType) {

                case "AutoComplete":
                case "DropDown":
                case "CascadedSelect":
                case "MultiSelectBox":
                case "Radio":
                    validateValueExistenceIntoSystem(value, fieldDefinition,
                            targetDynamicFieldValueMap, fieldKeyValueMap, fieldDefinitionMap);
                    break;
                case "Calendar":
                    validateCalendarField(value, fieldDefinition, targetDynamicFieldValueMap);
                    break;
                case "CheckBox":
                    if (!(TRUE.equalsIgnoreCase(value.toString()) || FALSE.equalsIgnoreCase(value.toString()))) {
                        throwValidationException(ERROR_CODE.INVALID_CHECKBOX_VALUE,"Invalid value for checkbox field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
                    } else {
                        targetDynamicFieldValueMap.put(fieldDefinition.getFieldKey(), value);
                    }
                    break;
                case "Money":
                    validateMoneyFieldValue(fieldDefinition, value.toString(), targetDynamicFieldValueMap);
                    break;
                case "Phone":
                    validateValueForPhoneTypeFields(value.toString(), fieldDefinition, targetDynamicFieldValueMap);
                    break;
                case "TextBox":
                case "TextArea":
                    targetDynamicFieldValueMap.put(fieldDefinition.getFieldKey(), value);
                    break;

                default:
            }
        }

    }

    private void validateMoneyFieldValue(FieldDefinition fieldDefinition, String value, Map<String, Object> targetDynamicFieldValueMap) {
        try {
            nonTransactionalMoneyService.parseMoney(value, null);
            targetDynamicFieldValueMap.put(fieldDefinition.getFieldKey(), value);
        } catch (Exception e) {
            throwValidationException(ERROR_CODE.INVALID_AMOUNT_FORMAT,
                    "Invalid amount format for field " + fieldDefinition.getFieldKey(), e, true,fieldDefinition.getFieldKey());
        }
    }

    private void validateValueForPhoneTypeFields(String value, FieldDefinition fieldDefinition,
                                                 Map<String, Object> targetDynamicFieldValueMap) {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        if (notNull(fieldDefinition.getMobile()) && fieldDefinition.getMobile()) { // If phone type is Mobile
            String[] mobileInputs = value.split(PHONE_NUMBER_DELIMETER);

            if (mobileInputs.length == 2) {
                String regionCode = mobileInputs[0]; // e.g. IN
                String mobileNumber = mobileInputs[1];

                boolean isValidMobileNum = isValidMobileNumber(regionCode, mobileNumber,
                        fieldDefinition, phoneUtil);
                if (!isValidMobileNum) {
                    throwValidationException(ERROR_CODE.INVALID_MOBILE_NUM,"Invalid mobile number for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
                }
                PhoneNumberVO phoneNumberVO = new PhoneNumberVO();
                phoneNumberVO.setCountryCode(regionCode);
                phoneNumberVO.setIsdCode(String.valueOf(phoneUtil.getCountryCodeForRegion(regionCode)));
                phoneNumberVO.setPhoneNumber(mobileNumber);
                targetDynamicFieldValueMap.put(fieldDefinition.getFieldKey(), phoneNumberVO);
            } else {
                throwValidationException(ERROR_CODE.INVALID_MOBILE_NUM_FORMAT,"Invalid mobile number format for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
            }
        } else { // If phone type is Phone
            String[] phoneInputs = value.split(PHONE_NUMBER_DELIMETER);
            if (phoneInputs.length == 4) {

                String regionCode = phoneInputs[0];
                String sTDCode = phoneInputs[1];
                String phoneNumber = phoneInputs[2];
                String extensionNumber = phoneInputs[3];

                boolean isRegionCodeValidForCountryCode = validateRegionCode(regionCode,
                        phoneUtil);
                if (isRegionCodeValidForCountryCode) {
                    if (sTDCode != null && sTDCode.length() > 4) {
                        throwValidationException(ERROR_CODE.INVALID_PHONE_NUM_STD_CODE,"Invalid STD code for phone number field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
                    }
                    if (phoneNumber != null && sTDCode.length() > 8) {
                        throwValidationException(ERROR_CODE.INVALID_PHONE_NUM_FORMAT,"Invalid Phone number format for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
                    }
                    if (extensionNumber != null && extensionNumber.length() > 6) {
                        throwValidationException(ERROR_CODE.INVALID_PHONE_NUM_EXT,"Invalid Phone number extension format for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
                    }
                }

                PhoneNumberVO phoneNumberVO = new PhoneNumberVO();

                phoneNumberVO.setCountryCode(regionCode);
                phoneNumberVO.setIsdCode(String.valueOf(phoneUtil.getCountryCodeForRegion(regionCode)));
                phoneNumberVO.setStdCode(sTDCode);
                phoneNumberVO.setPhoneNumber(phoneNumber);
                phoneNumberVO.setExtension(extensionNumber);
                targetDynamicFieldValueMap.put(fieldDefinition.getFieldKey(), phoneNumberVO);

            } else {
                throwValidationException(ERROR_CODE.INVALID_PHONE_NUM_FORMAT,"Invalid Phone number format for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
            }
        }

    }

    private boolean validateRegionCode(String regionCode,
                                       PhoneNumberUtil phoneUtil) {

        String countryCodeVal = String.valueOf(phoneUtil.getCountryCodeForRegion(regionCode));
        if (countryCodeVal == null || countryCodeVal.equals(ZERO)) {
            return false;
        }

        return true;
    }

    private boolean isValidMobileNumber(String regionCode, String mobileNumber, FieldDefinition fieldDefinition, PhoneNumberUtil phoneUtil) {
        com.google.i18n.phonenumbers.Phonenumber.PhoneNumber phoneNumberProto = null;

        try {

            boolean isRegionCodeValidForCountryCode = validateRegionCode(regionCode, phoneUtil);
            if (isRegionCodeValidForCountryCode) {
                phoneNumberProto = phoneUtil.parseAndKeepRawInput(mobileNumber, regionCode);
                return phoneUtil.isValidNumber(phoneNumberProto);
            }
        } catch (NumberParseException e) {
        }

        return false;
    }

    private void validateValueForCustomBinder(FieldDefinition fieldDefinition, List<FieldCustomOptions> customOptions, String value,
                                              Map<String, Object> targetValueMap, boolean isMultiselect) {
        if (customOptions != null && !customOptions.isEmpty()) {
            if (isMultiselect) {
                //Case not handled
            } else {
                checkAnyMatchWithCustomBinderValue(customOptions, value, fieldDefinition, targetValueMap);
            }
        }
    }

    private void checkAnyMatchWithCustomBinderValue(List<FieldCustomOptions> customOptions, String value, FieldDefinition fieldDefinition, Map<String, Object> targetValueMap) {
        if (customOptions.stream().anyMatch(item -> item.getCustomeItemValue().equals(value))) {
            targetValueMap.put(fieldDefinition.getFieldKey(), value);
        } else {
            throwValidationException(ERROR_CODE.VALUE_NOT_FOUND_IN_SYSTEM,"No value exists in system for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
        }

    }

    private void validateCalendarField(Object value, FieldDefinition fieldDefinition,
                                       Map<String, Object> targetDynamicFieldValueMap)
             {
        if (null != value && !"".equals(value.toString())) {
            String defaultSYSDateFormat = dynamicFormDateFormatProvider.getDateFormat();
            DateFormat formatter = new SimpleDateFormat(defaultSYSDateFormat);
            try {
                Date date = formatter.parse(value.toString());
                if (date != null && formatter.format(date).equals(value.toString())) {
                    validateDateRange(date, fieldDefinition);
                    DateTime calendar = new DateTime(date);
                    targetDynamicFieldValueMap.put(fieldDefinition.getFieldKey(), calendar.getMillis());
                } else {
                    throwValidationException(ERROR_CODE.INVALID_DATE_FORMAT,"Not able to parse date " + value + " expected format is " + defaultSYSDateFormat + " for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
                }
            } catch (ParseException e) {
                throwValidationException(ERROR_CODE.INVALID_DATE_FORMAT,"Not able to parse date " + value + " expected format is " + defaultSYSDateFormat + " for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
            }
        }
    }

    private void validateDateRange(Date date, FieldDefinition fieldDefinition) throws ParseException {

        String defaultSYSDateFormat = getSystemDateFormat();
        DateFormat formatter = new SimpleDateFormat(defaultSYSDateFormat);
        boolean isInvalid = false;
        if (StringUtils.isNotBlank(fieldDefinition.getMaxFieldValue())) {
            Date maxDate = formatter.parse(fieldDefinition.getMaxFieldValue());
            if (date.after(maxDate)) {
                isInvalid = true;
            }
        }

        if (StringUtils.isNotBlank(fieldDefinition.getMinFieldValue())) {
            Date minDate = formatter.parse(fieldDefinition.getMinFieldValue());
            if (date.before(minDate)) {
                isInvalid = true;
            }
        }

        if (isInvalid) {
            throwValidationException(ERROR_CODE.DATE_OUT_OF_ALLOWED_RANGE,"Date not in range as per allowed min, max date for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
        }
    }

    public void basicValidationsForDynamicFormFields(FieldDefinition fieldDefinition, Object inputValue) {

        if (inputValue != null && inputValue instanceof List) {
            //Validation for List type field like multiselect, if mandatory list should not be empty
            if (fieldDefinition.isMandatoryField() && ((List) inputValue).size() == 0) {
                throwValidationException(ERROR_CODE.MANDATORY_FIELD_MISSING,"Mandatory field missing for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
            }
            return;
        }
        if ((null == inputValue || StringUtils.isBlank(inputValue.toString())) && fieldDefinition.isMandatoryField()) {
            throwValidationException(ERROR_CODE.MANDATORY_FIELD_MISSING,"Mandatory field missing for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
        }
        if (null == inputValue){
            return;
        }
        String value = inputValue.toString();
        if (notNull(fieldDefinition.getMinFieldLength())
                && value.length() < fieldDefinition.getMinFieldLength()) {
            throwValidationException(ERROR_CODE.FIELD_VALUE_LESS_THAN_MIN,"Length of value provided for field is less than minimum length for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
        }

        if (notNull(fieldDefinition.getMaxFieldLength())
                && value.length() > fieldDefinition.getMaxFieldLength()) {

            throwValidationException(ERROR_CODE.FIELD_VALUE_MORE_THAN_MAX,"Length of value provided for field is greater than minimum length for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
        }

        if (fieldDefinition.getFieldType().equals(FormComponentType.TEXT_BOX)
                && (fieldDefinition.getFieldDataType() == FieldDataType.DATA_TYPE_INTEGER
                || fieldDefinition.getFieldDataType() == FieldDataType.DATA_TYPE_NUMBER)) {
            try {
                new BigDecimal(value);
            } catch (NumberFormatException numberFormatException) {
                throwValidationException(ERROR_CODE.INVALID_NUMBER_FORMAT,"Invalid number format for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
            }
        }
    }


    public void validateValueExistenceIntoSystem(Object value, FieldDefinition fieldDefinition,
                                                 Map<String, Object> targetFieldValues, Map<String, Object>
                                                         fieldKeyValueMap,
                                                 Map<String, FieldDefinition> fieldDefinitionMap)
            {

        String itemLabel = fieldDefinition.getItemLabel();
        String entityClassName = fieldDefinition.getEntityName();
        boolean isCascadeChild = false;
        boolean isMultiSelect = false;
        if (fieldDefinition.getFieldType().equalsIgnoreCase("MultiSelectBox")) {
            isMultiSelect = true;
        }
        if (fieldDefinition.getFieldType().equalsIgnoreCase("CascadedSelect")) {
            String parentFieldKey = fieldDefinition.getParentFieldKey();
            if (StringUtils.isNotEmpty(parentFieldKey)) {
                isCascadeChild = true;
            }
        }
        String binderName = fieldDefinition.getBinderName();
        if (FormConfigurationConstant.CUSTOM_BINDER.equals(binderName)) {
            List<FieldCustomOptions> customOptions = fieldDefinition.getFieldCustomOptionsList();
            validateValueForCustomBinder(fieldDefinition, customOptions, value.toString(), targetFieldValues, isMultiSelect);
            return;
        }

        try {
            Class entityClass = Class.forName(entityClassName);
            if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
                Map<String, Object> variableMap = new HashMap<String, Object>();
                CriteriaMapVO criteriaMapVO = new CriteriaMapVO();
                String[] multiSelectValues = null;

                if (isMultiSelect) {
                    multiSelectValues = getValues(value,fieldDefinition);
                    variableMap.put(itemLabel, Arrays.asList(multiSelectValues));
                } else {
                    variableMap.put(itemLabel, value);
                }
                variableMap.put("masterLifeCycleData.approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);

                criteriaMapVO.setEqualClauseCriteriaMap(variableMap);

                List<? extends BaseEntity> result = baseMasterService.findEntitiesByCriteria(entityClass, criteriaMapVO);

                if (result == null || result.isEmpty()) {
                    throwValidationException(ERROR_CODE.VALUE_NOT_FOUND_IN_SYSTEM,"No value exists in system for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
                } else if (isMultiSelect && multiSelectValues != null && result.size() != multiSelectValues.length) {
                    throwValidationException(ERROR_CODE.VALUE_NOT_FOUND_IN_SYSTEM,"No value exists in system for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
                } else if (isCascadeChild) {
                    String parentFieldValue = fieldKeyValueMap.get(fieldDefinition.getParentFieldKey()).toString();
                    FieldDefinition parentFieldDefinition = fieldDefinitionMap.get(fieldDefinition.getParentFieldKey());
                    String parentClassName = parentFieldDefinition.getEntityName();
                    Class parentClass = Class.forName(parentClassName);
                    for (BaseEntity object : result) {
                        String parentEntityAttributeName = getParentEntityAttributeName(object, parentClass);
                        Object parentObject = PropertyUtils.getProperty(object, parentEntityAttributeName);
                        String parentAttributeValue = String.valueOf(PropertyUtils.getProperty(parentObject, parentFieldDefinition.getItemLabel()));
                        if (parentAttributeValue != null && parentAttributeValue.equals(parentFieldValue)) {
                            targetFieldValues.put(fieldDefinition.getFieldKey(), object.getUri());
                        } else {
                            throwValidationException(ERROR_CODE.VALUE_NOT_FOUND_IN_SYSTEM,"Invalid value for cascade select with field key " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
                        }
                    }
                } else {
                    List<String> listValues = new ArrayList<>();
                    result.forEach(o -> listValues.add(o.getUri()));
                    if (isMultiSelect) {
                        targetFieldValues.put(fieldDefinition.getFieldKey(), listValues);
                    } else if (!listValues.isEmpty()) {
                        targetFieldValues.put(fieldDefinition.getFieldKey(), listValues.get(0));
                    }
                }
            }
        } catch (Exception e) {
            throwValidationException(ERROR_CODE.VALUE_NOT_FOUND_IN_SYSTEM,"No value exists in system for field " + fieldDefinition.getFieldKey(), true,fieldDefinition.getFieldKey());
        }
    }

    private String[] getValues(Object value, FieldDefinition fieldDefinition) {

        if (value instanceof java.util.List) {
            List<String> objectList = (List<String>) value;
            return objectList.toArray(new String[0]);
        } else {
            throwValidationException(ERROR_CODE.MULTIPLE_VALUES_EXPECTED,"Multiple values expected for multiselect", true,fieldDefinition.getFieldKey());
        }
        return new String[0];
    }

    private String getParentEntityAttributeName(Object object, Class parentClass) {
        String parentEntityAttributeName = "";

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(object.getClass());

        for (PropertyDescriptor descriptor : descriptors) {
            if (parentClass.equals(descriptor.getPropertyType())) {
                parentEntityAttributeName = descriptor.getName();
                break;
            }
        }

        return parentEntityAttributeName;
    }
}
