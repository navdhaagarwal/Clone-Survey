package com.nucleus.web.formDefinition;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Hibernate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.nucleus.core.dynamicform.service.FormDefinitionService;
import com.nucleus.core.formsConfiguration.FieldMetaData;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.persistence.EntityDao;
import com.nucleus.web.common.controller.BaseController;

@Transactional
@Controller
@RequestMapping(value = "/ModelMetaData")
@SessionAttributes("ModelMetaData")
public class ModelMetaDataController extends BaseController {

    @Inject
    @Named("formDefinitionService")
    FormDefinitionService formDefinitionService;

    @Inject
    @Named("entityDao")
    private EntityDao     entityDao;

    private static final String  MASTER_ID = "ModelMetaData";

    /**
     * @Description
     * Create ModelMetaData
     * 
     * @param map
     * @return jsp to create ModelMetaData
     */
    @PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
    @RequestMapping(value = "/create")
    public String createFormDefinition(ModelMap map) {
        map.put("ModelMetaData", new ModelMetaData());
        map.put("masterId", MASTER_ID);
        map.put("dynamicDataFieldType", formDefinitionService.getDynamicFormFieldDataType());
        map.put("typeSize", 0);
        return "modelMetaData";
    }

    /**
     * @Description
     * Save ModelMetaData
     * 
     * @return jsp to show ModelMetaData Grid
     */
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/save")
    public String saveDynamicFormFields(@ModelAttribute("ModelMetaData") ModelMetaData modelMetaData, BindingResult result,
            ModelMap map, @RequestParam("deletedEntries") String[] deletedEntries) {

        List<FieldMetaData> fieldMetaDataList = deleteFormMetaData(modelMetaData.getFields(), deletedEntries);

        List<FieldMetaData> fieldMetaDataListNew = setModelFieldKey(fieldMetaDataList, "");

        modelMetaData.setFields(fieldMetaDataListNew);

        entityDao.saveOrUpdate(modelMetaData);
        return "redirect:/app/ModelMetaDataGrid/ModelMetaData/ModelMetaData/loadColumnConfig";
    }

    /**
     * @Description
     * view ModelMetaData
     * 
     * @return jsp to view ModelMetaData
     */
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG')")
    @RequestMapping(value = "/view/{id}")
    public String viewDynamicFormFields(@PathVariable("id") Long id, ModelMap map) {
        getDataForEditViewMode(id, map);
        map.put("viewable", true);
        return "modelMetaData";
    }

    /**
     * @Description
     * edit ModelMetaData
     * 
     * @return jsp to edit ModelMetaData
     */
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/edit/{id}")
    public String editDynamicFormFields(@PathVariable("id") Long id, ModelMap map) {
        getDataForEditViewMode(id, map);
        map.put("edit", true);
        return "modelMetaData";
    }

    /**
     * @Description
     * delete ModelMetaData(s)
     * 
     * @return jsp to show ModelMetaData Grid
     */
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/delete/{id}")
    public String deleteDynamicFormFields(@PathVariable("id") String ids, ModelMap map) {

        String[] idsToDelete = ids.split(",");
        for (int i = 0 ; i < idsToDelete.length ; i++) {
            Long id = Long.parseLong(idsToDelete[i]);
            ModelMetaData modelMetaData = entityDao.find(ModelMetaData.class, id);
            entityDao.delete(modelMetaData);
        }

        return "redirect:/app/grid/FormConfigurationMapping/FormConfigurationMapping/loadColumnConfig";
    }

    /**
     * @Description
     * common code for view and edit function
     * 
     */
    private void getDataForEditViewMode(Long id, ModelMap map) {

        ModelMetaData modelMetaData = entityDao.find(ModelMetaData.class, id);
        Hibernate.initialize(modelMetaData.getFields());
        map.put("ModelMetaData", modelMetaData);
        map.put("id", id);
        map.put("masterId", MASTER_ID);
        map.put("dynamicDataFieldType", formDefinitionService.getDynamicFormFieldDataType());
        map.put("typeSize", modelMetaData.getFields().size());
    }

    /**
     * @Description
     * removing the deleted portion from the object of ModelMetaData before saving
     * 
     */
    private List<FieldMetaData> deleteFormMetaData(List<FieldMetaData> fieldMetaData, String[] deletedEntries) {

        if (null != deletedEntries && deletedEntries.length > 0) {

            Iterator<FieldMetaData> itr = fieldMetaData.iterator();

            while (itr.hasNext()) {
                FieldMetaData formDefinition = itr.next();
                Long actualId = formDefinition.getId();

                if (null != deletedEntries && null != actualId && deletedEntries.length > 0) {
                    for (int i = 0 ; i < deletedEntries.length ; i++) {
                        if (!deletedEntries[i].equals("") && actualId.equals(Long.valueOf(deletedEntries[i]))) {
                            itr.remove();
                            break;
                        }
                    }
                }
            }
        }
        return fieldMetaData;
    }

    /**
     * Method to set the field key
     */

    private List<FieldMetaData> setModelFieldKey(List<FieldMetaData> fieldMetaDataList, String name) {

        if (null != fieldMetaDataList) {
            for (FieldMetaData fieldMetaData : fieldMetaDataList) {
                fieldMetaData.setFieldKey(fieldMetaData.getName());
            }
        }
        return fieldMetaDataList;
    }
}
