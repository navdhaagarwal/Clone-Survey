package com.nucleus.web.formDefinition;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.dynamicform.service.DynamicFormUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;

import com.nucleus.core.dynamicform.service.FormConfigurationMappingService;
import com.nucleus.core.dynamicform.service.FormService;
import com.nucleus.core.formsConfiguration.FieldCustomOptions;
import com.nucleus.core.formsConfiguration.FieldCustomOptionsVO;
import com.nucleus.core.formsConfiguration.FieldDataType;
import com.nucleus.core.formsConfiguration.FieldDefinition;
import com.nucleus.core.formsConfiguration.FormComponentType;
import com.nucleus.core.formsConfiguration.FormComponentVO;
import com.nucleus.core.formsConfiguration.FormContainerType;
import com.nucleus.core.formsConfiguration.FormFieldVO;
import com.nucleus.core.formsConfiguration.PanelDefinition;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.core.formsConfiguration.UIMetaDataVo;
import com.nucleus.core.formsConfiguration.fieldcomponent.EmailInfoVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.web.common.controller.BaseController;

/**
 * 
 * @author Nucleus Software Exports Limited
 * formConfigurationProcessor Implementation
 */
public class BaseDynamicFormController extends BaseController {

    @Inject
    @Named("formConfigService")
    protected FormService                     formService;

    @Inject
    @Named("formConfigurationMappingService")
    protected FormConfigurationMappingService formConfigurationMappingService;

    /**
     * 
     * Method to populate the UIMetaDataVo Object.
     * This is used to render the UI
     * @param uiMetaData
     * @param persistentFormData
     * @return
     */

    public UIMetaDataVo mergeFormDetailsAndData(UIMetaData uiMetaData, Map<String, Object> dataMap) {
        return DynamicFormUtil.mergeFormDetailsAndData(uiMetaData,dataMap);
    }

    /**
     * 
     * Remove comma from the amount
     * @param text
     * @return
     * @throws ParseException
     */
    protected String parse(String text) {
        String newStr = text.replaceAll("[^\\d.]+", "");
        return newStr;
    }

    /**
     * 
     * Method to set the default date based on user selection
     * @param fieldDefinition
     * @param formFieldVO
     */
}
