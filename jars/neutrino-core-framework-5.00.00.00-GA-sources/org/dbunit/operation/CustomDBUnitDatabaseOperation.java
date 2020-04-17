package org.dbunit.operation;

import com.nucleus.core.database.seed.viewobject.CsvDataVO;
import com.nucleus.core.misc.util.StringUtil;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.logging.BaseLoggers;
import org.apache.commons.lang.StringUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.statement.IPreparedBatchStatement;
import org.dbunit.database.statement.SimplePreparedStatement;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoPrimaryKeyException;
import org.dbunit.dataset.RowFilterTable;
import org.dbunit.dataset.RowOutOfBoundsException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.IRowFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This operation inserts dataset contents into the database if the primary key is absent in the database.
 * This means that data of existing rows is not updated and non-existing row get
 * inserted. Any rows which exist in the database but not in dataset stay unaffected.
 */

public class CustomDBUnitDatabaseOperation extends AbstractOperation {

    public static final DatabaseOperation SAFE_INSERT = new CustomDBUnitDatabaseOperation();

    /**
     * Logger for this class
     */
    private static final Logger           logger      = LoggerFactory.getLogger(CustomDBUnitDatabaseOperation.class);

    private final InsertOperation         _insertOperation;

    CustomDBUnitDatabaseOperation() {
        _insertOperation = (InsertOperation) DatabaseOperation.INSERT;
    }

    private boolean isEmpty(ITable table) throws DataSetException {
        return AbstractBatchOperation.isEmpty(table);
    }

    // //////////////////////////////////////////////////////////////////////////
    // DatabaseOperation class

    @Override
    public void execute(IDatabaseConnection connection, IDataSet dataSet) throws DatabaseUnitException, SQLException {
        logger.debug("execute(connection={}, dataSet) - start", connection);

        // for each table
        ITableIterator iterator = dataSet.iterator();
        connection.getConfig().setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
        while (iterator.next()) {
            ITable table = iterator.getTable();

            // Do not process empty table
            if (isEmpty(table)) {
                logger.info("Dataset for table {} is EMPTY - SKIPPING Seeding", table.getTableMetaData().getTableName());
                continue;
            }
            logger.info("BEGINNING seeding operation  for TABLE {} ", table.getTableMetaData().getTableName());
            ITableMetaData metaData = getOperationMetaDataLocal(connection, table.getTableMetaData());
            RowOperation rowExistOperation = new RowExistOperation(connection, metaData);
            RowOperation insertRowOperation = new InsertRowOperation(connection, metaData);
            
            boolean rowExists=true;
            int i = 0;

            table = filterTable(table);
            do {
                try {
                    if(!rowExistOperation.execute(table, i)){
					    insertRowOperation.execute(table, i);
					}						
				} catch (RowOutOfBoundsException e) {
					rowExists = false;
					// This exception occurs when records are exhausted
					// and we reach the end of the table. Ignore this error.
					logger.info("SUCCESSFULLY seeded TABLE {} ", table.getTableMetaData().getTableName());
					// end of table
				} finally {
					if (!rowExists) {
						rowExistOperation.close();
						insertRowOperation.close();
					}

				}
				i++;
			} while (rowExists);
        }

    }

    /**
     *
     * @param connection
     * @param metaData
     * @return
     * @throws DatabaseUnitException
     * @throws SQLException
     * Change this method if changing dbunit version
     */
    private ITableMetaData getOperationMetaDataLocal(IDatabaseConnection connection,
                                               ITableMetaData metaData) throws DatabaseUnitException, SQLException
    {
        logger.debug("getOperationMetaDataLocal(connection={}, metaData={}) - start", connection, metaData);
        IDataSet databaseDataSet = connection.createDataSet();
        String tableName = metaData.getTableName();
        if(CsvDataVO.schemaNameMap.get(tableName.toUpperCase()) != null) {
            tableName = CsvDataVO.schemaNameMap.get(tableName.toUpperCase()) + "." + tableName;
        } else {
            tableName = CsvDataVO.SCHEMA_NAME + "." + tableName;
        }
        ITableMetaData tableMetaData = databaseDataSet.getTableMetaData(tableName);
        Column[] columns = metaData.getColumns();

        List columnList = new ArrayList();
        for (int j = 0; j < columns.length; j++)
        {
            String columnName = columns[j].getColumnName();
            // Check if column exists in database
            // method "getColumnIndex()" throws NoSuchColumnsException when columns have not been found
            int dbColIndex = tableMetaData.getColumnIndex(columnName);
            // If we get here the column exists in the database
            Column dbColumn = tableMetaData.getColumns()[dbColIndex];
            columnList.add(dbColumn);
        }

        return new DefaultTableMetaData(tableMetaData.getTableName(),
                (Column[])columnList.toArray(new Column[0]),
                tableMetaData.getPrimaryKeys());
    }

