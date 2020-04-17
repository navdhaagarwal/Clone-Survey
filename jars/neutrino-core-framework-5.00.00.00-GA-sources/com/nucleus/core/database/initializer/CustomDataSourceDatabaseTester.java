package com.nucleus.core.database.initializer;

import javax.sql.DataSource;

import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

import com.nucleus.logging.BaseLoggers;

public class CustomDataSourceDatabaseTester extends DataSourceDatabaseTester{

	private DataSource dataSource;
	
	private String schema;
	
	public CustomDataSourceDatabaseTester(DataSource dataSource) {
		super(dataSource);
		this.dataSource=dataSource;
	}
	
	
	public CustomDataSourceDatabaseTester(DataSource dataSource, String schema) 
	{
		super(dataSource,schema);
		this.dataSource=dataSource;
		this.schema=schema;
	}
	
    public IDatabaseConnection getConnection() throws Exception
	{
		BaseLoggers.flowLogger.debug("getConnection() - start");
		assertTrue( "DataSource is not set", dataSource!=null );
		return new CustomDatabaseConnection( dataSource.getConnection(), getSchema() );
	}

}
