package com.nucleus.core.organizationtemplate.entity;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Table(name= "org_temp_mapping",indexes={@Index(name="templateKey_index",columnList="templateKey")})
@Synonym(grant="ALL")
public class OrganizationTemplateMapping extends BaseEntity{

    private static final long serialVersionUID = 1L;
    
    @OneToOne
    private OrganizationBranch organizationBranch;
    private String templateKey;
    
    @Column(length = 2000)
    private String templateMessage;
    
    public OrganizationBranch getOrganizationBranch() {
        return organizationBranch;
    }
    public void setOrganizationBranch(OrganizationBranch organizationBranch) {
        this.organizationBranch = organizationBranch;
    }
    public String getTemplateKey() {
        return templateKey;
    }
    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }
    public String getTemplateMessage() {
        return templateMessage;
    }
    public void setTemplateMessage(String templateMessage) {
        this.templateMessage = templateMessage;
    }
       

}
