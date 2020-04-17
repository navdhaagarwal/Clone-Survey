package com.nucleus.finnone.pro.lov;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.persistence.EntityDao;
import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.springframework.core.io.Resource;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.core.xml.util.XmlUtils;
import org.springframework.transaction.annotation.Transactional;

@Named("lovConfigurationLoader")
public class LOVConfigurationLoader {

	@Inject
	@Named("lovConfigResourceLoader")
	private NeutrinoResourceLoader resourceLoader;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	private final Map<String, LovConfig> lovConfigCacheMap;

	private List<String> allLovKeys;

	private static final String LOV_XML_CONFIG_EXTENSION = ".xml";

	public LOVConfigurationLoader()
	{
	  this.lovConfigCacheMap = new LinkedHashMap();
	  this.allLovKeys = null;
	}

	@Transactional
	public LovConfig getConfiguration(String lovKey) 
	{

	    if(!this.lovConfigCacheMap.containsKey(lovKey)){
            LovConfig loadedLovConfig = null;

            loadedLovConfig = loadLovConfigFromDB(lovKey);

            if(loadedLovConfig == null){
                loadedLovConfig = loadLovConfigFromXML(lovKey);
                if(loadedLovConfig != null){
                    loadedLovConfig.setId(1L); // Because JSONIFYING BaseEntity calls ABE.getDisplayName() - which generates an EntityURI
                    if(loadedLovConfig.getColumnNameList() != null){
                        for(LovColumnConfig lovColumnConfig : loadedLovConfig.getColumnNameList()){
                            lovColumnConfig.setId(1L); // Because JSONIFYING BaseEntity calls ABE.getDisplayName() - which generates an EntityURI
                        }
                    }
                }

            }

            this.lovConfigCacheMap.put(lovKey,loadedLovConfig);
        }

	    return this.lovConfigCacheMap.get(lovKey);
	}

	private LovConfig loadLovConfigFromDB(String lovKey){
        NamedQueryExecutor<LovConfig> lovKeysListQuery =  new NamedQueryExecutor<>("lovConfig.fetchLovConfigForLovKey");
        lovKeysListQuery.addParameter("lovKey",lovKey);
        List<LovConfig> lovConfigList = entityDao.executeQuery(lovKeysListQuery);

        if(lovConfigList == null || lovConfigList.isEmpty()){
            return null;
        }else{
            if (lovConfigList.size()>1) {
                throw new SystemException("Multiple active configurations found in DB for one lovKey: " + lovKey);
            } else{
                LovConfig lovConfig = lovConfigList.get(0);
                Hibernate.initialize(lovConfig.getColumnNameList());
                return lovConfig;
            }
        }
    }

    private LovConfig loadLovConfigFromXML(String lovKey){
	    Resource resource = this.resourceLoader.getResource(lovKey + LOV_XML_CONFIG_EXTENSION);

        if (!(resource.exists())) {
            return null;
        } else {
            try {
                LovConfig config = XmlUtils.readFromXml(IOUtils.toString(resource.getInputStream()), LovConfig.class);
                return config;
            } catch (Exception e) {
                throw new SystemException("Application is unable to read " + lovKey + ".xml", e);
            }
        }
    }

    @Transactional
	public List<String> getListOfConfiguredLovKeys()
	{
	    if(allLovKeys == null){
            Map<String,String> lovKeysMap = new HashMap<>();

			NamedQueryExecutor<String> lovKeysListQuery =  new NamedQueryExecutor<>("lovConfig.fetchAllLovKeys");
			List<String> lovKeyListFromDB = entityDao.executeQuery(lovKeysListQuery);
            if(lovKeyListFromDB != null && !lovKeyListFromDB.isEmpty()) {
                for (String lovKeyFromDB : lovKeyListFromDB) {
                    lovKeysMap.put(lovKeyFromDB, lovKeyFromDB);
                }
            }

			List<Resource> lovXmlList = this.resourceLoader.getIncludedResources("*"+LOV_XML_CONFIG_EXTENSION);
            if(lovXmlList != null && lovXmlList.size() > 0) {
                for (Resource lovXmlConfig : lovXmlList) {
                    String lovKeyFromXml = lovXmlConfig.getFilename().substring(0,lovXmlConfig.getFilename().length()-LOV_XML_CONFIG_EXTENSION.length());
                    lovKeysMap.putIfAbsent(lovKeyFromXml,lovKeyFromXml);
                }
            }

            allLovKeys = new ArrayList<>(lovKeysMap.keySet());
		}

		return allLovKeys;
	}

	public void setResourceLoader(NeutrinoResourceLoader resourceLoader) {
	    this.resourceLoader = resourceLoader;
	}

}
