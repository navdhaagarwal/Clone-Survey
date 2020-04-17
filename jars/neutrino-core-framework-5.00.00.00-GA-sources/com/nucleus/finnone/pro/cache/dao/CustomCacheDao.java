package com.nucleus.finnone.pro.cache.dao;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.cache.entity.CacheBuildHistory;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.cache.entity.ImpactedCacheHistory;
import com.nucleus.finnone.pro.cache.vo.CacheMasterVO;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;
import com.nucleus.persistence.EntityDao;

public interface CustomCacheDao extends EntityDao {

	public ImpactedCache persistImpactedCache(ImpactedCacheVO impactedCacheVO);

	public Long getEntityIdByQuery(String query, Map<String, Object> parameters);
	
	public <T extends BaseEntity> T getEntityByQuery(String query, Map<String, Object> parameters);

	public <T extends BaseEntity> List<Map<Object, T>> getEntityListByQuery(String query);

	public <T extends BaseEntity> Object getCurrentFieldValue(Class<T> entityClass, Long id, String fieldName);

	public <T extends BaseEntity> T findBaseEntityById(Long id, Class<T> entityClass);

	public void moveImpactedCacheToHistory(ImpactedCache impactedCache, ImpactedCacheHistory impactedCacheHistory);

	public CacheBuildHistory persistCacheBuildHistoryEntry(CacheMasterVO cacheMasterVO);

	public List<ImpactedCacheVO> getFailedImpactedCache(DateTime beforeDate);

	public List<ImpactedCache> getImpactedCachesByImpactedCacheVO(ImpactedCacheVO impactedCacheVO, DateTime beforeDate);

	public void updateImpactedCacheLastUpdatedTime(ImpactedCacheVO impactedCacheVO, DateTime currentTime);

	public Long getFailedImpactedCacheCountByGroupName(String groupName, DateTime beforeDate);

	public void updateImpactedCacheLastUpdatedTimeByGroup(String groupName, DateTime currentTime);

}
