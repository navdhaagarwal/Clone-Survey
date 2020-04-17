package com.nucleus.web.masters;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;


import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.persistence.HibernateUtils;


import com.nucleus.autocomplete.AutocompleteVO;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.hibernate.Hibernate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.dynamicform.service.FormService;
import com.nucleus.core.formDefinition.FormDefinitionUtility;
import com.nucleus.core.formsConfiguration.DynamicFormScreenMapping;
import com.nucleus.core.formsConfiguration.DynamicFormScreenMappingDetail;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.ScreenId;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.html.util.HtmlUtils;


@Transactional
@Controller
@RequestMapping("/DynamicFormScreenMapping")
public class DynamicFormScreenMappingController extends BaseController {

    private static final String DYNAMIC_FORM_SCREEN_MASTER_ID = "DynamicFormScreenMapping";
    private static final String DYNAMIC_FORM_SCREEN_GRID_URI = "redirect:/app/grid/DynamicFormScreenMapping/DynamicFormScreenMapping/loadColumnConfig";
    private static final String DYNAMIC_FORM_SCREEN = "dynamicFormScreen";
    private static final String DYNAMIC_FORM_SCREEN_MAPPING = "dynamicFormScreenMapping";
    private static final String MASTER_ID="masterID";
    private static final String VIEW_MODE="viewable";
    private static final String ALL_DYNAMIC_FORM_LIST="allDynamicFormConfigTypeList";

    @Inject
    @Named("formConfigService")
    private FormService formService;

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("formDefinitionUtility")
    protected FormDefinitionUtility formDefinitionUtility;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Inject
    @Named("organizationService")
    private OrganizationService organizationService;

    @PreAuthorize("hasAnyAuthority('MAKER_DYNAMICFORMSCREENMAPPING')")
    @RequestMapping({ "/create" })
    public String createDynamicFormScreenMapping(ModelMap map) {
        initializeNewDynamicFormScreenMapping(map);
        return DYNAMIC_FORM_SCREEN_MAPPING;
    }

    @PreAuthorize("hasAnyAuthority('MAKER_DYNAMICFORMSCREENMAPPING')")
    @RequestMapping(value = "/validateIfScreenIsSingleDynamicFormEnabledAndFetchFormNames", method = RequestMethod.POST)
    public String checkIfScreenIdIsSingleDynamicFormEnabled(
            @RequestParam(required = true) String id,@RequestParam(required = true) String sourceProduct,ModelMap map) {
        Boolean singleDynamicFormEnabled = Boolean.FALSE;
        Long sourceProductValue =null;
        Long screenIdValue=null;
        if (!isEmpty(id)) {
            screenIdValue = Long.parseLong(id);
            singleDynamicFormEnabled = formService
                    .checkIfScreenIdIsSingleDynamicFormEnabled(screenIdValue);
        }
        if(!isEmpty(sourceProduct)){
            sourceProductValue = Long.parseLong(sourceProduct);
        }
        map.put("singleDynamicFormEnabled", singleDynamicFormEnabled);
        updateModelMapWithDynamicFormsForSourceProduct(map,sourceProductValue);
        map.put(MASTER_ID, DYNAMIC_FORM_SCREEN_MASTER_ID);
        map.put(VIEW_MODE, false);
        DynamicFormScreenMapping dynamicFormScreenMapping=new DynamicFormScreenMapping();
        ScreenId screenIdObject=formService.fetchScreenIdBasedOnId(screenIdValue);
        dynamicFormScreenMapping.setScreenId(screenIdObject);
        dynamicFormScreenMapping.setScreenIdValue(screenIdValue);
        SourceProduct sourceProductObj =formService.fetchSourceProductBasedOnId(sourceProductValue);
        dynamicFormScreenMapping.setSourceProduct(sourceProductObj);
        dynamicFormScreenMapping.setSourceProductId(sourceProductValue);
        map.put("activeFlag", true);
        map.put("activeFlagApproved", true);
        map.put("editActive", false);
        map.put("viewable", false);
        map.put("create", true);
        map.put("inactiveReasonFlag", false);
        map.put("flagForFirstTimeEdit",false);
        map.put(DYNAMIC_FORM_SCREEN, dynamicFormScreenMapping);
        return "dynamicFormScreenMap";
    }


