package com.nucleus.persistence;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanCreationNotAllowedException;

import com.nucleus.core.datetime.entity.TimeZoneAESUtil;
import com.nucleus.core.datetime.entity.TimeZoneDetails;
import com.nucleus.core.datetime.entity.TimeZoneService;

/**
 * 
 * This is a post processor for JPA Persistence properties
 * 
 * If you want to add any validation to an existing property in
 * database-config.properties or EntityManager settings in general, this is the
 * class you should consider.
 * 
 * 
 * @since GA 2.5
 * @author prateek.chachra
 *
 */
public class JPAPropertyValidationBeanPostProcessor {

	private static final String ENTITY_MANAGER_CONST = "entityManagerFactory";
	
	
	@Inject
	@Named("timeZoneService")
	private TimeZoneService timeZoneService;

	private EntityManagerFactory entityManagerFactory;

	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	/**
	 * As of now, the property being tested for and checked is database zone.
	 * 
	 * 
	 */
	@PostConstruct
	public void postProcessAfterInitialization() {
		
		Map<String, Object> properties = entityManagerFactory.getProperties();
		String applyTimeZoneConfig = (String) properties.get("timezone.config.applicable");
		if("true".equals(applyTimeZoneConfig)){
			String databaseTimeZone = (String) properties.get("jadira.usertype.databaseZone");
			String dbZone;
			TimeZoneDetails dbTimeZoneDetails;
			if (StringUtils.isNotBlank(databaseTimeZone)) {

				if ((dbTimeZoneDetails = timeZoneService.getExistingTimeZone()) == null) {
					dbTimeZoneDetails = new TimeZoneDetails();
					dbTimeZoneDetails.setDatabaseZone(TimeZoneAESUtil.encrypt(databaseTimeZone));
					timeZoneService.persist(dbTimeZoneDetails);
				}
				dbZone = TimeZoneAESUtil.decrypt(dbTimeZoneDetails.getDatabaseZone());
				
				if (!databaseTimeZone.equals(dbZone)) {
					throw new BeanCreationNotAllowedException(ENTITY_MANAGER_CONST,
							"Persistence unit was provided with a different time zone than was setup with : " + dbZone);
				}
			}

			else {
				throw new BeanCreationNotAllowedException(ENTITY_MANAGER_CONST,
						"No value provided for jadira.usertype.databaseZone, please check your database configuration");
			}
		}
		
		else {

			TimeZoneDetails dbTimeZoneDetails;
			if ((dbTimeZoneDetails = timeZoneService.getExistingTimeZone()) != null) {
				throw new BeanCreationNotAllowedException(ENTITY_MANAGER_CONST,
						"Setup Already done with existing Time Zone property : " + dbTimeZoneDetails.getDatabaseZone());
			
			}
			
		}
		
	}

}
