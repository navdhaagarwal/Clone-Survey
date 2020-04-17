package com.nucleus.cas.businessmapping;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.ParentBranchMapping;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.process.beans.EntityApprovalPreProcessor;
import com.nucleus.user.User;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named("userOrgBranchPreProcessor")
public class UserOrgBranchPreProcessor implements EntityApprovalPreProcessor {

    public static final String USER_MANAGEMENT_GET_USER_ORG_BRANCH_FROM_PARENT_BRANCH_CHILDS = "UserManagement.getUserOrgBranchFromParentBranchChilds";
    public static final String USER_MANAGEMENT_GET_CHILD_IDS_FROM_PARENT_BRANCH_ID = "UserManagement.getChildIdsFromParentBranchId";
    public static final String SYSTEM_NAME = "systemName";
    public static final String APPROVAL_STATUS = "approvalStatus";
    public static final String PARENT_ORG_ID = "parentOrgId";
    public static final String CHILD_BRANCH_IDS = "childBranchIds";
    @Inject
    @Named("organizationService")
    private OrganizationService organizationService;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;



    @Override
    public void handleApprovalForModification(BaseMasterEntity originalRecord, BaseMasterEntity toBeDeletedRecord, BaseMasterEntity toBeHistoryRecord, Long reviewerId) {

    }

