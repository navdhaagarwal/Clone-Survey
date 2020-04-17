package com.nucleus.core.team.vo;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.user.User;

import java.io.Serializable;
import java.util.Set;

public class TeamVO implements Serializable {

    public TeamVO() {
    }

    private static final long serialVersionUID = 1L;

    private String operationType;
    private String name;
    private OrganizationBranch teamBranch;
    private String description;
    private User teamLead;
    private boolean regionOfficeBranch;
    private boolean regionOfficeTeam;
    private Set<User> users;
    //private String email;
    //private String associatedWithBP;
    private boolean activeFlag = true;
    private ReasonsActiveInactiveMapping reasonActInactMap;

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    public OrganizationBranch getTeamBranch() {
        return teamBranch;
    }

    public void setTeamBranch(OrganizationBranch teamBranch) {
        this.teamBranch = teamBranch;
    }

    public User getTeamLead() {
        return teamLead;
    }

    public void setTeamLead(User teamLead) {
        this.teamLead = teamLead;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public boolean isRegionOfficeTeam() {
        return regionOfficeTeam;
    }

    public void setRegionOfficeTeam(boolean regionOfficeTeam) {
        this.regionOfficeTeam = regionOfficeTeam;
    }


    public boolean isRegionOfficeBranch() {
        return regionOfficeBranch;
    }

    public void setRegionOfficeBranch(boolean regionOfficeBranch) {
        this.regionOfficeBranch = regionOfficeBranch;
    }
}
