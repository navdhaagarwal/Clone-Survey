package com.nucleus.otp;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.cfi.sms.pojo.ShortMessageSendResponsePojo;
import com.nucleus.cfi.sms.pojo.SmsMessage;
import com.nucleus.cfi.sms.service.ShortMessageIntegrationService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.constants.CoreConstant;
import com.nucleus.finnone.pro.general.util.templatemerging.TemplateMergingUtility;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.mail.MimeMailMessageBuilder;
import com.nucleus.master.BaseMasterService;
import com.nucleus.otp.VerificationStatus.STATUS;
import com.nucleus.reason.BlockReason;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.userotpcount.entity.UserOTPCount;
import com.nucleus.userotpcount.service.UserOtpCountService;

/**
 * Created by gajendra.jatav on 4/17/2019.
 */
@Named("otpSenderService")
public class OTPSenderServiceImpl implements OTPSenderService {
	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;

    @Inject
    @Named("userService")
    private UserService userService;
    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore   userManagementServiceCore;
    @Inject
    @Named("authenticationTokenService")
    private AuthenticationTokenService authenticationTokenService;
    @Inject
    @Named("userOtpCountService")
    private UserOtpCountService userOtpCountService;
    
    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;
    
    @Inject
	@Named("configurationService")
	private ConfigurationService configurationService;
    @Inject
    @Named("mailService")
    private MailService mailService;

    @Inject
    @Named("mailMessageIntegrationService")
    private MailMessageIntegrationService mailMessageIntegrationService;

    @Inject
    @Named("shortMessageIntegrationService")
    ShortMessageIntegrationService shortMessageIntegrationService;

    @Inject
    @Named("templateMergingUtility")
    private TemplateMergingUtility templateMergingUtility;

    private static final String USERNAME = "userName";

    private static final String OTP = "otp";

    private static final String OTP_VALIDITY_MINUTES = "otp_validity_minites";

    private static final String OTP_VALIDITY_SECONDS = "otp_validity_seconds";

    private String emailTemplateLocation;
    private String emailTemplateLocationForLogin;
    private String smsTemplateLocation;
    private String smsTemplateLocationForLogin;
    @Value(value = "#{'${system.forgotPassword.mail.from}'}")
    private String forgotPasswordFromMail;

    private String smsFrom;

    private OTPPolicy otpPolicy;

    private String tokenValidityTimeInMillis;

    private String tokenValidityTimeInSeconds;

    private String tokenValidityTimeInMinutes;

    private String otpEmailSubject;

    private String otpLength;

    private String otpType;

    private OTPPolicy otpPolicyForLogin;
   
    private boolean otpRequiredOnSMSForLogin;
	
    private String otpEmailSubjectForLogin;
	private boolean otpRequiredOnEmail;

	private boolean otpRequiredOnEmailForLogin;
    private String otpLengthForLogin;

    private String otpTypeForLogin;

    public OTPPolicy getOtpPolicyForLogin() {
		return otpPolicyForLogin;
	}

	public void setOtpPolicyForLogin(OTPPolicy otpPolicyForLogin) {
		this.otpPolicyForLogin = otpPolicyForLogin;
	}

	public String getOtpLengthForLogin() {
		return otpLengthForLogin;
	}
    public void setUserService(UserService userService) {
		this.userService = userService;
	}
    public boolean isOtpRequiredOnSMSForLogin() {
		return otpRequiredOnSMSForLogin;
	}

	public void setOtpRequiredOnSMSForLogin(boolean otpRequiredOnSMSForLogin) {
		this.otpRequiredOnSMSForLogin = otpRequiredOnSMSForLogin;
	}

