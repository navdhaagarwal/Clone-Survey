package com.nucleus.shortcut;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;


@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@Table(
	    uniqueConstraints=
	        @UniqueConstraint(columnNames={"identifier", "source_product_id"})
	)
public class HotkeyElementIdentifier extends BaseMasterEntity{

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	
	private String identifier;
	
	private boolean identifierType;
	
	private String shortCutKeySuggestion;
	
	private String description;
	
	
	@OneToMany(fetch = FetchType.EAGER)
	@JoinTable(name="ElementRelatedToMapping",joinColumns={@JoinColumn(name="identifierId")})
	private List<HotkeyElementIdentifier> relatedToId;
	

	@OneToOne(fetch = FetchType.EAGER)
	private HotkeyElementIdentifier relatedTo;
	

    @ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID")
    private SourceProduct sourceProduct;
    
	public List<HotkeyElementIdentifier> getRelatedToId() {
		return relatedToId;
	}

	public void setRelatedToId(List<HotkeyElementIdentifier> relatedTo) {
		this.relatedToId = relatedTo;
	}

	public SourceProduct getSourceProduct() {
		return sourceProduct;
	}

	public void setSourceProduct(SourceProduct sourceProduct) {
		this.sourceProduct = sourceProduct;
	}


	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public boolean isIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(boolean identifierType) {
		this.identifierType = identifierType;
	}

	public String getShortCutKeySuggestion() {
		return shortCutKeySuggestion;
	}

	public void setShortCutKeySuggestion(String shortCutKeySuggestion) {
		this.shortCutKeySuggestion = shortCutKeySuggestion;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	

	public HotkeyElementIdentifier getRelatedTo() {
		return relatedTo;
	}

	public void setRelatedTo(HotkeyElementIdentifier relatedTo) {
		this.relatedTo = relatedTo;
	}

}
