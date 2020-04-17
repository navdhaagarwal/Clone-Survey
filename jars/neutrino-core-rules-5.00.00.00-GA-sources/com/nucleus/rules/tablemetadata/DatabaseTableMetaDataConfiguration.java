package com.nucleus.rules.tablemetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import com.nucleus.core.formsConfiguration.validationcomponent.Tuple_2;
import com.nucleus.service.BaseService;

@Component("databaseTableMetaDataConfiguration")
public class DatabaseTableMetaDataConfiguration implements BaseService{

	@Inject
	@Named("dataSource")
	private DataSource ds;
	
	// to hold list of all tables whoes metadata to be extract
	private  List<String> rootTablesForMetadata = Arrays.asList(new String[]{"base_assignment_master"});
	
	// to hold java regex for table name exclusion like CAS_*_MST|RULE*
	private  String exclusionTableNamePattern = "{CAS_*_MST|RULE*}";
	
	// to hold list of all column that to be excluse from all tables
	private  List<String> exclusionAllTableColumns = new ArrayList<String>();
	
	// to hold pair ot table name and column name to be excluse
	private  List<Tuple_2> exclusionTableColumnInfo = new ArrayList<>();
	
	// to add business vise fk -> like long of subload id act like FK for subloan table only
	private  Map<String,List<ManualFKConfiguration>> manualFKConfig =  new HashMap<>();
	
	// to hold help text for a column with table name
	private  Map<Tuple_2,String> columnHelpText = new HashMap<>();
	
	// to hold help text table wise
	private  Map<String,String> tableHelpText = new HashMap<>();
	
	private  Set<String> sqlAnsiKeyWords = new HashSet<String>(Arrays.asList(new String[]{"ALL",
			"ALTER",
			"AND",
			"ANY",
			"AS",
			"ASC",
			"BETWEEN",
			"BY",
			"CASE",
			"CAST",
			"CHECK",
			"CLUSTER",
			"COLUMN",
			"CONNECT",
			"CREATE",
			"CROSS",
			"CURRENT",
			"DELETE",
			"DESC",
			"DISTINCT",
			"DROP",
			"ELSE",
			"EXCLUSIVE",
			"EXISTS",
			"FALSE",
			"FOR",
			"FROM",
			"FULL",
			"GRANT",
			"GROUP",
			"HAVING",
			"IDENTIFIED",
			"ILIKE",
			"IMMEDIATE",
			"IN",
			"INCREMENT",
			"INNER",
			"INSERT",
			"INTERSECT",
			"INTO",
			"IS",
			"JOIN",
			"LATERAL",
			"LEFT",
			"LIKE",
			"LOCK",
			"LONG",
			"MAXEXTENTS",
			"MINUS",
			"MODIFY",
			"NATURAL",
			"NOT",
			"NULL",
			"OF",
			"ON",
			"OPTION",
			"OR",
			"ORDER",
			"REGEXP",
			"RENAME",
			"REVOKE",
			"RIGHT",
			"RLIKE",
			"ROW",
			"ROWS",
			"SAMPLE",
			"SELECT",
			"SET",
			"SOME",
			"START",
			"TABLE",
			"TABLESAMPLE",
			"THEN",
			"TO",
			"TRIGGER",
			"TRUE",
			"UNION",
			"UNIQUE",
			"UPDATE",
			"USING",
			"VALUES",
			"VIEW",
			"WHEN",
			"WHENEVER",
			"WHERE",
			"WITH",
}));
	
	private Map<Integer,List<String>> operatorByType;
	
	private Map<Integer,List<String>> sqlFunctionByType;
	
	
	public boolean isTableFromExcluseList(String tableName){
		//TODO
		return false;
	}
	
	public boolean isColumnFromExclusionList(String tableName,String columnName){
		if(exclusionAllTableColumns.indexOf(columnName) > -1){
			return true;
		}
		if(exclusionTableColumnInfo.indexOf(new Tuple_2(tableName, columnName)) > -1){
			return true;
		}
		return false;
	}

	public String getExclusionTableNamePattern() {
		return exclusionTableNamePattern;
	}

	public List<String> getExclusionAllTableColumns() {
		return exclusionAllTableColumns;
	}

	public List<Tuple_2> getExclusionTableColumnInfo() {
		return exclusionTableColumnInfo;
	}

	public List<ManualFKConfiguration> getManualFKConfig(String tableName) {
		return manualFKConfig.get(tableName);
	}

	public List<String> getRootTablesForMetadata() {
		return rootTablesForMetadata;
	}

	public String getHelpTextForColumn(String tableName,String columnname){
		return columnHelpText.get(new Tuple_2(tableName, columnname));
	}
	
	public String getHelpTextFortable(String tableName){
		return tableHelpText.get(tableName);
	}

	public Set<String> getSqlAnsiKeyWords() {
		return sqlAnsiKeyWords;
	}
	
	public boolean isSqlKeyWords(String keyword) {
		return sqlAnsiKeyWords.contains(keyword.toUpperCase());
	}
	
	public Map<Integer, List<String>> getOperatorByType() {
		return operatorByType;
	}

	public void initConfiguration() throws SQLException{

		try(Connection connection=ds.getConnection()){
			DatabaseMetaData metaData =  connection.getMetaData();
			// loading sql keywords
			sqlAnsiKeyWords.addAll(new HashSet<String>(
					Arrays.asList(Optional.ofNullable(metaData.getSQLKeywords()).orElse("").split(","))));
			List<String> numericFunction = Arrays.asList(metaData.getNumericFunctions().split(","));
			List<String> varcharFunction = Arrays.asList(metaData.getStringFunctions().split(","));
			List<String> dateTimeFunction = Arrays.asList(metaData.getTimeDateFunctions().split(","));

			sqlFunctionByType.put(Types.BIGINT, numericFunction);
			sqlFunctionByType.put(Types.DECIMAL, numericFunction);
			sqlFunctionByType.put(Types.NUMERIC, numericFunction);
			sqlFunctionByType.put(Types.DECIMAL, numericFunction);
			sqlFunctionByType.put(Types.INTEGER, numericFunction);
			sqlFunctionByType.put(Types.DOUBLE, numericFunction);
			sqlFunctionByType.put(Types.FLOAT, numericFunction);
			sqlFunctionByType.put(Types.SMALLINT, numericFunction);

			sqlFunctionByType.put(Types.NVARCHAR, varcharFunction);
			sqlFunctionByType.put(Types.VARCHAR, varcharFunction);
			sqlFunctionByType.put(Types.VARBINARY, varcharFunction);

			sqlFunctionByType.put(Types.TIMESTAMP, dateTimeFunction);
			sqlFunctionByType.put(Types.DATE, dateTimeFunction);
		}
	}
	
}
