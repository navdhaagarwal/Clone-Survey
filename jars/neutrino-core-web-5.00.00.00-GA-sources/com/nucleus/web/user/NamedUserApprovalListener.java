package com.nucleus.web.user;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.businessmapping.service.UserManagementServiceCore;
import org.springframework.context.MessageSource;

import com.nucleus.core.role.entity.Role;
import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEventListener;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.license.cache.LicenseClientCacheService;
import com.nucleus.license.content.model.LicenseDetail;
import com.nucleus.license.pojo.LicenseMobilityModuleInfo;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.User;
import com.nucleus.user.UserService;

@Named
public class NamedUserApprovalListener extends GenericEventListener {
	@Inject
	@Named("userService")
	private UserService userService;

	@Inject
	@Named("licenseClientCacheService")
	private   LicenseClientCacheService licenseClientCacheService;
	
	@Inject
	@Named("messageSource")
	protected MessageSource messageSource;
	@Inject
	@Named("entityDao")
	protected EntityDao entityDao;
	@Inject
	@Named("userManagementServiceCore")
	private UserManagementServiceCore userManagementServiceCore;

	@Override
	public boolean canHandleEvent(Event event) {
		// should not check for for MAKER_CHECKER_UPDATED_APPROVED events
		// because we only want to map super admins to a newly created
		// organization branch.
		if (event instanceof MakerCheckerEvent && (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED||event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED)) {
			MakerCheckerEvent makerCheckerEvent = (MakerCheckerEvent) event;
			EntityId entityId = makerCheckerEvent.getOwnerEntityId();
			if (entityId.getEntityClass() != null && entityId.getEntityClass().equals(User.class)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void handleEvent(Event event) {
		MakerCheckerEvent makerCheckerEvent = (MakerCheckerEvent) event;
		EntityId userEntityId = makerCheckerEvent.getOwnerEntityId();
		User user = entityDao.get(userEntityId);

		if (user.isLoginEnabled()) {
			isMoreLoginUserNameAllowed(user);
		}
		if(event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED) {
			userManagementServiceCore.notifyNewUser("", user);
		}
	}

	private void isMoreLoginUserNameAllowed(User user)

	{

		List<Role> userRoles = user.getUserRoles();
		List<Long> userRoleIDs = new ArrayList<>();
		if (userRoles == null || userRoles.isEmpty()) {
			return;
		}
		for (Role userRole : userRoles) {
			userRoleIDs.add(userRole.getId());
		}
		List<String> productAssociatedWithUser = userService.getProductListFromRoleIds(userRoleIDs);
		

		Map<String, LicenseDetail> productCodeAndlicDetailMap =licenseClientCacheService.getAll();

		if (productCodeAndlicDetailMap == null) {
			return;
		}
		String userUUID=user.getEntityLifeCycleData().getUuid();
		for (String product : productAssociatedWithUser) {
			for (Map.Entry<String, LicenseDetail> entry : productCodeAndlicDetailMap.entrySet()) {
				
				validateUserAllowed(product,entry,userUUID);
				
					
				
				
				
			}

		}

	}

	private void validateUserAllowed(String product, Entry<String, LicenseDetail> entry,String userUUID) {
		
		Integer maximumNumberOfUsersAllowed ;
		if (product.equals(entry.getKey())) {
			 maximumNumberOfUsersAllowed = entry.getValue().getMaxNamedUsers();

			   iscreationAllowed(maximumNumberOfUsersAllowed,entry.getKey(),userUUID);
					
					

			
		}
		List<LicenseMobilityModuleInfo> mobilityInfoList = entry.getValue().getLicenseMobilityModuleInfoList();
		if (notNull(entry.getValue().getLicenseMobilityModuleInfoList())) {

			for (LicenseMobilityModuleInfo moblityInfo : mobilityInfoList) {
				if (product.equals(moblityInfo.getMobilityModuleCode())) {
					 maximumNumberOfUsersAllowed = moblityInfo.getNamedUserCount();

					iscreationAllowed(maximumNumberOfUsersAllowed, product, userUUID);
							
					}
				}
			}

		
	}

	private void iscreationAllowed(Integer maximumNumberOfUsersAllowed, String productCode, String uuid) {
		boolean creationAllowed= (maximumNumberOfUsersAllowed != -1
				&& maximumNumberOfUsersAllowed > userService.getUsersCountByProductName(productCode, uuid))
				|| maximumNumberOfUsersAllowed == -1;
		if (!creationAllowed) {
		Message message = new Message("label.license.named.user.exceeds", MessageType.ERROR,
				String.valueOf(maximumNumberOfUsersAllowed), productCode);

		throw ExceptionBuilder.getInstance(BusinessException.class).setMessage(message)
				.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();

	}
	
}
	}
