/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.web.fileconsolidator.controller;

import com.nucleus.NeutrinoUUIDGenerator;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.*;
import com.nucleus.makerchecker.*;
import com.nucleus.master.BaseMasterUtils;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.fileconsolidator.businessobject.FileConsolidatorSetupBusinessObject;
import com.nucleus.web.fileconsolidator.util.FileConsolidatorGridUtility;
import com.nucleus.web.fileconsolidator.util.UserFormatHeaderChangesVO;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author Nucleus Software India Pvt Ltd This field is being used for
 *         controlling fileUploadDownloadUserFormat CRUD and task allocation work-flow related
 *         operations.
 */
@Controller
@Transactional
@RequestMapping(value = "/FileUploadDownloadUserFormat")
public class UserFormatController extends BaseController{

    String masterId = "FileUploadDownloadUserFormat";

    public static final String FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME = "fileUploadDownloadUserFormat";

    String childId = "FileUploadDownloadUserFormatDetail";
    @Inject
    @Named("stringEncryptor")
    private StandardPBEStringEncryptor encryptor;
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;

    @Inject
    @Named("fileConsolidatorSetupBusinessObject")
    public FileConsolidatorSetupBusinessObject fileConsolidatorSetupBusinessObject;

    /*Method Added to send current Entity Uri for working of comments,activity,history,notes*/
    @ModelAttribute("currentEntityClassName")
    public String getEntityClassName() {
        return FileUploadDownloadUserFormat.class.getName();
    }


