package com.nucleus.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.Hibernate;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

@DiscriminatorOptions(force = true)
@MappedSuperclass
public abstract class AbstractBaseEntity implements Entity{

	private static final long serialVersionUID = -378170340833783226L;
   
	@Column(name = "TENANT_ID")
    private Long                tenantId;
  
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
        
    /*
     * Added for Rule Engine to get the entity id used for Ognl with reference
     * data type used while doing rule auditing
     */

    public final String toEntityId() {
        /* This is implemented for mainly Rule Engine */
        return getClass().getName() + ":" + getId();
    }
    
    public static boolean identityEquals(AbstractBaseEntity entity, AbstractBaseEntity other) {
        boolean identityEquals = false;
        if (entity == other) {
            identityEquals = true;
        } else if (entity != null && entity.equals(other)) {
            identityEquals = true;
        }
        return identityEquals;
    }

    /**
     * Whether the current entity has the same type and id as the other one.
     * Both side must have id. Null ids are considered not equal.
     * 
     * @param other
     *            The other entity to compare to.
     * @return True if this entity and the other entity has the same type and
     *         id.
     */
    public boolean identityEquals(AbstractBaseEntity other) {
        boolean identityEquals = false;
        // class check doesn't work when Hibernate proxies are used
        if (other != null /* && this.getClass().equals(other.getClass()) */) {
            if (this.getId() != null && this.getId().equals(other.getId())) {
                identityEquals = true;
            }
        }
        return identityEquals;
    }
    

    @SuppressWarnings("unchecked")
    @Override
    public EntityId getEntityId() {
        return new EntityId(Hibernate.getClass(this), getEntityId(this));
    }

    @Override
    public String getUri() {
        return getEntityId().getUri();
    }

    /**
     * Returns the set of Entity objects that is in thisSet but not thatSet.
     * 
     * @param thisSet
     *            a set
     * @param thatSet
     *            a set
     * @return a set
     */
    public static <T extends Entity> Set<T> getEntitiesInThisButNotInThat(Set<T> thisSet, Set<T> thatSet) {
        Set<T> addedLines = null;
        if (thisSet != null && thatSet != null) {
            addedLines = new LinkedHashSet<T>();
            for (T i : thisSet) {
                Object iId = i.getId();
                if (iId != null) {
                    boolean found = false;
                    for (T j : thatSet) {
                        Object jId = j.getId();
                        if (iId.equals(jId)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        addedLines.add(i);
                }
            }
        }
        return addedLines;
    }

    /**
     * Utility method that can retrieve lazy-loaded entity's id without loading
     * the full entity.
     * 
     * @param entity
     * @return
     */
    public static Long getEntityId(Entity entity) {
        Long id = null;
        if (entity instanceof HibernateProxy) {
            LazyInitializer lazyInitializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
            id = (Long) lazyInitializer.getIdentifier();
        } else if (entity != null) {
            id = (Long) entity.getId();
        }
        return id;
    }
    
    /**
     * Subclasses should override this method to allow faster load of lazy-fetch
     * fields.
     */
    @Override
    public void loadLazyFields() {    	
    }
  
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj != null) && (obj instanceof AbstractBaseEntity) && (this.getId() != null)
                && (this.getId().equals(((AbstractBaseEntity) obj).getId()))) {
            Class<?> thisClass = this.getClass();
            if (this instanceof HibernateProxy) {
                thisClass = ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass();
            }

            Class<?> objClass = obj.getClass();
            if (obj instanceof HibernateProxy) {
                objClass = ((HibernateProxy) obj).getHibernateLazyInitializer().getPersistentClass();
            }

            if (thisClass.equals(objClass)) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        /*
         * Do not implement reflection based to string as it will start
         * initializing proxy (lazy) objects and result into huge number of
         * select statements
         */
        return getClass().getName() + ":" + getId();
    }
    
    @Override
    public int hashCode() {
        return this.getId() != null ? this.getId().hashCode() : super.hashCode();
    }
    
    public String getDisplayName() {
        return getEntityId().getUri();
    }
    
    public String getEntityDisplayName() {
        return this.getClass().getSimpleName();
    }
 
}
