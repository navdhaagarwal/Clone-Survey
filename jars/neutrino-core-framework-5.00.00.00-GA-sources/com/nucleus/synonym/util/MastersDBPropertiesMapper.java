package com.nucleus.synonym.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.nucleus.finnone.pro.base.constants.CoreConstant;

/**
 * This is DB properties holder class to hold the respective master's Db properties 
 * @author harikant.verma
 *
 */
@Named
public class MastersDBPropertiesMapper implements BeanFactoryPostProcessor  {
	
	private static Map<String, Map<String, String>> MastersDBPropertiesMap = new HashMap<String, Map<String, String>>();
	private static ConfigurableListableBeanFactory beanFactory;
	private static BeanExpressionContext beanExpressionContext;
	/**
	 * Method prepare the MastersDBPropertiesMap for origin from database properties file
	 * @param environment
	 * @param dbOrigin
	 */
	public static void prepareMasterDbProperties(String dbOrigin) {
		if(dbOrigin != null && MastersDBPropertiesMap.get(dbOrigin) == null) {		
			Map<String, String> dbProperties = new HashMap<String, String>();
			dbProperties.put(CoreConstant.MASTER_DATABASE_USERNAME_KEY,
					resolveProperty(dbOrigin + CoreConstant.SUFFIX_MASTER_DATABASE_USERNAME));
			dbProperties.put(CoreConstant.MASTER_DATABASE_PASSWORD_KEY,
					resolveProperty(dbOrigin + CoreConstant.SUFFIX_MASTER_DATABASE_PASSWORD));
			dbProperties.put(CoreConstant.MASTER_DATABASE_URL_KEY,
					resolveProperty(dbOrigin + CoreConstant.SUFFIX_MASTER_DATABASE_URL));
			dbProperties.put(CoreConstant.MASTER_DATABASE_JNDI_NAME_KEY,
					resolveProperty(dbOrigin + CoreConstant.SUFFIX_MASTER_DATABASE_JNDI_NAME));
			dbProperties.put(CoreConstant.MASTER_DATABASE_DISABLE_ORIGIN_KEY,
					resolveProperty(dbOrigin + CoreConstant.SUFFIX_MASTER_DATABASE_DISABLE_ORIGIN));
			
			//This is to support exiting property set
			if(dbOrigin.equalsIgnoreCase(CoreConstant.MASTERS)) {
				String dblink = resolveProperty(CoreConstant.DEFAULT_MASTER_DATABASE_DBLINK);
				dblink = StringUtils.isNotEmpty(dblink) ? dblink : resolveProperty(dbOrigin + CoreConstant.SUFFIX_MASTER_DATABASE_DBLINK);
				
				String dbSchema = resolveProperty(CoreConstant.DEFAULT_MASTER_DATABASE_SCHEMA_NAME);
				dbSchema = StringUtils.isNotEmpty(dbSchema) ? dbSchema : resolveProperty(dbOrigin + CoreConstant.SUFFIX_MASTER_DATABASE_SCHEMA_NAME);
				
				dbProperties.put(CoreConstant.MASTER_DATABASE_DBLINK_KEY,dblink);
				dbProperties.put(CoreConstant.MASTER_DATABASE_SCHEMA_NAME_KEY,dbSchema);
			}else {
				dbProperties.put(CoreConstant.MASTER_DATABASE_DBLINK_KEY,
						resolveProperty(dbOrigin + CoreConstant.SUFFIX_MASTER_DATABASE_DBLINK));
				dbProperties.put(CoreConstant.MASTER_DATABASE_SCHEMA_NAME_KEY,
						resolveProperty(dbOrigin + CoreConstant.SUFFIX_MASTER_DATABASE_SCHEMA_NAME));
			}
			MastersDBPropertiesMap.put(dbOrigin, dbProperties);
		}
	}

	/**
	 * fetch property from MastersDBPropertiesMap for origin
	 * @param dbOrigin
	 * @param key
	 * @return String
	 */
	public static String getProperty(String dbOrigin, String key) {
		Map<String, String> dbProperties = MastersDBPropertiesMap.get(dbOrigin);
		if(dbProperties != null) {
			return dbProperties.get(key);
		}
		return null;
	}
	
	/**
	 * @return set of schema origins 
	 */
	public static Set<String> getMasterOrigins(){
		return MastersDBPropertiesMap.keySet();
	}

	private static String resolveProperty(String key) {
		String result= (String)beanFactory.getBeanExpressionResolver().evaluate(beanFactory.resolveEmbeddedValue("#{'${" + key + "}' matches '.*"+key+".*' ? '' : '${" + key + "}'}"), beanExpressionContext);
		if(StringUtils.isNotEmpty(result)) {
			return result;
		}
		return null;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		MastersDBPropertiesMapper.beanFactory = beanFactory;
		MastersDBPropertiesMapper.beanExpressionContext = new BeanExpressionContext(beanFactory, null);
	}
}