	public void setAuthenticationTokenService(AuthenticationTokenService authenticationTokenService) {
		this.authenticationTokenService = authenticationTokenService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public void setMailMessageIntegrationService(MailMessageIntegrationService mailMessageIntegrationService) {
		this.mailMessageIntegrationService = mailMessageIntegrationService;
	}

	public void setShortMessageIntegrationService(ShortMessageIntegrationService shortMessageIntegrationService) {
		this.shortMessageIntegrationService = shortMessageIntegrationService;
	}

	public void setTemplateMergingUtility(TemplateMergingUtility templateMergingUtility) {
		this.templateMergingUtility = templateMergingUtility;
	}

	public void setOtpPolicy(OTPPolicy otpPolicy) {
		this.otpPolicy = otpPolicy;
	}

	public void setTokenValidityTimeInSeconds(String tokenValidityTimeInSeconds) {
		this.tokenValidityTimeInSeconds = tokenValidityTimeInSeconds;
	}

	public void setTokenValidityTimeInMinutes(String tokenValidityTimeInMinutes) {
		this.tokenValidityTimeInMinutes = tokenValidityTimeInMinutes;
	}

	public void setOtpRequiredOnEmailForLogin(boolean otpRequiredOnEmailForLogin) {
		this.otpRequiredOnEmailForLogin = otpRequiredOnEmailForLogin;
	}
@PostConstruct
    public void init() {
        this.otpPolicy = OTPPolicy.builder().setOtpLength(Integer.valueOf(otpLength)).setOtpType(otpType).build();
        this.otpPolicyForLogin = OTPPolicy.builder().setOtpLength(Integer.valueOf(otpLengthForLogin)).setOtpType(otpTypeForLogin).build();
    }

    @Value(value = "#{'${otp.required.on.email}'}")
    public void setOtpRequiredOnEmail(String otpRequiredOnEmail) {
        if (StringUtils.isEmpty(otpRequiredOnEmail)
                || "${otp.required.on.email}".equalsIgnoreCase(otpRequiredOnEmail)) {
            this.otpRequiredOnEmail = false;
            return;
        }
        this.otpRequiredOnEmail = Boolean.valueOf(otpRequiredOnEmail);
    }


    @Value(value = "#{'${otp.policy.length}'}")
    public void setOtpLength(String otpLength) {
        if (StringUtils.isEmpty(otpLength)
                || "${otp.policy.length}".equalsIgnoreCase(otpLength)) {
            this.otpLength = "6";
            return;
        }
        this.otpLength = otpLength;
    }

    @Value(value = "#{'${otp.policy.type.for.login}'}")
    public void setOtpTypeForLogin(String otpType) {
        if (StringUtils.isEmpty(otpType)
                || "${otp.policy.type.for.login}".equalsIgnoreCase(otpType)) {
            this.otpTypeForLogin = "numeric";
            return;
        }
        this.otpTypeForLogin = otpType;
    }
    @Value(value = "#{'${otp.required.on.email.for.login}'}")
    public void setOtpRequiredOnEmailForLogin(String otpRequiredOnEmail) {
        if (StringUtils.isEmpty(otpRequiredOnEmail)
                || "${otp.required.on.email.for.login}".equalsIgnoreCase(otpRequiredOnEmail)) {
            this.otpRequiredOnEmailForLogin = false;
            return;
        }
        this.otpRequiredOnEmailForLogin = Boolean.valueOf(otpRequiredOnEmail);
    }
    @Value("${otp.email.subject.for.login}")
    public void setOtpEmailSubjectForLogin(String otpEmailSubjectForlogin) {
        if (StringUtils.isEmpty(otpEmailSubjectForlogin)
                || "${otp.email.subject.for.login}".equalsIgnoreCase(otpEmailSubjectForlogin)) {
            this.otpEmailSubjectForLogin = "One-Time Password for Login Verification";
            return;
        }
        this.otpEmailSubjectForLogin = otpEmailSubjectForlogin;
    }
    @Value(value = "#{'${otp.required.on.sms.for.login}'}")
    public void setOtpRequiredOnSMSForLogin(String otpRequiredOnSMS) {
        if (StringUtils.isEmpty(otpRequiredOnSMS)
                || "${otp.required.on.sms.for.login}".equalsIgnoreCase(otpRequiredOnSMS)) {
            this.otpRequiredOnSMSForLogin = false;
            return;
        }
        this.otpRequiredOnSMSForLogin = Boolean.valueOf(otpRequiredOnSMS);
    }

    @Value(value = "#{'${otp.policy.length.for.login}'}")
    public void setOtpLengthForLogin(String otpLength) {
        if (StringUtils.isEmpty(otpLength)
                || "${otp.policy.length.for.login}".equalsIgnoreCase(otpLength)) {
            this.otpLengthForLogin = "6";
            return;
        }
        this.otpLengthForLogin = otpLength;
    }

    @Value(value = "#{'${otp.policy.type}'}")
    public void setOtpType(String otpType) {
        if (StringUtils.isEmpty(otpType)
                || "${otp.policy.type}".equalsIgnoreCase(otpType)) {
            this.otpType = "numeric";
            return;
        }
        this.otpType = otpType;
    }

    @Override
    public String getOtpType() {
        return otpType;
    }

    @Value("${otp.sms.from}")
    public void setSmsFrom(String smsFrom) {
        if (StringUtils.isEmpty(smsFrom)
                || "${otp.sms.from}".equalsIgnoreCase(smsFrom)) {
            this.smsFrom = "Neutrino";
            return;
        }
        this.smsFrom = smsFrom;
    }

    @Value("${otp.email.subject}")
    public void setOtpEmailSubject(String otpEmailSubject) {
        if (StringUtils.isEmpty(otpEmailSubject)
                || "${otp.email.subject}".equalsIgnoreCase(otpEmailSubject)) {
            this.otpEmailSubject = "One Time Password";
            return;
        }
        this.otpEmailSubject = otpEmailSubject;
    }

    @Value("${otp.validity.time.seconds}")
    public void setTokenValidityTimeInMillis(String tokenValidityTimeInSeconds) {
        if (StringUtils.isEmpty(tokenValidityTimeInSeconds)
                || "${otp.validity.time.seconds}".equalsIgnoreCase(tokenValidityTimeInSeconds)) {
            this.tokenValidityTimeInSeconds = "900";
        } else {
            this.tokenValidityTimeInSeconds = tokenValidityTimeInSeconds;
        }
        this.tokenValidityTimeInMillis = "" + (Integer.valueOf(this.tokenValidityTimeInSeconds) * 1000);
        this.tokenValidityTimeInMinutes = "" + ((Integer) Integer.valueOf(this.tokenValidityTimeInSeconds) / 60);
    }


    public String getForgotPasswordFromMail() {
        return forgotPasswordFromMail;
    }

    public void setForgotPasswordFromMail(String forgotPasswordFromMail) {
        BaseLoggers.flowLogger.info("Setting forgotPasswordFromMail to {}", forgotPasswordFromMail);
        this.forgotPasswordFromMail = forgotPasswordFromMail;
    }

    @Value(value = "#{'${otp.email.template}'}")
    public void setEmailTemplateLocation(String emailTemplate) {
        if (StringUtils.isEmpty(emailTemplate)
                || "${otp.email.template}".equalsIgnoreCase(emailTemplate)) {
            this.emailTemplateLocation = "passwordReset/templates/email.vm";
            return;
        }
        this.emailTemplateLocation = emailTemplate;
    }

    @Value(value = "#{'${otp.email.template.for.login}'}")
    public void setEmailTemplateLocationForLogin(String emailTemplate) {
        if (StringUtils.isEmpty(emailTemplate)
                || "${otp.email.template.for.login}".equalsIgnoreCase(emailTemplate)) {
            this.emailTemplateLocationForLogin = "passwordReset/templates/emailForLogin.vm";
            return;
        }
        this.emailTemplateLocationForLogin = emailTemplate;
    }
    
    @Value(value = "#{'${otp.sms.template}'}")
    public void setSmsTemplateLocation(String smsTemplateLocation) {
        if (StringUtils.isEmpty(smsTemplateLocation)
                || "${otp.sms.template}".equalsIgnoreCase(smsTemplateLocation)) {
            this.smsTemplateLocation = "passwordReset/templates/sms.vm";
            return;
        }
        this.smsTemplateLocation = smsTemplateLocation;
    }
   
   
    @Value(value = "#{'${otp.sms.template.for.login}'}")
    public void setSmsTemplateLocationForLogin(String smsTemplateLocation) {
        if (StringUtils.isEmpty(smsTemplateLocation)
                || "${otp.sms.template.for.login}".equalsIgnoreCase(smsTemplateLocation)) {
            this.smsTemplateLocationForLogin = "passwordReset/templates/smsForLogin.vm";
            return;
        }
        this.smsTemplateLocationForLogin = smsTemplateLocation;
    }
    @Override
    @Transactional
    public OTPSendStatus sendOtp(String userName) {
        User user = userService.findUserByUsername(userName);
        OTPSendStatus otpSendStatus = new OTPSendStatus();
        if (!userService.isUserValidForPasswordReset(user)) {
            BaseLoggers.flowLogger.error("Valid User not found with userName while sending otp");
            otpSendStatus.setStatus(OTPSendStatus.STATUS.USER_NOT_EXISTS);
            return otpSendStatus;
        }
        
        if(!"db".equals(user.getSourceSystem())){
        	BaseLoggers.flowLogger.error("Reset Password for Ldap user not allowed");
        	otpSendStatus.setStatus(OTPSendStatus.STATUS.ERROR_LDAP_USER);
        	return otpSendStatus;
        }
        String initCapUname= updateUserNameInInitCaps(user);
        String otp = generateOtp(user,false);
        if (otp == null) {
            otpSendStatus.setStatus(OTPSendStatus.STATUS.ERROR);
            BaseLoggers.flowLogger.error("Could not generate OTP");
            return otpSendStatus;
        }
        if (sendSms(user, otp, otpSendStatus,false)) {
            if (otpRequiredOnEmail) {
                sendEmail(user,initCapUname, otp, otpEmailSubject, otpSendStatus,false);
                otpSendStatus.setStatus(OTPSendStatus.STATUS.SENT_ON_BOTH);
                return otpSendStatus;
            }
            otpSendStatus.setStatus(OTPSendStatus.STATUS.SENT_ON_MOBILE);
        }
        return otpSendStatus;
    }
    @Override
    @Transactional
	public OTPSendStatus sendOtpForLogin(String userName) {
    	 OTPSendStatus otpSendStatus = new OTPSendStatus();
    	if(!isValidMaxOtpSentCounts(userName))
    	{
    		  otpSendStatus.setStatus(OTPSendStatus.STATUS.MAX_SENDING_LIMIT_EXHAUSTED);
    		  otpSendStatus.setErrorDescription(CoreConstant.LOCKED_REASON_DESCRIPTION+genericParameterService.findByCode("max_OTP_send_attempts", BlockReason.class).getDescription());
              BaseLoggers.flowLogger.error("Max OTP sending limit is exhausted");
              return otpSendStatus;
    	}
        User user = userService.findUserByUsername(userName);
        String otp = generateOtp(user,true);
        if (otp == null) {
            otpSendStatus.setStatus(OTPSendStatus.STATUS.ERROR);
            BaseLoggers.flowLogger.error("Could not generate OTP");
            return otpSendStatus;
        }
        String initCapUname= updateUserNameInInitCaps(user);
		boolean sentOnMobile = false;
		boolean sentOnMail = false;
		if (otpRequiredOnSMSForLogin) {
			sentOnMobile = sendSms(user, otp, otpSendStatus, true);
		}
		if (otpRequiredOnEmailForLogin) {
			sentOnMail = sendEmail(user, initCapUname,otp, otpEmailSubjectForLogin, otpSendStatus, true);
		}
		if (sentOnMobile && sentOnMail) {
			otpSendStatus.setStatus(OTPSendStatus.STATUS.SENT_ON_BOTH);
		} else if (sentOnMobile) {
			otpSendStatus.setStatus(OTPSendStatus.STATUS.SENT_ON_MOBILE);
		} else if (sentOnMail) {
			otpSendStatus.setStatus(OTPSendStatus.STATUS.SENT_ON_EMAIL);
		}
		if(sentOnMobile||sentOnMail)
		{
			userOtpCountService.incrementOtpSentCount(userName);
		}
		return otpSendStatus;
	}

	private boolean isValidMaxOtpSentCounts(String userName) {
		UserOTPCount userOtpTrials = userOtpCountService.getUserOtpCountByUsername(userName);
		if (userOtpTrials == null) {
			return true;
		}
		int maxAllowedOTPcount = 15;
		int otpSendCount = userOtpTrials.getNumberOfOTPSendAttempts();
		ConfigurationVO configuredVO = configurationService.getConfigurationPropertyFor(
				SystemEntity.getSystemEntityId(), "config.max.user.otp.count.before.login");

		if (configuredVO != null) {
			maxAllowedOTPcount = Integer.valueOf(configuredVO.getPropertyValue());
		}

		if (maxAllowedOTPcount > otpSendCount) {
			return true;
		} else {
			User user = userService.findUserByUsername(userName);
			userManagementServiceCore.blockUser(user.getId().toString(), baseMasterService
					.getMasterEntityById(User.class, userService.getUserFromUsername("system").getId()).getEntityId());
			userService.markReasonForSystem(user, BlockReason.MAX_OTP_SEND_ATTEMPTS);
			return false;
		}
	}

	private String getSmsBody(String username,String otp,boolean isOTPForLogin) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(USERNAME, username);
        dataMap.put(OTP, otp);
        dataMap.put(OTP_VALIDITY_MINUTES, tokenValidityTimeInMinutes);
        dataMap.put(OTP_VALIDITY_SECONDS, tokenValidityTimeInSeconds);
        if(isOTPForLogin)
        {
        	return templateMergingUtility.mergeTemplateIntoString(smsTemplateLocationForLogin, dataMap);
        }
        return templateMergingUtility.mergeTemplateIntoString(smsTemplateLocation, dataMap);
    }

