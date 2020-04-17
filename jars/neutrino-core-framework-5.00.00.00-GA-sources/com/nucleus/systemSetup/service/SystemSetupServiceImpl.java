package com.nucleus.systemSetup.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.dbunit.DefaultOperationListener;
import org.dbunit.IDatabaseTester;
import org.dbunit.IOperationListener;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.operation.CustomDBUnitDatabaseOperation;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.businessmapping.service.UserOrgBranchMappingServiceImpl;
import com.nucleus.core.database.initializer.CustomDataSourceDatabaseTester;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.EntityLifeCycleData;
import com.nucleus.entity.EntityLifeCycleDataBuilder;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.systemSetup.entity.AbstractSeedConfig;
import com.nucleus.systemSetup.entity.ApplicationFeatures;
import com.nucleus.systemSetup.entity.CompanyLicenseInfo;
import com.nucleus.systemSetup.entity.CountryConfig;
import com.nucleus.user.OutOfOfficeDetails;
import com.nucleus.user.User;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserService;
import com.nucleus.user.UserStatus;

public class SystemSetupServiceImpl extends BaseServiceImpl implements SystemSetupService {

    @Autowired
    private DataSource                 dataSource;

    @Inject
    @Named("frameworkConfigResourceLoader")
    public NeutrinoResourceLoader      resourceLoader;

    @Inject
    @Named("userService")
    private UserService                userService;
    
    @Inject
    @Named("organizationService")
    private OrganizationService        organizationService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService          baseMasterService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Value("${system-setup-seed-operation}")
    private String                     operation;

    @Inject
    @Named("dbunitDataTypeFactory")
    private IDataTypeFactory           datatypeFactory;

    @Inject
    @Named("customPrimaryKeyFilter")
    private IColumnFilter              primaryKeyFilter;

    @Value("${system-setup-seed-config-batch-size}")
    private String                     seedConfigBatchSize;

    @Value("${system-setup-seed-config-isTransaction-enable}")
    private Boolean                    isTransactionEnabled;

    private String seedLocation;

    private boolean consolidatedSeedOperation;

    private String defaultSchemaName;

    private static List<String>        AVAILABLE_OPERATIONS = Arrays.asList(new String[] { "update", "refresh", "delete",
            "delete_all", "truncate_table", "insert", "none", "clean_insert", "safe_insert" });
    
    @Value(value = "#{'${system.http.protocol.secured}'}")
    private String                       securedProtocol;
    
    @Inject
    @Named("userOrgBranchMappingService")
    private UserOrgBranchMappingServiceImpl userOrgBranchMappingService;

    @Override
    public List<Future<String>> executeSeedOperation(List<String> resourceNames) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        List<Future<String>> futures = new ArrayList<Future<String>>();

