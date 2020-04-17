package com.nucleus.web.trustedsource;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.license.service.LicenseClientService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.mail.MimeMailMessageBuilder;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.security.oauth.apim.APIDetails;
import com.nucleus.security.oauth.apim.APIManagementService;
import com.nucleus.security.oauth.apim.ThrottlingPolicy;
import com.nucleus.security.oauth.constants.TrustedSourceRegistrationConstant;
import com.nucleus.security.oauth.domainobject.AuthorizedGrantType;
import com.nucleus.security.oauth.domainobject.AuthorizedGrantTypeMapping;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.security.oauth.domainobject.OauthScope;
import com.nucleus.security.oauth.domainobject.OauthScopeMapping;
import com.nucleus.security.oauth.util.TrustedSourceHelper;
import com.nucleus.user.IPAddressRange;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.user.UserServiceImpl;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.security.AesUtil;

@Transactional
@Controller
@RequestMapping(value = "/OauthClientDetails")
public class TrustedSourceController extends BaseController {

	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService makerCheckerService;
	@Inject
	@Named("mailService")
	private MailService mailService;

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	@Inject
	@Named("licenseClientService")
	private LicenseClientService licenseClientService;

	@Inject
	@Named("trustedSourceSecretService")
	private TrustedSourceSecretService trustedSourceSecretService;

	@Inject
	@Named("apiManagementService")
	private APIManagementService apiManagementService;

	@Inject
	@Named("userService")
	private UserService userService;

	@Inject
	@Named("mailMessageIntegrationService")
	private MailMessageIntegrationService mailMessageIntegrationService;
	private static final String  ACTIONS="actions";
	@PreAuthorize("hasAuthority('MAKER_OAUTHCLIENTDETAILS')")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String createNewClient(ModelMap map) {

		OauthClientDetails trustedSource = new OauthClientDetails();

		List<OauthScope> scopeList = baseMasterService.getAllApprovedAndActiveEntities(OauthScope.class);
		List<AuthorizedGrantType> authorizedGrantTypeList = baseMasterService
				.getAllApprovedAndActiveEntities(AuthorizedGrantType.class);

		List<IPAddressRange> ipAddressRangeList = new ArrayList<>();
		List<User> trustedUsersList = ((UserServiceImpl) userService).getAllActiveUsers();

		int i = 0;
		if (notNull(scopeList)) {
			long[] oAuthScopeIds = new long[scopeList.size()];

			for (OauthScope oAuthScope : scopeList) {
				oAuthScopeIds[i++] = oAuthScope.getId();
			}
			trustedSource.setOAuthScopeIds(oAuthScopeIds);
		}

		map.put(TrustedSourceRegistrationConstant.SCOPE_LIST, scopeList);
		map.put(TrustedSourceRegistrationConstant.OAUTH_CLIENT_DETAILS, trustedSource);
		map.put(TrustedSourceRegistrationConstant.MASTER_ID, TrustedSourceRegistrationConstant.MASTER_NAME);
		map.put(TrustedSourceRegistrationConstant.MAPPED_APIS_LIST, this.createNewAPIMapping());
		map.put(TrustedSourceRegistrationConstant.MAX_TRUSTED_SOURCES_LIMIT_EXCEEDED, this.checkIfLimitExceeded());
		map.put(TrustedSourceRegistrationConstant.MAX_MAPPABLE_USERS,
				licenseClientService.getLicenseDetail(ProductInformationLoader.getProductCode()).getMaxNamedUserPerTrustedClient());

		map.put(TrustedSourceRegistrationConstant.IP_ADDRESS_RANGE_LIST, ipAddressRangeList);
		map.put(TrustedSourceRegistrationConstant.TRUSTED_USERS_MAP_LIST, trustedUsersList);
		map.put(TrustedSourceRegistrationConstant.AUTHORIZED_GRANT_TYPES_LIST, authorizedGrantTypeList);

		return TrustedSourceRegistrationConstant.TRUSTED_SOURCE;
	}

