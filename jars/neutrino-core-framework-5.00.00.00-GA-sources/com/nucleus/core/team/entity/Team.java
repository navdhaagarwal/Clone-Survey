/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.team.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.user.User;

/**
 * Team is a group of employees who form a team. A team may be formed of sales representatives, 
 * a team may be formed of escalation members, team is therefore more closely related to business 
 * whereas group is more related to administrators even though team names and group names may be same.
 * 
 * More information is available at following JIRA link:
 *  <a href="http://jira.nucleussoftware.com:8080/browse/CAS-531">CAS-531</a>
 * 
 * @author Nucleus Software Exports Limited
 * 
 */
@Entity
@DynamicInsert
@DynamicUpdate
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@DeletionPreValidator
@Synonym(grant="ALL")
@Table(indexes={@Index(name="RAIM_PERF_45_4410",columnList="REASON_ACT_INACT_MAP"),
        @Index(name="team_name_index",columnList="name")})
public class Team extends BaseMasterEntity {

    private static final long  serialVersionUID     = -3833525874840394711L;

    private String             name;

    private String             description;

    private String             email;

    @ManyToMany(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<User>          users;

    @ManyToOne(fetch = FetchType.LAZY)
    private User               teamLead;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrganizationBranch teamBranch;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    private boolean            regionOfficeTeam;

    /*Describe if this team is having same name as that of its associated BP , possible values are Y/N*/
    private String             associatedWithBP;

    public static final String ASSOCAITED_WITH_BP_Y = "Y";
    public static final String ASSOCAITED_WITH_BP_N = "N";

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the users.
     *
     * @return the users
     */
    public Set<User> getUsers() {
        return users;
    }

    /**
     * Sets the users.
     *
     * @param users the users to set
     */
    public void setUsers(Set<User> users) {
        this.users = users;
    }

    /**
     * Gets the team lead.
     *
     * @return the teamLead
     */
    public User getTeamLead() {
        return teamLead;
    }

    /**
     * Sets the team lead.
     *
     * @param teamLead the teamLead to set
     */
    public void setTeamLead(User teamLead) {
        this.teamLead = teamLead;
    }

    public OrganizationBranch getTeamBranch() {
        return teamBranch;
    }

    public void setTeamBranch(OrganizationBranch teamBranch) {
        this.teamBranch = teamBranch;
    }

    /**
     * @return the regionOfficeTeam
     */
    public boolean getRegionOfficeTeam() {
        return regionOfficeTeam;
    }

    /**
     * @param regionOfficeTeam the regionOfficeTeam to set
     */
    public void setRegionOfficeTeam(boolean regionOfficeTeam) {
        this.regionOfficeTeam = regionOfficeTeam;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Team team = (Team) baseEntity;
        super.populate(team, cloneOptions);
        team.setDescription(description);
        team.setEmail(email);
        team.setName(name);
        team.setTeamBranch(teamBranch);
        team.setTeamLead(teamLead);
        Set<User> clonedSetForUsers = new HashSet<User>(users);
        team.setUsers(clonedSetForUsers);
        team.setRegionOfficeTeam(regionOfficeTeam);
        if (reasonActInactMap != null) {
            team.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Team team= (Team) baseEntity;
        super.populateFrom(team, cloneOptions);
        this.setName(team.getName());
        this.setDescription(team.getDescription());
        this.setEmail(team.getEmail());
        this.setTeamBranch(team.getTeamBranch());
        this.setTeamLead(team.getTeamLead());
        this.setRegionOfficeTeam(team.getRegionOfficeTeam());
        this.setUsers(team.getUsers());
        if (team.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) team.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("Team Name:" + name);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Description:" + description);
        stf.append(SystemPropertyUtils.getNewline());
        if (teamBranch != null) {
            stf.append("BranchID:" + teamBranch.getId());

        }
        log = stf.toString();

        return log;
    }

    public String getAssociatedWithBP() {
        return associatedWithBP;
    }

    public void setAssociatedWithBP(String associatedWithBP) {
        this.associatedWithBP = associatedWithBP;
    }
    
    /**
     * Gets the Approved users.
     *
     * @return the users
     */
    public Set<User> getApprovedUsers() {
    	Set<User> approvedUsers = new HashSet<User>();
    	if(users!=null){
	    	for (User user : users) {
	    		if(user.getMasterLifeCycleData().getApprovalStatus().equals(ApprovalStatus.APPROVED) 
	        			|| user.getMasterLifeCycleData().getApprovalStatus().equals(ApprovalStatus.APPROVED_MODIFIED)){
	    			approvedUsers.add(user);
	    		}
	    	}
    	}
    	
    	return approvedUsers;
    }
    
    @Override
    public String getDisplayName() {
        return name;
    }

}
