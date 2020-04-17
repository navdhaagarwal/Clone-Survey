package com.nucleus.notificationMaster.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;

import org.apache.commons.collections4.CollectionUtils;

import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.notification.CommonMailContent;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.EntityId;
import com.nucleus.mail.MailService;
import com.nucleus.mail.MimeMailMessageBuilder;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;

@Named("InAppHelper")
public class InAppHelperImpl extends BaseServiceImpl implements InAppMailHelper {

   

    protected static final String         CORPORATE_EMAIL_ENABLED = "config.user.corporateMails.enabled";

    @Inject
    @Named("configurationService")
    private ConfigurationService          configurationService;

    /** The Constant SMTP_FROM. */
    private static final String           SMTP_FROM               = "neutrino@nucleussoftware.com";

    @Inject
    @Named("mailMessageIntegrationService")
    private MailMessageIntegrationService mailMessageIntegrationService;

    @Inject
    @Named("mailService")
    private MailService                   mailService;

    @Named("organizationService")
    @Inject
    private OrganizationService           organizationService;

    @Inject
    @Named("userManagementServiceCore")
    protected UserManagementServiceCore   userManagementServiceCore;
    
    public static String NEUTRINO_SYSTEM_USER = "system";

    @Override
    public void sendNotificationAndCorporateEmails(Set<String> userUris2, CommonMailContent commonMailContent,
            boolean filteringEnabled, String notificationPriority) {
        Set<String> userUris3 = null;
        if (filteringEnabled) {
            userUris3 = filterUsersForInAppEmail(userUris2);
        } else {
            userUris3 = userUris2;
        }
        List<String> userIds = new ArrayList<String>();
        for (String userUri : userUris3) {
           
            ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(EntityId.fromUri(userUri),
                    CORPORATE_EMAIL_ENABLED);
            boolean enabled = false;
            if (configurationVO != null && configurationVO.getPropertyValue() != null) {
                enabled = Boolean.valueOf(configurationVO.getPropertyValue().toLowerCase());
            }
            if (enabled) {

                userIds.add(((User) entityDao.get(EntityId.fromUri(userUri))).getId().toString());

            }

        }
        if (CollectionUtils.isNotEmpty(userIds)) {
            sendCorporateEmails(userIds, commonMailContent, filteringEnabled);
        }

    }

    @Override
    public boolean checkUserEnabled(String userUri) {
        

       
        try {
            User user=(User) entityDao.get(EntityId
                    .fromUri(userUri));
            if(user.getUsername().equalsIgnoreCase(NEUTRINO_SYSTEM_USER))
            {
            return false;    
            }
          
        } catch (Exception e) {
            throw new SystemException(e);
        }

        
     
            return true;

    }

    private Set<String> filterUsersForInAppEmail(Set<String> userUris) {
        Set<String> userUris1 = new HashSet<String>();

        for (String userUri : userUris) {
            if (checkUserEnabled(userUri)) {
                userUris1.add(userUri);
            }

        }
        return userUris1;

    }

    private void sendCorporateEmails(List<String> userIds, CommonMailContent commonMailContent, boolean filteringEnabled) {
        String[] emailArr = null;
        Set<String> emailAddressList = new HashSet<String>();

        List<Long> idList = new ArrayList<Long>();
        if (userIds != null && !userIds.isEmpty()) {
            for (String userId : userIds) {
                Long id = Long.valueOf(userId);
                idList.add(id);
            }
        }

        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>("notification.users.getEmail").addParameter(
                "userIds", idList);
        List<String> list = entityDao.executeQuery(executor);

        if (list != null && !list.isEmpty()) {
            emailAddressList.addAll(list);
        }
        List<Long> idList1 = new ArrayList<Long>();
        idList1.add(((User) entityDao.get(EntityId.fromUri(commonMailContent.getFromUserUri()))).getId());

        NamedQueryExecutor<String> executor1 = new NamedQueryExecutor<String>("notification.users.getEmail").addParameter(
                "userIds", idList1);

        List<String> fromUserList = entityDao.executeQuery(executor1);

        MimeMailMessageBuilder mimeMailMessageBuilder = mailService.createMimeMailBuilder();
        if (CollectionUtils.isNotEmpty(fromUserList) && !filteringEnabled) {
            mimeMailMessageBuilder.setFrom(fromUserList.get(0)).setSubject(commonMailContent.getSubject())
                    .setHtmlBody(commonMailContent.getBody());
        } else {
            mimeMailMessageBuilder.setFrom(SMTP_FROM).setSubject(commonMailContent.getSubject())
                    .setHtmlBody(commonMailContent.getBody());

        }

        if (emailAddressList != null) {
            int emailListSize = emailAddressList.size();
            if (emailListSize > 0) {
                emailArr = emailAddressList.toArray(new String[emailListSize]);
                mimeMailMessageBuilder.setTo(emailArr);

                try {
                    mailMessageIntegrationService
                            .sendMailMessageToIntegrationServer(mimeMailMessageBuilder.getMimeMessage());
                } catch (MessagingException e) {
                    throw new SystemException("Mesage Exception Ocurred", e);
                } catch (IOException e) {
                    throw new SystemException("IO Exception Ocurred", e);
                }

            }
        }
    }

}
