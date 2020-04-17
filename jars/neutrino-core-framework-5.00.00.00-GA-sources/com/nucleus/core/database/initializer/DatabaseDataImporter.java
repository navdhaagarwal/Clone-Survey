/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.database.initializer;

import java.io.File;
import java.io.FileInputStream;

import org.dbunit.DefaultOperationListener;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.operation.TransactionOperation;

/**
 * Imports the data from specified file into database
 * @author Nucleus Software Exports Limited
 */
public class DatabaseDataImporter {

    private static String            driverClassName = "com.mysql.jdbc.Driver";
    private static String            url             = "jdbc:mysql://localhost:3306/fo-test";
    private static String            dataFilePath    = "target/exported_db_unit_data.xml";
    private static DatabaseOperation operation       = DatabaseOperation.UPDATE;
    private static IDataTypeFactory  datatypeFactory = new MySqlDataTypeFactory();

    public static void main(String[] args) throws Exception {
    	String            username        = args[0];
    	String            password        = args[1];
        IDatabaseTester databaseTester = new JdbcDatabaseTester(driverClassName, url, username, password);
        TransactionOperation transactionWrappedOperation = new TransactionOperation(operation);
        databaseTester.setSetUpOperation(transactionWrappedOperation);
        databaseTester.setDataSet(createDataset());
        databaseTester.setOperationListener(new DefaultOperationListener() {
            @Override
            public void connectionRetrieved(IDatabaseConnection connection) {
                super.connectionRetrieved(connection);
                connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, datatypeFactory);
                connection.getConfig().setProperty("http://www.dbunit.org/properties/primaryKeyFilter",
                        createPrimaryKeyFilter());
            }
        });

        databaseTester.onSetup();

    }

    private static IColumnFilter createPrimaryKeyFilter() {
        return new IColumnFilter() {
            @Override
            public boolean accept(String tableName, Column column) {
                boolean hibernateTable = tableName.equalsIgnoreCase("hibernate_sequence")
                        && column.getColumnName().equalsIgnoreCase("next_val");
                boolean activitiTable = tableName.toLowerCase().startsWith("act_")
                        && column.getColumnName().equalsIgnoreCase("id_");
                boolean activitiPropertyTable = tableName.toLowerCase().startsWith("act_ge_property")
                        && column.getColumnName().equalsIgnoreCase("name_");
                boolean idColumn = column.getColumnName().equalsIgnoreCase("id");
                return hibernateTable || activitiTable || activitiPropertyTable || idColumn;
            }
        };
    }

    private static IDataSet createDataset() throws Exception {
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        builder.setColumnSensing(true);
        return builder.build(new FileInputStream(new File(dataFilePath)));
    }

}