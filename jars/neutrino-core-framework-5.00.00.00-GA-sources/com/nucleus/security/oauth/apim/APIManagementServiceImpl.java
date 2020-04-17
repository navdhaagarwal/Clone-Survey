package com.nucleus.security.oauth.apim;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.license.pojo.LicenseApiDetailsVO;
import com.nucleus.persistence.EntityDao;
import com.nucleus.persistence.HibernateUtils;


@Named("apiManagementService")
public class APIManagementServiceImpl implements APIManagementService{

	
	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	
	@Override
    public List<APIDetails> getAPIDetailsFromCodes(List<String> codes) {
		
		  NamedQueryExecutor<APIDetails> apiExecutor = new NamedQueryExecutor<APIDetails>("apiDetails.loadAPIDetailsbyAPICode")
	                .addParameter("listOfCodes", codes);
		  List<APIDetails> apis =  entityDao.executeQuery(apiExecutor);
		  
		  for(APIDetails api : apis){
			  HibernateUtils.initializeAndUnproxy(api.getPolicies());
			  for(ThrottlingPolicy policy : api.getPolicies()){
				  HibernateUtils.initializeAndUnproxy(policy.getTimeUnit());
			  }
		  }
		  return apis;
	}

	 public List<APIDetails> getAllFunctioningAPIs(){		 
		 return entityDao.findAll(APIDetails.class);
	 }
	
	 
		@Override
	    public List<APIDetails> getAPIDetailsFromLicense(List<LicenseApiDetailsVO> apisFromlicense) {
			if(apisFromlicense == null) {
				return null;
			}
			List<String> codes = apisFromlicense.stream().map(LicenseApiDetailsVO::getApiCode).collect(Collectors.toList());
			
			  NamedQueryExecutor<APIDetails> apiExecutor = new NamedQueryExecutor<APIDetails>("apiDetails.loadAPIDetailsbyAPICode")
		                .addParameter("listOfCodes", codes);
			  List<APIDetails> apis =  entityDao.executeQuery(apiExecutor);
			  
			  for(APIDetails api : apis){
				  HibernateUtils.initializeAndUnproxy(api.getPolicies());
				  for(ThrottlingPolicy policy : api.getPolicies()){
					  HibernateUtils.initializeAndUnproxy(policy.getTimeUnit());
				  }
			  }
			  return apis;
		}

	
}
