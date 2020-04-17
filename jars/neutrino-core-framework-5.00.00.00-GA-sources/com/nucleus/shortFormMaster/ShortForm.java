package com.nucleus.shortFormMaster;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class ShortForm extends BaseMasterEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String            code;
    private String            description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ShortForm shortForm = (ShortForm) baseEntity;
        super.populate(shortForm, cloneOptions);
        shortForm.setCode(code);
        shortForm.setDescription(description);

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ShortForm shortForm = (ShortForm) baseEntity;
        super.populateFrom(shortForm, cloneOptions);
        this.setCode(shortForm.getCode());
        this.setDescription(shortForm.getDescription());
    }

    @Override
    public String getDisplayName() {
        return code;
    }
}
