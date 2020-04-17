package com.nucleus.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.search.annotations.DocumentId;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.formsConfiguration.PersistentFormData;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.finnone.pro.base.utility.CoreUtility;

import io.swagger.annotations.ApiModelProperty;

/**
 * Base class for all entity classes.
 */
@MappedSuperclass
public abstract class BaseEntity extends AbstractBaseEntity {

    private static final long   serialVersionUID = -5364714940059919768L;
    
    private static final String UUID_SEPARATOR ="-";

    @Id
    @GenericGenerator(name = "sequencePerEntityGenerator", strategy = "com.nucleus.core.generator.NeutrinoSequenceGenerator", parameters = {
            @Parameter(name = "prefer_sequence_per_entity", value = "true"),
            @Parameter(name = "sequence_per_entity_suffix", value = "_seq"),
            @Parameter(name = "initial_value", value = "5000000")})
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequencePerEntityGenerator")
    @DocumentId
    private Long                id;


    @Embedded
    private EntityLifeCycleData entityLifeCycleData;

	@ApiModelProperty(hidden=true)
    @Transient
    private final String        uuid;

	@ApiModelProperty(hidden=true)
    @Transient
    private boolean	isIdNull;

	@ApiModelProperty(hidden=true)
    @Transient
    private HashMap<String, Object> viewProperties;
    
	@ApiModelProperty(hidden=true)
	@Transient
    private HashMap<String, Object> transientMaskingMap;
    
    public BaseEntity() {
        //this.uuid = UUID.randomUUID().toString();
        this.uuid =  String.valueOf(System.nanoTime()).concat(UUID_SEPARATOR).concat(String.valueOf(Math.random()));
    }

    private Date                    makeBusinessDate;

    private Date                    authorizationBusinessDate;
    
	@ApiModelProperty(hidden=true)
	@Transient
    private String uploadOperationType;
    
	public String getUploadOperationType() {
		return uploadOperationType;
	}

	public void setUploadOperationType(String uploadOperationType) {
		this.uploadOperationType = uploadOperationType;
	}

	public void addProperty(String key, Object value) {
        if (viewProperties == null) {
            this.viewProperties = new LinkedHashMap<String, Object>();
        }
        this.viewProperties.put(key, value);
    }

	public void addMaskingProperty(String key, Object value) {
        if (transientMaskingMap == null) {
            this.transientMaskingMap = new LinkedHashMap<String, Object>();
        }
        this.transientMaskingMap.put(key, value);
    }
	
    public BaseEntity(Long id) {
        this();
        this.id = id;
    }

