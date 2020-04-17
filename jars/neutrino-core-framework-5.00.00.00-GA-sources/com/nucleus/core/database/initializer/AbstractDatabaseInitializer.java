package com.nucleus.core.database.initializer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import com.nucleus.core.database.seed.operation.ResourceLoaderService;
import org.dbunit.DefaultOperationListener;
import org.dbunit.IDatabaseTester;
import org.dbunit.IOperationListener;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.operation.CustomDBUnitDatabaseOperation;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.nucleus.core.database.seed.audit.SeedDataAuditService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.logging.BaseLoggers;


/**
 * This class allows us to leverage the XML based database seeding with intelligent update/create/refresh of rows.<br/>
 * We can hook it up with spring to make things work in a very flexible and configurable way.
 * @see http://www.dbunit.org
 */

public abstract class AbstractDatabaseInitializer {

    private static List<String>    AVAILABLE_OPERATIONS = Arrays.asList(new String[] { "update", "refresh", "delete",
            "delete_all", "truncate_table", "insert", "none", "clean_insert", "safe_insert" });

    private DataSource             dataSource;
    protected Resource             resource;
    private String                 operation;
    private IDataTypeFactory       datatypeFactory;
    private boolean                synchronousExecution;
    private ThreadPoolTaskExecutor neutrinoThreadPoolExecutor;
    private String defaultSchemaName;

    private IColumnFilter          customPrimaryKeyFilter;

    private boolean turnSeedOperationsOn;
    private boolean consolidatedSeedOperation;

    
    @Inject
	@Named("seedDataAuditService")
	SeedDataAuditService seedDataAuditService;

    @Inject
    @Named("resourceLoaderService")
    private ResourceLoaderService resourceLoaderService;

     
    
    public void setTurnSeedOperationsOn(boolean turnSeedOperationsOn) {
		this.turnSeedOperationsOn = turnSeedOperationsOn;
	}
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void setResource(Resource resource) {
        this.resource = resource;
    }
    
    public String getDefaultSchemaName() {
		return defaultSchemaName;
	}

	public void setDefaultSchemaName(String defaultSchemaName) {
		this.defaultSchemaName = defaultSchemaName;
	}

	/**
     * @param synchronousCompile the synchronousCompile to set
     */
    public void setSynchronousExecution(boolean synchronousExecution) {
        this.synchronousExecution = synchronousExecution;
    }

    /**
     * @param neutrinoThreadPoolExecutor the neutrinoThreadPoolExecutor to set
     */
    public void setNeutrinoThreadPoolExecutor(ThreadPoolTaskExecutor neutrinoThreadPoolExecutor) {
        this.neutrinoThreadPoolExecutor = neutrinoThreadPoolExecutor;
    }

