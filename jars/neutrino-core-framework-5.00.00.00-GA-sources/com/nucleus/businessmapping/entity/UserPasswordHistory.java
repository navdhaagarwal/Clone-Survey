/*
 * 
 */
package com.nucleus.businessmapping.entity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;

/**
 * This entity maintains the history of a User's password
 * @author Nucleus Software Exports Limited.
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="user_fk_index",columnList="user_fk")})
public class UserPasswordHistory extends BaseMasterEntity {

    private static final long serialVersionUID = 1L;

    private String            password;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          passwordChangedDate;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DateTime getPasswordChangedDate() {
        return passwordChangedDate;
    }

    public void setPasswordChangedDate(DateTime passwordChangedDate) {
        this.passwordChangedDate = passwordChangedDate;
    }

}
