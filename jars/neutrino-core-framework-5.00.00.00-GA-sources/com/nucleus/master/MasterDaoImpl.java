package com.nucleus.master;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.apache.commons.collections4.CollectionUtils;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.Entity;
import com.nucleus.persistence.DaoUtils;
import com.nucleus.persistence.EntityDao;


@Named("masterDao")
public class MasterDaoImpl implements MasterDao{
	
	@Inject
    @Named("entityDao")
    protected EntityDao                     entityDao;
	
	private final  String PARAM="PARAM";
	
	@Override 
 public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap) {
	    NeutrinoValidator.notNull(entityClass, "Application Id can not be null for upadting persistence status.");
	    StringBuilder dynamicQuery = new StringBuilder();
	    StringBuilder qlString = new StringBuilder();
	     qlString .append( "FROM ")
	     .append( entityClass.getSimpleName())
	     .append( " masterEntity WHERE ".intern())
	     .append( "(masterEntity.entityLifeCycleData.snapshotRecord is null OR masterEntity.entityLifeCycleData.snapshotRecord = false)".intern());

	    StringBuilder addParameters = null;
	    Map<Integer, Object> parameteMap = null;
	    if (variablesMap != null && !variablesMap.isEmpty()) {
	        addParameters = new StringBuilder();
	        parameteMap=new HashMap<Integer, Object>();
	        for (Entry<String, Object> paramEntry : variablesMap.entrySet()) {
	            addParameters.append(" AND ");
	            addParameters.append("masterEntity." + paramEntry.getKey()).append("= :").append(PARAM).append(parameteMap.size());
	            parameteMap.put(parameteMap.size(), paramEntry.getValue());
	        }
	    }
	    dynamicQuery.append(qlString);
	    if (addParameters != null) {
	        dynamicQuery.append(addParameters);
	    }

	    Query qry = entityDao.getEntityManager().createQuery(dynamicQuery.toString());
	    qry.setHint("org.hibernate.cacheable", Boolean.TRUE);
	    if (parameteMap != null && !parameteMap.isEmpty()) {	     
	        for (Entry<Integer, Object> paramEntry : parameteMap.entrySet()) {
	        	qry.setParameter((PARAM+paramEntry.getKey()).intern(), paramEntry.getValue());
	        }
	    }
	    
	    List<T> entities = DaoUtils.executeQuery(entityDao.getEntityManager(), qry);
	    if (CollectionUtils.isNotEmpty(entities)) {
	        return entities.get(0);
	    }
	    return null;
	}



}
