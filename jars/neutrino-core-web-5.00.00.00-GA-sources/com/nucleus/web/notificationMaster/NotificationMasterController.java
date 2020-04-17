package com.nucleus.web.notificationMaster;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;


import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.persistence.HibernateUtils;


import com.nucleus.questionairePdf.QuestionairePdfHandler;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.nucleus.contact.EMailInfo;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.dao.query.MapQueryExecutor;
import com.nucleus.document.core.entity.Document;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.AttachmentEncryptionPolicy;
import com.nucleus.letterMaster.LetterType;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.notificationMaster.NotificationMaster;
import com.nucleus.notificationMaster.NotificationMasterType;
import com.nucleus.notificationMaster.service.NotificationMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

@Controller
public class NotificationMasterController extends BaseController {

    @Inject
    @Named("userService")
    private UserService           userService;

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService   makerCheckerService;

    @Inject
    @Named("notificationMasterService")
    private NotificationMasterService   notificationMasterService;

    @Inject
    @Named("couchDataStoreDocumentService")
    private DatastorageService docService2;

    @Inject
    @Named("entityDao")
    private EntityDao             entityDao;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Autowired(required = false)
    @Qualifier("questionairePdfHandlerImpl")
    QuestionairePdfHandler questionairePdfHandler;

    private static final String                        masterId                     = "NotificationMaster";
    @Value(value = "${notificationmaster.tab.config}")
    private String  notificationMasterConfig;

    private static final String            ATTACHMENT_ENCRYTION_POLICY_LIST       = "attachmentEncryptionPolicyList";

    private static final String            LETTER_TYPE_LIST       = "letterTypeList";

    private static final String            LETTER_TYPE_PARENT_CODE       = "letterTemplate";

    @PreAuthorize("hasAuthority('MAKER_NOTIFICATIONMASTER')")
    @RequestMapping("/NotificationMaster/create")
    public String createNotificationMaster(ModelMap map) {
        ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
        NotificationMaster notificationMaster= new NotificationMaster();
        notificationMaster.setReasonActInactMap(reasActInactMap);
        map.put("reasonsActiveInactiveMapping",notificationMaster.getReasonActInactMap());
        map.put("notificationMaster", notificationMaster);
        map.put("masterID", masterId);
        List<User> userList = getListofUsers();
        map.put("userList", userList);
        map.put(ATTACHMENT_ENCRYTION_POLICY_LIST, getAttachmentEncryptionPolicyBySourceProduct());
        map.put(LETTER_TYPE_LIST, getLetterTypeListByParentCode());
        map.put("notificationMasterConfig", notificationMasterConfig);
        return "notificationMaster";
    }


    @PreAuthorize("hasAuthority('MAKER_NOTIFICATIONMASTER')")
    @RequestMapping(value = "/NotificationMaster/getAttachedDocument/{documentId}")
    @ResponseBody
    public HttpEntity<FileSystemResource> getDocumentFromId(ModelMap map, @PathVariable("documentId") String documentId)
            throws IOException {
        File documnetFile_0 = docService2.retriveDocument(documentId);
        List<Map<String, ?>> listOfMap = getAttachedDocumnetDescription(documentId);
        Map<String, ?> documnetMap = null;
        if (listOfMap != null) {
            documnetMap = listOfMap.get(0);
        }
        String fileName = (documnetMap != null && documnetMap.containsKey("uploadedFileName")) ? (String) documnetMap
                .get("uploadedFileName") : "tmpdocumnent01";
        MediaType mediaType = (documnetMap != null && documnetMap.containsKey("uploadedFileName")) ? MediaType
                .parseMediaType((String) documnetMap.get("contentType")) : MediaType.APPLICATION_OCTET_STREAM;
        FileSystemResource fileSystemResource = new FileSystemResource(documnetFile_0);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentDispositionFormData("attachment", fileName);
        responseHeaders.setContentType(mediaType);
        HttpEntity<FileSystemResource> fileEntity = new HttpEntity<FileSystemResource>(fileSystemResource, responseHeaders);
        return fileEntity;
    }

