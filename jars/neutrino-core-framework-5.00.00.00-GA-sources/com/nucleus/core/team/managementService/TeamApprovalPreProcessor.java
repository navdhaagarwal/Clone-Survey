package com.nucleus.core.team.managementService;

import com.nucleus.core.team.entity.Team;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.process.beans.EntityApprovalPreProcessor;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("teamApprovalPreProcessor")
public class TeamApprovalPreProcessor implements EntityApprovalPreProcessor {


    @Inject
    @Named("userService")
    private UserService userService;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    @Override
    public void handleApprovalForModification(BaseMasterEntity originalRecord, BaseMasterEntity toBeDeletedRecord, BaseMasterEntity toBeHistoryRecord, Long reviewerId) {
        String master = toBeDeletedRecord.getClass().getName();
        String teamName = Team.class.getName();
        if (master.equalsIgnoreCase(teamName)) {

            Team team = (Team) toBeDeletedRecord;
            if (null != team) {
                if (team.getTeamLead() != null && team.getTeamLead().getId() != null) {
                    String userName = userService.getUserNameByUserId(team.getTeamLead().getId());
                    List<User> userList = userService.getUserReferenceByUsername(userName, Boolean.FALSE);
                    if (CollectionUtils.isNotEmpty(userList)) {
                        for (User user : userList) {
                            if (null != user) {
                                user.setTeamLead(true);
                                entityDao.update(user);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleApprovalForNew(BaseMasterEntity originalRecord, BaseMasterEntity toBeDeletedRecord, BaseMasterEntity toBeHistoryRecord, Long reviewerId) {

        String master = toBeDeletedRecord.getClass().getName();
        String teamName = Team.class.getName();
        if (master.equalsIgnoreCase(teamName)) {

            Team team = (Team) toBeDeletedRecord;
            if (null != team) {
                if (team.getTeamLead() != null && team.getTeamLead().getId() != null) {
                    String userName = userService.getUserNameByUserId(team.getTeamLead().getId());
                    List<User> userList = userService.getUserReferenceByUsername(userName, Boolean.FALSE);
                    if (CollectionUtils.isNotEmpty(userList)) {
                        for (User user : userList) {
                            if(null != user) {
                                user.setTeamLead(true);
                                entityDao.update(user);
                            }
                        }
                    }
                }
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


}
