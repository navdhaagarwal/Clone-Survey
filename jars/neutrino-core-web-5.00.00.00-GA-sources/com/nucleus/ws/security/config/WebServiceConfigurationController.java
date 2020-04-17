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
package com.nucleus.ws.security.config;

import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.core.dynamicQuery.service.DynamicQueryMetadataService;
import com.nucleus.core.dynamicQuery.service.DynamicQueryTranslatorService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.json.util.JsonUtils;
import com.nucleus.core.util.security.cert.*;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.IPAddressRange;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.ws.core.inbound.config.InBoundServiceInfoPojo;
import com.nucleus.ws.core.inbound.config.msg.IntegrationMessageExchange;
import com.nucleus.ws.core.inbound.config.msg.IntegrationMessageSearchCriteria;
import com.nucleus.ws.core.inbound.config.service.IntegrationConfigurationService;
import com.nucleus.ws.core.inbound.config.user.SystemUser;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Controller
@RequestMapping(value = "/integrationConfig")
public class WebServiceConfigurationController extends BaseController {

    private static final String            STATE_BY_COUNTRY_CODE = "select DISTINCT state.stateName FROM State state WHERE state.country.countryISOCode = :countryISOCode and state.masterLifeCycleData.approvalStatus IN :approvalStatus AND (state.entityLifeCycleData.snapshotRecord IS NULL OR state.entityLifeCycleData.snapshotRecord = false) AND state.activeFlag = true";

    @Inject
    @Named("entityDao")
    protected EntityDao                    entityDao;

    @Value("${config.passWord:storepass@Neutrino-CAS}")
    private  String passWord;

    @Inject
    @Named("mailMessageIntegrationService")
    MailMessageIntegrationService          mailMessageIntegrationService;

    @Inject
    @Named("mailService")
    MailService                            mailService;

    @Inject
    @Named("configurationService")
    public ConfigurationService            configurationService;

    @Inject
    @Named("integrationConfigurationService")
    public IntegrationConfigurationService integrationConfigurationService;

    @Inject
    @Named(value = "dynamicQueryMetadataService")
    DynamicQueryMetadataService            queryMetadataService;

    @Inject
    @Named(value = "dynamicQueryTranslatorService")
    DynamicQueryTranslatorService          queryTranslatorService;
    
    @PreAuthorize("hasAuthority('MAKER_WEBSERVICECONFIGURATION')")
    @RequestMapping(value = "/getWsUserCreateForm")
    public String getCreateUserPage(ModelMap map) {

        SystemUserCreateForm systemUserCreateForm = new SystemUserCreateForm();
        systemUserCreateForm.setCertificateIssuedByUser(getUsername());
        systemUserCreateForm.setFullDayAccessAllowed(true);
        systemUserCreateForm.setThrottleRequests(true);
        systemUserCreateForm.setRejectRequestsOnRateExceed(false);
        systemUserCreateForm.setCertificateIssueDate(DateTime.now().toDateTime(DateTimeZone.UTC));

        map.put("systemUserCreateForm", systemUserCreateForm);
        map.put("allAuthoritiesList", integrationConfigurationService.getAllowedAuthoritiesForAllEndpoints());
        return "webServiceUserCreate";
    }

    
    @PreAuthorize("hasAuthority('MAKER_WEBSERVICECONFIGURATION')")
    @RequestMapping(value = "/submit/getCertNow")
    @ResponseBody
    public HttpEntity<byte[]> submitCreateUserPage(ModelMap map, SystemUserCreateForm userCreateForm) {

        SubjectInfo subjectInfo = createSubjectInfo(userCreateForm);

        KeyAndCertificateHolder certificateHolder = NeutrinoX509CertificateGenerator.generateIssuerSignedX509Certificate(
                getIssuerInfo(), subjectInfo, StoreType.JKS);

        // create a system user
        createAndPersistSystemUser(userCreateForm, certificateHolder);

        // send mail for store and key password
        sendMailForStoreCredentialsInfo(userCreateForm, certificateHolder);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(mediaType);
        responseHeaders.setContentDispositionFormData("attachment", certificateHolder.getStoreFileName());
        HttpEntity<byte[]> fileEntity = new HttpEntity<byte[]>(certificateHolder.getKeyCertificateStore(), responseHeaders);
        return fileEntity;

    }
    
