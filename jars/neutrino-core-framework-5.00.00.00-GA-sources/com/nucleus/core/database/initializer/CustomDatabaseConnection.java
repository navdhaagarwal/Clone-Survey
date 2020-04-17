package com.nucleus.core.database.initializer;

import java.sql.Connection;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ITableFilterSimple;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.logging.BaseLoggers;
/**
 * 
 * @author gajendra.jatav
 *
 * CustomDatabaseConnection will enable to add a table name filter to exclude some of tables while seeding. 
 */
public class CustomDatabaseConnection extends DatabaseConnection{

	
    private IDataSet dataSet = null;
	
	public CustomDatabaseConnection(Connection connection) throws DatabaseUnitException {
		super(connection);
	}

	public CustomDatabaseConnection(Connection connection, String schema, boolean validate)
			throws DatabaseUnitException {
		super(connection, schema, validate);
	}

	public CustomDatabaseConnection(Connection connection, String schema) throws DatabaseUnitException {
		super(connection, schema);
	}

    public IDataSet createDataSet() throws SQLException
    {
        BaseLoggers.flowLogger.debug("createDataSet() - start");
        ITableFilterSimple tableFilterSimple=NeutrinoSpringAppContextUtil.getBeanByName("tableFilterSimple", ITableFilterSimple.class);
        if(tableFilterSimple==null)
        {
        	BaseLoggers.flowLogger.error("tableFilterSimple not configured ");
        }
        if (dataSet == null)
        {
        	dataSet = new DatabaseDataSet(this,
					this.getConfig().getFeature(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES), tableFilterSimple);
        }

        return dataSet;
    }
	
}
