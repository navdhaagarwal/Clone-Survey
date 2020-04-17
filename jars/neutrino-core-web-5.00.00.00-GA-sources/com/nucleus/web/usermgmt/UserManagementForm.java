package com.nucleus.web.usermgmt;

import java.util.List;

import javax.persistence.Transient;

import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.user.User;
import com.nucleus.user.UserDefaultUrlMappingVO;
import com.nucleus.user.UserMobilityInfo;
import com.nucleus.user.UserProfile;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
public class UserManagementForm {

    public User                  associatedUser;

    public UserProfile           userprofile;

    private Long[]               teamIds;

    private Long[]               roleIds;

    public String                productList;

    public List<ConfigurationVO> configVOList;

    public Long                  mappedBPid;

    public List<String>          myFavs;

    public Long                  defaultBranch;

    @Transient
    public Long[]                adminOfBranches;

    public boolean               isUserTeamLead;
    
    public UserMobilityInfo      userMobilityInfo;

    public List<UserDefaultUrlMappingVO> userDefaultUrlMappingVOList;

    public List<Long>              deletedUserUrlMappings;



    public UserMobilityInfo getUserMobilityInfo() {
        return userMobilityInfo;
    }

    public void setUserMobilityInfo(UserMobilityInfo userMobilityInfo) {
        this.userMobilityInfo = userMobilityInfo;
    }

    public boolean isUserTeamLead() {
        return isUserTeamLead;
    }

    public void setUserTeamLead(boolean isUserTeamLead) {
        this.isUserTeamLead = isUserTeamLead;
    }

    public Long getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(Long defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public List<String> getMyFavs() {
        return myFavs;
    }

    public void setMyFavs(List<String> myFavs) {
        this.myFavs = myFavs;
    }

    /*   public List<LoanProduct> loanProductList;*/

    public User getAssociatedUser() {
        return associatedUser;
    }

    public void setAssociatedUser(User associatedUser) {
        this.associatedUser = associatedUser;
    }

    /* public List<LoanProduct> getLoanProductList() {
         return loanProductList;
     }

     public void setLoanProductList(List<LoanProduct> loanProductList) {
         this.loanProductList = loanProductList;
     }*/

    public UserProfile getUserprofile() {
        return userprofile;
    }

    public void setUserprofile(UserProfile userprofile) {
        this.userprofile = userprofile;
    }

    public Long getMappedBPid() {
        return mappedBPid;
    }

    public void setMappedBPid(Long mappedBPid) {
        this.mappedBPid = mappedBPid;
    }

    public List<ConfigurationVO> getConfigVOList() {
        return configVOList;
    }

    public void setConfigVOList(List<ConfigurationVO> configVOList) {
        this.configVOList = configVOList;
    }

    public void setTeamIds(Long[] longs) {
        this.teamIds = longs;
    }

    public Long[] getTeamIds() {
        return teamIds;
    }

    public Long[] getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Long[] roleIds) {
        this.roleIds = roleIds;
    }

    public String getProductList() {
        return productList;
    }

    public void setProductList(String productList) {
        this.productList = productList;
    }

    public Long[] getAdminOfBranches() {
        return adminOfBranches;
    }

    public void setAdminOfBranches(Long[] adminOfBranches) {
        this.adminOfBranches = adminOfBranches;
    }

    public List<UserDefaultUrlMappingVO> getUserDefaultUrlMappingVOList() {
        return userDefaultUrlMappingVOList;
    }

    public void setUserDefaultUrlMappingVOList(List<UserDefaultUrlMappingVO> userDefaultUrlMappingVOList) {
        this.userDefaultUrlMappingVOList = userDefaultUrlMappingVOList;
    }

    public List<Long> getDeletedUserUrlMappings() {
        return deletedUserUrlMappings;
    }

    public void setDeletedUserUrlMappings(List<Long> deletedUserUrlMappings) {
        this.deletedUserUrlMappings = deletedUserUrlMappings;
    }
}
