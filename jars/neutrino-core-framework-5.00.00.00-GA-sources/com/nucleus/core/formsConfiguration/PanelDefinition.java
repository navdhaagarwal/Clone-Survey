package com.nucleus.core.formsConfiguration;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="ui_panel_def_fk_index",columnList="ui_panel_def_fk")})
public class PanelDefinition extends BaseEntity {

    private static final long     serialVersionUID = -154827368156123813L;

    private String                panelName;

    private String                panelHeader;

    private boolean               accordian;

    private boolean               displayBorder;

    private int                   panelType;

    private String                panelKey;

    /**
     * value used to decide panel layout i.e one column or two column layout
     */
    private int                   panelColumnLayout;

    @OrderBy("fieldSequence")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "panel_field_def_fk")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<FieldDefinition> fieldDefinitionList;

    @ManyToOne(fetch = FetchType.LAZY)
    private SpecialTable specialTable;

    private String savedSpColumn;

    private String specialTablePartyRoles;

    private Integer panelSequence;



    public Integer getPanelSequence() {
		return panelSequence;
	}

	public void setPanelSequence(Integer panelSequence) {
		this.panelSequence = panelSequence;
	}

	public String getSavedSpColumn() {
        return savedSpColumn;
    }

    public void setSavedSpColumn(String savedSpColumn) {
        this.savedSpColumn = savedSpColumn;
    }

    public SpecialTable getSpecialTable() {
        return specialTable;
    }

    public void setSpecialTable(SpecialTable specialTable) {
        this.specialTable = specialTable;
    }
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductSchemeMetaData productSchemeMetaData;

    private Boolean allowPanelSave;

    public Boolean getAllowPanelSave() {
        return allowPanelSave;
    }

    public void setAllowPanelSave(Boolean allowPanelSave) {
        this.allowPanelSave = allowPanelSave;
    }
    
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	PanelDefinition panelDefinition=(PanelDefinition)baseEntity;
        super.populate(panelDefinition, cloneOptions);
        panelDefinition.setPanelName(panelName);
        panelDefinition.setPanelHeader(panelHeader);
        panelDefinition.setAccordian(accordian);
        panelDefinition.setDisplayBorder(displayBorder);
        panelDefinition.setPanelType(panelType);
        panelDefinition.setPanelKey(panelKey);
        panelDefinition.setPanelColumnLayout(panelColumnLayout);
        panelDefinition.setSpecialTable(specialTable);
        panelDefinition.setProductSchemeMetaData(productSchemeMetaData);
        panelDefinition.setSpecialTablePartyRoles(specialTablePartyRoles);
        if (hasElements(fieldDefinitionList)) {
        	List<FieldDefinition> clonedFieldDefinitionList = new ArrayList<FieldDefinition>();
            for (FieldDefinition fieldCustomOption : fieldDefinitionList) {
            	clonedFieldDefinitionList.add((FieldDefinition) fieldCustomOption.cloneYourself(cloneOptions));
            }
            panelDefinition.setFieldDefinitionList(clonedFieldDefinitionList);
        }  
        
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	
    	 
    	PanelDefinition panelDefinition=(PanelDefinition)baseEntity;
        super.populateFrom(panelDefinition, cloneOptions);
        this.setPanelName(panelDefinition.getPanelName());
        this.setPanelHeader(panelDefinition.getPanelHeader());
        this.setAccordian(panelDefinition.isAccordian());
        this.setDisplayBorder(panelDefinition.isDisplayBorder());
        this.setPanelType(panelDefinition.getPanelType());
        this.setPanelKey(panelDefinition.getPanelKey());
        this.setPanelColumnLayout(panelDefinition.getPanelColumnLayout());
        this.setSpecialTable(panelDefinition.getSpecialTable());
        this.setProductSchemeMetaData(panelDefinition.getProductSchemeMetaData());
        this.setSpecialTablePartyRoles(panelDefinition.getSpecialTablePartyRoles());
        if(this.getFieldDefinitionList()==null)
        {
        	this.setFieldDefinitionList(new ArrayList<FieldDefinition>());
        }
        if (hasElements(panelDefinition.getFieldDefinitionList())) {
        	
            this.getFieldDefinitionList().clear();
            for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {
            	this.getFieldDefinitionList().add((FieldDefinition) fieldDefinition.cloneYourself(cloneOptions));
            }
        }        
    }
    
    
    
    /**
     * @return the panelName
     */
    public String getPanelName() {
        return panelName;
    }

    /**
     * @return the panelKey
     */
    public String getPanelKey() {
        return panelKey;
    }

    /**
     * @param panelKey the panelKey to set
     */
    public void setPanelKey(String panelKey) {
        this.panelKey = panelKey;
    }

    /**
     * @param panelName the panelName to set
     */
    public void setPanelName(String panelName) {
        this.panelName = panelName;
    }

    /**
     * @return the panelHeader
     */
    public String getPanelHeader() {
        return panelHeader;
    }

    /**
     * @param panelHeader the panelHeader to set
     */
    public void setPanelHeader(String panelHeader) {
        this.panelHeader = panelHeader;
    }

    /**
     * @return the accordian
     */
    public boolean isAccordian() {
        return accordian;
    }

    /**
     * @param accordian the accordian to set
     */
    public void setAccordian(boolean accordian) {
        this.accordian = accordian;
    }

    /**
     * @return the fieldDefinitionList
     */
    public List<FieldDefinition> getFieldDefinitionList() {
        return fieldDefinitionList;
    }

    /**
     * @param fieldDefinitionList the fieldDefinitionList to set
     */
    public void setFieldDefinitionList(List<FieldDefinition> fieldDefinitionList) {
        this.fieldDefinitionList = fieldDefinitionList;
    }

    /**
     * @return the displayBorder
     */
    public boolean isDisplayBorder() {
        return displayBorder;
    }

    /**
     * @param displayBorder the displayBorder to set
     */
    public void setDisplayBorder(boolean displayBorder) {
        this.displayBorder = displayBorder;
    }

    /**
     * @return the panelColumnLayout
     */
    public int getPanelColumnLayout() {
        return panelColumnLayout;
    }

    /**
     * @param panelColumnLayout the panelColumnLayout to set
     */
    public void setPanelColumnLayout(int panelColumnLayout) {
        this.panelColumnLayout = panelColumnLayout;
    }

    /**
     * @return the panelType
     */
    public int getPanelType() {
        return panelType;
    }

    /**
     * @param panelType the panelType to set
     */
    public void setPanelType(int panelType) {
        this.panelType = panelType;
    }

	public ProductSchemeMetaData getProductSchemeMetaData() {
		return productSchemeMetaData;
	}

	public void setProductSchemeMetaData(ProductSchemeMetaData productSchemeMetaData) {
		this.productSchemeMetaData = productSchemeMetaData;
	}

    public String getSpecialTablePartyRoles() {
        return specialTablePartyRoles;
    }

    public void setSpecialTablePartyRoles(String specialTablePartyRoles) {
        this.specialTablePartyRoles = specialTablePartyRoles;
    }
}
