package com.nucleus.web.formDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.nucleus.core.dynamicform.service.FormDefinitionGridService;
import com.nucleus.core.dynamicform.service.FormDefinitionService;
import com.nucleus.core.dynamicform.service.FormService;
import com.nucleus.core.formsConfiguration.FieldDefinition;
import com.nucleus.core.formsConfiguration.FieldMetaData;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.PanelDefinition;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.persistence.EntityDao;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;

@Transactional
@Controller
@RequestMapping(value = "/UIMetaData")
@SessionAttributes("UIMetaData")
public class UiMetaDataController extends BaseController {

    @Inject
    @Named("formConfigService")
    private FormService               formService;

    @Inject
    @Named("formDefinitionGridService")
    private FormDefinitionGridService formDefinitionGridService;

    @Inject
    @Named("entityDao")
    private EntityDao                 entityDao;
    
    @Inject
    @Named("formDefinitionService")
    private FormDefinitionService      formDefinitionService;

    /**
     * 
     * Create Form Definition method
     * 
     * @param map
     * @return
     */

    private static final String              masterId = "UIMetaData";

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/create")
    public String createFormDefinition(ModelMap map) {
        map.put("UIMetaData", new UIMetaData());
        map.put("masterId", masterId);
        map.put("dynamicDataFieldType", formService.getFormComponentType());
        map.put("typeSize", 0);
        map.put("modelNameList", getModelNameList());
        return "uiMetaData";

    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/save")
    public String saveDynamicFormFields(@ModelAttribute("UIMetaData") UIMetaData uiMetaData, BindingResult result,
            ModelMap map, @RequestParam("deletedEntries") String[] deletedEntries,
            @RequestParam("deletedEntriesForPanels") String[] deletedEntriesForPanels) {

        uiMetaData = deleteSelectedItems(uiMetaData, deletedEntries, deletedEntriesForPanels);
        if (uiMetaData.getId() == null) {
            uiMetaData.getEntityLifeCycleData().setCreatedByUri(getUserDetails().getUserEntityId().getUri());
        } else {
            uiMetaData.getEntityLifeCycleData().setLastUpdatedByUri(getUserDetails().getUserEntityId().getUri());
        }
        entityDao.saveOrUpdate(uiMetaData);

        return "redirect:/app/UIMetaDataGrid/UIMetaData/UIMetaData/loadColumnConfig";
    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/edit/{id}")
    public String editDynamicFormFields(@PathVariable("id") Long id, ModelMap map) {

        getDataForEditViewMode(id, map);
        map.put("edit", true);
        return "uiMetaData";
    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/delete/{id}")
    public String deleteDynamicFormFields(@PathVariable("id") String ids, ModelMap map) {

        String[] idsToDelete = ids.split(",");
        for (int i = 0 ; i < idsToDelete.length ; i++) {
            Long id = Long.parseLong(idsToDelete[i]);
            id = Long.parseLong(idsToDelete[i]);
            if (ValidatorUtils.notNull(id)) {
                formDefinitionService.deleteDynamicForm(id);
            }
        }

        return "redirect:/app/UIMetaDataGrid/UIMetaData/UIMetaData/loadColumnConfig";
    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG')")
    @RequestMapping(value = "/view/{id}")
    public String viewDynamicFormFields(@PathVariable("id") Long id, ModelMap map) {

        getDataForEditViewMode(id, map);
        map.put("viewable", true);
        return "uiMetaData";
    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/getFieldOnBasisOfModelName/{modelName}", method = RequestMethod.GET)
    @ResponseBody
    public String getTeamUsers(@PathVariable("modelName") String modelName) {
        List<String> fieldKeyListName = getFieldKeyListForModelName(modelName);
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.deepSerialize(fieldKeyListName);
        return jsonString;
    }

    private UIMetaData deleteSelectedItems(UIMetaData uiMetaData, String[] deletedEntries, String[] deletedEntriesForPanels) {

        Map<Integer, List<FieldDefinition>> mapToDelete = new HashMap<Integer, List<FieldDefinition>>();

        for (int i = 0 ; i < deletedEntries.length ; i++) {
            if (deletedEntries[i] != null && deletedEntries[i].length() > 0) {
                String[] thisFieldEntry = deletedEntries[i].split(":");
                if (thisFieldEntry != null && thisFieldEntry.length > 1) {
                    int panelNumber = Integer.parseInt(thisFieldEntry[0]);
                    int fieldNumber = Integer.parseInt(thisFieldEntry[1]);

                    FieldDefinition fD = uiMetaData.getPanelDefinitionList().get(panelNumber).getFieldDefinitionList()
                            .get(fieldNumber);

                    List<FieldDefinition> fDList;
                    if (mapToDelete.get(panelNumber) != null && mapToDelete.get(panelNumber).size() > 0) {
                        fDList = mapToDelete.get(panelNumber);
                    } else {
                        fDList = new ArrayList<FieldDefinition>();
                    }

                    fDList.add(fD);
                    mapToDelete.put(panelNumber, fDList);
                }
            }
        }

        for (int i : mapToDelete.keySet()) {

            List<FieldDefinition> fDList = uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList();
            fDList.removeAll(mapToDelete.get(i));

            uiMetaData.getPanelDefinitionList().get(i).setFieldDefinitionList(fDList);
        }

        for (int i = 0 ; i < deletedEntriesForPanels.length ; i++) {
            if (deletedEntriesForPanels[i] != null && deletedEntriesForPanels[i].length() > 0) {
                int panelNumber = Integer.parseInt(deletedEntriesForPanels[i]);
                List<PanelDefinition> pDList = uiMetaData.getPanelDefinitionList();
                PanelDefinition pD = pDList.get(panelNumber);
                pDList.remove(pD);
                uiMetaData.setPanelDefinitionList(pDList);
            }
        }

        return uiMetaData;
    }

    private List<String> getFieldKeyListForModelName(String modelName) {
        List<String> fieldKeyListName = new ArrayList<String>();
        List<FieldMetaData> fieldMetaDataList = new ArrayList<FieldMetaData>();

        ModelMetaData modelMetaData = formService.getModelByModelName(modelName);
        fieldMetaDataList = modelMetaData.getFields();

        for (FieldMetaData fieldMetaData : fieldMetaDataList) {
            fieldKeyListName.add(fieldMetaData.getName());
        }

        return fieldKeyListName;
    }

    private List<String> getModelNameList() {
        List<String> modelNames = new ArrayList<String>();
        List<ModelMetaData> modelMetaDataList = formDefinitionGridService.getModelMetaDatas();
        for (ModelMetaData modelMetaData : modelMetaDataList) {
            modelNames.add(modelMetaData.getName());
        }
        return modelNames;
    }

    private void getDataForEditViewMode(Long id, ModelMap map) {

        UIMetaData uiMetaData = entityDao.find(UIMetaData.class, id);

        Hibernate.initialize(uiMetaData.getPanelDefinitionList());

        for (int i = 0 ; i < uiMetaData.getPanelDefinitionList().size() ; i++) {
            Hibernate.initialize(uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList());
        }

        map.put("UIMetaData", uiMetaData);
        map.put("masterId", masterId);
        map.put("dynamicDataFieldType", formService.getFormComponentType());
        map.put("id", id);
        map.put("typeSize", uiMetaData.getPanelDefinitionList().get(0).getFieldDefinitionList().size());
        map.put("modelNameList", getModelNameList());
        map.put("fieldKeyList", getFieldKeyListForModelName(uiMetaData.getModelName()));
    }

}
