/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.taskAssignmentMaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.EntityType;
import com.nucleus.rules.service.BaseRuleServiceImpl;
import com.nucleus.rules.service.RuleService;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
public class DefaultColumnDataHandler extends BaseRuleServiceImpl implements ColumnDataHandler {

    @Inject
    @Named("ruleService")
    private RuleService ruleService;

    @Override
    public boolean canHandle(Class entityClass, Map contextMap) {

        if (BaseEntity.class.isAssignableFrom(entityClass)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map<String, Object> fetchData(Class entityName) {

        String packageName = entityName.getName();
        EntityType entityType = ruleService.getEntityTypeData(packageName);

        String fields = entityType.getFields();
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        Map<String, Object> mapOfUriandDisplayName = new HashMap<String, Object>();

        result = ruleService.searchEntityData(entityName, fields.split(","));

        if (result != null) {

            for (Map<String, String> mapp : result) {
                String id = null;
                String displayName = null;
                Iterator it = mapp.entrySet().iterator();
                while (it.hasNext()) {
                    Entry entry = (Entry) it.next();
                    String key = (String) entry.getKey();
                    String val = String.valueOf(entry.getValue());
                    if (key.equalsIgnoreCase("id")) {
                        id = val;
                    } else {
                        displayName = String.valueOf(entry.getValue());
                    }

                }
                mapOfUriandDisplayName.put(id, displayName);

            }

        }
        return mapOfUriandDisplayName;

    }

    @Override
    public Object handleData(Object value, Map contextMap) {
        return value;
    }

}
