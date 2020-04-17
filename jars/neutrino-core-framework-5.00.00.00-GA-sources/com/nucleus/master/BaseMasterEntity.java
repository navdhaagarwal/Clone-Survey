package com.nucleus.master;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import com.nucleus.core.annotations.ComparableField;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.formsConfiguration.DynamicForm;
import com.nucleus.core.formsConfiguration.MasterDynamicForm;
import com.nucleus.core.formsConfiguration.UIMetaDataVo;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.entity.CloneOptions;
import com.nucleus.entity.MasterLifeCycleData;
import com.nucleus.makerchecker.EntityChange;
import com.nucleus.makerchecker.FieldChange;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;

import io.swagger.annotations.ApiModelProperty;

/**
 * Base implementation of MasterEntity.
 */
@MappedSuperclass
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseMasterEntity extends BaseEntity implements MasterDynamicForm {

	private static final long serialVersionUID = -1264757476503380126L;

	@Embedded
	private MasterLifeCycleData masterLifeCycleData;

	@EmbedInAuditAsValue
	private boolean activeFlag = true;
	
	private Boolean offlineFlag = Boolean.FALSE;
	
	
	@ApiModelProperty(hidden=true)
	@Transient
    private String operationType;
	
	@Embedded
	@ApiModelProperty(hidden=true)
	@AttributeOverrides({
			@AttributeOverride(name = "dataJsonString", column = @Column(name = "DATA_JSON_STRING_MST", length = 4000)),
			@AttributeOverride(name = "placeholderId", column = @Column(name = "PLACEHOLDER_ID_MST")),
			@AttributeOverride(name = "modelMetaDataId", column = @Column(name = "MODEL_META_DATA_ID_MST")),
			@AttributeOverride(name = "uiMetaDataId", column = @Column(name = "UI_META_DATA_ID_MST")) })
	private DynamicForm dynamicForm;
	
	@ApiModelProperty(hidden=true)
	@Transient
	@XmlTransient
	private UIMetaDataVo uiMetaDataVo;
	 
	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public Boolean getOfflineFlag() {
	        return notNull(offlineFlag)? offlineFlag:Boolean.FALSE;
	}
	
	 public void setOfflineFlag(Boolean offlineFlag) {
	      this.offlineFlag=notNull(offlineFlag)?offlineFlag:Boolean.FALSE;
	}

	public final static EntityChange beanCompare(
			BaseMasterEntity baseMasterEntity1,
			BaseMasterEntity toBecomparedWith) throws InvalidDataException,
			IllegalAccessException {
		EntityChange entityChange = new EntityChange();
		Field[] fields = baseMasterEntity1.getClass().getDeclaredFields();
		if (fields != null) {
			for (Field field : fields) {
				if (field.isAnnotationPresent(ComparableField.class)) {
					field.setAccessible(true);
					if (Collection.class.isAssignableFrom(field.getType())) {
						if (null != (field.get(baseMasterEntity1))
								&& null != (field.get(toBecomparedWith))) {
							@SuppressWarnings("unchecked")
							Iterator<BaseMasterEntity> itr1 = ((Collection<BaseMasterEntity>) (field
									.get(baseMasterEntity1))).iterator();
							@SuppressWarnings("unchecked")
							Iterator<BaseMasterEntity> itr2 = ((Collection<BaseMasterEntity>) (field
									.get(toBecomparedWith))).iterator();
							while (itr1.hasNext() && itr2.hasNext()) {
								EntityChange ec = beanCompare(
										(BaseMasterEntity) itr1.next(),
										(BaseMasterEntity) itr2.next());
								entityChange.addChildEntityChange(ec);
							}
						}
					} else if (BaseMasterEntity.class.isAssignableFrom(field
							.getType())) {
						if (null != (field.get(baseMasterEntity1))
								&& null != (field.get(toBecomparedWith))) {
							EntityChange ec = beanCompare(
									(BaseMasterEntity) field
											.get(baseMasterEntity1),
									(BaseMasterEntity) field
											.get(toBecomparedWith));
							entityChange.addChildEntityChange(ec);
						}
					} else if ((null != field.get(baseMasterEntity1))
							&& (null != field.get(toBecomparedWith))) {
						if (!(field.get(baseMasterEntity1)).equals(field
								.get(toBecomparedWith))) {
							FieldChange fieldChange = new FieldChange(
									field.getName(), field.get(
											baseMasterEntity1).toString(),
									field.get(toBecomparedWith).toString());
							entityChange.addFieldChange(fieldChange);
							field.setAccessible(false);
						}
					}
				}
			}
		}
		return entityChange;
	}

	public MasterLifeCycleData getMasterLifeCycleData() {
		if (masterLifeCycleData == null) {
			masterLifeCycleData = new MasterLifeCycleData();
		}
		return masterLifeCycleData;
	}

	public void setMasterLifeCycleData(MasterLifeCycleData masterLifeCycleData) {
		this.masterLifeCycleData = masterLifeCycleData;
	}

	/**
	 * @return the approvalStatus
	 */
	public int getApprovalStatus() {
		return getMasterLifeCycleData().getApprovalStatus();
	}

	/**
	 * @param approvalStatus
	 *            the approvalStatus to set
	 */
	public void setApprovalStatus(int approvalStatus) {
		getMasterLifeCycleData().setApprovalStatus(approvalStatus);
	}

	@PrePersist
	protected void prePersistCallback() {
		if (getEntityLifeCycleData().getUuid() == null) {
			getEntityLifeCycleData().setUuid(UUID.randomUUID().toString());
		}
	}

	/*
	 * (non-Javadoc) @see
	 * com.nucleus.entity.BaseEntity#populate(com.nucleus.entity.BaseEntity,
	 * com.nucleus.entity.CloneOptions)
	 */
	@Override
	protected void populate(BaseEntity clonedEntity, CloneOptions cloneOptions) {
		super.populate(clonedEntity, cloneOptions);
		if (cloneOptions.getCloneOptionAsLong(CloneOptionConstants.APPROVAL_STATUS_KEY) == -1 
				|| cloneOptions.getCloneOptionAsLong(CloneOptionConstants.APPROVAL_STATUS_KEY) == CloneOptionConstants.APPROVAL_STATUS_COPY_TRUE)
		{
			((BaseMasterEntity) clonedEntity)
					.setApprovalStatus(getApprovalStatus());
		} else {
			((BaseMasterEntity) clonedEntity)
					.setApprovalStatus((int) cloneOptions
							.getCloneOptionAsLong(CloneOptionConstants.APPROVAL_STATUS_KEY));
		}
		((BaseMasterEntity) clonedEntity).setActiveFlag(activeFlag);
		((BaseMasterEntity) clonedEntity).setOfflineFlag(offlineFlag);
		((BaseMasterEntity) clonedEntity).setUiMetaDataVo(uiMetaDataVo);
		((BaseMasterEntity) clonedEntity).setDynamicForm(dynamicForm);
	}

	@Override
	protected void populateFrom(BaseEntity copyEntity, CloneOptions cloneOptions) {
		if (cloneOptions
				.getCloneOptionAsBoolean(CloneOptionConstants.COPY_UUID_KEY)) {
			if (copyEntity.getEntityLifeCycleData() != null) {
				this.getEntityLifeCycleData().setUuid(
						copyEntity.getEntityLifeCycleData().getUuid());
			}
		}
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.COPY_ID_KEY)&&copyEntity.getId()!=null) 
        {
            	this.setId(copyEntity.getId());
        }
        if (cloneOptions
				.getCloneOptionAsLong(CloneOptionConstants.APPROVAL_STATUS_KEY) == CloneOptionConstants.APPROVAL_STATUS_COPY_TRUE) {
			this.setApprovalStatus(((BaseMasterEntity) copyEntity).getApprovalStatus());
		}
		this.getEntityLifeCycleData().setPersistenceStatus(
				copyEntity.getEntityLifeCycleData().getPersistenceStatus());
		this.setActiveFlag(((BaseMasterEntity) copyEntity).isActiveFlag());
		this.setTenantId(((BaseMasterEntity) copyEntity).getTenantId());
		this.setOfflineFlag(((BaseMasterEntity) copyEntity).getOfflineFlag());
		this.setUiMetaDataVo(((BaseMasterEntity) copyEntity).getUiMetaDataVo());
		this.setDynamicForm(((BaseMasterEntity) copyEntity).getDynamicForm());
		
	}

	/**
	 * @return the activeFlag
	 */
	public boolean isActiveFlag() {
		return activeFlag;
	}

	/**
	 * @param activeFlag
	 *            the activeFlag to set
	 */
	public void setActiveFlag(boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	
	public boolean isApproved(Integer approvalStatus) {
		return (approvalStatus == ApprovalStatus.APPROVED || approvalStatus == ApprovalStatus.APPROVED_DELETED
				||approvalStatus ==  ApprovalStatus.APPROVED_DELETED_IN_PROGRESS ||approvalStatus ==  ApprovalStatus.APPROVED_MODIFIED);

	}
	
	
	public boolean isApproved() {
		
		if(this.getMasterLifeCycleData()==null || this.getMasterLifeCycleData().getApprovalStatus()==null)
		{
			return false;
		}
		int approvalStatus=this.getApprovalStatus();
		return (approvalStatus == ApprovalStatus.APPROVED || approvalStatus == ApprovalStatus.APPROVED_DELETED
				||approvalStatus ==  ApprovalStatus.APPROVED_DELETED_IN_PROGRESS ||approvalStatus ==  ApprovalStatus.APPROVED_MODIFIED);

	}
	
	public boolean isApprovedAndActive() {

		if (this.getMasterLifeCycleData() == null || this.getMasterLifeCycleData().getApprovalStatus() == null) {
			return false;
		}
		int approvalStatus = this.getApprovalStatus();
		return (approvalStatus == ApprovalStatus.APPROVED || approvalStatus == ApprovalStatus.APPROVED_DELETED
				|| approvalStatus == ApprovalStatus.APPROVED_DELETED_IN_PROGRESS
				|| approvalStatus == ApprovalStatus.APPROVED_MODIFIED) && isActiveFlag();

	}
	
	public DynamicForm getDynamicForm() {
		return dynamicForm;
	}

	public void setDynamicForm(DynamicForm dynamicForm) {
		this.dynamicForm = dynamicForm;
	}

	public UIMetaDataVo getUiMetaDataVo() {
		return uiMetaDataVo;
	}

	public void setUiMetaDataVo(UIMetaDataVo uiMetaDataVo) {
		this.uiMetaDataVo = uiMetaDataVo;
	}
	

	
}