package org.springframework.orm.jpa;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nucleus.synonym.listener.EntityManagerFactoryListener;

public class CustomEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean{
	
	@Autowired(required=false)
	private List<EntityManagerFactoryListener> listenerList;

	protected void postProcessEntityManagerFactory(EntityManagerFactory emf, PersistenceUnitInfo pui) {
		if(CollectionUtils.isNotEmpty(listenerList) && listenerList.size() > 0){
			for(EntityManagerFactoryListener listener : listenerList){
				listener.process(emf);
			}
		}
	}

}
