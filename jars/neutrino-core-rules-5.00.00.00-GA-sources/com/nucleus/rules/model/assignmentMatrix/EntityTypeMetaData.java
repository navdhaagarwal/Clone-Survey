package com.nucleus.rules.model.assignmentMatrix;

import javax.persistence.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class EntityTypeMetaData.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public class EntityTypeMetaData extends BaseEntity {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The display name. */
    @EmbedInAuditAsValue
    private String            displayName;

    /** The fully qualified name. */
    @EmbedInAuditAsValue
    private String            fullyQualifiedName;

    @Transient
    private Map teamOrUserMap;

    @Transient
    private Map reverseTeamOrUserMap;

    /* (non-Javadoc) @see com.nucleus.entity.BaseEntity#getDisplayName() */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name.
     *
     * @param displayName the new display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the fully qualified name.
     *
     * @return the fully qualified name
     */
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    /**
     * Sets the fully qualified name.
     *
     * @param fullyQualifiedName the new fully qualified name
     */
    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        EntityTypeMetaData entityTypeMetaData = (EntityTypeMetaData) baseEntity;
        super.populate(entityTypeMetaData, cloneOptions);

        entityTypeMetaData.setDisplayName(displayName);
        entityTypeMetaData.setFullyQualifiedName(fullyQualifiedName);

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        EntityTypeMetaData entityTypeMetaData = (EntityTypeMetaData) baseEntity;
        super.populateFrom(entityTypeMetaData, cloneOptions);
        this.setDisplayName(entityTypeMetaData.getDisplayName());
        this.setFullyQualifiedName(entityTypeMetaData.getFullyQualifiedName());

    }

    public Map getTeamOrUserMap() {
        return teamOrUserMap;
    }

    public void setTeamOrUserMap(Map teamOrUserMap) {
        this.teamOrUserMap = teamOrUserMap;
    }

    public Map getReverseTeamOrUserMap() {
        return reverseTeamOrUserMap;
    }

    public void setReverseTeamOrUserMap(Map reverseTeamOrUserMap) {
        this.reverseTeamOrUserMap = reverseTeamOrUserMap;
    }
}
