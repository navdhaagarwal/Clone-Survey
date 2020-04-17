package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;

@Entity
@Table(name="COM_COMM_DATA_PREP_DTL")
@Cacheable
@DynamicInsert 
@DynamicUpdate
@NamedQuery(name = "getDataPrepServiceBasedOnModuleAndServSel", query = "select a from CommunicationDataPreparationDetail a where a.sourceProduct=:sourceProduct and a.serviceSelectionCriteria =:serviceSelectionId and a.masterLifeCycleData.approvalStatus IN (:approvalStatus) and a.activeFlag=:activeFlag")
@Synonym(grant="SELECT")
public class CommunicationDataPreparationDetail extends BaseMasterEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name="CLASS_NAME")
	private String className;

	@Column(name="METHOD_TO_EXECUTE")
	private String methodToExecute;
	
	@Column(name="BEANID")
	private String beanId;
	
	@ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID")
	private SourceProduct sourceProduct;
	
	@Column(name="SERVICE_SEL_CRITERIA_ID")
	private Long serviceSelectionCriteria;

	/**
	 * @return the className	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return the methodToExecute
	 */
	public String getMethodToExecute() {
		return methodToExecute;
	}

	/**
	 * @param methodToExecute the methodToExecute to set
	 */
	public void setMethodToExecute(String methodToExecute) {
		this.methodToExecute = methodToExecute;
	}

	/**
	 * @return the beanId
	 */
	public String getBeanId() {
		return beanId;
	}

	/**
	 * @param beanId the beanId to set
	 */
	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}
	
	public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    /**
	 * @return the serviceSelectionCriteria
	 */
	public Long getServiceSelectionCriteria() {
		return serviceSelectionCriteria;
	}

	/**
	 * @param serviceSelectionCriteria the serviceSelectionCriteria to set
	 */
	public void setServiceSelectionCriteria(Long serviceSelectionCriteria) {
		this.serviceSelectionCriteria = serviceSelectionCriteria;
	}
	
	
	

}
