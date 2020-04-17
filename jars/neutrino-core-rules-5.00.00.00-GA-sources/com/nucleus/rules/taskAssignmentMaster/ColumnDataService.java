/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.taskAssignmentMaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nucleus.core.team.entity.Team;
import com.nucleus.service.BaseServiceImpl;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
public class ColumnDataService extends BaseServiceImpl {

    private List<ColumnDataHandler> columnDataHandlers;

    public List<ColumnDataHandler> getColumnDataHandlers() {
        return columnDataHandlers;
    }

    public void setColumnDataHandlers(List<ColumnDataHandler> columnDataHandlers) {
        this.columnDataHandlers = columnDataHandlers;
    }

    @SuppressWarnings("rawtypes")
    public Map<String, Object> populateDropDown(Class entityClass, Map contextMap) {
        Map<String, Object> mapOfIdandDisplayName = new HashMap<String, Object>();
        for (ColumnDataHandler columnDataHandler : getColumnDataHandlers()) {
            if (columnDataHandler.canHandle(entityClass, contextMap)) {
                mapOfIdandDisplayName = columnDataHandler.fetchData(entityClass);
                return mapOfIdandDisplayName;
            }
        }

        return mapOfIdandDisplayName;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, Object> populateResultMap(Map<String, Object> fieldKeyValueMap, Map contextMap)
            throws ClassNotFoundException {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String key = null;
        Object value;
        Class entityClass;
        Object val;
        for (Entry entry : fieldKeyValueMap.entrySet()) {
            key = (String) entry.getKey();
            value = entry.getValue();
            entityClass = Class.forName(key);
            
            String teamLeadId = (String) fieldKeyValueMap.get("com.nucleus.user.User");
            if(teamLeadId!=null){
                if(teamLeadId.equalsIgnoreCase("-6")){
                    String teamId = (String)fieldKeyValueMap.get("com.nucleus.core.team.entity.Team");
                    Team team = entityDao.find(Team.class, Long.valueOf(teamId));
                    if(team != null && team.getTeamLead() != null){
                        contextMap.put("teamLead", team.getTeamLead());
                    }
                }
            }
            for (ColumnDataHandler columnDataHandler : getColumnDataHandlers()) {
                if (columnDataHandler.canHandle(entityClass, contextMap)) {
                    val = columnDataHandler.handleData(value, contextMap);
                    if (val instanceof java.util.HashMap) {
                        updateResultMap(resultMap, (Map<String, Object>) val);
                        break;
                    }
                    resultMap.put(key, val);
                    break;
                }
            }

        }

        return resultMap;

    }

    @SuppressWarnings("rawtypes")
    private void updateResultMap(Map<String, Object> resultMap, Map<String, Object> valueMap) {
        for (Entry entry : valueMap.entrySet()) {
            resultMap.put((String) entry.getKey(), entry.getValue());

        }

    }

}