    @Override
    public void handleApprovalForNew(BaseMasterEntity originalRecord, BaseMasterEntity toBeDeletedRecord, BaseMasterEntity toBeHistoryRecord, Long reviewerId) {
        String master =toBeDeletedRecord.getClass().getName();
        String organizationName=OrganizationBranch.class.getName();
        if(master.equalsIgnoreCase(organizationName)){

                OrganizationBranch organizationBranch=(OrganizationBranch)toBeDeletedRecord;
                if(isTypeBranch(organizationBranch)) {
                    mapUserOrgBranch(organizationBranch);
                }
        }
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


    private List<Map<String, Object>> getUserOrgBranchMappings(List<Long> childBranchesIds,String ModuleName){
        TreeSet<Long> childIds=new TreeSet<>(childBranchesIds);
        childBranchesIds=new ArrayList<>(childIds);
        List<Map<String, Object>> userOrgBranchMappings=new ArrayList<>();
        for(int startIndex=0,endIndex=1000;startIndex<childIds.size();startIndex=endIndex,endIndex+=1000) {
            List<Long> subSetChildIds=null;
            if(endIndex<childIds.size()){
                subSetChildIds=childBranchesIds.subList(startIndex, endIndex);
            }else{
                subSetChildIds=childBranchesIds.subList(startIndex, childIds.size());
            }
            NamedQueryExecutor<Map<String, Object>> userOrgBranchMappingQuery = new NamedQueryExecutor<>(USER_MANAGEMENT_GET_USER_ORG_BRANCH_FROM_PARENT_BRANCH_CHILDS);
            userOrgBranchMappingQuery.addParameter(CHILD_BRANCH_IDS, subSetChildIds).addParameter(SYSTEM_NAME, ModuleName);
            List<Map<String, Object>> subUserOrgBranchMappings = entityDao.executeQuery(userOrgBranchMappingQuery);
            if(CollectionUtils.isNotEmpty(subUserOrgBranchMappings))
            userOrgBranchMappings.addAll(subUserOrgBranchMappings);
        }
        
        return userOrgBranchMappings;
    }

    private List<Long> getChildIds(OrganizationBranch organizationBranchParent,String ModuleName){
        List<Long> childIds=null;
        if(organizationBranchParent!=null && !isTypeBranch(organizationBranchParent)) {
            NamedQueryExecutor<OrganizationBranch> parentChildNamedQuery = new NamedQueryExecutor<>(USER_MANAGEMENT_GET_CHILD_IDS_FROM_PARENT_BRANCH_ID);
            parentChildNamedQuery.addParameter(PARENT_ORG_ID, organizationBranchParent.getId()).addParameter(SYSTEM_NAME, ModuleName).addParameter(APPROVAL_STATUS, ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
            List<OrganizationBranch> organizationBranches = entityDao.executeQuery(parentChildNamedQuery);
            Set<OrganizationBranch> totalChildList=new HashSet<>();
            if (CollectionUtils.isNotEmpty(organizationBranches)) {
                Iterator<OrganizationBranch> itr=organizationBranches.iterator();
                while (itr.hasNext()){
                    OrganizationBranch organizationBranch=itr.next();
                    if (!isTypeBranch(organizationBranch)){
                        parentChildNamedQuery.addParameter(PARENT_ORG_ID, organizationBranch.getId());
                        List<OrganizationBranch> organizationBranchesChild = entityDao.executeQuery(parentChildNamedQuery);
                        totalChildList.addAll(organizationBranchesChild);
                        itr.remove();
                    }
                }
            }
            totalChildList.addAll(organizationBranches);
            childIds=totalChildList.stream().map(OrganizationBranch::getId).collect(Collectors.toList());
        }

        return childIds;
    }
    private boolean isTypeBranch(OrganizationBranch organizationBranch){
        boolean isTypeBranch=false;
        if(organizationBranch.getOrganizationType()!=null && "branch".equalsIgnoreCase(organizationBranch.getOrganizationType().getCode())) {
            isTypeBranch = true;
        }
        return isTypeBranch;
    }

    private void mapUserOrgBranch(OrganizationBranch organizationBranch){
        if(CollectionUtils.isNotEmpty(organizationBranch.getParentBranchMapping())){
            List<ParentBranchMapping> parentBranchMappings=organizationBranch.getParentBranchMapping();
            for(ParentBranchMapping parentBranchMapping:parentBranchMappings){
                Map<User,List<Long>> userOrgBranchMappingMap=new HashMap<>();
                List<Long> childBranchesIds=getChildIds(parentBranchMapping.getParentBranch(),parentBranchMapping.getModuleName().getCode());
                if(childBranchesIds!=null && childBranchesIds.contains(organizationBranch.getId())) {
                    childBranchesIds.remove(organizationBranch.getId());
                }
                if(CollectionUtils.isEmpty(childBranchesIds)){
                    OrganizationBranch parentOfParentBranch=parentBranchMapping.getParentBranch();
                    if(CollectionUtils.isNotEmpty(parentOfParentBranch.getParentBranchMapping())){
                        for(ParentBranchMapping parentBranchMapping1:parentOfParentBranch.getParentBranchMapping()){
                            if(parentBranchMapping1.getModuleName().getId().equals(parentBranchMapping.getModuleName().getId())){
                                childBranchesIds=getChildIds(parentBranchMapping1.getParentBranch(),parentBranchMapping.getModuleName().getCode());
                                if(childBranchesIds!=null && childBranchesIds.contains(organizationBranch.getId())) {
                                    childBranchesIds.remove(organizationBranch.getId());
                                }
                            }

                        }
                    }
                }
                if(parentBranchMapping.getModuleName()!=null) {
                    List<Map<String, Object>> userOrgBranchMappings = getUserOrgBranchMappings(childBranchesIds,parentBranchMapping.getModuleName().getCode());

                    for (Map<String, Object> userOrgBranchMap : userOrgBranchMappings) {
                        User user = (User) userOrgBranchMap.get("user");
                        Long branchId = (Long) userOrgBranchMap.get("branchId");
                        List<Long> branchIds=null;
                        if(userOrgBranchMappingMap.get(user)==null){
                            branchIds=new ArrayList<>();
                            branchIds.add(branchId);
                        }else{
                            branchIds=userOrgBranchMappingMap.get(user);
                            branchIds.add(branchId);
                        }
                        userOrgBranchMappingMap.put(user, branchIds);
                    }
                    if(userOrgBranchMappingMap!=null){
                        for(Map.Entry<User,List<Long>> entrySet:userOrgBranchMappingMap.entrySet()){
                            List<Long> branchesIds=entrySet.getValue();
                            if(branchesIds.containsAll(childBranchesIds)){
                                UserOrgBranchMapping userOrgBranchMapping=new UserOrgBranchMapping();
                                userOrgBranchMapping.setOperationType("A");
                                userOrgBranchMapping.setPrimaryBranch(false);
                                userOrgBranchMapping.setBranchAdmin(false);
                                userOrgBranchMapping.setActiveFlag(true);
                                userOrgBranchMapping.setApprovalStatus(0);
                                userOrgBranchMapping.setOrganizationBranch(organizationBranch);
                                userOrgBranchMapping.setOrganizationBranchId(organizationBranch.getId());
                                userOrgBranchMapping.setAssociatedUser(entrySet.getKey());
                                entityDao.saveOrUpdate(userOrgBranchMapping);
                            }
                        }
                    }
                }
            }
        }
    }

}
