package com.nucleus.persistence;

import org.hibernate.search.indexes.interceptor.EntityIndexingInterceptor;
import org.hibernate.search.indexes.interceptor.IndexingOverride;

public class DoNotIndexEntityIndexingInterceptor implements EntityIndexingInterceptor {

	@Override
	public IndexingOverride onAdd(Object entity) {
		return IndexingOverride.SKIP;
	}

	@Override
	public IndexingOverride onUpdate(Object entity) {
		return IndexingOverride.SKIP;
	}

	@Override
	public IndexingOverride onDelete(Object entity) {
		return IndexingOverride.SKIP;
	}

	@Override
	public IndexingOverride onCollectionUpdate(Object entity) {
		return IndexingOverride.SKIP;
	}

}