    // ~ Methods
    // ====================================================================================

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Serializable id) {
        if ((id != null) && (((Long) id).longValue() != 0)) {
            this.id = (Long) id;
        }else{
        	setIdNull(true);
        }
    }

    public void clearId() {
        this.id = null;
    }

    public void markActive() {
        getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.ACTIVE);
    }

    public void markInActive() {
        getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.INACTIVE);
    }

    public void markDeleted() {
        getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.DELETED);
    }

    public void markTemp() {
        getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.TEMP);
    }

    public void markDraft() {
        getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.DRAFT);
    }

    public void markAppSaveAsDraft() {
        getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.APP_SAD);
    }
    
    public void markEmptyParent() {
    	getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.EMPTY_PARENT);
    	
    }

    /**
     * @see PersistenceStatus
     * @param status
     */
    public void setPersistenceStatus(int status) {
        getEntityLifeCycleData().setPersistenceStatus(status);
    }

    public Integer getPersistenceStatus() {
        return getEntityLifeCycleData().getPersistenceStatus();
    }

    @Override
    public final BaseEntity cloneYourself(CloneOptions cloneOptions) {
        BaseEntity baseEntity = createEmptyClone();
        populate(baseEntity, cloneOptions);
        return baseEntity;
    }

    private BaseEntity createEmptyClone() {
        try {
            return (BaseEntity) Hibernate.getClass(this).newInstance();
        } catch (Exception e) {
            throw new SystemException("Exception occured in clone for snapshot operation", e);
        }
    }

    protected void populate(BaseEntity clonedEntity, CloneOptions cloneOptions) {
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.COPY_ID_KEY)) {
            clonedEntity.id = this.id;
        }
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.SNAPSHOT_RECORD_KEY)) {
            clonedEntity.setEntityLifeCycleData(new EntityLifeCycleData());
            EntityLifeCycleData entityLifeCycleData = new EntityLifeCycleDataBuilder().setSnapshotRecord(
                    cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.SNAPSHOT_RECORD_KEY)).getEntityLifeCycleData();
            clonedEntity.setEntityLifeCycleData(entityLifeCycleData);
        }
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.COPY_UUID_KEY)) {
            if (this.getEntityLifeCycleData() != null) {
                clonedEntity.getEntityLifeCycleData().setUuid(getEntityLifeCycleData().getUuid());
            }
        }
    }

    /**
     * Copy Method
     * 
     * @param entity
     * @return
     */
    public void copyFrom(BaseEntity sourceEntity, CloneOptions cloneOptions) {
        populateFrom(sourceEntity, cloneOptions);
    }

    protected void populateFrom(BaseEntity copyEntity, CloneOptions cloneOptions) {
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.COPY_UUID_KEY)) {
            if (copyEntity.getEntityLifeCycleData() != null) {
                this.getEntityLifeCycleData().setUuid(copyEntity.getEntityLifeCycleData().getUuid());
            }
        }
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.COPY_ID_KEY)) {
            this.id = copyEntity.id;
        }
        this.getEntityLifeCycleData().setPersistenceStatus(copyEntity.getEntityLifeCycleData().getPersistenceStatus());
    }

    /**
     * @return the viewProperties
     */
    public HashMap<String, Object> getViewProperties() {
        if (viewProperties == null) {
            viewProperties = new LinkedHashMap<String, Object>();
        }
        return viewProperties;
    }

    /**
     * @param viewProperties
     *            the viewProperties to set
     */
    public void setViewProperties(HashMap<String, Object> viewProperties) {
        this.viewProperties = viewProperties;
    }
    
    

    public HashMap<String, Object> getTransientMaskingMap() {
        if (transientMaskingMap == null) {
            transientMaskingMap = new LinkedHashMap<String, Object>();
        }
        return transientMaskingMap;
    }

    public void setTransientMaskingMap(HashMap<String, Object> transientMaskingMap) {
        this.transientMaskingMap = transientMaskingMap;
    }

    /**
     * @return the entityLifeCycleData
     */
    public EntityLifeCycleData getEntityLifeCycleData() {
        if (entityLifeCycleData == null) {
            entityLifeCycleData = new EntityLifeCycleData();
        }
        return entityLifeCycleData;
    }

    /**
     * @param entityLifeCycleData
     *            the entityLifeCycleData to set
     */
    public void setEntityLifeCycleData(EntityLifeCycleData entityLifeCycleData) {
        this.entityLifeCycleData = entityLifeCycleData;
    }

    @PrePersist
    protected void setupLifecycleData() {
        if (getEntityLifeCycleData().getPersistenceStatus() == null) {
            getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.ACTIVE);
        }
        getEntityLifeCycleData().setCreationTimeStamp(DateUtils.getCurrentUTCTime());
        if (this.getMakeBusinessDate() == null) {
            this.setMakeBusinessDate(CoreUtility.getBusinessDate());
        }

    }

	/*
	 * Fields updated in @PreUpdate are not included in update query
	 * when @DynamicUpdate is used. There is an open hibernate bug for the same:
	 * https://hibernate.atlassian.net/browse/HHH-9754
	 */
    
    @PreUpdate
	protected void updateLifecycleData() {
    	getEntityLifeCycleData().setDirtyFlag(true);
        getEntityLifeCycleData().setLastUpdatedTimeStamp(DateUtils.getCurrentUTCTime());
        if (this.getMakeBusinessDate() == null) {
            this.setMakeBusinessDate(CoreUtility.getBusinessDate());
        }
    }

    public String getUuid() {
        return uuid;
    }

    public Date getMakeBusinessDate() {
        return makeBusinessDate;
    }

    public void setMakeBusinessDate(Date makeBusinessDate) {
        this.makeBusinessDate = makeBusinessDate;
    }

    public Date getAuthorizationBusinessDate() {
        return authorizationBusinessDate;
    }

    public void setAuthorizationBusinessDate(Date authorizationBusinessDate) {
        this.authorizationBusinessDate = authorizationBusinessDate;
    }

    public boolean isIdNull() {
		return isIdNull;
	}

	public void setIdNull(boolean isIdNull) {
		this.isIdNull = isIdNull;
	}
}