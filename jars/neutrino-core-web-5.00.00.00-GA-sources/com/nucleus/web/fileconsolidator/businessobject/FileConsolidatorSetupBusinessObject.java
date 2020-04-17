package com.nucleus.web.fileconsolidator.businessobject;

import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.*;
import com.nucleus.finnone.pro.fileconsolidator.util.FileUploadDownloadConstants;
import com.nucleus.finnone.pro.fileconsolidator.util.FileUploadDownloadUtil;
import com.nucleus.persistence.EntityDao;
import com.nucleus.web.fileconsolidator.util.CompositeFormatDetailVO;
import com.nucleus.web.fileconsolidator.util.UserFormatDetailChangesVO;
import com.nucleus.web.fileconsolidator.util.UserFormatHeaderChangesVO;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("fileConsolidatorSetupBusinessObject")
public class FileConsolidatorSetupBusinessObject {

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    @Inject
    @Named("messageSource")
    protected MessageSource messageSource;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileUploadDownloadUserFormat fetchAndMergeChangesFromVO(UserFormatHeaderChangesVO formatHeaderChanges) {

        Long userFormatId = Long.valueOf(formatHeaderChanges.getUserFormatId());
        FileUploadDownloadUserFormat originalUserFormat = entityDao.getEntityManager().find(FileUploadDownloadUserFormat.class,Long.valueOf(userFormatId));
        Hibernate.initialize(originalUserFormat.getUserFormatDetail());
        /*for(FileUploadDownloadUserFormatDetail userFormatDetail : originalUserFormat.getUserFormatDetail()){
            Hibernate.initialize(userFormatDetail.getPermissibleValuesList());
            userFormatDetail.getPermissibleValuesList().size();
            for (PermissibleValuesMappingDetail permissibleValuesMappingDetail : userFormatDetail.getPermissibleValuesList()) {
                permissibleValuesMappingDetail.getId();
            }
        }*/
        for(FileUploadDownloadUserFormatDetail userFormatDetail : originalUserFormat.getUserFormatDetail()){
            Hibernate.initialize(userFormatDetail.getPermissibleValuesList());
        }
        Hibernate.initialize(originalUserFormat.getValidatorDetail());
        Hibernate.initialize(originalUserFormat.getUserRecordIdentifierDetail());
        Hibernate.initialize(originalUserFormat.getProcessingModeTypes());
        entityDao.getEntityManager().detach(originalUserFormat);
        //FileUploadDownloadUserFormat changedUserFormat = (FileUploadDownloadUserFormat)  originalUserFormat.cloneYourself(CloneOptionConstants.COPY_WITH_ID_AND_UUID_AND_STATUS);

        FileUploadDownloadUserFormat changedUserFormat = mergeUserFormatChanges(originalUserFormat,formatHeaderChanges);

        return changedUserFormat;

    }

    //TODO Add validations somehow - or move to a ValidatorUtil class
    //@Transactional(propagation = Propagation.NOT_SUPPORTED)
    public FileUploadDownloadUserFormat mergeUserFormatChanges(FileUploadDownloadUserFormat userFormatToModify, UserFormatHeaderChangesVO userFormatHeaderChangesVO){
        userFormatToModify.setActiveFlag(userFormatHeaderChangesVO.getActiveFlag());
        List<FileUploadDownloadUserFormatDetail> changedUserFormatDetailsList = new ArrayList<>();

        Map<String,UserFormatDetailChangesVO> modifiedAndDeletedUserFormatDetailsMap = getModifiedAndDeletedUserFormatDetailsMap(userFormatHeaderChangesVO.getUserFormatDetailChanges());
        List<UserFormatDetailChangesVO> addedUserFormatDetailVOList = getAddedUserFormatDetailsList(userFormatHeaderChangesVO.getUserFormatDetailChanges());

        //Merge modifications and make deletions
        for(FileUploadDownloadUserFormatDetail originalUserFormatDetail:userFormatToModify.getUserFormatDetail()){

            UserFormatDetailChangesVO userFormatDetailChangesVO = modifiedAndDeletedUserFormatDetailsMap.get(originalUserFormatDetail.getId().toString());

            if(userFormatDetailChangesVO != null){
                if(UserFormatDetailChangesVO.MODIFICATION_OPERATION.equalsIgnoreCase(userFormatDetailChangesVO.getOperationType())){
                    FileUploadDownloadUserFormatDetail modifiedUserFormatDetail = mergeUserFormatDetailChanges(originalUserFormatDetail,userFormatDetailChangesVO);
                    changedUserFormatDetailsList.add(modifiedUserFormatDetail);
                }else if(UserFormatDetailChangesVO.DELETION_OPERATION.equalsIgnoreCase(userFormatDetailChangesVO.getOperationType())){
                    //Do nothing
                }else{
                    changedUserFormatDetailsList.add(originalUserFormatDetail);
                }
            }else{
                changedUserFormatDetailsList.add(originalUserFormatDetail);
            }
        }

        //Make Additions
        changedUserFormatDetailsList.addAll(createAddedUserFormatDetails(addedUserFormatDetailVOList,userFormatToModify.getId()));

        FileUploadDownloadUtil.sortUserFormatDetailByFieldOrder(changedUserFormatDetailsList);
        userFormatToModify.setUserFormatDetail(changedUserFormatDetailsList);

        return userFormatToModify;
    }

