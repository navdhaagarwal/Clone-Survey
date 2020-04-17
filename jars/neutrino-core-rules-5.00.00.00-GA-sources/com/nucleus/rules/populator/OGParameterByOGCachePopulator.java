package com.nucleus.rules.populator;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.dao.ParameterDao;
import com.nucleus.rules.model.ObjectGraphParameter;
import com.nucleus.rules.model.PlaceHolderParameter;

@Named("oGParameterByOGCachePopulator")
public class OGParameterByOGCachePopulator extends FWCachePopulator {

	@Inject
	@Named("parameterDao")
	private ParameterDao parameterDao;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : OGParameterByOGCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		List<ObjectGraphParameter> listOfAllParameters = getObjectGraphParametersByObjectGraph((String) key);
		if (listOfAllParameters != null) {
			for (ObjectGraphParameter parameter : listOfAllParameters) {
				try {
					if (parameter.getName() != null) {
						return parameter.getId();
					}
				} catch (Exception e) {
					BaseLoggers.exceptionLogger
							.error(" Error occured in fallback of OG Parameter by OG cache for Parameter Id:: "
									+ parameter.getId());
				}

			}
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		List<ObjectGraphParameter> listOfAllParameters = parameterDao
				.getAllParametersFromDB(ObjectGraphParameter.class);
		if (listOfAllParameters == null) {
			return;
		}
		for (ObjectGraphParameter parameter : listOfAllParameters) {
			try {
				if (parameter.getName() != null) {
					if (parameter instanceof PlaceHolderParameter
							&& ((PlaceHolderParameter) parameter).getContextName() != null) {
						put(((PlaceHolderParameter) parameter).getContextName(), parameter.getId());
					} else if (parameter.getObjectGraph() != null) {
						put(parameter.getObjectGraph(), parameter.getId());
					}
				}
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(
						" Error occured in build of OG Parameter by OG cache for Parameter Id:: " + parameter.getId());

			}

		}

	}

	private List<ObjectGraphParameter> getObjectGraphParametersByObjectGraph(String objectGraph) {
		NamedQueryExecutor<ObjectGraphParameter> executor = new NamedQueryExecutor<ObjectGraphParameter>(
				"Rules.findApprovedOGParameterByObjectGraph")
						.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
						.addParameter("objectGraph", objectGraph).addQueryHint(QueryHint.QUERY_HINT_FETCHSIZE, 500);
		return parameterDao.executeQuery(executor);

	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.INSERT) && ValidatorUtils.notNull(object)) {
			putAll(object);
		} else if (action.equals(Action.DELETE) && ValidatorUtils.notNull(object)) {
			remove(object);
		}

	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.OG_PARAMETER_BY_OG;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.PARAMETER_CACHE_GROUP;
	}

}
