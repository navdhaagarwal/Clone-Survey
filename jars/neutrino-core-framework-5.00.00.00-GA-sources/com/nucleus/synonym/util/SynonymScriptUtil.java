package com.nucleus.synonym.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nucleus.core.dbmapper.util.DataBaseMappingUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.synonym.metadata.pojo.SynonymScriptMetadata;

public class SynonymScriptUtil {
	
	public static Logger synonymLogger = LoggerFactory.getLogger(SynonymScriptUtil.class);
	
	 public static final int DEFAULT_LENGTH = 26;
	
	public static String generateDropScript(List<SynonymScriptMetadata> synonymMetaData,String schemaName){
		StringBuffer dropScriptBuffer = null;
		if(CollectionUtils.isNotEmpty(synonymMetaData)){
			synonymLogger.info("************ Generated DROP Script ************");
			Set<String> definedTableName = new HashSet<String>();
			dropScriptBuffer = new StringBuffer();
			List<String> availableTables = MetaDataHolderUtil.getMetaData("SOURCE_TABLE");
			availableTables.addAll(MetaDataHolderUtil.getMetaData("SOURCE_SEQUENCE"));
			for(SynonymScriptMetadata metaData : synonymMetaData){
				String tableName = (StringUtils.isNotEmpty(metaData.getSynonymName()) && metaData.getSynonymName().length() > DEFAULT_LENGTH) ? DataBaseMappingUtil.abbreviateName(metaData.getSynonymName()) : metaData.getSynonymName();
				
				if (definedTableName.contains(tableName) || (CollectionUtils.isNotEmpty(availableTables)
						&& !availableTables.contains(tableName))) {
					continue;
				} else {
					if (metaData.isSequence()) {
						dropScriptBuffer.append("DROP SEQUENCE " + tableName + ";")
								.append(System.lineSeparator());
						synonymLogger.info("DROP SEQUENCE " + tableName + ";");
					} else {
						dropScriptBuffer.append("DROP TABLE " + tableName + " CASCADE CONSTRAINTS;")
								.append(System.lineSeparator());
						synonymLogger.info("DROP TABLE " + tableName + " CASCADE CONSTRAINTS;");
					}
					definedTableName.add(tableName);
				}
			}
		}
		return dropScriptBuffer.toString();
	}
	
	public static String generateDropScriptPostgres(List<SynonymScriptMetadata> synonymMetaData,String schemaName){
		StringBuffer dropScriptBuffer = null;
		if(CollectionUtils.isNotEmpty(synonymMetaData)){
			synonymLogger.info("************ Generated DROP Script ************");
			Set<String> definedTableName = new HashSet<String>();
			dropScriptBuffer = new StringBuffer();
			List<String> availableTables = MetaDataHolderUtil.getMetaData("SOURCE_TABLE");
			availableTables.addAll(MetaDataHolderUtil.getMetaData("SOURCE_SEQUENCE"));
			for(SynonymScriptMetadata metaData : synonymMetaData){
				String tableName = (StringUtils.isNotEmpty(metaData.getSynonymName()) && metaData.getSynonymName().length() > DEFAULT_LENGTH) ? DataBaseMappingUtil.abbreviateName(metaData.getSynonymName()) : metaData.getSynonymName();
				
				if (definedTableName.contains(tableName) || (CollectionUtils.isNotEmpty(availableTables)
						&& !availableTables.contains(tableName))) {
					continue;
				} else {
					if (metaData.isSequence()) {
						dropScriptBuffer.append("DROP SEQUENCE "+schemaName+"." + tableName + ";")
								.append(System.lineSeparator());
						BaseLoggers.flowLogger.info("DROP SEQUENCE " +schemaName+"." + tableName + ";");
						System.out.println("DROP SEQUENCE " +schemaName+"." + tableName + ";");
					} else {
						dropScriptBuffer.append("DROP TABLE " +schemaName+"." + tableName + " CASCADE;")
								.append(System.lineSeparator());
						BaseLoggers.flowLogger.info("DROP TABLE "+schemaName+"."  + tableName + " CASCADE; ");
						System.out.println("DROP TABLE "+schemaName+"."  + tableName + " CASCADE; ");
					}
					definedTableName.add(tableName);
				}
			}
		}
		return dropScriptBuffer.toString();
	}
	
