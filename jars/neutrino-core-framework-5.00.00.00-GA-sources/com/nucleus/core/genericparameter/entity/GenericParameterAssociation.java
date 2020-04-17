package com.nucleus.core.genericparameter.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
@Table(indexes={@Index(name="associationName_index",columnList="associationName")})
public class GenericParameterAssociation extends BaseEntity {

    private static final long     serialVersionUID = 474379179789030804L;

    private String                associationName;

    @ManyToOne(fetch=FetchType.LAZY)
    private GenericParameter      genericParameter;

    /*JoinTable name given to keep table name under 30 chars for oracle*/
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "JT_GP_GP_Association")
    private Set<GenericParameter> associatedGenericParameter;

    public String getAssociationName() {
        return associationName;
    }

    public void setAssociationName(String associationName) {
        this.associationName = associationName;
    }

    public GenericParameter getGenericParameter() {
        return genericParameter;
    }

    public void setGenericParameter(GenericParameter genericParameter) {
        this.genericParameter = genericParameter;
    }

    public Set<GenericParameter> getAssociatedGenericParameter() {
        return associatedGenericParameter;
    }

    public void setAssociatedGenericParameter(Set<GenericParameter> associatedGenericParameter) {
        this.associatedGenericParameter = associatedGenericParameter;
    }

}