    //TODO Add validations somehow - or move to a ValidatorUtil class
    public FileUploadDownloadUserFormatDetail mergeUserFormatDetailChanges(FileUploadDownloadUserFormatDetail userFormatDetailToModify, UserFormatDetailChangesVO userFormatDetailChangesVO){
        if((StringUtils.isNotBlank(userFormatDetailChangesVO.getUserFormatDetailId()) || userFormatDetailToModify.getId() != null )
                && !userFormatDetailChangesVO.getUserFormatDetailId().equals(String.valueOf(userFormatDetailToModify.getId()))){
            //Throw an exception!
        }else{
            userFormatDetailToModify.setFieldOrder(Long.valueOf(userFormatDetailChangesVO.getFieldOrder()));

            if(StringUtils.isNotBlank(userFormatDetailChangesVO.getOptionalMandatoryFlag())){
                userFormatDetailToModify.setOptionalMandatoryFlag(Character.valueOf(userFormatDetailChangesVO.getOptionalMandatoryFlag().charAt(0)));
            }

            if(StringUtils.isNotBlank(userFormatDetailChangesVO.getTrackingFieldFlag())){
                userFormatDetailToModify.setTrackingFieldFlag(Character.valueOf(userFormatDetailChangesVO.getTrackingFieldFlag().charAt(0)));
            }else{
                userFormatDetailToModify.setTrackingFieldFlag(FileUploadDownloadConstants.TRACKING_FLAG_NO);
            }

            if(StringUtils.isBlank(userFormatDetailChangesVO.getTrackingFieldOrder())){
                userFormatDetailToModify.setTrackingFieldOrder(null);
            }else{
                userFormatDetailToModify.setTrackingFieldOrder(Long.valueOf(userFormatDetailChangesVO.getTrackingFieldOrder()));
            }

            if(StringUtils.isNotBlank(userFormatDetailChangesVO.getOperationType())){
                userFormatDetailToModify.setOperationType(userFormatDetailChangesVO.getOperationType());
            }
        }

        return userFormatDetailToModify;
    }

    //TODO Do Validations??
    private Map<String,UserFormatDetailChangesVO> getModifiedAndDeletedUserFormatDetailsMap (List<UserFormatDetailChangesVO> userFormatDetailChanges){
        Map<String,UserFormatDetailChangesVO> modifiedAndDeletedUserFormatDetailsMap = new HashMap<>();

        for(UserFormatDetailChangesVO userFormatDetail : userFormatDetailChanges){
            if(UserFormatDetailChangesVO.MODIFICATION_OPERATION.equalsIgnoreCase(userFormatDetail.getOperationType())
                    || UserFormatDetailChangesVO.DELETION_OPERATION.equalsIgnoreCase(userFormatDetail.getOperationType())){
                modifiedAndDeletedUserFormatDetailsMap.put(userFormatDetail.getUserFormatDetailId(),userFormatDetail);
            }
        }

        return modifiedAndDeletedUserFormatDetailsMap;
    }

    private List<UserFormatDetailChangesVO> getAddedUserFormatDetailsList (List<UserFormatDetailChangesVO> userFormatDetailChanges){
        List<UserFormatDetailChangesVO> addedUserFormatDetailsList = new ArrayList<>();

        for(UserFormatDetailChangesVO userFormatDetail : userFormatDetailChanges){
            if(StringUtils.isBlank(userFormatDetail.getUserFormatDetailId())){
                addedUserFormatDetailsList.add(userFormatDetail);
            }
        }

        return addedUserFormatDetailsList;
    }

