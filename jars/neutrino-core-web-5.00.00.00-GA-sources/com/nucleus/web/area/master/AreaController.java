package com.nucleus.web.area.master;

import static com.nucleus.logging.BaseLoggers.exceptionLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.persistence.HibernateUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriUtils;
import com.nucleus.html.util.HtmlUtils;

import com.nucleus.address.Area;
import com.nucleus.address.City;
import com.nucleus.address.Country;
import com.nucleus.address.ZipCode;
import com.nucleus.core.area.service.AreaService;
import com.nucleus.core.web.util.ComboBoxAdapterUtil;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import flexjson.JSONSerializer;

/**
 * @author Nucleus Software India Pvt Ltd This field is being used for
 *         controlling area CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller
@RequestMapping(value = "/Area")
public class AreaController extends BaseController {

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService        makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService          baseMasterService;

    @Inject
    @Named("areaService")
    private AreaService                areaService;

    @Inject
    @Named("stringEncryptor")
    private StandardPBEStringEncryptor encryptor;

    private static final String                             masterId = "Area";

   public static final String         ZIPCODE  = "zipCode";

    @InitBinder("area")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new AreaValidator());
    }

    /*
     * Method Added to send current Entity Uri for working of
     * comments,activity,history,notes
     */
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException {
        return UriUtils.encodeQueryParam("enc_" + encryptor.encrypt(Area.class.getName()), "UTF-8");
    }

    /**
     * @param area
     *            object containing area name,area code,area
     *            categorization,city,zipcode etc.
     * @return String
     * @throws IOException
     * @description to save area object from view
     */
    @PreAuthorize("hasAuthority('MAKER_AREA')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveArea(@Validated Area area, BindingResult result, ModelMap map,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        BaseLoggers.flowLogger.debug(area.getLogInfo());
        /*
         * Map whoes Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */

        Area dubplicateArea = null;
        if(null!=area.getId()){
            dubplicateArea = entityDao.find(Area.class,area.getId());
            if(null != dubplicateArea.getEntityLifeCycleData()){
                area.setEntityLifeCycleData(dubplicateArea.getEntityLifeCycleData());
            }
            if(null != dubplicateArea.getMasterLifeCycleData()){
                area.setMasterLifeCycleData(dubplicateArea.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("areaCode", area.getAreaCode());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(area, Area.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            if(area.getId() != null) {
                Area a = baseMasterService.getMasterEntityById(Area.class, area.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == a.getApprovalStatus() || ApprovalStatus.CLONED == a.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }

            List<Map<String, Object>> zipCodeList = baseMasterService.getAllApprovedAndActiveSelectedListEntities(
                    ZipCode.class, ZIPCODE);
            String masterName = area.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != area.getId()) {
                //Area areaForCode = baseMasterService.findById(Area.class, area.getId());
                uniqueValue = dubplicateArea.getAreaCode();
                uniqueParameter = "areaCode";
                getActInactReasMapForEditApproved(map, area, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                area.setReasonActInactMap(reasActInactMap);
            }
            getActInactReasonsForEdit(map,area);
            map.put("viewable" , false);
            map.put("edit" , true);
            prepareDataMap(map, area);
            map.put("zipCodeList", zipCodeList);
            /*
             * if List Contains Any Duplicate Values Column Names, Then set them
             * in result
             */
            if (colNameList != null && colNameList.size() > 0) {
                for (String c : colNameList) {
                    result.rejectValue(c, "label." + c + ".validation.exists");
                }
            }
            return "area";
        }

        boolean eventResult = executeMasterEvent(area,"contextObjectArea",map);
        if(!eventResult){
            List<Map<String, Object>> zipCodeList = baseMasterService.getAllApprovedAndActiveSelectedListEntities(
                    ZipCode.class, ZIPCODE);
            //getActInactReasMapForEdit(map,area);
            String masterName = area.getClass().getSimpleName();
            String uniqueValue = area.getAreaCode();
            String uniqueParameter = "areaCode";
            getActInactReasMapForEditApproved(map,area,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            getActInactReasonsForEdit(map,area);
            prepareDataMap(map, area);
            map.put("viewable" , false);
            map.put("activeFlag",area.isActiveFlag());
            map.put("zipCodeList", zipCodeList);
            return "area";
        }

        /*
         * To check if referenced entity id is null,set entity as null
         */
        if (area.getCity().getId() == null) {
            area.setCity(null);
        }
        if (area.getVillage().getId() == null) {
            area.setVillage(null);
        }
        if (area.getZipcode().getId() == null) {
            area.setZipcode(null);
        }
        if (area.getAreaCategorization().getId() == null) {
            area.setAreaCategorization(null);
        }
        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = area.getReasonActInactMap();
            if( area.getReasonActInactMap() != null && area.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
                List<MasterActiveInactiveReasons> reasonInActList = area.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                        .filter(m -> ((m.getReasonInactive() != null))).collect(Collectors.toList());
                List<MasterActiveInactiveReasons> reasonActList = area.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                        .filter(m -> ((m.getReasonActive() != null))).collect(Collectors.toList());

                if (reasonInActList.size() != 0 || reasonActList.size() != 0) {
                    saveActInactReasonForMaster(reasonsActiveInactiveMapping, area);
                    area.setReasonActInactMap(reasonsActiveInactiveMapping);
                }
                else{
                    area.setReasonActInactMap(null);
                }
            }
            makerCheckerService.masterEntityChangedByUser(area, user);

        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            Area areaForCreateAnother= new Area();
            areaForCreateAnother.setReasonActInactMap(reasActInactMap);
            prepareDataMap(map,areaForCreateAnother);
            return "area";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/Area/Area/loadColumnConfig";

    }

    /**
     * @description to create area
     */
    @PreAuthorize("hasAuthority('MAKER_AREA')")
    @RequestMapping(value = "/create")
    public String createArea(ModelMap map) {
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        Area area= new Area();
        area.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",area.getReasonActInactMap());
        prepareDataMap(map, area);
        return "area";
    }
    
    private void prepareDataMap(ModelMap map, Area area){
    	map.put("area", area);
        map.put("masterID", masterId);
        map.put("cityList", baseMasterService.getLastApprovedEntities(City.class));
        map.put("villageList", baseMasterService.getLastApprovedEntities(VillageMaster.class));
    }

    /**
     * @param record
     *            id for edit
     * @return void
     * @throws
     * @description to edit area
     */
    @PreAuthorize("hasAuthority('MAKER_AREA')")
    @RequestMapping(value = "/edit/{id}")
    public String editArea(@PathVariable("id") Long id, ModelMap map) {
        Area area = baseMasterService.getMasterEntityById(Area.class, id);
        if (area.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            Area prevArea = (Area) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(area.getEntityId());
            map.put("prevArea", prevArea);
            map.put("editLink", false);
        }
        
        List<ZipCode> zipCodeList = null;
        if (ValidatorUtils.notNull(area)) {
            if (ValidatorUtils.notNull(area.getCity())
                    && ValidatorUtils.notNull(area.getCity().getId())) {
                zipCodeList = areaService.getZipCodesByCityId(area.getCity().getId());
            } else if (ValidatorUtils.notNull(area.getVillage())
                    && ValidatorUtils.notNull(area.getVillage().getId())) {
                zipCodeList = areaService.getZipCodesByVillageId(area.getVillage().getId());
            }
        }
        if(!(ApprovalStatus.UNAPPROVED_ADDED == area.getApprovalStatus() || ApprovalStatus.CLONED == area.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,area.getReasonActInactMap());
        area.setReasonActInactMap(reasonsActiveInactiveMapping);
        String masterName = area.getClass().getSimpleName();
        String uniqueValue = area.getAreaCode();
        String uniqueParameter = "areaCode";
        getActInactReasMapForEditApproved(map,area,masterName,uniqueParameter,uniqueValue);
        prepareDataMap(map, area);
        map.put("zipCodeList", zipCodeList);
        map.put("edit", true);

        map.put("viewable" ,false);
        return "area";
    }

    /**
     * @description to save and send for approval * @return String
     * @throws IOException
     * @description to save and send for approval Area object from view
     */
    @PreAuthorize("hasAuthority('MAKER_AREA')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(ModelMap map, @Validated Area area, BindingResult result,
            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        BaseLoggers.flowLogger.debug(area.getLogInfo());
        /*
         * Map whoes Key Is Table Column Name with whom to validate and Value is
         * The One to be validated.This Map Is Send in the Validator Method
         */

        Area dubplicateArea = null;
        if(null!=area.getId()){
            dubplicateArea = entityDao.find(Area.class,area.getId());
            if(null != dubplicateArea.getEntityLifeCycleData()){
                area.setEntityLifeCycleData(dubplicateArea.getEntityLifeCycleData());
            }
            if(null != dubplicateArea.getMasterLifeCycleData()){
                area.setMasterLifeCycleData(dubplicateArea.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("areaCode", area.getAreaCode());

        /*
         * Code to check as if any existing(or new) record is being modified(or created) into another existing record
         */
        List<String> colNameList = checkValidationForDuplicates(area, Area.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            if(area.getId() != null) {
                Area a = baseMasterService.getMasterEntityById(Area.class, area.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == a.getApprovalStatus() || ApprovalStatus.CLONED == a.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            List<Map<String, Object>> zipCodeList = baseMasterService.getAllApprovedAndActiveSelectedListEntities(
                    ZipCode.class, ZIPCODE);
            String masterName = area.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != area.getId()) {
                //Area areaForCode = baseMasterService.findById(Area.class, area.getId());
                uniqueValue = dubplicateArea.getAreaCode();
                uniqueParameter = "areaCode";
                getActInactReasMapForEditApproved(map, area, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                area.setReasonActInactMap(reasActInactMap);
            }
            getActInactReasonsForEdit(map,area);
            map.put("edit" , true);
            map.put("viewable" , false);
            prepareDataMap(map, area);
            map.put("zipCodeList", zipCodeList);
            /*
             * if List Contains Any Duplicate Values Column Names, Then set them
             * in result
             */
            if (colNameList != null && colNameList.size() > 0) {
                for (String c : colNameList) {
                    result.rejectValue(c, "label." + c + ".validation.exists");
                }
            }
            return "area";
        }

        boolean eventResult = executeMasterEvent(area,"contextObjectArea",map);
        if(!eventResult){
            List<Map<String, Object>> zipCodeList = baseMasterService.getAllApprovedAndActiveSelectedListEntities(
                    ZipCode.class, ZIPCODE);
            //getActInactReasMapForEdit(map,area);
            String masterName = area.getClass().getSimpleName();
            String uniqueValue = area.getAreaCode();
            String uniqueParameter = "areaCode";
            getActInactReasMapForEditApproved(map,area,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            getActInactReasonsForEdit(map,area);
            prepareDataMap(map, area);
            map.put("viewable" , false);
            map.put("activeFlag",area.isActiveFlag());
            map.put("zipCodeList", zipCodeList);
            return "area";
        }

        if (area.getCity().getId() == null) {
            area.setCity(null);
        }
        if (area.getVillage().getId() == null) {
            area.setVillage(null);
        }

        // we need to get below logged in user from session
        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = area.getReasonActInactMap();
            if( area.getReasonActInactMap() != null && area.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
                List<MasterActiveInactiveReasons> reasonInActList = area.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                        .filter(m -> ((m.getReasonInactive() != null))).collect(Collectors.toList());
                List<MasterActiveInactiveReasons> reasonActList = area.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                        .filter(m -> ((m.getReasonActive() != null))).collect(Collectors.toList());

                if (reasonInActList.size() != 0 || reasonActList.size() != 0) {
                    saveActInactReasonForMaster(reasonsActiveInactiveMapping, area);
                    area.setReasonActInactMap(reasonsActiveInactiveMapping);
                }
                else{
                    area.setReasonActInactMap(null);
                }
            }
            makerCheckerService.saveAndSendForApproval(area, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            Area areaForCreateAnother= new Area();
            areaForCreateAnother.setReasonActInactMap(reasActInactMap);
            prepareDataMap(map,areaForCreateAnother);
            return "area";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/Area/Area/loadColumnConfig";

    }

    /**
     * @param record
     *            id for view
     * @return void
     * @throws
     * @description to view Area
     */
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('VIEW_AREA') or hasAuthority('MAKER_AREA') or hasAuthority('CHECKER_AREA')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewArea(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        Area area = baseMasterService.getMasterEntityWithActionsById(Area.class, id, currentUser.getUserEntityId().getUri());
        if (area.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            Area prevArea = (Area) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(area.getEntityId());
            map.put("prevArea", prevArea);
            map.put("viewLink", false);
        } else if (area.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED
                || area.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            Area prevArea = (Area) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(area.getEntityId());
            map.put("prevArea", prevArea);
            map.put("editLink", false);
        }
        getActInactReasonsForEdit(map,area);
        //getActInactReasMapForEdit(map,area);
        String masterName = area.getClass().getSimpleName();
        String uniqueValue = area.getAreaCode();
        String uniqueParameter = "areaCode";
        getActInactReasMapForEditApproved(map,area,masterName,uniqueParameter,uniqueValue);
        List<City> cityList = new ArrayList<City>();
        cityList.add(area.getCity());
        List<VillageMaster> villageList = new ArrayList<>();
        villageList.add(area.getVillage());
        List<ZipCode> zipCodeList = new ArrayList<ZipCode>();
        zipCodeList.add(area.getZipcode());
        map.put("zipCodeList", zipCodeList);
        map.put("cityList", cityList);
        map.put("villageList", villageList);
        map.put("area", area);
        map.put("masterID", masterId);
        map.put("viewable", true);
        map.put("codeViewMode", true);
        if (area.getViewProperties() != null) {
            ArrayList<String> actions = (ArrayList<String>) area.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }

            }

        }

        return "area";
    }

    /**
     * @description to get the Zip Code on the basis of City Selected 
     * @return String
     * @throws IOException
     * 
     */
    
    
  
    
    
    @PreAuthorize("hasAuthority('MAKER_AREA')")
    @RequestMapping(value = "/zipCodeList/{cityID}")
public String filterlistZipCodesAutoComplete(@PathVariable String cityID,ModelMap map,@RequestParam String value,
    		
                                            @RequestParam String itemVal, @RequestParam String searchCol, @RequestParam String className,
                                            @RequestParam Boolean loadApprovedEntityFlag, @RequestParam String i_label, @RequestParam String idCurr,
                                            @RequestParam String content_id, @RequestParam int page, @RequestParam(required = false) String itemsList,
                                            @RequestParam(required = false) Boolean strictSearchOnitemsList) {
    	List<Map<String, ?>> list= new ArrayList<Map<String, ?>>();
          //String[] searchColumnList = searchCol.split(" ");
          
       /* if (strictSearchOnitemsList == null) {
            strictSearchOnitemsList = false;
        }
        if (loadApprovedEntityFlag == null) {
            loadApprovedEntityFlag = false;
        }*/
       
        if (cityID != null && (!cityID.equals(""))) {
            
               list =areaService.getZipCodesByCityId(Long.parseLong(cityID),page);
        	
        }
        int sizeList = 0;

        if (list.size() > 0) {
            Map<String, ?> listMap = list.get(list.size() - 1);
            sizeList = ((Long) listMap.get("size")).intValue();
            list.remove(list.size() - 1);
            map.put("size", sizeList);
            map.put("page", page);
        }

        if (i_label != null && i_label.contains(".")) {
            i_label = i_label.replace(".", "");
        }

        map.put("data", list);
        if(idCurr!=null && idCurr.trim().length()>0){
        	idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
        }
        map.put("idCurr", HtmlUtils.htmlEscape(idCurr));
        map.put("i_label", i_label);
        map.put("content_id", content_id);
        map.put("itemVal", itemVal);

        
        return "autocomplete";


    }
    
   
    @PreAuthorize("hasAuthority('MAKER_AREA')")
    @RequestMapping(value = "/list/{cityID}")
    public @ResponseBody
    String listZipCodes(@PathVariable String cityID, ModelMap map) {

        try {
            List<Map<String, ?>> par = new ArrayList<Map<String, ?>>();
            if (cityID != null && (!cityID.equals(""))) {
                // map.put("loanType",
                List<ZipCode> zipCodeList = areaService.getZipCodesByCityId(Long.parseLong(cityID));
                for (ZipCode zipCode : zipCodeList) {
                    Map<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put("id", String.valueOf(zipCode.getId()));
                    valueMap.put("zipCode", zipCode.getZipCode());
                    par.add(valueMap);
                }
                // Map list = new M

                Map consolidateMap = ComboBoxAdapterUtil.listOfMapsToSingleMap(par, "id", "zipCode");
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

    @PreAuthorize("hasAuthority('MAKER_AREA')")
    @RequestMapping(value = "/zipCodeListByVillageId/{villageId}")
    public String filterListZipCodesAutoCompleteByVillageId(@PathVariable String villageId,ModelMap map,@RequestParam String value,
                                                 @RequestParam String itemVal, @RequestParam String searchCol, @RequestParam String className,
                                                 @RequestParam Boolean loadApprovedEntityFlag, @RequestParam String i_label, @RequestParam String idCurr,
                                                 @RequestParam String content_id, @RequestParam int page, @RequestParam(required = false) String itemsList,
                                                 @RequestParam(required = false) Boolean strictSearchOnitemsList) {
        List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();

        if (villageId != null && (!villageId.equals("")) && !("0").equals(villageId)) {
            list = areaService.getZipCodesByVillageId(Long.parseLong(villageId), page);
        }
        int sizeList;

        if (list.size() > 0) {
            Map<String, ?> listMap = list.get(list.size() - 1);
            sizeList = ((Long) listMap.get("size")).intValue();
            list.remove(list.size() - 1);
            map.put("size", sizeList);
            map.put("page", page);
        }

        if (i_label != null && i_label.contains(".")) {
            i_label = i_label.replace(".", "");
        }

        map.put("data", list);
        if (idCurr != null && idCurr.trim().length() > 0) {
            idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
        }
        map.put("idCurr", HtmlUtils.htmlEscape(idCurr));
        map.put("i_label", i_label);
        map.put("content_id", content_id);
        map.put("itemVal", itemVal);

        return "autocomplete";
    }

    public void getActInactReasonsForEdit(ModelMap map,Area area){
        if(area.getReasonActInactMap() != null) {
            map.put("reasonsActiveInactiveMapping", area.getReasonActInactMap());
        }
        else{
            ReasonsActiveInactiveMapping reasonsActiveInactiveMappingEdit = new ReasonsActiveInactiveMapping();
            MasterActiveInactiveReasons masterActiveInactiveReasonsEdit = new MasterActiveInactiveReasons();
            List<MasterActiveInactiveReasons> masterActiveInactiveReasonsEditList = new ArrayList<MasterActiveInactiveReasons>();
            masterActiveInactiveReasonsEditList.add(masterActiveInactiveReasonsEdit);
            reasonsActiveInactiveMappingEdit.setMasterActiveInactiveReasons(masterActiveInactiveReasonsEditList);
            area.setReasonActInactMap(reasonsActiveInactiveMappingEdit);
            map.put("reasonsActiveInactiveMapping", area.getReasonActInactMap());
        }
        if(area.getReasonActInactMap() != null&& area.getReasonActInactMap().getMasterActiveInactiveReasons() != null){
            for(MasterActiveInactiveReasons mstReason:area.getReasonActInactMap().getMasterActiveInactiveReasons()){
                if(mstReason.getReasonActive() != null){
                    HibernateUtils.initializeAndUnproxy(mstReason.getReasonActive());
                }
                if(mstReason.getReasonInactive() != null){
                    HibernateUtils.initializeAndUnproxy(mstReason.getReasonInactive());
                }
            }
        }
    }
}