package com.nucleus.user;

import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.authority.Authority;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;

/**
 * Represents mapping between user and its associated roles.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="ALL")
public class UserAuthority extends BaseMasterEntity {
    // ~ Static fields/initializers =================================================================

    private static final long serialVersionUID = 1;

    @OneToOne(fetch = FetchType.LAZY)
    private User              associatedUser;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Authority>    authorities;

    public User getAssociatedUser() {
        return associatedUser;
    }

    public void setAssociatedUser(User user) {
        this.associatedUser = user;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }
}