package com.nucleus.entityhistory;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;

@Named("entityHistoryService")
public class EntityHistoryServiceImpl extends BaseServiceImpl implements
		EntityHistoryService {

	/*
	 * This query retrieves all records based on condition by giving entityName to it.
	 */
	private static final String ENTITY_BY_UUID_QUERY = "FROM %s en WHERE en.entityLifeCycleData.uuid = :entityUUId";

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Override
	public BaseEntity getBaseEntityByEntityId(
			Class<? extends BaseEntity> entityClass, Long entityId) {

		BaseEntity baseEntity = null;
		if (entityId != null) {
			baseEntity = entityDao.find(entityClass, entityId);
		}
		return baseEntity;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List getBaseEntityByEntityUUID(
			Class<? extends BaseEntity> entityClass, String entityUUId) {

		List baseEntityByUUID = null;
		if (entityUUId != null) {
			JPAQueryExecutor queryExecutor = new JPAQueryExecutor(
					String.format(ENTITY_BY_UUID_QUERY,
							entityClass.getSimpleName())).addParameter(
					"entityUUId", entityUUId);
			baseEntityByUUID = entityDao.executeQuery(queryExecutor);
		}

		return baseEntityByUUID;

	}
}
