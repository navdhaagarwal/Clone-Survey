/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.persistence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.proxy.HibernateProxy;

import com.nucleus.logging.BaseLoggers;

/**
 * The Class DaoUtils.
 *
 * @author Nucleus Software Exports Limited
 */
public class DaoUtils {

    /**
     * Execute query making use of second level cache.
     *
     * @param query the query
     * @return the list
     */
    public static List executeQuery(EntityManager em, Query query) {
        /*org.hibernate.Query hibernateQuery = query.unwrap(org.hibernate.Query.class);
        Type[] types = hibernateQuery.getReturnTypes();
        if (types.length == 1) {
            Type type = types[0];
            Class<?> returnedClass = type.getReturnedClass();
            BaseLoggers.persistenceLogger.trace("Returned entity class is '{}' for query --> {} ", returnedClass.getName(),
                    hibernateQuery.getQueryString());
            if (Entity.class.isAssignableFrom(returnedClass)) {
                BaseLoggers.persistenceLogger.trace("Entity '{}' is assignable with Entity.class", returnedClass.getName());
                if (isEntityCacheable(returnedClass)) {
                    Iterator<Entity> it = hibernateQuery.iterate();
                    List<Entity> entities = new ArrayList<Entity>();
                    // Iterate and initialize the proxies retured by hibernate query.iterate()
                    while (it.hasNext()) {
                        Entity entity = it.next();
                        entity = (Entity) em.find(returnedClass, entity.getId());
                        if (entity instanceof HibernateProxy) {
                            entity = (Entity) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
                        }
                        entities.add(entity);
                    }
                    return entities;
                }
            } else {
                BaseLoggers.persistenceLogger.trace("Entity '{}' is not assignable with Entity.class",
                        returnedClass.getName());
            }
        } else {
            BaseLoggers.persistenceLogger.trace(
                    "Multiple entities being returned by the query --> {}. Executing direct query from database.",
                    hibernateQuery.getQueryString());
        }*/

        List entities = new ArrayList();
        List resultList = query.getResultList();
        if (CollectionUtils.isNotEmpty(resultList)) {
            Iterator it = resultList.iterator();
            while (it.hasNext()) {
                Object entity = it.next();
                // entity = (Entity) em.find(returnedClass, entity.getId());
                if (entity instanceof HibernateProxy) {
                    entity = ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
                }
                entities.add(entity);
            }
        }
        return entities;
    }

    /**
     * Checks if the passed entity class is cacheable.
     * 
     * @see {@link #getCacheableAnnotationValue(Class)}
     * @param underlyingClass the underlying class
     * @return true, if this entity is cacheable
     */
    public static boolean isEntityCacheable(Class<?> underlyingClass) {
        boolean annotationValue = false;
        BaseLoggers.persistenceLogger.trace("Fetching the cacheable annotation value for class '{}'",
                underlyingClass.getName());
        try {
            annotationValue = getCacheableAnnotationValue(underlyingClass);
        } catch (NullPointerException npe) {
            // Handling exception as it can be due to @Cacheable annotation not present
            Class<?> parentClass = underlyingClass.getSuperclass();
            if (parentClass != null) {
                annotationValue = isEntityCacheable(parentClass);
            }
        }
        BaseLoggers.persistenceLogger.trace("Class '{}' is '{}'", underlyingClass.getName(),
                annotationValue == true ? "cacheable" : "not cacheable");
        return annotationValue;
    }

    /**
     * Gets the {@link Cacheable} annotation value.
     * <p/><b><u>Note</u>:</b>This method will throw NullPointerException
     * in an attempt to fetch annotation value without checking whether {@link Cacheable}
     * annotation is present on the class or not
     *
     * @param currentClass the current class
     * @return the {@link Cacheable} value
     */
    private static boolean getCacheableAnnotationValue(Class<?> currentClass) {
        Cacheable cacheable = currentClass.getAnnotation(Cacheable.class);
        return cacheable.value();
    }

}
