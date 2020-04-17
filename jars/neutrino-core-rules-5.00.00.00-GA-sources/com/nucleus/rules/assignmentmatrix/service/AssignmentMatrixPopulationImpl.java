package com.nucleus.rules.assignmentmatrix.service;

import com.nucleus.core.dynamicform.service.AssignmentMatrixPopulation;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.persistence.EntityDao;
import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("assignmentMatrixPopulation")
public class AssignmentMatrixPopulationImpl implements AssignmentMatrixPopulation {

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    @Override
    public List<Map<String, ?>> searchOnAssignmetMaster(String itemVal, String[] searchColumnList, String value) {
        NeutrinoValidator.notNull(searchColumnList, "Columns List cannot be null");
        NeutrinoValidator.notNull(itemVal, "Item value cannot be null");
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

        Map<String, String> selectedProperties = new LinkedHashMap<String, String>();
        StringBuilder dynamicQuery = new StringBuilder();
        dynamicQuery.append("SELECT new Map(");
        for (String columnName : searchColumnList) {
            String alias = columnName;
            if (columnName.contains(".")) {
                alias = columnName.replace(".", "");
            }
            columnName = "assignmentMaster." + columnName;
            selectedProperties.put(columnName, alias);

        }
        String columnName = itemVal;
        String alias = columnName;
        if (columnName.contains(".")) {
            alias = columnName.replace(".", "");
        }
        columnName = "assignmentMaster." + columnName;
        selectedProperties.put(columnName, alias);

        Iterator<Map.Entry<String, String>> iterator = selectedProperties.entrySet().iterator();
        for (; iterator.hasNext() ;) {
            Map.Entry<String, String> nextEntry = iterator.next();
            dynamicQuery.append(nextEntry.getKey()).append(" as ").append(nextEntry.getValue());
            if (iterator.hasNext()) {
                dynamicQuery.append(",");
            }
        }
        dynamicQuery.append(")").append(" FROM AssignmentMaster assignmentMaster WHERE ");

        boolean isFirstClause = true;
        for (String search_col : searchColumnList) {
            if (isFirstClause) {
                dynamicQuery.append("( lower(assignmentMaster.").append(search_col).append(") LIKE lower(:value) ");
                isFirstClause = false;
            } else {
                dynamicQuery.append(" OR lower(assignmentMaster.").append(search_col).append(") LIKE lower(:value) ");
            }

        }
        dynamicQuery.append(") ");
        dynamicQuery.append(" AND assignmentMaster.masterLifeCycleData.approvalStatus IN (:statusList) ");
        dynamicQuery.append(
                " AND (assignmentMaster.entityLifeCycleData.snapshotRecord IS NULL OR assignmentMaster.entityLifeCycleData.snapshotRecord = false) ");
        dynamicQuery.append(" ORDER BY lower(" + searchColumnList[0] + "),lower(" + searchColumnList[1] + ")");
        JPAQueryExecutor jpaQueryExecutor = new JPAQueryExecutor<Map<String, ?>>(dynamicQuery.toString());
        jpaQueryExecutor.addParameter("value", "%" + value + "%");
        jpaQueryExecutor.addParameter("statusList", statusList);

        List<Map<String, ?>> result = entityDao.executeQuery(jpaQueryExecutor);

        return result;
    }
}
