package com.nucleus.infinispan.console.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.infinispan.manager.DefaultCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.infinispan.console.custom.NeutrinoDefaultCacheManager;
import com.nucleus.infinispan.console.entity.ClusterNodeAddresses;
import com.nucleus.infinispan.console.entity.ClusterStatus;
import com.nucleus.infinispan.console.event.ClusterEventVo;
import com.nucleus.infinispan.console.security.DevOpsUser;
import com.nucleus.infinispan.console.security.DevOpsUserManager;
import com.nucleus.infinispan.console.service.ClusterConsoleService;
import com.nucleus.infinispan.console.service.ClusterConsoleServiceImpl;
import com.nucleus.logging.BaseLoggers;

/**
 * 
 * @author gajendra.jatav
 *
 */
@RequestMapping("/clusterConsole")
@Controller
public class ClusterConsoleHandler {

	private static final String DONE = "done";

	private static String credentialsFilePath;

	@Named("devOpsUserManager")
	@Inject
	private DevOpsUserManager devOpsUserManager;
	
	@Named("clusterConsoleService")
	@Inject
	private ClusterConsoleService clusterConsoleService;

	public String getCredentialsFilePath() {
		return credentialsFilePath;
	}

	@RequestMapping(value = "/getClusterStatus")
    @ResponseBody
	public ClusterStatus getClusterStatus()  {
		DefaultCacheManager cacheManager = NeutrinoDefaultCacheManager.getAvailableCacheManager().iterator().next();
		return clusterConsoleService.getClusterStatus(cacheManager);
	}



	@RequestMapping(value = "/generateCredentials")
    @ResponseBody
	public String generateCredentials(HttpServletRequest request){
		Properties properties = new Properties();
		String password = UUID.randomUUID().toString();
		DevOpsUser devOpsUser = new DevOpsUser();
		devOpsUser.setPassword(password);
		devOpsUser.setAuthority("DEV_ADMIN");
		devOpsUser.setUsername("devopsadmin");
		devOpsUserManager.setDevOpsUser(devOpsUser);

		properties.put("security-code",password);
		properties.put("user", "devopsadmin");
		String outFile = getOutFileName();
		File file = new File(outFile);
		
		try(FileOutputStream fileOutputStream=new FileOutputStream(file)) {
			properties.store(fileOutputStream, "");
			fileOutputStream.close();
		} catch ( IOException e) {
			BaseLoggers.flowLogger.error("Error in generateCredentials {}",e);
		}
		return "Created at "+request.getLocalAddr()+":"+request.getLocalPort();
	}

	@RequestMapping(value = "/base")
	public String getHomePage(){
		return "clusterConsole";
	}

	@RequestMapping(value = "/addNodeAddress")
	@ResponseBody
	public String saveClusterNodeAddresses(@RequestParam("nodeAddress") String nodeAddress){
		clusterConsoleService.addNodeAddress(nodeAddress);
		return DONE;
	}

	@RequestMapping(value = "/deleteNodeAddress")
	@ResponseBody
	public String deleteNodeAddress(@RequestParam("nodeAddress") String nodeAddress){
		clusterConsoleService.deleteNodeAddress(nodeAddress);
		return DONE;
	}

	
	@RequestMapping(value = "/getClusterNodeAddresses")
	@ResponseBody
	@Transactional
	public List<String> getClusterNodeAddresses(){
		List<String> nodeAddressesList=new ArrayList<>();
		ClusterNodeAddresses addresses=clusterConsoleService.fetchNodeAddressesEntity();
		if(addresses!=null && addresses.getNodeAddresses()!=null){
			nodeAddressesList.addAll(addresses.getNodeAddresses());
		}
		return nodeAddressesList;
	}

	@RequestMapping(value = "/getClusterStatusOnNode")
	@ResponseBody
	public ClusterStatus getClusterStatusOnNode(@RequestParam("nodeAddress") String nodeAddress){
		
		return clusterConsoleService.fetchRemoteNodeClusterStatus(nodeAddress);
	}

	@RequestMapping(value = "/getClusterStatusRemotely")
	@ResponseBody
	public ClusterStatus getClusterStatusRemotely(@RequestParam(ClusterConsoleServiceImpl.COMM_TOKEN) String communicationToken){
		if(clusterConsoleService.validToken(communicationToken)){
			DefaultCacheManager cacheManager = NeutrinoDefaultCacheManager.getAvailableCacheManager().iterator().next();
			
			return clusterConsoleService.getClusterStatus(cacheManager);
		}else{
			BaseLoggers.flowLogger.error("Communication token now valid ");
			return null;
		}
	}


