package com.nucleus.api.documentation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.api.documentation.entity.ApiMessageCode;
import com.nucleus.api.documentation.service.ApiDocumentationService;
import com.nucleus.api.documentation.vo.ApiMessageCodeVO;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;

@Controller
@RequestMapping("/api-docs")
public class ApiDocumentationController {

	@Inject
	@Named("messageSource")
	private MessageSource messageSource;

	@Inject
	@Named("apiDocumentationService")
	private ApiDocumentationService apiDocumentationService;

	@RequestMapping(value = "/fetchAllMessageCodes", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody List<ApiMessageCodeVO> getAllApiMessageCodes() {

		List<ApiMessageCode> apiMessageCodes = apiDocumentationService
				.getApiMessageCodesByModuleCode(ProductInformationLoader.getProductCode());

		if (CollectionUtils.isNotEmpty(apiMessageCodes)) {

			return prepareMessageCodesVO(apiMessageCodes);
		}

		return new ArrayList<ApiMessageCodeVO>();
	}

	private List<ApiMessageCodeVO> prepareMessageCodesVO(List<ApiMessageCode> apiMessageCodes) {
		List<ApiMessageCodeVO> apiMessageCodeVOs = new ArrayList<>();

		apiMessageCodes.stream().forEach(apiMessageCode -> {
			try {
				if (apiMessageCode.getMessageCode() != null) {

					String messageDescription = messageSource.getMessage(apiMessageCode.getMessageCode(),
							new String[] {}, new Locale("en"));
					messageDescription = apiMessageCode.getMessageCode().contentEquals(messageDescription)
							? messageSource.getMessage(apiMessageCode.getMessageCode(), new String[] {},
									new Locale("en", "US"))
							: messageDescription;

					if (!apiMessageCode.getMessageCode().contentEquals(messageDescription)) {
						ApiMessageCodeVO apiMessageCodeVo = new ApiMessageCodeVO();
						apiMessageCodeVo.setMessageCode(apiMessageCode.getMessageCode());
						apiMessageCodeVo.setMessageDescription(messageDescription);
						apiMessageCodeVo.setApiCode(apiMessageCode.getApiCode());
						apiMessageCodeVOs.add(apiMessageCodeVo);
					}
				}

			} catch (NoSuchMessageException noSuchMethodException) {
				BaseLoggers.flowLogger.error("Error in resolving message for message code [" + apiMessageCode
						+ "].Error : " + noSuchMethodException.getMessage());
			}

		});

		return apiMessageCodeVOs;
	}

}
