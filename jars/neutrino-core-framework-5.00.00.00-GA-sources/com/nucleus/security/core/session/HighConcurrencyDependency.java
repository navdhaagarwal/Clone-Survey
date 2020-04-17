package com.nucleus.security.core.session;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.SystemEntity;
import com.nucleus.service.BaseServiceImpl;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Component
public class HighConcurrencyDependency extends BaseServiceImpl {

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    @Inject
    @Named(value = "sessionModuleService")
    private SessionModuleService sessionModuleService;


    @PostConstruct
    public void init() {
        Boolean isConcurrencySwichingEnabled = false;
        ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), "config.concurrency.highlow.mode.enable");
        if (configurationVO == null || configurationVO.getPropertyValue() == null || BooleanUtils.toBooleanObject(configurationVO.getPropertyValue()) == null) {
            throw new RuntimeException("Exception occured during configuring high concurrency mode: Property value missing or invalid for : config.concurrency.highlow.mode.enable");
        }
        String highconcurrencyPropertyValue = configurationVO.getPropertyValue();
        if (BooleanUtils.toBooleanObject(highconcurrencyPropertyValue) != null) {
            isConcurrencySwichingEnabled = BooleanUtils.toBooleanObject(highconcurrencyPropertyValue);
            try {
                if (isConcurrencySwichingEnabled) {
                    validateConfigForModule();
                }
            } catch (Exception e) {
                throw new RuntimeException("Exception occured during configuring high concurrency mode" , e);
            }
        }
        cleanSessionModuleMappings();

    }

    private void cleanSessionModuleMappings() {
        String module = ProductInformationLoader.getProductName();
        sessionModuleService.deleteAllSessionModuleMapping(module);
    }

    private void validateConfigForModule() {

        //Generic
        String delayProp = "config.concurrency.logout.delay";
        ConfigurationVO delayVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), delayProp);
        if (delayVO == null || StringUtils.isEmpty(delayVO.getPropertyValue()) || !StringUtils.isNumeric(delayVO.getPropertyValue())) {
            throw new RuntimeException("Exception occured during configuring high concurrency mode: Property value missing or invalid for : config.concurrency.logout.delay");
        }

        //For modules
        String module = ProductInformationLoader.getProductName();
        String hcPropKey = module + ".config.concurrency.high.activation.count";
        String maxThPropKey = module + ".config.concurrency.max.threshold.count";
        ConfigurationVO highConcurrencyStartCountVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), hcPropKey);
        ConfigurationVO maxThresholdCountVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), maxThPropKey);
        if (highConcurrencyStartCountVO == null || StringUtils.isEmpty(highConcurrencyStartCountVO.getPropertyValue()) || !StringUtils.isNumeric(highConcurrencyStartCountVO.getPropertyValue())) {
            throw new RuntimeException("Exception occured during configuring high concurrency mode: Property value missing or invalid for : "+hcPropKey);
        }

        if (maxThresholdCountVO == null || StringUtils.isEmpty(maxThresholdCountVO.getPropertyValue()) || !StringUtils.isNumeric(maxThresholdCountVO.getPropertyValue())) {
            throw new RuntimeException("Exception occured during configuring high concurrency mode: Property value missing or invalid for : "+maxThPropKey);
        }
    }

}