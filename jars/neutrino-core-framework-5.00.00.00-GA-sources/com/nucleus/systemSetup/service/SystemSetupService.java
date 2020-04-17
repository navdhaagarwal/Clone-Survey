package com.nucleus.systemSetup.service;

import java.util.List;
import java.util.concurrent.Future;

import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.service.BaseService;
import com.nucleus.systemSetup.entity.AbstractSeedConfig;
import com.nucleus.systemSetup.entity.ApplicationFeatures;
import com.nucleus.systemSetup.entity.CompanyLicenseInfo;
import com.nucleus.systemSetup.entity.CountryConfig;

public interface SystemSetupService extends BaseService {

    public List<Future<String>> executeSeedOperation(List<String> resourceNames);

    public CompanyLicenseInfo saveConfiguration(CompanyLicenseInfo companyLicenseInfo, EntityId userEntityId);

    public <T extends BaseEntity> T findAbstractConfigById(Class<T> entityClass, Long Id);

    public <T extends AbstractSeedConfig> List<T> getAllSeedConfigEntities(Class<T> entityClass);

    public List<CountryConfig> getAllCountryConfig();

    public CountryConfig getCountryConfigByid(Long id);

    List<ApplicationFeatures> getallApplicationFeatures();

	String getSystemHttpProtocol();

}
