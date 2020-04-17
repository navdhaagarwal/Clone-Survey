package com.nucleus.core.database.seed.audit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.logging.BaseLoggers;
import org.apache.commons.lang3.StringUtils;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.excel.XlsDataSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.ProductInformationLoader;

public class SeedDataAuditProcessor implements InitializingBean {

    @Inject
    @Named("seedDataAuditService")
    private SeedDataAuditService seedDataAuditService;
    private List<String>         resourceLocations;
    private Boolean              isSeedCountRequired;
    
    private boolean              isMasterSeedOn;
    private boolean              isNonMasterSeedOn;
    private boolean              consolidatedSeedOperation;
    private String               seedFolderLocation;


    private void postContruct() {

        if ((( isMasterSeedOn) || (isNonMasterSeedOn) || (consolidatedSeedOperation)) && (this.isSeedCountRequired != null) && (this.isSeedCountRequired.booleanValue() == true))
            try {
            	if(consolidatedSeedOperation){
                    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                    if(StringUtils.isNotEmpty(seedFolderLocation)){
                        Resource[] resources = resolver.getResources(String.format("%s%s%s","file:",Paths.get(seedFolderLocation).toString(),"/**.xls"));
                        int previousVersion = seedDataAuditService.getLatestVersion(ProductInformationLoader.getProductCode());
                        int currentVersion = previousVersion+1;
                        for (Resource resource : resources) {
                            InputStream inputStr = resource.getInputStream();
                            String fileName = resource.getFilename();
                            extractSeedInfo(inputStr, fileName,currentVersion);
                        }
                    }
                } else {
                    ClassLoader cl = super.getClass().getClassLoader();
                    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
                    if (this.resourceLocations.size() > 0) {
                        int previousVersion = seedDataAuditService.getLatestVersion(ProductInformationLoader.getProductCode());
                        int currentVersion = previousVersion+1;
                        for (String location : resourceLocations) {

                            Resource[] resources = resolver.getResources(location);
                            for (Resource resource : resources) {
                                InputStream inputStr = resource.getInputStream();
                                String fileName = resource.getFilename();
                                extractSeedInfo(inputStr, fileName,currentVersion);
                            }

                        }

                    }

                }

            } catch (IOException ex) {
				BaseLoggers.exceptionLogger.error(ex.getMessage(), ex);
            }
    }

   

    private void extractSeedInfo(InputStream inputStream, String fileName,int currentVersion) {

        try {
            XlsDataSet xlsDataSet = new XlsDataSet(inputStream);

            if (xlsDataSet != null) {
                ITable[] tables = xlsDataSet.getTables();
                if (tables != null && tables.length > 0) {
                      for (ITable table : tables) {
                        SeedDataAudit seedDataCount = new SeedDataAudit();
                        seedDataCount.setSeedFileName(fileName);
                        seedDataCount.setVersion(currentVersion);
                        seedDataCount.setTableName(table.getTableMetaData().getTableName());
                        seedDataCount.setRowCount(table.getRowCount());
                        seedDataCount.setProductInfoCode(ProductInformationLoader.getProductCode());
                        this.seedDataAuditService.saveSeedDataAudit(seedDataCount);
                    }
                }
            }
        } catch (DataSetException e) {
            throw new SystemException("Error occured while auditing seed data : ", e);

        } catch (IOException ex) {
            throw new SystemException("Error occured while auditing seed data : ", ex);
        }
    }

    public String getSeedFolderLocation() {
        return seedFolderLocation;
    }

    public void setSeedFolderLocation(String seedFolderLocation) {
        this.seedFolderLocation = seedFolderLocation;
    }

    public List<String> getResourceLocations() {
        return this.resourceLocations;
    }

    public void setResourceLocations(List<String> resourceLocations) {
        this.resourceLocations = resourceLocations;
    }

    public Boolean getIsSeedCountRequired() {
        return this.isSeedCountRequired;
    }

    public void setIsSeedCountRequired(Boolean isSeedCountRequired) {
        this.isSeedCountRequired = isSeedCountRequired;
    }

    public void afterPropertiesSet() throws Exception {
        postContruct();

    }
    public boolean getIsMasterSeedOn() {
        return isMasterSeedOn;
    }

    public void setIsMasterSeedOn(boolean isMasterSeedOn) {
        this.isMasterSeedOn = isMasterSeedOn;
    }

    public boolean getIsNonMasterSeedOn() {
        return isNonMasterSeedOn;
    }

    public void setIsNonMasterSeedOn(boolean isNonMasterSeedOn) {
        this.isNonMasterSeedOn = isNonMasterSeedOn;
    }

    public boolean isConsolidatedSeedOperation() {
        return consolidatedSeedOperation;
    }

    public void setConsolidatedSeedOperation(boolean consolidatedSeedOperation) {
        this.consolidatedSeedOperation = consolidatedSeedOperation;
    }


}