    private boolean sendSms(User user, String otp, OTPSendStatus otpSendStatus,boolean isOTPForLogin) {
        boolean otpSent = false;
        OTPSendStatus.STATUS status = OTPSendStatus.STATUS.ERROR;
        try {
            PhoneNumber mobileNumber = userService.getUserMobileNumber(user.getId());
            if (mobileNumber != null && mobileNumber.getIsdCode() != null && mobileNumber.getPhoneNumber() != null) {
                SmsMessage smsMessage = new SmsMessage();
                String userContactNumber = mobileNumber.getIsdCode() + mobileNumber.getPhoneNumber();
                smsMessage.setTo(userContactNumber);
                otpSendStatus.setMaskedMobile(getMaskedMobileNumber(mobileNumber.getPhoneNumber()));
                smsMessage.setBody(getSmsBody(user.getUsername(),otp,isOTPForLogin));
                smsMessage.setFrom(smsFrom);
               ShortMessageSendResponsePojo responsePojo = shortMessageIntegrationService.sendShortMessage(smsMessage);
                otpSent = true;
              
             if (responsePojo == null || responsePojo.getDeliveryStatus().equals("FAILED")
                        || responsePojo.getDeliveryStatus().equals("FAILED_TO_SEND")) {
                    status = OTPSendStatus.STATUS.ERROR_IN_SENDING_SMS;
                    otpSent = false;
                }
            } else {
                status = OTPSendStatus.STATUS.MOBILE_NOT_FOUND;
            }
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception occurred while sending otp on mobile:", e.getMessage());
            status = OTPSendStatus.STATUS.ERROR_IN_SENDING_SMS;
        }
        otpSendStatus.setStatus(status);
        return otpSent;
    }