	@PreAuthorize("hasAuthority('MAKER_OAUTHCLIENTDETAILS')")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveClient(@Validated OauthClientDetails trustedSource, BindingResult result, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster, HttpServletRequest request) {
		//Internal modules cannnot be added from UI
		map.put(TrustedSourceRegistrationConstant.MASTER_ID, TrustedSourceRegistrationConstant.MASTER_NAME);
		String newInternalSource =isNewInternalClient(trustedSource, result, map);

		if (newInternalSource != null) {
			return newInternalSource;
		}
		
		
		
		String res = this.checkDataForDuplication(trustedSource, result, map);

		if (res != null) {
			return res;
		}

		this.putTransientDataToObject(trustedSource);
		putIdpDetailsToObject(trustedSource, (String)request.getSession(false).getAttribute("PASS_PHRASE"));
		User user = getUserDetails().getUserReference();
		if (user != null) {
			makerCheckerService.masterEntityChangedByUser(trustedSource, user);
		}
		if (createAnotherMaster) {
			map.put(TrustedSourceRegistrationConstant.SCOPE_LIST,
					baseMasterService.getAllApprovedAndActiveEntities(OauthScope.class));
			map.put(TrustedSourceRegistrationConstant.OAUTH_CLIENT_DETAILS, trustedSource);

			map.put(TrustedSourceRegistrationConstant.IP_ADDRESS_RANGE_LIST, trustedSource.getIpAddresses());
			map.put(TrustedSourceRegistrationConstant.AUTHORIZED_GRANT_TYPES_LIST,
					baseMasterService.getAllApprovedAndActiveEntities(AuthorizedGrantType.class));

			map.put(TrustedSourceRegistrationConstant.MAPPED_APIS_LIST, this.createNewAPIMapping());
			map.put(TrustedSourceRegistrationConstant.MAX_TRUSTED_SOURCES_LIMIT_EXCEEDED, this.checkIfLimitExceeded());
			map.put(TrustedSourceRegistrationConstant.MAX_MAPPABLE_USERS,
					licenseClientService.getLicenseDetail(ProductInformationLoader.getProductCode()).getMaxNamedUserPerTrustedClient());
			map.put(TrustedSourceRegistrationConstant.TRUSTED_USERS_MAP_LIST,
					((UserServiceImpl) userService).getAllActiveUsers());
			return TrustedSourceRegistrationConstant.TRUSTED_SOURCE;
		}

		return "redirect:/app/grid/OauthClientDetails/OauthClientDetails/loadColumnConfig";

	}

	private void setTrustedUsersForViewAndEdit(OauthClientDetails trustedSource) { // NOSONAR

		List<User> mappedTrustedUsers = trustedSource.getTrustedUsers();
		int i = 0;
		if (notNull(mappedTrustedUsers) && !mappedTrustedUsers.isEmpty()) {
			long[] mappedUsersIds = new long[mappedTrustedUsers.size()];

			for (User user : mappedTrustedUsers) {
				mappedUsersIds[i] = user.getId();
				i++;
			}

			trustedSource.setTrustedUserIds(mappedUsersIds);

		}

	}

	private void setAuthorizeGrantTypesForViewAndEdit(OauthClientDetails trustedSource) { // NOSONAR
		List<AuthorizedGrantTypeMapping> authorizedGrantTypeMappings = trustedSource.getAuthorizedGrantTypeMappings();
		int i = 0;
		if (notNull(authorizedGrantTypeMappings) && !authorizedGrantTypeMappings.isEmpty()) {
			long[] authorizedGrantTypeIds = new long[authorizedGrantTypeMappings.size()];

			for (AuthorizedGrantTypeMapping authorizedGrantTypeMapping : authorizedGrantTypeMappings) {
				authorizedGrantTypeIds[i] = authorizedGrantTypeMapping.getAuthorizedGrantType().getId();
				i++;
			}
			trustedSource.setAuthorizedGrantTypeIds(authorizedGrantTypeIds);
		}

	}

	private void setOauthScopesForViewAndEdit(OauthClientDetails trustedSource) { // NOSONAR
		List<OauthScopeMapping> oauthScopeMappingMapping = trustedSource.getOauthScopeList();
		if (notNull(oauthScopeMappingMapping) && !oauthScopeMappingMapping.isEmpty()) {
			int i = 0;
			long[] oAuthScopeIds = new long[oauthScopeMappingMapping.size()];
			for (OauthScopeMapping oauthScopeMapping : oauthScopeMappingMapping) {

				oAuthScopeIds[i++] = oauthScopeMapping.getOauthScope().getId();
			}
			trustedSource.setOAuthScopeIds(oAuthScopeIds);
		}

	}

