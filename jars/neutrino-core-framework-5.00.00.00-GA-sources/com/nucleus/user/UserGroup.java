package com.nucleus.user;

import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.authority.Authority;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;

/**
 * Entity to represent User groups in the system. Contains the information of group metadata (name, description etc), 
 * its mapped users and authorities of the group. 
 * @author Nucleus Software India Pvt Ltd
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="ALL")
public class UserGroup extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 3532976267201390247L;

    @Column(unique = true)
    private String            name;

    private String            description;

    @OneToMany(fetch = FetchType.LAZY)
    private Set<User>         users;

    @OneToMany(fetch = FetchType.LAZY)
    private Set<Authority>    authorities;

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

}
