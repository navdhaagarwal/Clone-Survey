package com.nucleus.team.teamUploadDownloadService;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonActive;
import com.nucleus.activeInactiveReason.ReasonInActive;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.actInactReasService.ActiveInactiveReasonService;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.ITeamUploadBusinessObj;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.core.team.vo.TeamVO;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.validation.domainobject.ValidationRuleResult;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.CASValidationUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import com.nucleus.finnone.pro.base.utility.CoreUtility;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Named("teamUploadBusinessObj")
public class TeamUploadBusinessObj extends BaseServiceImpl implements ITeamUploadBusinessObj {

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    @Inject
    @Named("activeInactiveReasonService")
    private ActiveInactiveReasonService activeInactiveReasonService;

    @Inject
    @Named("teamService")
    private TeamService teamService;

    @Inject
    @Named("userService")
    private UserService userService;

    @Inject
    @Named("teamUploadDownloadUtility")
    private TeamUploadDownloadUtility teamUploadDownload;

    private static final String DELETE = "Delete";

    private static final String EDIT = "Edit";

    private static final String ACTIVE = "ACTIVE";

    private static final String INACTIVE = "INACTIVE";

    public TeamVO uploadTeam(TeamVO teamVO) {
        List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
        teamUploadOperation(teamVO, dataValidationRuleResults);
        printError(teamVO, dataValidationRuleResults);
        return teamVO;
    }

    public void chekForMaliciousString(String name, String description, List<ValidationRuleResult> dataValidationRuleResults) {

        chekForMaliciousTeamName(name, dataValidationRuleResults);

        if (StringUtils.isNotEmpty(description))  {
            if (description.length() > 255) {
                teamUploadDownload.setErrorMessages("Team Description up to length 255 is allowed", dataValidationRuleResults, description);
            }
            if (!CASValidationUtils.isAlphaNumeric(description)) {
                description = new StringBuilder(description).insert(0, "'").toString();
                teamUploadDownload.setErrorMessages("Team Description allowed only alphanumeric characters hyphen and spaces between characters.", dataValidationRuleResults, description);
            }
        }

    }