    private List<FileUploadDownloadUserFormatDetail> createAddedUserFormatDetails(List<UserFormatDetailChangesVO> addedUserFormatDetailVOList,Long userFormatId){
        List<FileUploadDownloadUserFormatDetail> addedUserFormatDetailsList = new ArrayList<>();

        for(UserFormatDetailChangesVO addedUserFormatDetailVO : addedUserFormatDetailVOList){
            if(StringUtils.isBlank(addedUserFormatDetailVO.getUserFormatDetailId())){
                FileUploadDownloadUserFormatDetail addedUserFormatDetail = new FileUploadDownloadUserFormatDetail();

                addedUserFormatDetail.setFormatId(userFormatId);

                FileUploadDownloadBaseFormatDetail referencedBaseFormatDetail = null;

                if(StringUtils.isNotBlank(addedUserFormatDetailVO.getBaseFormatDetailId())){
                    addedUserFormatDetail.setBaseFormatDetailId(Long.valueOf(addedUserFormatDetailVO.getBaseFormatDetailId()));
                    referencedBaseFormatDetail = entityDao.find(FileUploadDownloadBaseFormatDetail.class,addedUserFormatDetail.getBaseFormatDetailId());
                }

                if(StringUtils.isNotBlank(addedUserFormatDetailVO.getPaddingStrategyId())){
                    addedUserFormatDetail.setPaddingStrategyId(Long.valueOf(addedUserFormatDetailVO.getPaddingStrategyId()));
                }else if (referencedBaseFormatDetail != null){
                    addedUserFormatDetail.setPaddingStrategyId(referencedBaseFormatDetail.getPaddingStrategyId());
                }

                if(StringUtils.isNotBlank(addedUserFormatDetailVO.getRecordIdentifierId())){
                    addedUserFormatDetail.setRecordIdentifierId(Long.valueOf(addedUserFormatDetailVO.getRecordIdentifierId()));
                }else{
                    //TODO Derive userRecordIdentifier id
                }

                if(referencedBaseFormatDetail != null){
                    setBaseValuesInUserFormatDetail(addedUserFormatDetail,referencedBaseFormatDetail);
                }

                mergeUserFormatDetailChanges(addedUserFormatDetail,addedUserFormatDetailVO);

                addedUserFormatDetailsList.add(addedUserFormatDetail);
            }
        }

        return addedUserFormatDetailsList;
    }

    private FileUploadDownloadUserFormatDetail setBaseValuesInUserFormatDetail(FileUploadDownloadUserFormatDetail userFormatDetail, FileUploadDownloadBaseFormatDetail baseFormatDetail){

        userFormatDetail.setFormatMask(baseFormatDetail.getFormatMask());
        userFormatDetail.setOptionalMandatoryFlag(baseFormatDetail.getMandatoryFlag()); //Overriden by UI later on
        userFormatDetail.setLength(baseFormatDetail.getLength());
        //fieldOrder set by UI
        userFormatDetail.setStatus(baseFormatDetail.getStatus());
        userFormatDetail.setDefaultValue(baseFormatDetail.getDefaultValue());
        //trackingFieldFlag set by UI
        //trackingFieldOrder set by UI
        userFormatDetail.setLeftOfDecimalLength(baseFormatDetail.getLeftOfDecimalLength());
        userFormatDetail.setRightOfDecimalLength(baseFormatDetail.getRightOfDecimalLength());

        return userFormatDetail;
    }

    //TODO Add validations somehow - or move to a ValidatorUtil class
    public List<Object> fetchLocalizedMessages(List<Object> nestedGridData, Locale locale){
        for(Object gridRow : nestedGridData){
            if(gridRow instanceof CompositeFormatDetailVO){
                CompositeFormatDetailVO formatDetailGridRow = (CompositeFormatDetailVO)gridRow;
                if(StringUtils.isNotBlank(formatDetailGridRow.getFieldI18Key())){
                    String localizedMessage = messageSource.getMessage(formatDetailGridRow.getFieldI18Key(),null,locale);
                    formatDetailGridRow.setFieldDescription(localizedMessage);
                }
            }
        }

        return nestedGridData;
    }

}