    @PreAuthorize("hasAuthority('MAKER_NOTIFICATIONMASTER')")
    @RequestMapping("/NotificationMaster/save")
    public String saveNotificationMaster(@ModelAttribute @Validated NotificationMaster notificationMaster,
                                         BindingResult result, ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster,
                                         @RequestParam("attachedFile") CommonsMultipartFile attachedFile,
                                         @RequestParam("eBodyFile") CommonsMultipartFile eBodyFile) throws IOException {
        Map<String, Object> validateMap = new HashMap<String, Object>();

        NotificationMaster dubplicateNotificationMaster = null;
        if(null!=notificationMaster.getId()){
            dubplicateNotificationMaster = entityDao.find(NotificationMaster.class,notificationMaster.getId());
            if(null != dubplicateNotificationMaster.getEntityLifeCycleData()){
                notificationMaster.setEntityLifeCycleData(dubplicateNotificationMaster.getEntityLifeCycleData());
            }
            if(null != dubplicateNotificationMaster.getMasterLifeCycleData()){
                notificationMaster.setMasterLifeCycleData(dubplicateNotificationMaster.getMasterLifeCycleData());
            }
        }

        validateMap.put("notificationCode", notificationMaster.getNotificationCode());

        List<String> colNameList = checkValidationForDuplicates(notificationMaster, NotificationMaster.class,
                validateMap);

        if (result.hasErrors() || (colNameList != null && !colNameList.isEmpty())) {
            if(notificationMaster.getId() != null) {
                NotificationMaster nm = baseMasterService.getMasterEntityById(NotificationMaster.class, notificationMaster.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == nm.getApprovalStatus() || ApprovalStatus.CLONED == nm.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            List<User> userList = getListofUsers();
            map.put("userList", userList);
            map.put(ATTACHMENT_ENCRYTION_POLICY_LIST, getAttachmentEncryptionPolicyBySourceProduct());
            map.put(LETTER_TYPE_LIST, getLetterTypeListByParentCode());
            map.put("notificationMasterConfig", notificationMasterConfig);
            String masterName = notificationMaster.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != notificationMaster.getId()) {
                //NotificationMaster notifMas = baseMasterService.findById(NotificationMaster.class, notificationMaster.getId());
                uniqueValue = dubplicateNotificationMaster.getNotificationCode();
                uniqueParameter = "notificationCode";
                getActInactReasMapForEditApproved(map, notificationMaster, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                notificationMaster.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,notificationMaster.getReasonActInactMap());
            notificationMaster.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("edit" , true);
            map.put("viewable" , false);
            map.put("notification", notificationMaster);
            map.put("masterID", masterId);
            if (notificationMaster != null && colNameList != null && !colNameList.isEmpty()) {
                result.rejectValue("notificationCode", "label.notificationCode.validation.exists");
            }
            return "notificationMaster";
        }

        boolean eventResult = executeMasterEvent(notificationMaster,"contextObjectNotificationMaster",map);
        if(!eventResult){
            // getActInactReasMapForEdit(map,currency);
            String masterName = notificationMaster.getClass().getSimpleName();
            String uniqueParameter = "notificationCode";
            String uniqueValue = notificationMaster.getNotificationCode();
            getActInactReasMapForEditApproved(map,notificationMaster,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,notificationMaster.getReasonActInactMap());
            notificationMaster.setReasonActInactMap(reasonsActiveInactiveMapping);
            List<User> userList = getListofUsers();
            map.put("userList", userList);
            map.put("notificationMaster", notificationMaster);
            map.put("viewable" , false);
            map.put("masterID", masterId);
            map.put("activeFlag",notificationMaster.isActiveFlag());
            return "notificationMaster";
        }

        BaseLoggers.flowLogger.debug("Saving Notification Master details-->" + notificationMaster.getLogInfo());
        if(notificationMaster.getAttachmentEncryptionPolicy()!=null && notificationMaster.getAttachmentEncryptionPolicy().getId()==null){
            notificationMaster.setAttachmentEncryptionPolicy(null);
        }
        if(notificationMaster.getLetterType()!=null && notificationMaster.getLetterType().getId()==null){
            notificationMaster.setLetterType(null);
        }
        if (notificationMaster.getNotificationAdditionalInformation() != null) {
            if (notificationMaster.getNotificationAdditionalInformation().getEmail() == null) {
                List<EMailInfo> email = new ArrayList<EMailInfo>();
                notificationMaster.getNotificationAdditionalInformation().setEmail(email);

            }
            if (notificationMaster.getNotificationAdditionalInformation().getPhoneNumber() == null) {
                List<PhoneNumber> phoneNumList = new ArrayList<PhoneNumber>();
                notificationMaster.getNotificationAdditionalInformation().setPhoneNumber(phoneNumList);
            }
        }
        Document document=null;
        Document eBodyDocument;
        NotificationMasterService notificationMasterService=null;
        if (notificationMaster.getAttachedDocument() != null&&notificationMaster.getAttachedDocument().getId() == null) {
            notificationMaster.setAttachedDocument(null);
        }
        if (notificationMaster.getEmailBodyDocument()!=null && notificationMaster.getEmailBodyDocument().getId()==null){
            notificationMaster.setEmailBodyDocument(null);
        }
        if (notificationMaster.getWhatsAppBodyDocument()!=null && notificationMaster.getWhatsAppBodyDocument().getId()==null){
       	 	notificationMaster.setWhatsAppBodyDocument(null);
          
       }
        if (notificationMaster.isAttachment()&&!notificationMaster.isAttachmentOption()&&notificationMaster.getAttachedDocument() != null&&notificationMaster.getAttachedDocument().getId() != null ) {


            document=notificationMaster.getAttachedDocument();

        }

        User user = getUserDetails().getUserReference();
        if (user != null) {

            if (notificationMaster.isAttachment()&&!notificationMaster.isAttachmentOption()&&attachedFile != null && attachedFile.getSize() > 1) {
                try {

                    String attachmentCouchDbId = docService2.saveDocument(attachedFile.getInputStream(), FilenameUtils.removeExtension(attachedFile.getOriginalFilename()), FilenameUtils.getExtension(attachedFile.getOriginalFilename()));


                    document = new Document();
                    document.setDocumentStoreId(attachmentCouchDbId);
                    document.setContentType(attachedFile.getContentType());
                    document.setUploadedFileName(attachedFile.getFileItem().getName());
                    entityDao.persist(document);
                    notificationMaster.setAttachedDocument(document);

                } catch (Exception e) {
                    BaseLoggers.exceptionLogger.error(e.toString());
                    throw new ServiceInputException("Error while saving document",e);
                }
            }else if (document!=null)
            {
                notificationMaster.setAttachedDocument(document);
            }
            else {
                notificationMaster.setAttachedDocument(null);
            }
            if (notificationMaster.getEmailBodyType() != null
                    && notificationMaster.getEmailBodyType().equalsIgnoreCase(NotificationMaster.InlineTextBody)) {
                notificationMaster.setEmailBodyDocument(null);
            } else if (notificationMaster.getEmailBodyType() != null
                    && notificationMaster.getEmailBodyType().equalsIgnoreCase(NotificationMaster.UploadedEmailBody)) {
                notificationMaster.setTemplateText(null);
                if (eBodyFile != null && eBodyFile.getSize() > 1) {
                    try {

                        String eBodyCouchDbId = docService2.saveDocument(eBodyFile.getInputStream(),
                                FilenameUtils.removeExtension(eBodyFile.getOriginalFilename()),
                                FilenameUtils.getExtension(eBodyFile.getOriginalFilename()));
                        eBodyDocument = new Document();
                        eBodyDocument.setDocumentStoreId(eBodyCouchDbId);
                        eBodyDocument.setContentType(eBodyFile.getContentType());
                        eBodyDocument.setUploadedFileName(eBodyFile.getFileItem().getName());
                        entityDao.persist(eBodyDocument);
                        notificationMaster.setEmailBodyDocument(eBodyDocument);

                    } catch (Exception e) {
                        BaseLoggers.exceptionLogger.error(e.toString());
                        throw new ServiceInputException("Error while saving document", e);
                    }
                }
            }

            if (notificationMaster.getNotificationAdditionalInformation() != null) {

                if (notificationMaster.getNotificationAdditionalInformation().getEmail() != null) {
                    Iterator<EMailInfo> itr = notificationMaster.getNotificationAdditionalInformation().getEmail()
                            .iterator();

                    while (itr.hasNext()) {
                        EMailInfo eMailInfo = itr.next();
                        if (!StringUtils.hasText(eMailInfo.getEmailAddress())) {
                            itr.remove();
                        }
                    }
                }

                if (notificationMaster.getNotificationAdditionalInformation().getPhoneNumber() != null) {
                    Iterator<PhoneNumber> itr = notificationMaster.getNotificationAdditionalInformation()
                            .getPhoneNumber().iterator();

                    while (itr.hasNext()) {
                        PhoneNumber pInfo = itr.next();
                        if (!StringUtils.hasText(pInfo.getPhoneNumber())) {
                            itr.remove();
                        }
                    }
                }
            }
            if (notificationMaster.getNotificationAdditionalInformation() != null
                    && notificationMaster.getNotificationAdditionalInformation().getEmail().isEmpty()
                    && notificationMaster.getNotificationAdditionalInformation().getPhoneNumber().isEmpty()) {
                notificationMaster.setNotificationAdditionalInformation(null);
            }

        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = notificationMaster.getReasonActInactMap();
        if(reasonsActiveInactiveMapping != null){
            saveActInactReasonForMaster(reasonsActiveInactiveMapping,notificationMaster);
        }
        notificationMaster.setReasonActInactMap(reasonsActiveInactiveMapping);
        makerCheckerService.masterEntityChangedByUser(notificationMaster, user);

        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            NotificationMaster notificationMasterCreateAnother= new NotificationMaster();
            notificationMasterCreateAnother.setReasonActInactMap(reasActInactMap);

            map.put("notificationMaster",notificationMasterCreateAnother);

            map.put("masterID", masterId);
            List<User> userList = getListofUsers();
            map.put("userList", userList);
            map.put(ATTACHMENT_ENCRYTION_POLICY_LIST, getAttachmentEncryptionPolicyBySourceProduct());
            map.put(LETTER_TYPE_LIST, getLetterTypeListByParentCode());
            map.put("notificationMasterConfig", notificationMasterConfig);
            return "notificationMaster";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/NotificationMaster/NotificationMaster/loadColumnConfig";

    }

    @PreAuthorize("hasAuthority('MAKER_NOTIFICATIONMASTER')")
    @RequestMapping("/NotificationMaster/saveAndSendForApproval")
    public String saveAndSendForApprovalNotificationMaster(@ModelAttribute NotificationMaster notificationMaster,
                                                           BindingResult result,ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster,
                                                           @RequestParam("attachedFile") CommonsMultipartFile attachedFile,
                                                           @RequestParam("eBodyFile") CommonsMultipartFile eBodyFile,
                                                           @RequestParam("whatsAppBodyFile") CommonsMultipartFile whatsAppBodyFile) throws IOException {

        Map<String, Object> validateMap = new HashMap<String, Object>();

        NotificationMaster dubplicateNotificationMaster = null;
        if(null!=notificationMaster.getId()){
            dubplicateNotificationMaster = entityDao.find(NotificationMaster.class,notificationMaster.getId());
            if(null != dubplicateNotificationMaster.getEntityLifeCycleData()){
                notificationMaster.setEntityLifeCycleData(dubplicateNotificationMaster.getEntityLifeCycleData());
            }
            if(null != dubplicateNotificationMaster.getMasterLifeCycleData()){
                notificationMaster.setMasterLifeCycleData(dubplicateNotificationMaster.getMasterLifeCycleData());
            }
        }

        validateMap.put("notificationCode", notificationMaster.getNotificationCode());

        List<String> colNameList = checkValidationForDuplicates(notificationMaster, NotificationMaster.class,
                validateMap);

        if (result.hasErrors() || (colNameList != null && !colNameList.isEmpty())) {
            if(notificationMaster.getId() != null) {
                NotificationMaster nm = baseMasterService.getMasterEntityById(NotificationMaster.class, notificationMaster.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == nm.getApprovalStatus() || ApprovalStatus.CLONED == nm.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            String masterName = notificationMaster.getClass().getSimpleName();
            String uniqueValue = null;
            String uniqueParameter = null;
            if (null != notificationMaster.getId()) {
                //NotificationMaster notifMas = baseMasterService.findById(NotificationMaster.class, notificationMaster.getId());
                uniqueValue = dubplicateNotificationMaster.getNotificationCode();
                uniqueParameter = "notificationCode";
                getActInactReasMapForEditApproved(map, notificationMaster, masterName, uniqueParameter, uniqueValue);
            }
            else {
                ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
                notificationMaster.setReasonActInactMap(reasActInactMap);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,notificationMaster.getReasonActInactMap());
            notificationMaster.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("edit" , true);
            map.put("viewable" , false);
            map.put("notification", notificationMaster);
            map.put("masterID", masterId);
            List<User> userList = getListofUsers();
            map.put("userList", userList);
            map.put(ATTACHMENT_ENCRYTION_POLICY_LIST, getAttachmentEncryptionPolicyBySourceProduct());
            map.put(LETTER_TYPE_LIST, getLetterTypeListByParentCode());
            map.put("notificationMasterConfig", notificationMasterConfig);
            if (notificationMaster != null && colNameList != null && !colNameList.isEmpty()) {
                result.rejectValue("notificationCode", "label.notificationCode.validation.exists");
            }
            return "notificationMaster";
        }

        boolean eventResult = executeMasterEvent(notificationMaster,"contextObjectNotificationMaster",map);
        if(!eventResult){
            List<User> userList = getListofUsers();
            map.put("userList", userList);
            // getActInactReasMapForEdit(map,currency);
            String masterName = notificationMaster.getClass().getSimpleName();
            String uniqueParameter = "notificationCode";
            String uniqueValue = notificationMaster.getNotificationCode();
            getActInactReasMapForEditApproved(map,notificationMaster,masterName,uniqueParameter,uniqueValue);
            map.put("edit" , true);
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,notificationMaster.getReasonActInactMap());
            notificationMaster.setReasonActInactMap(reasonsActiveInactiveMapping);
            map.put("notificationMaster", notificationMaster);
            map.put("viewable" , false);
            map.put("masterID", masterId);
            map.put("activeFlag",notificationMaster.isActiveFlag());
            return "notificationMaster";
        }

        BaseLoggers.flowLogger.debug("Saving Notification Master details-->" + notificationMaster.getLogInfo());
        if(notificationMaster.getAttachmentEncryptionPolicy()!=null && notificationMaster.getAttachmentEncryptionPolicy().getId()==null){
            notificationMaster.setAttachmentEncryptionPolicy(null);
        }
        if(notificationMaster.getLetterType()!=null && notificationMaster.getLetterType().getId()==null){
            notificationMaster.setLetterType(null);
        }

        if (notificationMaster.getNotificationAdditionalInformation() != null) {
            if (notificationMaster.getNotificationAdditionalInformation().getEmail() == null) {
                List<EMailInfo> email = new ArrayList<EMailInfo>();
                notificationMaster.getNotificationAdditionalInformation().setEmail(email);

            }
            if (notificationMaster.getNotificationAdditionalInformation().getPhoneNumber() == null) {
                List<PhoneNumber> phoneNumList = new ArrayList<PhoneNumber>();
                notificationMaster.getNotificationAdditionalInformation().setPhoneNumber(phoneNumList);
            }
        }
        Document document=null;
        Document eBodyDocument;
        if (notificationMaster.getAttachedDocument() != null&&notificationMaster.getAttachedDocument().getId() == null) {
            notificationMaster.setAttachedDocument(null);
        }
        if (notificationMaster.getEmailBodyDocument()!=null && notificationMaster.getEmailBodyDocument().getId()==null){
            notificationMaster.setEmailBodyDocument(null);
           
        }
        if (notificationMaster.getWhatsAppBodyDocument()!=null && notificationMaster.getWhatsAppBodyDocument().getId()==null){
        	 notificationMaster.setWhatsAppBodyDocument(null);
           
        }
       
        if (notificationMaster.isAttachment()&&!notificationMaster.isAttachmentOption()&&notificationMaster.getAttachedDocument() != null&&notificationMaster.getAttachedDocument().getId() != null ) {


            document=notificationMaster.getAttachedDocument();

        }

        User user = getUserDetails().getUserReference();
        if (user != null) {
            if (notificationMaster.isAttachment()&&!notificationMaster.isAttachmentOption()&&attachedFile != null && attachedFile.getSize() > 1) {
                try {


                    String attachmentCouchDbId = docService2.saveDocument(attachedFile.getInputStream(), FilenameUtils.removeExtension(attachedFile.getOriginalFilename()), FilenameUtils.getExtension(attachedFile.getOriginalFilename()));
                    document = new Document();
                    document.setDocumentStoreId(attachmentCouchDbId);
                    document.setContentType(attachedFile.getContentType());
                    document.setUploadedFileName(attachedFile.getFileItem().getName());
                    entityDao.persist(document);
                    notificationMaster.setAttachedDocument(document);

                }
                catch (Exception e) {
                    BaseLoggers.exceptionLogger.error(e.toString());
                    throw new ServiceInputException("Error while saving document",e);
                }
            }
            else if (document!=null)
            {
                notificationMaster.setAttachedDocument(document);
            }
            else {
                notificationMaster.setAttachedDocument(null);
            }
            if (notificationMaster.getEmailBodyType() != null
                    && notificationMaster.getEmailBodyType().equalsIgnoreCase(NotificationMaster.InlineTextBody)) {
                notificationMaster.setEmailBodyDocument(null);
            } else if (notificationMaster.getEmailBodyType() != null
                    && notificationMaster.getEmailBodyType().equalsIgnoreCase(NotificationMaster.UploadedEmailBody)) {
                notificationMaster.setTemplateText(null);
                if (eBodyFile != null && eBodyFile.getSize()>1) {
                    try {

                        String eBodyCouchDbId =  docService2.saveDocument(eBodyFile.getInputStream(), FilenameUtils.removeExtension(eBodyFile.getOriginalFilename()), FilenameUtils.getExtension(eBodyFile.getOriginalFilename()));
                        eBodyDocument = new Document();
                        eBodyDocument.setDocumentStoreId(eBodyCouchDbId);
                        eBodyDocument.setContentType(eBodyFile.getContentType());
                        eBodyDocument.setUploadedFileName(eBodyFile.getFileItem(). getName());
                        entityDao.persist(eBodyDocument);
                        notificationMaster.setEmailBodyDocument(eBodyDocument);

                    } catch (Exception e) {
                        BaseLoggers.exceptionLogger.error(e.toString());
                        throw new ServiceInputException("Error while saving document",e);
                    }
                }

            }
            
                
                if (whatsAppBodyFile != null && whatsAppBodyFile.getSize()>1) {
                    try {
                    	notificationMaster.setTemplateText(null);
                        String whatsAppBodyCouchDbId =  docService2.saveDocument(whatsAppBodyFile.getInputStream(), FilenameUtils.removeExtension(whatsAppBodyFile.getOriginalFilename()), FilenameUtils.getExtension(whatsAppBodyFile.getOriginalFilename()));
                        Document whatsAppBodyDocument = new Document();
                        whatsAppBodyDocument.setDocumentStoreId(whatsAppBodyCouchDbId);
                        whatsAppBodyDocument.setContentType(whatsAppBodyFile.getContentType());
                        whatsAppBodyDocument.setUploadedFileName(whatsAppBodyFile.getFileItem(). getName());
                        entityDao.persist(whatsAppBodyDocument);
                        notificationMaster.setWhatsAppBodyDocument(whatsAppBodyDocument);

                    } catch (Exception e) {
                        BaseLoggers.exceptionLogger.error(e.toString());
                        throw new ServiceInputException("Error while saving document",e);
                    }
                }



            if (notificationMaster.getNotificationAdditionalInformation() != null) {

                if (notificationMaster.getNotificationAdditionalInformation().getEmail() != null) {
                    Iterator<EMailInfo> itr = notificationMaster.getNotificationAdditionalInformation().getEmail()
                            .iterator();

                    while (itr.hasNext()) {
                        EMailInfo eMailInfo = itr.next();
                        if (!StringUtils.hasText(eMailInfo.getEmailAddress())) {
                            itr.remove();
                        }
                    }
                }

                if (notificationMaster.getNotificationAdditionalInformation().getPhoneNumber() != null) {
                    Iterator<PhoneNumber> itr = notificationMaster.getNotificationAdditionalInformation().getPhoneNumber()
                            .iterator();

                    while (itr.hasNext()) {
                        PhoneNumber pInfo = itr.next();
                        if (!StringUtils.hasText(pInfo.getPhoneNumber())) {
                            itr.remove();
                        }
                    }
                }
            }

            if (notificationMaster.getNotificationAdditionalInformation() != null && notificationMaster.getNotificationAdditionalInformation().getEmail().isEmpty() && notificationMaster.getNotificationAdditionalInformation().getPhoneNumber().isEmpty()) {
                notificationMaster.setNotificationAdditionalInformation(null);
            }
            ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = notificationMaster.getReasonActInactMap();
            if(reasonsActiveInactiveMapping != null){
                saveActInactReasonForMaster(reasonsActiveInactiveMapping,notificationMaster);
            }
            notificationMaster.setReasonActInactMap(reasonsActiveInactiveMapping);
            makerCheckerService.saveAndSendForApproval(notificationMaster, user);

        }

        if (createAnotherMaster) {
            ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
            NotificationMaster notificationMasterCreateAnother= new NotificationMaster();
            notificationMasterCreateAnother.setReasonActInactMap(reasActInactMap);
            map.put("notificationMaster",notificationMasterCreateAnother);
            map.put("masterID", masterId);
            List<User> userList = getListofUsers();
            map.put("userList", userList);
            map.put(ATTACHMENT_ENCRYTION_POLICY_LIST, getAttachmentEncryptionPolicyBySourceProduct());
            map.put(LETTER_TYPE_LIST, getLetterTypeListByParentCode());
            map.put("notificationMasterConfig", notificationMasterConfig);
            return "notificationMaster";
        }
        map.put("masterID", masterId);
        return "redirect:/app/grid/NotificationMaster/NotificationMaster/loadColumnConfig";

    }

    @RequestMapping("/ajax/emails/{count}")
    public String ajaxMails(ModelMap map, @PathVariable("count") Integer count) {
        map.put("count", count);
        map.put("notificationMaster", new NotificationMaster());
        return "/notificationMaster/addInfo";

    }

    @RequestMapping("/ajax/sms/{count}")
    public String ajaxSMS(ModelMap map, @PathVariable("count") Integer count) {
        map.put("count", count);
        map.put("notificationMaster", new NotificationMaster());
        return "/notificationMaster/addInfoSMS";

    }
    
    @RequestMapping("/ajax/whatsApp/{count}")
    public String ajaxWhatsApp(ModelMap map, @PathVariable("count") Integer count) {
        map.put("count", count);
        map.put("notificationMaster", new NotificationMaster());
        return "/notificationMaster/addInfoWhatsApp";

    }

    @PreAuthorize("hasAuthority('VIEW_NOTIFICATIONMASTER') or hasAuthority('MAKER_NOTIFICATIONMASTER') or hasAuthority('CHECKER_NOTIFICATIONMASTER')")
    @RequestMapping("NotificationMaster/view/{id}")
    public String viewNotificationMaster(@PathVariable("id") long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        NotificationMaster notificationMaster = baseMasterService.getMasterEntityWithActionsById(NotificationMaster.class, id, currentUser.getUserEntityId().getUri());

        if (notificationMaster != null && notificationMaster.getNotificationAdditionalInformation() != null) {
            Hibernate.initialize(notificationMaster.getNotificationAdditionalInformation().getPhoneNumber());
            Hibernate.initialize(notificationMaster.getNotificationAdditionalInformation().getEmail());
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,notificationMaster.getReasonActInactMap());
        notificationMaster.setReasonActInactMap(reasonsActiveInactiveMapping);
        // getActInactReasMapForEdit(map,currency);
        String masterName = notificationMaster.getClass().getSimpleName();
        String uniqueParameter = "notificationCode";
        String uniqueValue = notificationMaster.getNotificationCode();
        getActInactReasMapForEditApproved(map,notificationMaster,masterName,uniqueParameter,uniqueValue);
        map.put("notificationMaster", notificationMaster);
        map.put("masterID", masterId);
        List<User> userList = getListofUsers();
        map.put("userList", userList);
        map.put(ATTACHMENT_ENCRYTION_POLICY_LIST, getAttachmentEncryptionPolicyBySourceProduct());
        map.put(LETTER_TYPE_LIST, getLetterTypeListByParentCode());
        map.put("notificationMasterConfig", notificationMasterConfig);
        map.put("viewable", true);
        map.put("codeViewMode", true);
        getMasterActions(notificationMaster, map);

        return "notificationMaster";
    }

    @SuppressWarnings("unchecked")
    private void getMasterActions(NotificationMaster notificationMaster, ModelMap map) {
        if (notificationMaster != null && notificationMaster.getViewProperties() != null) {
            List<String> actions = (List<String>) notificationMaster.getViewProperties().get("actions");
            if (actions != null) {
                for (String act : actions) {
                    String actionString = "act" + act;
                    map.put(actionString.replaceAll(" ", ""), false);
                }
            }
        }
    }

    @PreAuthorize("hasAuthority('MAKER_NOTIFICATIONMASTER')")
    @RequestMapping("NotificationMaster/edit/{id}")
    public String editNotificationMaster(@PathVariable("id") long id, ModelMap map) {
        UserInfo currentUser = getUserDetails();
        NotificationMaster notificationMaster = baseMasterService.getMasterEntityWithActionsById(NotificationMaster.class, id, currentUser.getUserEntityId().getUri());

        if (notificationMaster != null && notificationMaster.getNotificationAdditionalInformation() != null) {
            Hibernate.initialize(notificationMaster.getNotificationAdditionalInformation().getPhoneNumber());
            Hibernate.initialize(notificationMaster.getNotificationAdditionalInformation().getEmail());
        }
        if(notificationMaster != null && !(ApprovalStatus.UNAPPROVED_ADDED == notificationMaster.getApprovalStatus() || ApprovalStatus.CLONED == notificationMaster.getApprovalStatus())) {
            map.put("codeViewMode", true);
        }
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,notificationMaster.getReasonActInactMap());
        notificationMaster.setReasonActInactMap(reasonsActiveInactiveMapping);        // getActInactReasMapForEdit(map,currency);
        String masterName = notificationMaster.getClass().getSimpleName();
        String uniqueParameter = "notificationCode";
        String uniqueValue = notificationMaster.getNotificationCode();
        getActInactReasMapForEditApproved(map,notificationMaster,masterName,uniqueParameter,uniqueValue);
        map.put("viewable" ,false);
        map.put("notificationMaster", notificationMaster);
        map.put("masterID", masterId);
        map.put("notificationMasterConfig", notificationMasterConfig);
        List<User> userList = getListofUsers();
        map.put("userList", userList);
        map.put(ATTACHMENT_ENCRYTION_POLICY_LIST, getAttachmentEncryptionPolicyBySourceProduct());
        map.put(LETTER_TYPE_LIST, getLetterTypeListByParentCode());
        map.put("edit", true);
        map.put("viewable", false);
        getMasterActions(notificationMaster, map);

        return "notificationMaster";
    }



    public List<User> getListofUsers() {

        List<User> ul;
        ul = userService.getAllUser();

        return ul;
    }

    public List<Map<String, ?>> getAttachedDocumnetDescription(String documnetStoreId) {
        MapQueryExecutor mapExecutor = new MapQueryExecutor(Document.class).addQueryColumns("contentType",
                "uploadedFileName").addAndClause("documentStoreId = " + "'" + documnetStoreId + "'");
        return entityDao.executeQuery(mapExecutor);
    }

    public List<AttachmentEncryptionPolicy> getAttachmentEncryptionPolicyBySourceProduct() {
        if(notNull(ProductInformationLoader.getProductCode())){
            SourceProduct sourceProduct=genericParameterService.findByCode(ProductInformationLoader.getProductCode(), SourceProduct.class);
            if(notNull(sourceProduct)){
                return notificationMasterService.findAttachmentEncryptionPolicyBySourceProduct(sourceProduct);
            }
        }
        return new ArrayList<AttachmentEncryptionPolicy>();
    }

    public List<LetterType> getLetterTypeListByParentCode() {

        List<LetterType> letterTypeList=genericParameterService.findChildrenByParentCode(LETTER_TYPE_PARENT_CODE, LetterType.class);
        if(questionairePdfHandler!=null){
            questionairePdfHandler.addOptionalLetterTypesToList(letterTypeList);
        }
        return letterTypeList;
    }


} 