    public ITable filterTable(ITable table){
        String tableName = table.getTableMetaData().getTableName().toLowerCase();
        if(CsvDataVO.filterMap.containsKey(tableName)) {
            Map<String, List<Long>> rangeMap = CsvDataVO.filterMap.get(tableName);
            IRowFilter rowFilter = rowValueProvider -> {
                AtomicBoolean omitFlag = new AtomicBoolean(false);
                rangeMap.forEach((k, v) -> {
                    try {
                        Object columnValue = rowValueProvider.getColumnValue(k);
                        Long currentRowVal =  ((BigDecimal)columnValue).longValue();
                        List<Long> rangeList = rangeMap.get(k);
                        for(int i=0;i<rangeList.size(); i+=2) {
                            if((currentRowVal >= rangeList.get(i)) && (currentRowVal <= rangeList.get(i+1))){
                                omitFlag.set(true);
                                break;
                            }
                        }
                    } catch (DataSetException e) {
                        BaseLoggers.exceptionLogger.error("Some error occurred accessing {} of {}", k, tableName);
                    }
                });
                return omitFlag.get();
            };
            try {
                return new RowFilterTable(table, rowFilter);
            } catch (DataSetException e) {
                BaseLoggers.exceptionLogger.error("Error creating Row filter ", e);
                throw new SystemException(e);
            }
        }
        return table;
    }

    /**
     * This class represents a operation executed on a single table row.
     */
    class RowOperation {

        /**
         * Logger for this class
         */
        private final Logger              logger = LoggerFactory.getLogger(RowOperation.class);

        protected IPreparedBatchStatement _statement;
        protected OperationData           _operationData;
        protected BitSet                  _ignoreMapping;

        /**
         * Execute this operation on the sepcified table row.
         * @return <code>true</code> if operation have been executed on the row.
         */
        public boolean execute(ITable table, int row) throws DataSetException, SQLException {
            logger.debug("execute(table={}, row={}) - start", table, String.valueOf(row));

            Column[] columns = _operationData.getColumns();
            for (int i = 0 ; i < columns.length ; i++) {
                // Bind value only if not in ignore mapping
                if (_ignoreMapping == null || !_ignoreMapping.get(i)) {
                    Object value = null;
                    try {
                        value = table.getValue(row, columns[i].getColumnName());
                        if (value instanceof String && StringUtils.isEmpty((String)value)){
                            value = null;
                        }
                    } catch (Exception e) {
                        if (!RowOutOfBoundsException.class.isAssignableFrom(e.getClass())) {
                            BaseLoggers.bugLogger.error(e.getClass() + " with message {" + e.getMessage()
                                    + "} occurred while fetching value for table: "
                                    + table.getTableMetaData().getTableName() + ", row: " + row + ", column: " + columns[i]);
                        }
                        if (DataSetException.class.isAssignableFrom(e.getClass())) {
                            throw (DataSetException) e;
                        } else {
                            throw (RuntimeException) e;
                        }
                    }
                    _statement.addValue(value, columns[i].getDataType());
                }
            }
            int result = 0;
            try {
                _statement.addBatch();
                result = _statement.executeBatch();
            } catch (SQLException e) {
                BaseLoggers.bugLogger.error("SQLException with message {" + e.getMessage()
                        + "} occurred while executing/adding sql batch statements for table: "
                        + table.getTableMetaData().getTableName() + ", row: " + row);
                throw e;
            }
            _statement.clearBatch();

            return result == 1;
        }

        /**
         * Cleanup this operation state.
         */
        public void close() throws SQLException {
            logger.debug("close() - start");

            if (_statement != null) {
                _statement.close();
            }
        }
    }

    /**
     * Insert row operation.
     */
    private class InsertRowOperation extends RowOperation {

