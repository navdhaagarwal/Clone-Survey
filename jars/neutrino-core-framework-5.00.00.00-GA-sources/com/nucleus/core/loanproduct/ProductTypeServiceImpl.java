package com.nucleus.core.loanproduct;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.transaction.annotation.Transactional;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;

@Transactional(readOnly=true)
@Named("productTypeService")
public class ProductTypeServiceImpl implements ProductTypeService {

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Override
    public List<?> getAllActiveProductTypes(String... columnNameList) {

        StringBuilder dynamicQuery = new StringBuilder();
        String qlString = null;

        String QuerySelect = "Select new Map(entityClass.id as id";
        dynamicQuery.append(QuerySelect);
        if (columnNameList != null) {
            for (String columnName : columnNameList) {
                dynamicQuery.append(", ");
                dynamicQuery.append("entityClass.").append(columnName).append(" as ").append(columnName);
            }
        }
        String QueryFromWhere = ") " + "FROM  ProductType  entityClass WHERE ";
        String dynamicQueryForBaseEntity = null;
        dynamicQuery.append(QueryFromWhere);
        if (columnNameList != null) {
            String lastColumnValue = columnNameList[columnNameList.length - 1];
            dynamicQueryForBaseEntity = "(entityClass.entityLifeCycleData.snapshotRecord is null or entityClass.entityLifeCycleData.snapshotRecord = false) AND (isActive is null OR isActive NOT IN ('n','N')) ORDER BY entityClass."
                    + lastColumnValue;
        } else {
            dynamicQueryForBaseEntity = "(entityClass.entityLifeCycleData.snapshotRecord is null or entityClass.entityLifeCycleData.snapshotRecord = false) AND (isActive is null OR isActive NOT IN ('n','N'))";
        }

        qlString = dynamicQuery.append(dynamicQueryForBaseEntity).toString();
        JPAQueryExecutor<Map<String, Object>> jpaQueryExecutor = new JPAQueryExecutor<Map<String, Object>>(qlString)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(jpaQueryExecutor);

    }

}
