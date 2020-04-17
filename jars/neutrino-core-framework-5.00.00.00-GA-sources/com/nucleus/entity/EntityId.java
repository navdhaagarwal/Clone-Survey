package com.nucleus.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;

public class EntityId implements IEntityId {

    // ~ Static variables/initializers ==============================================================

    private static final long       serialVersionUID   = 4393545204232753829L;

    public static final String      URI_PART_SEPARATOR = ":";

    // ~ Member variables ===========================================================================

    private Class<? extends Entity> entityClass;
    private Long                    localId;
    private String                  uri;

    // ~ Constructors ===============================================================================

    public EntityId(Class<? extends Entity> entityClass, Long localId) {
        super();
        if (entityClass == null || localId == null || localId <= 0) {
            throw new InvalidDataException("Entity class and id should not be null. Also the id should be > 0");
        }
        if (entityClass == SystemEntity.class && localId != 1L) {
            throw new InvalidDataException(
                    "Couldn't initialize EntityId for SystemEntity class with id other than 1L. Use SystemEntity.getSystemEntityId() instead");
        }
        this.entityClass = entityClass;
        this.localId = localId;
        this.uri = entityClass.getName() + URI_PART_SEPARATOR + getLocalId();
    }

    public EntityId() {
    }

    // ~ Methods ====================================================================================

    @Override
    public Long getLocalId() {
        return localId;
    }

    @Override
    public Class<? extends Entity> getEntityClass() {
        return entityClass;
    }

    @Override
    public String getUri() {
        return uri;
    }

    /**
     * Get an instance of EntityId from a URI string.
     *
     * @param uris     String List of uris
     * @return List    of entity ids
     */
    public static List<EntityId> fromUris(List<String> uris) {
        List<EntityId> entityIds = new ArrayList<EntityId>(uris.size());
        for (String uri : uris) {
            EntityId entityId = fromUri(uri);
            entityIds.add(entityId);
        }
        return entityIds;
    }

    /**
     * Get an instance of EntityId from a URI string.
     *
     * @param uri      String uri
     * @return         the entity id
     */
    @SuppressWarnings("unchecked")
    public static EntityId fromUri(String uri) {
        if (StringUtils.isBlank(uri)) {
            return null;
        }
        try {
            String[] parts = StringUtils.split(uri, URI_PART_SEPARATOR);
            if (parts.length != 2) {
                throw new InvalidDataException(String.format(
                        "Invalid uri passed to parse. Expecting following format: %s but got this: %s", "class"
                                + URI_PART_SEPARATOR + "id", uri));
            }
            String typePart = parts[0];
            Class<?> clazz = Class.forName(typePart.trim());
            if (!Entity.class.isAssignableFrom(clazz)) {
                throw new InvalidDataException("Invalid uri passed to parse. The parsed class is not assignable from Entity");
            }
            Class<Entity> entityClass = (Class<Entity>) clazz;
            String localIdPart = parts[1];
            Long localId = Long.valueOf(localIdPart.trim());
            return new EntityId(entityClass, localId);
        } catch (InvalidDataException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Exception occured while creating EntityId from uri: " + uri);
        }
    }

    public String toUri() {
        return this.entityClass.getName() + URI_PART_SEPARATOR + this.getLocalId();
    }

    /**
     * Check whether the given uri is for the given class.
     *
     * @param uri      uri for entity
     * @param clazz    expected class
     * @return boolean is the entity for the given class
     */
    public static boolean isUriOfClass(String uri, Class<?> clazz) {
        boolean isUriOfClass = false;
        try {
            String[] parts = StringUtils.split(uri, URI_PART_SEPARATOR);
            if(parts.length==2) {
            	String typePart = parts[0];
                Class<?> entityClass = Class.forName(typePart);
            if (entityClass == clazz)
    				Long.parseLong(parts[1]);
                    isUriOfClass = true;
                }
            }
        catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception occured while checking class " + clazz.getSimpleName()
                    + " for a given URI " + uri + " : " + e.getMessage());
        }
        return isUriOfClass;
    }

    /**
     * Instantiates a new instance from EntityId information of contained class and id. 
     * @return The instantiated object with id.
     * @throws SystemException if instantiation fails.
     */
    public <T extends Entity> T toInstance(Class<T> expectedClass) {
        try {
            if (!expectedClass.isAssignableFrom(entityClass)) {
                throw new InvalidDataException("The passed entity class in argument is not subclass of Entity");
            }
            T baseEntity = (T) entityClass.newInstance();
            baseEntity.setId(getLocalId());
            return baseEntity;
        } catch (InvalidDataException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemException("Exception occured while instantiating EntityId. Expected class: "
                    + expectedClass.getName() + " Actual Class: " + getEntityClass().getName(), e);
        }

    }
    
    /**
     * Instantiates a new instance from uri information of contained class.
     * @return The instantiated object.
     * @throws SystemException if instantiation fails.
     */
    @SuppressWarnings("unchecked")
	public static <T extends Entity> T getNewInstance(String entityName) {
    	if (StringUtils.isBlank(entityName)) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName(entityName.trim());
            if (!Entity.class.isAssignableFrom(clazz)) {
                throw new InvalidDataException("The passed entity class in argument is not subclass of Entity");
            }
           return ((T) clazz.newInstance());
        } catch (InvalidDataException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidDataException("Exception occured while creating Entity Instance from entityName: " + entityName);
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
        EntityId other = (EntityId) obj;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        return true;
    }

    /* (non-Javadoc) @see java.lang.Object#toString() */
    @Override
    public String toString() {
        return "EntityId - " + getUri();
    }

    /*
     * Utility method to create a predicate on entity. This method can( should) be used to find an entity in given collection as follows 
     *  CollectionUtils.find(entityList, new EntityId(User.class, 101l).createPredicate());
     */
    public Predicate createPredicate() {
        return new EntityIdPredicate(this);
    }
    
    public static String getUri(Long entityId, Class<? extends BaseEntity> entityClass){
    	return entityClass.getName() + URI_PART_SEPARATOR + entityId;
    }

}