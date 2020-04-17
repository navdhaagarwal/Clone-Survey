package com.nucleus.synonym.listener;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.collection.OneToManyPersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.persister.walking.spi.AttributeDefinition;
import org.hibernate.tuple.entity.EntityBasedAssociationAttribute;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.ReflectionUtils;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.annotations.SynonymProps;
import com.nucleus.core.database.initializer.SpringDatabaseExecutorDelegate;
import com.nucleus.core.dbmapper.util.DataBaseMappingUtil;
import com.nucleus.finnone.pro.base.constants.CoreConstant;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.license.audit.LicenseAuditLog;
import com.nucleus.license.core.entities.AppliedLicenses;
import com.nucleus.license.core.entities.LicenseInfoHolder;
import com.nucleus.license.event.core.LicenseEvent;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.synonym.metadata.pojo.SynonymScriptMetadata;
import com.nucleus.synonym.util.MastersDBPropertiesMapper;
import com.nucleus.synonym.util.MetaDataHolderUtil;
import com.nucleus.synonym.util.SynonymScriptUtil;

@Named("synonymEntityManagerFactoryListener")
@Profile(value="synonymEnabled")
public class SynonymEntityManagerFactoryListener implements EntityManagerFactoryListener {
	
	@Value(value = "#{'${masters.database.username}'}")
	private String mastersDatabaseUserName;
	
	@Value(value = "#{'${masters.database.password}'}")
	private String mastersDatabasePassword;
	
	@Value(value = "#{'${masters.database.connection.url}'}")
	private String mastersDatabaseUrl;
	
	@Value(value = "#{'${database.username}'}")
	private String targetDatabaseUserName;
	
	@Value(value = "#{'${database.password}'}")
	private String targetDatabasePassword;
	
	@Value(value = "#{'${database.connection.url}'}")
	private String targetDatabaseUrl;
	
	@Value(value = "#{'${database.dataSource.name}'}")
	private String targetDatabaseJndiName;
	
	@Value(value = "#{'${database.target.originName}'}")
	private String targetOriginName;
		
	@Value(value = "${database.type}")
	private String databaseType;
	
	@Value(value = "#{'${db.link.name}'}")
	private String dbLinkName;

	@Value(value = "#{'${database.master.schemaname}'}")
	private String masterSchemaName;

	@Value(value = "#{'${database.app.schemaname}'}")
	private String appSchemaName;
	
	@Autowired
	private Environment environment;

	private Map<Class<?>, SynonymProps> beanSynonymPropsMapping = new HashMap<Class<?>, SynonymProps>();
	
	private Map<String, List<SynonymScriptMetadata>> synonymMetaData = new HashMap<String, List<SynonymScriptMetadata>>();
	
	public void setMastersDatabaseUserName(String mastersDatabaseUserName) {
		this.mastersDatabaseUserName = mastersDatabaseUserName;
	}

	public void setMastersDatabasePassword(String mastersDatabasePassword) {
		this.mastersDatabasePassword = mastersDatabasePassword;
	}

	public void setMastersDatabaseUrl(String mastersDatabaseUrl) {
		this.mastersDatabaseUrl = mastersDatabaseUrl;
	}

	public void setTargetDatabaseUserName(String targetDatabaseUserName) {
		this.targetDatabaseUserName = targetDatabaseUserName;
	}

	public void setTargetDatabasePassword(String targetDatabasePassword) {
		this.targetDatabasePassword = targetDatabasePassword;
	}

	public void setTargetDatabaseUrl(String targetDatabaseUrl) {
		this.targetDatabaseUrl = targetDatabaseUrl;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}
	
	public void setDbLinkName(String dbLinkName) {
		this.dbLinkName = dbLinkName;
	}

