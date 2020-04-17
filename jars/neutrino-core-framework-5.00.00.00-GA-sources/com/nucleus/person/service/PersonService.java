package com.nucleus.person.service;

import com.nucleus.person.entity.Person;
import com.nucleus.service.BaseService;

public interface PersonService extends BaseService {

    public void createPerson(Person person);

    public Person retrievePerson(Long id);

    public Person updatePerson(Person p);

}
