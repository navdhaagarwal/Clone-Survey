package com.nucleus.core.database.seed.audit;

import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.springframework.core.io.Resource;

import com.nucleus.service.BaseService;

public abstract interface SeedDataAuditService extends BaseService {
	public abstract void saveSeedDataAudit(SeedDataAudit paramSeedDataAudit);

	public abstract void deleteAllSeedDataAuditRecords(String productInfoCode);

	public SeedDataAudit findByFileAndTableName(String fileName, String tableName,String ProductInfoCode,int version);

	public SeedDataAudit updateSeedDataAudit(SeedDataAudit seedDataAudit);

	public Integer retreiveActualSeededCount(String seedFileName, String tableName,String ProductInfoCode,int version);

	public void logSeedData(Resource resource, IDataSet dataSet, Boolean isSeedingOn, Boolean isSeedingCompleted,
			IDatabaseTester databaseTester, String operation, Exception exc,String productInfoCode);
	
	
	public Long findCountByProductInfoCode(String productInfoCode);
	
	public int getLatestVersion(String producInfoCode);

}