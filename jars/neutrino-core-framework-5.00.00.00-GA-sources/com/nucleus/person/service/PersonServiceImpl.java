package com.nucleus.person.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.person.dao.PersonDao;
import com.nucleus.person.entity.Person;
import com.nucleus.service.BaseServiceImpl;

@Named("personService")
public class PersonServiceImpl extends BaseServiceImpl implements PersonService {

    @Inject
    @Named("personDao")
    private PersonDao personDao;

    @Override
    public void createPerson(Person person) {
        personDao.persist(person);
    }

    @Override
    public Person retrievePerson(Long personId) {
        Person person = personDao.find(Person.class, personId);
        return person;
    }

    /* (non-Javadoc) @see com.nucleus.person.PersonService#updatePerson(com.nucleus.employment.Employee) */
    @Override
    public Person updatePerson(Person p) {
        return personDao.update(p);
    }

}