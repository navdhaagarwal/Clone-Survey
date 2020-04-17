package com.nucleus.person.dao;

import com.nucleus.persistence.BaseDao;
import com.nucleus.person.entity.Person;

public interface PersonDao extends BaseDao<Person>{
	
	/**
	 * This method is just for testing currently. To be removed later.
	 */
	public long findTotalNumberOfPersonRecordsInDatabase();

}
