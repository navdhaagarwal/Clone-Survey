package com.nucleus.employment;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class EmploymentInfo extends BaseEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    private String            employeeId;
    private String            department;
    private String            title;
    private String            company;

    public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        EmploymentInfo employmentInfo = (EmploymentInfo) baseEntity;
        super.populate(employmentInfo, cloneOptions);
        employmentInfo.setEmployeeId(employeeId);
    }
}