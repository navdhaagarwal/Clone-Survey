package com.nucleus.config.persisted.configconvertors;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.vo.ConfigurationVO;

public interface IConfigConvertor {

    /**
     * Creates the <b>Configuration</b> from <b>ConfigurationVO</b>.
     *
     * @param configurationVO the configurationVO
     * @return the configuration 
     */
    public Configuration toConfiguration(ConfigurationVO configurationVO);

    /**
     * Creates the <b>ConfigurationVO</b> from <b>Configuration</b>.
     *
     * @param configuration the configuration 
     * @return the configuration vo
     */
    public ConfigurationVO fromConfiguration(Configuration configuration);

    /**
     * States that <b>ConfigurationVO</b> has been changed from the previous ConfigurationVO.
     *
     * @param configurationVO the configurationVO 
     * @return the boolean
     */
    public boolean isConfigurationChanged(ConfigurationVO configurationVO);

}
