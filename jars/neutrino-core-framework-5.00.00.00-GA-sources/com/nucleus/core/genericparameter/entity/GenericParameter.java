package com.nucleus.core.genericparameter.entity;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.authority.Authority;
import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.i18n.entity.MultiLingualValue;
import io.swagger.annotations.ApiModelProperty;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="gp_code_index",columnList="code")})
public abstract class GenericParameter extends BaseMasterEntity {

    private static final long serialVersionUID = 1196733183588786010L;

    @Column(updatable = false)
    private String            code;

    @Embedded
    @ApiModelProperty(hidden=true)
    @AttributeOverrides({ @AttributeOverride(name = "delimitedValue", column = @Column(name = "MULTI_LINGUAL_NAME")) })
    private MultiLingualValue multiLingualName;

    @Embedded
    @ApiModelProperty(hidden=true)
    @AttributeOverrides({ @AttributeOverride(name = "delimitedValue", column = @Column(name = "MULTI_LINGUAL_DESCRIPTION")) })
    private MultiLingualValue multiLingualDescription;

    @Sortable
    private String            name;

    private String            description;

    private String            parentCode;

    private Boolean           notModifiable    = Boolean.FALSE;

    @ApiModelProperty(hidden=true)
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<Authority>    authorities;
    
    private Boolean offlineFlag = Boolean.FALSE;

    private Boolean  defaultFlag =Boolean.FALSE;
    private String dynamicParameterName;
    
	public String getDynamicParameterName() {
		return dynamicParameterName;
	}

	public void setDynamicParameterName(String dynamicParameterName) {
		this.dynamicParameterName = dynamicParameterName;
	}

	public Boolean getOfflineFlag() {
      return notNull(offlineFlag)?offlineFlag:Boolean.FALSE;
    }    
    
    public void setOfflineFlag(Boolean offlineFlag) {
      this.offlineFlag=notNull(offlineFlag)?offlineFlag:Boolean.FALSE;
    }

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public MultiLingualValue getMultiLingualName() {
        return multiLingualName;
    }

    public void setMultiLingualName(MultiLingualValue name) {
        this.multiLingualName = name;
    }

    public MultiLingualValue getMultiLingualDescription() {
        return multiLingualDescription;
    }

    public void setMultiLingualDescription(MultiLingualValue description) {
        this.multiLingualDescription = description;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    /**
     * @return the notModifiable
     */
    public Boolean getNotModifiable() {
        return notModifiable;
    }

    /**
     * @param notModifiable the notModifiable to set
     */
    public void setNotModifiable(Boolean notModifiable) {
        this.notModifiable = notModifiable;
    }

    /**
     * for search framework
     */
    public String getDisplayName() {
        return name;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public Boolean getDefaultFlag() {
        return defaultFlag;
    }

    public void setDefaultFlag(Boolean defaultFlag) {
        this.defaultFlag = defaultFlag;
    }
    
    public void initializeAuthorities() {
    	if(getAuthorities()!=null) {
 			getAuthorities().size();	
 		}
    }

    @Override
	public void loadLazyFields() {
		
		super.loadLazyFields();
		if(getAuthorities()!=null)
		{
			for(Authority authority:getAuthorities())
			{
				if(authority!=null)
				{
					authority.loadLazyFields();
				}
			}
		}
	}


    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        GenericParameter genericParameter = (GenericParameter) baseEntity;
        super.populate(genericParameter, cloneOptions);
        genericParameter.setCode(code);
        genericParameter.setName(name);
        genericParameter.setDescription(description);
        genericParameter.setParentCode(parentCode);
        genericParameter.setDefaultFlag(defaultFlag);
        genericParameter.setOfflineFlag(offlineFlag);
        genericParameter.setDynamicParameterName(dynamicParameterName);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        GenericParameter genericParameter = (GenericParameter) baseEntity;
        super.populateFrom(genericParameter, cloneOptions);
        this.setCode(genericParameter.getCode());
        this.setName(genericParameter.getName());
        this.setDescription(genericParameter.getDescription());
        this.setParentCode(genericParameter.getParentCode());
        this.setDefaultFlag(genericParameter.getDefaultFlag());
        this.setOfflineFlag(genericParameter.getOfflineFlag());
        this.setDynamicParameterName(genericParameter.getDynamicParameterName());

    }

}