	private static String removeSchemaName(String tableName, String schemaName) {
		if(StringUtils.isNoneEmpty(schemaName))
		{
			return tableName.replaceFirst(schemaName+"\\.", "");
		}
		return tableName;
	}

	public static String generateGrantScript(List<SynonymScriptMetadata> synonymMetaData, String targetUserName, String dbOrigin){
		StringBuffer grantScriptBuffer = null;
		if(CollectionUtils.isNotEmpty(synonymMetaData)){
			synonymLogger.info("************ Generated GRANT Script ************");
			Set<String> definedTableName = new HashSet<String>();
			grantScriptBuffer = new StringBuffer();

			List<String> nonAvailableSSequence = MetaDataHolderUtil.getMetaData(dbOrigin.toUpperCase() + "_SEQUENCE");
			
			for(SynonymScriptMetadata metaData : synonymMetaData){
				//generating origin master specific script
				if (metaData.getOrigin().equalsIgnoreCase(dbOrigin)) {
					String remoteTableName = (StringUtils.isNotEmpty(metaData.getTableName())
							&& metaData.getTableName().length() > DEFAULT_LENGTH)
									? DataBaseMappingUtil.abbreviateName(metaData.getTableName())
									: metaData.getTableName();
									
					if (definedTableName.contains(remoteTableName)) {
						continue;
					} else {
						if (metaData.isSequence()) {
							if (CollectionUtils.isNotEmpty(nonAvailableSSequence)&& !nonAvailableSSequence.contains(remoteTableName)) {
								continue;
							} else {
								grantScriptBuffer.append("GRANT SELECT ON " + remoteTableName + " TO " + targetUserName + ";")
										.append(System.lineSeparator());
								synonymLogger.info("GRANT SELECT ON " + remoteTableName + " TO " + targetUserName + ";");
							}
						} else {
							grantScriptBuffer.append("GRANT " + metaData.getAuthority() + " ON " + remoteTableName + " TO "
									+ targetUserName + ";").append(System.lineSeparator());
							synonymLogger.info("GRANT " + metaData.getAuthority() + " ON " + remoteTableName + " TO "
									+ targetUserName + ";");
						}
						definedTableName.add(remoteTableName);
					}
				}
			}
		}
		return grantScriptBuffer.toString();
	}
	

