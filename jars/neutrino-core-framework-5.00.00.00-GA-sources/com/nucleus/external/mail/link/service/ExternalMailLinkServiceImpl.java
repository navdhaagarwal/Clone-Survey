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
package com.nucleus.external.mail.link.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.entity.SystemEntity;
import com.nucleus.service.BaseServiceImpl;

/**
 * @author Nucleus Software Exports Limited
 */

@Named("externalMailLinkService")
public class ExternalMailLinkServiceImpl extends BaseServiceImpl implements ExternalMailLinkService {

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    public static final String   HTTP  = "http://";
    public static final String   COLON = ":";
    public static final String   SLASH = "/";
    public static final String   EQUAL = "=";
    public static final String   APP   = "app?";

    @Value("${core.web.config.target.url.param}")
    private String               redirectURL;

    @Override
    public String getExternalMailLink(String externalMailLink) {
        String portNumber = null;
        String appContext = null;
        String ipAddress = null;
        String appPath = null;

        ConfigurationVO portNumberVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                "config.notification.port.number");
        if (portNumberVO != null) {
            portNumber = portNumberVO.getText();
        } else {
            throw new InvalidDataException("Port Number can not be null");
        }

        ConfigurationVO appContextVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                "config.notification.app.context");
        if (appContextVO != null) {
            appContext = appContextVO.getText();
        } else {
            throw new InvalidDataException("Application Context can not be null");
        }

        ConfigurationVO ipAddrVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
                "config.notification.ip.address");
        if (ipAddrVO != null && StringUtils.isNotEmpty(ipAddrVO.getText())) {
            ipAddress = ipAddrVO.getText();
        } else {
            InetAddress ip;
            try {

                ip = InetAddress.getLocalHost();
                ipAddress = ip.getHostAddress();

            } catch (UnknownHostException e) {
                throw new SystemException("Unable to connect to Remote IP", e);

            }
        }

        appPath = HTTP + ipAddress + COLON + portNumber + SLASH + appContext + SLASH + APP + redirectURL + EQUAL
                + externalMailLink;
        return appPath;
    }
}