        for (String resourceName : resourceNames) {
            SeedTask seedTask = new SeedTask(resourceName);
            Future<String> futureTask = (Future<String>) executorService.submit(seedTask);
            futures.add(futureTask);
        }
        executorService.shutdown();
        return futures;
    }

    protected void executeDatabaseSeeding(Resource resource) {
        try {
            BaseLoggers.flowLogger.error("Using schema name {}", defaultSchemaName);
            IDatabaseTester databaseTester = new CustomDataSourceDatabaseTester(dataSource);
            databaseTester.setSetUpOperation(getSetUpOperation());
            databaseTester.setDataSet(createDataset(resource));
            databaseTester.setOperationListener(getOperationListener());
            databaseTester.onSetup();
            BaseLoggers.flowLogger.info(String.format("Database \"%s\" operation successfully performed from resource: %s ",
                    operation, resource));
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error while seed execution", e);
            throw new SystemException("Problem while executing DB initialization from location: " + resource, e);
        }

    }

    protected IDataSet createDataset(Resource resource) throws Exception {

        InputStream is = resource.getInputStream();
        XlsDataSet lDataSet = new XlsDataSet(is);
        return lDataSet;
    }

    protected IOperationListener getOperationListener() {
        return new DefaultOperationListener() {
            @Override
            public void connectionRetrieved(IDatabaseConnection connection) {
                super.connectionRetrieved(connection);
                if (datatypeFactory != null) {
                    connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, datatypeFactory);
                }
                connection.getConfig().setProperty(DatabaseConfig.FEATURE_BATCHED_STATEMENTS, true);
                connection.getConfig()
                        .setProperty(DatabaseConfig.PROPERTY_BATCH_SIZE, Integer.parseInt(seedConfigBatchSize));
                connection.getConfig().setProperty(DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, primaryKeyFilter);
            }
        };
    }

    private void validate(Resource resource) {
        if (dataSource == null) {
            BaseLoggers.exceptionLogger.error("Datasource is null");
            throw new SystemException("Data Source is null.");
        }
        if (resource == null || (resource != null && !resource.exists())) {
            BaseLoggers.exceptionLogger.error("Resource doesn't exists");
            throw new SystemException("Resource doesn't exists");
        }
        if (!AVAILABLE_OPERATIONS.contains(operation)) {
            BaseLoggers.exceptionLogger.error("Not a valid operation to perform");
            throw new SystemException("Cannot perform database operation as operation name: " + operation
                    + " is not a valid operation name. Possible operation names are: " + AVAILABLE_OPERATIONS);
        }
    }

    protected DatabaseOperation getSetUpOperation() throws Exception {
        DatabaseOperation databaseOperation = null;
        if (operation.equalsIgnoreCase("update")) {
            databaseOperation = DatabaseOperation.UPDATE;
        } else if (operation.equalsIgnoreCase("refresh")) {
            databaseOperation = DatabaseOperation.REFRESH;
        } else if (operation.equalsIgnoreCase("delete")) {
            databaseOperation = DatabaseOperation.DELETE;
        } else if (operation.equalsIgnoreCase("delete_all")) {
            databaseOperation = DatabaseOperation.DELETE_ALL;
        } else if (operation.equalsIgnoreCase("truncate_table")) {
            databaseOperation = DatabaseOperation.TRUNCATE_TABLE;
        } else if (operation.equalsIgnoreCase("insert")) {
            databaseOperation = DatabaseOperation.INSERT;
        } else if (operation.equalsIgnoreCase("clean_insert")) {
            databaseOperation = DatabaseOperation.CLEAN_INSERT;
        } else if (operation.equalsIgnoreCase("safe_insert")) {
            databaseOperation = CustomDBUnitDatabaseOperation.SAFE_INSERT;
        } else {
            databaseOperation = DatabaseOperation.NONE;
        }

        // This will wrap the database operation in a transaction boundary
//        if (isTransactionEnabled) {
//            databaseOperation = DatabaseOperation.TRANSACTION(databaseOperation);
//        }

        return databaseOperation;
    }

    @Override
    public CompanyLicenseInfo saveConfiguration(CompanyLicenseInfo companyLicenseInfo, EntityId userEntityId) {
        NeutrinoValidator.notNull(companyLicenseInfo, "CompanyLicenseInfo Entity can not be null");
        NeutrinoValidator.notNull(userEntityId, "User Entity Id can not be null");
        saveCompanyAddressAndUser(companyLicenseInfo, userEntityId);
        entityDao.persist(companyLicenseInfo);
        return addLifeCycleDataToConfiguration(companyLicenseInfo, userEntityId);
    }

    public <T extends CompanyLicenseInfo> T addLifeCycleDataToConfiguration(T changedEntity, EntityId entityId) {
        if (null != entityId.getLocalId()) {
            User user = entityDao.find(User.class, entityId.getLocalId());
            if (changedEntity.getId() != null) {
                CompanyLicenseInfo originalEntity = entityDao.find(changedEntity.getClass(), changedEntity.getId());
                EntityLifeCycleData entityLifeCycleData = new EntityLifeCycleDataBuilder().setCreatedByEntityId(
                        user.getEntityId()).getEntityLifeCycleData();
                entityLifeCycleData.setUuid(originalEntity.getEntityLifeCycleData().getUuid());
                changedEntity.setEntityLifeCycleData(entityLifeCycleData);
                T returnedEntity = entityDao.update(changedEntity);
                return returnedEntity;
            }
            return changedEntity;
        }
        return changedEntity;
    }

    private void saveCompanyAddressAndUser(CompanyLicenseInfo companyLicenseInfo, EntityId userEntityId) {
        UserProfile userProfile = companyLicenseInfo.getUserProfile();

        if (userProfile != null && userProfile.getAssociatedUser() != null) {
            User user = companyLicenseInfo.getUserProfile().getAssociatedUser();
            user.setUserStatus(UserStatus.STATUS_ACTIVE);
            user.setApprovalStatus(ApprovalStatus.APPROVED);
            user.setPersistenceStatus(PersistenceStatus.ACTIVE);
            user.getEntityLifeCycleData().setCreatedByUri("com.nucleus.user.User:" + userEntityId.getLocalId());
            user.getEntityLifeCycleData().setSystemModifiableOnly(false);
            user.setSourceSystem(UserService.SOURCE_DB);
            user.setMailId(userProfile.getSimpleContactInfo().getEmail().getEmailAddress());
            
            // set the superadmin flag for new user to be created 
            user.setSuperAdmin(true);
            userProfile.setAssociatedUser(user);

            if (userProfile.getAddressRange().getIpaddress() != null
                    && userProfile.getAddressRange().getIpaddress().length() == 0) {
                userProfile.getAddressRange().setIpaddress(null);
            }
            if(user.getOutOfOfficeDetails()==null){
                OutOfOfficeDetails ood = new OutOfOfficeDetails();
                ood.setOutOfOffice(false);
                user.setOutOfOfficeDetails(ood);
            }

            else if (userProfile.getAddressRange().getFromIpAddress() != null
                    && userProfile.getAddressRange().getFromIpAddress().length() == 0) {
                userProfile.getAddressRange().setFromIpAddress(null);
                userProfile.getAddressRange().setToIpAddress(null);
            }
            companyLicenseInfo.setUserProfile(userProfile);
            userService.createUser(userProfile.getAssociatedUser());
            userService.saveUserProfile(companyLicenseInfo.getUserProfile());
            Map<String, Object> queryMap = new HashMap<String, Object>();
            queryMap.put("name", "CAS_ADMIN");

            // Add admin roles to Super Admin User.

            List<Role> roleList = baseMasterService.findEntityUsingEqualMatch(Role.class, queryMap);
            if (roleList != null && roleList.size() > 0) {
                userService.saveRolesForUser(companyLicenseInfo.getUserProfile().getAssociatedUser(), new Long[] { roleList
                        .get(0).getId() });
            }
            
            // Add organization branch mapping for Super Admin User.
            OrganizationBranch headOffice = organizationService.getHeadOffice();
            if(headOffice != null) {
            	
            	List<UserOrgBranchMapping> userOrgBranchMappings = new ArrayList<UserOrgBranchMapping>();
            	 UserOrgBranchMapping userOrgBranchMapping = new UserOrgBranchMapping();
                 userOrgBranchMapping.setOrganizationBranch(headOffice);
                 userOrgBranchMapping.setOrganizationBranchId(headOffice.getId());
                 userOrgBranchMapping.setAssociatedUser(companyLicenseInfo.getUserProfile().getAssociatedUser());
                 userOrgBranchMapping.setBranchAdmin(true);
                 userOrgBranchMapping.setPrimaryBranch(true);
                 userOrgBranchMapping.setApprovalStatus(ApprovalStatus.APPROVED);
                 entityDao.persist(userOrgBranchMapping);
               
                if (notNull(ProductInformationLoader.getProductCode())) {
                	List<OrganizationBranch> orgChildBranchList=organizationService.getAllChildBranches(headOffice.getId(), ProductInformationLoader.getProductCode());
                	if(hasElements(orgChildBranchList)){
                		for(OrganizationBranch orgBranch:orgChildBranchList){
                			userOrgBranchMappings.add(userOrgBranchMappingService.generateUserOrgBranchMapping(orgBranch.getEntityId(), companyLicenseInfo.getUserProfile().getAssociatedUser()));
                		}
                	}
        			
        		}
            }
        }
    }

    @Override
    public <T extends BaseEntity> T findAbstractConfigById(Class<T> entityClass, Long id) {
        return entityDao.find(entityClass, id);
    }

    @Override
    public <T extends AbstractSeedConfig> List<T> getAllSeedConfigEntities(Class<T> entityClass) {
        NeutrinoValidator.notNull(entityClass, "Entity cannot be null");
        String qlString = "FROM " + entityClass.getName() + " s WHERE (s.entityLifeCycleData.persistenceStatus=0)";
        JPAQueryExecutor<T> jPAQueryExecutor = new JPAQueryExecutor<T>(qlString);
        List<T> result = entityDao.executeQuery(jPAQueryExecutor);
        return result;
    }

    @Override
    public CountryConfig getCountryConfigByid(Long id) {

        CountryConfig countryConfig = entityDao.find(CountryConfig.class, id);
        return countryConfig;
    }

    @Override
    public List<CountryConfig> getAllCountryConfig() {
        List<CountryConfig> countryConfigList = entityDao.findAll(CountryConfig.class);
        return countryConfigList;
    }

    @Override
    public List<ApplicationFeatures> getallApplicationFeatures() {
        List<ApplicationFeatures> appfeaturesList = entityDao.findAll(ApplicationFeatures.class);
        return appfeaturesList;
    }

    class SeedTask implements Callable {
        private final String resourceName;

        public SeedTask(String resourceName) {
            this.resourceName = resourceName;
        }

        public String call() {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    Resource resource;
                    if(consolidatedSeedOperation) {
                        resource = getResource();
                    } else {
                        resource = resourceLoader.getResource(resourceName);
                    }
                    validate(resource);
                    executeDatabaseSeeding(resource);
                }

            });
            return resourceName;
        }

        private Resource getResource() {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String normalizedPath = String.format("%s%s%s%s","file:", Paths.get(seedLocation).toString(), File.separator,this.resourceName);
            return resolver.getResource(normalizedPath);
        }
    }

    public boolean isConsolidatedSeedOperation() {
        return consolidatedSeedOperation;
    }

    public void setConsolidatedSeedOperation(boolean consolidatedSeedOperation) {
        this.consolidatedSeedOperation = consolidatedSeedOperation;
    }

    public String getSeedLocation() {
        return seedLocation;
    }

    public void setSeedLocation(String seedLocation) {
        this.seedLocation = seedLocation;
    }

    private boolean isSystemHttpProtocolSecured() {
        BaseLoggers.flowLogger.info("SystemHttpProtocolSecured = {}", securedProtocol);
        return Boolean.valueOf(securedProtocol);
    }

    public String getDefaultSchemaName() {
        return defaultSchemaName;
    }

    public void setDefaultSchemaName(String defaultSchemaName) {
        this.defaultSchemaName = defaultSchemaName;
    }

    @Override
    public String getSystemHttpProtocol() {
        if (isSystemHttpProtocolSecured()) {
            return "https";
        } else {
            return "http";
        }
    }

}
