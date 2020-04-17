package com.nucleus.security.masking;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.role.entity.Role;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.NeutrinoSessionRegistry;
import com.nucleus.security.masking.dao.MaskingPolicyDao;
import com.nucleus.security.masking.entities.MaskingDefinition;
import com.nucleus.security.masking.entities.MaskingPolicy;
import com.nucleus.security.masking.entities.TagType;
import com.nucleus.security.masking.types.MaskingType;
import com.nucleus.security.masking.types.MaskingTypeFactory;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;
import com.nucleus.user.UserInfo;

@Transactional
@Named("maskingUtility")
public class MaskingUtility {

	@Inject
	@Named("maskingPolicyDAO")
	private MaskingPolicyDao maskingPolicyDao;

	@Inject
	@Named("sessionRegistry")
	private NeutrinoSessionRegistry sessionRegistry;

	@Inject
	@Named("neutrinoExecutionContextHolder")
	protected INeutrinoExecutionContextHolder neutrinoExecutionContextHolder;

	@Inject
	@Named("maskingTypeFactory")
	private MaskingTypeFactory maskingTypeFactory;

	public String getMaskedValue(String maskingPolicyCode, String inputValue) {
		return getMaskedValue(maskingPolicyCode, inputValue, TagType.TAG_TYPE_NO_TAG);
	}

	public String getMaskedValue(String maskingPolicyCode, String inputValue, String tagName) {

		if (StringUtils.isBlank(inputValue)) {
			return inputValue;
		} else if (StringUtils.isBlank(maskingPolicyCode)) {
			throwIllegalArgumentsException("maskingPolicyCode");
		} else if (StringUtils.isBlank(tagName)) {
			// If no tag name is supplied, we treat default tag name as no_Tag.
			// The same is used whenever creating a Masking policy without
			// selecting any tag
			tagName = TagType.TAG_TYPE_NO_TAG;
		}

		MaskingPolicy maskingPolicy = getMaskingPolicy(maskingPolicyCode);
		if (maskingPolicy == null) {
			throwIllegalArgumentsException("maskingPolicy");
		}

		if (!tagName.equals(maskingPolicy.getTagNameToBeMasked().getCode())) {
			throw new IllegalArgumentException("Supplied tag name " + tagName
					+ "doesn't match the masking policy tag name" + maskingPolicy.getTagNameToBeMasked().getCode());
		}

		List<MaskingDefinition> maskingDefinitions = maskingPolicy.getApprovedMaskingDefinitions();

		if (CollectionUtils.isEmpty(maskingDefinitions)) {
			throw new IllegalStateException("Selected masking policy " + maskingPolicyCode
					+ "doesn't have any approved masking definitions. Please check.");
		}

		UserInfo userInfo = neutrinoExecutionContextHolder.getLoggedInUserDetails();
		Set<Long> currentUserRoleIds = userInfo.getUserRoleIds();

		Set<Role> maskingPolicyRoles = maskingPolicy.getUserRoles();
		Set<Long> maskingPolicyRoleIds = maskingPolicyRoles.stream().map(Role::getId).collect(Collectors.toSet());
		// TODO: Explore L2 cache

		boolean hasRolesToSeeUnmaskedValue = CollectionUtils.containsAny(currentUserRoleIds, maskingPolicyRoleIds);
		String maskedValue = inputValue;
		if (!hasRolesToSeeUnmaskedValue) {
			maskedValue = maskValueBasedOnMaskingDefinitions(maskingDefinitions, inputValue, tagName);
		} else {
			BaseLoggers.flowLogger.debug(
					"Current user has atleast one role which has access to see unmasked data for masking policy: ",
					maskingPolicyCode);
		}

		return maskedValue;
	}

	
	private String maskValueBasedOnMaskingDefinitions(List<MaskingDefinition> maskingDefinitions, String inputValue,
			String tagName) {
		String maskedValue = inputValue;
		if (TagType.TAG_TYPE_INPUT.equals(tagName) || TagType.TAG_TYPE_NO_TAG.equals(tagName)) {
			MaskingDefinition maskingDefinition = maskingDefinitions.get(0);// check
	
			MaskingType maskingType = maskingTypeFactory.getMaskingType(maskingDefinition.getType().getCode());

			if (!maskingDefinition.isEnabled()) {
				BaseLoggers.flowLogger
						.error("MaskingDefintion is disabled. Unmasked value will be returned. Masking Defintion id: "
								+ maskingDefinition.getId());
				return inputValue;
			}
			if (maskingType == null) {
				BaseLoggers.flowLogger.error("MaskingType: " + maskingDefinition.getType().getCode() + " is wrong.");
				return inputValue;
			}

			maskedValue = maskingType.getMaskedValue(inputValue, maskingDefinition);
		} else {
			throw new IllegalArgumentException(
					"Supplied tag name" + tagName + "doesn't match any of the possible tag name values");
		}
		return maskedValue;
	}

	private void throwIllegalArgumentsException(String parameterName) {
		throw new IllegalArgumentException("Required parameter" + parameterName + "is null");

	}

	
	private MaskingPolicy getMaskingPolicy(String maskingPolicyCode) {
		return maskingPolicyDao.getMaskingPolicyByCode(maskingPolicyCode);
	}

}
