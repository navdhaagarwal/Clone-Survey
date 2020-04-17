/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.makerchecker;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.core.xml.util.XmlUtils;
import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 */
@Named("entityUpdateHelper")
@Singleton
public class EntityUpdateHelper {

    private HashMap<String, List<EntityUpdateInfo>> _helperMap;
    private EntityUpdateMapper                      mapper;
    private List<MappedEntityInfo>                  mappedEntityInfoList;

    @Inject
    @Named("frameworkConfigResourceLoader")
    private NeutrinoResourceLoader                  resourceLoader;

    public List<EntityUpdateInfo> getEntityUpdateInfoList(String masterEntityName) {
        if (_helperMap == null) {
            loadHelperMap();
        }
        return _helperMap.get(masterEntityName);
    }

    public boolean getEntityAutoApprovalFlag(String masterEntityName) {
        boolean autoAuthorization = false;
        if (_helperMap == null) {
            loadHelperMap();
        }
        for (MappedEntityInfo mapEI : mappedEntityInfoList) {
            if (masterEntityName.equalsIgnoreCase(mapEI.getMasterEntityName())) {
                if (mapEI.getAutoAuthorizationFlag().equalsIgnoreCase(MasterApprovalFlowConstants.FLAG_Y)) {
                    autoAuthorization = true;
                }
                break;
            }
        }
        return autoAuthorization;
    }

    private void loadHelperMap() {
        try {
            InputStream is = resourceLoader.getResource("entity-approval-updates-config.xml").getInputStream();
            String input = IOUtils.toString(is);
            mapper = XmlUtils.readFromXml(input, EntityUpdateMapper.class);
        } catch (Exception e) {
            throw new SystemException("Application is unable to read entity-approval-updates-config.xml", e);
        }
        // validation
        _helperMap = new HashMap<String, List<EntityUpdateInfo>>();
        mappedEntityInfoList = mapper.getMappedEntityInfoList();
        for (MappedEntityInfo mapEI : mappedEntityInfoList) {
            List<EntityUpdateInfo> mapInfoList = mapEI.getEntityUpdateInfoList();
            for (EntityUpdateInfo eIO : mapInfoList) {
                if (StringUtils.isEmpty(eIO.getIncludeApprovalStatuses())) {
                    BaseLoggers.bugLogger.info("Included Approval Statuses is empty.");
                }
                if (StringUtils.isEmpty(eIO.getIncludePersistenceStatuses())) {
                    BaseLoggers.bugLogger.info("Included Persistence Statuses is empty.");
                }
                if (StringUtils.isEmpty(eIO.getUpdateEntityName())) {
                    throw new SystemException("Missing updateEntityName " + " for MasterEntity :"
                            + mapEI.getMasterEntityName());
                }
                if (StringUtils.isEmpty(eIO.getUpdateFieldName())) {
                    throw new SystemException("Missing updateFieldName " + " for MasterEntity :"
                            + mapEI.getMasterEntityName());
                }
            }
            if (!_helperMap.containsKey(mapEI.getMasterEntityName())) {
                _helperMap.put(mapEI.getMasterEntityName(), Collections.unmodifiableList(mapEI.getEntityUpdateInfoList()));
            } else {
                throw new SystemException("Master entity name mentioned more than once : " + mapEI.getMasterEntityName());
            }
        }

    }

}
