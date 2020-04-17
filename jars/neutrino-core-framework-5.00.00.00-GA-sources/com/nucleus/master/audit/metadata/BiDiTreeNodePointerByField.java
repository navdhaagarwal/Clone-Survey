package com.nucleus.master.audit.metadata;

public class BiDiTreeNodePointerByField {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
		BiDiTreeNodePointerByField other = (BiDiTreeNodePointerByField) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.getSimpleName().equals(other.className.getSimpleName()))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		return true;
	}

	private Class className;
	
	private String fieldName;

	public BiDiTreeNodePointerByField(Class className, String fieldName) {
		super();
		this.className = className;
		this.fieldName = fieldName;
	}

	public Class getClassName() {
		return className;
	}

	public String getFieldName() {
		return fieldName;
	}
	
	
}
