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
@Table(name="PASSWORD_POLICY_DICT_WORDS")
@Synonym(grant = "ALL")
public class PasswordPolicyDictWords extends BaseEntity {

    private static final long serialVersionUID = 1L;

    String dictWords;

    public String getDictWords() {
        return dictWords;
    }

    public void setDictWords(String dictWords) {
        this.dictWords = dictWords;
    }
}