	@PreAuthorize("hasAuthority('MAKER_OAUTHCLIENTDETAILS')")
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/edit/{id}")
	public String editClient(@PathVariable("id") Long id, ModelMap map) {
		UserInfo currentUser = getUserDetails();
		OauthClientDetails trustedSource = baseMasterService.getMasterEntityWithActionsById(OauthClientDetails.class,
				id, currentUser.getUserEntityId().getUri());

		List<AuthorizedGrantTypeMapping> authorizedGrantTypeMappings = trustedSource.getAuthorizedGrantTypeMappings();
		int i = 0;
		if (notNull(authorizedGrantTypeMappings)) {
			long[] authorizedGrantTypeIds = new long[authorizedGrantTypeMappings.size()];

			for (AuthorizedGrantTypeMapping authorizedGrantTypeMapping : authorizedGrantTypeMappings) {
				authorizedGrantTypeIds[i] = authorizedGrantTypeMapping.getAuthorizedGrantType().getId();
				i++;
			}
			trustedSource.setAuthorizedGrantTypeIds(authorizedGrantTypeIds);
		}
		trustedSource.setIsClientSecretChangeRequired(Boolean.TRUE);
		setTrustedUsersForViewAndEdit(trustedSource);
		setAuthorizeGrantTypesForViewAndEdit(trustedSource);
		setOauthScopesForViewAndEdit(trustedSource);

		List<APIMappingDetailsVO> mappedApis = this.createNewAPIMapping();
		Map<Long, String> apisActiveMap = new HashMap<>();

		List<User> trustedUsersList = ((UserServiceImpl) userService).getAllActiveUsers();
		HibernateUtils.initializeAndUnproxy(trustedSource.getMappedAPIs());
		for (APIMappingDetailsVO mappedAPIVO : mappedApis) {
			for (APIDetails det : trustedSource.getMappedAPIs()) {
				if (det.getApiCode().equals(mappedAPIVO.getApiCode())) {
					mappedAPIVO.setIsAllowed(true);
					apisActiveMap.put(det.getId(), "true");

				}
			}
		}
		trustedSource.setMappedApis(apisActiveMap);
		List<OauthScopeMapping> oauthScopeMappingMapping = trustedSource.getOauthScopeList();
		if (notNull(oauthScopeMappingMapping)) {
			i = 0;
			long[] oAuthScopeIds = new long[oauthScopeMappingMapping.size()];
			for (OauthScopeMapping oauthScopeMapping : oauthScopeMappingMapping) {

				oAuthScopeIds[i++] = oauthScopeMapping.getOauthScope().getId();
			}
			trustedSource.setOAuthScopeIds(oAuthScopeIds);
		}

		map.put("edit", true);
		map.put(TrustedSourceRegistrationConstant.SCOPE_LIST,
				baseMasterService.getAllApprovedAndActiveEntities(OauthScope.class));
		map.put(TrustedSourceRegistrationConstant.OAUTH_CLIENT_DETAILS, trustedSource);
		map.put(TrustedSourceRegistrationConstant.IP_ADDRESS_RANGE_LIST, trustedSource.getIpAddresses());
		map.put(TrustedSourceRegistrationConstant.MAPPED_APIS_LIST, mappedApis);
		map.put(TrustedSourceRegistrationConstant.MAX_MAPPABLE_USERS,
				licenseClientService.getLicenseDetail(ProductInformationLoader.getProductCode()).getMaxNamedUserPerTrustedClient());
		map.put(TrustedSourceRegistrationConstant.MASTER_ID, TrustedSourceRegistrationConstant.MASTER_NAME);
		map.put(TrustedSourceRegistrationConstant.AUTHORIZED_GRANT_TYPES_LIST,
				baseMasterService.getAllApprovedAndActiveEntities(AuthorizedGrantType.class));
		map.put(TrustedSourceRegistrationConstant.TRUSTED_USERS_MAP_LIST, trustedUsersList);
		ArrayList<String> actions = (ArrayList<String>) trustedSource.getViewProperties().get(ACTIONS);
		if (actions != null) {
			for (String act : actions) {
				map.put("act" + act, false);
			}
		}

		return TrustedSourceRegistrationConstant.TRUSTED_SOURCE;
	}

