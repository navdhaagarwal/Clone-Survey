
package com.nucleus.web.ipaddress;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.misc.util.IpAddressUtils;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.user.AccessType;
import com.nucleus.user.ipaddress.IpAddress;
import org.hibernate.Hibernate;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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
import org.springframework.web.util.UriUtils;

import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;

@Controller
@RequestMapping(value = "/IpAddress")
public class IpAddressController extends BaseController {

    @InitBinder("ipAddress")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new IpAddressValidator());
    }
    @Inject
    @Named("stringEncryptor")
    private StandardPBEStringEncryptor encryptor;
    private static final String MASTER_ID    =   "IpAddress";

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService      			  makerCheckerService;

    final static String masterId		 = 		  "IpAddress";

    @Inject
    @Named("baseMasterService")
    private BaseMasterService   				  baseMasterService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() throws UnsupportedEncodingException{

        String encrypt = "enc_" + encryptor.encrypt(IpAddress.class.getName());
        String returnUri = UriUtils.encodeQueryParam(encrypt, "UTF-8");

        return returnUri;
    }

    @PreAuthorize("hasAuthority('MAKER_IPADDRESS')")
    @RequestMapping(value = "/create")
    public String createIpAddress(ModelMap map) {
        IpAddress ipAddress = new IpAddress();
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        ipAddress.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",ipAddress.getReasonActInactMap());
        ipAddress.setAccessType(genericParameterService.findByCode(AccessType.INTRANET,AccessType.class));
        map.put("ipAddress", ipAddress);
        map.put("masterID", MASTER_ID);
        return "ipAddress";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveIpAddress(@Validated IpAddress ipAddress, BindingResult result, ModelMap map,
                                @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        BaseLoggers.flowLogger.debug(ipAddress.toString());

        IpAddress dubplicateIpAddress = null;
        if(null!=ipAddress.getId()){
            dubplicateIpAddress = entityDao.find(IpAddress.class,ipAddress.getId());
            if(null != dubplicateIpAddress.getEntityLifeCycleData()){
                ipAddress.setEntityLifeCycleData(dubplicateIpAddress.getEntityLifeCycleData());
            }
            if(null != dubplicateIpAddress.getMasterLifeCycleData()){
                ipAddress.setMasterLifeCycleData(dubplicateIpAddress.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();
        //   put the unique field for checking duplicates
        validateMap.put("ipAddress", ipAddress.getIpAddress());
        List<String> colNameList = checkValidationForDuplicates(ipAddress, IpAddress.class, validateMap);
        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0) ) {
            String masterName = ipAddress.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != ipAddress.getId()) {
                //IpAddress ipAdd = baseMasterService.findById(IpAddress.class, ipAddress.getId());
                uniqueValue = dubplicateIpAddress.getIpAddress();
                uniqueParameter = "ipAddress";
                getActInactReasMapForEditApproved(map, ipAddress, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                ipAddress.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,ipAddress.getReasonActInactMap());
            ipAddress.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("edit" , true);
            map.put("viewable" , false);
            map.put("ipAddress", ipAddress);
            map.put("masterID", masterId);


            //add the column name which is unique
            if (colNameList != null && colNameList.size() > 0 && colNameList.contains("ipAddress")) {
                result.rejectValue("ipAddress", "label.ip.address.validation.exists");
            }

            return "ipAddress";
        }

        boolean eventResult = executeMasterEvent(ipAddress,"contextObjectIpAddress",map);
        if(!eventResult){
            //getActInactReasMapForEdit(map,ipAddress);
            String masterName = ipAddress.getClass().getSimpleName();
            String uniqueValue = ipAddress.getIpAddress();
            String uniqueParameter = "ipAddress";
            getActInactReasMapForEditApproved(map,ipAddress,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,ipAddress.getReasonActInactMap());
            ipAddress.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("ipAddress", ipAddress);
            map.put("viewable" , false);
            map.put("masterID", masterId);
            map.put("activeFlag",ipAddress.isActiveFlag());
            return "ipAddress";
        }

        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = ipAddress.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,ipAddress);
            }
            ipAddress.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.masterEntityChangedByUser(ipAddress, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            IpAddress ipAddressForCreateAnother= new IpAddress();
            ipAddressForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("ipAddress", ipAddressForCreateAnother);
            map.put("masterID", masterId);
            map.put("viewable", false);
            return "ipAddress";

        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/IpAddress/IpAddress/loadColumnConfig";
    }

    @PreAuthorize("hasAuthority('MAKER_IPADDRESS')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@Validated IpAddress ipAddress, BindingResult result, ModelMap map,
                                         @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        BaseLoggers.flowLogger.debug(ipAddress.toString());

        IpAddress dubplicateIpAddress = null;
        if(null!=ipAddress.getId()){
            dubplicateIpAddress = entityDao.find(IpAddress.class,ipAddress.getId());
            if(null != dubplicateIpAddress.getEntityLifeCycleData()){
                ipAddress.setEntityLifeCycleData(dubplicateIpAddress.getEntityLifeCycleData());
            }
            if(null != dubplicateIpAddress.getMasterLifeCycleData()){
                ipAddress.setMasterLifeCycleData(dubplicateIpAddress.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();

        //   put the unique field for checking duplicates
        validateMap.put("ipAddress", ipAddress.getIpAddress());
        List<String> colNameList = checkValidationForDuplicates(ipAddress, IpAddress.class, validateMap);

        if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            String masterName = ipAddress.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != ipAddress.getId()) {
                //IpAddress ipAdd = baseMasterService.findById(IpAddress.class, ipAddress.getId());
                uniqueValue = dubplicateIpAddress.getIpAddress();
                uniqueParameter = "ipAddress";
                getActInactReasMapForEditApproved(map, ipAddress, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                ipAddress.setReasonActInactMap(reasActInactMap);
            }
            map.put("viewable" , false);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,ipAddress.getReasonActInactMap());
            ipAddress.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("ipAddress", ipAddress);
            map.put("masterID", masterId);

            //add the column name which is unique
            if (colNameList != null && colNameList.size() > 0 && colNameList.contains("ipAddress")) {
                result.rejectValue("ipAddress", "label.ip.address.validation.exists");
            }

            return "ipAddress";
        }

        boolean eventResult = executeMasterEvent(ipAddress,"contextObjectIpAddress",map);
        if(!eventResult){
            //getActInactReasMapForEdit(map,ipAddress);
            String masterName = ipAddress.getClass().getSimpleName();
            String uniqueValue = ipAddress.getIpAddress();
            String uniqueParameter = "ipAddress";
            getActInactReasMapForEditApproved(map,ipAddress,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,ipAddress.getReasonActInactMap());
            ipAddress.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("ipAddress", ipAddress);
            map.put("viewable" , false);
            map.put("masterID", masterId);
            map.put("activeFlag",ipAddress.isActiveFlag());
            return "ipAddress";
        }

        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = ipAddress.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,ipAddress);
            }
            ipAddress.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.saveAndSendForApproval(ipAddress, user);
        }
        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            IpAddress ipAddressForCreateAnother= new IpAddress();
            ipAddressForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("ipAddress", ipAddressForCreateAnother);
            map.put("masterID", masterId);
            map.put("viewable", false);
            return "ipAddress";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/IpAddress/IpAddress/loadColumnConfig";
    }

    @PreAuthorize("hasAuthority('MAKER_IPADDRESS')")
    @RequestMapping(value = "/edit/{id}")
    public String editIpAddress(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        IpAddress ipAddress = baseMasterService.getMasterEntityWithActionsById(IpAddress.class, id, currentUser.getUserEntityId()
                .getUri());

        Hibernate.initialize(ipAddress.getAccessType());
        if (ipAddress.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            IpAddress prevIpAddress = (IpAddress) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(ipAddress.getEntityId());
            map.put("prevIpAddress", prevIpAddress);
            map.put("editLink", true);
        }
        if (ipAddress.getApprovalStatus() == ApprovalStatus.APPROVED
                || ipAddress.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || ipAddress.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            map.put("disableIsoCode", true);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,ipAddress.getReasonActInactMap());
        ipAddress.setReasonActInactMap(reasonsActiveInactiveMapping);
        String masterName = ipAddress.getClass().getSimpleName();
        String uniqueValue = ipAddress.getIpAddress();
        String uniqueParameter = "ipAddress";
        getActInactReasMapForEditApproved(map,ipAddress,masterName,uniqueParameter,uniqueValue);
        map.put("viewable" ,false);
        map.put("ipAddress", ipAddress);
        map.put("masterID", masterId);
        map.put("edit", true);

        return "ipAddress";
    }

    @PreAuthorize("hasAuthority('VIEW_IPADDRESS') or hasAuthority('MAKER_IPADDRESS') or hasAuthority('CHECKER_IPADDRESS')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewIpAddress(@PathVariable("id") Long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        IpAddress ipAddress = baseMasterService.getMasterEntityWithActionsById(IpAddress.class, id, currentUser.getUserEntityId()
                .getUri());
        Hibernate.initialize(ipAddress.getAccessType());
        if (ipAddress.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || ipAddress.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            IpAddress prevIpAddress = (IpAddress) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(ipAddress
                    .getEntityId());
            map.put("prevIpAddress", prevIpAddress);
            map.put("editLink", true);
        } else if (ipAddress.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            IpAddress prevIpAddress = (IpAddress) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(ipAddress
                    .getEntityId());
            map.put("prevIpAddress", prevIpAddress);
            map.put("viewLink", true);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,ipAddress.getReasonActInactMap());
        ipAddress.setReasonActInactMap(reasonsActiveInactiveMapping);
        String masterName = ipAddress.getClass().getSimpleName();
        String uniqueValue = ipAddress.getIpAddress();
        String uniqueParameter = "ipAddress";
        getActInactReasMapForEditApproved(map,ipAddress,masterName,uniqueParameter,uniqueValue);
        map.put("ipAddress", ipAddress);
        map.put("masterID", masterId);
        map.put("viewable", true);
        if (ipAddress.getViewProperties() != null) {
            @SuppressWarnings("unchecked")
            ArrayList<String> actions = (ArrayList<String>) ipAddress.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }
        return "ipAddress";
    }

}