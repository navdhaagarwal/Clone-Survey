package com.nucleus.event;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;

/**
 * The Class Team Event.
 *
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class TeamEvent extends GenericEvent {

	 private static final long serialVersionUID = -1240898845338429793L;
	 private static final String TEAM_NAME = "TEAM_NAME";
	 private static final String ADDED_USERS = "ADDED_USERS";
	 private static final String REMOVED_USRES = "REMOVED_USERS";
	 private static final String ASSOCIATED_USER = "ASSOCIATED_USER";
	 private static final String TEAM_LEADER = "TEAM_LEADER";
	
	 /**
	     * Sets the TEAM_LEADER.
	     *
	     * @param teamLeader the new ASSOCIATED_USER
	     */
	    public void setTeamLeader(String teamLeader) {
	        addPersistentProperty(TEAM_LEADER, teamLeader);
	    }

	    /**
	     * Gets the TEAM_LEADER.
	     *
	     * @return the TEAM_LEADER
	     */
	    public String getTeamLeader() {
	        return getPersistentProperty(TEAM_LEADER);
	    }
	 
	 /**
     * Sets the ASSOCIATED_USER.
     *
     * @param associatedUsers the new ASSOCIATED_USER
     */
    public void setAssociatedUser(String associatedUser) {
        addPersistentProperty(ASSOCIATED_USER, associatedUser);
    }

    /**
     * Gets the ASSOCIATED_USER.
     *
     * @return the ASSOCIATED_USER
     */
    public String getAssociatedUser() {
        return getPersistentProperty(ASSOCIATED_USER);
    }
    
    /**
     * Sets the REMOVED_USRES.
     *
     * @param removedUsers the new REMOVED_USRES
     */
    public void setRemovedUsers(String removedUsers) {
        addPersistentProperty(REMOVED_USRES, removedUsers);
    }

    /**
     * Gets the REMOVED_USRES.
     *
     * @return the REMOVED_USRES
     */
    public String getRemovedUsers() {
        return getPersistentProperty(REMOVED_USRES);
    }
    
    
    /**
     * Sets the ADDED_USERS.
     *
     * @param addedUsers the new ADDED_USERS
     */
    public void setAddedUsers(String addedUsers) {
        addPersistentProperty(ADDED_USERS, addedUsers);
    }

    /**
     * Gets the ADDED_USERS.
     *
     * @return the ADDED_USERS
     */
    public String getAddedUsers() {
        return getPersistentProperty(ADDED_USERS);
    }
    
    
    /**
     * Sets the TEAM_NAME.
     *
     * @param teamName the new TEAM_NAME
     */
    public void setTeamName(String teamName) {
        addPersistentProperty(TEAM_NAME, teamName);
    }

    /**
     * Gets the TEAM_NAME.
     *
     * @return the TEAM_NAME
     */
    public String getTeamName() {
        return getPersistentProperty(TEAM_NAME);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Instantiates a new team event.
     */
    protected  TeamEvent() {
        super();
    }

    /**
     * Instantiates a new team event.
     *
     * @param eventType the event type
     */
    protected TeamEvent(int eventType) {
        super(eventType);
    }

    /**
     * Instantiates a new team event.
     *
     * @param <T> the generic type
     * @param eventType the event type
     * @param successFlag true for 'success' and false for 'failure'
     * @param userEntityId the user entity id
     * @param entity the entity
     * @param associatedEntityUris the associated entity uris
     */
    public <T extends BaseEntity> TeamEvent(int eventType, boolean successFlag, EntityId userEntityId, T entity) {
        super(eventType);

        addPersistentProperty(SUCCESS_FLAG, successFlag ? "success" : "failure");
        setAssociatedUserUri(userEntityId.getUri());
        setStandardEventPropertiesUsingEntity(entity);

    }
    
    

}