	@PreAuthorize("hasAuthority('MAKER_OAUTHCLIENTDETAILS')")
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	public String saveAndSendForApproval(@Validated OauthClientDetails trustedSource, BindingResult result,
			ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster, HttpServletRequest request) {
		map.put(TrustedSourceRegistrationConstant.MASTER_ID, TrustedSourceRegistrationConstant.MASTER_NAME);
		String newInternalSource =isNewInternalClient(trustedSource, result, map);

		if (newInternalSource != null) {
			return newInternalSource;
		}
		String res = this.checkDataForDuplication(trustedSource, result, map);
		if (notNull(res)) {
			return res;
		}
		
		this.putTransientDataToObject(trustedSource);
		
		putIdpDetailsToObject(trustedSource, (String)request.getSession(false).getAttribute("PASS_PHRASE"));

		User user = getUserDetails().getUserReference();
		if (notNull(user)) {
			makerCheckerService.saveAndSendForApproval(trustedSource, user);
		}
		if (createAnotherMaster) {
			map.put(TrustedSourceRegistrationConstant.SCOPE_LIST,
					baseMasterService.getAllApprovedAndActiveEntities(OauthScope.class));
			map.put(TrustedSourceRegistrationConstant.OAUTH_CLIENT_DETAILS, trustedSource);

			map.put(TrustedSourceRegistrationConstant.AUTHORIZED_GRANT_TYPES_LIST,
					baseMasterService.getAllApprovedAndActiveEntities(AuthorizedGrantType.class));
			map.put(TrustedSourceRegistrationConstant.IP_ADDRESS_RANGE_LIST, new ArrayList<IPAddressRange>());
			map.put(TrustedSourceRegistrationConstant.MAPPED_APIS_LIST, this.createNewAPIMapping());
			map.put(TrustedSourceRegistrationConstant.MAX_TRUSTED_SOURCES_LIMIT_EXCEEDED, checkIfLimitExceeded());
			map.put(TrustedSourceRegistrationConstant.MAX_MAPPABLE_USERS,
					licenseClientService.getLicenseDetail(ProductInformationLoader.getProductCode()).getMaxNamedUserPerTrustedClient());
			map.put(TrustedSourceRegistrationConstant.TRUSTED_USERS_MAP_LIST,
					((UserServiceImpl) userService).getAllActiveUsers());
			return TrustedSourceRegistrationConstant.TRUSTED_SOURCE;
		}

		return "redirect:/app/grid/OauthClientDetails/OauthClientDetails/loadColumnConfig";

	}

	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('VIEW_OAUTHCLIENTDETAILS') or hasAuthority('MAKER_OAUTHCLIENTDETAILS') or hasAuthority('CHECKER_OAUTHCLIENTDETAILS') ")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewClient(@PathVariable("id") Long id, ModelMap map) {

		UserInfo currentUser = getUserDetails();
		OauthClientDetails trustedSource = baseMasterService.getMasterEntityWithActionsById(OauthClientDetails.class,
				id, currentUser.getUserEntityId().getUri());

		setTrustedUsersForViewAndEdit(trustedSource);
		setAuthorizeGrantTypesForViewAndEdit(trustedSource);
		setOauthScopesForViewAndEdit(trustedSource);

		List<APIMappingDetailsVO> mappedApis = this.createNewAPIMapping();
		HibernateUtils.initializeAndUnproxy(trustedSource.getMappedAPIs());

		List<User> trustedUsersList = ((UserServiceImpl) userService).getAllActiveUsers();

		for (APIMappingDetailsVO mappedAPIVO : mappedApis) {
			for (APIDetails det : trustedSource.getMappedAPIs()) {
				if (det.getApiCode().equals(mappedAPIVO.getApiCode())) {
					mappedAPIVO.setIsAllowed(true);
				}
			}
		}

