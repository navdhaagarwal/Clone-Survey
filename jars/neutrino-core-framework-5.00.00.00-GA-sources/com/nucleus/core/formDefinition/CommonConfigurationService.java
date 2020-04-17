package com.nucleus.core.formDefinition;

import java.util.List;

import com.nucleus.core.configuration.entity.CommonConfiguration;
import com.nucleus.service.BaseService;

public interface CommonConfigurationService extends BaseService {

    /* public void saveConfiguration(List<PersistentFormData> persistentFormDatas);*/

    public List<CommonConfiguration> getConfigurationData();

    public CommonConfiguration getCommonConfigById(Long id);

    public void saveCommonConfig(CommonConfiguration commonConfiguration);

    public void deleteCommonConfig(CommonConfiguration commonConfiguration);

}
