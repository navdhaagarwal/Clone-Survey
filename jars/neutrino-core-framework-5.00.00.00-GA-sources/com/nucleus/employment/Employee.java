package com.nucleus.employment;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.money.entity.Money;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.person.entity.Person;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class Employee extends Person {

    @Transient
    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EmploymentInfo    employmentInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employee          manager;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="baseAmount.baseValue",column=@Column(name="ys_cl_base_value",precision = 25, scale = 7)),
        @AttributeOverride(name="baseAmount.baseCurrencyCode",column=@Column(name="ys_cl_base_curr_code")),
        @AttributeOverride(name="nonBaseAmount.nonBaseValue",column=@Column(name="ys_cl_non_base_value",precision = 25, scale = 7)),
        @AttributeOverride(name="nonBaseAmount.nonBasecurrencyCode",column=@Column(name="ys_cl_non_base_curr_code"))
        })

    private Money             yearlySalary;

    public EmploymentInfo getEmploymentInfo() {
        return employmentInfo;
    }

    public void setEmploymentInfo(EmploymentInfo employmentInfo) {
        this.employmentInfo = employmentInfo;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public Money getYearlySalary() {
        return yearlySalary;
    }

    public void setYearlySalary(Money yearlySalary) {
        this.yearlySalary = yearlySalary;
    }
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Employee employee = (Employee) baseEntity;
        super.populate(employee, cloneOptions);
        if(employmentInfo!= null){
            employee.setEmploymentInfo((EmploymentInfo)employmentInfo.cloneYourself(cloneOptions));
        }
        employee.setManager(employee);
        if(yearlySalary!= null){
            employee.setYearlySalary(yearlySalary.cloneYourself(cloneOptions));
        }
    }
    
    
    @Override
    public void loadLazyFields()
    {
    	super.loadLazyFields();
    	if(getEmploymentInfo()!=null)
    	{
    		getEmploymentInfo().loadLazyFields();
    	}
    	if(getManager()!=null)
    	{
    		getManager().loadLazyFields();
    	}
    }
}