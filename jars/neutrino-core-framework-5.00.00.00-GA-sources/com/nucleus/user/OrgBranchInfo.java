/**
 * OrgBranchInfo denotes the branch of the organization for which the
 * software is running. This is a light weight object this can be maintained in session.
 * 
 * @author Nucleus Software Exports Limited
 */
package com.nucleus.user;

import java.io.Serializable;
import java.util.List;

public class OrgBranchInfo implements Serializable {

    private static final long serialVersionUID = -3177802807321395823L;

    private Long              id;
    private String            orgName;
    private String            title;
    private String            key;
    private Boolean           lazy;
    private Long              childOrgCount;
    private List<OrgBranchInfo> children;
    private String organizationType;    

	public OrgBranchInfo(Long id, String orgName, String organizationType,
			Long childOrgCount) {
		super();
		this.id = id;
		this.orgName = orgName;
		this.childOrgCount = childOrgCount;
		this.organizationType = organizationType;
	}

	public OrgBranchInfo(Long id, String orgName, Long childOrgCount) {
        super();
        this.id = id;
        this.orgName = orgName;
        this.childOrgCount = childOrgCount;
    }
    
    public OrgBranchInfo(Long id, String orgName) {
        super();
        this.id = id;
        this.orgName = orgName;
    }
    

    public OrgBranchInfo() {
        super();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getLazy() {
        return lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Long getChildOrgCount() {
        return childOrgCount;
    }

    public void setChildOrgCount(Long childOrgCount) {
        this.childOrgCount = childOrgCount;
    }


    public List<OrgBranchInfo> getChildren() {
        return children;
    }


    public void setChildren(List<OrgBranchInfo> children) {
        this.children = children;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

}
