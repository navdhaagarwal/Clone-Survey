package com.nucleus.sso;

import java.util.List;

import com.nucleus.user.OrgBranchInfo;

public class SsoUserInfo {
	
    private List<OrgBranchInfo> approvedAndActiveUserBranchList;
    private OrgBranchInfo loggedInBranch;
    
	public List<OrgBranchInfo> getApprovedAndActiveUserBranchList() {
		return approvedAndActiveUserBranchList;
	}
	public void setApprovedAndActiveUserBranchList(List<OrgBranchInfo> approvedAndActiveUserBranchList) {
		this.approvedAndActiveUserBranchList = approvedAndActiveUserBranchList;
	}
	public OrgBranchInfo getLoggedInBranch() {
		return loggedInBranch;
	}
	public void setLoggedInBranch(OrgBranchInfo loggedInBranch) {
		this.loggedInBranch = loggedInBranch;
	}
    

}
