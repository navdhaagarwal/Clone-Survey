package com.nucleus.web.trustedsource;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasNoElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.license.pojo.LicenseMobilityModuleInfo;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.security.oauth.apim.APIDetails;
import com.nucleus.security.oauth.constants.TrustedSourceRegistrationConstant;
import com.nucleus.security.oauth.domainobject.AuthorizedGrantType;
import com.nucleus.security.oauth.domainobject.AuthorizedGrantTypeMapping;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.security.oauth.domainobject.OauthScope;
import com.nucleus.security.oauth.domainobject.OauthScopeMapping;
import com.nucleus.user.User;

@Named("trustedSourceRegistrationService")
public class TrustedSourceRegistrationServiceImpl implements TrustedSourceRegistrationService {
	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	@Value("${accessTokenValiditySeconds}")
	private Integer accessTokenValiditySeconds;
	@Value("${refreshTokenValiditySeconds}")
	private Integer refreshTokenValiditySeconds;
	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService makerCheckerService;
	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;
	
	@Override
	public List<OauthClientDetails> getAllActiveInternalTrustedSource() {
		NamedQueryExecutor<OauthClientDetails> oauthClientDetailsExecutor = new NamedQueryExecutor<>(
				"oauthClient.loadInternalTrustedSource");

		return entityDao.executeQuery(oauthClientDetailsExecutor);

	}

	@Override
	public void deActivateInternalTrustedSource() {
		List<OauthClientDetails> oauthClientDetailsList = getAllActiveInternalTrustedSource();
		if (oauthClientDetailsList == null || oauthClientDetailsList.isEmpty())
			return;
		for (OauthClientDetails oauthClientDetails : oauthClientDetailsList) {
			oauthClientDetails.setActiveFlag(false);
			oauthClientDetails.getEntityLifeCycleData().setSystemModifiableOnly(true);
		}
	}

	private List<LicenseMobilityModuleInfo> filterToBeRegisterInternalTrustedSource(List<LicenseMobilityModuleInfo> mobilityModuleInfoList) {
		
		List<LicenseMobilityModuleInfo> toBeAddedmobilityModuleInfoList=new ArrayList<>();
		toBeAddedmobilityModuleInfoList.addAll(mobilityModuleInfoList);
		List<OauthClientDetails> existingTrustedSourceList = getAllActiveInternalTrustedSource();
		List<LicenseMobilityModuleInfo> existingMobilityModuleInfoList = new ArrayList<>();
		for (OauthClientDetails existingTrustedSource : existingTrustedSourceList) {
			for (LicenseMobilityModuleInfo mobilityModuleInfo : mobilityModuleInfoList) {
				if (mobilityModuleInfo.getMobilityModuleCode().equalsIgnoreCase(existingTrustedSource.getClientId().toLowerCase())) {
					existingMobilityModuleInfoList.add(mobilityModuleInfo);
				}
			}
		}
		toBeAddedmobilityModuleInfoList.removeAll(existingMobilityModuleInfoList);
		return toBeAddedmobilityModuleInfoList;
	}

	@Override
	public void registerInternalTrustedSourceFromLicense(List<LicenseMobilityModuleInfo> mobilityModuleInfoList,
			String emailId, List<APIDetails> apiDetailsList, User user) {

		List<LicenseMobilityModuleInfo> toBeAddedmobilityModuleInfoList=	filterToBeRegisterInternalTrustedSource(mobilityModuleInfoList);
		List<OauthScope> scopeList = baseMasterService.getAllApprovedAndActiveEntities(OauthScope.class);
		List<AuthorizedGrantType> authorizedGrantTypeList = baseMasterService
				.getAllApprovedAndActiveEntities(AuthorizedGrantType.class);
		for (LicenseMobilityModuleInfo mobilityModuleInfo : toBeAddedmobilityModuleInfoList) {

			OauthClientDetails trustedSource = new OauthClientDetails();
			trustedSource.setNamedUserCount(mobilityModuleInfo.getNamedUserCount());
			trustedSource.setConcurrentUserCount(mobilityModuleInfo.getConcurrentUserCount());
			trustedSource.setClientId(mobilityModuleInfo.getMobilityModuleCode());
			trustedSource.setMailId(emailId);
			trustedSource.setIsInternal(true);
			trustedSource.setMappedAPIs(apiDetailsList);
			trustedSource.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
			trustedSource.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
            setAuthorizationGrantType(trustedSource,mobilityModuleInfo,authorizedGrantTypeList);
			
				if (notNull(scopeList) && !scopeList.isEmpty()) {
					List<OauthScopeMapping> oAuthScopeMappingMappings = new ArrayList<>();
					for (OauthScope scope : scopeList) {
						OauthScopeMapping oauthScopeMapping = new OauthScopeMapping();
						oauthScopeMapping.setOauthScope(scope);
						oAuthScopeMappingMappings.add(oauthScopeMapping);

					}
					trustedSource.setOauthScopeList(oAuthScopeMappingMappings);
				}

				if (user != null) {
					makerCheckerService.saveAndSendForApproval(trustedSource, user);
				}

			

		}

	}

	private void setAuthorizationGrantType(OauthClientDetails trustedSource,
			LicenseMobilityModuleInfo mobilityModuleInfo, List<AuthorizedGrantType> authorizedGrantTypeList) {
		if (hasNoElements(authorizedGrantTypeList)) {
			return;
		}

		List<AuthorizedGrantTypeMapping> authorizedGrantTypeMappings = new ArrayList<>();

		if (mobilityModuleInfo.isAnnonymousModule()) {

			for (AuthorizedGrantType authorizedGrantType : authorizedGrantTypeList) {
				if (authorizedGrantType.getGrantType().equals(TrustedSourceRegistrationConstant.ANONYMOUS)) {
					AuthorizedGrantTypeMapping authorizedGrantTypeMapping = new AuthorizedGrantTypeMapping();
					authorizedGrantTypeMapping.setAuthorizedGrantType(authorizedGrantType);
					authorizedGrantTypeMappings.add(authorizedGrantTypeMapping);
					break;
				}
			}
		} else {
			for (AuthorizedGrantType authorizedGrantType : authorizedGrantTypeList) {
				if (!authorizedGrantType.getGrantType().equals(TrustedSourceRegistrationConstant.ANONYMOUS)) {
					AuthorizedGrantTypeMapping authorizedGrantTypeMapping = new AuthorizedGrantTypeMapping();
					authorizedGrantTypeMapping.setAuthorizedGrantType(authorizedGrantType);
					authorizedGrantTypeMappings.add(authorizedGrantTypeMapping);
				}
			}

		}
		trustedSource.setAuthorizedGrantTypes(authorizedGrantTypeMappings);

	}
}