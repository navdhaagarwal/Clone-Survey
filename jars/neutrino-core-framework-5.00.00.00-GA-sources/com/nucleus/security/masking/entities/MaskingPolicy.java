package com.nucleus.security.masking.entities;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterUtils;
import com.nucleus.master.marker.HistoryOptimizable;

@Entity
@DynamicUpdate
@DynamicInsert
@NamedQueries({
		@NamedQuery(name = "getMaskingPolcyByCode", query = "select maskingPolicy from MaskingPolicy maskingPolicy "
				+ "join fetch maskingPolicy.tagNameToBeMasked join fetch maskingPolicy.userRoles "
				+ "join fetch maskingPolicy.maskingDefinitions where maskingPolicy.maskingPolicyCode = :code"), })
@Synonym(grant = "SELECT")
public class MaskingPolicy extends BaseMasterEntity implements HistoryOptimizable{

	private static final long serialVersionUID = 1L;
	
	public static final String MASKING_DEFINITIONS = "maskingDefinitions";

	private String maskingPolicyName;

	@ManyToOne(fetch = FetchType.LAZY)
	private TagType tagNameToBeMasked;

	private String maskingPolicyCode;// TODO: Custom cache

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "masking_policy_role_mapping", joinColumns = {
			@JoinColumn(name = "masking_policy") }, inverseJoinColumns = { @JoinColumn(name = "role") })
	private Set<Role> userRoles;


	@OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
	@JoinColumn(name = "masking_policy")
	private List<MaskingDefinition> maskingDefinitions;

	public String getMaskingPolicyName() {
		return maskingPolicyName;
	}

	public void setMaskingPolicyName(String maskingPolicyName) {
		this.maskingPolicyName = maskingPolicyName;
	}

	public TagType getTagNameToBeMasked() {
		return tagNameToBeMasked;
	}

	public void setTagNameToBeMasked(TagType tagNameToBeMasked) {
		this.tagNameToBeMasked = tagNameToBeMasked;
	}

	public String getMaskingPolicyCode() {
		return maskingPolicyCode;
	}

	public void setMaskingPolicyCode(String maskingPolicyCode) {
		this.maskingPolicyCode = maskingPolicyCode;
	}

	public Set<Role> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(Set<Role> userRoles) {
		this.userRoles = userRoles;
	}

	public List<MaskingDefinition> getMaskingDefinitions() {
		return maskingDefinitions;
	}

	public List<MaskingDefinition> getApprovedMaskingDefinitions() {
		List<MaskingDefinition> approvedMaskingDefintitions= new ArrayList<>();
		for(MaskingDefinition maskingDefinition:maskingDefinitions){
			if(ApprovalStatus.APPROVED==maskingDefinition.getApprovalStatus()){
				approvedMaskingDefintitions.add(maskingDefinition);
			}
		}
		return approvedMaskingDefintitions;
	}
	public void setMaskingDefinitions(List<MaskingDefinition> maskingDefinitions) {
		this.maskingDefinitions = maskingDefinitions;
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {

		MaskingPolicy maskingPolicy = (MaskingPolicy) baseEntity;
		super.populate(maskingPolicy, cloneOptions);

		maskingPolicy.setMaskingPolicyName(maskingPolicyName);
		maskingPolicy.setTagNameToBeMasked(tagNameToBeMasked);
		maskingPolicy.setMaskingPolicyCode(maskingPolicyCode);
		//maskingPolicy.setUserRoles(userRoles);
		Set<Role> clonedUserRoles = new HashSet<Role>(userRoles);
		maskingPolicy.setUserRoles(clonedUserRoles);
		if (notNull(maskingDefinitions) && !maskingDefinitions.isEmpty()) {
			List<MaskingDefinition> cloneMaskingDefinitions = new ArrayList<MaskingDefinition>();
			for (MaskingDefinition maskingDefinition : maskingDefinitions) {
				cloneMaskingDefinitions.add((MaskingDefinition) maskingDefinition.cloneYourself(cloneOptions));
			}
			maskingPolicy.setMaskingDefinitions(cloneMaskingDefinitions);
		}

	}

	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		MaskingPolicy maskingPolicy = (MaskingPolicy) baseEntity;
		super.populateFrom(maskingPolicy, cloneOptions);

		this.setMaskingPolicyName(maskingPolicy.getMaskingPolicyName());
		this.setTagNameToBeMasked(maskingPolicy.getTagNameToBeMasked());
		this.setMaskingPolicyCode(maskingPolicy.getMaskingPolicyCode());
		this.setUserRoles(maskingPolicy.getUserRoles());
		if (ValidatorUtils.hasNoElements(this.getMaskingDefinitions())) {
			this.setMaskingDefinitions(new ArrayList<MaskingDefinition>());
		}
		if (ValidatorUtils.hasElements(maskingPolicy.getMaskingDefinitions())) {
			BaseMasterUtils.mergeModificationsToOrigionalEntity(this.getMaskingDefinitions(),
					maskingPolicy.getMaskingDefinitions(), cloneOptions);
		}
	}

	@Override
	public void loadLazyFields() {
		super.loadLazyFields();
		if (getUserRoles() != null) {
			for (Role role : getUserRoles()) {
				role.loadLazyFields();
			}
		}
		if (getMaskingDefinitions() != null) {
			for (MaskingDefinition maskingDefinition : getMaskingDefinitions()) {
				maskingDefinition.loadLazyFields();
			}
		}
	}

	public String getLogInfo() {
		String log = null;
		StringBuilder stf = new StringBuilder();
		stf.append("Masking Policy Master Object received to be saved ------------> ");
		stf.append(SystemPropertyUtils.getNewline());
		stf.append("Masking Policy Name : " + maskingPolicyName);
		stf.append(SystemPropertyUtils.getNewline());
		stf.append("Masking Policy Code :" + maskingPolicyCode);
		log = stf.toString();
		return log;
	}

}
