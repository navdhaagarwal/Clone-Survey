package com.nucleus.sso.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.nucleus.sso.BannerStorageVO;
import com.nucleus.user.OrgBranchInfo;

public interface NeutrinoCasRestAuthenticationService {

	public OrgBranchInfo setLoggedinBranch(Long branchId, Boolean allBranchesFlag, String username);

	public Long getActiveBannerCount();

	public String uploadBanner(MultipartFile file, String imageTitle, String imageCaption) throws IOException;

	public List<BannerStorageVO> findAllBanners();

	public void deleteImage(String imageId);
	
	String checkValidFederatedUser(String username);

}