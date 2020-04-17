package com.nucleus.core.genericparameter.service;

import static com.nucleus.core.genericparameter.service.GenericParameterServiceImpl.NEW_GENERIC_PARAMETER;
import static com.nucleus.core.genericparameter.service.GenericParameterServiceImpl.OLD_GENERIC_PARAMETER;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.process.beans.EntityApprovalPreProcessor;

@Named("genericParameterApprovalListner")
public class GenericParameterApprovalListner implements EntityApprovalPreProcessor {

    @Inject
    @Named("genericParameterPostCommitWork")
    private TransactionPostCommitWork genericParameterPostCommitWork;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Inject
    private EntityDao entityDao;


    @Override
    public void handleApprovalForModification(BaseMasterEntity originalRecord, BaseMasterEntity toBeDeletedRecord, BaseMasterEntity toBeHistoryRecord, Long reviewerId) {


       /* Map<String,Object> argumentsMap = new HashMap<>();
        argumentsMap.put(NEW_GENERIC_PARAMETER, entityDao.get(originalRecord.getEntityId()));
        argumentsMap.put(OLD_GENERIC_PARAMETER, entityDao.get(toBeHistoryRecord.getEntityId()));*/

        Class genericParameterClass =  originalRecord.getClass();
        GenericParameter genericParameter = (GenericParameter)originalRecord;
        GenericParameter genericParameter1;
        if("DynamicGenericParameter".equalsIgnoreCase(genericParameter.getClass().getSimpleName())){
        	genericParameter1= genericParameterService.getDefaultValueForDynamicDtype(genericParameterClass,genericParameter.getDynamicParameterName());
        }else{
        	genericParameter1= genericParameterService.getDefaultValue(genericParameterClass);
        }

        if(genericParameter1!=null && genericParameter.getDefaultFlag()){
            genericParameter1.setDefaultFlag(false);
            //entityDao.saveOrUpdate(genericParameter1);
            genericParameterService.updateGenericParameter(genericParameter1);
            entityDao.flush();
        }
        genericParameterService.createOrUpdateGenericParameterCache(entityDao.get(originalRecord.getEntityId()),entityDao.get(toBeHistoryRecord.getEntityId()),null,Boolean.TRUE);
    }

    @Override
    public void handleApprovalForNew(BaseMasterEntity originalRecord, BaseMasterEntity toBeDeletedRecord, BaseMasterEntity toBeHistoryRecord, Long reviewerId) {
       /* Map<String,Object> argumentsMap = new HashMap<>();
        argumentsMap.put(NEW_GENERIC_PARAMETER, entityDao.get(toBeDeletedRecord.getEntityId()) );
        argumentsMap.put(OLD_GENERIC_PARAMETER, null);*/

        Class genericParameterClass =  toBeDeletedRecord.getClass();
        GenericParameter genericParameter = (GenericParameter)toBeDeletedRecord;
        GenericParameter genericParameter1;
        if("DynamicGenericParameter".equalsIgnoreCase(genericParameter.getClass().getSimpleName())){
        	genericParameter1= genericParameterService.getDefaultValueForDynamicDtype(genericParameterClass,genericParameter.getDynamicParameterName());
        }else{
        	genericParameter1= genericParameterService.getDefaultValue(genericParameterClass);
        }

        if(genericParameter1!=null && genericParameter.getDefaultFlag()){
            genericParameter1.setDefaultFlag(false);
           // entityDao.saveOrUpdate(genericParameter1);
            genericParameterService.updateGenericParameter(genericParameter1);
            entityDao.flush();
        }
        genericParameterService.createOrUpdateGenericParameterCache(entityDao.get(toBeDeletedRecord.getEntityId()),null,null);
    }

    @Override
    public void handleDeclineForModification(BaseMasterEntity originalRecord, BaseMasterEntity toBeDeletedRecord, Long reviewerId) {

    }

    @Override
    public void handleDeclineForNew(BaseMasterEntity originalRecord, BaseMasterEntity toBeDeletedRecord, Long reviewerId) {

    }

    @Override
    public void handleSendBackForNew(BaseMasterEntity originalRecord, BaseMasterEntity toBeDeletedRecord, BaseMasterEntity toBeHistoryRecord, Long reviewerId) {

    }

    @Override
    public void handleSendBackForModification(BaseMasterEntity originalRecord, BaseMasterEntity toBeDeletedRecord, BaseMasterEntity toBeHistoryRecord, Long reviewerId) {

    }
}