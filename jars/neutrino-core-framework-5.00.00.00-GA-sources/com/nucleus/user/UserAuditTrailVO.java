/**
 * 
 */
package com.nucleus.user;


import com.nucleus.core.role.entity.Role;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class UserAuditTrailVO {

    private Collection productType ;
    private Set<Long> products;
    private Map<Long,List<Long>> schemeProductMap;
    private Set<Long> branchAdmin;
    private Set<Long> branches;
    private Long lastUpdatedUserId;
    Map<Long, Set<String>> productBranchList;
    private Boolean accessToAllProducts;
    private List<Role> roles;

    public Collection getProductType() {
        return productType;
    }

    public void setProductType(Collection productType) {
        this.productType = productType;
    }

    public Set<Long> getProducts() {
        return products;
    }

    public void setProducts(Set<Long> products) {
        this.products = products;
    }

    public void setBranches(Set<Long> branches) {
        this.branches = branches;
    }

    public Set<Long> getBranches() {
        return branches;
    }

    public void setBranchAdmin(Set<Long> branchAdmin) {
        this.branchAdmin = branchAdmin;
    }

    public Set<Long> getBranchAdmin() {
        return branchAdmin;
    }

    public void setLastUpdatedUserId(Long lastUpdatedUserId) {
        this.lastUpdatedUserId = lastUpdatedUserId;
    }

    public Long getLastUpdatedUserId() {
        return lastUpdatedUserId;
    }

    public Map<Long, Set<String>> getProductBranchList() {
        return productBranchList;
    }

    public void setProductBranchList(Map<Long, Set<String>> productBranchList) {
        this.productBranchList = productBranchList;
    }

    public Boolean getAccessToAllProducts() {
        return accessToAllProducts;
    }

    public void setAccessToAllProducts(Boolean accessToAllProducts) {
        this.accessToAllProducts = accessToAllProducts;
    }

    public Map<Long, List<Long>> getSchemeProductMap() {
        return schemeProductMap;
    }

    public void setSchemeProductMap(Map<Long, List<Long>> schemeProductMap) {
        this.schemeProductMap = schemeProductMap;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