		map.put("viewable", true);
		map.put(TrustedSourceRegistrationConstant.IP_ADDRESS_RANGE_LIST, trustedSource.getIpAddresses());
		map.put(TrustedSourceRegistrationConstant.SCOPE_LIST,
				baseMasterService.getAllApprovedAndActiveEntities(OauthScope.class));
		map.put(TrustedSourceRegistrationConstant.OAUTH_CLIENT_DETAILS, trustedSource);
		map.put(TrustedSourceRegistrationConstant.MASTER_ID, TrustedSourceRegistrationConstant.MASTER_NAME);
		map.put(TrustedSourceRegistrationConstant.AUTHORIZED_GRANT_TYPES_LIST,
				baseMasterService.getAllApprovedAndActiveEntities(AuthorizedGrantType.class));
		map.put(TrustedSourceRegistrationConstant.MAPPED_APIS_LIST, mappedApis);
		map.put(TrustedSourceRegistrationConstant.TRUSTED_USERS_MAP_LIST, trustedUsersList);
		if (trustedSource.isApproved() && trustedSource.getEncryptedSecret() != null) {
			map.put("getCredentials", true);
		}
		ArrayList<String> actions = (ArrayList<String>) trustedSource.getViewProperties().get(ACTIONS);
		if(TrustedSourceHelper.isInternalModule(trustedSource.getClientId()))
		{
		
			if(actions!=null)
			{
			actions.remove("Delete");
			trustedSource.addProperty(ACTIONS,actions);
			}
			
		}

		if (actions != null) {
			for (String act : actions) {
				map.put("act" + act, false);
			}
		}

