package com.nucleus.core.formsConfiguration;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "DYNAMIC_FORM_SCREEN_MAPPING",indexes={@Index(name="RAIM_PERF_45_4334",columnList="REASON_ACT_INACT_MAP")})
@Cacheable
@Synonym(grant="SELECT")
public class DynamicFormScreenMapping extends BaseMasterEntity {

    private static final long serialVersionUID = 1L;
    
    @OrderBy("formSequence")
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "DYNAMIC_FORM_SCREEN_MAP_ID", referencedColumnName = "ID")
    private List<DynamicFormScreenMappingDetail> dynamicFormScreenDtlList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCREEN_ID", referencedColumnName = "ID", updatable = false, insertable = false)
    private ScreenId screenId;

    @Column(name = "SCREEN_ID")
    private Long screenIdValue;
    
    @ManyToOne
    @JoinColumn(name = "SOURCE_PRODUCT_ID", referencedColumnName = "ID", updatable = false, insertable = false)
    private SourceProduct sourceProduct;
    
    @Column(name = "SOURCE_PRODUCT_ID")
    private Long sourceProductId;


    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    public Long getSourceProductId() {
        return sourceProductId;
    }

    public void setSourceProductId(Long sourceProductId) {
        this.sourceProductId = sourceProductId;
    }

    public List<DynamicFormScreenMappingDetail> getDynamicFormScreenDtlList() {
        return dynamicFormScreenDtlList;
    }

    public void setDynamicFormScreenDtlList(
            List<DynamicFormScreenMappingDetail> dynamicFormScreenDtlList) {
        this.dynamicFormScreenDtlList = dynamicFormScreenDtlList;
    }


    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DynamicFormScreenMapping dynamicFormScreen = (DynamicFormScreenMapping) baseEntity;
        super.populate(dynamicFormScreen, cloneOptions);
		dynamicFormScreen.setScreenIdValue(this.screenIdValue);
		dynamicFormScreen.setScreenId(this.screenId);
		dynamicFormScreen
				.setDynamicFormScreenDtlList(dynamicFormScreenDtlList != null && !dynamicFormScreenDtlList.isEmpty()
						? new ArrayList<DynamicFormScreenMappingDetail>(dynamicFormScreenDtlList) : null);
		dynamicFormScreen.setSourceProduct(this.sourceProduct);
		dynamicFormScreen.setSourceProductId(this.sourceProductId);
        if (reasonActInactMap != null) {
            dynamicFormScreen.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DynamicFormScreenMapping dynamicFormScreen = (DynamicFormScreenMapping) baseEntity;
        super.populateFrom(dynamicFormScreen, cloneOptions);
        setDynamicFormScreenDtlList(dynamicFormScreen.getDynamicFormScreenDtlList() != null && !dynamicFormScreen.getDynamicFormScreenDtlList().isEmpty() ? dynamicFormScreen.getDynamicFormScreenDtlList() : null);
        setScreenIdValue(dynamicFormScreen.getScreenIdValue());
        setScreenId(dynamicFormScreen.getScreenId());
        setSourceProduct(dynamicFormScreen.getSourceProduct());
        setSourceProductId(dynamicFormScreen.getSourceProductId());
        if (dynamicFormScreen.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) dynamicFormScreen.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    public ScreenId getScreenId() {
        return screenId;
    }

    public void setScreenId(ScreenId screenId) {
        this.screenId = screenId;
    }

    public Long getScreenIdValue() {
        return screenIdValue;
    }

    public void setScreenIdValue(Long screenIdValue) {
        this.screenIdValue = screenIdValue;
    }

    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }
    
    @Override
	public String getDisplayName() {
    	if(getScreenId()!=null){
    		return  getScreenId().getScreenName();
    	}
    	
    	return super.getDisplayName();
	}

}