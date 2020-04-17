/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.organization.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.address.City;
import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import com.nucleus.master.audit.annotation.NeutrinoAuditableMaster;
import com.nucleus.person.entity.PersonInfo;

/**
 * OrganizationBranch denotes the branch of the organization for which the
 * software is running. It can be head office, zonal office, branch, sub branch
 * etc.
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@DeletionPreValidator
@NeutrinoAuditableMaster(identifierColumn="branchCode")
public class OrganizationBranch extends Organization {

    private static final long         serialVersionUID = 8116637900762630423L;

    @EmbedInAuditAsReference
    @ManyToOne(fetch = FetchType.EAGER)
    private OrganizationType          organizationType;

    @EmbedInAuditAsValue(displayKey="label.branch.name")
    private String                    branchCode;

    private String                    parentBranchIds;

    @EmbedInAuditAsValue(displayKey="label.product.offered")
    private String                    productOffered;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "SIGNATURE_AUTHORITY")
    @EmbedInAuditAsValueObject(displayKey="label.signatory.authority")
    private PersonInfo                signatureAuthority;

    @EmbedInAuditAsValueObject(displayKey="label.branchCalendar.branchCalendar")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_CALENDAR")
    private BranchCalendar            branchCalendar;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "org_branch_fk")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private List<ParentBranchMapping> parentBranchMapping;

    @EmbedInAuditAsReference(columnToDisplay="cityCode")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ORGANIZATION_SERVED_CITY_LIST", joinColumns= {@JoinColumn(name="ORGANIZATION", referencedColumnName = "ID")},
    	    inverseJoinColumns = {@JoinColumn(name="SERVED_CITIES", referencedColumnName = "ID")}, indexes = { @javax.persistence.Index(columnList = "ORGANIZATION", name = "org_br_served_city_fk_index") })
    private List<City>                servedCities;

    @EmbedInAuditAsReference
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ORGANIZATION_VILLAGE_LIST", joinColumns= {@JoinColumn(name="ORGANIZATION", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name="VILLAGE", referencedColumnName = "ID")})
    private List<VillageMaster> servedVillages;

    @Transient
    private long[]                    servedCityIds;

    @Transient
    private long[]                    servedVillageIds;

    /*@ManyToOne(fetch = FetchType.LAZY)
    private OrganizationBranch               parentOrganizationBranch;*/

    @EmbedInAuditAsValue(displayKey="label.max.allowed.emails")
    private Long                      maximumEmails;
    
    @EmbedInAuditAsValue(displayKey="label.email.filter.enabled")
    private Boolean                   isEmailFilterEnabled=Boolean.FALSE;

    @EmbedInAuditAsValue(displayKey="label.assignmentMaster.moduleName")
    private String                    systemName;

    // Field added to monitor out of working hours login.
    @EmbedInAuditAsValue(displayKey="label.organization.outside.login.hours.enabled")
    private Boolean                   isOutOfWorkingHoursLoginAllowed;

    // Field added to monitor out of workin hours activity, to logout users after working hours if enabled.
    @EmbedInAuditAsValue(displayKey="label.organization.outside.login.hours.enabled")
    private Boolean                   isLogoutAfterWorkingHoursEnabled;

    @EmbedInAuditAsValue(displayKey="label.branch.short.name")
    private String                    shortName;

    @EmbedInAuditAsValue(displayKey="")
    private Boolean                   hasParentBranchCalender =true;

    // Field added to check printing branch if enabled.
    @EmbedInAuditAsValue(displayKey="label.organization.printing.branch.enabled")
    private Boolean                   isPrintingBranch  =   Boolean.TRUE;
  
	// Field added to stop fresh booking of loans from a particular branch if enabled.
    @EmbedInAuditAsValue(displayKey="label.organization.stop.fresh.booking.enabled")
    private Boolean                   isStopFreshBooking=Boolean.FALSE;
    
    @EmbedInAuditAsValue(displayKey="label.GST.GSTIN")
    private String 					  gstin;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrganizationBranchRiskCategory branchRiskCategory;

    public long[] getServedCityIds() {
        return servedCityIds;
    }

    public void setServedCityIds(long[] servedCityIds) {
        this.servedCityIds = servedCityIds;
    }

    public List<City> getServedCities() {
        return servedCities;
    }

    public void setServedCities(List<City> servedCities) {
        this.servedCities = servedCities;
    }

    public List<VillageMaster> getServedVillages() {
        return servedVillages;
    }

    public void setServedVillages(List<VillageMaster> servedVillages) {
        this.servedVillages = servedVillages;
    }

    public long[] getServedVillageIds() {
        return servedVillageIds;
    }

    public void setServedVillageIds(long[] servedVillageIds) {
        this.servedVillageIds = servedVillageIds;
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    /**
     * Sets the type of organization branch.
     */
    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public PersonInfo getSignatureAuthority() {
        return signatureAuthority;
    }

    public void setSignatureAuthority(PersonInfo signatureAuthority) {
        this.signatureAuthority = signatureAuthority;
    }

    /*public OrganizationBranch getParentOrganizationBranch() {
        return parentOrganizationBranch;
    }

    public void setParentOrganizationBranch(OrganizationBranch parentOrganizationBranch) {
        this.parentOrganizationBranch = parentOrganizationBranch;
    }*/

    public String getProductOffered() {
        return productOffered;
    }

    public void setProductOffered(String productOffered) {
        this.productOffered = productOffered;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    public String getParentBranchIds() {
        return parentBranchIds;
    }

    public void setParentBranchIds(String parentBranchIds) {
        this.parentBranchIds = parentBranchIds;
    }

    public Boolean getIsPrintingBranch() {
        return isPrintingBranch;
    }

    public void setIsPrintingBranch(Boolean isPrintingBranch) {
        this.isPrintingBranch = isPrintingBranch;
    }

    public Boolean getIsStopFreshBooking() {
		return isStopFreshBooking;
	}

	public void setIsStopFreshBooking(Boolean isStopFreshBooking) {
		this.isStopFreshBooking = isStopFreshBooking;
	}
	
	public String getGstin() {
			return gstin;
	}

	public void setGstin(String gstin) {
			this.gstin = gstin;
	}

    public OrganizationBranchRiskCategory getBranchRiskCategory() {
        return branchRiskCategory;
    }

    public void setBranchRiskCategory(OrganizationBranchRiskCategory branchRiskCategory) {
        this.branchRiskCategory = branchRiskCategory;
    }

	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        OrganizationBranch organizationBranch = (OrganizationBranch) baseEntity;
        super.populate(organizationBranch, cloneOptions);
        organizationBranch.setBranchCode(branchCode);
        organizationBranch.setOrganizationType(organizationType);
        if (parentBranchMapping != null) {
            organizationBranch.setParentBranchMapping(new ArrayList<ParentBranchMapping>());
            for (ParentBranchMapping mapping : parentBranchMapping) {
                organizationBranch.getParentBranchMapping().add((ParentBranchMapping) mapping.cloneYourself(cloneOptions));
            }
        }
        // organizationBranch.setParentOrganizationBranch(parentOrganizationBranch);
        organizationBranch.setProductOffered(productOffered);
        if (signatureAuthority != null) {
            organizationBranch.setSignatureAuthority((PersonInfo) signatureAuthority.cloneYourself(cloneOptions));
        }
        if (branchCalendar != null) {
            organizationBranch.setBranchCalendar((BranchCalendar) branchCalendar.cloneYourself(cloneOptions));
        }

        if (null != servedCities && servedCities.size() > 0) {
            organizationBranch.setServedCities(new ArrayList<City>(servedCities));
        }

        if(servedVillages!=null && !servedVillages.isEmpty()){
            organizationBranch.setServedVillages(new ArrayList<>(servedVillages));
        }
        // organizationBranch.setBranchCalendar(branchCalendar);
        organizationBranch.setParentBranchIds(parentBranchIds);
        organizationBranch.setSystemName(systemName);
        organizationBranch.setMaximumEmails(maximumEmails);
        organizationBranch.setIsEmailFilterEnabled(isEmailFilterEnabled);
        organizationBranch.setIsLogoutAfterWorkingHoursEnabled(isLogoutAfterWorkingHoursEnabled);
        organizationBranch.setIsOutOfWorkingHoursLoginAllowed(isOutOfWorkingHoursLoginAllowed);
        organizationBranch.setHasParentBranchCalender(hasParentBranchCalender);
        organizationBranch.setShortName(shortName);
        organizationBranch.setIsStopFreshBooking(isStopFreshBooking);
        organizationBranch.setIsPrintingBranch(isPrintingBranch);
        organizationBranch.setGstin(gstin);
        organizationBranch.setBranchRiskCategory(branchRiskCategory);

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        OrganizationBranch organizationBranch = (OrganizationBranch) baseEntity;
        super.populateFrom(organizationBranch, cloneOptions);
        this.setBranchCode(organizationBranch.getBranchCode());
        this.setOrganizationType(organizationBranch.getOrganizationType());
        // this.setParentOrganizationBranch(organizationBranch.getParentOrganizationBranch());
        if (parentBranchMapping != null) {
            this.getParentBranchMapping().clear();
        } else {
            this.setParentBranchMapping(new ArrayList<ParentBranchMapping>());
        }
        if (organizationBranch.getParentBranchMapping() != null && organizationBranch.getParentBranchMapping().size() > 0) {
            for (ParentBranchMapping mapping : organizationBranch.getParentBranchMapping()) {
                this.getParentBranchMapping().add((ParentBranchMapping) mapping.cloneYourself(cloneOptions));
            }

        }
        this.setParentBranchMapping(parentBranchMapping);
        this.setProductOffered(organizationBranch.getProductOffered());
        if (organizationBranch.getSignatureAuthority() != null) {
            this.setSignatureAuthority((PersonInfo) organizationBranch.getSignatureAuthority().cloneYourself(cloneOptions));
        }
        else{
        	this.setSignatureAuthority(null);
        }
        //CAS-20443-- wrong null check was there.
        if (organizationBranch.getBranchCalendar() != null) {
            this.setBranchCalendar((BranchCalendar) organizationBranch.getBranchCalendar().cloneYourself(cloneOptions));

        }else{
        	this.setBranchCalendar(null);
        }

        this.setServedCities(organizationBranch.getServedCities() != null && !organizationBranch.getServedCities().isEmpty() ? new ArrayList<>(organizationBranch
                .getServedCities()) : null);

        this.setServedVillages(organizationBranch.getServedVillages() !=null && !organizationBranch.getServedVillages().isEmpty() ?
                new ArrayList<>(organizationBranch.getServedVillages()):null);
        this.setParentBranchIds(organizationBranch.getParentBranchIds());
        this.setSystemName(organizationBranch.getSystemName());
        this.setMaximumEmails(organizationBranch.getMaximumEmails());
        this.setIsEmailFilterEnabled(organizationBranch.getIsEmailFilterEnabled());
        this.setIsLogoutAfterWorkingHoursEnabled(organizationBranch.getIsLogoutAfterWorkingHoursEnabled());
        this.setIsOutOfWorkingHoursLoginAllowed(organizationBranch.getIsOutOfWorkingHoursLoginAllowed());
        this.setHasParentBranchCalender(organizationBranch.getHasParentBranchCalender());
        this.setShortName(organizationBranch.getShortName());
        this.setIsPrintingBranch(organizationBranch.getIsPrintingBranch());
        this.setIsStopFreshBooking(organizationBranch.getIsStopFreshBooking());
        this.setGstin(organizationBranch.getGstin());
        this.setBranchRiskCategory(organizationBranch.getBranchRiskCategory());
    }

    /**
     * @return the branchCalendar
     */
    public BranchCalendar getBranchCalendar() {
        return branchCalendar;
    }

    /**
     * @param branchCalendar the branchCalendar to set
     */
    public void setBranchCalendar(BranchCalendar branchCalendar) {
        this.branchCalendar = branchCalendar;
    }

    public String getLogInfo() {
        String log = null;

        log = "\nBranch Code: " + branchCode;
        log += "\nBranch Name: " + getName();
        if (organizationType != null) {
            log += "\nOrganisation Type: " + organizationType.getId();
        }
        if (parentBranchMapping != null) {
            log += "\nParent Organisation Branch: " + parentBranchMapping;
        }
        /* if (parentOrganizationBranch != null) {
             log += "\nParent Organisation Branch: " + parentOrganizationBranch.getId();
         }*/
        if (getContactInfo() != null && getContactInfo().getAddress() != null) {
            log += "\nOrganisation Branch Address: " + getContactInfo().getAddress().getLogInfo();
        }
        return log;
    }

    public Long getMaximumEmails() {
        return maximumEmails;
    }

    public void setMaximumEmails(Long maximumEmails) {
        this.maximumEmails = maximumEmails;
    }

    public Boolean getIsEmailFilterEnabled() {
        return isEmailFilterEnabled;
    }

    public void setIsEmailFilterEnabled(Boolean isEmailFilterEnabled) {
        this.isEmailFilterEnabled = isEmailFilterEnabled;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Boolean getIsOutOfWorkingHoursLoginAllowed() {
        return isOutOfWorkingHoursLoginAllowed;
    }

    public void setIsOutOfWorkingHoursLoginAllowed(Boolean isOutOfWorkingHoursLoginAllowed) {
        this.isOutOfWorkingHoursLoginAllowed = isOutOfWorkingHoursLoginAllowed;
    }

    public Boolean getIsLogoutAfterWorkingHoursEnabled() {
        return isLogoutAfterWorkingHoursEnabled;
    }

    public void setIsLogoutAfterWorkingHoursEnabled(Boolean isLogoutAfterWorkingHoursEnabled) {
        this.isLogoutAfterWorkingHoursEnabled = isLogoutAfterWorkingHoursEnabled;
    }

    public List<ParentBranchMapping> getParentBranchMapping() {
        return parentBranchMapping;
    }

    public void setParentBranchMapping(List<ParentBranchMapping> parentBranchMapping) {
        this.parentBranchMapping = parentBranchMapping;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Boolean getHasParentBranchCalender() {
        if(hasParentBranchCalender!=null){
        	return hasParentBranchCalender;
    	}else{
    		return Boolean.FALSE;
    	}
    }

    public void setHasParentBranchCalender(Boolean hasParentBranchCalender) {
    	if(hasParentBranchCalender!=null){
    		this.hasParentBranchCalender = hasParentBranchCalender;
    	}else{
    		this.hasParentBranchCalender=Boolean.FALSE;
    	}
        
    }

}