    @PreAuthorize("hasAnyAuthority('MAKER_DYNAMICFORMSCREENMAPPING')")
    @RequestMapping(value = { "/save" }, method = { RequestMethod.POST })
    public String saveDynamicFormScreenMasterData(
            DynamicFormScreenMapping dynamicFormScreen,
            BindingResult result,
            ModelMap map,
            @RequestParam(value = "createAnotherMaster", required = false) boolean createAnotherMaster,
            @RequestParam(value = "dynamicFormScreenDtlList", required = false) String dynamicFormScreenDtlList) {
        DynamicFormScreenMapping persistedDynamicFormScreenMapping = null;
        if(notNull(dynamicFormScreen.getId())){
            persistedDynamicFormScreenMapping=formService
                    .fetchDynamicFormScreenMappingById(dynamicFormScreen.getId());
        }
        if(notNull(persistedDynamicFormScreenMapping) && ValidatorUtils.isNull(dynamicFormScreen.getSourceProduct())){
            dynamicFormScreen.setSourceProduct(persistedDynamicFormScreenMapping.getSourceProduct());
            dynamicFormScreen.setSourceProductId(persistedDynamicFormScreenMapping.getSourceProduct().getId());
        }else{
            dynamicFormScreen.setSourceProductId(dynamicFormScreen.getSourceProduct().getId());
        }
        if(notNull(persistedDynamicFormScreenMapping) && ValidatorUtils.isNull(dynamicFormScreen.getScreenId())){
            dynamicFormScreen.setScreenId(persistedDynamicFormScreenMapping.getScreenId());
            dynamicFormScreen.setScreenIdValue(persistedDynamicFormScreenMapping.getScreenIdValue());
        }else{
            ScreenId screenIdObject=formService.fetchScreenIdBasedOnId(dynamicFormScreen.getScreenIdValue());
            dynamicFormScreen.setScreenId(screenIdObject);
            dynamicFormScreen.setScreenIdValue(dynamicFormScreen.getScreenIdValue());
        }
        boolean eventResult = executeMasterEvent(dynamicFormScreen,"contextObjectDynamicFormScreenMapping",map);
        if(!eventResult){

            String masterName = dynamicFormScreen.getClass().getSimpleName();
            Long uniqueValue = dynamicFormScreen.getScreenIdValue();
            String uniqueParameter = "screenIdValue";
            getActInactReasMapForEditApproved(map,dynamicFormScreen,masterName,uniqueParameter,uniqueValue.toString(),true);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,dynamicFormScreen.getReasonActInactMap());
            dynamicFormScreen.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("viewable" , false);
            map.put("MASTER_ID", DYNAMIC_FORM_SCREEN_MASTER_ID);
            return "dynamicFormScreenMapping";
        }
        List<Object> updatedUserOrgBranchMappings = parseUserOrgBranchMappingString(dynamicFormScreenDtlList);
        dynamicFormScreen.setDynamicFormScreenDtlList(updateDynamicFormDetailListDataInDynamicFormScreenMapping(persistedDynamicFormScreenMapping,updatedUserOrgBranchMappings));
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = dynamicFormScreen.getReasonActInactMap();
        if( dynamicFormScreen.getReasonActInactMap() != null && dynamicFormScreen.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
            List<MasterActiveInactiveReasons> reasonInActList = dynamicFormScreen.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                    .filter(m -> ((m.getReasonInactive() != null))).collect(Collectors.toList());
            List<MasterActiveInactiveReasons> reasonActList = dynamicFormScreen.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                    .filter(m -> ((m.getReasonActive() != null))).collect(Collectors.toList());

            if (reasonInActList.size() != 0 || reasonActList.size() != 0) {
                saveActInactReasonForMaster(reasonsActiveInactiveMapping, dynamicFormScreen);
                dynamicFormScreen.setReasonActInactMap(reasonsActiveInactiveMapping);
            }
            else{
                dynamicFormScreen.setReasonActInactMap(null);
            }
        }

