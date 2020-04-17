/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.formDefinition;

import java.util.List;

import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import com.nucleus.core.configuration.entity.CommonConfiguration;
import com.nucleus.service.BaseServiceImpl;

/**
 * @author Nucleus Software Exports Limited
 */
@Named(value = "commonConfigurationService")
@MonitoredWithSpring(name = "CommonConfiguration_Service_IMPL_")
public class CommonConfigurationServiceImpl extends BaseServiceImpl implements CommonConfigurationService {

    /*@Override
    public void saveConfiguration(List<PersistentFormData> persistentFormDatas) {

        for (PersistentFormData persistentFormData : persistentFormDatas) {
            CommonConfiguration dataConfiguration = new CommonConfiguration();
            dataConfiguration.setPersistentFormData(persistentFormData);
            entityDao.persist(dataConfiguration);
        }

    }*/

    @Override
    @MonitoredWithSpring(name = "CCSI_FETCH_CONF_DATA")
    public List<CommonConfiguration> getConfigurationData() {
        return entityDao.findAll(CommonConfiguration.class);
    }

    @Override
    public CommonConfiguration getCommonConfigById(Long id) {
        return entityDao.find(CommonConfiguration.class, id);
    }

    @Override
    public void saveCommonConfig(CommonConfiguration commonConfiguration) {
        entityDao.persist(commonConfiguration);
    }

    @Override
    public void deleteCommonConfig(CommonConfiguration commonConfiguration) {
        entityDao.delete(commonConfiguration);
    }

}
