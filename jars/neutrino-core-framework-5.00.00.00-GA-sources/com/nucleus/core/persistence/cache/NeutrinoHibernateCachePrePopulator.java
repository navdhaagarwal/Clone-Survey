/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.persistence.cache;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;
import javax.xml.bind.JAXBContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import com.nucleus.persistence.DaoUtils;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;

/**
 * Class to load all cacheable entities in second level cache on server startup
 * 
 * @author Nucleus Software Exports Limited
 * 
 */
public class NeutrinoHibernateCachePrePopulator implements InitializingBean {

    private static final Logger LOGGER            = LoggerFactory.getLogger(NeutrinoHibernateCachePrePopulator.class);

    //to exclude lms entities from cache on startup
    private static final String LMS_ENTITY_PREFIX = "com.nucleus.finnone";

    private static final String SELECT_ALL_QUERY  = "FROM  %s  baseEntity WHERE (baseEntity.entityLifeCycleData.snapshotRecord is null OR baseEntity.entityLifeCycleData.snapshotRecord = false)";

    @Inject
    @Named("entityDao")
    protected EntityDao         entityDao;

    private Resource            cachePrePopulatorConfig;

    @SuppressWarnings({ "rawtypes" })
    public void prepopulateAllCacheableEntities(CachePrePopulatorConfig config) {

        Session session = (Session) entityDao.getEntityManager().getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        SessionFactoryImplementor factoryImplementor = (SessionFactoryImplementor) sessionFactory;

        Map<String, ClassMetadata> classMetadata = factoryImplementor.getAllClassMetadata();

        LOGGER.debug("No. of total entities mapped {}", classMetadata.size());
        int count = 0;
        for (Entry<String, ClassMetadata> entry : classMetadata.entrySet()) {

            try {
                String entityName = entry.getKey();
                ClassMetadata metadata = entry.getValue();
                Class clazz = metadata.getMappedClass();
                if (!Modifier.isAbstract(clazz.getModifiers()) && DaoUtils.isEntityCacheable(clazz)
                        && !clazz.getName().startsWith(LMS_ENTITY_PREFIX)) {
                    if (!excludeClass(clazz, config)) {
                        count++;
                        LOGGER.debug("Loading Cacheable entity {}.#={}", entityName, count);

                        Query query = entityDao.getEntityManager().createQuery(String.format(SELECT_ALL_QUERY, entityName));
                        query.setHint(QueryHint.QUERY_HINT_CACHEABLE, true);
                        List list = query.getResultList();
                        LOGGER.debug("No. of results returned for Cacheable entity {} ", list.size());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error in Loading Cacheable entity", e);
            }

        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        LOGGER.info("Loading all Cacheable entities on server startup.");
        try {
            JAXBContext context = JAXBContext.newInstance("com.nucleus.core.persistence.cache");
            CachePrePopulatorConfig config = (CachePrePopulatorConfig) context.createUnmarshaller().unmarshal(
                    cachePrePopulatorConfig.getInputStream());

            if (config != null) {
                LOGGER.info("Excluding packages from CachePrePopulator:{}", config.getExcludePackage());
                LOGGER.info("Excluding classes from CachePrePopulator:{}", config.getExcludeClass());
            }

            prepopulateAllCacheableEntities(config);
        } catch (Exception e) {
            LOGGER.error("Error in Loading all Cacheable entities on server startup", e);
        }
    }

    private boolean excludeClass(Class clazz, CachePrePopulatorConfig config) {

        if (config != null) {

            if (config.getExcludePackage() != null) {
                for (String excludedPackageName : config.getExcludePackage()) {
                    if (clazz.getName().startsWith(excludedPackageName)) {
                        LOGGER.info("Class {} excluded from CachePrePopulator due to package {} exclude in config.",
                                clazz.getName(), excludedPackageName);
                        return true;
                    }
                }

            }

            if (config.getExcludeClass() != null && config.getExcludeClass().contains(clazz.getName())) {
                LOGGER.info("Class {} excluded from CachePrePopulator due to class exclude in config.", clazz.getName());
                return true;
            }
        }

        return false;

    }

    public Resource getCachePrePopulatorConfig() {
        return cachePrePopulatorConfig;
    }

    public void setCachePrePopulatorConfig(Resource cachePrePopulatorConfig) {
        this.cachePrePopulatorConfig = cachePrePopulatorConfig;
    }
}
