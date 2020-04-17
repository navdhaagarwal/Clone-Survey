package com.nucleus.core.role.roleVO;

public class RoleInfoVO
{
    private String[] authorityNames;
    private String[] authorityIds;
    private String[] authorityDescription;
    private String[] authorityModuleName;

    public String[] getAuthorityIds() {
        return authorityIds;
    }

    public void setAuthorityIds(String[] authorityIds) {
        this.authorityIds = authorityIds;
    }

    public String[] getAuthorityNames() {
        return authorityNames;
    }

    public void setAuthorityNames(String[] authorityNames) {
        this.authorityNames = authorityNames;
    }
    public String[] getAuthorityDescription() {
        return authorityDescription;
    }

    public void setAuthorityDescription(String[] authorityDescription) {
        this.authorityDescription = authorityDescription;
    }

    public String[] getAuthorityModuleName() {
        return authorityModuleName;
    }

    public void setAuthorityModuleName(String[] authorityModuleName) {
        this.authorityModuleName = authorityModuleName;
    }

}
