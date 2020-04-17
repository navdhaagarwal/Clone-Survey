package com.nucleus.rules.simulation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.dao.query.MapQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.EntityType;
import com.nucleus.rules.service.RuleService;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Default Rule Simulation for handeling Rule Simulation for All Entity Types
 */
@Named("defaultRuleSimulationProcess")
public class DefaultRuleSimulationProcess implements RuleSimulationProcess {

    @Inject
    @Named("entityDao")
    private EntityDao   entityDao;

    @Named(value = "ruleService")
    private RuleService ruleService;

    @Override
    public List<BaseEntity> listEntityProcess(EntityType entityType, EntityTypeFilterCriteria entityTypeFilterCriteria)
            throws ClassNotFoundException {

        String className = entityType.getClassName();
        Class entityClass = Class.forName(className);
        String fields = entityType.getFields();

        List<BaseEntity> entityList = new ArrayList<BaseEntity>();

        entityList = searchEntityData(entityClass, fields.split(","), entityTypeFilterCriteria);

        return entityList;

    }

    @Override
    public <T extends BaseEntity> Map<Object, Object> populateContextObject(T baseEntity, Class<T> entityClass) {
        Map<Object, Object> contextMap = new HashMap<Object, Object>();
        contextMap.put("contextObject" + entityClass.getSimpleName(), baseEntity);
        return contextMap;
    }

    @Override
    public boolean canHandleEntity(Class<? extends BaseEntity> entityClass) {

        NamedQueryExecutor<Integer> parameterExecutor = new NamedQueryExecutor<Integer>("Rules.canHandleEntityType")
                .addParameter("className", entityClass.getSimpleName()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE,
                        Boolean.TRUE);
        List<Integer> count = entityDao.executeQuery(parameterExecutor);
        if (null != count) {
            return (count.get(0) > 0) ? true : false;
        } else {
            return false;
        }

    }

    private <T extends BaseEntity> List searchEntityData(Class<T> entityClass, String[] fields,
            EntityTypeFilterCriteria entityTypeFilterCriteria) {
        List<Map<String, ?>> result = null;
        List<Map<String, ?>> countResult = null;

        MapQueryExecutor executor = new MapQueryExecutor(entityClass);
        MapQueryExecutor countExecutor = new MapQueryExecutor(entityClass);

        if (fields != null) {
            for (String colName : fields) {
                executor.addQueryColumns(colName);
            }
            executor.addQueryColumns("id");

            countExecutor.addQueryColumns("id");
            boolean isBaseMasterEntity = BaseMasterEntity.class.isAssignableFrom(entityClass);
            if (isBaseMasterEntity) {
                String baseMasterString = "masterLifeCycleData.approvalStatus in (" + ApprovalStatus.APPROVED + " )";
                executor.addAndClause(baseMasterString);
                countExecutor.addAndClause(baseMasterString);

            }
            String searchQueryString = "";
            for (String colName : fields) {
                searchQueryString += " lower(" + colName + ") like :value OR"; 
            }
            searchQueryString = searchQueryString.substring(0, searchQueryString.length() - 3);  // Remove last OR
            executor.addAndClause(searchQueryString);
            executor.addBoundParameter("value", "%"+entityTypeFilterCriteria.getsSearch().toLowerCase()+"%");
            executor.addAndClause("entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false");

            countExecutor.addAndClause(searchQueryString);
            countExecutor.addBoundParameter("value", "%"+entityTypeFilterCriteria.getsSearch().toLowerCase()+"%");
            countExecutor
                    .addAndClause("entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false");

            if(entityTypeFilterCriteria.getiDisplayStart() == 0 && entityTypeFilterCriteria.getiDisplayLength() == 0){
            	result = entityDao.executeQuery(executor);
            }else{
            	result = entityDao.executeQuery(executor, entityTypeFilterCriteria.getiDisplayStart(),
                        entityTypeFilterCriteria.getiDisplayLength());
            	countResult = entityDao.executeQuery(countExecutor);

                int listSize = countResult.size();
                entityTypeFilterCriteria.setiTotalDisplayRecords(listSize);
                entityTypeFilterCriteria.setiTotalRecords(listSize);
            }

        }
        return result;

    }

}
