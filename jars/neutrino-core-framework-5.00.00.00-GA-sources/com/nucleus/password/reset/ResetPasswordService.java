package com.nucleus.password.reset;

import java.util.List;

/**
 * @author shivendra.kumar
 *
 */
public interface ResetPasswordService {


	/**Resets user's old password on first login with new password and updates security questions and answers
	 * @param userFirstTimeLoginDetails
	 * @param username
	 * @param oldPassword
	 * @param newPassword
	 * @param passPhrase
	 * @param token
	 * @return
	 */
	String resetPasswordOnLogin(UserFirstTimeLoginDetails userFirstTimeLoginDetails,
			String oldPassword, String newPassword, String token);

	/**logs out the user from all logged in channels
	 * @param accessTokenValue
	 * @return
	 */
	Integer invalidateAccessToken(String accessTokenValue);

	String getPassPhrase(String token);

	/**Service to reset user's password
	 * @param isLicenseAccepted
	 * @param username
	 * @param oldPassword
	 * @param newPassword
	 * @param timeToken
	 * @return
	 */
	String resetUserPassword(Boolean isLicenseAccepted, String username, String oldPassword, String newPassword,String timeToken);

	/**Fetches user's scurity question list from DB
	 * @return
	 */
	List<Object> getSecurityQuestionsList();

}
