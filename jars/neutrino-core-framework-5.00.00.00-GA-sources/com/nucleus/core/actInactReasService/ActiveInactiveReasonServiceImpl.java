package com.nucleus.core.actInactReasService;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonActive;
import com.nucleus.activeInactiveReason.ReasonInActive;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.base.validation.domainobject.ValidationRuleResult;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.persistence.HibernateUtils;
import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Named("activeInactiveReasonService")
public class ActiveInactiveReasonServiceImpl implements ActiveInactiveReasonService{

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Override
    public Boolean checkForActiveInactiveForApprovedModified(ReasonsActiveInactiveMapping reasonsActiveInactiveMapping,String masterName,String uniqueParameter,String uniqueValue) {
        boolean flag = true;
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        StringBuilder sb = new StringBuilder();
        sb.append("select distinct c from "+masterName+" c where c."+uniqueParameter+" = :uniqueValue and c.masterLifeCycleData.approvalStatus IN :approvalStatus");
        JPAQueryExecutor<BaseMasterEntity> jpaQueryExecutor = new JPAQueryExecutor<BaseMasterEntity>(sb.toString());
        jpaQueryExecutor.addParameter("uniqueValue", uniqueValue).addParameter("approvalStatus", approvalStatusList);
        BaseMasterEntity entity1  = entityDao.executeQueryForSingleValue(jpaQueryExecutor);
        if (entity1 != null) {
            Field field = null;
            try {
                field = entity1.getClass().getDeclaredField("reasonActInactMap");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            field.setAccessible(true);
            try {
                ReasonsActiveInactiveMapping value = (ReasonsActiveInactiveMapping) field.get(entity1);
                if (value == null) {
                    if (entity1 !=  null && entity1.isActiveFlag()) {
                        if (reasonsActiveInactiveMapping !=null && reasonsActiveInactiveMapping.getMasterActiveInactiveReasons() != null) {
                            for (MasterActiveInactiveReasons mst : reasonsActiveInactiveMapping.getMasterActiveInactiveReasons()) {
                                if (mst.getReasonInactive() != null && mst.getReasonInactive().getCode() != null) {
                                    flag = true;
                                }
                                if (mst.getReasonActive() != null && mst.getReasonActive().getCode() != null) {
                                    flag = false;
                                    return flag;
                                }
                                if(mst.getReasonActive() == null && mst.getReasonInactive() == null){
                                    flag = true;
                                }
                            }

                        }

                    }
                    if (entity1 !=  null && !entity1.isActiveFlag()) {
                        if (reasonsActiveInactiveMapping !=null && reasonsActiveInactiveMapping.getMasterActiveInactiveReasons() != null) {
                            for (MasterActiveInactiveReasons mst : reasonsActiveInactiveMapping.getMasterActiveInactiveReasons()) {
                                if (mst.getReasonInactive() != null && mst.getReasonInactive().getCode() != null) {
                                    flag = false;
                                    return flag;
                                    }
                                if (mst.getReasonActive() != null && mst.getReasonActive().getCode() != null) {
                                    flag = true;
                                }
                                if(mst.getReasonActive() == null && mst.getReasonInactive() == null){
                                    flag = true;
                                }
                            }
                        }

                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
      else if(entity1 == null && (reasonsActiveInactiveMapping != null && reasonsActiveInactiveMapping.getMasterActiveInactiveReasons() != null)){
            List<ReasonActive> activeReasonList = new ArrayList<>();
            List<ReasonInActive> InactiveReasonList = new ArrayList<>();
            reasonsActiveInactiveMapping.getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonInactive() != null).forEach(m -> InactiveReasonList.add(m.getReasonInactive()));
            reasonsActiveInactiveMapping.getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonActive() != null).forEach(m -> activeReasonList.add(m.getReasonActive()));
            if(CollectionUtils.isNotEmpty(activeReasonList) || CollectionUtils.isNotEmpty(InactiveReasonList))
                flag = false;
        }

        return flag;
    }

    @Override
    public List<MasterActiveInactiveReasons> getMasterReasonList(List<MasterActiveInactiveReasons> msterReasListFromUpdate,List<MasterActiveInactiveReasons> msterReasListToUpdate,List<ValidationRuleResult> dataValidationRuleResults) {
        if (msterReasListToUpdate != null){
            for (MasterActiveInactiveReasons mst : msterReasListToUpdate) {
                MasterActiveInactiveReasons mstReason = new MasterActiveInactiveReasons();
                if (mst.getReasonInactive() != null && mst.getReasonInactive().getCode() != null) {
                    ReasonInActive reasonInActive = genericParameterService.findByCode(mst.getReasonInactive().getCode(), ReasonInActive.class);
                    mstReason.setReasonInactive(reasonInActive);
                    HibernateUtils.initializeAndUnproxy(reasonInActive);
                    if (mst.getDescription() != null) {
                        if (reasonInActive.getCode().equalsIgnoreCase("Other")) {
                            mstReason.setDescription(mst.getDescription());
                        } else {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Description Not Required,Required in OTHER REASON only", Message.MessageType.ERROR, mst.getDescription()));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                    } else {
                        if (!reasonInActive.getCode().equalsIgnoreCase("Other"))
                        mstReason.setDescription(reasonInActive.getDescription());
                    }
                    if (reasonInActive == null) {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Incorrect code for reason", Message.MessageType.ERROR, mst.getReasonInactive().getCode()));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                }
                if (mst.getReasonActive() != null && mst.getReasonActive().getCode() != null) {
                    ReasonActive reasonActive = genericParameterService.findByCode(mst.getReasonActive().getCode(), ReasonActive.class);
                    mstReason.setReasonActive(reasonActive);
                    HibernateUtils.initializeAndUnproxy(reasonActive);
                    if (mst.getDescription() != null) {
                        if (reasonActive.getCode().equalsIgnoreCase("Other")) {
                            mstReason.setDescription(mst.getDescription());
                        } else {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Description Not Required,Required in OTHER REASON only", Message.MessageType.ERROR, mst.getDescription()));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                    } else {
                        if (!reasonActive.getCode().equalsIgnoreCase("Other"))
                        mstReason.setDescription(reasonActive.getDescription());
                    }
                    if (reasonActive == null) {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Incorrect code for reason", Message.MessageType.ERROR, mst.getReasonActive().getCode()));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                }
                if ((mst.getReasonActive() == null && mst.getReasonInactive() == null) && mst.getDescription() != null) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Description Not Required,Required in OTHER REASON only", Message.MessageType.ERROR, mst.getDescription()));
                    dataValidationRuleResults.add(validationRuleResult);
                }

               // entityDao.saveOrUpdate(mstReason);
                if (mst.getReasonActive() != null || mst.getReasonInactive() != null)
                msterReasListFromUpdate.add(mstReason);

            }
            checkDescriptionForOthers(msterReasListToUpdate, dataValidationRuleResults);
    }
        return msterReasListFromUpdate;
    }

    @Override
    public Boolean checkForActionofReasons(ReasonsActiveInactiveMapping reasonsActiveInactiveMapping) {
        boolean actionFlag = true;
        if (reasonsActiveInactiveMapping != null && reasonsActiveInactiveMapping.getTypeOfAction() != null) {
            List<ReasonActive> activeReasonList = new ArrayList<>();
            List<ReasonInActive> InactiveReasonList = new ArrayList<>();
            if (reasonsActiveInactiveMapping.getTypeOfAction().equalsIgnoreCase("Active") && CollectionUtils.isNotEmpty(reasonsActiveInactiveMapping.getMasterActiveInactiveReasons())) {
                reasonsActiveInactiveMapping.getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonInactive() != null).forEach(m -> InactiveReasonList.add(m.getReasonInactive()));
                if (CollectionUtils.isNotEmpty(InactiveReasonList)) {
                    actionFlag = false;
                    return actionFlag;
                }
            } else if (reasonsActiveInactiveMapping.getTypeOfAction().equalsIgnoreCase("Inactive") && CollectionUtils.isNotEmpty(reasonsActiveInactiveMapping.getMasterActiveInactiveReasons())) {
                reasonsActiveInactiveMapping.getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonActive() != null).forEach(m -> activeReasonList.add(m.getReasonActive()));
                if (CollectionUtils.isNotEmpty(activeReasonList)) {
                    actionFlag = false;
                    return actionFlag;
                }
            }


        }
        return actionFlag;
    }

    @Override
    public Boolean checkForDuplicateReasons(ReasonsActiveInactiveMapping reasonsActiveInactiveMapping) {
        boolean reasonFlag = true;
            Set<ReasonActive> reasonActiveTreeSet = new TreeSet<ReasonActive>(new Comparator<ReasonActive>() {
                @Override
                public int compare(ReasonActive o1, ReasonActive o2) {
                    if ((o1.getCode() != null && o2.getCode() != null) && (o1.getCode().equals(o2.getCode()))) {
                        if(o1.getCode().equalsIgnoreCase("other"))
                            return 1;
                        else
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });


            Set<ReasonInActive> reasonInActiveTreeSet = new TreeSet<ReasonInActive>(new Comparator<ReasonInActive>() {
                @Override
                public int compare(ReasonInActive o1, ReasonInActive o2) {
                    if ((o1.getCode() != null && o2.getCode() != null) && (o1.getCode().equals(o2.getCode()))) {
                        if(o1.getCode().equalsIgnoreCase("other"))
                            return 1;
                        else
                            return 0;
                    } else {
                        return 1;
                    }
                }
            });
            List<ReasonActive> activeReasonList1 = new ArrayList<>();
            List<ReasonInActive> InactiveReasonList1 = new ArrayList<>();
            if (reasonsActiveInactiveMapping != null && reasonsActiveInactiveMapping.getMasterActiveInactiveReasons() != null) {
                reasonsActiveInactiveMapping.getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonActive() != null).forEach(m -> reasonActiveTreeSet.add(m.getReasonActive()));
                reasonsActiveInactiveMapping.getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonActive() != null).forEach(m -> activeReasonList1.add(m.getReasonActive()));
                if (activeReasonList1.size() != reasonActiveTreeSet.size()) {

                    reasonFlag = false;
                    return reasonFlag;

                }
                reasonsActiveInactiveMapping.getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonInactive() != null).forEach(m -> reasonInActiveTreeSet.add(m.getReasonInactive()));
                reasonsActiveInactiveMapping.getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonInactive() != null).forEach(m -> InactiveReasonList1.add(m.getReasonInactive()));
                if (InactiveReasonList1.size() != reasonInActiveTreeSet.size()) {
                    reasonFlag = false;
                    return reasonFlag;
                }
            }

        return reasonFlag;
    }

    public Boolean checkForGenericReasons(ReasonsActiveInactiveMapping reasonsActiveInactiveMapping) {
        boolean correctReasonFlag = true;
        if (reasonsActiveInactiveMapping != null && reasonsActiveInactiveMapping.getMasterActiveInactiveReasons() != null ) {
        for(MasterActiveInactiveReasons mst : reasonsActiveInactiveMapping.getMasterActiveInactiveReasons()){
            if(mst.getReasonActive() != null){
                ReasonActive reasonActive = genericParameterService.findByCode(mst.getReasonActive().getCode(), ReasonActive.class);
                if(reasonActive == null)
                    correctReasonFlag = false;
            }
            if(mst.getReasonInactive() != null){
                ReasonInActive reasonInActive = genericParameterService.findByCode(mst.getReasonInactive().getCode(), ReasonInActive.class);
                if(reasonInActive == null)
                    correctReasonFlag = false;
            }
          }

        }
       return correctReasonFlag;
    }

    public void checkDescriptionForOthers(List<MasterActiveInactiveReasons> masterActiveInactiveReasons, List<ValidationRuleResult> dataValidationRuleResults){
         Boolean descFlag = true;

        if(!masterActiveInactiveReasons.isEmpty() && masterActiveInactiveReasons.size() > 0) {
            Optional flagActive = null;
            Optional flagInactive = null;
            flagActive = masterActiveInactiveReasons.stream().filter(m -> Objects.nonNull(m))
                    .filter(m -> m.getReasonActive() != null && m.getReasonActive().getCode().equalsIgnoreCase("Other") && m.getDescription() == null).findAny();
            flagInactive = masterActiveInactiveReasons.stream().filter(m -> Objects.nonNull(m))
                    .filter(m -> m.getReasonInactive() != null && m.getReasonInactive().getCode().equalsIgnoreCase("Other") && m.getDescription() == null).findAny();

            if(flagActive != Optional.empty()  || flagInactive != Optional.empty()){
                descFlag = false;
            }
            else
                descFlag = true;

        }
        if(!descFlag){
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Error in Other Reason's Description", Message.MessageType.ERROR,"Description for Other Reason is mandatory"));
            dataValidationRuleResults.add(validationRuleResult);
        }
    }


}
