package com.nucleus.web.fileconsolidator.util;

import static com.nucleus.web.fileconsolidator.util.FileConsolidatorGridConstants.*;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.*;
import com.nucleus.finnone.pro.fileconsolidator.util.FileUploadDownloadUtil;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.persistence.EntityDao;

import java.util.*;

public class FileConsolidatorGridUtility {

    public static Boolean isRowReorderApplicable(String tableConfigKey, String viewable){
        if(viewable.equals(Boolean.FALSE.toString()) && NESTED_UFD_COLCONFIG_KEY.equals(tableConfigKey)){
                return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }


    public static List<Object> generateNestedGridData(
            String tableConfigKey, FileUploadDownloadUserFormat userFormat, FileUploadDownloadBaseFormat baseFormat,
             FileUploadDownloadBaseRecordIdentifierDetail baseRecordIdentifier, FileUploadDownloadUserRecordIdentifierDetail userRecordIdentifier){

        List<Object> gridRows = new ArrayList<>();
        if(ADD_UFD_COLCONFIG_KEY.equals(tableConfigKey)){
            gridRows = generateNestedGridDataForAddingUserFormatDetail(baseFormat,userFormat,baseRecordIdentifier,userRecordIdentifier);
        } else if(userFormat != null){
            gridRows = generateNestedGridDataForUserFormat(tableConfigKey,userFormat,baseRecordIdentifier,userRecordIdentifier);
        } else if (baseFormat!=null){
            gridRows = generateNestedGridDataForBaseFormat(tableConfigKey,baseFormat,baseRecordIdentifier);
        }

        return gridRows;
    }

    private static List<Object> generateNestedGridDataForAddingUserFormatDetail(FileUploadDownloadBaseFormat baseFormat, FileUploadDownloadUserFormat userFormat, FileUploadDownloadBaseRecordIdentifierDetail baseRecordIdentifier, FileUploadDownloadUserRecordIdentifierDetail userRecordIdentifier) {
        List<Object> gridRows = new ArrayList<>();

        Map<FileUploadDownloadBaseRecordIdentifierDetail, List<FileUploadDownloadBaseFormatDetail>> baseFormatDetailMapByBaseRecordIdentifier = prepareBaseFormatDetailMapByBaseRecordIdentifier(baseFormat);
        Map<FileUploadDownloadBaseRecordIdentifierDetail, List<FileUploadDownloadUserFormatDetail>> userFormatDetailMapByBaseRecordIdentifier = prepareUserFormatDetailMapByBaseRecordIdentifier(userFormat);

        List<FileUploadDownloadUserFormatDetail> existingUserFormatDetails = userFormatDetailMapByBaseRecordIdentifier.get(baseRecordIdentifier);

        //Base Format Details which are already mapped with a userFormatDetail
        Map<Long,FileUploadDownloadUserFormatDetail> mappedBaseFormatDetailIds = new HashMap<>();
        for(FileUploadDownloadUserFormatDetail existingUserFormatDetail : existingUserFormatDetails){
            if(existingUserFormatDetail.getBaseFormatDetail() != null){
                mappedBaseFormatDetailIds.put(existingUserFormatDetail.getBaseFormatDetailId(),existingUserFormatDetail);
            }
        }

        List<FileUploadDownloadBaseFormatDetail> baseFormatDetails = baseFormatDetailMapByBaseRecordIdentifier.get(baseRecordIdentifier);
        List<FileUploadDownloadBaseFormatDetail> sortedBaseFormatDetails = FileUploadDownloadUtil.sortBaseFormatDetailByFieldOrder(baseFormatDetails);

        gridRows.add(createGridRowForEmptyBaseFormatDetail(userFormat,userRecordIdentifier));

        for(FileUploadDownloadBaseFormatDetail baseFormatDetail : sortedBaseFormatDetails){
            Boolean isThisBaseFormatDetailAlreadyMappedWithUserFormatDetail = Boolean.FALSE;
            if(mappedBaseFormatDetailIds.containsKey(baseFormatDetail.getId())){
                isThisBaseFormatDetailAlreadyMappedWithUserFormatDetail = Boolean.TRUE;
            }

            CompositeNewFormatDetailVO gridRow = new CompositeNewFormatDetailVO(baseFormatDetail,userFormat,userRecordIdentifier,isThisBaseFormatDetailAlreadyMappedWithUserFormatDetail);
            gridRow.updateOptionalMandatorySelectedOption();
            gridRows.add(gridRow);
        }

        return gridRows;
    }

    private static Object createGridRowForEmptyBaseFormatDetail(FileUploadDownloadUserFormat userFormat, FileUploadDownloadUserRecordIdentifierDetail userRecordIdentifier) {
        CompositeNewFormatDetailVO gridRowForEmptyBaseFormatDetail = new CompositeNewFormatDetailVO(userFormat,userRecordIdentifier);
        gridRowForEmptyBaseFormatDetail.setFieldName(UFD_IGNORE_FIELD_MSG);
        gridRowForEmptyBaseFormatDetail.updateOptionalMandatorySelectedOption();
        return gridRowForEmptyBaseFormatDetail;
    }

    public static List<Object> generateNestedGridDataForBaseFormat(String tableConfigKey, FileUploadDownloadBaseFormat baseFormat,FileUploadDownloadBaseRecordIdentifierDetail baseRecordIdentifier){
        Map<FileUploadDownloadBaseRecordIdentifierDetail, List<FileUploadDownloadBaseFormatDetail>> baseFormatDetailMapByBaseRecordIdentifier = prepareBaseFormatDetailMapByBaseRecordIdentifier(baseFormat);

        List<Object> gridRows = new ArrayList<>();

        // Case where we have root table
        if(NESTED_RID_COLCONFIG_KEY.equals(tableConfigKey) && baseRecordIdentifier == null){
            List<FileUploadDownloadBaseRecordIdentifierDetail> sortedRootIdentifiers = getSortedRootIdentifiers(baseFormatDetailMapByBaseRecordIdentifier.keySet());

            for(FileUploadDownloadBaseRecordIdentifierDetail rootIdentifier : sortedRootIdentifiers){
                CompositeRecordIdentifierVO gridRow = new CompositeRecordIdentifierVO(baseFormat,rootIdentifier);

                if(ValidatorUtils.hasNoElements(rootIdentifier.getChildrenRecordIdentifier())){
                    gridRow.setTableConfigKey(NESTED_UFD_COLCONFIG_KEY);
                }else{
                    gridRow.setTableConfigKey(NESTED_RID_COLCONFIG_KEY);
                }

                gridRows.add(gridRow);
            }
        }

        // Case where we have non-leaf RID Table
        else if (NESTED_RID_COLCONFIG_KEY.equals(tableConfigKey)){
            List<FileUploadDownloadBaseRecordIdentifierDetail> sortedBaseIdentifiers = FileUploadDownloadUtil.sortBaseRecordIdentifierByType(baseRecordIdentifier.getChildrenRecordIdentifier());

            CompositeRecordIdentifierVO viewColumnsRow = new CompositeRecordIdentifierVO(baseFormat,baseRecordIdentifier);

            viewColumnsRow = CompositeRecordIdentifierVO.changeVOForViewColumns(viewColumnsRow);
            viewColumnsRow.setTableConfigKey(NESTED_UFD_COLCONFIG_KEY);

            gridRows.add(viewColumnsRow);

            for(FileUploadDownloadBaseRecordIdentifierDetail baseIdentifier : sortedBaseIdentifiers){
                CompositeRecordIdentifierVO gridRow = new CompositeRecordIdentifierVO(baseFormat,baseIdentifier);

                if(ValidatorUtils.hasNoElements(baseIdentifier.getChildrenRecordIdentifier())){
                    gridRow.setTableConfigKey(NESTED_UFD_COLCONFIG_KEY);
                }else{
                    gridRow.setTableConfigKey(NESTED_RID_COLCONFIG_KEY);
                }

                gridRows.add(gridRow);
            }
        }

        // Case where we have leaf BFD Table (same as UFD)
        else if (NESTED_UFD_COLCONFIG_KEY.equals(tableConfigKey) && baseRecordIdentifier != null){
            List<FileUploadDownloadBaseFormatDetail> baseFormatDetails = baseFormatDetailMapByBaseRecordIdentifier.get(baseRecordIdentifier);
            List<FileUploadDownloadBaseFormatDetail> sortedBaseFormatDetails = FileUploadDownloadUtil.sortBaseFormatDetailByFieldOrder(baseFormatDetails);

            for(FileUploadDownloadBaseFormatDetail baseFormatDetail : sortedBaseFormatDetails){
                CompositeFormatDetailVO gridRow = new CompositeFormatDetailVO(baseFormatDetail);
                gridRow.updateOptionalMandatorySelectedOption();
                gridRows.add(gridRow);
            }
        }

        return gridRows;
    }

    private static Map<FileUploadDownloadBaseRecordIdentifierDetail, List<FileUploadDownloadBaseFormatDetail>> prepareBaseFormatDetailMapByBaseRecordIdentifier(FileUploadDownloadBaseFormat baseFormat){

        Map<Long, List<FileUploadDownloadBaseFormatDetail>> baseFormatDetailMapByBaseRecordIdentifierId = new HashMap();

        Map<Long, FileUploadDownloadBaseRecordIdentifierDetail> baseRecordIdentifierMapById = new HashMap();

        List<FileUploadDownloadBaseFormatDetail> sortedUserFormatDetail = FileUploadDownloadUtil.sortBaseFormatDetailByFieldOrder(baseFormat.getSystemFormatDetail());

        for(FileUploadDownloadBaseFormatDetail baseFormatDetail : sortedUserFormatDetail){
            Long baseRecordIdentifierId = baseFormatDetail.getSystemRecordIdentifier().getId();

            List<FileUploadDownloadBaseFormatDetail> mappedBaseFormatDetailList = baseFormatDetailMapByBaseRecordIdentifierId.get(baseRecordIdentifierId);
            if(mappedBaseFormatDetailList == null){
                mappedBaseFormatDetailList = new ArrayList<>();
                baseFormatDetailMapByBaseRecordIdentifierId.put(baseRecordIdentifierId,mappedBaseFormatDetailList);
            }

            if(baseRecordIdentifierMapById.get(baseRecordIdentifierId) == null){
                baseRecordIdentifierMapById.put(baseRecordIdentifierId,baseFormatDetail.getSystemRecordIdentifier());
            }

            mappedBaseFormatDetailList.add(baseFormatDetail); // Already sorted by fieldOrder
        }

        Map<FileUploadDownloadBaseRecordIdentifierDetail, List<FileUploadDownloadBaseFormatDetail>> baseFormatDetailMapByBaseRecordIdentifier = new HashMap();

        for(Map.Entry<Long, FileUploadDownloadBaseRecordIdentifierDetail> baseRecordIdentifier : baseRecordIdentifierMapById.entrySet()){
            baseFormatDetailMapByBaseRecordIdentifier.put(baseRecordIdentifier.getValue(), baseFormatDetailMapByBaseRecordIdentifierId.get(baseRecordIdentifier.getKey()));
        }

        return baseFormatDetailMapByBaseRecordIdentifier;
    }

    public static List<Object> generateNestedGridDataForUserFormat(
            String tableConfigKey, FileUploadDownloadUserFormat userFormat, FileUploadDownloadBaseRecordIdentifierDetail baseRecordIdentifier,
            FileUploadDownloadUserRecordIdentifierDetail userRecordIdentifier){
        Map<FileUploadDownloadBaseRecordIdentifierDetail, List<FileUploadDownloadUserFormatDetail>> userFormatDetailMapByBaseRecordIdentifier = prepareUserFormatDetailMapByBaseRecordIdentifier(userFormat);

        List<Object> gridRows = new ArrayList<>();

        // Case where we have root table
        if(NESTED_RID_COLCONFIG_KEY.equals(tableConfigKey) && baseRecordIdentifier == null){
            List<FileUploadDownloadBaseRecordIdentifierDetail> sortedRootIdentifiers = getSortedRootIdentifiers(userFormatDetailMapByBaseRecordIdentifier.keySet());

            for(FileUploadDownloadBaseRecordIdentifierDetail rootIdentifier : sortedRootIdentifiers){
                FileUploadDownloadUserRecordIdentifierDetail rootUserRecordIdentifier = findCommonUserRecordIdentifier(userFormatDetailMapByBaseRecordIdentifier.get(rootIdentifier));
                CompositeRecordIdentifierVO gridRow = new CompositeRecordIdentifierVO(userFormat,rootIdentifier,rootUserRecordIdentifier);

                if(ValidatorUtils.hasNoElements(rootIdentifier.getChildrenRecordIdentifier())){
                    gridRow.setTableConfigKey(NESTED_UFD_COLCONFIG_KEY);
                }else{
                    gridRow.setTableConfigKey(NESTED_RID_COLCONFIG_KEY);
                }

                gridRows.add(gridRow);
            }
        }

        // Case where we have non-leaf RID Table
        else if (NESTED_RID_COLCONFIG_KEY.equals(tableConfigKey)){
            List<FileUploadDownloadBaseRecordIdentifierDetail> sortedBaseIdentifiers = FileUploadDownloadUtil.sortBaseRecordIdentifierByType(baseRecordIdentifier.getChildrenRecordIdentifier());

            CompositeRecordIdentifierVO viewColumnsRow = new CompositeRecordIdentifierVO(userFormat,baseRecordIdentifier,userRecordIdentifier);

            viewColumnsRow = CompositeRecordIdentifierVO.changeVOForViewColumns(viewColumnsRow);
            viewColumnsRow.setTableConfigKey(NESTED_UFD_COLCONFIG_KEY);

            gridRows.add(viewColumnsRow);

            for(FileUploadDownloadBaseRecordIdentifierDetail baseIdentifier : sortedBaseIdentifiers){
                FileUploadDownloadUserRecordIdentifierDetail userRecordIdentifierForRow = findCommonUserRecordIdentifier(userFormatDetailMapByBaseRecordIdentifier.get(baseIdentifier));
                CompositeRecordIdentifierVO gridRow = new CompositeRecordIdentifierVO(userFormat,baseIdentifier,userRecordIdentifierForRow);

                if(ValidatorUtils.hasNoElements(baseIdentifier.getChildrenRecordIdentifier())){
                    gridRow.setTableConfigKey(NESTED_UFD_COLCONFIG_KEY);
                }else{
                    gridRow.setTableConfigKey(NESTED_RID_COLCONFIG_KEY);
                }

                gridRows.add(gridRow);
            }
        }

        // Case where we have leaf UFD Table
        else if (NESTED_UFD_COLCONFIG_KEY.equals(tableConfigKey) && baseRecordIdentifier != null){
            List<FileUploadDownloadUserFormatDetail> userFormatDetails = userFormatDetailMapByBaseRecordIdentifier.get(baseRecordIdentifier);
            List<FileUploadDownloadUserFormatDetail> sortedUserFormatDetails = FileUploadDownloadUtil.sortUserFormatDetailByFieldOrder(userFormatDetails);
            List<FileUploadDownloadUserFormatDetail> skimmedUserFormatDetails = removeDeletedRecords(sortedUserFormatDetails);

            for(FileUploadDownloadUserFormatDetail userFormatDetail : skimmedUserFormatDetails){
                CompositeFormatDetailVO gridRow = new CompositeFormatDetailVO(userFormatDetail);
                gridRow.updateOptionalMandatorySelectedOption();
                gridRows.add(gridRow);
            }
        }

        return gridRows;
    }


    private static FileUploadDownloadUserRecordIdentifierDetail findCommonUserRecordIdentifier(List<FileUploadDownloadUserFormatDetail> userFormatDetails){
        for(FileUploadDownloadUserFormatDetail userFormatDetail : userFormatDetails){
            if(userFormatDetail.getRecordIdentifierId() != null && userFormatDetail.getUserRecordIdentifier() != null){
                return userFormatDetail.getUserRecordIdentifier();
            }
        }

        return null;
    }

    private static Map<FileUploadDownloadBaseRecordIdentifierDetail, List<FileUploadDownloadUserFormatDetail>> prepareUserFormatDetailMapByBaseRecordIdentifier(FileUploadDownloadUserFormat userFormat){

        Map<Long, List<FileUploadDownloadUserFormatDetail>> userFormatDetailMapByBaseRecordIdentifierId = new HashMap();

        Map<Long, FileUploadDownloadBaseRecordIdentifierDetail> baseRecordIdentifierMapById = new HashMap();

        List<FileUploadDownloadUserFormatDetail> sortedUserFormatDetail = FileUploadDownloadUtil.sortUserFormatDetailByFieldOrder(userFormat.getUserFormatDetail());

        EntityDao entityDao = NeutrinoSpringAppContextUtil.getBeanByName("entityDao",EntityDao.class);

        for(FileUploadDownloadUserFormatDetail userFormatDetail : sortedUserFormatDetail){
            FileUploadDownloadBaseRecordIdentifierDetail baseRecordIdentifier = null;
            if(userFormatDetail.getBaseFormatDetail() != null) {
                baseRecordIdentifier = userFormatDetail.getBaseFormatDetail().getSystemRecordIdentifier();
            }else{
                baseRecordIdentifier = entityDao.find(FileUploadDownloadBaseRecordIdentifierDetail.class,userFormatDetail.getUserRecordIdentifier().getBaseIdentifierId());
            }

            List<FileUploadDownloadUserFormatDetail> mappedUserFormatDetailList = userFormatDetailMapByBaseRecordIdentifierId.get(baseRecordIdentifier.getId());
            if(mappedUserFormatDetailList == null){
                mappedUserFormatDetailList = new ArrayList<>();
                userFormatDetailMapByBaseRecordIdentifierId.put(baseRecordIdentifier.getId(),mappedUserFormatDetailList);
            }

            if(baseRecordIdentifierMapById.get(baseRecordIdentifier.getId()) == null){
                baseRecordIdentifierMapById.put(baseRecordIdentifier.getId(),baseRecordIdentifier);
            }

            mappedUserFormatDetailList.add(userFormatDetail); // Already sorted by fieldOrder
        }

        Map<FileUploadDownloadBaseRecordIdentifierDetail, List<FileUploadDownloadUserFormatDetail>> userFormatDetailMapByBaseRecordIdentifier = new HashMap();

        for(Map.Entry<Long, FileUploadDownloadBaseRecordIdentifierDetail> baseRecordIdentifier : baseRecordIdentifierMapById.entrySet()){
            userFormatDetailMapByBaseRecordIdentifier.put(baseRecordIdentifier.getValue(),
                    userFormatDetailMapByBaseRecordIdentifierId.get(baseRecordIdentifier.getKey()));
        }

        return userFormatDetailMapByBaseRecordIdentifier;

    }

    private static List<FileUploadDownloadBaseRecordIdentifierDetail> getSortedRootIdentifiers(Set<FileUploadDownloadBaseRecordIdentifierDetail> baseRecordIdentifierSet) {
        List<FileUploadDownloadBaseRecordIdentifierDetail> sortedRootIdentifiers = new ArrayList<>();

        for(FileUploadDownloadBaseRecordIdentifierDetail baseRecordIdentifier:baseRecordIdentifierSet){
            if(baseRecordIdentifier.getParentRecordIdentifier() == null){
                sortedRootIdentifiers.add(baseRecordIdentifier);
            }
        }

        sortedRootIdentifiers = FileUploadDownloadUtil.sortBaseRecordIdentifierByType(sortedRootIdentifiers);

        return sortedRootIdentifiers;
    }

    private static List<FileUploadDownloadUserFormatDetail> removeDeletedRecords(List<FileUploadDownloadUserFormatDetail> userFormatDetails){
        List<FileUploadDownloadUserFormatDetail> skimmedUserFormatDetails = new ArrayList<>();

        for(FileUploadDownloadUserFormatDetail userFormatDetail : userFormatDetails){

            //Refer BaseMasterUtils.addDeletedRecords(...)
            if(userFormatDetail.getApprovalStatus() != ApprovalStatus.CHILD_DELETED && userFormatDetail.getApprovalStatus() != ApprovalStatus.DELETED_APPROVED_IN_HISTORY){
                skimmedUserFormatDetails.add(userFormatDetail);
            }
        }

        return skimmedUserFormatDetails;
    }



}
