package com.nucleus.authenticationToken;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.businessmapping.service.UserManagementDao;
import com.nucleus.contact.EMailInfo;
import com.nucleus.contact.EmailVerificationType;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.misc.util.PasswordEncryptorUtil;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.otp.OTPGenerator;
import com.nucleus.otp.OTPPolicy;
import com.nucleus.otp.PasswordResetOTPToken;
import com.nucleus.otp.VerificationStatus;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserService;

import net.bull.javamelody.MonitoredWithSpring;

@Named(value = "authenticationTokenService")
@MonitoredWithSpring(name = "AuthenticationToken_Service_IMPL_")
public class AuthenticationTokenServiceImpl extends BaseServiceImpl implements AuthenticationTokenService {

	private Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");

	
    @Inject
    @Named("userManagementDaoCore")
    private UserManagementDao userManagementDao;

    @Inject
    @Named("userService")
    protected UserService     userService;
    
    @Inject
    @Named("otpGenerator")
    private OTPGenerator otpGenerator;

    @Inject
    @Named("neutrinoPasswordEncoder")
    private PasswordEncoder neutrinoPasswordEncoder;

    private static String hashKey = "tokenSalt";

    @Override
    public TokenDetails getUserTaskAndStatusMapFromTokenId(String timeTokenID) {
        TokenDetails tokenDetails = new TokenDetails();

        Object[] userIdAndTokenId = userService.findApproveLinkTokenByTokenId(timeTokenID);

        tokenDetails.setStatus(AuthenticationTokenConstants.INVALID_TOKEN);

        if (userIdAndTokenId != null) {

            Long userId = null;
            ApprovalLinkToken approvalLinkToken = null;
            String taskId = null;
            if (userIdAndTokenId[0] != null) {
                userId = (Long) userIdAndTokenId[0];
            }
            if (userIdAndTokenId[1] != null) {
                approvalLinkToken = (ApprovalLinkToken) userIdAndTokenId[1];
                taskId = approvalLinkToken.getTaskId();
            }
            if (approvalLinkToken != null) {
                BaseLoggers.flowLogger.debug("User Token :" + approvalLinkToken.getTokenId());
                long approvalLinkTokenTime = approvalLinkToken.getTokenValidity().getMillis();
                if (System.currentTimeMillis() > approvalLinkTokenTime) {
                    tokenDetails.setStatus(AuthenticationTokenConstants.PAGE_EXPIRED);
                    tokenDetails.setTaskId(taskId);

                } else {
                    tokenDetails.setStatus(AuthenticationTokenConstants.VALID_TOKEN);
                    tokenDetails.setTaskId(taskId);
                    tokenDetails.setUserId(userId);
                }
            }
        }
        return tokenDetails;

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generatePasswordResetOTPForUser(User user, String tokenValidityTimeInMillis, OTPPolicy otpPolicy) {

        PasswordResetOTPToken passwordResetOTP = getPasswordResetToken(user.getId());
        if (passwordResetOTP != null) {
            passwordResetOTP.setUsed(Boolean.FALSE);
        }else{
            passwordResetOTP = new PasswordResetOTPToken();
            passwordResetOTP.setUsed(Boolean.FALSE);
        }

        String otp = otpGenerator.generateOTP(otpPolicy);
        DateTime val = DateUtils.getCurrentUTCTime();
        val = val.plusMillis(Integer.valueOf(tokenValidityTimeInMillis));
        passwordResetOTP.setTokenValidity(val);
        passwordResetOTP.setTokenId(getEncryptedTokenWithBcrypt(otp));
        passwordResetOTP.setUserId(user.getId());
        entityDao.saveOrUpdate(passwordResetOTP);

        return otp;
    }

    
	@Override
	public boolean isPasswordResetOTPValid(Long userId, String otp, VerificationStatus verificationStatus) {
		User user = userManagementDao.find(User.class, userId);
		PasswordResetOTPToken passwordResetOTPToken = getPasswordResetToken(user.getId());
		if (user != null && StringUtils.isNotBlank(otp)) {
			if (isTokenValid(passwordResetOTPToken, otp, verificationStatus)
					&& (passwordResetOTPToken.getUsed() == null || passwordResetOTPToken.getUsed() == Boolean.FALSE)) {
				passwordResetOTPToken.setUsed(Boolean.TRUE);
				entityDao.saveOrUpdate(passwordResetOTPToken);
				return true;
			}
		}
		return false;
	}

	private boolean isTokenValid(AuthenticationToken existingToken, String tokenToMatch,
			VerificationStatus verificationStatus) {
		if (existingToken == null || tokenToMatch == null) {
			return false;
		}
		boolean isNotExpired = existingToken.getTokenValidity().isAfter(DateTime.now());
		if (!isNotExpired &&verificationStatus !=null) {
			verificationStatus.setStatus(VerificationStatus.STATUS.OTP_EXPIRED);
		}
		return matches(tokenToMatch, existingToken.getTokenId()) && isNotExpired;
	}
    
    private boolean matches(String rawString, String encodedString) {
    	if (encodedString == null || encodedString.length() == 0) {
			return false;
		} else if (!encodedString.contains("{bcrypt}")) {
			try {
				return PasswordEncryptorUtil.encryptPassword(rawString, hashKey).equals(encodedString);
			} catch (NoSuchAlgorithmException e) {
				throw new SystemException(e);
			}
		} else {
			return neutrinoPasswordEncoder.matches(rawString, encodedString);
		}
    }

    private PasswordResetOTPToken getPasswordResetToken(Long id) {

        NamedQueryExecutor<PasswordResetOTPToken> executor = new NamedQueryExecutor<PasswordResetOTPToken>("UserOTPVerification.getPasswordResetOTPByUserId")
                .addParameter("userId", id);
        PasswordResetOTPToken passwordResetOTPToken = entityDao.executeQueryForSingleValue(executor);
        return passwordResetOTPToken;
    }

    public String generatePasswordResetTokenForUser(User lstUser, String tokenValidityTimeInMillis, OTPPolicy otpPolicy){

            String timeToken = otpGenerator.generateOTP(otpPolicy);
            PasswordResetToken passwordResetAuthenticationToken = new PasswordResetToken();
            DateTime val = DateUtils.getCurrentUTCTime();
            val = val.plusMillis(Integer.valueOf(tokenValidityTimeInMillis));
            passwordResetAuthenticationToken.setTokenValidity(val);
            passwordResetAuthenticationToken.setTokenId(getEncryptedToken(timeToken));
            PasswordResetToken passwordResetToken = lstUser.getPasswordResetToken();
            lstUser.setPasswordResetToken(passwordResetAuthenticationToken);
            entityDao.saveOrUpdate(passwordResetAuthenticationToken);
            userService.updateUser(lstUser);
            if (passwordResetToken != null) {
                entityDao.delete(passwordResetToken);
            }
            flushCurrentTransaction();
            return timeToken;
    }

    @Override
    public String generatePasswordResetTokenForUser(User lstUser, String tokenValidityTimeInMillis) {
        return generatePasswordResetTokenForUser(lstUser,tokenValidityTimeInMillis,null);
    }



    @Override
    public String generateApproveLinkTokenForUserAndTask(User lstUser, String taskId, String tokenValidityTimeInMillis) {

        String timeToken = otpGenerator.generateOTP();

        ApprovalLinkToken approvalLinkToken = new ApprovalLinkToken();
        DateTime validity = DateUtils.getCurrentUTCTime();
        validity = validity.plusMillis(Integer.valueOf(tokenValidityTimeInMillis));

        approvalLinkToken.setTokenValidity(validity);
        approvalLinkToken.setTokenId(getEncryptedToken(timeToken));
        approvalLinkToken.setTaskId(taskId);
        lstUser.addApprovalLinkToken(approvalLinkToken);
        userService.updateUser(lstUser);
        return timeToken;
    }

    @Override
    public String getEncryptedToken(String token) {
        try {
            return PasswordEncryptorUtil.encryptPassword(token, hashKey);
        } catch (NoSuchAlgorithmException e) {
            throw new SystemException(e);
        }
    }

    @Override
    public String getEncryptedTokenMD5(String token) {
        try {
            return PasswordEncryptorUtil.encryptPasswordMD5(token, hashKey);
        } catch (NoSuchAlgorithmException e) {
            throw new SystemException(e);
        }
    }
    
    public String getEncryptedTokenWithBcrypt(String token) {
        return neutrinoPasswordEncoder.encode(token);
}

    @Override
    public String generateEmailAuthenticationToken(TokenDetails tokenDetails, String tokenValidityTimeInMillis) {
        String timeToken = otpGenerator.generateOTP();
        EmailAuthenticationToken emailvalidationAuthenticationToken = new EmailAuthenticationToken();
        DateTime val = DateUtils.getCurrentUTCTime();
        val = val.plusMillis(Integer.valueOf(tokenValidityTimeInMillis));
        // val.add(Calendar.MILLISECOND,
        // Integer.valueOf(tokenValidityTimeInMillis));
        emailvalidationAuthenticationToken.setTokenValidity(val);
        emailvalidationAuthenticationToken.setTokenId(getEncryptedToken(timeToken));
        emailvalidationAuthenticationToken.setEmailId(tokenDetails.getEmailId());
        emailvalidationAuthenticationToken.setTaskId(tokenDetails.getTaskId());
        emailvalidationAuthenticationToken.setUserId(tokenDetails.getUserId());
        emailvalidationAuthenticationToken.setEmailUId(tokenDetails.getEmailUId());

        // populateEmailAuthTokenFromTokenDetails(emailvalidationAuthenticationToken,
        // tokenDetails);
        entityDao.persist(emailvalidationAuthenticationToken);
        return timeToken;
    }

    @Override
    public TokenDetails getEmailAuthenticationTokenByTokenId(String timeTokenID) {
        TokenDetails tokenDetails = new TokenDetails();
        tokenDetails.setStatus(AuthenticationTokenConstants.INVALID_TOKEN);
        /*
         * MapQueryExecutor executor = new
         * MapQueryExecutor(EmailAuthenticationToken.class,
         * "e").addColumn("e.tokenValidity",
         * "tokenValidity").addColumn("e.emailId", "emailId"); StringBuilder
         * whereClause = new StringBuilder();
         * whereClause.append(" e.tokenId = '" + getEncryptedToken(timeTokenID)
         * + "'"); executor.addAndClause(whereClause.toString());
         */

        NamedQueryExecutor<EmailAuthenticationToken> executor = new NamedQueryExecutor<EmailAuthenticationToken>(
                "UserEMailVerification.getEmailAuthenticationDetails").addParameter("tokenId",
                getEncryptedToken(timeTokenID));
        EmailAuthenticationToken emailAuthenticationToken = entityDao.executeQueryForSingleValue(executor);
        if (emailAuthenticationToken != null) {
            if ((emailAuthenticationToken.getTokenValidity()).isBeforeNow()) {
                tokenDetails.setStatus(AuthenticationTokenConstants.PAGE_EXPIRED);
            } else {
                tokenDetails.setStatus(AuthenticationTokenConstants.VALID_TOKEN);
                tokenDetails.setEmailId(emailAuthenticationToken.getEmailId());
                tokenDetails.setUserId(emailAuthenticationToken.getUserId());
                tokenDetails.setTaskId(emailAuthenticationToken.getTaskId());
                tokenDetails.setEmailUId(emailAuthenticationToken.getEmailUId());
            }
        }else
        {

            NamedQueryExecutor<EmailAuthenticationToken> executorMD5 = new NamedQueryExecutor<EmailAuthenticationToken>(
                    "UserEMailVerification.getEmailAuthenticationDetails").addParameter("tokenId",
                    getEncryptedTokenMD5(timeTokenID));
            EmailAuthenticationToken emailAuthenticationTokenMD5 = entityDao.executeQueryForSingleValue(executorMD5);
            if (emailAuthenticationTokenMD5 != null) {
                if ((emailAuthenticationTokenMD5.getTokenValidity()).isBeforeNow()) {
                    tokenDetails.setStatus(AuthenticationTokenConstants.PAGE_EXPIRED);
                } else {
                    tokenDetails.setStatus(AuthenticationTokenConstants.VALID_TOKEN);
                    tokenDetails.setEmailId(emailAuthenticationTokenMD5.getEmailId());
                    tokenDetails.setUserId(emailAuthenticationTokenMD5.getUserId());
                    tokenDetails.setTaskId(emailAuthenticationTokenMD5.getTaskId());
                    tokenDetails.setEmailUId(emailAuthenticationTokenMD5.getEmailUId());
                }
            }
        }
        return tokenDetails;
    }

    @Override
    public void markEmailAsRejected(String emailID, Long emailDbId) {
        NamedQueryExecutor<EMailInfo> executor = null;

        // Fetch email by id if id is there, otherwise fetch email by emailAddress
        if (emailDbId != null) {
            executor = new NamedQueryExecutor<EMailInfo>("UserEMailVerification.getEmailInfoByEmailId").addParameter(
                    "emailId", emailDbId);
        } else {
            executor = new NamedQueryExecutor<EMailInfo>("UserEMailVerification.getEmailInfoByEmailAddress").addParameter(
                    "emailAddress", emailID);
        }

        List<EMailInfo> result = entityDao.executeQuery(executor);
        if (result != null && result.size() > 0) {
            EMailInfo targetEmail = result.get(0);
            targetEmail.setVerificationType(EmailVerificationType.EMAIL_NOT_VERIFIED);
            targetEmail.setVerificationTimeStamp(DateUtils.getCurrentUTCTime());
            entityDao.update(targetEmail);
        }
    }
    
    @Override
    public void markEmailAsVerified(String emailID, Long emailDbId) {
        NamedQueryExecutor<EMailInfo> executor = null;

        // Fetch email by id if id is there, otherwise fetch email by emailAddress
        if (emailDbId != null) {
            executor = new NamedQueryExecutor<EMailInfo>("UserEMailVerification.getEmailInfoByEmailId").addParameter(
                    "emailId", emailDbId);
        } else {
            executor = new NamedQueryExecutor<EMailInfo>("UserEMailVerification.getEmailInfoByEmailAddress").addParameter(
                    "emailAddress", emailID);
        }

        List<EMailInfo> result = entityDao.executeQuery(executor);
        if (result != null && result.size() > 0) {
            EMailInfo targetEmail = result.get(0);
            targetEmail.setVerificationType(EmailVerificationType.EMAIL_VERIFIED);
            targetEmail.setVerificationTimeStamp(DateUtils.getCurrentUTCTime());
            entityDao.update(targetEmail);
        }
    }

    @Override
    public boolean isTokenValid(Long userId, String token) {
        User user = userManagementDao.find(User.class, userId);
        if (user != null && StringUtils.isNotBlank(token)) {
            if (isTokenValid(user.getPasswordResetToken(), token,null)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteOldToken(Long userId, PasswordResetToken token) {
        if (token == null) {
            return false;
        }
        TokenDetails tokenDetails = getUserTaskAndStatusMapFromTokenId(token.getTokenId());
        if (tokenDetails.getUserId() == null) {
            entityDao.delete(token);
            return true;
        }
        return false;
    }

    /* This function will change later */
    @Override
    public String getRandomOTP(Long userId, int otpLength, String tokenValidityTimeInMillis) {
        NeutrinoValidator.notNull(userId, "User Id cannot be null for OTP token");

        String otp = otpGenerator.generateOTP();
        if (otp.length() < otpLength) {
            otpLength = otp.length() - 1;
        }

        // This will be changed when proper way of generating OTP is implemented
        otp = otp.substring(0, otpLength);

        OTPToken otpToken = new OTPToken();
        DateTime val = DateUtils.getCurrentUTCTime();
        val = val.plusMillis(Integer.valueOf(tokenValidityTimeInMillis));
        otpToken.setTokenValidity(val);
        otpToken.setTokenId(getEncryptedToken(otp));
        otpToken.setUserId(userId);
        entityDao.persist(otpToken);
        return otp;
    }

    @Override
    public TokenDetails getOTPTokenDetailsByTokenId(String tokenId) {
        TokenDetails tokenDetails = new TokenDetails();
        tokenDetails.setStatus(AuthenticationTokenConstants.INVALID_TOKEN);

        NamedQueryExecutor<OTPToken> executor = new NamedQueryExecutor<OTPToken>("UserOTPVerification.getOTPTokenDetails")
                .addParameter("tokenId", getEncryptedToken(tokenId));
        OTPToken otpToken = entityDao.executeQueryForSingleValue(executor);
        if (otpToken != null) {
            if ((otpToken.getTokenValidity()).isBeforeNow()) {
                tokenDetails.setStatus(AuthenticationTokenConstants.PAGE_EXPIRED);
            } else {
                tokenDetails.setStatus(AuthenticationTokenConstants.VALID_TOKEN);
                tokenDetails.setUserId(otpToken.getUserId());
            }
        }
        else{
            NamedQueryExecutor<OTPToken> executorMD5 = new NamedQueryExecutor<OTPToken>("UserOTPVerification.getOTPTokenDetails")
                    .addParameter("tokenId", getEncryptedTokenMD5(tokenId));
            OTPToken otpTokenMD5 = entityDao.executeQueryForSingleValue(executorMD5);
            if (otpTokenMD5 != null) {
                if ((otpTokenMD5.getTokenValidity()).isBeforeNow()) {
                    tokenDetails.setStatus(AuthenticationTokenConstants.PAGE_EXPIRED);
                } else {
                    tokenDetails.setStatus(AuthenticationTokenConstants.VALID_TOKEN);
                    tokenDetails.setUserId(otpTokenMD5.getUserId());
                }
            }
        }
        return tokenDetails;
    }
}
