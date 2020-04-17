/**
 * OrgBranchTree denotes the branch of the organization for which the
 * software is running. This is a light weight object this can be maintained in session.
 *
 * @author Nucleus Software Exports Limited
 */
package com.nucleus.user;

import java.io.Serializable;
import java.util.List;

public class OrgBranchTree implements Serializable {

    private static final long serialVersionUID = -3177802807321395823L;

    private Long              id;
    private String            orgName;
    private String            title;
    private String            key;
    private String            url;
    private String            level;
    private Boolean           isLazy;
    private Long              childOrgCount;
    private List<OrgBranchTree> children;
    private String organizationType;

    public OrgBranchTree(Long id, String orgName, String organizationType,
                         Long childOrgCount) {
        super();
        this.id = id;
        this.orgName = orgName;
        this.childOrgCount = childOrgCount;
        this.organizationType = organizationType;
    }

    public OrgBranchTree(Long id, String orgName, Long childOrgCount) {
        super();
        this.id = id;
        this.orgName = orgName;
        this.childOrgCount = childOrgCount;
    }

    public OrgBranchTree(Long id, String orgName) {
        super();
        this.id = id;
        this.orgName = orgName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Boolean getIsLazy() {
        return isLazy;
    }

    public void setIsLazy(Boolean lazy) {
        this.isLazy = lazy;
    }

    public OrgBranchTree() {
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


    public List<OrgBranchTree> getChildren() {
        return children;
    }


    public void setChildren(List<OrgBranchTree> children) {
        this.children = children;
    }

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

}
