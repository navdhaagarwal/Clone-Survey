package com.nucleus.shortcut;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.NamedQuery;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;


@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant = "ALL")
@NamedQuery(name = "getHotKeys", query = "select distinct hotKeys from Hotkeys hotKeys JOIN FETCH hotKeys.elementMapping as elementMapping where hotKeys.hotKeyType=:hotKeyType "
		+ "and hotKeys.masterLifeCycleData.approvalStatus IN :approvalStatus  and hotKeys.activeFlag = true "
		+ "and (hotKeys.entityLifeCycleData.snapshotRecord is null or hotKeys.entityLifeCycleData.snapshotRecord = false) "
		+ "and (elementMapping.sourceProduct is null or elementMapping.sourceProduct.id IN :sourceProduct)")
public class Hotkeys extends BaseMasterEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(unique = true)
	private String hotKey;

	private int hotKeyType;

	@OneToMany(fetch = FetchType.EAGER)
	private List<HotkeyElementIdentifier> elementMapping;

	public List<HotkeyElementIdentifier> getElementMapping() {
		return elementMapping;
	}

	public void setElementMapping(List<HotkeyElementIdentifier> elementMapping) {
		this.elementMapping = elementMapping;
	}

	private String description;

	public String getHotKey() {
		return hotKey;
	}

	public void setHotKey(String hotKey) {
		this.hotKey = hotKey;
	}

	public int getHotKeyType() {
		return hotKeyType;
	}

	public void setHotKeyType(int hotKeyType) {
		this.hotKeyType = hotKeyType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
