package com.nucleus.synonym.util;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.nucleus.logging.BaseLoggers;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.nucleus.synonym.metadata.pojo.UserObject;

public class MetaDataHolderUtil {
	
	private static Map<String, List<String>> databaseMetaData = new HashMap<String, List<String>>();
	
	//Method responsible to fetch metadata of data source based on that script of drop, grant and synonyms will be prepared. 
	public static void fetchAvailableMetaData(DataSource dataSource, String dbOrigin) {
		List<String> availableSynonyms = new ArrayList<String>();
		List<String> availableTables = new ArrayList<String>();
		List<String> availableSequence = new ArrayList<String>();
		
		String query = "select OBJECT_NAME,OBJECT_TYPE from user_objects where object_type in (:objectTypes) order by OBJECT_NAME, OBJECT_TYPE";
		NamedParameterJdbcTemplate jdbcTemplate=new NamedParameterJdbcTemplate(dataSource);
		//JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<UserObject> userObjects = jdbcTemplate.query(query ,Collections.singletonMap("objectTypes", Arrays.asList("SYNONYM","TABLE","SEQUENCE")), new UserObjectMapper());
		
		for(UserObject userObject : userObjects){
			if(userObject.getDbObjectType() != null){
            	if("SYNONYM".equalsIgnoreCase(userObject.getDbObjectType())){
            		availableSynonyms.add(userObject.getDbObjectName());
            	}else if("TABLE".equalsIgnoreCase(userObject.getDbObjectType())){
            		availableTables.add(userObject.getDbObjectName());
            	}else if("SEQUENCE".equalsIgnoreCase(userObject.getDbObjectType())){
            		availableSequence.add(userObject.getDbObjectName());
            	}
            }
		}
        databaseMetaData.put(dbOrigin+"_SYNONYM", availableSynonyms);
        databaseMetaData.put(dbOrigin+"_TABLE", availableTables);
        databaseMetaData.put(dbOrigin+"_SEQUENCE", availableSequence);
	}

	public static void fetchAvailableMetaDataPostgres(DataSource dataSource, String dbOrigin,String schemaName) {
		if (schemaName==null){
			try (Connection connection = dataSource.getConnection()){
				schemaName = connection.getSchema();
			} catch (Exception sqle) {
				BaseLoggers.exceptionLogger.error("Could not resolve schemaName ",sqle);
			}
		}
		List<String> availableSynonyms = new ArrayList<String>();
		List<String> availableTables = new ArrayList<String>();
		List<String> availableSequence = new ArrayList<String>();
		
		String sequenceQuery = "select sequence_name,sequence_schema from information_schema.sequences  where sequence_schema=?";

		String tableQuery = "select table_name,table_type from information_schema.tables where table_schema=?";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<UserObject> sequences = jdbcTemplate.query(sequenceQuery,new Object[]{schemaName}, new UserObjectMapper());

		List<UserObject> tables = jdbcTemplate.query(tableQuery,new Object[]{schemaName}, new UserObjectMapper());

		for(UserObject sequence : sequences)
		{
      		availableSequence.add(sequence.getDbObjectName());
		}
		for(UserObject table : tables)
		{
			availableTables.add(table.getDbObjectName());
		}

		databaseMetaData.put(dbOrigin+"_SYNONYM", availableSynonyms);
        databaseMetaData.put(dbOrigin+"_TABLE", availableTables);
        databaseMetaData.put(dbOrigin+"_SEQUENCE", availableSequence);
	}

	
	public static List<String> getMetaData(String objectType){
		if(CollectionUtils.isNotEmpty(databaseMetaData.get(objectType))){
			return databaseMetaData.get(objectType);
		}else{
			return new ArrayList<String>();
		}
		
	}

}
