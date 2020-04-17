package com.nucleus.core.database.seed.audit;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Named;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.springframework.core.io.Resource;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;

@Named("seedDataAuditService")
public class SeedDataAuditServiceImpl extends BaseServiceImpl implements SeedDataAuditService {
	
	
	public void deleteAllSeedDataAuditRecords(String productInfoCode) {
		String qlString = "delete from SeedDataAudit where productInfoCode=:productInfoCode";
		Query qry = this.entityDao.getEntityManager().createQuery(qlString);
		qry.setParameter("productInfoCode",productInfoCode);

		qry.executeUpdate();
	}

	public void saveSeedDataAudit(SeedDataAudit seedDataAudit) {
		this.entityDao.persist(seedDataAudit);
	}

    public SeedDataAudit findByFileAndTableName(String fileName, String tableName, String ProductInfoCode,int version) {
        NeutrinoValidator.notNull(fileName, "File Name can't be null");
        NeutrinoValidator.notNull(tableName, "Table Name can't be null");
        NamedQueryExecutor<SeedDataAudit> seedDataAuditExec = new NamedQueryExecutor<SeedDataAudit>(
                "SeedDataAudit.FindByFileAndTableName").addParameter("fileName", fileName)
                        .addParameter("tableName", tableName).addParameter("productInfoCode", ProductInfoCode).addParameter("version", version);
        List<SeedDataAudit> seedDataAudit = entityDao.executeQuery(seedDataAuditExec);
        if (CollectionUtils.isNotEmpty(seedDataAudit))
            return seedDataAudit.get(0);
        else
            return null;
    }
	
	public SeedDataAudit updateSeedDataAudit(SeedDataAudit seedDataAudit){
		return entityDao.saveOrUpdate(seedDataAudit);
	}
	
	public Integer retreiveActualSeededCount(String seedFileName,String tableName,String ProductInfoCode,int version){
		
		NeutrinoValidator.notNull(seedFileName, "File Name can't be null");
		NeutrinoValidator.notNull(tableName, "Table Name can't be null");
		NamedQueryExecutor<Integer> actualSeedCount = new NamedQueryExecutor<Integer>(
				"SeedDataAudit.RetreiveActualSeedCount").addParameter("seedFileName", seedFileName).addParameter("tableName",
						tableName).addParameter("productInfoCode", ProductInfoCode).addParameter("version", version);
		List<Integer> seedCounts = entityDao.executeQuery(actualSeedCount);
		int sum = 0;
		if(CollectionUtils.isNotEmpty(seedCounts)){
			sum = seedCounts.stream().mapToInt(Integer::intValue).sum();
		}
		
		return sum;
	}
	
