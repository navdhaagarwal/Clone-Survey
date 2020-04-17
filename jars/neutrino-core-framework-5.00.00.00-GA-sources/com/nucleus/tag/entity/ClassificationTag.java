package com.nucleus.tag.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Table;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(indexes={@Index(name="tagName_index",columnList="tagName")})
public class ClassificationTag extends BaseEntity {

    private static final long serialVersionUID = -8252029064834196574L;
    private String            tagName;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String>       entityUris;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Set<String> getEntityUris() {
        return entityUris;
    }

    public void addEntityUri(String entityUri) {
        if (CollectionUtils.isEmpty(entityUris)) {
            entityUris = new HashSet<String>();
        }
        entityUris.add(entityUri);
    }

    public void removeEntityUri(String entityUri) {
        if (CollectionUtils.isNotEmpty(entityUris)) {
            entityUris.remove(entityUri);
        }
    }

    public void setEntityUris(Set<String> entityUris) {
        this.entityUris = entityUris;
    }

}
