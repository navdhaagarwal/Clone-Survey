package com.nucleus.entity;

import java.io.Serializable;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.Hibernate;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import com.nucleus.core.exceptions.SystemException;

@MappedSuperclass
public abstract class AssignIdBaseEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 189001946000031024L;

    @Id       
    private Long                id;

    public AssignIdBaseEntity() {}
    
    public AssignIdBaseEntity(Long id) {
        this.id = id;
    }

	@Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Serializable id) {
        if ((id != null) && (((Long) id).longValue() != 0)) {
            this.id = (Long) id;
        }
    }

    public void clearId() {
        this.id = null;
    }

    @Override
    public final AssignIdBaseEntity cloneYourself(CloneOptions cloneOptions) {
    	AssignIdBaseEntity nonGeneratedIdentifierBaseEntity = createEmptyClone();
        populate(nonGeneratedIdentifierBaseEntity, cloneOptions);
        return nonGeneratedIdentifierBaseEntity;
    }

    private AssignIdBaseEntity createEmptyClone() {
        try {
            return (AssignIdBaseEntity) Hibernate.getClass(this).newInstance();
        } catch (Exception e) {
            throw new SystemException("Exception occured in clone for snapshot operation", e);
        }
    }

    protected void populate(AssignIdBaseEntity clonedEntity, CloneOptions cloneOptions) {
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.COPY_ID_KEY)) {
            clonedEntity.id = this.id;
        }        
    }

    /**
     * Copy Method
     *
     * @param entity
     * @return
     */
    public void copyFrom(AssignIdBaseEntity sourceEntity, CloneOptions cloneOptions) {
        populateFrom(sourceEntity, cloneOptions);
    }

    protected void populateFrom(AssignIdBaseEntity copyEntity, CloneOptions cloneOptions) {       
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.COPY_ID_KEY)) {
            this.id = copyEntity.id;
        }
    }

}
