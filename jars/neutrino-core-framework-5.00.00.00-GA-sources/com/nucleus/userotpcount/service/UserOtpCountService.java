package com.nucleus.userotpcount.service;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.constants.CoreConstant;
import com.nucleus.master.BaseMasterService;
import com.nucleus.otp.VerificationStatus;
import com.nucleus.otp.VerificationStatus.STATUS;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.reason.BlockReason;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.userotpcount.entity.UserOTPCount;

@Named(value = "userOtpCountService")
public class UserOtpCountService extends BaseServiceImpl {
	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;
	@Inject
	@Named("userManagementServiceCore")
	private UserManagementServiceCore userManagementServiceCore;
	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;
	
	@Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;
	@Inject
	@Named(value = "userService")
	private UserService userService;
	private static final String USER_NAME_NOT_NULL_ERR="username cannot be null";
	public UserOTPCount getUserOtpCountByUsername(String userName) {
		NeutrinoValidator.notNull(userName, USER_NAME_NOT_NULL_ERR);
		return this.entityDao
				.executeQueryForSingleValue(new NamedQueryExecutor<UserOTPCount>("getUserOtpCountByUsername")
						.addParameter("userName", userName.toLowerCase()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE));

	}

	private UserOTPCount saveOrUpdate(UserOTPCount userOtpTrials) {
		return this.entityDao.saveOrUpdate(userOtpTrials);

	}

	public void incrementOtpSentCount(String userName) {
		NeutrinoValidator.notNull(userName, USER_NAME_NOT_NULL_ERR);
		UserOTPCount userOtpCount = getUserOtpCountByUsername(userName);
		if (userOtpCount == null) {
			userOtpCount = new UserOTPCount();
			userOtpCount.setUserName(userName.toLowerCase());
		}
		userOtpCount.setNumberOfOTPSendAttempts(userOtpCount.getNumberOfOTPSendAttempts() + 1);
		saveOrUpdate(userOtpCount);
	}
     @Transactional(propagation = Propagation.REQUIRED)
	public void resetUserOtpCount(String userName) {
    		NeutrinoValidator.notNull(userName, USER_NAME_NOT_NULL_ERR);
		UserOTPCount userOTPCount = getUserOtpCountByUsername(userName);
		if(userOTPCount!=null) {
			userOTPCount.setNumberOfFailedOTPAttempts(0);
			userOTPCount.setNumberOfOTPSendAttempts(0);
			saveOrUpdate(userOTPCount);
		}
	}

	public void incrementOtpFailedCount(String userName, VerificationStatus verificationStatus) {
		NeutrinoValidator.notNull(userName, USER_NAME_NOT_NULL_ERR);
		UserOTPCount userOTPCount = getUserOtpCountByUsername(userName);
		int maxAllowedFailedOTPcount = 5;
		if (userOTPCount == null) {
			
			userOTPCount = new UserOTPCount();
			userOTPCount.setUserName(userName.toLowerCase());
			
			userOTPCount.setNumberOfFailedOTPAttempts(1);
			entityDao.saveOrUpdate(userOTPCount);
			if(verificationStatus.getStatus()==null)
			{
			verificationStatus.setStatus(STATUS.INVALID);
			}
			return ;
		}
	
			int otpfailedAttemts = userOTPCount.getNumberOfFailedOTPAttempts();
			ConfigurationVO configuredVO = configurationService.getConfigurationPropertyFor(
					SystemEntity.getSystemEntityId(), "config.user.allowed.failedOTPAttempts");

			if (configuredVO != null) {
				maxAllowedFailedOTPcount = Integer.valueOf(configuredVO.getPropertyValue());
			}

			if (maxAllowedFailedOTPcount > otpfailedAttemts) {
				userOTPCount.setNumberOfFailedOTPAttempts(userOTPCount.getNumberOfFailedOTPAttempts() + 1);
				entityDao.update(userOTPCount);
				if(verificationStatus.getStatus()==null)
				{
				verificationStatus.setStatus(STATUS.INVALID);
				}
			} else {
				User user = userService.findUserByUsername(userName);
				if(!user.isAccountLocked())
				{
				userManagementServiceCore.blockUser(user.getId().toString(),
						baseMasterService
								.getMasterEntityById(User.class, userService.getUserFromUsername("system").getId())
								.getEntityId());
				userService.markReasonForSystem(user, BlockReason.FAILED_OTP_ATTEMTS);
				verificationStatus.setErrorDescription(CoreConstant.LOCKED_REASON_DESCRIPTION + genericParameterService.findByCode(BlockReason.FAILED_OTP_ATTEMTS, BlockReason.class).getDescription());
				
				}
				verificationStatus.setStatus(STATUS.MAX_TIME_INVALID);

			}

		
	}

}
