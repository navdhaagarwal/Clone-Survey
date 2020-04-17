package com.nucleus.persistence;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import com.nucleus.core.misc.util.ExceptionUtility;

public class HibernateUtils {

    @SuppressWarnings("unchecked")
    public static <T> T initializeAndUnproxy(T entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof HibernateProxy) {
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }

        return entity;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getImplementationWithoutInitializingProxy(T entity) {
        if (entity == null) {
            return null;
        }

        Serializable id = null;
        if (entity instanceof HibernateProxy) {
            id = (((HibernateProxy) entity).getHibernateLazyInitializer().getIdentifier());
            // will not initialize the proxy
            Class<T> clazz = ((HibernateProxy) entity).getHibernateLazyInitializer().getPersistentClass();
            entity = BeanUtils.instantiate(clazz);
            Field f = ReflectionUtils.findField(clazz, "id");
            ReflectionUtils.makeAccessible(f);
            ReflectionUtils.setField(f, entity, id);
        }

        return entity;
    }

    public static <T> Serializable getIdWithoutInitializingProxy(T entity) {
        if (entity == null) {
            return null;
        }
        Serializable id = null;
        if (entity instanceof HibernateProxy) {
            id = (((HibernateProxy) entity).getHibernateLazyInitializer().getIdentifier());
        } else {
            Field f = ReflectionUtils.findField(entity.getClass(), "id");
            ReflectionUtils.makeAccessible(f);
            try {
                id = (Serializable) f.get(entity);
            } catch (Exception e) {
                ExceptionUtility.rethrowSystemException(e);
            }
        }
        return id;
    }

    public static <T> Serializable getClassWithoutInitializingProxy(T entity) {

        if (entity == null) {
            return null;
        }

        if (entity instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) entity;
            LazyInitializer li = proxy.getHibernateLazyInitializer();
            return li.getPersistentClass();
        } else {
            return entity.getClass();
        }
    }

    public static <T> String getSimpleNameWithoutInitializingProxy(T entity) {

        if (entity == null) {
            return null;
        }

        if (entity instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) entity;
            LazyInitializer li = proxy.getHibernateLazyInitializer();
            return li.getPersistentClass().getSimpleName();
        } else {
            return entity.getClass().getSimpleName();
        }
    }

}
