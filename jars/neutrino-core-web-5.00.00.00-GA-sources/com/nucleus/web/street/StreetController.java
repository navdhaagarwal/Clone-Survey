package com.nucleus.web.street;


import com.nucleus.activeInactiveReason.*;
import com.nucleus.address.*;
import com.nucleus.entity.*;
import com.nucleus.finnone.pro.general.util.*;
import com.nucleus.makerchecker.*;
import com.nucleus.persistence.*;
import com.nucleus.user.*;
import com.nucleus.web.common.controller.*;
import org.apache.commons.collections.*;
import org.hibernate.*;
import org.jasypt.encryption.pbe.*;
import org.springframework.security.access.prepost.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.validation.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.*;

import javax.inject.*;
import java.io.*;
import java.util.*;

@Controller
@RequestMapping(value = "/Street")
public class StreetController extends BaseController {

    private static final String masterId = "Street";

    @Inject
    @Named("stringEncryptor")
    private StandardPBEStringEncryptor encryptor;

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @InitBinder("street")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new StreetValidator());
    }

    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException {
        String encrypt="enc_"+encryptor.encrypt(Street.class.getName());
        String returnUri=UriUtils.encodeQueryParam(encrypt,"UTF-8");
        return returnUri;
    }

    @PreAuthorize("hasAuthority('MAKER_STREET')")
    @RequestMapping(value = "/create")
    public String createStreet(ModelMap map) {
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        Street street= new Street();
        street.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",street.getReasonActInactMap());
        map.put("street",street);
        map.put("masterID", masterId);
        return "street";
    }

    @PreAuthorize("hasAuthority('MAKER_STREET')")
    @RequestMapping(value = "/edit/{id}")
    public String editStreet(@PathVariable("id") Long id, ModelMap map){

        Street street = baseMasterService.getMasterEntityById(Street.class, id);
        Hibernate.initialize(street.getCity());
        if (street.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            Street prevStreet = (Street) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(street.getEntityId());
            map.put("prevStreet", prevStreet);
            map.put("viewLink", false);
        }
        if(!(ApprovalStatus.UNAPPROVED_ADDED == street.getApprovalStatus() || ApprovalStatus.CLONED == street.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }

        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,street.getReasonActInactMap());
        street.setReasonActInactMap(reasonsActiveInactiveMapping);
        String masterName = street.getClass().getSimpleName();
        String uniqueValue = street.getStreetCode();
        String uniqueParameter = "streetCode";
        getActInactReasMapForEditApproved(map,street,masterName,uniqueParameter,uniqueValue);

        map.put("viewable" ,false);
        map.put("masterID", masterId);
        map.put("street", street);
        map.put("edit", true);

        return "street";
    }


    @PreAuthorize("hasAuthority('VIEW_STREET') or hasAuthority('MAKER_STREET') or hasAuthority('CHECKER_STREET')")
    @RequestMapping(value = "/view/{id}")
    public String viewStreet(@PathVariable("id") Long id, ModelMap map){

        UserInfo currentUser = getUserDetails();
        Street street = baseMasterService.getMasterEntityWithActionsById(Street.class, id, currentUser.getUserEntityId().getUri());
        Hibernate.initialize(street.getCity());
        if (street.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || street.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            Street prevStreet = (Street) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(street.getEntityId());
            map.put("prevStreet", prevStreet);
            map.put("editLink", false);
        } else if (street.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            Street prevStreet = (Street) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(street.getEntityId());
            map.put("prevStreet", prevStreet);
            map.put("viewLink", false);
        }

        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,street.getReasonActInactMap());
        street.setReasonActInactMap(reasonsActiveInactiveMapping);
        String masterName = street.getClass().getSimpleName();
        String uniqueValue = street.getStreetCode();
        String uniqueParameter = "streetCode";
        getActInactReasMapForEditApproved(map,street,masterName,uniqueParameter,uniqueValue);

        map.put("street", street);
        map.put("masterID", masterId);
        map.put("viewable", true);
        map.put("codeViewMode", true);

        return "street";
    }


    @PreAuthorize("hasAuthority('MAKER_STREET')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveStreet(@Validated Street street, BindingResult result, ModelMap map,
                            @RequestParam("createAnotherMaster") boolean createAnotherMaster) {


        Street duplicateStreet = null;
        if(null!=street.getId()){
            duplicateStreet = entityDao.find(Street.class,street.getId());
            if(null != duplicateStreet.getEntityLifeCycleData()){
                street.setEntityLifeCycleData(duplicateStreet.getEntityLifeCycleData());
            }
            if(null != duplicateStreet.getMasterLifeCycleData()){
                street.setMasterLifeCycleData(duplicateStreet.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<>();
        validateMap.put("streetCode", street.getStreetCode());
        List<String> colNameList = checkValidationForDuplicates(street, Street.class, validateMap);
        if (result.hasErrors() || CollectionUtils.isNotEmpty(colNameList)) {
            map.put("street", street);
            map.put("masterID", masterId);
            if (CollectionUtils.isNotEmpty(colNameList) && colNameList.contains("streetCode")) {
                result.rejectValue("streetCode", "label.street.code.validation.exists");
            }
            if(street.getId() != null) {
                Street s = baseMasterService.getMasterEntityById(Street.class, street.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == s.getApprovalStatus() || ApprovalStatus.CLONED == s.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            String masterName = street.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != street.getId()) {

                uniqueValue = duplicateStreet.getStreetCode();
                uniqueParameter = "streetCode";
                getActInactReasMapForEditApproved(map, street, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                street.setReasonActInactMap(reasActInactMap);
            }
            map.put("viewable" , false);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,street.getReasonActInactMap());
            street.setReasonActInactMap(reasonsActiveInactiveMapping);

            map.put("street", street);
            map.put("masterID", masterId);
            return "street";
        }

        boolean eventResult = executeMasterEvent(street,"contextObjectStreet",map);
        if(!eventResult){

            String masterName = street.getClass().getSimpleName();
            String uniqueValue = street.getStreetCode();
            String uniqueParameter = "streetCode";
            getActInactReasMapForEditApproved(map,street,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,street.getReasonActInactMap());
            street.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("street", street);
            map.put("viewable" , false);
            map.put("masterID", masterId);

            return "street";
        }

        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = street.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,street);
            }
            street.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.masterEntityChangedByUser(street, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            Street streetForCreateAnother= new Street();
            streetForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("street", streetForCreateAnother);
            map.put("masterID", masterId);
            return "street";
        }
        map.put("masterID", masterId);

        return "redirect:/app/grid/Street/Street/loadColumnConfig";
    }



    @PreAuthorize("hasAuthority('MAKER_STREET')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated Street street, BindingResult result, ModelMap map,
                                         @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

        Street duplicateStreet = null;
        if(null!=street.getId()){
            duplicateStreet = entityDao.find(Street.class,street.getId());
            if(null != duplicateStreet.getEntityLifeCycleData()){
                street.setEntityLifeCycleData(duplicateStreet.getEntityLifeCycleData());
            }
            if(null != duplicateStreet.getMasterLifeCycleData()){
                street.setMasterLifeCycleData(duplicateStreet.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<>();
        validateMap.put("streetCode", street.getStreetCode());
        List<String> colNameList = checkValidationForDuplicates(street, Street.class, validateMap);
        if (result.hasErrors() || CollectionUtils.isNotEmpty(colNameList)) {
            map.put("street", street);
            map.put("masterID", masterId);
            if (CollectionUtils.isNotEmpty(colNameList) && colNameList.contains("streetCode")) {
                result.rejectValue("streetCode", "label.street.code.validation.exists");
            }
            if(street.getId() != null) {
                Street s = baseMasterService.getMasterEntityById(Street.class, street.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == s.getApprovalStatus() || ApprovalStatus.CLONED == s.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            String masterName = street.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != street.getId()) {

                uniqueValue = duplicateStreet.getStreetCode();
                uniqueParameter = "streetCode";
                getActInactReasMapForEditApproved(map, street, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                street.setReasonActInactMap(reasActInactMap);
            }
            map.put("viewable" , false);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,street.getReasonActInactMap());
            street.setReasonActInactMap(reasonsActiveInactiveMapping);

            map.put("street", street);
            map.put("masterID", masterId);
            return "street";
        }

        boolean eventResult = executeMasterEvent(street,"contextObjectStreet",map);
        if(!eventResult){

            String masterName = street.getClass().getSimpleName();
            String uniqueValue = street.getStreetCode();
            String uniqueParameter = "streetCode";
            getActInactReasMapForEditApproved(map,street,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,street.getReasonActInactMap());
            street.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("street", street);
            map.put("viewable" , false);
            map.put("masterID", masterId);

            return "street";
        }

        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = street.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,street);
            }
            street.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.saveAndSendForApproval(street, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            Street streetForCreateAnother= new Street();
            streetForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("street", streetForCreateAnother);
            map.put("masterID", masterId);
            return "street";
        }
        map.put("masterID", masterId);

        return "redirect:/app/grid/Street/Street/loadColumnConfig";

    }




}