    public void saveReasonForApprovedRecord(Team recordToUpdate, TeamVO entityVO, List<ValidationRuleResult> dataValidationRuleResults) {
        List<MasterActiveInactiveReasons> mstActInactReasList = new ArrayList<>();
        if (recordToUpdate.getReasonActInactMap() != null && recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("select r.masterActiveInactiveReasons from ReasonsActiveInactiveMapping r where r.id = :Value");
            JPAQueryExecutor<MasterActiveInactiveReasons> jpaQueryExecutor = new JPAQueryExecutor<MasterActiveInactiveReasons>(sb.toString());
            jpaQueryExecutor.addParameter("Value", recordToUpdate.getReasonActInactMap().getId());
            List<MasterActiveInactiveReasons> masterActiveInactiveReasonsList = entityDao.executeQuery(jpaQueryExecutor);
            mstActInactReasList = masterActiveInactiveReasonsList;
            mstActInactReasList.clear();
        }
        List<ReasonActive> activeReasonList = new ArrayList<>();
        List<ReasonInActive> InactiveReasonList = new ArrayList<>();
        if (entityVO.getReasonActInactMap() != null && entityVO.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
            entityVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonInactive() != null).forEach(m -> InactiveReasonList.add(m.getReasonInactive()));
            entityVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonActive() != null).forEach(m -> activeReasonList.add(m.getReasonActive()));
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(activeReasonList) || org.apache.commons.collections4.CollectionUtils.isNotEmpty(InactiveReasonList))
            mstActInactReasList = activeInactiveReasonService.getMasterReasonList(mstActInactReasList, entityVO.getReasonActInactMap().getMasterActiveInactiveReasons(), dataValidationRuleResults);
        else if (recordToUpdate.getReasonActInactMap() != null && recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons() != null
                && (entityVO.getReasonActInactMap() != null && (entityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(ACTIVE)
                && recordToUpdate.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(ACTIVE)) || ((entityVO.getReasonActInactMap() != null && entityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(INACTIVE)
                && (recordToUpdate.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(INACTIVE)))))) {
            mstActInactReasList = recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons();
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(mstActInactReasList))
                mstActInactReasList.add(new MasterActiveInactiveReasons());
        } else if (entityVO.getReasonActInactMap() == null && recordToUpdate.getReasonActInactMap() != null) {
            mstActInactReasList = recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons();
            entityVO.setReasonActInactMap(recordToUpdate.getReasonActInactMap());
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(mstActInactReasList))
                mstActInactReasList.add(new MasterActiveInactiveReasons());
        } else {
            mstActInactReasList.add(new MasterActiveInactiveReasons());
            if (entityVO.getReasonActInactMap() == null)
                entityVO.setReasonActInactMap(new ReasonsActiveInactiveMapping());
        }
        entityVO.getReasonActInactMap().setMasterActiveInactiveReasons(mstActInactReasList);
        recordToUpdate.setReasonActInactMap(entityVO.getReasonActInactMap());
        if (entityVO.getReasonActInactMap() != null && entityVO.getReasonActInactMap().getTypeOfAction() != null && entityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(ACTIVE)) {
            recordToUpdate.setActiveFlag(true);
        }
        if (entityVO.getReasonActInactMap() != null && entityVO.getReasonActInactMap().getTypeOfAction() != null && entityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(INACTIVE)) {
            recordToUpdate.setActiveFlag(false);
        }

    }

    public void chekForMaliciousTeamName(String name, List<ValidationRuleResult> dataValidationRuleResults) {
        if (StringUtils.isNotEmpty(name)) {
            if (name.length() > 255) {
                teamUploadDownload.setErrorMessages("Team Name up to length 255 is allowed", dataValidationRuleResults, name);
            }
            if (!CASValidationUtils.isAlphaNumeric(name)) {
                name = new StringBuilder(name).insert(0, "'").toString();
                teamUploadDownload.setErrorMessages("Team Name allowed only alphanumeric characters hyphen and spaces between characters.", dataValidationRuleResults, name);
            }
        }
    }

    public List<OrganizationBranch> getOrgBranchesOfBranchType(String organizationType, String branchCode) {
        List<OrganizationBranch> orgBranchList = null;
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
                "Organization.getOrgBranchByBranchCodeAndBranchType").addParameter("approvalStatus", statusList).addParameter(
                "branchCode", branchCode).addParameter("orgType", organizationType);
        if (StringUtils.isNotEmpty(branchCode) && StringUtils.isNotEmpty(organizationType)) {
            orgBranchList = entityDao.executeQuery(executor);
        }
        return orgBranchList;
    }

    public void teamUploadOperation(TeamVO teamVO, List<ValidationRuleResult> dataValidationRuleResults) {
        if (teamVO.getOperationType() != null) {

            if (teamVO.getOperationType().equalsIgnoreCase(DELETE)) {
                deleteTeamOperation(teamVO, dataValidationRuleResults);
            } else if (teamVO.getOperationType().equalsIgnoreCase(EDIT)) {
                editTeamOperation(teamVO, dataValidationRuleResults);
            } else if (!((teamVO.getOperationType().equalsIgnoreCase(EDIT)) || (teamVO.getOperationType().equalsIgnoreCase(DELETE)))) {
                teamUploadDownload.setErrorMessages("Invalid - ", dataValidationRuleResults, "Team Operation Type");
            }
        } else {
            createTeamOperation(teamVO, dataValidationRuleResults);
        }
    }

    public void createTeamOperation(TeamVO teamVO, List<ValidationRuleResult> dataValidationRuleResults) {
        Team team = new Team();
        List<String> userNameList = new ArrayList<>();
        teamUploadDownload.mandatoryCheckForTeamUpload(teamVO, dataValidationRuleResults, userNameList);
        chekForMaliciousString(teamVO.getName(), teamVO.getDescription(), dataValidationRuleResults);
        if (CollectionUtils.isEmpty(dataValidationRuleResults)) {
            if (teamService.isThisTeamNamePresent(teamVO.getName())) {
                teamUploadDownload.setErrorMessages("Team Name already exists", dataValidationRuleResults, teamVO.getName());
            } else {

                team.setName(teamVO.getName());
                team.setDescription(teamVO.getDescription());

                List<OrganizationBranch> orgBranchList = null;
                OrganizationBranch organizationBranch = null;
                if (teamVO.isRegionOfficeBranch()) {
                    orgBranchList = getOrgBranchesOfBranchType(OrganizationType.ORGANIZATION_TYPE_BRANCH_RO, teamVO.getTeamBranch().getBranchCode());
                } else {
                    orgBranchList = getOrgBranchesOfBranchType(OrganizationType.ORGANIZATION_TYPE_BRANCH, teamVO.getTeamBranch().getBranchCode());
                }
                if (CollectionUtils.isNotEmpty(orgBranchList)) {
                    organizationBranch = orgBranchList.get(0);
                    team.setTeamBranch(orgBranchList.get(0));
                } else {
                    teamUploadDownload.setErrorMessages("Team Branch is incorrect", dataValidationRuleResults, teamVO.getName());
                }

                if (null != organizationBranch){
                    List<UserInfo> userInfoList = teamService.getAllUsersPresentInTeamBranch(organizationBranch);
                    if (CollectionUtils.isNotEmpty(userInfoList)){
                        List<String> allowedUsernameList = userInfoList.stream().map(UserInfo::getUsername).collect(Collectors.toList());
                        teamUploadDownload.userbelongToThisTeam(allowedUsernameList,userNameList,dataValidationRuleResults);
                    }
                }

                User teamLead = userService.findUserByUsername(teamVO.getTeamLead().getUsername());
                if (null != teamLead) {
                    team.setTeamLead(teamLead);
                } else {
                    teamUploadDownload.setErrorMessages("No such team lead user found", dataValidationRuleResults);
                }

                Set<User> teamUsers = new LinkedHashSet<>();
                for (User userVO : teamVO.getUsers()) {
                    if (null != userVO && null != userVO.getUsername()) {
                        User dbUser = userService.findUserByUsername(userVO.getUsername());
                        if (dbUser != null) {
                            teamUsers.add(dbUser);
                        } else {
                            teamUploadDownload.setErrorMessages("Username is incorrect ", dataValidationRuleResults, userVO.getUsername());
                        }
                    }
                }
                team.setUsers(teamUsers);
                team.setActiveFlag(teamVO.isActiveFlag());
                if (teamVO.getReasonActInactMap() != null) {
                    boolean flag = false;
                    if (teamVO.isActiveFlag() && teamVO.getReasonActInactMap() != null && teamVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(INACTIVE)) {
                        teamUploadDownload.setErrorMessages("Ambiguity in ActiveFlag and Action", dataValidationRuleResults, new String[]{"Action cannot be INACTIVE when ActiveFlag is True"});
                    }
                    if (!teamVO.isActiveFlag() && teamVO.getReasonActInactMap() != null && teamVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(ACTIVE)) {
                        teamUploadDownload.setErrorMessages("Ambiguity in ActiveFlag and Action", dataValidationRuleResults, new String[]{"Action cannot be INACTIVE when ActiveFlag is False"});
                    }
                    if (teamVO.getReasonActInactMap().getTypeOfAction() != null && teamVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(ACTIVE))
                        team.setActiveFlag(true);
                    if (teamVO.getReasonActInactMap() != null && teamVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(INACTIVE)) {
                        team.setActiveFlag(false);
                    }
                    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(teamVO.getReasonActInactMap().getMasterActiveInactiveReasons())) {
                        List<MasterActiveInactiveReasons> result = teamVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                                .filter(m -> ((m.getReasonInactive() != null && m.getReasonInactive().getCode() != null) || (m.getReasonActive() != null && m.getReasonActive().getCode() != null))).collect(Collectors.toList());
                        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(result)) {
                            teamUploadDownload.setErrorMessages("Active/Inactive Reason is not required for new record,It is required only for approved or approved modified record", dataValidationRuleResults, "Please do not provide active/inactive reasons");
                        }
                        if (org.apache.commons.collections.CollectionUtils.isEmpty(result)) {
                            List<MasterActiveInactiveReasons> resultDesc = teamVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                                    .filter(m -> (m.getDescription() != null)).collect(Collectors.toList());
                            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(resultDesc)) {
                                teamUploadDownload.setErrorMessages("Description for Active/Inactive Reason not Required", dataValidationRuleResults, "Please do not provide active/inactive reasons description");
                            }
                        }
                    }
                } // active inactive handling
                if (dataValidationRuleResults.isEmpty()) {
                    User user1 = getCurrentUser().getUserReference();
                    if (team.getId() == null && user1 != null) {
                        makerCheckerService.masterEntityChangedByUser(team, user1);
                    }
                }
            }
        }
    }

    public void editTeamOperation(TeamVO teamVO, List<ValidationRuleResult> dataValidationRuleResults) {

        Boolean actInactFlag = false;
        Boolean actionAmbiguityFlag = false;
        Boolean checkForReasons = false;
        Boolean checkForDuplicateReasons = false;
        Team recordtoupdate = null;
        if (StringUtils.isNotEmpty(teamVO.getName())) {
            recordtoupdate = findRecord(teamVO.getName());
        }
        if (recordtoupdate != null) {
            if (null != recordtoupdate.getReasonActInactMap() && CollectionUtils.isNotEmpty(recordtoupdate.getReasonActInactMap().getMasterActiveInactiveReasons())) {
                Hibernate.initialize(recordtoupdate.getReasonActInactMap());
                Hibernate.initialize(recordtoupdate.getReasonActInactMap().getMasterActiveInactiveReasons());
            }
            Hibernate.initialize(recordtoupdate.getUsers());
            entityDao.detach(recordtoupdate);

            String masterName = recordtoupdate.getClass().getSimpleName();
            String uniqueParameter = "name";
            String uniqueValue = teamVO.getName();
            actInactFlag = activeInactiveReasonService.checkForActiveInactiveForApprovedModified(teamVO.getReasonActInactMap(), masterName, uniqueParameter, uniqueValue);
            actionAmbiguityFlag = activeInactiveReasonService.checkForActionofReasons(teamVO.getReasonActInactMap());
            checkForReasons = activeInactiveReasonService.checkForGenericReasons(teamVO.getReasonActInactMap());
            checkForDuplicateReasons = activeInactiveReasonService.checkForDuplicateReasons(teamVO.getReasonActInactMap());
            if (teamVO.isActiveFlag() && teamVO.getReasonActInactMap() != null && teamVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(INACTIVE)) {
                teamUploadDownload.setErrorMessages("Ambiguity in ActiveFlag and Action", dataValidationRuleResults, new String[]{"Action cannot be INACTIVE when ActiveFlag is True"});
            }
            if (!teamVO.isActiveFlag() && teamVO.getReasonActInactMap() != null && teamVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase(ACTIVE)) {
                teamUploadDownload.setErrorMessages("No Reason Required", dataValidationRuleResults, "Please do not give Reason For this record");
            }

            if (teamVO.getReasonActInactMap() != null && !actionAmbiguityFlag) {
                teamUploadDownload.setErrorMessages("Reason is not provided for defined action", dataValidationRuleResults, ",Please provide reason for action:" + teamVO.getReasonActInactMap().getTypeOfAction());
            }
            if (teamVO.getReasonActInactMap() != null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(teamVO.getReasonActInactMap().getMasterActiveInactiveReasons()) && !actInactFlag) {
                teamUploadDownload.setErrorMessages("No Reason Required", dataValidationRuleResults, "Please do not give Reason For this action");
            }
            if (teamVO.getReasonActInactMap() != null && !checkForReasons) {
                teamUploadDownload.setErrorMessages("Reason Code not correct", dataValidationRuleResults, "Provide correct reasons");
            }
            if (!checkForDuplicateReasons) {
                teamUploadDownload.setErrorMessages("Duplicate Reasons not allowed", dataValidationRuleResults, "Provide correct reasons");
            }
            if (actInactFlag && actionAmbiguityFlag && checkForDuplicateReasons && checkForReasons) {
                saveReasonForApprovedRecord(recordtoupdate, teamVO, dataValidationRuleResults);
            }

            if (recordtoupdate.getApprovalStatus() == ApprovalStatus.UNAPPROVED_ADDED || recordtoupdate.getApprovalStatus() == ApprovalStatus.APPROVED || recordtoupdate.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
                List<String> userNameList = new ArrayList<>();
                teamUploadDownload.mandatoryCheckForTeamUpload(teamVO, dataValidationRuleResults, userNameList);
                chekForMaliciousString(teamVO.getName(), teamVO.getDescription(), dataValidationRuleResults);
                if (CollectionUtils.isEmpty(dataValidationRuleResults)) {
                    recordtoupdate.setDescription(teamVO.getDescription());
                    OrganizationBranch organizationBranch = null;
                    if (null != recordtoupdate.getAssociatedWithBP() && Team.ASSOCAITED_WITH_BP_Y.equalsIgnoreCase(recordtoupdate.getAssociatedWithBP())) {
                        if (!(null != recordtoupdate.getTeamBranch() && null != teamVO.getTeamBranch() &&
                                StringUtils.isNotEmpty(recordtoupdate.getTeamBranch().getBranchCode()) && StringUtils.isNotEmpty(teamVO.getTeamBranch().getBranchCode()) &&
                                recordtoupdate.getTeamBranch().getBranchCode().equalsIgnoreCase(teamVO.getTeamBranch().getBranchCode()))) {
                            teamUploadDownload.setErrorMessages("Branch of team associated with Business Partner can not be edited", dataValidationRuleResults, "TEAM BRANCH");
                        }else {
                            organizationBranch = recordtoupdate.getTeamBranch();
                        }
                    } else {
                        List<OrganizationBranch> orgBranchList = null;
                        if (teamVO.isRegionOfficeBranch()) {
                            orgBranchList = getOrgBranchesOfBranchType(OrganizationType.ORGANIZATION_TYPE_BRANCH_RO, teamVO.getTeamBranch().getBranchCode());
                        } else {
                            orgBranchList = getOrgBranchesOfBranchType(OrganizationType.ORGANIZATION_TYPE_BRANCH, teamVO.getTeamBranch().getBranchCode());
                        }
                        if (CollectionUtils.isNotEmpty(orgBranchList)) {
                            recordtoupdate.setTeamBranch(orgBranchList.get(0));
                            organizationBranch = orgBranchList.get(0);
                        } else {
                            teamUploadDownload.setErrorMessages("Team Branch is incorrect", dataValidationRuleResults, teamVO.getName());
                        }
                    }

                    if (null != organizationBranch){
                        List<UserInfo> userInfoList = teamService.getAllUsersPresentInTeamBranch(organizationBranch);
                        if (CollectionUtils.isNotEmpty(userInfoList)){
                            List<String> allowedUsernameList = userInfoList.stream().map(UserInfo::getUsername).collect(Collectors.toList());
                            teamUploadDownload.userbelongToThisTeam(allowedUsernameList,userNameList,dataValidationRuleResults);
                        }
                    }


                    User teamLead = userService.findUserByUsername(teamVO.getTeamLead().getUsername());
                    if (null != teamLead) {
                        recordtoupdate.setTeamLead(teamLead);
                    } else {
                        teamUploadDownload.setErrorMessages("No such team lead user found", dataValidationRuleResults);
                    }

                    Set<User> teamUsers = new LinkedHashSet<>();
                    for (User userVO : teamVO.getUsers()) {
                        if (null != userVO && null != userVO.getUsername()) {
                            User dbUser = userService.findUserByUsername(userVO.getUsername());
                            if (dbUser != null) {
                                teamUsers.add(dbUser);
                            } else {
                                teamUploadDownload.setErrorMessages("Username is incorrect ", dataValidationRuleResults, userVO.getUsername());
                            }
                        }
                    }

                    recordtoupdate.getUsers().removeAll(recordtoupdate.getApprovedUsers());
                    recordtoupdate.getUsers().addAll(teamUsers);
                    recordtoupdate.setRegionOfficeTeam(teamVO.isRegionOfficeTeam());
                    recordtoupdate.setActiveFlag(teamVO.isActiveFlag());
                }
            } else {
                teamUploadDownload.setErrorMessages("The record can not be edit. It is either Already Deleted or Already marked for Deletion.", dataValidationRuleResults, "Check the Team Name");
            }
        } else {
            teamUploadDownload.setErrorMessages("Either Empty or Invalid - ", dataValidationRuleResults, "Team Name");
        }
        if (dataValidationRuleResults.isEmpty()) {
            if (null != recordtoupdate.getAssociatedWithBP() && Team.ASSOCAITED_WITH_BP_Y.equalsIgnoreCase(recordtoupdate.getAssociatedWithBP())) {
                teamService.saveTeam(recordtoupdate);
            } else {
                User user1 = getCurrentUser().getUserReference();
                Hibernate.initialize(recordtoupdate.getUsers());
                if (recordtoupdate.getId() != null && user1 != null) {
                    entityDao.detach(recordtoupdate);
                    makerCheckerService.masterEntityChangedByUser(recordtoupdate, user1);
                }
            }
        }
    }

    public void deleteTeamOperation(TeamVO teamVO, List<ValidationRuleResult> dataValidationRuleResults) {

        if (StringUtils.isEmpty(teamVO.getName())) {
            teamUploadDownload.setErrorMessages("Team Name cannot be Left Blank", dataValidationRuleResults, "It is a Mandatory Field.");
        } else {
            Team deletedrecordDetails = findRecord(teamVO.getName());
            if (deletedrecordDetails != null) {
                if (null != deletedrecordDetails.getAssociatedWithBP() && Team.ASSOCAITED_WITH_BP_Y.equalsIgnoreCase(deletedrecordDetails.getAssociatedWithBP())) {
                    teamService.deleteTeam(deletedrecordDetails);
                } else {
                    if (deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.UNAPPROVED_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.DELETED_APPROVED_IN_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED_IN_PROGRESS) {
                        entityDao.detach(deletedrecordDetails);
                        User user = getCurrentUser().getUserReference();
                        EntityId updatedById = user.getEntityId();
                        makerCheckerService.masterEntityMarkedForDeletion(deletedrecordDetails, updatedById);
                    } else {
                        teamUploadDownload.setErrorMessages("Record Either Already Deleted or Already marked for Deletion.", dataValidationRuleResults, "Check the Team Name");
                    }
                }
            } else {
                teamUploadDownload.setErrorMessages("Record Does Not Exists.", dataValidationRuleResults, "Check the Team Name");
            }
        }
    }

    public void printError(TeamVO teamVO, List<ValidationRuleResult> dataValidationRuleResults) {
        if (!dataValidationRuleResults.isEmpty()) {
            List<Message> validationMessages = new ArrayList<Message>();
            for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                validationMessages.add(validationRuleResult.getI18message());
            }
            throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Team Upload", "Error in Team Upload").setMessages(validationMessages).build();
        }
    }

    public Team findRecord(String teamName) {

        if (StringUtils.isEmpty(teamName)) {
            return null;
        }
        NamedQueryExecutor<Team> executor = new NamedQueryExecutor<Team>("Team.findTeamByTeamName").addParameter("teamName", teamName);
        List<Team> teams = entityDao.executeQuery(executor);
        if (CollectionUtils.isNotEmpty(teams)) {
            return teams.get(0);
        }
        return null;
    }


}
