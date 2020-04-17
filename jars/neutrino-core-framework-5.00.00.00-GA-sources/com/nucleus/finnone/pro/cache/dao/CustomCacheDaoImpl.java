package com.nucleus.finnone.pro.cache.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.joda.time.DateTime;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.cache.common.CacheManager;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.CacheBuildHistory;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.entity.ImpactedCacheHistory;
import com.nucleus.finnone.pro.cache.vo.CacheMasterVO;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.persistence.EntityDaoImpl;

@Named("customCacheDao")
public class CustomCacheDaoImpl extends EntityDaoImpl implements CustomCacheDao {

	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;
	
	@Override
	public <T extends BaseEntity> T findBaseEntityById(Long id, Class<T> entityClass) {
		return super.find(entityClass, id);
	}

	@Override
	public CacheBuildHistory persistCacheBuildHistoryEntry(CacheMasterVO cacheMasterVO) {
		CacheBuildHistory cacheBuildHistory = new CacheBuildHistory(cacheMasterVO);
		persist(cacheBuildHistory);
		return cacheBuildHistory;
	}
	
	@Override
	public ImpactedCache persistImpactedCache(ImpactedCacheVO impactedCacheVO) {
		ImpactedCache impactedCache = new ImpactedCache();
		impactedCache.setCacheName(impactedCacheVO.getCacheName());
		impactedCache.setRegionName(impactedCacheVO.getRegionName());
		if(impactedCacheVO.getGroupName() != null) {
			impactedCache.setGroupName(impactedCacheVO.getGroupName());
		} else {
			impactedCache.setGroupName(cacheManager
					.getNeutrinoCachePopulatorInstance(impactedCacheVO.getRegionName(), impactedCacheVO.getCacheName())
					.getCacheGroupName());
		}
		persist(impactedCache);
		return impactedCache;
	}

	@Override
	public void moveImpactedCacheToHistory(ImpactedCache impactedCache, ImpactedCacheHistory impactedCacheHistory) {
		ImpactedCache managedImpactedCache = findBaseEntityById(impactedCache.getId(), ImpactedCache.class);
		if (managedImpactedCache != null) {
			persist(impactedCacheHistory);
			delete(managedImpactedCache);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Long getEntityIdByQuery(String query, Map<String, Object> parameters) {
		Query qry = getEntityManager().createQuery(query.intern());
		if (parameters != null) {
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				qry.setParameter(entry.getKey(), entry.getValue());
			}
		}

		Long resultId = null;

		List<Long> idList = qry.getResultList();
		if (ValidatorUtils.hasElements(idList)) {
			resultId = idList.get(0);
		}

		return resultId;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends BaseEntity> T getEntityByQuery(String query, Map<String, Object> parameters) {
		Query qry = getEntityManager().createQuery(query.intern());
		if (parameters != null) {
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				qry.setParameter(entry.getKey(), entry.getValue());
			}
		}

		T entity = null;

		List<T> entityList = qry.getResultList();
		if (ValidatorUtils.hasElements(entityList)) {
			entity = entityList.get(0);
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BaseEntity> List<Map<Object, T>> getEntityListByQuery(String query) {
		Query qry = getEntityManager().createQuery(query.intern());
		return qry.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BaseEntity> Object getCurrentFieldValue(Class<T> entityClass, Long id, String fieldName) {
		String query = new StringBuilder().append("SELECT e.").append(fieldName).append(" FROM ")
				.append(entityClass.getName()).append(" e WHERE e.id = :id ").toString();
		Query qry = getEntityManager().createQuery(query.intern());
		qry.setParameter("id", id);
		List<T> list = qry.getResultList();
		if(list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	
	@Override
	public List<ImpactedCacheVO> getFailedImpactedCache(DateTime beforeDate) {
		NamedQueryExecutor<ImpactedCacheVO> impactedCacheVOExecutor = new NamedQueryExecutor<ImpactedCacheVO>(
				"ImpactedCache.getImpactedCachesVOsBeforeDate").addParameter("beforeDate", beforeDate);
		return executeQuery(impactedCacheVOExecutor);
	}
	
	@Override
	public void updateImpactedCacheLastUpdatedTime(ImpactedCacheVO impactedCacheVO, DateTime currentTime) {
		Query query = getEntityManager().createNamedQuery("ImpactedCache.updateLastUpdatedTime");
		query.setParameter("currentTime", currentTime);
		query.setParameter("regionName", impactedCacheVO.getRegionName());
		query.setParameter("cacheName", impactedCacheVO.getCacheName());
		query.executeUpdate();
	}
	
	@Override
	public void updateImpactedCacheLastUpdatedTimeByGroup(String groupName, DateTime currentTime) {
		Query query = getEntityManager().createNamedQuery("ImpactedCache.updateLastUpdatedTimeByGroupName");
		query.setParameter("currentTime", currentTime);
		query.setParameter("groupName", groupName);
		query.executeUpdate();
	}
	
	@Override
	public List<ImpactedCache> getImpactedCachesByImpactedCacheVO(ImpactedCacheVO impactedCacheVO, DateTime beforeDate) {
		NamedQueryExecutor<ImpactedCache> impactedCacheExecutor = new NamedQueryExecutor<ImpactedCache>(
				"ImpactedCache.getImpactedCachesByCacheAndRegionName")
						.addParameter("regionName", impactedCacheVO.getRegionName())
						.addParameter("cacheName", impactedCacheVO.getCacheName())
						.addParameter("beforeDate", beforeDate);
		return executeQuery(impactedCacheExecutor);
	}
	
	@Override
	public Long getFailedImpactedCacheCountByGroupName(String groupName, DateTime beforeDate) {
		NamedQueryExecutor<Long> impactedCacheVOExecutor = new NamedQueryExecutor<Long>(
				"ImpactedCache.getImpactedCacheCountByGroupNameAndDate").addParameter("groupName", groupName)
						.addParameter("beforeDate", beforeDate);
		return executeQueryForSingleValue(impactedCacheVOExecutor);
	}

}