	@Override
	public void process(EntityManagerFactory emf) {
		DataSource appDatasource = getDataSource(targetDatabaseUrl, targetDatabaseUserName, targetDatabasePassword, targetDatabaseJndiName);
		
		if(appDatasource == null){
			BaseLoggers.flowLogger.error("Datasource could not be created with provided details.");
			return;
		}
		
		String appUserName = null;
		Connection appDatasourceConnection = null;
		try {
			appDatasourceConnection = appDatasource.getConnection();
			appUserName  = appDatasourceConnection.getMetaData().getUserName();
			
			if(StringUtils.isBlank(appUserName))
			{
				BaseLoggers.flowLogger.error("Extrated username is Null OR Empty from target datasource");
				throw new SystemException("Extrated username is Null OR Empty from target datasource");
			}
			
		} catch (Exception sqle) {
			BaseLoggers.flowLogger.error("Error in extrating username from application datasource " + sqle);
			throw new SystemException("Error in extrating username from application datasource, Exception Message: "+ sqle.getMessage());
		}finally {
			if(appDatasourceConnection !=null) {
				try {
					/*Issue is coming when close() performed on SingleConnectionDataSource and using the same datasource after close() method called.
					  So using doCloseConnection() to close the Connection for DataSource or SmartDataSource.
					  As per doCloseConnection() method summary : Close the Connection, unless a SmartDataSource doesn't want us to.
					*/

					DataSourceUtils.doCloseConnection(appDatasourceConnection,appDatasource);
				}catch (Exception exception) {
					BaseLoggers.flowLogger.error("Error in closing application datasource connection : " + exception);
				}
			}
		}

		targetDatabaseUserName = appUserName;
		
		
		if(databaseType != null && CoreConstant.ORACLE.equalsIgnoreCase(databaseType)){
			BaseLoggers.flowLogger.info("Start creating Synonym as execution of synonym creation is enabled");
				
			MetaDataHolderUtil.fetchAvailableMetaData(appDatasource, CoreConstant.SOURCE);
			SessionFactoryImpl sf = getSessionFactory(emf);

			prepareDataToProcess(emf);
			// SYNONYM-Change repeat these steps for all masters
			// need to be done after DB map prepare need to be moved after call prepareDataToProcess
			for (String dbOrigin : MastersDBPropertiesMapper.getMasterOrigins()) {
				if(isOriginDisabled(dbOrigin)) {
					continue;
				}
				MetaDataHolderUtil.fetchAvailableMetaData(getDataSource(
						MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_URL_KEY),
						MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_USERNAME_KEY),
						MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_PASSWORD_KEY),
						MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_JNDI_NAME_KEY)),
						dbOrigin.toUpperCase());
			}
			 prepareScriptMetaData(sf);
			 prepareAndExecuteScriptOracle(appUserName);

		}
		else if(databaseType != null && CoreConstant.POSTGRES.equalsIgnoreCase(databaseType))
		{
			BaseLoggers.flowLogger.info("Start creating Synonym as execution of synonym creation is enabled");
			
			if(StringUtils.isEmpty(appSchemaName))
			{
				BaseLoggers.flowLogger.info("database.app.schemaname not found so taking appUserName as schema name for application");
				appSchemaName=appUserName;
			}
			
			MetaDataHolderUtil.fetchAvailableMetaDataPostgres(appDatasource, CoreConstant.SOURCE, appSchemaName);
			SessionFactoryImpl sf = getSessionFactory(emf);
			prepareDataToProcess(emf);
			 
			// SYNONYM-Change repeat these steps for all masters
			for (String dbOrigin : MastersDBPropertiesMapper.getMasterOrigins()) {
				if(isOriginDisabled(dbOrigin)) {
					continue;
				}
				
				MetaDataHolderUtil.fetchAvailableMetaDataPostgres(getDataSource(
						MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_URL_KEY),
						MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_USERNAME_KEY),
						MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_PASSWORD_KEY), 
						MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_JNDI_NAME_KEY)),
						dbOrigin.toUpperCase(),
						MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_SCHEMA_NAME_KEY));
			}
			prepareScriptMetaData(sf); 
			prepareAndExecuteScriptPostgres(appUserName, appSchemaName);
		}
		else
		{
			BaseLoggers.flowLogger.info("Ignore creating Synonym as database type is not supported");
		}
	}
	
	private SessionFactoryImpl getSessionFactory(EntityManagerFactory emf) {
		 return (SessionFactoryImpl)emf ;
	}
	
	//Method responsible to create Map which holds data of class having Synonym annotation and the grant of that class.
	private void prepareDataToProcess(EntityManagerFactory emf) {
		Set<EntityType<?>> entities = emf.getMetamodel().getEntities();
		
		
		for (EntityType<?> en : entities) {
			Class<?> clas = en.getJavaType();
			Synonym synonym = AnnotationUtils.findAnnotation(clas, Synonym.class);
			if (synonym != null) {
				if (CoreConstant.GRANT_ALL.equalsIgnoreCase(synonym.grant())
						|| CoreConstant.GRANT_SELECT.equalsIgnoreCase(synonym.grant())
						|| CoreConstant.GRANT_SELECT_REFERENCES.equalsIgnoreCase(synonym.grant())) {

					// SYNONYM-Change added SynonymProps in the mapping instead of just grant value
					SynonymProps synonymProps = new SynonymProps(synonym.grant(), synonym.remoteTableName(),
							synonym.originSchema());
					beanSynonymPropsMapping.put(clas, synonymProps);

					// prepare the DB property map based on the value of the masters
					if (synonymProps.getOriginSchema() != null
							&& !"".equalsIgnoreCase(synonymProps.getOriginSchema().trim())) {
						MastersDBPropertiesMapper
								.prepareMasterDbProperties(synonymProps.getOriginSchema().toLowerCase());
					}
				} else {
					throw new SystemException(
							"Passed Synonym Grant : " + synonym.grant() + " on entity : " + clas + " is not allowed");
				}
			}

		}
		//Classes from license client where annotation is not given
		 List<Class<?>> specificClassList=Arrays.asList(AppliedLicenses.class,LicenseEvent.class,LicenseInfoHolder.class,LicenseAuditLog.class);
		 prepareDataForSpecificClasses(specificClassList);
		
		 
	}
	
	private void prepareDataForSpecificClasses(List<Class<?>> specificClassList) {
		for(Class<?> clas:specificClassList)
		{
			 SynonymProps synonymProps = new SynonymProps("ALL", "","MASTERS");
			 beanSynonymPropsMapping.put(clas, synonymProps);
			 MastersDBPropertiesMapper.prepareMasterDbProperties(synonymProps.getOriginSchema().toLowerCase());
		}
		
	}


	//Method responsible to generate meta data which in turn helps in generating scripts for (drop, grant and synonyms).
	private void prepareScriptMetaData(SessionFactoryImpl sf) {
		
		for (Map.Entry<Class<?>, SynonymProps> metaData : beanSynonymPropsMapping.entrySet()) {
			try {
	            ClassMetadata hibernateMetadata = sf.getClassMetadata(metaData.getKey());
	            
	            if (hibernateMetadata != null && hibernateMetadata instanceof AbstractEntityPersister){
	            	AbstractEntityPersister persister = (AbstractEntityPersister) hibernateMetadata;
	            	 
	            	prepareDataForDirectTables(persister, metaData);
	            	
	 	           //Fetch attributes of the entity
	 	           Iterable<AttributeDefinition> attributeDefinitions = persister.getAttributes();
	 	           
	 	           if(attributeDefinitions != null){
	 	        	   prepareDataForJoinTables(attributeDefinitions, metaData, sf);
		            }
	            }
	        } catch (Exception e) {

	        	throw new SystemException("Exception Occurs while preparing Script Meta data. Exception Message: "+ e.getMessage());
	        }
        }
		
	}

	//Method responsible to generate meta data for the entities directly available.
	//synonymName is the name of synonym in Application database(target)
	//remoteTableName is the name of table in remote database(Master)
	//if remotTableName is not provided then synonymName and remotTableName will be same
	private void prepareDataForDirectTables(AbstractEntityPersister persister, Entry<Class<?>, SynonymProps> metaData) {
		try{
			 String remoteTableName = null;
			 String synonymName = null;
			 
			 if(persister instanceof UnionSubclassEntityPersister){
	    		 Field field = persister.getClass().getDeclaredField("tableName");
	    		 field.setAccessible(true);
	    		 synonymName = (String) field.get(persister);
	    	 }else{
	    		 synonymName = persister.getTableName();
	    	 }
			 remoteTableName = synonymName;
			 
			 //if remotTable name is not provided then synonym name(Entity Name) will be same as remotTable name
			 if(StringUtils.isNotEmpty(metaData.getValue().getRemoteTableName())) {
				 remoteTableName = metaData.getValue().getRemoteTableName();
			 }
			 
	    	 //tableName=removePrefix(tableName);
	        //SET GRANT for Tables From Source DataSource To Target DataSource
	    	 createMetaData(true, false, metaData.getValue().getGrant(), remoteTableName, metaData.getValue().getOriginSchema(),  synonymName);
	         
	         //SET GRANT to Sequences From Source DataSource To Target DataSource
	        // if(metaData.getValue() != null && !metaData.getValue().equalsIgnoreCase("SELECT")){
			//changed against PDDEV-22305
			//for creating sequence, and grant script for 'SELECT'
			createMetaData(true, true, CoreConstant.GRANT_SELECT, getSequenceName(remoteTableName), metaData.getValue().getOriginSchema(), getSequenceName(synonymName));

			//for creating just sequence without providing grant value
			createMetaData(false, true, "", getSequenceName(remoteTableName), metaData.getValue().getOriginSchema(), getSequenceName(synonymName));

	    	//Create Synonym Data for creating synonym script
	         createMetaData(false, false, "", remoteTableName, metaData.getValue().getOriginSchema(), synonymName);
		}catch(Exception e){

			throw new SystemException("Exception Occurs while preparing data for entities available directly. Exception Message: "+ e.getMessage());
		}
	}
	
	private static String removeSchemaName(String tableName, String schemaName) {
		if(StringUtils.isNoneEmpty(schemaName))
		{
			return tableName.replaceFirst(schemaName+"\\.", "");
		}
		return tableName;
	}

	//Method responsible to generate meta data for the join tables.
	@SuppressWarnings("rawtypes")
	private void prepareDataForJoinTables(Iterable<AttributeDefinition> attributeDefinitions, Entry<Class<?>, SynonymProps> metaData, SessionFactoryImpl sf) {
		try{
		    Iterator<AttributeDefinition> classMetadata = attributeDefinitions.iterator();
        	while(classMetadata.hasNext()){
        		AttributeDefinition definition = classMetadata.next();
        		if(definition instanceof EntityBasedAssociationAttribute){
        			AssociationType associationType = ((EntityBasedAssociationAttribute)definition).getType();
        			if(associationType.isCollectionType()){
        				CollectionType type = (CollectionType)associationType;
        				
        				//Fetch associated Entity Name
        				String entityName = null;
        				boolean createJoinTableForcelly = false;
        				try{
        					entityName = associationType.getAssociatedEntityName(sf);
        				}catch(Exception e){
        					BaseLoggers.exceptionLogger.info("Something went wrong, loading entity using reflections.....");
        					Field field = ReflectionUtils.findField(metaData.getKey(), definition.getName());
        					if(field != null){
        						java.lang.reflect.Type fieldType = field.getGenericType();
        						if(fieldType instanceof ParameterizedType){
        							entityName = ((Class)((ParameterizedType) fieldType).getActualTypeArguments()[0]).getName();
        							createJoinTableForcelly = true;
        						}
        					}
        				}
        				
        				if((StringUtils.isNotEmpty(entityName) && beanSynonymPropsMapping.get(Class.forName(entityName)) != null) || createJoinTableForcelly){
        					String role = type.getRole();
        					String joinTableName = ((AbstractCollectionPersister)(sf).getCollectionMetadata(role)).getTableName();
        					
        					//IMP CONDITION : Check this condition if identified persister is of type One To Many and the generated table name size is greater than 50
        					//Problem occur for the properties like subLoanCharges in SUBLOAN entity which generates table name as union of all classes of 
        					//ApplicationCharges including it's subclasses.
        					if(((AbstractCollectionPersister)(sf).getCollectionMetadata(role) instanceof OneToManyPersister) && joinTableName.length() > 50){
        						continue;
        					}
        					createMetaData(true, false, metaData.getValue().getGrant(), joinTableName, metaData.getValue().getOriginSchema(), joinTableName);
        					
							//changed against PDDEV-22305
        					createMetaData(true, true, CoreConstant.GRANT_SELECT, getSequenceName(joinTableName), metaData.getValue().getOriginSchema(), getSequenceName(joinTableName));
        					createMetaData(false, true, "", getSequenceName(joinTableName), metaData.getValue().getOriginSchema(),  getSequenceName(joinTableName));

        					createMetaData(false, false, "", joinTableName, metaData.getValue().getOriginSchema(), joinTableName);
        				}
        			}
        		}
        	}
		}catch(Exception e){

			throw new SystemException("Exception Occurs while preparing data for Join tables. Exception Message: "+ e.getMessage());
		}
	}
	
	//Method responsible to generate drop, grant and synonyms script.
	private void prepareAndExecuteScriptOracle(String appUserName) {
		String dropScript = SynonymScriptUtil.generateDropScript(synonymMetaData.get(CoreConstant.GRANT),"");
		
		if(StringUtils.isNotEmpty(dropScript)){
			executeDropScript(dropScript);
		}
		
		//This will be executed multiple times for each master data source
		for (String dbOrigin : MastersDBPropertiesMapper.getMasterOrigins()) {
			
			if(isOriginDisabled(dbOrigin)) {
				continue;
			}
						
			String grantScript = null;
			
			DataSource masterDataSoure = getDataSource(MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_URL_KEY),
					MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_USERNAME_KEY),
					MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_PASSWORD_KEY),
					MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_JNDI_NAME_KEY));
			
			String masterUserName = null;
			Connection masterDataSoureConnection = null;
			try {
				masterDataSoureConnection =  masterDataSoure.getConnection();				
				masterUserName = masterDataSoureConnection.getMetaData().getUserName();
				
				if(StringUtils.isEmpty(masterUserName)){
					BaseLoggers.flowLogger.error("Extrated username is Null OR Empty from master datasource for master [" + dbOrigin+"]");
					throw new SystemException("Extrated username is Null OR Empty from master datasource for master [" + dbOrigin+"]");
				}
				
			} catch (Exception sqle) {
				BaseLoggers.flowLogger.error("Error in extrating username from master datasource for master [" + dbOrigin+"]" + sqle);
				throw new SystemException("Error in extrating username from master datasourcefor master [" + dbOrigin + "]" + ", Exception Message: "+ sqle.getMessage());
			}finally {
				if(masterDataSoureConnection != null) {
					try {
						/*Issue is coming when close() performed on SingleConnectionDataSource and using the same datasource after close() method called.
						  So using doCloseConnection() to close the Connection for DataSource or SmartDataSource.
						  As per doCloseConnection() method summary : Close the Connection, unless a SmartDataSource doesn't want us to.
						*/

						DataSourceUtils.doCloseConnection(masterDataSoureConnection,masterDataSoure);
					}catch (Exception exception) {
						BaseLoggers.flowLogger.error("Error in closing master application datasource connection : " + exception);
					}					
				}
			}
			
			
			//IMP CONDITION : This check ensure that grant script is only executable on local DB server for which DBLINK is not required
			//and for remote DB server grant script will be executed manually, due to this DBlink is keeping blank 
			if(StringUtils.isEmpty(MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_DBLINK_KEY))){
				grantScript = SynonymScriptUtil.generateGrantScript(synonymMetaData.get(CoreConstant.GRANT), appUserName, dbOrigin);
			}
			
			if(StringUtils.isNotEmpty(grantScript)){
				executeGrantScript(grantScript, masterDataSoure);
			}
			
			String synonymScript = SynonymScriptUtil.generateSynonymScript(synonymMetaData.get(CoreConstant.SYNONYM),
					masterUserName,
					MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_DBLINK_KEY), dbOrigin);
					
			if(StringUtils.isNotEmpty(synonymScript)){
				executeSynonymScript(synonymScript);
			}
		}
	}

	private boolean isOriginDisabled(String dbOrigin) {
		String disabledOrigin = MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_DISABLE_ORIGIN_KEY);
		if("true".equalsIgnoreCase(disabledOrigin)) {
			return true;
		}
		return false;
	}

	//Method responsible to generate drop, grant and synonyms script.
	private void prepareAndExecuteScriptPostgres(String appUserName, String appSchemaName) {
		String dropScript = SynonymScriptUtil.generateDropScriptPostgres(synonymMetaData.get(CoreConstant.GRANT),
				appSchemaName);
		BaseLoggers.flowLogger.debug(dropScript);
		if(StringUtils.isNotEmpty(dropScript)){
			executeDropScript(dropScript);
		}
		
		//SYNONYM CHANGE - This script must be called per Master  
		for (String dbOrigin : MastersDBPropertiesMapper.getMasterOrigins()) {
			if (isOriginDisabled(dbOrigin)) {
				continue;
			}

			String grantScript = SynonymScriptUtil.generateGrantScriptPostgres(synonymMetaData.get(CoreConstant.GRANT),
					appUserName, appSchemaName, dbOrigin);
			BaseLoggers.flowLogger.debug(grantScript);

			DataSource masterDataSoure = getDataSource(
					MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_URL_KEY),
					MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_USERNAME_KEY),
					MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_PASSWORD_KEY),
					MastersDBPropertiesMapper.getProperty(dbOrigin, CoreConstant.MASTER_DATABASE_JNDI_NAME_KEY));

			if (StringUtils.isNotEmpty(grantScript)) {
				executeGrantScript(grantScript, masterDataSoure);
			}
		}
	}

	//Method responsible to execute drop script.
	private void executeDropScript(String dropScript) {
		BaseLoggers.flowLogger.info("Executing drop script for which synonyms have to be created.");
		try{
			DataSource targetDataSoure = getDataSource(targetDatabaseUrl, targetDatabaseUserName, targetDatabasePassword, targetDatabaseJndiName);
			Resource resource = new InputStreamResource(IOUtils.toInputStream(dropScript));
			
			SpringDatabaseExecutorDelegate dbScriptExecutor = new SpringDatabaseExecutorDelegate();
			dbScriptExecutor.setDataSource(targetDataSoure);
			dbScriptExecutor.setResources(new Resource[] {resource});
			dbScriptExecutor.setIgnoreAllFailures(true);
			dbScriptExecutor.invokeDelegate();
		}catch(Exception e){

			throw new SystemException("Some error occured while executing drop script for which synonyms have to be created. Exception Message: "+ e.getMessage());
		}
		BaseLoggers.flowLogger.info("Successfully executing drop script for which synonyms have to be created.");
	}
	//Method responsible to execute grant script.
	private void executeGrantScript(String grantScript, DataSource masterDataSoure) {
		BaseLoggers.flowLogger.info("Executing grant script for which synonyms have to be created.");
		try{
			Resource resource = new InputStreamResource(IOUtils.toInputStream(grantScript));
			
			SpringDatabaseExecutorDelegate dbScriptExecutor = new SpringDatabaseExecutorDelegate();
			dbScriptExecutor.setDataSource(masterDataSoure);
			dbScriptExecutor.setResources(new Resource[] {resource});
			dbScriptExecutor.setIgnoreAllFailures(false);
			dbScriptExecutor.invokeDelegate();
		}catch(Exception e){

			throw new SystemException("Some error occured while executing grant script for which synonyms have to be created. Exception Message: "+ e.getMessage());
		}
		BaseLoggers.flowLogger.info("Successfully executing grant script for which synonyms have to be created.");
	}

	//Method responsible to execute synonym script.
	private void executeSynonymScript(String synonymScript) {
		BaseLoggers.flowLogger.info("Executing synonym script which creates synonyms.");
		try{
			DataSource targetDataSoure = getDataSource(targetDatabaseUrl, targetDatabaseUserName, targetDatabasePassword, targetDatabaseJndiName);
			Resource resource = new InputStreamResource(IOUtils.toInputStream(synonymScript));
			
			SpringDatabaseExecutorDelegate dbScriptExecutor = new SpringDatabaseExecutorDelegate();
			dbScriptExecutor.setDataSource(targetDataSoure);
			dbScriptExecutor.setResources(new Resource[] {resource});
			dbScriptExecutor.setIgnoreAllFailures(true);
			dbScriptExecutor.invokeDelegate();
		}catch(Exception e){

			throw new SystemException("Some error occured while executing synonym script which creates synonyms. Exception Message: "+ e.getMessage());
		}
		BaseLoggers.flowLogger.info("Successfully executing synonym script which creates synonyms.");
	}

	//Method responsible to prepare metadata for grant and synonym script.
	//SYNONYM-change added Master Schema origin while preparing meta data
	private SynonymScriptMetadata createMetaData(boolean isGrantScript, boolean isSequenceScript, String authority,
			String tableName, String schemaOrigin, String synonymName) {
		if (StringUtils.isNotEmpty(targetOriginName) && targetOriginName.equalsIgnoreCase(schemaOrigin)) {
			return null;
		}
		
		SynonymScriptMetadata grantTableMetaData = new SynonymScriptMetadata();
		grantTableMetaData.setAuthority(authority);
		grantTableMetaData.setGrant(isGrantScript);
		grantTableMetaData.setSequence(isSequenceScript);
		grantTableMetaData.setSynonym(!isGrantScript);
		grantTableMetaData.setOrigin(schemaOrigin);
		
		if(CoreConstant.POSTGRES.equalsIgnoreCase(databaseType))
		{
			grantTableMetaData.setTableName(removeSchemaName(tableName.toLowerCase(),appSchemaName.toLowerCase()));
			grantTableMetaData.setSynonymName(removeSchemaName(synonymName.toLowerCase(),appSchemaName.toLowerCase()));
		}
		else
		{
			grantTableMetaData.setTableName(removeSchemaName(tableName.toUpperCase(),targetDatabaseUserName.toUpperCase()));
			grantTableMetaData.setSynonymName(removeSchemaName(synonymName.toUpperCase(),targetDatabaseUserName.toUpperCase()));
		}
		List<SynonymScriptMetadata> scriptMetadataList = null;
		if(isGrantScript){
			scriptMetadataList = synonymMetaData.get(CoreConstant.GRANT);
			if(scriptMetadataList == null){
				scriptMetadataList = new ArrayList<SynonymScriptMetadata>();
			}
			scriptMetadataList.add(grantTableMetaData);
			synonymMetaData.put(CoreConstant.GRANT, scriptMetadataList);
		}else{
			scriptMetadataList = synonymMetaData.get(CoreConstant.SYNONYM);
			if(scriptMetadataList == null){
				scriptMetadataList = new ArrayList<SynonymScriptMetadata>();
			}
			scriptMetadataList.add(grantTableMetaData);
			synonymMetaData.put(CoreConstant.SYNONYM, scriptMetadataList);
		}
		return grantTableMetaData;
	}
	
	private String getSequenceName(String tableName){
		String sequenceName = DataBaseMappingUtil.abbreviateName(tableName+"_SEQ");
		BaseLoggers.flowLogger.info("Sequence Generated using Strategy ::: "+sequenceName);
		return sequenceName;
	}
	
	private DataSource getDataSource(String url, String userName, String password, String jndiName){
		String[] defaultProfiles = environment.getDefaultProfiles();
		String[] activeProfiles = environment.getActiveProfiles();
		
		//check if profile is app-server-provided  then use jndiname otherwise use default credentials
		if(defaultProfiles != null 
				&& defaultProfiles.length > 0 
				&& (Arrays.asList(defaultProfiles).contains("app-server-provided") || Arrays.asList(defaultProfiles).contains("app-server-cluster-provided"))
				|| (activeProfiles != null && activeProfiles.length  > 0 
					&& (Arrays.asList(activeProfiles).contains("app-server-provided") || Arrays.asList(activeProfiles).contains("app-server-cluster-provided")))){
			
			if(jndiName != null && !"".equals(jndiName.trim())){
				//fetch the data source from jndi name
				try {
					Context ctx = new InitialContext();
					return (DataSource)ctx.lookup(jndiName);
				} catch (Exception e) {
					BaseLoggers.flowLogger.error("Could not find the datasource with jndi name : " + jndiName + ", Error : "  + e);
					throw new SystemException("Error in datasource lookup for jndi name :" + jndiName + " Exception Message: "+ e.getMessage());
				}
			}else{
				BaseLoggers.flowLogger.error("For profile app-server-provided / app-server-cluster-provided jndiname is required");
				throw new SystemException("For profile app-server-provided / app-server-cluster-provided datsource jndiname is required");
			}
		}
		return new SingleConnectionDataSource(url, userName, password, false);
	}
	
	private String decrypt(String encryptedText, SecretKey secretKey) {
		String decryptedText = "";
		try {
			Cipher cipher = Cipher.getInstance("AES");
			Base64.Decoder decoder = Base64.getDecoder();
			byte[] encryptedTextByte = decoder.decode(encryptedText);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
			decryptedText = new String(decryptedByte);
		} catch (Exception e) {
			throw new SystemException("Unable to parse encrypted password");
		}
        return decryptedText;
    }
}
