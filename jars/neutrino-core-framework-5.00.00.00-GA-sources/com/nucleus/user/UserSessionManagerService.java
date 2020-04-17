package com.nucleus.user;

import java.util.List;

import com.nucleus.finnone.pro.base.Message;
import com.nucleus.security.core.session.NeutrinoSessionInformation;

public interface UserSessionManagerService {

    /**
     * 
     * This method is meant for fetching all users which are currently logged in the application.Should be used for admin purpose only.
     * @return list of all principals
     */
    public <T> List<T> getAllLoggedInUsers();

    /**
     * 
     * This method is meant to make a user forcefully logout from his session.To be used by admin.
     * @param ID of user to remove from session
     * @return true if succeeds else false
     */
    public boolean invalidateUserSession(Long userId);

    /**
     * This method returns current logged in user.
     * @return
     */
    public UserInfo getLoggedinUserInfo();

    /**
     * This method invalidates the current logged in user session, if any
     * 
     */
    public void invalidateCurrentLoggedinUserSession();
    
    public void logOutAllUsers();

    void logOutAllUsers(String logoutEvent, Message message);
    
    void logOutAllUsers(String logoutEvent, Message message, Long logoutBy);
    
    /**
     * This method updates the session information of all the users logged into 
     * that module and their corresponding session information in other modules
     * and then logout all those users
     * 
     * @param logoutEvent
     * @param message
     * @param logoutBy
     * @param forceLogoutIP
     */
    void logOutAllUsers(String logoutEvent, Message message, Long logoutBy, String forceLogoutIP);

	void invalidateCurrentUserSession(String sessionId);
	int getCurrentWebUserSessionCount(String username);
	boolean invalidateUserSessionAndUpdateRegistry(Long userId, NeutrinoSessionInformation neutrinoSessionInformation);
	
	/**
	 * @param principal
	 * @return
	 */
	public List<String> getLoggedInModules(UserInfo principal);

	/**
	 * This method makes invalidates all the users sessions across web modules.  
	 * @param userId
	 * @param logOutBy
	 * @param forceLogOutIP
	 * @param logOutType
	 * @return true/false
	 */
	public boolean invalidateUserSessionAcrossModulesAndUpdateRegistry(Long userId, Long logOutBy, String forceLogOutIP, String logOutType);

	List<UserInfo> getAllLoggedInUsersAcrossModule();


	/**
	 *
	 * This method gets all the sessions of user across all modules even if user is not logged in current module and invalidates it.
	 * @param userId
	 * @param logOutBy
	 * @param forceLogOutIP
	 * @param logOutType
	 * @return true/false
	 */
	boolean invalidateUserSessionInAllModules(Long userId, Long logOutBy, String forceLogOutIP, String logOutType);
		
}
