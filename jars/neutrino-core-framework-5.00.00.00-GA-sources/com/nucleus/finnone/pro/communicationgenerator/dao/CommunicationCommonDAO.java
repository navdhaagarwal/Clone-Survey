package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.Entity;
import com.nucleus.persistence.DaoUtils;
import com.nucleus.persistence.EntityDao;
import com.nucleus.persistence.EntityDaoImpl;

@Repository("communicationCommonDAO")
public class CommunicationCommonDAO extends EntityDaoImpl implements ICommunicationCommonDAO{
	
	@Override
	public <T extends Entity> T findMasterByCode(Class<T> entityClass, Map<String, Object> variablesMap)
	  {
	    NeutrinoValidator.notNull(entityClass, "entityClass can not be null");
	    StringBuilder dynamicQuery = new StringBuilder();
	    String qlString = new StringBuilder().append("FROM ").append(entityClass.getSimpleName()).append(" masterEntity WHERE ").append("(masterEntity.entityLifeCycleData.snapshotRecord is null OR masterEntity.entityLifeCycleData.snapshotRecord = false)").toString();
	    StringBuilder addParameters = null;
	    if ((variablesMap != null) && (!(variablesMap.isEmpty())))
	    {
	      addParameters = new StringBuilder();
	      for (Iterator i$ = variablesMap.entrySet().iterator(); i$.hasNext(); ) { Map.Entry paramEntry = (Map.Entry)i$.next();
	        addParameters.append(" AND ");
	        addParameters.append(new StringBuilder().append("masterEntity.").append((String)paramEntry.getKey()).append("='").append(paramEntry.getValue()).append("'").toString());
	      }
	    }

	    dynamicQuery.append(qlString);
	    if (addParameters != null)
	    {
	      dynamicQuery.append(addParameters);
	    }

	    Query qry = getEntityManager().createQuery(dynamicQuery.toString());
	    List entities = DaoUtils.executeQuery(getEntityManager(), qry);
	    if (CollectionUtils.isNotEmpty(entities))
	      return (T) ((Entity)entities.get(0));

	    return null;
	  }
	
}
