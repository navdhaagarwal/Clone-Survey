package com.nucleus.makerchecker;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.core.xml.util.XmlUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.regional.RegionalEnabled;
import com.nucleus.regional.metadata.service.RegionalMetaDataService;

@Named(value = "masterConfigurationRegistry")
public class MasterConfigurationRegistry implements BeanPostProcessor {

    private final List<MasterConfigurationLoader>   masterConfigurationLoaderList = new ArrayList<MasterConfigurationLoader>();

    private static final String EXCEPTION_OCCURED="Exception occured while instantiating SourceEntity: ";
    
    @Inject
    @Named("frameworkConfigResourceLoader")
    private NeutrinoResourceLoader                  resourceLoader;
    
    @Inject
    @Named("regionalMetaDataService")
    private RegionalMetaDataService regionalMetaDataService;

    private HashMap<String, List<EntityUpdateInfo>> _helperMap;
    private EntityUpdateMapper                      mapper;
    private List<MappedEntityInfo>                  mappedEntityInfoList;

    public List<EntityUpdateInfo> getEntityUpdateInfoList(Class<?> entityClass) {
        GridConfiguration gridConfiguration = getConfiguration(entityClass.getSimpleName());
        if (gridConfiguration != null) {
            return gridConfiguration.getMasterEntityConfiguration().getEntityUpdateInfoList();
        }
        return null;
    }

    public List<ColumnConfiguration> getColumnConfigurationList(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if(notNull(gridConfiguration)){        	
        	validateRegionalFieldsInColumnConfiguration(gridConfiguration);
        	 return gridConfiguration.getColumnConfigurationList();
        }
        return null;
    }

    private void validateRegionalFieldsInColumnConfiguration(
			GridConfiguration gridConfiguration) {
    	String sourceEntityName=gridConfiguration.getEntityClass();
    	if(notNull(sourceEntityName)){
    		try {
    			Object classObject=Class.forName(sourceEntityName).newInstance();
    			if(classObject instanceof RegionalEnabled){
    				setRegionalFieldInColumnConfiguration(sourceEntityName,gridConfiguration);
    			}
    		} catch (InstantiationException e) {
    			BaseLoggers.exceptionLogger.debug(EXCEPTION_OCCURED+ e);
    		} catch (IllegalAccessException e) {
    			BaseLoggers.exceptionLogger.debug(EXCEPTION_OCCURED+ e);
    		} catch (ClassNotFoundException e) {
    			BaseLoggers.exceptionLogger.debug(EXCEPTION_OCCURED+ e);
    		}
    	}   	
		
	}

	private void setRegionalFieldInColumnConfiguration(String sourceEntityName,
			GridConfiguration gridConfiguration) {
		Map<String, String> logicalNameAndFieldNameMap = regionalMetaDataService.getLogicalNameAndRegionalFieldMapping(sourceEntityName);
		List<ColumnConfiguration> columnConfigurationList=gridConfiguration.getColumnConfigurationList();		
		ListIterator<ColumnConfiguration> columnConfigurationItr = columnConfigurationList.listIterator();
		while(columnConfigurationItr.hasNext()){
			 ColumnConfiguration columnConfiguration=columnConfigurationItr.next();
			 if(columnConfiguration.getIsRegional()){
					if(logicalNameAndFieldNameMap.containsKey(columnConfiguration.getDataField())){
						StringBuilder regionalDataField=new StringBuilder();
						 regionalDataField=regionalDataField.append("regionalData.").append(logicalNameAndFieldNameMap.get(columnConfiguration.getDataField()));
						columnConfiguration.setRegionalDataField(regionalDataField.toString());
					}else{
						columnConfigurationItr.remove();
					}
			}
		 }
		
	}