    /**
     * Set the operation to be performed at the time of initialization on xml.<br/>
     * Possble Value are: update, refresh, delete, delete_all, truncate_table, insert, none, clean_insert <br/>
     * <b>Note:</b> The default value is clean_insert
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setConsolidatedSeedOperation(boolean consolidatedSeedOperation) {
        this.consolidatedSeedOperation = consolidatedSeedOperation;
    }

    @PostConstruct
    protected void postContruct() {
        if(consolidatedSeedOperation) {
            List<Resource> resourceList = resourceLoaderService.getResourceList();
            for (Resource res : resourceList) {
                this.resource = res;
                validateAndStartDatabaseSeeding();
            }
        } else if(turnSeedOperationsOn){
            validateAndStartDatabaseSeeding();
        } else if(!consolidatedSeedOperation){
            /*if(validatedSeededTablesAndStopJVM()){
                BaseLoggers.flowLogger.error("******************JVM process stopped as the Seeding Operation is not successful****************");
                System.exit(0);
            }*/
        }

    }

    private Boolean validatedSeededTablesAndStopJVM(){
        List<String> resourceList = resourceLoaderService.getActiveSeededTablesList();
        validate();
        Connection con = null;
        Statement stmt = null;
        final ResultSet[] rs = {null};
        try {
            con = dataSource.getConnection();
            stmt = con.createStatement();
            Statement finalStmt = stmt;
            for (String tableName : resourceList) {
                String[] tempTable = tableName.split("\\.",-1);
                rs[0] = finalStmt.executeQuery("select count(*) as countVal from " + tempTable[0]);
                while (rs[0].next()) {
                    Long countVal = Long.parseLong(rs[0].getString("countVal"));
                    BaseLoggers.flowLogger.info("Table " + tempTable[0] +" has column count =" + rs[0].getString("countVal"));
                    if (countVal <= 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            BaseLoggers.flowLogger.error("******************Database error while seeding Operation****************");
        }finally{
            try {
                if(rs[0] != null) rs[0].close();
                if(stmt != null) stmt.close();
                if(con != null) con.close();
            } catch (SQLException e) {
                BaseLoggers.flowLogger.error("******************Connection Closing issue : Database error while seeding Operation.****************");
            }
        }
        return false;
    }

    private void validateAndStartDatabaseSeeding() {

        validate();
        // If async compilation is on and Executor is available (it might not be available in case of direct instantiation)
        if (!synchronousExecution && neutrinoThreadPoolExecutor != null) {
            neutrinoThreadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // If we are running in a different thread, do not throw exception and log it instead
                    BaseLoggers.flowLogger.debug("Asynchronously seeding the data for resource {}", resource);
                    executeDatabaseSeeding();
                }
            });
        } else {
            BaseLoggers.flowLogger.debug("Synchronously seeding the data for resource {}", resource);
            // For synchronous compilation
            executeDatabaseSeeding();
        }
    }

    protected void executeDatabaseSeeding() {
		
		boolean isSeedingOn = false;
		boolean isSeedingCompleted = false;

		IDataSet dataSet = null;
		IDatabaseTester databaseTester = new CustomDataSourceDatabaseTester(dataSource);
		try {
			BaseLoggers.flowLogger.error("******************* turnSeedOperationsOn = " + turnSeedOperationsOn
					+ " *************************");

			if (turnSeedOperationsOn) {
				databaseTester.setSetUpOperation(getSetUpOperation());
				
			} else {
				databaseTester.setSetUpOperation(DatabaseOperation.NONE);
				
			}
			
			if(turnSeedOperationsOn && (operation.equalsIgnoreCase("safe_insert") || operation.equalsIgnoreCase("insert") ||  operation.equalsIgnoreCase("clean_insert"))){
			    //  if(turnSeedOperationsOn && !(operation.equalsIgnoreCase("none"))){
			    isSeedingOn = true;
            }

			dataSet = createDataset();
			databaseTester.setDataSet(dataSet);
			databaseTester.setOperationListener(getOperationListener());
			databaseTester.onSetup();
			BaseLoggers.flowLogger.info(String.format(
					"Database \"%s\" operation successfully performed from resource: %s ", operation, resource));
			if(turnSeedOperationsOn && (operation.equalsIgnoreCase("safe_insert") || operation.equalsIgnoreCase("insert") ||  operation.equalsIgnoreCase("clean_insert")))
			    //if(turnSeedOperationsOn && !(operation.equalsIgnoreCase("none")))
			isSeedingCompleted = true;
			seedDataAuditService.logSeedData(resource, dataSet, isSeedingOn, isSeedingCompleted,databaseTester,operation,null,ProductInformationLoader.getProductCode());
		} catch (Exception e) {
			isSeedingCompleted = false;
			seedDataAuditService.logSeedData(resource, dataSet, isSeedingOn, isSeedingCompleted,databaseTester,operation,e,ProductInformationLoader.getProductCode());
			throw new SystemException("Problem while executing DB initialization from location: " + resource, e);
		}
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
                connection.getConfig().setProperty(DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, customPrimaryKeyFilter);
            }
        };
    }

    private void validate() {
        if (dataSource == null) {
            throw new SystemException("Data Source cannot be null.");
        }
        if (!consolidatedSeedOperation && turnSeedOperationsOn && resource == null) {
            throw new SystemException("Resource cannot be null");
        }
        if (!AVAILABLE_OPERATIONS.contains(operation)) {
            throw new SystemException("Cannot perform database operation as operation name: " + operation
                    + " is not a valid operation name. Possible operation names are: " + AVAILABLE_OPERATIONS);
        }
    }

    protected DatabaseOperation getSetUpOperation() throws Exception {
        if (operation.equalsIgnoreCase("update")) {
            return DatabaseOperation.UPDATE;
        } else if (operation.equalsIgnoreCase("refresh")) {
            return DatabaseOperation.REFRESH;
        } else if (operation.equalsIgnoreCase("delete")) {
            return DatabaseOperation.DELETE;
        } else if (operation.equalsIgnoreCase("delete_all")) {
            return DatabaseOperation.DELETE_ALL;
        } else if (operation.equalsIgnoreCase("truncate_table")) {
            return DatabaseOperation.TRUNCATE_TABLE;
        } else if (operation.equalsIgnoreCase("insert")) {
            return DatabaseOperation.INSERT;
        } else if (operation.equalsIgnoreCase("clean_insert")) {
            return DatabaseOperation.CLEAN_INSERT;
        } else if (operation.equalsIgnoreCase("safe_insert")) {
            return CustomDBUnitDatabaseOperation.SAFE_INSERT;
        } else {
            return DatabaseOperation.NONE;
        }
    }

    public void setDatatypeFactory(IDataTypeFactory datatypeFactory) {
        this.datatypeFactory = datatypeFactory;
    }

    public void setCustomPrimaryKeyFilter(IColumnFilter customPrimaryKeyFilter) {
        this.customPrimaryKeyFilter = customPrimaryKeyFilter;
    }

    abstract protected IDataSet createDataset() throws Exception;
}