        makerCheckerService.masterEntityChangedByUser(dynamicFormScreen,
                getUserDetails().getUserReference());
        if (createAnotherMaster) {
            initializeNewDynamicFormScreenMapping(map);
        }
        map.put(MASTER_ID, DYNAMIC_FORM_SCREEN_MASTER_ID);
        return createAnotherMaster ? DYNAMIC_FORM_SCREEN_MAPPING
                : DYNAMIC_FORM_SCREEN_GRID_URI;
    }



    private List<Object> parseUserOrgBranchMappingString(String jsonString) {

        /*
         * Convert json string for updated branch data to Map
         */
        List<Object> updatedBranchData = new ArrayList<Object>();
        ObjectMapper mapper = new ObjectMapper();
        if (StringUtils.isNotBlank(jsonString)) {

            try {

                updatedBranchData = mapper.readValue(jsonString,
                        new TypeReference<List<Object>>() {
                        });

            } catch (JsonMappingException e) {
                BaseLoggers.exceptionLogger
                        .error("JsonMappingException while converting json string updated DynamicFormScreenDetail Data",
                                e);
            } catch (IOException e) {
                BaseLoggers.exceptionLogger
                        .error("IOException while converting json string updated DynamicFormScreenDetail Data",
                                e);
            }

        }
        return updatedBranchData;
    }

    @PreAuthorize("hasAuthority('VIEW_DYNAMICFORMSCREENMAPPING') or hasAuthority('MAKER_DYNAMICFORMSCREENMAPPING') or hasAuthority('CHECKER_DYNAMICFORMSCREENMAPPING')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewDynamicFormScreenMasterData(@PathVariable("id") Long id,
                                                  ModelMap map) {
        UserInfo currentUser = getUserDetails();
        DynamicFormScreenMapping dynamicFormScreenMapping = baseMasterService.getMasterEntityWithActionsById(DynamicFormScreenMapping.class, id, currentUser.getUserEntityId()
                .getUri());
        initializeDynamicFormScreenMappingObject(dynamicFormScreenMapping);
        if (notNull(dynamicFormScreenMapping) && notNull(dynamicFormScreenMapping.getScreenId())) {
            map.put("singleDynamicFormEnabled", dynamicFormScreenMapping.getScreenId().getSingleDynamicFormEnabled());
        }
        updateModelMapWithDynamicFormsForSourceProduct(map, dynamicFormScreenMapping.getSourceProductId());
        updateModelMapWithSelectedDynamicForms(dynamicFormScreenMapping, map);
        map.put(VIEW_MODE, true);
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,dynamicFormScreenMapping.getReasonActInactMap());
        dynamicFormScreenMapping.setReasonActInactMap(reasonsActiveInactiveMapping);
        String masterName = dynamicFormScreenMapping.getClass().getSimpleName();
        Long uniqueValue = dynamicFormScreenMapping.getScreenIdValue();
        String uniqueParameter = "screenIdValue";
        getActInactReasMapForEditApproved(map,dynamicFormScreenMapping,masterName,uniqueParameter,uniqueValue.toString(),true);
        String descriptionOfScreen=organizationService.getScreenDescription(uniqueValue);
        map.put("desc",descriptionOfScreen);
        map.put(DYNAMIC_FORM_SCREEN, dynamicFormScreenMapping);
        map.put(MASTER_ID, DYNAMIC_FORM_SCREEN_MASTER_ID);

        if (dynamicFormScreenMapping.getViewProperties() != null) {
            ArrayList<String> actions = (ArrayList<String>) dynamicFormScreenMapping.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }
        return DYNAMIC_FORM_SCREEN_MAPPING;
    }

    @PreAuthorize("hasAnyAuthority('MAKER_DYNAMICFORMSCREENMAPPING')")
    @RequestMapping(value = { "/saveAndSendForApproval" }, method = { RequestMethod.POST })
    public String saveAndSendForApprovalDynamicFormScreenMasterData(
            DynamicFormScreenMapping dynamicFormScreen,
            BindingResult result,
            ModelMap map,
            @RequestParam(value = "createAnotherMaster", required = false) boolean createAnotherMaster,
            @RequestParam(value = "dynamicFormScreenDtlList", required = false) String dynamicFormScreenDtlList) {
        DynamicFormScreenMapping persistedDynamicFormScreenMapping = null;
        if(notNull(dynamicFormScreen.getId())){
            persistedDynamicFormScreenMapping=formService
                    .fetchDynamicFormScreenMappingById(dynamicFormScreen.getId());
        }
        if(notNull(persistedDynamicFormScreenMapping) && ValidatorUtils.isNull(dynamicFormScreen.getSourceProduct())){
            dynamicFormScreen.setSourceProduct(persistedDynamicFormScreenMapping.getSourceProduct());
            dynamicFormScreen.setSourceProductId(persistedDynamicFormScreenMapping.getSourceProduct().getId());
        }else{
            dynamicFormScreen.setSourceProductId(dynamicFormScreen.getSourceProduct().getId());
        }
        if(notNull(persistedDynamicFormScreenMapping) && ValidatorUtils.isNull(dynamicFormScreen.getScreenId())){
            dynamicFormScreen.setScreenId(persistedDynamicFormScreenMapping.getScreenId());
            dynamicFormScreen.setScreenIdValue(persistedDynamicFormScreenMapping.getScreenIdValue());
        }else{
            ScreenId screenIdObject=formService.fetchScreenIdBasedOnId(dynamicFormScreen.getScreenIdValue());
            dynamicFormScreen.setScreenId(screenIdObject);
            dynamicFormScreen.setScreenIdValue(dynamicFormScreen.getScreenIdValue());
        }
        boolean eventResult = executeMasterEvent(dynamicFormScreen,"contextObjectDynamicFormScreenMapping",map);
        if(!eventResult){
            String masterName = dynamicFormScreen.getClass().getSimpleName();
            Long uniqueValue = dynamicFormScreen.getScreenIdValue();
            String uniqueParameter = "screenIdValue";
            getActInactReasMapForEditApproved(map,dynamicFormScreen,masterName,uniqueParameter,uniqueValue.toString(),true);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,dynamicFormScreen.getReasonActInactMap());
            dynamicFormScreen.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("viewable" , false);
            map.put("MASTER_ID", DYNAMIC_FORM_SCREEN_MASTER_ID);
            return "dynamicFormScreenMapping";
        }

        List<Object> updatedUserOrgBranchMappings = parseUserOrgBranchMappingString(dynamicFormScreenDtlList);
        dynamicFormScreen.setDynamicFormScreenDtlList(updateDynamicFormDetailListDataInDynamicFormScreenMapping(persistedDynamicFormScreenMapping,updatedUserOrgBranchMappings));
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = dynamicFormScreen.getReasonActInactMap();
        if( dynamicFormScreen.getReasonActInactMap() != null && dynamicFormScreen.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
            List<MasterActiveInactiveReasons> reasonInActList = dynamicFormScreen.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                    .filter(m -> ((m.getReasonInactive() != null))).collect(Collectors.toList());
            List<MasterActiveInactiveReasons> reasonActList = dynamicFormScreen.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                    .filter(m -> ((m.getReasonActive() != null))).collect(Collectors.toList());

            if (reasonInActList.size() != 0 || reasonActList.size() != 0) {
                saveActInactReasonForMaster(reasonsActiveInactiveMapping, dynamicFormScreen);
                dynamicFormScreen.setReasonActInactMap(reasonsActiveInactiveMapping);
            }
            else{
                dynamicFormScreen.setReasonActInactMap(null);
            }
        }

        makerCheckerService.saveAndSendForApproval(dynamicFormScreen,
                getUserDetails().getUserReference());
        if (createAnotherMaster) {
            initializeNewDynamicFormScreenMapping(map);
        }
        map.put(MASTER_ID, DYNAMIC_FORM_SCREEN_MASTER_ID);
        return createAnotherMaster ? DYNAMIC_FORM_SCREEN_MAPPING
                : DYNAMIC_FORM_SCREEN_GRID_URI;

    }

    @PreAuthorize("hasAnyAuthority('MAKER_DYNAMICFORMSCREENMAPPING')")
    @RequestMapping({ "/edit/{id}" })
    public String editDynamicFormScreenMapping(@PathVariable("id") Long id,
                                               ModelMap map) {
        DynamicFormScreenMapping dynamicFormScreenMapping = formService
                .fetchDynamicFormScreenMappingById(id);

        initializeDynamicFormScreenMappingObject(dynamicFormScreenMapping);
        if (notNull(dynamicFormScreenMapping) && notNull(dynamicFormScreenMapping.getScreenId())) {
            map.put("singleDynamicFormEnabled", dynamicFormScreenMapping.getScreenId().getSingleDynamicFormEnabled());
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,dynamicFormScreenMapping.getReasonActInactMap());
        dynamicFormScreenMapping.setReasonActInactMap(reasonsActiveInactiveMapping);
        String masterName = dynamicFormScreenMapping.getClass().getSimpleName();
        Long uniqueValue = dynamicFormScreenMapping.getScreenIdValue();
        String uniqueParameter = "screenIdValue";
        getActInactReasMapForEditApproved(map,dynamicFormScreenMapping,masterName,uniqueParameter,uniqueValue.toString(),true);
        String descriptionOfScreen=organizationService.getScreenDescription(uniqueValue);
        map.put("viewable" ,false);
        map.put(DYNAMIC_FORM_SCREEN, dynamicFormScreenMapping);
        map.put(MASTER_ID, DYNAMIC_FORM_SCREEN_MASTER_ID);
        map.put(VIEW_MODE, false);
        map.put("edit", true);
        map.put("desc",descriptionOfScreen);
        updateModelMapWithDynamicFormsForEditMode(map,dynamicFormScreenMapping);
        updateModelMapWithSelectedDynamicForms(dynamicFormScreenMapping, map);
        return DYNAMIC_FORM_SCREEN_MAPPING;
    }


    private void updateModelMapWithDynamicFormsForEditMode(
            ModelMap map, DynamicFormScreenMapping dynamicFormScreenMapping) {
        List<FormConfigurationMapping> allDynamicFormConfigTypeList = formService.loadUniqueDynamicFormsForSourceProduct(dynamicFormScreenMapping.getSourceProductId());
        List<FormConfigurationMapping> deSelectedDynamicFormConfigList=null;
        if(hasElements(dynamicFormScreenMapping.getDynamicFormScreenDtlList())){
            deSelectedDynamicFormConfigList =	fetchUnSelectedDynamicFormsList(allDynamicFormConfigTypeList,dynamicFormScreenMapping);
        }else{
            deSelectedDynamicFormConfigList = allDynamicFormConfigTypeList;
        }
        map.put(ALL_DYNAMIC_FORM_LIST, deSelectedDynamicFormConfigList);


    }


    private List<FormConfigurationMapping> fetchUnSelectedDynamicFormsList(
            List<FormConfigurationMapping> allDynamicFormConfigTypeList,DynamicFormScreenMapping dynamicFormScreenMapping) {
        List<FormConfigurationMapping> deSelectedDynamicFormConfigList = new ArrayList<FormConfigurationMapping>();
        if(hasElements(allDynamicFormConfigTypeList) && hasElements(dynamicFormScreenMapping.getDynamicFormScreenDtlList())){
            Map<Long,FormConfigurationMapping> formConfigIdSelectedMap=formDefinitionUtility.prepareConfigIdMapForSelectedForms(dynamicFormScreenMapping);
            if(isNotEmpty(formConfigIdSelectedMap) && hasElements(allDynamicFormConfigTypeList) ){
                for(FormConfigurationMapping formConfig:allDynamicFormConfigTypeList){
                    if(!formConfigIdSelectedMap.containsKey(formConfig.getId())){
                        deSelectedDynamicFormConfigList.add(formConfig);
                    }
                }
            }
        }
        return deSelectedDynamicFormConfigList;
    }




    @PreAuthorize("hasAnyAuthority('MAKER_DYNAMICFORMSCREENMAPPING')")
    @RequestMapping(value = "/listPlaceHolderIds", method = RequestMethod.POST)
    @ResponseBody
    public AutocompleteVO fetchPlaceHolderIdsBasedOnSourceProduct(ModelMap map, @RequestParam String value,
                                                                  @RequestParam String itemVal, @RequestParam String searchCol, @RequestParam String className,
                                                                  @RequestParam Boolean loadApprovedEntityFlag, @RequestParam String i_label, @RequestParam String idCurr,
                                                                  @RequestParam String content_id, @RequestParam int page, @RequestParam(required = false) String itemsList,
                                                                  @RequestParam(required = false) Boolean strictSearchOnitemsList, @RequestParam(value = "sourceProductId") String sourceProductId) {

        Long sourceProductIdValue = null;
        List<Map<String, ?>> screenIds =  new ArrayList<Map<String, ?>>();
        AutocompleteVO autocompleteVO = new AutocompleteVO();
        if (!StringUtils.isEmpty(sourceProductId)) {
            sourceProductIdValue = Long.parseLong(sourceProductId);
            screenIds = formService.fetchPlaceHolderListMappedToSourceProduct(value, sourceProductIdValue, page);
        }
        int sizeList = 0;

        if (ValidatorUtils.hasElements(screenIds)) {
            Map<String, ?> listMap = screenIds.get(screenIds.size() - 1);
            sizeList = ((Long) listMap.get("size")).intValue();
            screenIds.remove(screenIds.size() - 1);

            autocompleteVO.setS(sizeList);
            autocompleteVO.setP(page);
            //map.put("size", sizeList);
            //map.put("page", page);
        }
        String[] searchColumnList = searchCol.split(" ");
        String[] sclHeading=new String[searchColumnList.length];
        for(int i=0;i<searchColumnList.length;i++)
        {
            searchColumnList[i]=searchColumnList[i].replace(".", "");
            sclHeading[i]=messageSource.getMessage("label.autoComplete."+searchColumnList[i],null, Locale.getDefault());
        }
        autocompleteVO.setD(screenIds);
        //map.put("data", screenIds);
        //map.put("idCurr", idCurr);
        //map.put("i_label", i_label);
        //map.put("content_id", content_id);
        //map.put("itemVal", itemVal);
        autocompleteVO.setIc(idCurr);
        autocompleteVO.setIl(i_label);
        autocompleteVO.setCi(content_id);
        autocompleteVO.setIv(itemVal);
        autocompleteVO.setScl(searchColumnList);
        autocompleteVO.setColh(sclHeading);


        return autocompleteVO;
        //return "autocomplete";

    }

    @RequestMapping(value = "/getProductTypeList" ,method = {RequestMethod.POST,RequestMethod.GET})
    public String getProductTypes(@RequestParam("index") int index,ModelMap map) {
        map.put("index",index);
        return "formConfigurationMapping/productTypeMultiselect";
    }

    private void updateModelMapWithDynamicForms(ModelMap map) {
        List<FormConfigurationMapping> allDynamicFormConfigTypeList = formService
                .loadUniqueDynamicForms();
        map.put(ALL_DYNAMIC_FORM_LIST, allDynamicFormConfigTypeList);
    }

    private void updateModelMapWithDynamicFormsForSourceProduct(
            Map<String, Object> map,Long sourceProductValue) {
        List<FormConfigurationMapping> allDynamicFormConfigTypeList = formService.loadUniqueDynamicFormsForSourceProduct(sourceProductValue);
        map.put(ALL_DYNAMIC_FORM_LIST, allDynamicFormConfigTypeList);

    }

    private List<DynamicFormScreenMappingDetail>  updateDynamicFormDetailListDataInDynamicFormScreenMapping(
            DynamicFormScreenMapping dynamicFormScreen, List<Object> updatedUserOrgBranchMappings) {
        List<DynamicFormScreenMappingDetail> dtlList = new ArrayList<DynamicFormScreenMappingDetail>();
        if(notNull(dynamicFormScreen)&& ValidatorUtils.hasElements(dynamicFormScreen.getDynamicFormScreenDtlList())
                && (ApprovalStatus.UNAPPROVED_ADDED==dynamicFormScreen.getApprovalStatus() ||
                ApprovalStatus.UNAPPROVED_MODIFIED == dynamicFormScreen.getApprovalStatus())){
            for (Object object : updatedUserOrgBranchMappings) {
                DynamicFormScreenMappingDetail newDynamicFormDetail = prepareNewDynamicFormDetailRecord(object);
                for(DynamicFormScreenMappingDetail dynamicFormMappingDtl : dynamicFormScreen.getDynamicFormScreenDtlList()){
                    if(dynamicFormMappingDtl.getFormConfigValue().equals(newDynamicFormDetail.getFormConfigValue())){
                        dynamicFormMappingDtl.setScreenId(newDynamicFormDetail.getScreenId());
                        dynamicFormMappingDtl.setEditModeEnabled(newDynamicFormDetail.getEditModeEnabled());
                        dynamicFormMappingDtl.setFormSequence(newDynamicFormDetail.getFormSequence());
                        dynamicFormMappingDtl.setProductTypes(newDynamicFormDetail.getProductTypes());
                        dtlList.add(dynamicFormMappingDtl);
                        break;
                    }else{
                        dtlList.add(newDynamicFormDetail);
                        break;
                    }
                }
            }
        }else{
            for (Object object : updatedUserOrgBranchMappings) {
                dtlList.add(prepareNewDynamicFormDetailRecord(object));
            }
        }
        return dtlList;
    }

    private DynamicFormScreenMappingDetail prepareNewDynamicFormDetailRecord(Object object){

        DynamicFormScreenMappingDetail dynamicFormScreenDtl = new DynamicFormScreenMappingDetail();
        Map<String, Object> dynamicFormDetailMap = (Map<String, Object>) object;
        if (dynamicFormDetailMap.containsKey("formConfigId")) {
            dynamicFormScreenDtl.setFormConfigValue(Long
                    .parseLong((String) dynamicFormDetailMap
                            .get("formConfigId")));

        }
        if (dynamicFormDetailMap.containsKey("screenId")) {
            dynamicFormScreenDtl.setScreenIdValue(Long
                    .parseLong((String) dynamicFormDetailMap
                            .get("screenId")));

        }
        if (dynamicFormDetailMap.containsKey("editModeEnabled")) {
            dynamicFormScreenDtl
                    .setEditModeEnabled((Boolean) dynamicFormDetailMap
                            .get("editModeEnabled"));
        }


        if (dynamicFormDetailMap.containsKey("productType")) {
            dynamicFormScreenDtl
                    .setProductTypes((String) dynamicFormDetailMap
                            .get("productType"));
        }
        if (dynamicFormDetailMap.containsKey("sequence")) {
            dynamicFormScreenDtl
                    .setFormSequence((Integer) dynamicFormDetailMap
                            .get("sequence"));
        }

        return dynamicFormScreenDtl;

    }

    private void initializeDynamicFormScreenMappingObject(
            DynamicFormScreenMapping dynamicFormScreenMapping) {
        Hibernate.initialize(dynamicFormScreenMapping.getScreenId());
        if (notNull(dynamicFormScreenMapping.getScreenId())) {
            Hibernate.initialize(dynamicFormScreenMapping.getScreenId()
                    .getScreenName());
            Hibernate.initialize(dynamicFormScreenMapping.getScreenId()
                    .getScreenIdValue());
            Hibernate.initialize(dynamicFormScreenMapping.getScreenId()
                    .getSingleDynamicFormEnabled());
        }
        if(notNull(dynamicFormScreenMapping.getSourceProduct())){
            Hibernate.initialize(dynamicFormScreenMapping.getSourceProduct().getId());
            Hibernate.initialize(dynamicFormScreenMapping.getSourceProduct().getDisplayName());
            Hibernate.initialize(dynamicFormScreenMapping.getSourceProduct().getName());
        }
    }

    private void updateModelMapWithSelectedDynamicForms(
            DynamicFormScreenMapping dynamicFormScreenMapping, ModelMap map) {
        if (notNull(dynamicFormScreenMapping)
                && hasElements(dynamicFormScreenMapping
                .getDynamicFormScreenDtlList())) {
            List<DynamicFormScreenMappingDetail> mappingDetailList =dynamicFormScreenMapping.getDynamicFormScreenDtlList();
            for(DynamicFormScreenMappingDetail mappingDetail : mappingDetailList) {
                if (notNull(mappingDetail.getFormConfigurationMapping())) {
                    Hibernate.initialize(mappingDetail.getFormConfigurationMapping().getUiMetaData().getId());
                }
                if (mappingDetail.getProductTypes() != null) {
                    String[] array = mappingDetail.getProductTypes().split(",");

                        Long[] productTypeList = new Long[array.length];
                        for (int i = 0; i < array.length; i++)
                            productTypeList[i] = Long.parseLong(array[i]);
                        mappingDetail.setProductTypeList(productTypeList);

                }
            }

            map.put("defSelectedDynamicFormConfigTypeList",
                    dynamicFormScreenMapping.getDynamicFormScreenDtlList());
        }
    }

    private void initializeNewDynamicFormScreenMapping(ModelMap map){
        DynamicFormScreenMapping dynamicFormScreenMapping = new DynamicFormScreenMapping();
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        dynamicFormScreenMapping.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",dynamicFormScreenMapping.getReasonActInactMap());
        if(notNull(ProductInformationLoader.getProductName())){
            SourceProduct sourceProduct=genericParameterService.findByCode(ProductInformationLoader.getProductName(), SourceProduct.class);
            if(notNull(sourceProduct)){
                dynamicFormScreenMapping.setSourceProduct(sourceProduct);
                dynamicFormScreenMapping.setSourceProductId(sourceProduct.getId());
            }
        }


        map.put(DYNAMIC_FORM_SCREEN, dynamicFormScreenMapping);
        updateModelMapWithDynamicForms(map);
        map.put(MASTER_ID, DYNAMIC_FORM_SCREEN_MASTER_ID);
        map.put(VIEW_MODE, false);
    }

}