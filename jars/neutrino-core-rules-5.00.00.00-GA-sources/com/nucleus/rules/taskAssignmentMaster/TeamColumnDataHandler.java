package com.nucleus.rules.taskAssignmentMaster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.entity.EntityId;
import com.nucleus.rules.model.EntityType;
import com.nucleus.rules.model.assignmentMatrix.AssignmentConstants;
import com.nucleus.rules.service.RuleService;
import com.nucleus.service.BaseServiceImpl;

public class TeamColumnDataHandler extends BaseServiceImpl implements ColumnDataHandler {

	public static final String lastDDETeam = "-5";

    public static final String leastLoadedTeam = "-7";

    @Inject
    @Named("ruleService")
    private RuleService         ruleService;

    @Inject
    @Named("teamService")
    private TeamService         teamService;

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canHandle(Class entityName, Map contextMap) {

        if (Team.class.isAssignableFrom(entityName)) {
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
        String descriptionName = entityType.getDescriptionName();
        Map<String, Object> mapOfUriandDisplayName = new HashMap<String, Object>();

        mapOfUriandDisplayName.put(lastDDETeam, "lastDDETeam");

        mapOfUriandDisplayName.put(leastLoadedTeam, "Least Loaded Team");

        List<Map<String, String>> result = ruleService.searchEntityData(entityName, fields.split(","));

        if (result != null) {

            for (Map<String, String> mapp : result) {
                String id = null;
                String displayName = null;
                Iterator it = mapp.entrySet().iterator();
                while (it.hasNext()) {
                    Entry entry = (Entry) it.next();
                    String key = (String) entry.getKey();
                    String val = String.valueOf(entry.getValue());
                    if ("id".equalsIgnoreCase(key)) {
                        id = val;
                    } 
                    if (StringUtils.isNotEmpty(descriptionName)) {
                        if (descriptionName.equalsIgnoreCase(key)) {
                            displayName = String.valueOf(entry.getValue());
                        }
                    } else {
                        displayName = String.valueOf(entry.getValue());
                    }

                }
                if (displayName != null) {
                    mapOfUriandDisplayName.put(id, displayName);
                }

            }

        }

        return mapOfUriandDisplayName;
    }

    @Override
    public Object handleData(Object value, Map contextMap) {
        String val = String.valueOf(value);
        Team team = null;
        Map<String, Object> resultMap = new HashMap<String, Object>();

        if (!(val.equals(lastDDETeam))) {
            return val;
        }

        if (val.equals(lastDDETeam) && null != contextMap && null != contextMap.get("contextObjectLastDDETeam")) {
                team = entityDao.get(EntityId.fromUri((String) contextMap.get("contextObjectLastDDETeam")));
        }

        if (team != null) {
            resultMap.put(AssignmentConstants.Team.getName(), team.getId());
            return resultMap;
        }

        return val;

    }

}
