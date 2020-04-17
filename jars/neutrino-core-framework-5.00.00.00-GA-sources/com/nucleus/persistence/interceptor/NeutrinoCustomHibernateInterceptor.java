package com.nucleus.persistence.interceptor;

import java.io.Serializable;

import org.apache.commons.lang.BooleanUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import com.nucleus.entity.BaseEntity;

public class NeutrinoCustomHibernateInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 1L;

	public NeutrinoCustomHibernateInterceptor() {
		super();
	}

	/*
	 * Method overridden to handle the PreUpdate of setLastUpdatedTimeStamp
	 * field with DynamicUpdate annotation on the Entity. Called from
	 * DefaultFlushEntityEventListener inside invokeInterceptor method.
	 */

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) {
		BaseEntity baseEntity = cast(entity);
		if (baseEntity != null && baseEntity.getEntityLifeCycleData() != null
				&& BooleanUtils.isTrue(baseEntity.getEntityLifeCycleData().getDirtyFlag())) {
			return true;
		}
		return false;
	}

	private BaseEntity cast(Object entity) {
		if (!(entity instanceof BaseEntity)) {
			return null;
		}
		return BaseEntity.class.cast(entity);
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		boolean flag=false;
		for (int i = 0; i < state.length; i++) {
			if (("").equals(state[i])) {
				state[i] = null;
				flag = true;
			}
		}
		return flag;
	}

}
