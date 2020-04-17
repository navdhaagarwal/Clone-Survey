package com.nucleus.core.database.dbunitFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.filter.IColumnFilter;
import org.springframework.core.io.Resource;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.NeutrinoResourceLoader;
import com.nucleus.core.xml.util.XmlUtils;

@Named("customPrimaryKeyFilter")
public class CustomPrimaryKeyFilter implements IColumnFilter {

    @Inject
    @Named("frameworkConfigResourceLoader")
    public NeutrinoResourceLoader     resourceLoader;

    public List<Resource>             resources;

    private Map<String, List<String>> tablePrimaryKeyMap;

    @Override
    public boolean accept(String tableName, Column column) {
        boolean flag = false;
        // xml configuration contains keys in lower case hence the table key map contains keys and columns in lower case
        String lowerCaseTableName = tableName.toLowerCase();
        String lowerCaseColumnName = column.getColumnName().toLowerCase();
        String[] nameArr = lowerCaseTableName.split("\\.", -1);
        lowerCaseTableName = nameArr[nameArr.length -1];
        List<String> columnList = tablePrimaryKeyMap.get(lowerCaseTableName);
        if (CollectionUtils.isNotEmpty(columnList)) {           
            //sorting required as without it the binary search is unpredictable according to docs
            Collections.sort(columnList);
            int index = Collections.binarySearch(columnList, lowerCaseColumnName, String.CASE_INSENSITIVE_ORDER);
            if (index > -1) {
                flag = true;
            }
        } else {
            if ("id".equals(lowerCaseColumnName)) {
                flag = true;
            }
        }
        return flag;
    }

    @PostConstruct
    public void populateTableColumnMappings() {
        for (Resource resource : resources) {
            populateTableColumnMapping(resource);
        }
    }

    private void populateTableColumnMapping(Resource resource) {
        if (tablePrimaryKeyMap == null) {
            tablePrimaryKeyMap = new HashMap<String, List<String>>();
        }
        try {
            tablePrimaryKeyMap.putAll(XmlUtils.readFromXml(IOUtils.toString(resource.getInputStream()), Map.class));
        } catch (InvalidDataException e) {
            throw new InvalidDataException("InvalidDataException reading the " + resource.getFilename() + " file", e);

        } catch (IOException e) {
            throw new SystemException("IOException reading the " + resource.getFilename() + " file", e);
        }
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

}