	public static String generateGrantScriptPostgres(List<SynonymScriptMetadata> synonymMetaData, String targetUserName,String schemaName, String dbOrigin){
		StringBuffer grantScriptBuffer = null;
		if(CollectionUtils.isNotEmpty(synonymMetaData)){
			synonymLogger.info("************ Generated GRANT Script ************");
			Set<String> definedTableName = new HashSet<String>();
			grantScriptBuffer = new StringBuffer();
			List<String> nonAvailableDbObjects = MetaDataHolderUtil.getMetaData(dbOrigin.toUpperCase()+"_SEQUENCE");
			nonAvailableDbObjects.addAll(MetaDataHolderUtil.getMetaData(dbOrigin.toUpperCase() +"_TABLE"));
			
			for(SynonymScriptMetadata metaData : synonymMetaData){
				if(metaData.getOrigin().equalsIgnoreCase(dbOrigin)) {
					
					String tableName = (StringUtils.isNotEmpty(metaData.getTableName()) && metaData.getTableName().length() > DEFAULT_LENGTH) ? DataBaseMappingUtil.abbreviateName(metaData.getTableName()) : metaData.getTableName();
					
					if (definedTableName.contains(tableName)
							|| !nonAvailableDbObjects.contains(tableName)) {
						continue;
					} else {
						if (metaData.isSequence()) {
							grantScriptBuffer.append(
									"GRANT SELECT,USAGE,UPDATE ON SEQUENCE " + tableName + " TO " + targetUserName + ";")
									.append(System.lineSeparator());
							BaseLoggers.flowLogger.info(
									"GRANT SELECT,USAGE,UPDATE ON SEQUENCE " + tableName + " TO " + targetUserName + ";");
							System.out.println("GRANT SELECT,USAGE,UPDATE ON SEQUENCE " + tableName + " TO " + targetUserName + ";");
						} else {
							grantScriptBuffer.append("GRANT " + metaData.getAuthority() + " ON TABLE "
									+ tableName + " TO " + targetUserName + ";")
									.append(System.lineSeparator());
							BaseLoggers.flowLogger.info("GRANT " + metaData.getAuthority() + " ON TABLE " + tableName
									+ " TO " + targetUserName + ";");
							System.out.println("GRANT " + metaData.getAuthority() + " ON TABLE " + tableName
									+ " TO " + targetUserName + ";");
						}
						definedTableName.add(tableName);
					}
				}
			}
		}
		return grantScriptBuffer.toString();
	}

	
	public static String generateSynonymScript(List<SynonymScriptMetadata> synonymMetaData, String masterUserName, String dbLinkName, String dbOrigin){
		StringBuffer synonymScriptBuffer = null;
		if(CollectionUtils.isNotEmpty(synonymMetaData)){
			synonymLogger.info("************ Generated SYNONYM Script ************");
			synonymScriptBuffer = new StringBuffer();
			Set<String> definedTableName = new HashSet<String>();
			List<String> availableSynonyms = MetaDataHolderUtil.getMetaData("SOURCE_SYNONYM");
			List<String> nonAvailableSSequence = MetaDataHolderUtil.getMetaData(dbOrigin.toUpperCase() +"_SEQUENCE");
			
			for(SynonymScriptMetadata metaData : synonymMetaData){
				if(metaData.getOrigin().equalsIgnoreCase(dbOrigin)) {
					
					String remoteTableName = (StringUtils.isNotEmpty(metaData.getTableName()) && metaData.getTableName().length() > DEFAULT_LENGTH) ? DataBaseMappingUtil.abbreviateName(metaData.getTableName()) : metaData.getTableName();					
					String synonymName = (StringUtils.isNotEmpty(metaData.getSynonymName()) && metaData.getSynonymName().length() > DEFAULT_LENGTH) ? DataBaseMappingUtil.abbreviateName(metaData.getSynonymName()) : metaData.getSynonymName();
					
					if(definedTableName.contains(synonymName) 
							|| (CollectionUtils.isNotEmpty(availableSynonyms) && availableSynonyms.contains(synonymName))
							|| (metaData.isSequence() && StringUtils.isEmpty(dbLinkName) && CollectionUtils.isNotEmpty(nonAvailableSSequence) && !nonAvailableSSequence.contains(synonymName))){
						continue;
					}else{
						if(StringUtils.isNotEmpty(dbLinkName)){
							synonymScriptBuffer.append("CREATE SYNONYM "+synonymName +" FOR "+remoteTableName+"@"+dbLinkName+";").append(System.lineSeparator());
							synonymLogger.info("CREATE SYNONYM "+synonymName +" FOR "+remoteTableName+"@"+dbLinkName+";");
						}else{
							synonymScriptBuffer.append("CREATE SYNONYM "+synonymName +" FOR "+masterUserName+"."+remoteTableName+";").append(System.lineSeparator());
							synonymLogger.info("CREATE SYNONYM "+synonymName +" FOR "+masterUserName+"."+remoteTableName+";");
						}
						definedTableName.add(synonymName);
					}
				}
			}
		}
		return synonymScriptBuffer.toString();
	}
}
