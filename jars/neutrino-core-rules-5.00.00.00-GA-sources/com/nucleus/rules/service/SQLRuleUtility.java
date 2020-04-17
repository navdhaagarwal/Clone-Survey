package com.nucleus.rules.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLRuleUtility {

	public static final String ORACLE = "oracle";
	public static final String POSTGRES = "postgres";
	public static final String MYSQL = "mysql";
	public static final String SQLSERVER = "sqlserver";
	
	
	public static final String ALTER = "ALTER";
	public static final String DROP = "DROP";
	public static final String TRUNCATE = "TRUNCATE";
	public static final String CREATE = "CREATE";
	public static final String UPDATE = "UPDATE";
	public static final String DELETE = "DELETE";
	
	public static final String SELECT = "SELECT";
	
	
	private static final Map<String,List<String>> excludedKeywords = new HashMap<>();
	
	static{
		List<String> excluseForAllDB = Arrays.asList(new String[]{ALTER,DROP,TRUNCATE,CREATE,UPDATE,DELETE});
		excludedKeywords.put(ORACLE, excluseForAllDB);
		excludedKeywords.put(POSTGRES, excluseForAllDB);
		excludedKeywords.put(MYSQL, excluseForAllDB);
		excludedKeywords.put(SQLSERVER, excluseForAllDB);
		
	}

	public static List<String> getExcludedkeywordsByDB(String dataBaseType) {
		return excludedKeywords.get(dataBaseType);
	}
	
	
	
}
