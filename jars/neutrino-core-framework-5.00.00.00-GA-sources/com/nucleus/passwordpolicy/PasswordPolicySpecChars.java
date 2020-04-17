package com.nucleus.passwordpolicy;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name="PASSWORD_POLICY_SPEC_CHARS")
@Synonym(grant = "ALL")
public class PasswordPolicySpecChars extends BaseEntity {
    private static final long serialVersionUID = 1L;

    String specChar;

    public String getSpecChar() {
        return specChar;
    }

    public void setSpecChar(String specChar) {
        this.specChar = specChar;
    }
}