		return TrustedSourceRegistrationConstant.TRUSTED_SOURCE;
	}

	private void putTransientDataToObject(OauthClientDetails trustedSource) { // NOSONAR
			if(trustedSource.getIsClientSecretChangeRequired() != null && !trustedSource.getIsClientSecretChangeRequired()){
				OauthClientDetails originalTrustedSource = baseMasterService.getMasterEntityWithActionsById(
						OauthClientDetails.class,
						trustedSource.getId(), getUserDetails().getUserEntityId().getUri());
				trustedSource.setClientSecret(originalTrustedSource.getClientSecret());
				trustedSource.setHashKey(originalTrustedSource.getHashKey());
				trustedSource.setPassPhrase(originalTrustedSource.getPassPhrase());
				
			}
		
			else if (trustedSource.getEncryptedSecret() == null) {
				trustedSource.setEncryptedSecret("dummyEncryptedSec");
				trustedSource.setClientSecret(null);
				}

		putTrustedUsersToObject(trustedSource);
		if (notNull(trustedSource.getOAuthScopeIds()) && trustedSource.getOAuthScopeIds().length > 0) {
			List<OauthScopeMapping> oAuthScopeMappingMappings = new ArrayList<>();
			for (long oAuthScopeId : trustedSource.getOAuthScopeIds()) {
				OauthScopeMapping oauthScopeMapping = new OauthScopeMapping();
				oauthScopeMapping.setOauthScope(baseMasterService.findById(OauthScope.class, oAuthScopeId));
				oAuthScopeMappingMappings.add(oauthScopeMapping);

			}
			trustedSource.setOauthScopeList(oAuthScopeMappingMappings);
		}

		List<APIDetails> apiMappingList = apiManagementService
				.getAPIDetailsFromLicense(licenseClientService.getLicenseDetail(ProductInformationLoader.getProductCode()).getLicenseApiDetailsVOList());
		if (apiMappingList == null) {

			trustedSource.setMappedAPIs(Collections.<APIDetails>emptyList());
		} else {
			List<APIDetails> actualApisToBeAdded = new ArrayList<>();

			for (APIDetails api : apiMappingList) {
				String isApplied = trustedSource.getMappedApis().get(api.getId());
				if (isApplied != null && "checked".equals(isApplied)) {
					actualApisToBeAdded.add(api);
				}
			}
			trustedSource.setMappedAPIs(actualApisToBeAdded);
		}
	}

	private void putTrustedUsersToObject(OauthClientDetails trustedSource) { // NOSONAR
		if (notNull(trustedSource.getAuthorizedGrantTypeIds())
				&& trustedSource.getAuthorizedGrantTypeIds().length > 0) {
			List<AuthorizedGrantTypeMapping> authorizedGrantTypeMappings = new ArrayList<>();
			for (long authorizedGrantType : trustedSource.getAuthorizedGrantTypeIds()) {
				AuthorizedGrantTypeMapping authorizedGrantTypeMapping = new AuthorizedGrantTypeMapping();
				authorizedGrantTypeMapping.setAuthorizedGrantType(
						baseMasterService.findById(AuthorizedGrantType.class, authorizedGrantType));
				authorizedGrantTypeMappings.add(authorizedGrantTypeMapping);
				if (authorizedGrantType == 1 && notNull(trustedSource.getTrustedUserIds())
						&& trustedSource.getTrustedUserIds().length > 0) {
				this.setLimitedTrustedUsersToSource(trustedSource);
				
				}

			}
			trustedSource.setAuthorizedGrantTypes(authorizedGrantTypeMappings);
		}

	}

	private void setLimitedTrustedUsersToSource(OauthClientDetails trustedSource) { // NOSONAR
		List<User> trustedUsers = new ArrayList<>();
		int i = 0;

		Integer maxLimit = licenseClientService.getLicenseDetail(ProductInformationLoader.getProductCode()).getMaxNamedUserPerTrustedClient();
		if (maxLimit == null || maxLimit == -1) {
			for (long userId : trustedSource.getTrustedUserIds()) {
				User user = baseMasterService.findById(User.class, userId);
				trustedUsers.add(user);
			}

		} else {
			for (long userId : trustedSource.getTrustedUserIds()) {
				if (i >= maxLimit) {
					break;
				}
				User user = baseMasterService.findById(User.class, userId);
				trustedUsers.add(user);
				i++;

			}
		}
		
		trustedSource.setTrustedUsers(trustedUsers);
	}

	@RequestMapping(value = "/appendTimeUnitChoices", method = RequestMethod.GET)
	public String appendTimeUnitChoices(@RequestParam int endSize, ModelMap map) {
		map.put("endSize", endSize);
		return "/OauthClientDetails/timeUnit";

	}

	@ResponseBody
	@RequestMapping(value = "/checkDuplicateClientId", method = RequestMethod.GET)
	public String checkDuplicateClientId(OauthClientDetails trustedSource)  {
		Map<String, Object> validateMap = new HashMap<>();
		validateMap.put(TrustedSourceRegistrationConstant.CLIENT_ID, trustedSource.getClientId());
		List<String> colNameList = checkValidationForDuplicates(trustedSource, OauthClientDetails.class, validateMap);

		if (colNameList != null && !colNameList.isEmpty()) {
			return "UnAvailable";
		}

		return "Available";
	}

	@ResponseStatus(value = HttpStatus.OK)
	@Transactional
	@RequestMapping(value = "/downloadclientcredentials/{id}")
	public void downloadFile(HttpServletResponse response, @PathVariable("id") Long id) throws IOException {

		OauthClientDetails originaltrustedSource = baseMasterService.findById(OauthClientDetails.class, id);
		String clientId = originaltrustedSource.getClientId();
		if (originaltrustedSource.getClientSecret() == null) {

			trustedSourceSecretService.setDefaultData(originaltrustedSource);
			trustedSourceSecretService.editClientSecret(originaltrustedSource);
			trustedSourceSecretService.persistTrustedSource(originaltrustedSource);

		}

		response.setContentType("application/force-download");
		response.setHeader("Content-Disposition", "attachment;  filename=\"" + clientId + "_client_details" + ".txt\"");
		PrintWriter out = response.getWriter();

		String passPhraseToShare = AesUtil.Decrypt(originaltrustedSource.getPassPhrase(),
				OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE);

		String clientSecretOriginal = null;
		if (originaltrustedSource.getEncryptedSecret() != null) {
			clientSecretOriginal = AesUtil.Decrypt(originaltrustedSource.getEncryptedSecret(),
					OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE);

		}

		originaltrustedSource.setEncryptedSecret(null);
		clientSecretOriginal = AesUtil.encrypt(clientSecretOriginal, passPhraseToShare);
		out.println(TrustedSourceRegistrationConstant.CLIENTID + ":" + clientId);
		out.println(TrustedSourceRegistrationConstant.PASS_PHRASE + ":" + passPhraseToShare);
		out.println(TrustedSourceRegistrationConstant.CLIENT_SECRET + ":" + clientSecretOriginal);

		out.close();

	}

	@ResponseStatus(value = HttpStatus.OK)
	@Transactional
	@RequestMapping(value = "/mailclientcredentials/{id}")
	public void emailFile(@PathVariable("id") Long id) {
		OauthClientDetails originaltrustedSource = baseMasterService.findById(OauthClientDetails.class, id);

		String clientId = originaltrustedSource.getClientId();

		if (originaltrustedSource.getClientSecret() == null) {

			trustedSourceSecretService.setDefaultData(originaltrustedSource);
			trustedSourceSecretService.editClientSecret(originaltrustedSource);
			trustedSourceSecretService.persistTrustedSource(originaltrustedSource);
		}

		String passPhraseToShare = AesUtil.Decrypt(originaltrustedSource.getPassPhrase(),
				OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE);

		String clientSecretOriginal = null;
		if (originaltrustedSource.getEncryptedSecret() != null) {
			clientSecretOriginal = AesUtil.Decrypt(originaltrustedSource.getEncryptedSecret(),
					OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE);

		}
		clientSecretOriginal = AesUtil.encrypt(clientSecretOriginal, passPhraseToShare);
		String msg = TrustedSourceRegistrationConstant.CLIENTID + ":" + clientId + "<br>"
				+ TrustedSourceRegistrationConstant.PASS_PHRASE + ":" + passPhraseToShare + "<br>"
				+ TrustedSourceRegistrationConstant.CLIENT_SECRET + ":" + clientSecretOriginal;

		MimeMailMessageBuilder mimeMailMessageBuilder = mailService.createMimeMailBuilder();
		String subject = "Oauth Registered Client Details";

		String senderEmailAddress = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
				TrustedSourceRegistrationConstant.FROM_ADDRESS).getPropertyValue();
		mimeMailMessageBuilder.setFrom(senderEmailAddress).setSubject(subject).setHtmlBody(msg);

		mimeMailMessageBuilder.setTo(originaltrustedSource.getMailId());

		try {
			mailMessageIntegrationService.sendMailMessageToIntegrationServer(mimeMailMessageBuilder.getMimeMessage());
			originaltrustedSource.setEncryptedSecret(null);
		} catch (MessagingException e) {
			throw new SystemException(TrustedSourceRegistrationConstant.MESSAGE_EXCEPTION, e);
		} catch (IOException e) {
			throw new SystemException(TrustedSourceRegistrationConstant.IO_EXCEPTION, e);
		}

	}

	private String checkDataForDuplication(OauthClientDetails trustedSource, BindingResult result, ModelMap map) { // NOSONAR
		Map<String, Object> validateMap = new HashMap<>();
		validateMap.put(TrustedSourceRegistrationConstant.CLIENT_ID, trustedSource.getClientId());

		List<String> colNameList = checkValidationForDuplicates(trustedSource, OauthClientDetails.class, validateMap);
		if (result.hasErrors() || (colNameList != null && !colNameList.isEmpty())) {

			prepareDefaultData( trustedSource, map);
			map.put("error", "label.clientId.validation.exists");

			if (colNameList != null && !colNameList.isEmpty()
					&& colNameList.contains(TrustedSourceRegistrationConstant.CLIENT_ID)) {
				result.rejectValue(TrustedSourceRegistrationConstant.CLIENT_ID, "label.clientId.validation.exists");
			}

			return TrustedSourceRegistrationConstant.TRUSTED_SOURCE;
		}

		return null;
	}

	private String isNewInternalClient(OauthClientDetails trustedSource, BindingResult result, ModelMap map) { // NOSONAR

		if (TrustedSourceHelper.isInternalModule(trustedSource.getClientId()) && !trustedSource.getIsInternal()) {

			prepareDefaultData(trustedSource, map);
			map.put("error", "label.internal.source.manual");
			result.rejectValue(TrustedSourceRegistrationConstant.CLIENT_ID, "label.internal.source.manual");

			return TrustedSourceRegistrationConstant.TRUSTED_SOURCE;

		}

		return null;
	}
	private void prepareDefaultData(OauthClientDetails trustedSource,ModelMap map)
	{
		List<APIMappingDetailsVO> mappedApis = this.createNewAPIMapping();

		for (APIMappingDetailsVO apiDetailsVO : mappedApis) {

			apiDetailsVO.setIsAllowed("checked".equals(trustedSource.getMappedApis().get(apiDetailsVO.getApiId())));

		}

		map.put(TrustedSourceRegistrationConstant.SCOPE_LIST,
				baseMasterService.getAllApprovedAndActiveEntities(OauthScope.class));
		map.put(TrustedSourceRegistrationConstant.OAUTH_CLIENT_DETAILS, trustedSource);
		map.put(TrustedSourceRegistrationConstant.IP_ADDRESS_RANGE_LIST, trustedSource.getIpAddresses());

		map.put(TrustedSourceRegistrationConstant.MAPPED_APIS_LIST, mappedApis);
		map.put(TrustedSourceRegistrationConstant.TRUSTED_USERS_MAP_LIST,
				baseMasterService.getAllApprovedAndActiveEntities(User.class));

		map.put(TrustedSourceRegistrationConstant.AUTHORIZED_GRANT_TYPES_LIST,
				baseMasterService.getAllApprovedAndActiveEntities(AuthorizedGrantType.class));
		

	}
	private List<APIMappingDetailsVO> createNewAPIMapping() { // NOSONAR

		List<APIDetails> allowedAPIs = apiManagementService
				.getAPIDetailsFromLicense(licenseClientService.getLicenseDetail(ProductInformationLoader.getProductCode()).getLicenseApiDetailsVOList());
		if (allowedAPIs == null) {
			return Collections.<APIMappingDetailsVO>emptyList();
		}
		List<APIMappingDetailsVO> mappedAPIs = new ArrayList<>();

		for (APIDetails api : allowedAPIs) {
			APIMappingDetailsVO apiDetailsVO = new APIMappingDetailsVO();
			apiDetailsVO.setApiId(api.getId());
			apiDetailsVO.setApiCode(api.getApiCode());

			if (!api.getPolicies().isEmpty()) {
				ThrottlingPolicy policy = api.getPolicies().iterator().next();

				if (policy != null) {
					apiDetailsVO.setTimeUnit(policy.getTimeUnit().getCode());
					apiDetailsVO.setAccessCount(policy.getAllowedQuota());
				}
			}
			mappedAPIs.add(apiDetailsVO);

		}
		return mappedAPIs;

	}

	private boolean checkIfLimitExceeded() {

		Integer maxTrustedSourceLimit = licenseClientService.getLicenseDetail(ProductInformationLoader.getProductCode()).getMaxTrustedClient();

		boolean limitExceeded = false;
		if (maxTrustedSourceLimit != null && maxTrustedSourceLimit != -1 && maxTrustedSourceLimit != 0) {
			List<OauthClientDetails> trustedSources = baseMasterService
					.getAllApprovedAndActiveEntities(OauthClientDetails.class);

			if (trustedSources.size() >= maxTrustedSourceLimit) {
				BaseLoggers.flowLogger.warn("Client " + maxTrustedSourceLimit);
				limitExceeded = true;
			}
		}
		return limitExceeded;

	}
	
	private void putIdpDetailsToObject(OauthClientDetails trustedSource, String passPhrase) {

		Set<String> grantTypeSet = trustedSource.getAuthorizedGrantTypes();
		
		if(!grantTypeSet.contains(TrustedSourceRegistrationConstant.GRANT_TYPE_FEDERATED)) {
			trustedSource.setIdpClientId(null);
			trustedSource.setIdpClientSecret(null);
			trustedSource.setRedirectUri(null);
			return;
		}
		
		String idpClientSecret = null;
		if(!StringUtils.isEmpty(trustedSource.getIdpClientSecret())) {
			idpClientSecret = AesUtil.Decrypt(trustedSource.getIdpClientSecret(), passPhrase);
		}
		
		if(!StringUtils.isEmpty(trustedSource.getClientId()) && !StringUtils.isEmpty(trustedSource.getIdpClientSecret()) && !StringUtils.isEmpty(idpClientSecret) &&
				!StringUtils.isEmpty(trustedSource.getRedirectUri())) {
			trustedSource.setIdpClientSecret(idpClientSecret);
		}
		
	}

}