    @PreAuthorize("hasAuthority('MAKER_FILEUPLOADDOWNLOADUSERFORMAT')")
    @RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
    public String saveAndSendForApproval(@RequestParam("userFormatChangesJsonString") String userFormatChangesJsonString, ModelMap map) {
        UserFormatHeaderChangesVO userFormatChangesVO = (UserFormatHeaderChangesVO) (new JSONDeserializer()).use( null, UserFormatHeaderChangesVO.class ).deserialize( userFormatChangesJsonString );
        FileUploadDownloadUserFormat fileUploadDownloadUserFormat = fileConsolidatorSetupBusinessObject.fetchAndMergeChangesFromVO(userFormatChangesVO);
        String boilerPlateReturn = saveBoilerPlate(fileUploadDownloadUserFormat, null, map);
        if (boilerPlateReturn.equals(FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME)){
            return FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME;
        }

        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = fileUploadDownloadUserFormat.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,fileUploadDownloadUserFormat);
            }
            fileUploadDownloadUserFormat.setReasonActInactMap(reasonsActiveInactiveMapping);

            makerCheckerService.saveAndSendForApproval(fileUploadDownloadUserFormat, user);
        }

        //No "create another master" option
        return boilerPlateReturn;


    }

    @PreAuthorize("hasAuthority('MAKER_FILEUPLOADDOWNLOADUSERFORMAT')")
    @RequestMapping(value = "/edit/{id}")
    public String editFileUploadDownloadUserFormat(@PathVariable("id") Long id, FileUploadDownloadUserFormat fileUploadDownloadUserFormat, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        fileUploadDownloadUserFormat = baseMasterService.getMasterEntityWithActionsById(FileUploadDownloadUserFormat.class, id, currentUser.getUserEntityId()
                .getUri());

        initializeUserFormatForDisplay(fileUploadDownloadUserFormat);

        if (fileUploadDownloadUserFormat.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
            FileUploadDownloadUserFormat prevFileUploadDownloadUserFormat = (FileUploadDownloadUserFormat) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(fileUploadDownloadUserFormat
                    .getEntityId());
            map.put("prevFileUploadDownloadUserFormat", prevFileUploadDownloadUserFormat);
            map.put("editLink", false);
        }
        if(!(ApprovalStatus.UNAPPROVED_ADDED == fileUploadDownloadUserFormat.getApprovalStatus() || ApprovalStatus.CLONED == fileUploadDownloadUserFormat.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }
        removeChildDeletedRecord(fileUploadDownloadUserFormat);
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,fileUploadDownloadUserFormat.getReasonActInactMap());
        fileUploadDownloadUserFormat.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,fileUploadDownloadUserFormat);
        String masterName = fileUploadDownloadUserFormat.getClass().getSimpleName();

        String uniqueValue = fileUploadDownloadUserFormat.getFormatName();
        String uniqueParameter = "formatName";
        getActInactReasMapForEditApproved(map,fileUploadDownloadUserFormat,masterName,uniqueParameter,uniqueValue);
        map.put("viewable" ,false);

        map.put(FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME, fileUploadDownloadUserFormat);
        map.put("masterID", masterId);
        map.put("edit", true);
        map.put("viewable" ,false);

        ArrayList<String> actions = (ArrayList<String>) fileUploadDownloadUserFormat.getViewProperties().get("actions");
        if (actions != null) {
            for (String act : actions) {
                map.put("act" + act, false);
            }
        }
        return FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME;
    }

    @PreAuthorize("hasAuthority('VIEW_FILEUPLOADDOWNLOADUSERFORMAT') or hasAuthority('MAKER_FILEUPLOADDOWNLOADUSERFORMAT') or hasAuthority('CHECKER_FILEUPLOADDOWNLOADUSERFORMAT')")
    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String viewFileUploadDownloadUserFormat(@PathVariable("id") Long id, ModelMap map, HttpServletRequest request) {

        FileUploadDownloadUserFormat fileUploadDownloadUserFormat =   (FileUploadDownloadUserFormat) BaseMasterUtils.getMergeEditedRecords(FileUploadDownloadUserFormat.class, id);

        initializeUserFormatForDisplay(fileUploadDownloadUserFormat);

        if (fileUploadDownloadUserFormat.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || fileUploadDownloadUserFormat.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
            FileUploadDownloadUserFormat prevFileUploadDownloadUserFormat = (FileUploadDownloadUserFormat) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(fileUploadDownloadUserFormat
                    .getEntityId());
            map.put("prevFileUploadDownloadUserFormat", prevFileUploadDownloadUserFormat);
            map.put("editLink", false);
        } else if (fileUploadDownloadUserFormat.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            FileUploadDownloadUserFormat prevFileUploadDownloadUserFormat = (FileUploadDownloadUserFormat) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(fileUploadDownloadUserFormat
                    .getEntityId());
            map.put("prevFileUploadDownloadUserFormat", prevFileUploadDownloadUserFormat);
            map.put("viewLink", false);
        }

        removeChildDeletedRecord(fileUploadDownloadUserFormat);
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,fileUploadDownloadUserFormat.getReasonActInactMap());
        fileUploadDownloadUserFormat.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,fileUploadDownloadUserFormat);
        String masterName = fileUploadDownloadUserFormat.getClass().getSimpleName();
        String uniqueValue = fileUploadDownloadUserFormat.getFormatName();
        String uniqueParameter = "formatName";
        getActInactReasMapForEditApproved(map,fileUploadDownloadUserFormat,masterName,uniqueParameter,uniqueValue);

        map.put(FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME, fileUploadDownloadUserFormat);
        map.put("masterID", masterId);
        map.put("viewable", true);
        map.put("codeViewMode", true);
        if (fileUploadDownloadUserFormat.getViewProperties() != null) {

            ArrayList<String> actions = (ArrayList<String>) fileUploadDownloadUserFormat.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }

            }

        }

        return FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME;
    }

    @PreAuthorize("hasAuthority('MAKER_FILEUPLOADDOWNLOADUSERFORMAT')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveFileUploadDownloadUserFormat(@RequestParam("userFormatChangesJsonString") String userFormatChangesJsonString, ModelMap map) {
        UserFormatHeaderChangesVO userFormatChangesVO = (UserFormatHeaderChangesVO) (new JSONDeserializer()).use( null, UserFormatHeaderChangesVO.class ).deserialize( userFormatChangesJsonString );
        FileUploadDownloadUserFormat fileUploadDownloadUserFormat = fileConsolidatorSetupBusinessObject.fetchAndMergeChangesFromVO(userFormatChangesVO);
        String boilerPlateReturn = saveBoilerPlate(fileUploadDownloadUserFormat, null, map);
        if (boilerPlateReturn.equals(FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME)){
            return FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME;
        }

        User user = getUserDetails().getUserReference();
        if (user != null) {
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = fileUploadDownloadUserFormat.getReasonActInactMap();
            if (reasonsActiveInactiveMapping != null) {
                saveActInactReasonForMaster(reasonsActiveInactiveMapping, fileUploadDownloadUserFormat);
            }
            fileUploadDownloadUserFormat.setReasonActInactMap(reasonsActiveInactiveMapping);

            if (fileUploadDownloadUserFormat.getId() == null) {
                createUserFormat(fileUploadDownloadUserFormat, user);
            } else {
                updateUserFormat(fileUploadDownloadUserFormat, user);
            }

            //makerCheckerService.startMakerCheckerFlow(fileUploadDownloadUserFormat.getEntityId(), user.getEntityId());
        }

        //No "create another master" option
        return boilerPlateReturn;


    }

    private String saveBoilerPlate(FileUploadDownloadUserFormat fileUploadDownloadUserFormat, BindingResult result, ModelMap map) {
        removeDeletedRecordByOrder(fileUploadDownloadUserFormat);

        FileUploadDownloadUserFormat dubplicateFileUploadDownloadUserFormat = null;
        if (null != fileUploadDownloadUserFormat.getId()) {
            dubplicateFileUploadDownloadUserFormat = entityDao.find(FileUploadDownloadUserFormat.class, fileUploadDownloadUserFormat.getId());
            if (null != dubplicateFileUploadDownloadUserFormat.getEntityLifeCycleData()) {
                fileUploadDownloadUserFormat.setEntityLifeCycleData(dubplicateFileUploadDownloadUserFormat.getEntityLifeCycleData());
            }
            if (null != dubplicateFileUploadDownloadUserFormat.getMasterLifeCycleData()) {
                fileUploadDownloadUserFormat.setMasterLifeCycleData(dubplicateFileUploadDownloadUserFormat.getMasterLifeCycleData());
            }
        }

        Map<String, Object> validateMap = new HashMap<String, Object>();
        validateMap.put("formatName", fileUploadDownloadUserFormat.getFormatName());

        List<String> colNameList = checkValidationForDuplicates(fileUploadDownloadUserFormat, FileUploadDownloadUserFormat.class, validateMap);

        List<String> codeList = new ArrayList<String>();

        Hibernate.initialize(fileUploadDownloadUserFormat.getUserFormatDetail());
        List<FileUploadDownloadUserFormatDetail> modifiedUserFormatDetail = (List<FileUploadDownloadUserFormatDetail>) BaseMasterUtils.getUpdatedChildRecordsFromChangedChildList(fileUploadDownloadUserFormat, FileUploadDownloadUserFormat.USER_FORMAT_DETAIL_FIELD, fileUploadDownloadUserFormat.getUserFormatDetail());

        fileUploadDownloadUserFormat.setUserFormatDetail(modifiedUserFormatDetail);

        if (result != null && result.hasErrors() || (colNameList != null && colNameList.size() > 0)) {
            if (fileUploadDownloadUserFormat.getId() != null) {
                FileUploadDownloadUserFormat gr = baseMasterService.getMasterEntityById(FileUploadDownloadUserFormat.class, fileUploadDownloadUserFormat.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == gr.getApprovalStatus() || ApprovalStatus.CLONED == gr.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            String masterName = fileUploadDownloadUserFormat.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != fileUploadDownloadUserFormat.getId()) {
                //FileUploadDownloadUserFormat Reg = baseMasterService.findById(FileUploadDownloadUserFormat.class, fileUploadDownloadUserFormat.getId());
                uniqueValue = dubplicateFileUploadDownloadUserFormat.getFormatName();
                uniqueParameter = "formatName";
                getActInactReasMapForEditApproved(map, fileUploadDownloadUserFormat, masterName, uniqueParameter, uniqueValue);
            } else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                fileUploadDownloadUserFormat.setReasonActInactMap(reasActInactMap);
            }

            map.put("viewable", false);
            map.put("edit", true);

            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map, fileUploadDownloadUserFormat.getReasonActInactMap());
            fileUploadDownloadUserFormat.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put(FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME, fileUploadDownloadUserFormat);
            map.put("masterID", masterId);


            if (colNameList != null && !colNameList.isEmpty() && colNameList.contains("formatName")) {
                result.rejectValue("formatName", "label.fileUploadDownloadUserFormatName.validation.exists");
            }
            return FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME;
        }

        boolean eventResult = executeMasterEvent(fileUploadDownloadUserFormat, "contextObjectFileUploadDownloadUserFormat", map);
        if (!eventResult) {
            String masterName = fileUploadDownloadUserFormat.getClass().getSimpleName();
            String uniqueParameter = "formatName";
            String uniqueValue = fileUploadDownloadUserFormat.getFormatName();
            getActInactReasMapForEditApproved(map, fileUploadDownloadUserFormat, masterName, uniqueParameter, uniqueValue);
            map.put("edit", true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map, fileUploadDownloadUserFormat.getReasonActInactMap());
            fileUploadDownloadUserFormat.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("viewable", false);
            map.put("FileUploadDownloadUserFormat", fileUploadDownloadUserFormat);
            map.put("masterID", masterId);
            map.put("activeFlag", fileUploadDownloadUserFormat.isActiveFlag());
            return FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME;
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/FileUploadDownloadUserFormat/FileUploadDownloadUserFormat/loadColumnConfig";
    }

    private String createMasterBoilerPlate(FileUploadDownloadUserFormat fileUploadDownloadUserFormat, BindingResult result, ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
        removeDeletedRecordByOrder(fileUploadDownloadUserFormat);

        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            FileUploadDownloadUserFormat fileUploadDownloadUserFormatForCreateAnother = new FileUploadDownloadUserFormat();
            fileUploadDownloadUserFormatForCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put(FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME, fileUploadDownloadUserFormatForCreateAnother);
            map.put("masterID", masterId);
            return FILE_UPLOAD_DOWNLOAD_USER_FORMAT_VIEWNAME;
        }

        map.put("masterID", masterId);
        return "redirect:/app/grid/FileUploadDownloadUserFormat/FileUploadDownloadUserFormat/loadColumnConfig";
    }


    private void removeChildDeletedRecord(FileUploadDownloadUserFormat fileUploadDownloadUserFormat) {
        List<FileUploadDownloadUserFormatDetail> fileUploadDownloadUserFormatDetaillist = fileUploadDownloadUserFormat.getUserFormatDetail();
        if (CollectionUtils.isNotEmpty(fileUploadDownloadUserFormatDetaillist)) {
            Iterator<FileUploadDownloadUserFormatDetail> iterator = fileUploadDownloadUserFormatDetaillist.iterator();
            while (iterator.hasNext()) {
                FileUploadDownloadUserFormatDetail fileUploadDownloadUserFormatDetail = iterator.next();
                if (fileUploadDownloadUserFormatDetail.getApprovalStatus() == ApprovalStatus.DELETED_APPROVED_IN_HISTORY) {
                    iterator.remove();
                }
            }
        }

    }

    private void removeDeletedRecordByOrder(FileUploadDownloadUserFormat fileUploadDownloadUserFormat) {
        List<FileUploadDownloadUserFormatDetail> fileUploadDownloadUserFormatDetaillist = fileUploadDownloadUserFormat.getUserFormatDetail();
        if (CollectionUtils.isNotEmpty(fileUploadDownloadUserFormatDetaillist)) {
            Iterator<FileUploadDownloadUserFormatDetail> iterator = fileUploadDownloadUserFormatDetaillist.iterator();
            while (iterator.hasNext()) {
                FileUploadDownloadUserFormatDetail fileUploadDownloadUserFormatDetail = iterator.next();
                if (fileUploadDownloadUserFormatDetail.getFieldOrder() == null) {
                    iterator.remove();
                }
            }
        }

    }

    private static FileUploadDownloadUserFormat initializeUserFormatForDisplay(FileUploadDownloadUserFormat userFormatToInitialize){
        if (userFormatToInitialize != null && userFormatToInitialize.getUserFormatDetail() != null) {
            Hibernate.initialize(userFormatToInitialize.getUserFormatDetail());
        }

        if (userFormatToInitialize != null && userFormatToInitialize.getBaseFormat() != null && userFormatToInitialize.getBaseFormat().getSystemFormatDetail() != null) {
            Hibernate.initialize(userFormatToInitialize.getBaseFormat().getSystemFormatDetail());
        }

        return userFormatToInitialize;
    }


    private FileUploadDownloadUserFormat createUserFormat(FileUploadDownloadUserFormat changedEntity, User user) {
        EntityId userEntityId = user.getEntityId();
        UnapprovedEntityData unapprovedEntityData = new UnapprovedEntityData();
        unapprovedEntityData.setUserEntityId(userEntityId);
        NeutrinoValidator.notNull(changedEntity, "User Format Entity Cannot be saved null");
        makerCheckerService.masterEntityChangedByUser(changedEntity, user);

        return changedEntity;
    }

    private FileUploadDownloadUserFormat updateUserFormat(FileUploadDownloadUserFormat changedEntity, User user) {
        EntityId userEntityId = user.getEntityId();
        UnapprovedEntityData unapprovedEntityData = new UnapprovedEntityData();
        unapprovedEntityData.setUserEntityId(userEntityId);
        NeutrinoValidator.notNull(changedEntity, "User Format Entity Cannot be updated to null");

        return (FileUploadDownloadUserFormat) makerCheckerService.masterEntityChangedByUser(changedEntity, user);

    }

    @PreAuthorize("hasAuthority('VIEW_FILEUPLOADDOWNLOADUSERFORMAT') or hasAuthority('MAKER_FILEUPLOADDOWNLOADUSERFORMAT') or hasAuthority('CHECKER_FILEUPLOADDOWNLOADUSERFORMAT')")
    @RequestMapping(value = "/getFormatDetailNestedTable", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView getFormatDetailNestedTable(
            @RequestParam("tableConfigKey") String tableConfigKey,
            @RequestParam("baseFormatId") String baseFormatId,
            @RequestParam("userFormatId") String userFormatId,
            @RequestParam("baseRecordIdentifierId") String baseRecordIdentifierId,
            @RequestParam("userRecordIdentifierId") String userRecordIdentifierId,
            @RequestParam("viewable") String viewMode
    ) {

        ModelAndView nestedTable = new ModelAndView("fileUploadDownloadUserFormatDetailNestedGrid");

        FileUploadDownloadUserFormat userFormat = null;
        FileUploadDownloadBaseFormat baseFormat = null;
        FileUploadDownloadUserRecordIdentifierDetail userRecordIdentifier = null;
        FileUploadDownloadBaseRecordIdentifierDetail baseRecordIdentifier = null;

        if(StringUtils.isNotBlank(userFormatId)){
            //userFormat =  entityDao.find(FileUploadDownloadUserFormat.class,Long.valueOf(userFormatId));
            userFormat =   (FileUploadDownloadUserFormat) BaseMasterUtils.getMergeEditedRecords(FileUploadDownloadUserFormat.class, Long.valueOf(userFormatId));
            initializeUserFormatForDisplay(userFormat);
        }
        if(StringUtils.isNotBlank(baseFormatId)){
            baseFormat =  entityDao.find(FileUploadDownloadBaseFormat.class,Long.valueOf(baseFormatId));
        }
        if(StringUtils.isNotBlank(userRecordIdentifierId)){
            userRecordIdentifier =  entityDao.find(FileUploadDownloadUserRecordIdentifierDetail.class,Long.valueOf(userRecordIdentifierId));
        }
        if(StringUtils.isNotBlank(baseRecordIdentifierId)){
            baseRecordIdentifier =  entityDao.find(FileUploadDownloadBaseRecordIdentifierDetail.class,Long.valueOf(baseRecordIdentifierId));
        }

        List<Object> nestedTableData = FileConsolidatorGridUtility.generateNestedGridData(tableConfigKey,userFormat,baseFormat,baseRecordIdentifier,userRecordIdentifier);

        nestedTableData = fileConsolidatorSetupBusinessObject.fetchLocalizedMessages(nestedTableData,getUserLocale());

        List<ColumnConfiguration> columnConfigList = masterConfigurationRegistry
                .getColumnConfigurationList(tableConfigKey);

        List<ActionConfiguration> actionConfigList = masterConfigurationRegistry
                .getActionConfigurationList(tableConfigKey);

        nestedTable.addObject("columnConfigList",columnConfigList);
        nestedTable.addObject("actionConfigList",actionConfigList);

        JSONSerializer jsonifier = new JSONSerializer();
        String jsonString = jsonifier.exclude("*.class").deepSerialize(nestedTableData);

        nestedTable.addObject("aaData",jsonString);

        String tableId = tableConfigKey+"_"+(new NeutrinoUUIDGenerator()).generateUuid();
        nestedTable.addObject("tableId",tableId.replace("-",""));

        if(userFormat == null && baseFormat != null){
            viewMode = Boolean.TRUE.toString();
        }

        nestedTable.addObject("viewable", viewMode);

        //Rowreorder
        nestedTable.addObject("enableRowReorder", FileConsolidatorGridUtility.isRowReorderApplicable(tableConfigKey,viewMode));

        return nestedTable;

    }

}