        /**
         * Logger for this class
         */
        private final Logger              logger = LoggerFactory.getLogger(InsertRowOperation.class);

        private final IDatabaseConnection _connection;
        private final ITableMetaData      _metaData;

        public InsertRowOperation(IDatabaseConnection connection, ITableMetaData metaData) throws DataSetException,
                SQLException {
            _connection = connection;
            _metaData = metaData;
        }

        @Override
        public boolean execute(ITable table, int row) throws DataSetException, SQLException {
            logger.debug("execute(table={}, row={}) - start", table, String.valueOf(row));

            // If current row has a different ignore value mapping than
            // previous one, we generate a new statement
            if (_ignoreMapping == null || !_insertOperation.equalsIgnoreMapping(_ignoreMapping, table, row)) {
                // Execute and close previous statement
                if (_statement != null) {
                    _statement.close();
                }

                _ignoreMapping = _insertOperation.getIgnoreMapping(table, row);
                _operationData = _insertOperation.getOperationData(_metaData, _ignoreMapping, _connection);
                _statement = new SimplePreparedStatement(_operationData.getSql(), _connection.getConnection());
            }

            return super.execute(table, row);
        }

    }

    /**
     * This operation verify if a row exists in the database.
     */
    private class RowExistOperation extends RowOperation {

        /**
         * Logger for this class
         */
        private final Logger logger = LoggerFactory.getLogger(RowExistOperation.class);

        PreparedStatement    _countStatement;

        public RowExistOperation(IDatabaseConnection connection, ITableMetaData metaData) throws DataSetException,
                SQLException {
            // setup select count statement
            _operationData = getSelectCountData(metaData, connection);
            _countStatement = connection.getConnection().prepareStatement(_operationData.getSql());
        }

        private OperationData getSelectCountData(ITableMetaData metaData, IDatabaseConnection connection)
                throws DataSetException {
            logger.debug("getSelectCountData(metaData={}, connection={}) - start", metaData, connection);

            Column[] primaryKeys = metaData.getPrimaryKeys();

            // cannot construct where clause if no primary key
            if (primaryKeys.length == 0) {
                throw new NoPrimaryKeyException(metaData.getTableName());
            }

            // select count
            StringBuffer sqlBuffer = new StringBuffer(128);
            sqlBuffer.append("select COUNT(*) from ");
            sqlBuffer.append(getQualifiedName(connection.getSchema(), metaData.getTableName(), connection));

            // where
            sqlBuffer.append(" where ");
            for (int i = 0 ; i < primaryKeys.length ; i++) {
                Column column = primaryKeys[i];

                if (i > 0) {
                    sqlBuffer.append(" and ");
                }
                sqlBuffer.append(getQualifiedName(null, column.getColumnName(), connection));
                sqlBuffer.append(" = ?");
            }

            return new OperationData(sqlBuffer.toString(), primaryKeys);
        }

        // //////////////////////////////////////////////////////////////////////
        // RowOperation class

        /**
         * Verify if the specified table row exists in the database.
         * @return <code>true</code> if row exists.
         */
        @Override
        public boolean execute(ITable table, int row) throws DataSetException, SQLException {
            logger.debug("execute(table={}, row={}) - start", table, String.valueOf(row));

            Column[] columns = _operationData.getColumns();
            for (int i = 0 ; i < columns.length ; i++) {
                Object value = null;
                try {
                    value = table.getValue(row, columns[i].getColumnName());
                } catch (Exception e) {
                    if (!RowOutOfBoundsException.class.isAssignableFrom(e.getClass())) {
                        BaseLoggers.bugLogger.error(e.getClass() + " with message {" + e.getMessage()
                                + "} occurred while fetching value for table: " + table.getTableMetaData().getTableName()
                                + ", row: " + row + ", column: " + columns[i]);
                    }
                    if (DataSetException.class.isAssignableFrom(e.getClass())) {
                        throw (DataSetException) e;
                    } else {
                        throw (RuntimeException) e;
                    }
                }
                DataType dataType = columns[i].getDataType();
                dataType.setSqlValue(value, i + 1, _countStatement);
            }

            ResultSet resultSet = _countStatement.executeQuery();
            try {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            } finally {
                resultSet.close();
            }
        }

        @Override
        public void close() throws SQLException {
            logger.debug("close() - start");

            _countStatement.close();
        }
    }

}