    @PreAuthorize("hasAuthority('MAKER_WEBSERVICECONFIGURATION') or hasAuthority('VIEW_WEBSERVICECONFIGURATION')  or hasAuthority('CHECKER_WEBSERVICECONFIGURATION')")
    @RequestMapping(value = "/inbound/serviceGrid")
    public String getInboundServiceGrid(ModelMap map, HttpServletRequest request) {

        List<InBoundServiceInfoPojo> boundServiceInfoPojos = integrationConfigurationService.getInboundServiceConfigGrid();

        // set exposed service and port from request if not provided
        for (InBoundServiceInfoPojo inBoundServiceInfoPojo : boundServiceInfoPojos) {
            if (StringUtils.isBlank(inBoundServiceInfoPojo.getExposedAtIP())) {
                inBoundServiceInfoPojo.setExposedAtIP(request.getServerName());
            }
            if (StringUtils.isBlank(inBoundServiceInfoPojo.getExposedAtPort())) {
                inBoundServiceInfoPojo.setExposedAtPort(String.valueOf(request.getServerPort()));
            }

        }

        map.put("inBoundServicesJson", JsonUtils.serializeWithoutLazyInitialization(boundServiceInfoPojos));
        return "inBoundServiceGrid";

    }

    @PreAuthorize("hasAuthority('MAKER_WEBSERVICECONFIGURATION') or hasAuthority('VIEW_WEBSERVICECONFIGURATION')  or hasAuthority('CHECKER_WEBSERVICECONFIGURATION')")
    @RequestMapping(value = "/inbound/view/{serviceId}")
    public String getInboundService(ModelMap map, @PathVariable("serviceId") String serviceId, HttpServletRequest request) {

        InBoundServiceInfoPojo inBoundServiceInfoPojo = integrationConfigurationService.getInboundServiceConfig(serviceId);
        inBoundServiceInfoPojo.setExposedAtIP(request.getServerName());
        inBoundServiceInfoPojo.setExposedAtPort(String.valueOf(request.getServerPort()));
        map.put("inBoundServiceForm", inBoundServiceInfoPojo);
        map.put("allAuthoritiesList", integrationConfigurationService.getAllowedAuthoritiesForAllEndpoints());

        return "inBoundServicePage";
    }

    @PreAuthorize("hasAuthority('MAKER_WEBSERVICECONFIGURATION')")
    @RequestMapping(value = "/inbound/update")
    public RedirectView updateInboundServiceInfo(InBoundServiceInfoPojo inBoundServiceInfoPojo) {

        integrationConfigurationService.updateInboundServiceConfig(inBoundServiceInfoPojo);
        RedirectView redirectView = new RedirectView("serviceGrid", true);
        return redirectView;
    }

    @PreAuthorize("hasAuthority('MAKER_WEBSERVICECONFIGURATION')")
    @RequestMapping(value = "/message/searchPage")
    public String messageSearchPage(ModelMap map) {

        IntegrationMessageSearchCriteria messageSearchCriteria = new IntegrationMessageSearchCriteria();
        messageSearchCriteria.setQueryContextId(20001L);
        Map<Long, String> queryTokens = queryMetadataService.getAllTokensIdNameMapWithContextIdAndType(20001L,
                Arrays.asList(QueryToken.SELECT_TYPE, QueryToken.BOTH));
        messageSearchCriteria.setSelectedTokenIds(queryTokens.keySet().toArray(new Long[] {}));
        map.put("selectItemList", queryTokens);
        map.put("messageSearchCriteria", messageSearchCriteria);
        return "messageSearchPage";
    }

    @PreAuthorize("hasAuthority('MAKER_WEBSERVICECONFIGURATION') or hasAuthority('VIEW_WEBSERVICECONFIGURATION')  or hasAuthority('CHECKER_WEBSERVICECONFIGURATION')")
    @RequestMapping(value = "/message/search")
    public String searchMessages(ModelMap map, IntegrationMessageSearchCriteria messageSearchCriteria) {

        List<Map<String, Object>> messageExchanges = integrationConfigurationService
                .getMessagesByCriteria(messageSearchCriteria);
        String messageListJson = JsonUtils.serializeWithoutLazyInitialization(messageExchanges, getUserDateTimeFormat());
        map.put("messageListJson", messageListJson);
        return "integrationConfig/messageGrid";
    }

