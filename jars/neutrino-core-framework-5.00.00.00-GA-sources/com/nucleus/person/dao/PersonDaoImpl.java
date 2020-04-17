package com.nucleus.person.dao;

import javax.inject.Named;

import com.nucleus.persistence.BaseDaoImpl;
import com.nucleus.person.entity.Person;

@Named("personDao")
public class PersonDaoImpl extends BaseDaoImpl<Person> implements PersonDao{

	@Override
	public long findTotalNumberOfPersonRecordsInDatabase() {
		return getJdbcTemplate().queryForObject("select count(*) from person", Long.class);
	}
	

}