    private String generateOtp(User user,boolean isOtpForLogin) {
        return isOtpForLogin?authenticationTokenService.generatePasswordResetOTPForUser(user, tokenValidityTimeInMillis, otpPolicyForLogin):authenticationTokenService.generatePasswordResetOTPForUser(user, tokenValidityTimeInMillis, otpPolicy);
    }

    private boolean sendEmail(User user,String initCapUname, String otp, String subject, OTPSendStatus otpSendStatus,boolean isOTPForLogin) {
        try {
            String mailId = user.getMailId();
            if (StringUtils.isEmpty(mailId)) {
                BaseLoggers.flowLogger.error("Email not found while sending otp for user with userName {}", initCapUname);
                return false;
            }
            if (!mailId.contains("@")) {
                BaseLoggers.flowLogger.debug("Invalid email found while sending OTP on email {}", mailId);
                return false;
            }
            otpSendStatus.setMaskedEmail(getMaskedEmail(mailId));
            MimeMailMessageBuilder mimeMailMessageBuilder = mailService.createMimeMailBuilder();
            mimeMailMessageBuilder.setFrom(getForgotPasswordFromMail()).setTo(mailId).setSubject(subject)
                    .setHtmlBody(getEmailBody(initCapUname, otp,isOTPForLogin));

            mailMessageIntegrationService
                    .sendMailMessageToIntegrationAsynchronously(mimeMailMessageBuilder.getMimeMessage());
            return true;
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception occurred while sending otp on email:", e);
            return false;
        }
    }