    @PreAuthorize("hasAuthority('MAKER_WEBSERVICECONFIGURATION')")
    @RequestMapping(value = "/states/{countryISOCode}")
    @ResponseBody
    public Map<String, String> findStateByCountryCode(@PathVariable String countryISOCode) {

        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        JPAQueryExecutor<String> jpaQueryExecutor = new JPAQueryExecutor<String>(STATE_BY_COUNTRY_CODE);
        jpaQueryExecutor.addParameter("countryISOCode", countryISOCode).addParameter("approvalStatus", approvalStatusList);
        List<String> stateNames = entityDao.executeQuery(jpaQueryExecutor);
        Map<String, String> states = new HashMap<String, String>();
        if (stateNames != null) {
            for (String state : stateNames) {
                states.put(state, state);
            }
        }

        return states;
    }

    @PreAuthorize("hasAuthority('MAKER_WEBSERVICECONFIGURATION') or hasAuthority('VIEW_WEBSERVICECONFIGURATION')  or hasAuthority('CHECKER_WEBSERVICECONFIGURATION')")
    @RequestMapping(value = "/message/{messageId}")
    public String showMessageInfo(@PathVariable("messageId") Long messageId, ModelMap map) {

        IntegrationMessageExchange exchange = integrationConfigurationService.getIntegrationMessageExchangeById(messageId);

        if (exchange != null) {
            exchange.getViewProperties().put("reqmsg", new String(exchange.getRequestMessage()));
            exchange.getViewProperties().put(
                    "respmsg",
                    new String(exchange.getResponseMessage() != null ? exchange.getResponseMessage() : exchange
                            .getFaultMessage()));

            map.put("messageExchange", exchange);
        }

        return "integrationMessageInfo";
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~private methods
    private void sendMailForStoreCredentialsInfo(SystemUserCreateForm userCreateForm,
            KeyAndCertificateHolder certificateHolder) {

        if (userCreateForm.getContactPersonEmail() != null
                && StringUtils.isNoneBlank(userCreateForm.getContactPersonEmail().getEmailAddress())) {

            String targetMail = userCreateForm.getContactPersonEmail().getEmailAddress();
            BaseLoggers.flowLogger.info("Sending key alias,password and store password to e-mail address [{}]", targetMail);

            MimeMessage mimeMessage = mailService
                    .createMimeMailBuilder()
                    .setTo(targetMail)
                    .setSubject("Neutrino Web Service Certificate")
                    .setPlainTextBody(
                            String.format("Dear User,\n\nKey Alias: %s\nKey Password: %s\n\nStore Password: %s",
                                    certificateHolder.getKeyAlias(), new String(certificateHolder.getKeyPassword()),
                                    new String(certificateHolder.getStorePassword()))).getMimeMessage();
            try {
                mailMessageIntegrationService.sendMailMessageToIntegrationServer(mimeMessage);
            } catch (MessagingException e) {
            	exceptionLoggingService.saveExceptionDataInCouch(getUserDetails(), e);
                BaseLoggers.exceptionLogger.error("Error in sending key alias,password and store password by E-mail", e);
            } catch (IOException e) {
            	exceptionLoggingService.saveExceptionDataInCouch(getUserDetails(), e);
                BaseLoggers.exceptionLogger.error("Error in sending key alias,password and store password by E-mail", e);
            }

        }

    }

    private void createAndPersistSystemUser(SystemUserCreateForm userCreateForm, KeyAndCertificateHolder certificateHolder) {

        SystemUser systemUser = new SystemUser();
        systemUser.setAllowAccessFromDayTime(userCreateForm.getAllowAccessFromDayTime());
        systemUser.setAllowAccessToDayTime(userCreateForm.getAllowAccessToDayTime());
        systemUser.setAuthoritiesAsCsv(StringUtils.join(userCreateForm.getAuthorities(), ","));
        systemUser.setCertificateExpirationDate(userCreateForm.getCertificateExpirationDate());
        systemUser.setCertificateIssueDate(userCreateForm.getCertificateIssueDate());
        systemUser.setCertificateSerialNumber(certificateHolder.getCertSerialNumber() != null ? certificateHolder
                .getCertSerialNumber().toString() : null);
        systemUser.setCertificateIssuedByUser(userCreateForm.getCertificateIssuedByUser());
        systemUser.setContactPersonEmail(userCreateForm.getContactPersonEmail());
        systemUser.setContactPersonName(userCreateForm.getContactPersonName());
        systemUser.setContactPersonPhone(userCreateForm.getContactPersonPhone());
        systemUser.setDistinguishedName(certificateHolder.getSubjectDN());

        IPAddressRange addressRange = new IPAddressRange();
        if (StringUtils.isNotBlank(userCreateForm.getRemoteIpAddressRangeStart())
                && StringUtils.isNotBlank(userCreateForm.getRemoteIpAddressRangeEnd())) {
            addressRange.setFromIpAddress(userCreateForm.getRemoteIpAddressRangeStart());
            addressRange.setToIpAddress(userCreateForm.getRemoteIpAddressRangeEnd());
        } else {
            if (StringUtils.isBlank(userCreateForm.getRemoteIpAddressRangeStart())
                    && StringUtils.isNotBlank(userCreateForm.getRemoteIpAddressRangeStart())) {
                addressRange.setIpaddress(userCreateForm.getRemoteIpAddressRangeStart());
            }
        }

        systemUser.setRejectRequestsOnRateExceed(userCreateForm.getRejectRequestsOnRateExceed());
        systemUser.setThrottleRequests(userCreateForm.getThrottleRequests());
        systemUser.setUsername(userCreateForm.getUsername());
        systemUser.setMaximumRequestsPerPeriod(userCreateForm.getMaximumRequestsPerPeriod());
        systemUser.setTimePeriodMillis(userCreateForm.getTimePeriodMillis());

        if (getUserDetails() != null && getUserDetails().getUserEntityId() != null) {
            systemUser.getEntityLifeCycleData().setCreatedByUri(getUserDetails().getUserEntityId().getUri());
        }
        entityDao.persist(systemUser);
        BaseLoggers.flowLogger.info("Created a new system user in system with UID [{}]", userCreateForm.getUsername());

    }

    private SubjectInfo createSubjectInfo(SystemUserCreateForm userCreateForm) {

        SubjectInfo subjectInfo = new SubjectInfo();

        subjectInfo.setCertificateExpirationDate(userCreateForm.getCertificateExpirationDate());
        subjectInfo.setCertificateIssueDate(userCreateForm.getCertificateIssueDate());
        subjectInfo.setCommonName(userCreateForm.getCommonName());

        if (userCreateForm.getCountry() != null) {
            subjectInfo.setCountryCode(userCreateForm.getCountry().getCountryISOCode());
        }
        if (userCreateForm.getStateOrProvince() != null) {
            subjectInfo.setStateOrProvinceName(userCreateForm.getStateOrProvince().getStateName());
        }
        subjectInfo.setStreetAddress(userCreateForm.getStreetAddress());
        subjectInfo.setEmailAddress(userCreateForm.getContactPersonEmail().getEmailAddress());
        subjectInfo.setOrganizationalUnitName(userCreateForm.getOrganizationalUnitName());
        subjectInfo.setOrganizationName(userCreateForm.getOrganizationName());
        subjectInfo.setUid(userCreateForm.getUsername());

        return subjectInfo;
    }

    // this is to load certificate which will sign the generated certificate.
    private IssuerInfo getIssuerInfo() {

        // can be fetched from DB
        ClassPathResource classPathResource = null;
        try {
            KeyStore store = KeyStore.getInstance(StoreType.JKS.name());
            classPathResource = new ClassPathResource("keystore/Neutrino-CAS_Root.jks");
            store.load(classPathResource.getInputStream(), passWord.toCharArray());
            Key key = store.getKey("Neutrino-CAS_PK", "privkeypass@Neutrino-CAS".toCharArray());
            PublicKey publicKey = store.getCertificate("Neutrino-CAS_PK").getPublicKey();
            IssuerInfo issuerInfo = new IssuerInfo((PrivateKey) key, publicKey,
                    (X509Certificate) store.getCertificate("Neutrino-CAS_PK"));
            return issuerInfo;
        } catch (Exception e) {
            throw new SystemException(
                    "Error in creating IssuerInfo from  resource [" + classPathResource != null ? classPathResource.getDescription()
                            : "" + "]", e);
        }

    }

}
