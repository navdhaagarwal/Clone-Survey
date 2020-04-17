package com.nucleus.team.teamUploadDownloadService;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.core.team.vo.TeamVO;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.base.validation.domainobject.ValidationRuleResult;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component("teamUploadDownloadUtility")
public class TeamUploadDownloadUtility extends BaseServiceImpl {

    @Inject
    @Named("teamService")
    private TeamService teamService;

    private static final String MANDATORY_FIELD_MSG = "It is a Mandatory Field.";

    public void createHeaderForTeam(Row header) {

        int index = 0;
        header.createCell(index++).setCellValue("IDENTIFIER");
        header.createCell(index++).setCellValue("Team Name");
        header.createCell(index++).setCellValue("Team Branch");
        header.createCell(index++).setCellValue("Team Description");
        header.createCell(index++).setCellValue("Team Lead");
        header.createCell(index++).setCellValue("Region Office Branch");
        header.createCell(index++).setCellValue("Region Office Team");
        header.createCell(index++).setCellValue("Status");
        header.createCell(index++).setCellValue("Users in Team");
        header.createCell(index++).setCellValue("Active Flag");
        header.createCell(index++).setCellValue("ACTION");
        header.createCell(index++).setCellValue("ACTIVE REASON");
        header.createCell(index++).setCellValue("Description");
        header.createCell(index++).setCellValue("INACTIVE REASON");
    }


    public void createDataForTeam(List<Long> masterIds, Sheet sheet) throws Exception {
        List<Team> teamList = new ArrayList<>();
        boolean regionOfficeBranch = false;

        if (CollectionUtils.isNotEmpty(masterIds)) {
            teamList = teamService.getTeamsByTeamIds(masterIds);
        } else {
            teamList = teamService.getAllTeams();
        }
        if (CollectionUtils.isNotEmpty(teamList)) {
            int rowIndex = 1;
            for (Team team : teamList) {
                if (null != team) {
                    Row row = sheet.createRow(rowIndex++);

                    row.createCell(0).setCellValue("D");
                    if (StringUtils.isNotEmpty(team.getName())) {
                        row.createCell(1).setCellValue(team.getName());
                    }
                    if (team.getTeamBranch() != null && StringUtils.isNotEmpty(team.getTeamBranch().getBranchCode())) {
                        row.createCell(2).setCellValue(team.getTeamBranch().getBranchCode());
                        if (null != team.getTeamBranch().getOrganizationType() && OrganizationType.ORGANIZATION_TYPE_BRANCH_RO.equalsIgnoreCase(team.getTeamBranch().getOrganizationType().getCode())) {
                            regionOfficeBranch = true;
                        }
                    }
                    if (StringUtils.isNotEmpty(team.getDescription())) {
                        row.createCell(3).setCellValue(team.getDescription());
                    }
                    if (team.getTeamLead() != null && StringUtils.isNotEmpty(team.getTeamLead().getUsername())) {
                        row.createCell(4).setCellValue(team.getTeamLead().getUsername());
                    }

                    row.createCell(5).setCellValue(regionOfficeBranch);
                    row.createCell(6).setCellValue(team.getRegionOfficeTeam());

                    row.createCell(9).setCellValue(team.isActiveFlag());
                    if (null != team.getReasonActInactMap() && StringUtils.isNotEmpty(team.getReasonActInactMap().getTypeOfAction())) {
                        row.createCell(10).setCellValue(team.getReasonActInactMap().getTypeOfAction());
                    }
                    if (CollectionUtils.isNotEmpty(team.getApprovedUsers())) {
                        for (User user : team.getApprovedUsers()) {
                            row = sheet.createRow(rowIndex++);
                            row.createCell(0).setCellValue("C1");
                            row.createCell(8).setCellValue(user.getUsername());
                        }
                    }

                    if (null != team.getReasonActInactMap() && CollectionUtils.isNotEmpty(team.getReasonActInactMap().getMasterActiveInactiveReasons())) {
                        for (MasterActiveInactiveReasons reason : team.getReasonActInactMap().getMasterActiveInactiveReasons()) {
                            if (null != reason) {
                                row = sheet.createRow(rowIndex++);
                                row.createCell(0).setCellValue("C2");
                                if (null != reason.getReasonActive()) {
                                    row.createCell(11).setCellValue(reason.getReasonActive().getCode());
                                }
                                row.createCell(12).setCellValue(reason.getDescription());
                                if (null != reason.getReasonInactive()) {
                                    row.createCell(13).setCellValue(reason.getReasonInactive().getCode());
                                }
                            }
                        }
                    }
                }
            }// end of Team List
        }

    }

    public void setErrorMessages(String msg, List<ValidationRuleResult> dataValidationRuleResults, String... arguments) {
        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage(msg, Message.MessageType.ERROR, arguments)));
    }

    public void mandatoryCheckForTeamUpload(TeamVO teamVO, List<ValidationRuleResult> dataValidationRuleResults, List<String> userNameList) {
        if (null != teamVO) {

            if (null == teamVO.getName()) {
                setErrorMessages("Team Name cannot be Left Blank", dataValidationRuleResults, MANDATORY_FIELD_MSG);
            }
            if (null == teamVO.getDescription()) {
                setErrorMessages("Team Description cannot be Left Blank", dataValidationRuleResults, MANDATORY_FIELD_MSG);
            }
            if (null == teamVO.getTeamLead() || (null != teamVO.getTeamLead() && null == teamVO.getTeamLead().getUsername())) {
                setErrorMessages("Team Lead cannot be Left Blank", dataValidationRuleResults, MANDATORY_FIELD_MSG);
            }
            if (null == teamVO.getTeamBranch()) {
                setErrorMessages("Team Branch cannot be Left Blank", dataValidationRuleResults, MANDATORY_FIELD_MSG);
            }
            if (CollectionUtils.isEmpty(teamVO.getUsers())) {
                setErrorMessages("Atleast one user needed to map team lead", dataValidationRuleResults);
            } else if (null != teamVO.getTeamLead() && null != teamVO.getTeamLead().getUsername()) {
                List<String> distinctUserNames = teamVO.getUsers().stream()
                        .filter(distinctByKey(p -> p.getUsername())).map(User::getUsername).collect(Collectors.toList());
                userNameList.addAll(distinctUserNames);
                if (distinctUserNames.size() < teamVO.getUsers().size()) {
                    setErrorMessages("Duplicate users in team are not allowed", dataValidationRuleResults);
                }
                if (null != distinctUserNames && !(distinctUserNames.contains(teamVO.getTeamLead().getUsername()))) {
                    setErrorMessages("Team Lead should be from users in team", dataValidationRuleResults, teamVO.getTeamLead().getUsername());
                }
            }

        }
    }

    public static <T>
    Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public boolean userbelongToThisTeam(List<String> allowedUsernameList, List<String> userNameList, List<ValidationRuleResult> dataValidationRuleResults){
        boolean userbelongToThisTeam = true;
        if (CollectionUtils.isNotEmpty(userNameList)) {
            for (String username : userNameList) {
                if (! (allowedUsernameList.contains(username))){
                    setErrorMessages("User does not belong to this team branch",dataValidationRuleResults, username);
                    userbelongToThisTeam = false;
                }
            }
        }
        return userbelongToThisTeam;
    }


}
