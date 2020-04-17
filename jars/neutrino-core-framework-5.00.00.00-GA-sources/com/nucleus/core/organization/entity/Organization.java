package com.nucleus.core.organization.entity;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.contact.SimpleContactInfo;
import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import com.nucleus.person.entity.PersonInfo;

/**
 * Organization is the abstract super class to denote all kind of organizations in the system like dealers, branches, agents etc.
 * This class is just meant to hold all generic fields which are common across anything which denotes an organization like its address, phone number contact person etc. 
 */
@Entity
@DynamicInsert
@DynamicUpdate
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cacheable
@Synonym(grant="SELECT,REFERENCES")
@Table(indexes={@Index(name="org_name_index",columnList="name")})
public abstract class Organization extends BaseMasterEntity {

    private static final long serialVersionUID = -1224275549881100322L;

    // ~ Instance fields ============================================================================

    @Sortable
    private String            name;

    private String            description;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "CONTACT_INFO")
    @EmbedInAuditAsValueObject(skipInDisplay = true)
    private SimpleContactInfo contactInfo;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PRIMARY_CONTACT_PERSON")
    @EmbedInAuditAsValueObject(displayKey="label.contact.person")
    private PersonInfo        primaryContactPerson;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SimpleContactInfo getContactInfo() {
        return contactInfo;
    }

    /**
     * Sets the contact info.
     *
     * @param contactInfo the new contact info
     */
    public void setContactInfo(SimpleContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public PersonInfo getPrimaryContactPerson() {
        return primaryContactPerson;
    }

    public void setPrimaryContactPerson(PersonInfo primaryContactPerson) {
        this.primaryContactPerson = primaryContactPerson;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Organization organization = (Organization) baseEntity;
        super.populate(organization, cloneOptions);
        // Root organization does not permit setting of name and description
        if (!(organization instanceof RootOrganization)) {
            organization.setDescription(description);
            organization.setName(name);
        }
        organization
                .setContactInfo(null != contactInfo ? (SimpleContactInfo) contactInfo.cloneYourself(cloneOptions) : null);
        organization.setPrimaryContactPerson(null != primaryContactPerson ? (PersonInfo) primaryContactPerson
                .cloneYourself(cloneOptions) : null);

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Organization organization = (Organization) baseEntity;
        super.populateFrom(organization, cloneOptions);
        this.setContactInfo(null != organization.getContactInfo() ? (SimpleContactInfo) organization.getContactInfo()
                .cloneYourself(cloneOptions) : null);
        this.setDescription(organization.getDescription());
        this.setName(organization.getName());
        this.setPrimaryContactPerson(null != organization.getPrimaryContactPerson() ? (PersonInfo) organization
                .getPrimaryContactPerson().cloneYourself(cloneOptions) : null);

    }

}