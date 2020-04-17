package com.nucleus.bookmark;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class Bookmark extends BaseEntity {

	private static final long serialVersionUID = 1234L;
	private com.nucleus.entity.BaseEntity fromId;
	private BaseEntity toId;
	private Boolean IsDeleted;
	private String description;

	public BaseEntity getFromId() {
		return fromId;
	}

	public void setFromId(BaseEntity fromId) {
		this.fromId = fromId;
	}

	public BaseEntity getToId() {
		return toId;
	}

	public void setToId(BaseEntity toId) {
		this.toId = toId;
	}

	public Boolean getIsDeleted() {
		return IsDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		IsDeleted = isDeleted;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
