package com.nucleussoft.reactive.dummy;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.address.Country;
import com.nucleus.persistence.EntityDao;

@Service
public class ReactorService {

	
    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;
    

	@Transactional(readOnly=true)
	public Country getCountryById(Long id){
		return entityDao.find(Country.class, id);
	}
}
