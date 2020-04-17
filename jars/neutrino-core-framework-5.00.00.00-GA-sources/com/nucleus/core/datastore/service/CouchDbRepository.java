package com.nucleus.core.datastore.service;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;

public class CouchDbRepository extends CouchDbRepositorySupport<CouchDataEntity> {

	public CouchDbRepository(Class<CouchDataEntity> class1, CouchDbConnector db) {
		super(class1, db);
		// TODO Auto-generated constructor stub
	}

}