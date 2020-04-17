package com.nucleus.core.common;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentCollectionConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedSetConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class EntityUtil {

    private final static XStream xstreamWithIdReset    = new XStream() {
                                                           @Override
                                                           protected MapperWrapper wrapMapper(final MapperWrapper next) {
                                                               return new HibernateMapper(next);
                                                           }
                                                       };

    private final static XStream xstreamWithoutIdReset = new XStream() {
                                                           @Override
                                                           protected MapperWrapper wrapMapper(final MapperWrapper next) {
                                                               return new HibernateMapper(next);
                                                           }
                                                       };
    static {
        xstreamWithIdReset.registerConverter(new HibernateProxyConverter());
        xstreamWithIdReset.registerConverter(new HibernatePersistentCollectionConverter(xstreamWithIdReset.getMapper()));
        xstreamWithIdReset.registerConverter(new HibernatePersistentMapConverter(xstreamWithIdReset.getMapper()));
        xstreamWithIdReset.registerConverter(new HibernatePersistentSortedMapConverter(xstreamWithIdReset.getMapper()));
        xstreamWithIdReset.registerConverter(new HibernatePersistentSortedSetConverter(xstreamWithIdReset.getMapper()));

        // it will make ids null  for entities (except Master and Generic Parameter)
        xstreamWithIdReset.registerConverter(new OmittingReflectionProvider(xstreamWithIdReset.getMapper(),
                xstreamWithIdReset.getReflectionProvider()));

        xstreamWithoutIdReset.registerConverter(new HibernateProxyConverter());
        xstreamWithoutIdReset
                .registerConverter(new HibernatePersistentCollectionConverter(xstreamWithoutIdReset.getMapper()));
        xstreamWithoutIdReset.registerConverter(new HibernatePersistentMapConverter(xstreamWithoutIdReset.getMapper()));
        xstreamWithoutIdReset
                .registerConverter(new HibernatePersistentSortedMapConverter(xstreamWithoutIdReset.getMapper()));
        xstreamWithoutIdReset
                .registerConverter(new HibernatePersistentSortedSetConverter(xstreamWithoutIdReset.getMapper()));

    }

    /**
     * This method could not be moved to core-utils project, since the core-utils does not have dependency of core-framework which is
     *  why Sortable and Entity were not available there. 
     *  
     *  
     * @param entityClassName
     *            the class whose elements needs to be sorted
     *          
     * @return String 
     *           a sortableField a field on which sorting needs to be done which is then passed to query
     *    
     * This method will be modified further when index attribute comes in use .
     */

    public static <T extends Entity> String getSortableField(Class<T> entityClassName) {
        String sortableField = "";
        if (entityClassName != null) {
            Field[] fields = getAllDeclaredFields(entityClassName, true, 0);
            if (fields != null) {
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Sortable.class)) {
                        field.setAccessible(true);
                        sortableField = field.getName();
                    }
                }
            }
        }

        return sortableField;

    }

    /**
     * Retrieving fields list of specified class
     * If recursively is true, retrieving fields from all class hierarchy
     *
     * @param clazz where fields are searching
     * @param recursively param
     * @param count - to break this recursive loop when count >2 as we are assuming that the annotation will be
     * found till 0 and 1  level - no need for traversing till baseEntity, Entity, Object..... 
     * @return list of fields
     */
    @SuppressWarnings("rawtypes")
    public static Field[] getAllDeclaredFields(Class clazz, boolean recursively, int recursiveLoopCount) {
        List<Field> fields = new LinkedList<Field>();
        Field[] declaredFields = clazz.getDeclaredFields();
        Collections.addAll(fields, declaredFields);
        Class superClass = clazz.getSuperclass();
        recursiveLoopCount += 1;
        if (superClass != null && recursively && recursiveLoopCount < 2) {
            Field[] declaredFieldsOfSuper = getAllDeclaredFields(superClass, recursively, recursiveLoopCount);
            if (declaredFieldsOfSuper.length > 0)
                Collections.addAll(fields, declaredFieldsOfSuper);
        }

        return fields.toArray(new Field[fields.size()]);
    }

    /**
     * Retrieving fields list of specified class and which
     * are annotated by incoming annotation class
     * If recursively is true, retrieving fields from all class hierarchy
     * This will be used when the concept of indexes will be  used in 
     * Sortable annotation class
     *
     * @param clazz - where fields are searching
     * @param annotationClass - specified annotation class
     * @param recursively param
     * @return list of annotated fields
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Field[] getAllAnnotatedDeclaredFields(Class clazz, Class annotationClass, boolean recursively) {
        Field[] allFields = getAllDeclaredFields(clazz, recursively, 0);
        List<Field> annotatedFields = new LinkedList<Field>();

        for (Field field : allFields) {
            if (field.isAnnotationPresent(annotationClass))
                annotatedFields.add(field);
        }

        return annotatedFields.toArray(new Field[annotatedFields.size()]);
    }

    /**
     * Initialize and Deep copy entity graph with id reset to null(except for Master and Generic Parameter)
     *
     * @param <T> the generic type
     * @param entityToDeepCopy the entity to deep copy
     * @return the fully initialized copy of entity with ids set to null for non masters and non generic parameters.
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseEntity> T deepCopyEntityGraphWithIdResetForNonMasters(T entityToDeepCopy) {
        if (entityToDeepCopy == null) {
            return null;
        }
        return (T) (xstreamWithIdReset.fromXML(xstreamWithIdReset.toXML(entityToDeepCopy)));
    }

    /**
     * Initialize and detach entity graph.
     *
     * @param <T> the generic type
     * @param entityToInitialize the entity to initialize
     * @return the t
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseEntity> T initializeAndDetachEntityGraph(T entityToInitialize) {
        if (entityToInitialize == null) {
            return null;
        }
        return (T) (xstreamWithoutIdReset.fromXML(xstreamWithoutIdReset.toXML(entityToInitialize)));
    }

}
