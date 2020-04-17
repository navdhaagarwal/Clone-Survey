/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.authenticationToken;

import com.nucleus.otp.OTPPolicy;
import com.nucleus.otp.VerificationStatus;
import com.nucleus.service.BaseService;
import com.nucleus.user.User;

public interface AuthenticationTokenService extends BaseService {

	/**
	 * Search Approved Token on the basic of time token id and populate the
	 * token detail VO
	 * 
	 * @param timeTokenID
	 * @return
	 */
	public TokenDetails getUserTaskAndStatusMapFromTokenId(String timeTokenID);

	/**
	 * Generate time Token for reset password request and update that token info
	 * in user object
	 * 
	 * @param lstUser
	 * @param tokenValidityTimeInMillis
	 * @return
	 */
	public String generatePasswordResetTokenForUser(User lstUser,
			String tokenValidityTimeInMillis);

	/**
	 * Generate approval link token and update into user instance
	 * 
	 * @param lstUser
	 * @param taskId
	 * @param tokenValidityTimeInMillis
	 * @return
	 */
	public String generateApproveLinkTokenForUserAndTask(User lstUser,
			String taskId, String tokenValidityTimeInMillis);

	/**
	 * Convert string token into encrypted token
	 * 
	 * @param token
	 * @return
	 */
	public String getEncryptedToken(String token);

	/**
	 * Generate time token for email Id validation and creat a entry in
	 * authentication table of the newaly generated token corresponsing the
	 * requested email Id
	 * 
	 * @param emailId
	 * @param tokenValidityTimeInMillis
	 * @return
	 */
	public String generateEmailAuthenticationToken(TokenDetails tokenDetails,
			String tokenValidityTimeInMillis);

	/**
	 * Getting token authentication on the basic of time token and return the
	 * updated Token Detail Vo.
	 * 
	 * @param timeTokenID
	 * @return
	 */
	public TokenDetails getEmailAuthenticationTokenByTokenId(String timeTokenID);

	/**
	 * Update the verified field of Email Info instance of the corresponding
	 * email Id
	 * 
	 * @param emailID
	 */
	public  void markEmailAsRejected(String emailID, Long emailDbId);
	
	/**
	 * Update the verified field of Email Info instance of the corresponding
	 * email Id
	 * 
	 * @param emailID
	 */
	public  void markEmailAsVerified(String emailID, Long emailDbId);

	/**
	 * Checks the validity of a token with a user
	 * 
	 * @param userId
	 * @param token
	 */
	public boolean isTokenValid(Long userId, String token);

	/**
	 * deletes the old token for this user
	 * 
	 * @param userId
	 * @param token
	 */
	public boolean deleteOldToken(Long userId, PasswordResetToken token);

	public String getRandomOTP(Long userId, int otpLength,
			String tokenValidityTimeInMillis);

	public TokenDetails getOTPTokenDetailsByTokenId(String tokenId);

	public String getEncryptedTokenMD5(String token);
	
	public String generatePasswordResetOTPForUser(User lstUser, String tokenValidityTimeInMillis, OTPPolicy otpPolicy);

	public boolean isPasswordResetOTPValid(Long userId, String otp,VerificationStatus verificationStatus);

   

}
