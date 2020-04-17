package com.nucleus.master.audit.metadata;

import java.lang.reflect.Method;

public class BiDiTreeNodePointerByGetter {

	private Class className;
	
	private Method getter;

	public BiDiTreeNodePointerByGetter(Class className, Method getter) {
		super();
		this.className = className;
		this.getter = getter;
	}

	public Class getClassName() {
		return className;
	}

	public void setClassName(Class className) {
		this.className = className;
	}

	public Method getGetter() {
		return getter;
	}

	public void setGetter(Method getter) {
		this.getter = getter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((getter == null) ? 0 : getter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BiDiTreeNodePointerByGetter other = (BiDiTreeNodePointerByGetter) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.getSimpleName().equals(other.className.getSimpleName()))
			return false;
		if (getter == null) {
			if (other.getter != null)
				return false;
		} else if (!getter.equals(other.getter))
			return false;
		return true;
	}
	
	
}
