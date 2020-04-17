package com.nucleus.core.dynamicform.service;

import com.nucleus.core.dynamicform.exception.InvalidDynamicFormDataException;
import com.nucleus.core.dynamicform.vo.DynamicFormDataVO;
import com.nucleus.core.formsConfiguration.IDynamicForm;

import java.util.List;
import java.util.Map;

/**
 * @author gajendra.jatav
 */

public interface IDynamicFormFilterService {


    /**
     * @param dynamicFormsList
     * @param serviceIdentifierCode
     * @param fields
     * @return
     */
    public Map<String, String> getFieldWiseFilteredFormData(List<IDynamicForm> dynamicFormsList, String serviceIdentifierCode, String... fields);

    /**
     * Deprecated use validateAndUpdateDynamicFormData instead.
     * @param dynamicFormsList
     * @param dynamicFormDataList
     * @param serviceIdentifierCode
     * @param placeHolderCode
     */
    @Deprecated
    public void filterDynamicFormDataAndUpdateEntities(List<IDynamicForm> dynamicFormsList, List<String> dynamicFormDataList, String serviceIdentifierCode, String... placeHolderCode);


    /**
     * @param dynamicFormsList
     * @param dynamicFormDataList
     * @param serviceIdentifierCode
     * @param placeHolderCode
     */
    public void validateAndUpdateDynamicFormData(List<IDynamicForm> dynamicFormsList, List<DynamicFormDataVO> dynamicFormDataList, String serviceIdentifierCode, String... placeHolderCode);

}
