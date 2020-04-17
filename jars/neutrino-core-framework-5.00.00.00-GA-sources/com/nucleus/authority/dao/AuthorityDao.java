package com.nucleus.authority.dao;

import java.util.List;

import com.nucleus.authority.Authority;

public interface AuthorityDao {

	List<Authority> getAllAuthoritiesWithQuery(String queryString);

	List<Authority> getAllAuthoritiesWithQueryExcludingAuthorityID(List<Long> authorityIdList, String queryString);

	List<Authority> getAllAuthorityOfSourceProductWithQuery(Long sourceProductID, String queryString);

	List<Authority> getAllAuthorityOfSourceProductWithQueryExcludingAuthorityID(Long sourceProductID,
			List<Long> authorityIdList, String queryString);
}