    private String getMaskedEmail(String mailId) {
        String[] parts = mailId.split("@");
        return Utility.maskString(parts[0], 1, parts[0].length() - 1, "*") + '@' + parts[1];
    }

    private String getMaskedMobileNumber(String phoneNumber) {
        return Utility.maskString(phoneNumber, 2, phoneNumber.length() - 2, "x");
    }

    private String getEmailBody(String username, String otp,boolean isOTPForLogin) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(USERNAME, username);
        dataMap.put(OTP, otp);
        dataMap.put(OTP_VALIDITY_MINUTES, tokenValidityTimeInMinutes);
        dataMap.put(OTP_VALIDITY_SECONDS, tokenValidityTimeInSeconds);
        if(isOTPForLogin)
        {
        	 return templateMergingUtility.mergeTemplateIntoString(emailTemplateLocationForLogin, dataMap);
        }
    	  
    	  
        return templateMergingUtility.mergeTemplateIntoString(emailTemplateLocation, dataMap);
    }

	@Override
	@Transactional
	public VerificationStatus verifyOTPandgeneratePasswordResetToken(String userName, String otp, Boolean isOTPForLogin) {
		VerificationStatus verificationStatus = new VerificationStatus();
		User user = userService.findUserByUsername(userName);
		if (authenticationTokenService.isPasswordResetOTPValid(user.getId(), otp, verificationStatus)) {
			verificationStatus.setStatus(VerificationStatus.STATUS.VALID);

			if (isOTPForLogin) {
				userOtpCountService.resetUserOtpCount(userName);
			} else {
				verificationStatus.setPasswordResetToken(
						authenticationTokenService.generatePasswordResetTokenForUser(user, tokenValidityTimeInMillis));
			}
		} else {
			if (isOTPForLogin) {
				userOtpCountService.incrementOtpFailedCount(userName, verificationStatus);
			} else {
				verificationStatus.setStatus(STATUS.INVALID);
			}
		}
		
		return verificationStatus;
	}
	private String updateUserNameInInitCaps(User user) {
		String uname=user.getUsername();
		if(uname!=null && uname.length()>0)
		{
			return  uname.substring(0, 1).toUpperCase() + uname.substring(1);

		
		
		}
		return uname;
		
	}
}
