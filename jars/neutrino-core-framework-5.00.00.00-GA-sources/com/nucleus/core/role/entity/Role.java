package com.nucleus.core.role.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.nucleus.authority.Authority;
import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.user.User;

/**
 * Role class to represent the concept of roles in the system. Contains the
 * information of role metadata (name, description etc), its mapped users and
 * authorities of the role.
 * 
 * @author Nucleus Software India Pvt Ltd
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@DeletionPreValidator
@Synonym(grant="ALL")
@Table(indexes={@Index(name="RAIM_PERF_45_4368",columnList="REASON_ACT_INACT_MAP"),
        @Index(name="name_index_Rle",columnList="name"),@Index(name="productDesc_index",columnList="productDescriminator")})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Role.class
)
public class Role extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 3532976267201390247L;

    @Column
    @Sortable
    private String            name;

    private String            description;

    private String            productDescriminator;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_users",
	joinColumns = { @JoinColumn(name = "role") },
	inverseJoinColumns = { @JoinColumn(name = "users") })
    @Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<User>         users;

    @ManyToMany(fetch = FetchType.LAZY)
    @Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<Authority>    authorities;

	@Transient
    private long[]            authorityIds;
	
	@Transient
    private String           authorityNames;	
	
	@Transient
    private String           authorityDescription;
	
	@Transient
    private String            authorityIdsString;
	
	@Transient
    private String            authoritySourceProductString;


    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;
	
	public String getAuthoritySourceProductString() {
		return authoritySourceProductString;
	}

	public void setAuthoritySourceProductString(String authoritySourceProductString) {
		this.authoritySourceProductString = authoritySourceProductString;
	}

	public String getAuthorityDescription() {
		return authorityDescription;
	}

	public void setAuthorityDescription(String authorityDescription) {
		this.authorityDescription = authorityDescription;
	}

	public String getAuthorityNames() {
		return authorityNames;
	}

	public void setAuthorityNames(String authorityNames) {
		this.authorityNames = authorityNames;
	}
	public String getAuthorityIdsString() {
		return authorityIdsString;
	}

	public void setAuthorityIdsString(String authorityIdsString) {
		this.authorityIdsString = authorityIdsString;
	}


	@Column(name = "IS_DISABLED")
    private Boolean				isDisabled;

	@Column(name = "DISABLED_FROM")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime			 	disabledFrom;

	@Column(name = "DISABLED_TO")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime			disabledTo;
    
	public Boolean getIsDisabled() {
		return isDisabled;
	}

	public void setIsDisabled(Boolean isDisabled) {
		this.isDisabled = isDisabled;
	}
    
	public DateTime getDisabledFrom() {
		return disabledFrom;
	}

	public void setDisabledFrom(DateTime disabledFrom) {
		this.disabledFrom = disabledFrom;
	}

	public DateTime getDisabledTo() {
		return disabledTo;
	}

	public void setDisabledTo(DateTime disabledTo) {
		this.disabledTo = disabledTo;
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

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> roles) {
        this.authorities = roles;
    }

    public long[] getAuthorityIds() {
        return authorityIds;
    }

    public void setAuthorityIds(long[] authorityIds) {
        this.authorityIds = authorityIds;
    }

    public String getProductDescriminator() {
        return productDescriminator;
    }

    public void setProductDescriminator(String productDescriminator) {
        this.productDescriminator = productDescriminator;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Role role = (Role) baseEntity;
        super.populate(role, cloneOptions);
        if (authorities != null && authorities.size() > 0) {
            Set<Authority> authoritySet = new HashSet<Authority>();
            for (Authority authority : authorities) {
                authoritySet.add(authority);
            }
            role.setAuthorities(authoritySet);
        }
        role.setDescription(description);
        role.setName(name);
        role.setProductDescriminator(productDescriminator);
        role.setIsDisabled(isDisabled);
        role.setDisabledFrom(disabledFrom);
        role.setDisabledTo(disabledTo);
        if (reasonActInactMap != null) {
            role.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Role role = (Role) baseEntity;
        super.populateFrom(role, cloneOptions);
        this.setName(role.getName());
        this.setDescription(role.getDescription());
        this.setProductDescriminator(productDescriminator);
        if (role.getAuthorities() != null && role.getAuthorities().size() > 0) {
            this.setAuthorities(new HashSet<Authority>(role.getAuthorities()));
        }
        this.setIsDisabled(role.isDisabled);
        this.setDisabledFrom(role.disabledFrom);
        this.setDisabledTo(role.disabledTo);
        if (role.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) role.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("Role Name: " + name);
        stf.append(SystemPropertyUtils.getNewline());
        String[] authorityIds = authorityIdsString.split(",");
        for (String authority : authorityIds) {
            stf.append("Authority IDs:" + authority);

        }
        log = stf.toString();

        return log;
    }

    @Override
    public void loadLazyFields()
    {
    	super.loadLazyFields();
    	if(getUsers()!=null)
    	{
    		for(User user:users)
    		{
    			if(user!=null)
    			{
        			user.loadLazyFields();
    			}
    		}
    	}
    	if(getAuthorities()!=null)
    	{
    		for(Authority authority:authorities)
    		{
    			if(authority!=null)
    			{
        			authority.loadLazyFields();
    			}
    		}
    	}
    }
}
