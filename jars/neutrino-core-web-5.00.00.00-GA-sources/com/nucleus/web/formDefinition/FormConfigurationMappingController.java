package com.nucleus.web.formDefinition;

import static com.nucleus.logging.BaseLoggers.exceptionLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.dynamicform.service.FormConfigurationMappingService;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.core.web.util.ComboBoxAdapterUtil;
import com.nucleus.persistence.EntityDao;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;

@Controller
@RequestMapping(value = "/FormConfigurationMapping")
public class FormConfigurationMappingController extends BaseController {

    @Inject
    @Named("entityDao")
    private EntityDao                       entityDao;

    @Inject
    @Named("formConfigurationMappingService")
    private FormConfigurationMappingService formConfigurationMappingService;

    private static final String                                  masterId = "FormConfigurationMapping";

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/create")
    public String createFormDefinition(ModelMap map) {
        map.put("masterId", masterId);
        map.put("formConfigurationMapping", new FormConfigurationMapping());
        return "formConfigurationMapping";
    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveFormConfigMapping(
            @ModelAttribute("FormConfigurationMapping") FormConfigurationMapping formConfigurationMapping,
            BindingResult result, ModelMap map) {

        if (formConfigurationMapping.getId() == null) {
            formConfigurationMapping.getEntityLifeCycleData().setCreatedByUri(getUserDetails().getUserEntityId().getUri());
        } else {
            formConfigurationMapping.getEntityLifeCycleData().setLastUpdatedByUri(
                    getUserDetails().getUserEntityId().getUri());
        }
        entityDao.saveOrUpdate(formConfigurationMapping);
        return "redirect:/app/FormConfigurationMappingGrid/FormConfigurationMapping/FormConfigurationMapping/loadColumnConfig";

    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/edit/{id}")
    public String editFormMapping(@PathVariable("id") Long id, ModelMap map) {
        FormConfigurationMapping formConfigurationMapping = entityDao.find(FormConfigurationMapping.class, id);
        map.put("formConfigurationMapping", formConfigurationMapping);
        map.put("edit", true);
        map.put("masterId", masterId);
        map.put("uiMetaData",
                formConfigurationMappingService.getUIMetaDataByModel(formConfigurationMapping.getModelMetaData().getId()));
        return "formConfigurationMapping";
    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG') ")
    @RequestMapping(value = "/view/{id}")
    public String viewFormMapping(@PathVariable("id") Long id, ModelMap map) {
        FormConfigurationMapping formConfigurationMapping = entityDao.find(FormConfigurationMapping.class, id);
        map.put("formConfigurationMapping", formConfigurationMapping);
        map.put("viewable", true);
        map.put("masterId", masterId);
        map.put("uiMetaData",
                formConfigurationMappingService.getUIMetaDataByModel(formConfigurationMapping.getModelMetaData().getId()));
        return "formConfigurationMapping";
    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/delete/{id}")
    public String deleteFormMapping(@PathVariable("id") String ids, ModelMap map) {

        String[] idsToDelete = ids.split(",");
        for (int i = 0 ; i < idsToDelete.length ; i++) {
            Long id = Long.parseLong(idsToDelete[i]);
            FormConfigurationMapping formConfigurationMapping = entityDao.find(FormConfigurationMapping.class, id);
            entityDao.delete(formConfigurationMapping);
        }

        return "redirect:/app/grid/FormConfigurationMapping/FormConfigurationMapping/loadColumnConfig";
    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG') ")
    @RequestMapping(value = "/list/{modelMetaData}")
    public @ResponseBody
    String listLoans(@PathVariable(value = "modelMetaData") Long modelMetaDataId, ModelMap map) {

        try {
            List<Map<String, ?>> par = new ArrayList<Map<String, ?>>();
            if (modelMetaDataId != null) {
                // map.put("loanType",
                List<UIMetaData> uiMetaDataList = formConfigurationMappingService.getUIMetaDataByModel(modelMetaDataId);
                for (UIMetaData uiMetaData : uiMetaDataList) {
                    Map<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put("id", String.valueOf(uiMetaData.getId()));
                    valueMap.put("formName", uiMetaData.getFormName());
                    par.add(valueMap);
                }
                // Map list = new M

                Map consolidateMap = ComboBoxAdapterUtil.listOfMapsToSingleMap(par, "id", "formName");
                JSONSerializer serializer = new JSONSerializer();
                return serializer.serialize(consolidateMap);
            } else {
                map.put("zipCode", null);
                return null;
            }
        } catch (Exception e) {
            exceptionLogger.error("Exception : ", e);
        }
        return null;

    }

}