	@RequestMapping(value = "/evictHibernateCache")
	@ResponseBody
	public String evictHibernateCache(){
		clusterConsoleService.evictHibernateCache();
		return DONE;
	}
	
	@RequestMapping(value = "/evictHibernateCacheRemotely")
	@ResponseBody
	public String evictHibernateCacheRemotely(
			@RequestParam(ClusterConsoleServiceImpl.COMM_TOKEN) String communicationToken) {
		if(clusterConsoleService.validToken(communicationToken)){
			clusterConsoleService.evictHibernateCache();
			BaseLoggers.flowLogger.error("Clearing l2 cache ");
		}else{
			BaseLoggers.flowLogger.error(" evictHibernateCacheRemotely Communication token now valid ");
		}
		return DONE;
	}

	
	@RequestMapping(value = "/evictHibernateCacheOnNode")
	@ResponseBody
	public String evictHibernateCacheOnNode(@RequestParam("nodeAddress") String nodeAddress){
		clusterConsoleService.evictHibernateCache(nodeAddress);
		return DONE;
	}

	@RequestMapping(value = "/markCacheActive")
	@ResponseBody
	public String markCacheActive(){
		DefaultCacheManager cacheManager = NeutrinoDefaultCacheManager.getAvailableCacheManager().iterator().next();
		clusterConsoleService.markCacheActive(cacheManager);
		return DONE;
	}
	
	@RequestMapping(value = "/markCacheActiveRemotely")
	@ResponseBody
	public String markCacheActiveRemotely(
			@RequestParam(ClusterConsoleServiceImpl.COMM_TOKEN) String communicationToken) {
		if(clusterConsoleService.validToken(communicationToken)){
			DefaultCacheManager cacheManager = NeutrinoDefaultCacheManager.getAvailableCacheManager().iterator().next();
			clusterConsoleService.markCacheActive(cacheManager);
			BaseLoggers.flowLogger.error("marking active l2 cache ");
		}else{
			BaseLoggers.flowLogger.error(" evictHibernateCacheRemotely Communication token now valid ");
		}
		return DONE;
	}

	
	@RequestMapping(value = "/markCacheActiveOnNode")
	@ResponseBody
	public String markCacheActiveOnNode(@RequestParam("nodeAddress") String nodeAddress){
		DefaultCacheManager cacheManager = NeutrinoDefaultCacheManager.getAvailableCacheManager().iterator().next();
		clusterConsoleService.markCacheActive(cacheManager,nodeAddress);
		return DONE;
	}


	private String getOutFileName() {

		return this.credentialsFilePath + File.separator + "DevOpsUserCredentials.txt";
	}



	@Value(value = "#{'${security.DevOpsUser.credentialsFilePath}'}")
	public void setCredentialsFilePath(String credentialsFilePath) {
		if (StringUtils.isEmpty(credentialsFilePath)
				|| "${security.DevOpsUser.credentialsFilePath}".equalsIgnoreCase(credentialsFilePath)) {
			this.credentialsFilePath = System.getProperty("java.io.tmpdir");
			return;
		}
		this.credentialsFilePath = credentialsFilePath;

	}
	
	
	@RequestMapping(value = "/getClusterEventsList")
	@ResponseBody
	public List<ClusterEventVo> getClusterEventsList(){
		return clusterConsoleService.getClusterEventsList();
	}

	@RequestMapping(value = "/getClusterEventsListOnNode")
	@ResponseBody
	public List<ClusterEventVo> getClusterEventsListOnNode(@RequestParam("nodeAddress") String nodeAddress){
		return clusterConsoleService.fetchRemoteNodeClusterEventsList(nodeAddress);
	}

	@RequestMapping(value = "/getClusterEventsListRemotely")
	@ResponseBody
	public List<ClusterEventVo> getClusterEventsListRemotely(@RequestParam(ClusterConsoleServiceImpl.COMM_TOKEN) String communicationToken){
		if(clusterConsoleService.validToken(communicationToken)){
			return clusterConsoleService.getClusterEventsList();
		}else{
			BaseLoggers.flowLogger.error("Communication token now valid ");
			return null;
		}
	}


}
