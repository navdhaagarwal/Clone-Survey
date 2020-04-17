package com.nucleus.regional.metadata;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.misc.util.ExceptionUtility;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.regional.RegionalEnabled;
@Named("regionalMetaDataProcessingBean")
@Singleton
public class RegionalMetaDataProcessingBean {

	private Map<String, Object> regionalMetaDataMap = new HashMap<String, Object>();

	//
	public String getRegionalPathAttribute(String name,
			String sourceEntityName, Map<String, Object> regionalMetaDataMap) {
		StringBuilder regionalPath = new StringBuilder();
		if (isNotEmpty(regionalMetaDataMap)) {
			RegionalMetaData regionalMetaData = getRegionalMetaDataForSourceEntityName(name,
					sourceEntityName, regionalMetaDataMap);
			if (notNull(regionalMetaData )) {
				String fieldName = regionalMetaData.getFieldName();
				Boolean isInnerEntity = regionalMetaData.getIsInnerEntity();
				StringBuilder sourceEntityClassName = getSourceEntityClassNameFromRegionalMetaData(regionalMetaData);
			
					regionalPath=fetchRegionalPathFromSourceEntityClassName(isInnerEntity,sourceEntityClassName,fieldName);
					
				}
			}
		
		return regionalPath.toString();
	}
	
	private StringBuilder fetchRegionalPathFromSourceEntityClassName(boolean isInnerEntity,StringBuilder sourceEntityClassName,String fieldName)
	{StringBuilder regionalPath = new StringBuilder();
		if(sourceEntityClassName.length() != 0 && !fieldName.isEmpty()) {
		
		if (isInnerEntity) {
			regionalPath = regionalPath.append(sourceEntityClassName)
					.append(".regionalData.").append(fieldName);
		} else {
			regionalPath = regionalPath.append("regionalData.").append(fieldName);
		}
		}
		return  regionalPath;
	}
	private RegionalMetaData getRegionalMetaDataForSourceEntityName(String name, String sourceEntityName,
			Map<String, Object> regionalMetaDataMap) {
		RegionalMetaData regionalMetaData = null;
		if (isNotEmpty(regionalMetaDataMap)) {
			Map<String, Object> logicalNameregionDataMap = (Map<String, Object>) regionalMetaDataMap
					.get(sourceEntityName);
			if (isNotEmpty(logicalNameregionDataMap))
			{
				BaseLoggers.flowLogger
						.debug(new StringBuilder().append("Regional meta data map find by source entity name [ "
								).append( sourceEntityName).append( " ] is [ ").append( logicalNameregionDataMap).append( " ]").toString());

			regionalMetaData = (RegionalMetaData) logicalNameregionDataMap.get(name);

			BaseLoggers.flowLogger
					.debug(new StringBuilder().append("Regional meta data map find by  name attribute [ ").append(
							 name).append( " ] is [ ").append( regionalMetaData).append(" ]").toString());

		}
		}
		return regionalMetaData;
	}

	private StringBuilder getSourceEntityClassNameFromRegionalMetaData(
			RegionalMetaData regionalMetaData) {
		StringBuilder sourceEntityClassName = new StringBuilder();
		String fullyQualifiedClassName = regionalMetaData
				.getFullyQualifiedEntityName();
		

		
			if (notNull(fullyQualifiedClassName) 
					&& !fullyQualifiedClassName.isEmpty()) {
				Class<?> sourceEntityClass=null;
				
				 try {
					sourceEntityClass = Class
							.forName(fullyQualifiedClassName);
				} catch (ClassNotFoundException e) {
					
					BaseLoggers.flowLogger
					.error(new StringBuilder().append("Exception occurred while loading class [").append(
							 fullyQualifiedClassName).append( "]").toString(),e);
					   ExceptionUtility.rethrowSystemException(e);

				}
			
				
					
				
				if (notNull(sourceEntityClass) 
						&& RegionalEnabled.class
								.isAssignableFrom(sourceEntityClass)) {
					sourceEntityClassName = sourceEntityClassName.append(
							Character.toLowerCase(sourceEntityClass
									.getSimpleName().charAt(0))).append(
							sourceEntityClass.getSimpleName().substring(1));
				}

			}
		
		return sourceEntityClassName;
	}

	

	public Object fetchRegionalValueFromRegionalPath(Object object,
			String regionalPath) {
		Object fieldValue = "";
		BeanWrapper beanWrapper = new BeanWrapperImpl(object);
		try {
			fieldValue = beanWrapper.getPropertyValue(regionalPath);
		} catch (BeansException e) {
			BaseLoggers.exceptionLogger.error(
					"Exception while fetching value of regionalField", e);
		} catch (Exception e) {
			throw new SystemException(e);
		}
		return fieldValue;
	}

	public void setRegionalMetaDataMap(Map<String, Object> regionalMetaDataMap) {
		this.regionalMetaDataMap = regionalMetaDataMap;
	}

	public Map<String, Object> getRegionalMetaDataMap() {
		return regionalMetaDataMap;
	}
}
