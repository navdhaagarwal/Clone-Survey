package com.nucleus.rules.tablemetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.exception.RuleException;
import com.nucleus.service.BaseService;

@Component("databaseTableMetaDataExtractorEngine")
public class DatabaseTableMetaDataExtractorEngine implements BaseService {

	private static Map<String, TableInfo> allLoadedTables = new ConcurrentHashMap<>();

	@Inject
	@Named("dataSource")
	private DataSource ds;

	@Inject
	@Named("databaseTableMetaDataConfiguration")
	private DatabaseTableMetaDataConfiguration metaConfig;

	public Map<String, TableInfo> getAllLoadedTables() {
		if (allLoadedTables.isEmpty()) {
			throw new RuleException("Table Meta data Cache Not created");
		}
		return allLoadedTables;
	}

	public void start(List<String> rootTables) {
		if (!CollectionUtils.isEmpty(rootTables)) {
			List<String> notLoadedList = new ArrayList<>();
			// check if all tables are present
			for (String rootTable : rootTables) {
				if(allLoadedTables.get(rootTable)==null){
					notLoadedList.add(rootTable);	
				}
			}
			if (!notLoadedList.isEmpty()) {
				reStart(notLoadedList);
			}
		}
	}

	public void reStart(List<String> rootTables) {
		BaseLoggers.flowLogger.info("Table Metadata Extractor Engine start");
		if (!CollectionUtils.isEmpty(rootTables)) {
			try (Connection connection = ds.getConnection()){
				DatabaseMetaData metaData = connection.getMetaData();
				for (String t : rootTables) {
					getTableInfoByName(t, metaData);
				}
			} catch (SQLException e) {
				BaseLoggers.exceptionLogger.error("Error in loading table Metadata", e);
			}
		}
		BaseLoggers.flowLogger.info("Table Metadata Extractor Engine End");
	}

	public TableInfo getTableInfoByName(String tableName, DatabaseMetaData metaData) throws SQLException {
		TableInfo presentTable = allLoadedTables.get(tableName);
		if(presentTable == null){
			return createTableInfoByName(tableName, metaData);
		}
		return presentTable;
	//	return Optional.ofNullable(allLoadedTables.get(tableName)).orElse(createTableInfoByName(tableName, metaData));
	}

	private TableInfo createTableInfoByName(String tableName, DatabaseMetaData metaData) throws SQLException {
		TableInfo t = new TableInfo(tableName);
		allLoadedTables.put(tableName, t);
		if (metaConfig.isTableFromExcluseList(tableName)) {
			return t;
		} else {
			t.setColumns(getAllColumns(tableName, metaData));
			t.setFkInfo(getAllFksInfo(tableName, metaData));
			t.setHelpText(metaConfig.getHelpTextFortable(tableName));
			List<ManualFKConfiguration> manualFks = metaConfig.getManualFKConfig(tableName);
			if (!CollectionUtils.isEmpty(manualFks)) {
				for (ManualFKConfiguration manualFKConfiguration : manualFks) {
					t.addFkInfo(convertToStandartFkInfo(manualFKConfiguration, tableName, metaData));
				}
			}
		}
		return t;
	}

	private ForeignKeyInfo convertToStandartFkInfo(ManualFKConfiguration manualFK, String tableName,
			DatabaseMetaData metaData) throws SQLException {
		ForeignKeyInfo fkInfo = new ForeignKeyInfo();
		fkInfo.setKeyName("Manually Added");
		fkInfo.setReferenceTable(getTableInfoByName(manualFK.getReferenceTableName(), metaData));
		fkInfo.setReferenceTableColumn(
				fkInfo.getReferenceTable().getColumnInfoByName(manualFK.getReferenceTableColumnName()));
		fkInfo.setSourceTable(getTableInfoByName(tableName, metaData));
		fkInfo.setSourceTableColumn(fkInfo.getSourceTable().getColumnInfoByName(manualFK.getSourceColumn()));
		reverseFKInfo("Manually Added", fkInfo);
		return fkInfo;
	}

	private List<ColumnInfo> getAllColumns(String tableName, DatabaseMetaData metaData) throws SQLException {
		List<ColumnInfo> cols = new ArrayList<>();
		try {
			ResultSet rs = metaData.getColumns(null, null, tableName, "");
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				if (metaConfig.isColumnFromExclusionList(tableName, columnName)) {
					continue;
				}
				int columnDataType = rs.getInt("DATA_TYPE");
				ColumnInfo colInfo = new ColumnInfo(columnName, columnDataType);
				colInfo.setTableName(tableName);
				colInfo.setHelpDesc(metaConfig.getHelpTextForColumn(tableName, columnName));
				cols.add(colInfo);
			}
		} catch (SQLException e) {
			BaseLoggers.webLogger.error("Error in extracting column of Table:" + tableName, e);
			throw e;
		}
		return cols;
	}

	private List<ForeignKeyInfo> getAllFksInfo(String tableName, DatabaseMetaData metaData) throws SQLException {
		List<ForeignKeyInfo> fks = new ArrayList<>();
		try {
			ResultSet rs = metaData.getImportedKeys(null, null, tableName);
			while (rs.next()) {
				String fkName = rs.getString("FK_NAME");
				String referenceTableName = rs.getString("PKTABLE_NAME");
				if(referenceTableName.equals(tableName)){
					continue;
				}
				String referenceTableColumn = rs.getString("PKCOLUMN_NAME");
				String selfColumn = rs.getString("FKCOLUMN_NAME");
				ForeignKeyInfo fkInfo = new ForeignKeyInfo();
				fkInfo.setKeyName(fkName);
				fkInfo.setReferenceTable(getTableInfoByName(referenceTableName, metaData));
				fkInfo.setReferenceTableColumn(fkInfo.getReferenceTable().getColumnInfoByName(referenceTableColumn));
				fkInfo.setSourceTable(allLoadedTables.get(tableName));
				fkInfo.setSourceTableColumn(fkInfo.getSourceTable().getColumnInfoByName(selfColumn));
				reverseFKInfo(fkName, fkInfo);
				fks.add(fkInfo);
			}
		} catch (SQLException e) {
			BaseLoggers.webLogger.error("Error in extracting FK of Table:" + tableName, e);
			throw e;
		}
		return fks;
	}

	private void reverseFKInfo(String fkName, ForeignKeyInfo fkInfo) {
		ForeignKeyInfo fkInfoforDesTable = new ForeignKeyInfo();
		fkInfoforDesTable.setKeyName(fkName);
		fkInfoforDesTable.setReferenceTable(fkInfo.getSourceTable());
		fkInfoforDesTable.setReferenceTableColumn(fkInfo.getSourceTableColumn());
		fkInfoforDesTable.setSourceTable(fkInfo.getReferenceTable());
		fkInfoforDesTable.setSourceTableColumn(fkInfo.getReferenceTableColumn());
		fkInfoforDesTable.getSourceTable().addFkInfo(fkInfoforDesTable);
	}
}
