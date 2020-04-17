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

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.database.search.TablesDependencyHelper;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;

/**
 * Exports the data from database into specified file
 * @author Nucleus Software Exports Limited
 */
public class DatabaseDataExporter {

    private static String           driverClassName = "com.mysql.jdbc.Driver";
    private static String           url             = "jdbc:mysql://localhost:3306/fo";
    private static String           exportFileName  = "exported_db_unit_data.xml";
    private static IDataTypeFactory datatypeFactory = new MySqlDataTypeFactory();

    public static void main(String[] args) throws Exception {
        // database connection
        Class<?> driverClass = Class.forName(driverClassName);
        String            username        = args[0];
    	String            password        = args[1];
        Connection jdbcConnection = DriverManager.getConnection(url, username, password);
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);
        connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, datatypeFactory);

        // partialDataExportWithAllDependentData(connection, "simple_contact_info");
        // partialDataExport(connection);
        fullDataExport(connection);

    }

    private static void partialDataExport(IDatabaseConnection connection) throws Exception {
        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("Users", "SELECT * FROM users WHERE persistence_status=1");
        partialDataSet.addTable("Customer");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("target" + SystemUtils.FILE_SEPARATOR + exportFileName));
    }

    private static void fullDataExport(IDatabaseConnection connection) throws Exception {
        // full database export
        List<String> tableNames = Arrays.asList(connection.createDataSet().getTableNames());
        List<String> tablesToBeIncluded = new ArrayList<String>(tableNames);
        for (String tableName : tableNames) {
            if (tableName.toLowerCase().startsWith("act_ge") || tableName.toLowerCase().startsWith("act_hi")
                    || tableName.toLowerCase().startsWith("act_id") || tableName.toLowerCase().startsWith("act_re")
                    || tableName.toLowerCase().startsWith("act_ru")) {
                tablesToBeIncluded.remove(tableName);
            }
        }
        IDataSet fullDataSet = connection.createDataSet(tableNames.toArray(new String[] {}));
        XlsDataSet.write(fullDataSet, new FileOutputStream("target" + SystemUtils.FILE_SEPARATOR + exportFileName));
    }

    private static void partialDataExportWithAllDependentData(IDatabaseConnection connection, String... tableNames)
            throws Exception {
        // dependent tables database export: export table X and all tables that have a PK which is a FK on X, in the right
        // order for insertion
        if (ArrayUtils.isEmpty(tableNames)) {
            return;
        }
        Set<String> tableNameSet = new LinkedHashSet<String>();
        for (String tableName : tableNames) {
            tableNameSet.addAll(Arrays.asList(TablesDependencyHelper.getAllDependentTables(connection, tableName)));
        }
        IDataSet depDataset = connection.createDataSet(new ArrayList<String>(tableNameSet).toArray(new String[] {}));
        FlatXmlDataSet.write(depDataset, new FileOutputStream("target" + SystemUtils.FILE_SEPARATOR + exportFileName));
    }
}