	public String getServiceBean(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getSpringBeanName();
        }
        return null;
    }

    public String getjspName(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getJspName();
        }
        return null;
    }

    public boolean getDisableMasterCreation(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getDisableMasterCreation();
        }
        return false;
    }


    public Boolean getProcessingType(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getProcessingType();
        }
        return null;
    }

    public Boolean getHrefBool(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getHrefBool();
        }
        return null;
    }

    public Boolean getPercentageAttribute(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getIsPercentage();
        } else {
            return false;
        }
    }
    
    public String getDefaultSortableColumn(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getDefaultSortable();
        }
        return null;
    }
    
    public String getSortDirection(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getSortDirection();
        }
        return null;
    }

    public String getKey(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getKey();
        }
        return null;
    }
    
    public Integer getMinCharToBeginSearch(String entityName){
    	 GridConfiguration gridConfiguration = getConfiguration(entityName);
    	 if (gridConfiguration != null && gridConfiguration.getMinCharToBeginSearch() != null) {
             return gridConfiguration.getMinCharToBeginSearch();
         }
         return 3;
    }

    public String getRecordURL(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getRecordUrl();
        }
        return null;
    }

    public List<ActionConfiguration> getActionConfigurationList(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getActionConfigurationList();
        }
        return null;
    }

    public List<String> getInitializingPath(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return getConfiguration(entityName).getInitializingPath();
        }
        return null;

    }

    public String getEntityClass(String entityName) {
    	
    	if(entityName.equalsIgnoreCase("User")){
    		entityName="UserInfo";
    	}
    	
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getEntityClass();
        }
        return null;
    }

    public boolean getEntityAutoApprovalFlag(Class<?> entityClass) {
    	
    	String entityName =entityClass.getSimpleName(); 
    	if(entityName.equalsIgnoreCase("User")){
    		entityName="UserInfo";
    	}

        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getMasterEntityConfiguration().getAutoApproval();
        } else {
        	//Since grid configuration are being maintained for super class for some entities 
        	//i.e for ConstantParameter, grid configuration is maintained for super class Parameter
        	String superClassEntityName = entityClass.getSuperclass().getSimpleName();
       	 	gridConfiguration = getConfiguration(superClassEntityName);
	       	if(gridConfiguration != null) 
	       	{
		       	  return gridConfiguration.getMasterEntityConfiguration().getAutoApproval();
	       	 }

	       	return getEntityAutoApprovalFlag(entityClass.getSimpleName());
      	 }

    }

    public boolean getEntityAutoApprovalFlag(String masterEntityName) {
        boolean autoAuthorization = false;
        if (_helperMap == null) {
            loadHelperMap();
        }
        for (MappedEntityInfo mapEI : mappedEntityInfoList) {
            if (masterEntityName.equalsIgnoreCase(mapEI.getMasterEntityName())) {
                if (mapEI.getAutoAuthorizationFlag().equalsIgnoreCase(MasterApprovalFlowConstants.FLAG_Y)) {
                    autoAuthorization = true;
                }
                break;
            }
        }
        return autoAuthorization;
    }

    public Boolean isRemoveCommonActions(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.isRemoveCommonActions();
        }
        return null;
    }
    
    public String getAttributeNameInParentClass(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getAttributeNameInParentClass();
        }
        return null;
    }
    
    public boolean isContainsSearchEnabled(String entityName) {
    	GridConfiguration gridConfiguration = getConfiguration(entityName);
    	if (gridConfiguration != null) {
    		return gridConfiguration.isContainsSearchEnabled();
    	}
    	return false;
    }
    
    private void loadHelperMap() {
        try {
            InputStream is = resourceLoader.getResource("entity-approval-updates-config.xml").getInputStream();
            String input = IOUtils.toString(is);
            mapper = XmlUtils.readFromXml(input, EntityUpdateMapper.class);
        } catch (Exception e) {
            throw new SystemException("Application is unable to read entity-approval-updates-config.xml", e);
        }
        _helperMap = new HashMap<String, List<EntityUpdateInfo>>();
        mappedEntityInfoList = mapper.getMappedEntityInfoList();
        for (MappedEntityInfo mapEI : mappedEntityInfoList) {
            List<EntityUpdateInfo> mapInfoList = mapEI.getEntityUpdateInfoList();
            for (EntityUpdateInfo eIO : mapInfoList) {
                if (StringUtils.isEmpty(eIO.getIncludeApprovalStatuses())) {
                    BaseLoggers.bugLogger.info("Included Approval Statuses is empty.");
                }
                if (StringUtils.isEmpty(eIO.getIncludePersistenceStatuses())) {
                    BaseLoggers.bugLogger.info("Included Persistence Statuses is empty.");
                }
                if (StringUtils.isEmpty(eIO.getUpdateEntityName())) {
                    throw new SystemException("Missing updateEntityName " + " for MasterEntity :"
                            + mapEI.getMasterEntityName());
                }
                if (StringUtils.isEmpty(eIO.getUpdateFieldName())) {
                    throw new SystemException("Missing updateFieldName " + " for MasterEntity :"
                            + mapEI.getMasterEntityName());
                }
            }
            if (!_helperMap.containsKey(mapEI.getMasterEntityName())) {
                _helperMap.put(mapEI.getMasterEntityName(), Collections.unmodifiableList(mapEI.getEntityUpdateInfoList()));
            } else {
                throw new SystemException("Master entity name mentioned more than once : " + mapEI.getMasterEntityName());
            }
        }

    }

    private GridConfiguration getConfiguration(String entityName) {
        for (MasterConfigurationLoader masterConfigurationLoader : masterConfigurationLoaderList) {
            GridConfiguration gridConfiguration = masterConfigurationLoader.getConfiguration(entityName);
            if (gridConfiguration != null) {
                return gridConfiguration;
            }
        }
        return null;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof MasterConfigurationLoader) {
            MasterConfigurationLoader loader = (MasterConfigurationLoader) bean;
            BaseLoggers.flowLogger.info("Registered a new MasterResourceLoader object into MasterConfigurationRegistry");
            masterConfigurationLoaderList.add(loader);
        }
        return bean;
    }


    public boolean getDescendingSortable(String entityName){
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.isDescendingSortable();
        }
        return false;
    }

    public String getDefaultSortingField(String entityName){
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if (gridConfiguration != null) {
            return gridConfiguration.getDefaultSortingField();
        }
        return null;
    }

    public ApplicationDataTableConfiguration getApplicationDataTableConfiguration(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if(notNull(gridConfiguration)){
            return gridConfiguration.getApplicationDataTableConfiguration();
        }
        return null;
    }

    public List<AccordianHeaders> getAccordianHeaderConfiguration(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if(notNull(gridConfiguration)){
            return gridConfiguration.getAccordianHeadersList();
        }
        return null;
    }

    public List<AccordianList> getAccordianListConfiguration(String entityName){
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if(notNull(gridConfiguration)){
            return gridConfiguration.getAccordianList();
        }
        return null;
    }
	
    public Boolean isUploadAvailable(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if(notNull(gridConfiguration)){
            return gridConfiguration.isUploadAvailable();
        }
        return null;
    }

    public Boolean isFormatDownloadAvailable(String entityName) {
        GridConfiguration gridConfiguration = getConfiguration(entityName);
        if(notNull(gridConfiguration)){
            return gridConfiguration.isFormatDownloadAvailable();
        }
        return null;
    }    

}
