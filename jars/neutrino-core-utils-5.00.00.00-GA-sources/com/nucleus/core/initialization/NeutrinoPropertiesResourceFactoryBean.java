package com.nucleus.core.initialization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class NeutrinoPropertiesResourceFactoryBean implements FactoryBean<Map<String, String>> {

    private String                 resourceConfigFile;

    private NeutrinoResourceLoader resourceLoader;

    @Override
    public Map<String, String> getObject() throws Exception {
        Resource resource = resourceLoader.getResource(resourceConfigFile);
        Properties properties = PropertiesLoaderUtils.loadProperties(resource);
        Map<String, String> propertyMap = new HashMap<String, String>();
        Iterator<Entry<Object, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Object, Object> entry = iterator.next();
            String propertyKey = (String) entry.getKey();
            String propertyValue = (String) entry.getValue();
            propertyMap.put(propertyKey, resourceLoader.resolvePath(propertyValue));
        }
        return propertyMap;
    }

    @Override
    public Class<?> getObjectType() {
        return HashMap.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public String getResourceConfigFile() {
        return resourceConfigFile;
    }

    public void setResourceConfigFile(String resourceConfigFile) {
        this.resourceConfigFile = resourceConfigFile;
    }

    public NeutrinoResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(NeutrinoResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