	public void logSeedData(Resource resource, IDataSet dataSet, Boolean isSeedingOn, Boolean isSeedingCompleted,IDatabaseTester databaseTester,String operation,Exception exc,String productInfoCode){
		
		try {
		    int version = getLatestVersion(productInfoCode);
			if (dataSet != null && dataSet.getTables().length > 0 && isSeedingOn) {
			  
				if (isSeedingCompleted) {
					BaseLoggers.flowLogger.error("Seeding compeleted for resource : " + resource);
					for (ITable table : dataSet.getTables()) {
						SeedDataAudit seedDataAuditInstance = findByFileAndTableName(resource.getFilename(), table.getTableMetaData().getTableName(),productInfoCode,version);
						if (seedDataAuditInstance != null) {
							seedDataAuditInstance.setSeedingOn(true);
							seedDataAuditInstance.setSeedingCompleted(isSeedingCompleted);
							seedDataAuditInstance.setActualSeededCount(table.getRowCount());
							seedDataAuditInstance.setSeedOperation(operation);
							
							updateSeedDataAudit(seedDataAuditInstance);
							
						}
					}
				} else if (!isSeedingCompleted) {
					BaseLoggers.flowLogger.error("Some issue occured during seeding for  : " + resource);

					try {
						ITable[] tables = dataSet.getTables();
						
						IDatabaseConnection connection = databaseTester.getConnection();
						QueryDataSet partialDataSet = new QueryDataSet(connection);
						try {
							if (tables != null && tables.length > 0) {
								for (ITable tableData : tables)
									partialDataSet.addTable(tableData.getTableMetaData().getTableName(),
											"SELECT count(*) FROM " + tableData.getTableMetaData().getTableName());
							}
							IDataSet actualDataset = new CachedDataSet(partialDataSet);

							String[] tablenames = dataSet.getTableNames();
							if(tablenames != null && tablenames.length > 0){
							for (int i = 0; i < tablenames.length; i++) {
								SeedDataAudit seedDataAuditInstance = findByFileAndTableName(resource.getFilename(), tablenames[i],productInfoCode,version);
								int expectedTableData = dataSet.getTable(tablenames[i]).getRowCount();
								BigDecimal actualTableData = (BigDecimal) actualDataset.getTable(tablenames[i]).getValue(0,
										"count(*)");
								if (seedDataAuditInstance != null) {
									seedDataAuditInstance.setActualSeededCount(actualTableData.intValue());
									seedDataAuditInstance.setSeedingOn(true);
									seedDataAuditInstance.setSeedingCompleted(true);
									seedDataAuditInstance.setSeedOperation(operation);
									updateSeedDataAudit(seedDataAuditInstance);
								
								if (actualTableData.intValue() != expectedTableData ) {
									if(actualTableData.intValue() > expectedTableData ){
										Integer actualSeededCount = retreiveActualSeededCount(resource.getFilename(), tablenames[i],productInfoCode,version);
										int shouldHaveVal = actualTableData.intValue()-actualSeededCount;
										if(shouldHaveVal == expectedTableData){
											seedDataAuditInstance.setActualSeededCount(shouldHaveVal);
											continue;
										}
									}
									seedDataAuditInstance.setSeedingCompleted(false);
									
									if(exc!=null)
									seedDataAuditInstance.setExceptionOccured(exc.getCause().toString());
									updateSeedDataAudit(seedDataAuditInstance);
									BaseLoggers.exceptionLogger.error("Seed issue at table : "
											+ actualDataset.getTable(tablenames[i]).getTableMetaData().getTableName());
									break;
								}
							}
							}
							}
						} catch (Exception e1) {
							BaseLoggers.exceptionLogger.error("Seed issue : ",e1);
						}

					} catch (Exception exception) {
						BaseLoggers.exceptionLogger.error(" Seed issue : ",exception);
					}
				}

			}else if(dataSet != null && dataSet.getTables().length > 0 && isSeedingOn == Boolean.FALSE){
                for (ITable table : dataSet.getTables()) {
                    SeedDataAudit seedDataAuditInstance = findByFileAndTableName(resource.getFilename(), table.getTableMetaData().getTableName(),ProductInformationLoader.getProductCode(),version);
                    if (seedDataAuditInstance != null) {
                        seedDataAuditInstance.setSeedingOn(false);
                        seedDataAuditInstance.setSeedingCompleted(false);
                        seedDataAuditInstance.setActualSeededCount(0);
                        seedDataAuditInstance.setSeedOperation(operation);
                        updateSeedDataAudit(seedDataAuditInstance);
                        
                    }
                }
			}
		} catch (DataSetException e) {
			BaseLoggers.exceptionLogger.error(" Seed issue : ",e);
		}
	}
	
	public Long findCountByProductInfoCode(String productInfoCode){
	    NamedQueryExecutor<Long> seedDataCount = new NamedQueryExecutor<Long>(
                "SeedDataAudit.getCount").addParameter("productInfoCode", productInfoCode);
        return  entityDao.executeQueryForSingleValue(seedDataCount); 
	}
	
	public int getLatestVersion(String producInfoCode){
	    NamedQueryExecutor<Integer> seedDataCount = new NamedQueryExecutor<Integer>(
                "SeedDataAudit.getLatestVersion").addParameter("productInfoCode", producInfoCode);
	   Integer version = entityDao.executeQueryForSingleValue(seedDataCount);
	    if(version != null)
        return version; 
	    else
	        return 0;
	}

   

   
}