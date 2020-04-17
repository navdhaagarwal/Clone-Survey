package com.nucleus.rules.model;

import java.util.*;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

/**
 * @author Nucleus Software Exports Limited
 * Represents a Condition Expression in the system.
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Table(name = "re_parameters")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
@DeletionPreValidator(fieldName = "name")
public class Parameter extends BaseMasterEntity {

    private static final long                 serialVersionUID = 1;

    private String                            code;

    @Sortable(index = 1)
    private String                            name;

    private String                            description;

    private Integer                           dataType;

    private Integer                           paramType;

    private String                            sourceProduct;

    private boolean                           collectionBased  = false;

    @ManyToOne
    private ModuleName                        moduleName;

    private final static Map<Integer, String> parameterTypesMap;

    static {
        parameterTypesMap = new HashMap<Integer, String>();
        parameterTypesMap.put(1, "Field");
        parameterTypesMap.put(2, "Query");
        parameterTypesMap.put(3, "Constant");
        parameterTypesMap.put(4, "Reference");
        parameterTypesMap.put(5, "Computed");
        parameterTypesMap.put(6, "PlaceHolder");
        parameterTypesMap.put(7, "Script");
        parameterTypesMap.put(8, "Derived");
        parameterTypesMap.put(10, "Sql");

    }

    /**
     * getter for moduleName
     * @return
     */

    public ModuleName getModuleName() {
        return moduleName;
    }

    /**
     * setter for moduleName
     * @param moduleName
     */

    public void setModuleName(ModuleName moduleName) {
        this.moduleName = moduleName;
    }

    /**
     *
     * Getter for sourceProduct
     * @return
     */
    public String getSourceProduct() {
        return sourceProduct;
    }

    /**
     *
     * Setter for sourceProduct Property
     * @param sourceProduct
     */
    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    /**
     *
     * Default Constructor
     */
    public Parameter() {

    }

    /**
     * Getter for Description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for Description
     * @param
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * Getter for Name Property
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * Getter for data Type Property
     * @return
     */
    public Integer getDataType() {
        return dataType;
    }

    /**
     *
     * Getter for Parameter Type Property
     * @return
     */
    public Integer getParamType() {
        return paramType;
    }

    /**
     *
     * Setter for Name Property
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * Setter for data Type Property
     * @param dataType
     */
    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    /**
     *
     * Setter for Parameter Type Property
     * @param paramType
     */
    public void setParamType(Integer paramType) {
        this.paramType = paramType;
    }

    /**
     * getter for code
     * @return
     */

    public String getCode() {
        return code;
    }

    /**
     * setter for code
     * @param code
     */

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * getter of collection based
     * @return
     */
    public boolean isCollectionBased() {
        return collectionBased;
    }

    /**
     * setter of collection based 
     * @param collectionBased
     */
    public void setCollectionBased(boolean collectionBased) {
        this.collectionBased = collectionBased;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Parameter parameter = (Parameter) baseEntity;
        super.populate(parameter, cloneOptions);
        parameter.setDataType(dataType);
        parameter.setName(name);
        parameter.setCode(code);
        parameter.setParamType(paramType);
        parameter.setDescription(description);
        parameter.setSourceProduct(sourceProduct);
        parameter.setCollectionBased(collectionBased);
        parameter.setModuleName(moduleName);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Parameter parameter = (Parameter) baseEntity;
        super.populateFrom(parameter, cloneOptions);
        this.setDataType(parameter.getDataType());
        this.setCode(parameter.getCode());
        this.setName(parameter.getName());
        this.setParamType(parameter.getParamType());
        this.setDescription(parameter.getDescription());
        this.setSourceProduct(parameter.getSourceProduct());
        this.setCollectionBased(parameter.isCollectionBased());
        this.setModuleName(moduleName);
    }

    /**
     *
     * returns Parameter Type Name of the parameter
     * @param parameterType
     * @return
     */
    public String getParameterTypeName() {
        return parameterTypesMap.get(getParamType());
    }

    public static List<Integer> getParameterTypeValue(String value) {
        List<Integer> list = new ArrayList<>();
        for(Integer key:parameterTypesMap.keySet()){
            if(StringUtils.containsIgnoreCase(parameterTypesMap.get(key),value))
                list.add(key);
        }
        return list;
    }

    @Override
    public String getDisplayName() {
        return getName();

    }
}
