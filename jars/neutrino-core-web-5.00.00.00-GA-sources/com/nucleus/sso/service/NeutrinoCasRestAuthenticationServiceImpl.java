package com.nucleus.sso.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.datastore.service.AntiVirusStatus;
import com.nucleus.core.datastore.service.AntivirusScanService;
import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.sso.BannerStorageVO;
import com.nucleus.sso.imageupload.SsoBannerStorage;
import com.nucleus.user.OrgBranchInfo;
import com.nucleus.user.OutOfOfficeDetails;
import com.nucleus.user.User;
import com.nucleus.user.UserBranchProductService;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.user.UserStatus;
import com.nucleus.web.login.LoginConstants;

@Named("neutrinoCasRestAuthenticationServiceImpl")
public class NeutrinoCasRestAuthenticationServiceImpl implements NeutrinoCasRestAuthenticationService{
	
	private static final String ANTI_VIRUS_SCAN_SERVICE="antivirusScanService";
	
	private List<String> supportedTypes =Collections.unmodifiableList(Arrays.asList(
			"image/jpeg",
			"image/pjpeg", 
			"image/jpg",
			"image/png", 
			"image/gif",
			"image/bmp",
			"image/x-xbitmap",
			"image/jp_",
			"image/pipeg",
			"image/vnd.swiftview-jpeg",
			"image/gi_",
			"image/x-windows-bmp",
			"image/ms-bmp",
			"image/x-ms-bmp",
			"image/x-bitmap"));
	
	  @Inject
	  @Named("userBranchProductService")
	  private UserBranchProductService userBranchProductService;
	  
	  @Inject
	  @Named("baseMasterService")
	  private BaseMasterService baseMasterService;
	  
	  @Inject
	  @Named("userService")
	  private UserService userService;
	  
	    @Inject
		@Named("couchDataStoreDocumentService")
		private DatastorageService dataStorageService;
	    
	    @Inject
	    @Named("entityDao")
	    private EntityDao entityDao;
	    
		@Inject
		@Named("tika")
		private Tika tika;
	
		

	
	@Override
	public OrgBranchInfo setLoggedinBranch(Long branchId, Boolean allBranchesFlag, String username ){
		 UserInfo userInfo = userService.getUserFromUsername(username);
	      OrgBranchInfo orgBranchInfo = new OrgBranchInfo();
	      userInfo.setAllBranchesFlag(allBranchesFlag);
	      if (allBranchesFlag) {
	          orgBranchInfo.setOrgName("All Branches");
	          orgBranchInfo.setId(Long.valueOf(-1));
	      } else {
	          OrganizationBranch organizationBranch = baseMasterService
	                  .getMasterEntityById(OrganizationBranch.class, branchId);
	          orgBranchInfo.setId(branchId);
	          orgBranchInfo.setOrgName(organizationBranch.getName());
	      }
	      userInfo.setLoggedInBranch(orgBranchInfo);
	      userBranchProductService.updateUserInfoLoggedInBranchProducts(userInfo);
	

	  return userInfo.getLoggedInBranch();
	}
	

	@Override
	public Long getActiveBannerCount(){
		 String query = "select count(s) from SsoBannerStorage s where s.imageStatus= :imageStatus"; 
		 JPAQueryExecutor<Long> jPAQueryExecutor = new JPAQueryExecutor<>(query);
		 jPAQueryExecutor.addParameter("imageStatus",1);
		 List<Long> imgCount = entityDao.executeQuery(jPAQueryExecutor);
		 return imgCount.get(0);
	}
	

	@Override
	@Transactional
	public String uploadBanner(MultipartFile file, String imageTitle, String imageCaption) throws IOException{
		
		 String extension = FilenameUtils.getExtension(file.getOriginalFilename()); 
		 ByteArrayInputStream bais = new ByteArrayInputStream(file.getBytes());
		 String mimeType = tika.detect(bais);
		 if(!supportedTypes.contains(mimeType))
			 return "File Type not supported";
		 
		 AntivirusScanService antivirusScanService=NeutrinoSpringAppContextUtil.getBeanByName(ANTI_VIRUS_SCAN_SERVICE, AntivirusScanService.class);
		 AntiVirusStatus scanStatus = antivirusScanService.fileScanner(bais,file.getOriginalFilename());
		
		 if(scanStatus == AntiVirusStatus.FILE_CLEAN)
		 {
			 String id = dataStorageService.saveDocument(new ByteArrayInputStream(file.getBytes()), file.getOriginalFilename(), extension);
			 SsoBannerStorage image = new SsoBannerStorage() ;
			 image.setFileName(file.getOriginalFilename());
			 image.setStorageId(id);
			 image.setActive(1);
			 image.setImageTitle(imageTitle);
			 image.setImageCaption(imageCaption);
			 entityDao.persist(image);
			 return "Successfully Uploaded";
		 }else{
			 return "Malicious File upload for :"+ file.getOriginalFilename();
		 }
	}
	
	
	@Override
	public  List<BannerStorageVO> findAllBanners(){
		String query = "select s.storageId, s.fileName, s.imageTitle, s.imageCaption, s.id from SsoBannerStorage s where s.imageStatus = :imageStatus";
		JPAQueryExecutor<Object[]> jPAQueryExecutor = new JPAQueryExecutor<>(query);
		jPAQueryExecutor.addParameter("imageStatus",1);
		List<Object[]> imageList =  entityDao.executeQuery(jPAQueryExecutor);
		List<BannerStorageVO> storageVoList = new ArrayList<>(); 
		for(Object[] image : imageList){
			BannerStorageVO bannerStorageVO = new BannerStorageVO();
			bannerStorageVO.setSsoBannerStorageFields(image);
			byte[] imageFile = dataStorageService.retriveDocumentAsByteArray((String)image[0]);
			bannerStorageVO.setImageFile(imageFile);
			bannerStorageVO.setContentType(tika.detect(imageFile));
			storageVoList.add(bannerStorageVO);
		}
		return storageVoList;
	}


	@Override
	public void deleteImage(String imageId){
		 String query = "select s from SsoBannerStorage s where s.id = :imageId";
		  JPAQueryExecutor<SsoBannerStorage> jPAQueryExecutor = new JPAQueryExecutor<>(query);
		  jPAQueryExecutor.addParameter("imageId", Long.parseLong(imageId));
		  List<SsoBannerStorage> imagelist = entityDao.executeQuery(jPAQueryExecutor);
		  SsoBannerStorage image = imagelist.get(0);
		  image.setActive(0);
		  
		  entityDao.saveOrUpdate(image);
	 }


	@Override
	public String checkValidFederatedUser(String username) {
		User user = userService.findUserByUsername(username);
		
		if(user==null || user.getUserStatus()!=UserStatus.STATUS_ACTIVE || !"federated".equals(user.getSourceSystem())) {
			return "invalidUser";
		}
		
		if (!user.isAccountNonLocked()) {
			return "userLocked";
		}
		
		if (!user.isEnabled()) {
			return "userDisabled";
		}
		
		if(!user.isLoginEnabled()) {
			return LoginConstants.USER_NON_LOGIN_CHECK;
		}
		
		
		OutOfOfficeDetails outOfOffice = user.getOutOfOfficeDetails();
		if (outOfOffice != null && outOfOffice.isOutOfOffice()) {
			BaseLoggers.exceptionLogger.error("User out of office : logging user out");
			return "outOfOfficeException";
		}
		
		return "none";
	}

	  
}
