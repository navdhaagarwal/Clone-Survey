package com.nucleus.web.custom.messageResolver;

import org.springframework.util.StringUtils;
import org.springframework.validation.MessageCodesResolver;

public class CustomMessageCodesResolver implements MessageCodesResolver {

	@Override
	public String[] resolveMessageCodes(String errorCode, String objectName) {

		return resolveMessageCodes(errorCode, objectName, "", null);
	}

	@Override
	public String[] resolveMessageCodes(String errorCode, String objectName,
			String field, Class<?> fieldType) {
		if (!StringUtils.isEmpty(errorCode)) {
			return new String[] { errorCode };
		}
		// return default message when no errorCode defines
		return new String[] { "label.default.errorCode.message" };
	}
}
