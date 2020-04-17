package com.nucleus.authority.dao;

import java.util.List;

import javax.inject.Named;

import com.nucleus.authority.Authority;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.BaseDaoImpl;

@Named(value = "AuthorityDao")
public class AuthorityDaoImpl extends BaseDaoImpl<Authority> implements AuthorityDao {

	private static final String SEARCHED_STRING = "searchedString";
	private static final String STATUS = "status";
	private static final String ERROR_STRING = "Error in loading authority data: ";
	private static final  String GET_ALL_AUTHORITIES_WITH_QUERY = "Authority.GetAllAuthoritiesWithQuery";
	private static final  String GET_ALL_AUTHORITIES_FILTERED_WITH_QUERY = "Authority.GetAllAuthoritiesFilteredWithQuery";
	private static final  String GET_ALL_AUTHORITIES_SOURCE_PRODUCT_WITH_QUERY = "Authority.GetAllAuthoritiesOfSourceProductWithQuery";
	private static final  String GET_ALL_AUTHORITIES_SOURCE_PRODUCT_FILTERED_WITH_QUERY = "Authority.GetAllAuthoritiesOfSourceProductFilteredWithQuery";

	@Override
	public List<Authority> getAllAuthoritiesWithQuery(String queryString) {
		List<Authority> authorityList = null;
		try {
			NamedQueryExecutor<Authority> executor = new NamedQueryExecutor<Authority>(GET_ALL_AUTHORITIES_WITH_QUERY)
					.addParameter(SEARCHED_STRING, getLikeQueryString(queryString))
					.addParameter(STATUS, PersistenceStatus.INACTIVE);
			authorityList = this.executeQuery(executor);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error(ERROR_STRING, e.getMessage());
		}
		return authorityList;
	}

	@Override
	public List<Authority> getAllAuthoritiesWithQueryExcludingAuthorityID(List<Long> authorityIdList,
			String queryString) {
		List<Authority> authorityList = null;
		try {
			NamedQueryExecutor<Authority> executor = new NamedQueryExecutor<Authority>(
					GET_ALL_AUTHORITIES_FILTERED_WITH_QUERY)
							.addParameter(SEARCHED_STRING, getLikeQueryString(queryString))
							.addParameter("authorityIDList", authorityIdList)
							.addParameter(STATUS, PersistenceStatus.INACTIVE);
			authorityList = this.executeQuery(executor);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error(ERROR_STRING, e.getMessage());
		}
		return authorityList;
	}

	@Override
	public List<Authority> getAllAuthorityOfSourceProductWithQuery(Long sourceProductID, String queryString) {
		List<Authority> authorityList = null;
		try {
			NamedQueryExecutor<Authority> executor = new NamedQueryExecutor<Authority>(
					GET_ALL_AUTHORITIES_SOURCE_PRODUCT_WITH_QUERY).addParameter("sourceProductId", sourceProductID)
							.addParameter(SEARCHED_STRING, getLikeQueryString(queryString))
							.addParameter(STATUS, PersistenceStatus.INACTIVE);
			authorityList = this.executeQuery(executor);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error(ERROR_STRING, e.getMessage());
		}
		return authorityList;
	}

	@Override
	public List<Authority> getAllAuthorityOfSourceProductWithQueryExcludingAuthorityID(Long sourceProductID,
			List<Long> authorityIdList, String queryString) {
		List<Authority> authorityList = null;
		try {
			NamedQueryExecutor<Authority> executor = new NamedQueryExecutor<Authority>(
					GET_ALL_AUTHORITIES_SOURCE_PRODUCT_FILTERED_WITH_QUERY)
							.addParameter(SEARCHED_STRING, getLikeQueryString(queryString))
							.addParameter("sourceProductId", sourceProductID)
							.addParameter("authorityIDList", authorityIdList)
							.addParameter(STATUS, PersistenceStatus.INACTIVE);
			authorityList = this.executeQuery(executor);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error(ERROR_STRING, e.getMessage());
		}
		return authorityList;
	}

	private String getLikeQueryString(String searchedString) {
		return "%" + searchedString.toLowerCase() + "%";
	}

}
