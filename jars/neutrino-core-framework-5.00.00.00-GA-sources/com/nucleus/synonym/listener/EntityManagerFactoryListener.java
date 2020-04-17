package com.nucleus.synonym.listener;

import javax.persistence.EntityManagerFactory;

public interface EntityManagerFactoryListener {
	
	public void process(EntityManagerFactory emf);

}
