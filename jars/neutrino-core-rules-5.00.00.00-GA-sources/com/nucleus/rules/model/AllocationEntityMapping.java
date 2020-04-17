package com.nucleus.rules.model;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@DynamicInsert
@DynamicUpdate
@Cacheable
@Table(name = "ALLOCATION_ENITY_MAPPING")
@Synonym(grant="ALL")
public class AllocationEntityMapping extends BaseEntity {

    private static final long serialVersionUID = 7743653192348654554L;

    private String baseContextObjectName;
    private String selectField;
    private String ognlField;
    private String alias;
    private String selectFromEntity;
    private String entityType;
    
    @Column(length=2000)
    private String templateQuery;
    
    @Column(length=1000)
    private String joinStatement;

    /**
     * Getter for property 'baseContextObjectName'.
     *
     * @return Value for property 'baseContextObjectName'.
     */
    public String getBaseContextObjectName() {
        return baseContextObjectName;
    }

    /**
     * Setter for property 'baseContextObjectName'.
     *
     * @param baseContextObjectName Value to set for property 'baseContextObjectName'.
     */
    public void setBaseContextObjectName(String baseContextObjectName) {
        this.baseContextObjectName = baseContextObjectName;
    }

    /**
     * Getter for property 'selectField'.
     *
     * @return Value for property 'selectField'.
     */
    public String getSelectField() {
        return selectField;
    }

    /**
     * Setter for property 'selectField'.
     *
     * @param selectField Value to set for property 'selectField'.
     */
    public void setSelectField(String selectField) {
        this.selectField = selectField;
    }

    /**
     * Getter for property 'ognlField'.
     *
     * @return Value for property 'ognlField'.
     */
    public String getOgnlField() {
        return ognlField;
    }

    /**
     * Setter for property 'ognlField'.
     *
     * @param ognlField Value to set for property 'ognlField'.
     */
    public void setOgnlField(String ognlField) {
        this.ognlField = ognlField;
    }

    /**
     * Getter for property 'alias'.
     *
     * @return Value for property 'alias'.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Setter for property 'alias'.
     *
     * @param alias Value to set for property 'alias'.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Getter for property 'selectFromEntity'.
     *
     * @return Value for property 'selectFromEntity'.
     */
    public String getSelectFromEntity() {
        return selectFromEntity;
    }

    /**
     * Setter for property 'selectFromEntity'.
     *
     * @param selectFromEntity Value to set for property 'selectFromEntity'.
     */
    public void setSelectFromEntity(String selectFromEntity) {
        this.selectFromEntity = selectFromEntity;
    }

    /**
     * Getter for property 'entityType'.
     *
     * @return Value for property 'entityType'.
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Setter for property 'entityType'.
     *
     * @param entityType Value to set for property 'entityType'.
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * Getter for property 'templateQuery'.
     *
     * @return Value for property 'templateQuery'.
     */
    public String getTemplateQuery() {
        return templateQuery;
    }

    /**
     * Setter for property 'templateQuery'.
     *
     * @param templateQuery Value to set for property 'templateQuery'.
     */
    public void setTemplateQuery(String templateQuery) {
        this.templateQuery = templateQuery;
    }

    /**
     * Getter for property 'joinStatement'.
     *
     * @return Value for property 'joinStatement'.
     */
    public String getJoinStatement() {
        return joinStatement;
    }

    /**
     * Setter for property 'joinStatement'.
     *
     * @param joinStatement Value to set for property 'joinStatement'.
     */
    public void setJoinStatement(String joinStatement) {
        this.joinStatement = joinStatement;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AllocationEntityMapping allocationEntityMapping = (AllocationEntityMapping) baseEntity;
        super.populate(allocationEntityMapping, cloneOptions);
        allocationEntityMapping.setBaseContextObjectName(baseContextObjectName);
        allocationEntityMapping.setSelectField(selectField);
        allocationEntityMapping.setOgnlField(ognlField);
        allocationEntityMapping.setAlias(alias);
        allocationEntityMapping.setSelectFromEntity(selectFromEntity);
        allocationEntityMapping.setEntityType(entityType);
        allocationEntityMapping.setTemplateQuery(templateQuery);
        allocationEntityMapping.setJoinStatement(joinStatement);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        AllocationEntityMapping allocationEntityMapping = (AllocationEntityMapping) baseEntity;
        super.populateFrom(allocationEntityMapping, cloneOptions);
        this.setBaseContextObjectName(allocationEntityMapping.getBaseContextObjectName());
        this.setSelectField(allocationEntityMapping.getSelectField());
        this.setOgnlField(getOgnlField());
        this.setAlias(getAlias());
        this.setSelectFromEntity(getSelectFromEntity());
        this.setEntityType(getEntityType());
        this.setTemplateQuery(getTemplateQuery());
        this.setJoinStatement(getJoinStatement());
    }
}
