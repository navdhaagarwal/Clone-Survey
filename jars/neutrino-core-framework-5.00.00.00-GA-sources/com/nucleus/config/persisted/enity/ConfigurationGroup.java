package com.nucleus.config.persisted.enity;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="ALL")
public class ConfigurationGroup extends BaseEntity {

    private static final long   serialVersionUID = 6775730006750256901L;

    /** The entity uri. */
    @Column(unique = true)
    private String              associatedEntityUri;

    /** The parent configuration group. */
    @ManyToOne
    private ConfigurationGroup  parentConfigurationGroup;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_group_fk")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<Configuration> configuration;

    public List<Configuration> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(List<Configuration> configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets the entity uri.
     *
     * @return the entity uri
     */
    public EntityId getAssociatedEntityId() {
        return EntityId.fromUri(associatedEntityUri);
    }

    /**
     * Sets the entity uri.
     *
     * @param entityUri the new entity uri
     */
    public void setAssociatedEntityId(EntityId entityId) {
        this.associatedEntityUri = entityId.getUri();
    }

    /**
     * Gets the parent configuration group.
     *
     * @return the parent configuration group
     */
    public ConfigurationGroup getParentConfigurationGroup() {
        return parentConfigurationGroup;
    }

    /**
     * Sets the parent configuration group.
     *
     * @param parentConfigurationGroup the parent configuration group
     */

    public void setParentConfigurationGroup(ConfigurationGroup parentConfigurationGroup) {
        this.parentConfigurationGroup = parentConfigurationGroup;
    }

    
 // this is to allow non entities to have their configuration
    public String getAssociatedEntityUri() {
        return associatedEntityUri;
    }

    public void setAssociatedEntityUri(String associatedEntityUri) {
        this.associatedEntityUri = associatedEntityUri;
    }
}
