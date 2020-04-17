package com.nucleus.web.apimgmt.filter;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.nucleus.security.oauth.dao.TrustedSourceDao;
import com.nucleus.user.IPAddressRange;
import com.nucleus.web.apimgmt.exception.IPNotInRangeException;

public class IPCheckAndWhitelistFilter  implements Filter{

	@Inject
	@Named("trustedSourceDao")
	private TrustedSourceDao trustedSourceDao;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//Required implementation : Hence empty.
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
	/*	String ipAddress = getIpAddressFromClient(request);
		String clientId = (String)request.getAttribute("clientId");
		
		if(clientId == null || "".equals(clientId)){
			throw new AccessDeniedException("Client ID Not Found");
			
		}
		if(ipAddress == null){
			BaseLoggers.apiManagementLogger.info("Cannot find IP for request from client : " + clientId);
			chain.doFilter(request, response);
			return;
			
		}
		OauthClientDetails details = (OauthClientDetails) request.getAttribute("clientDetails");
		List<IPAddressRange> ipRangeList = details.getIpAddresses(); 
		if(ipRangeList.isEmpty()){
			BaseLoggers.apiManagementLogger.info("No  : " + clientId);
			chain.doFilter(request, response);
			return;
		}
		
		
		boolean isAllowedFlag = this.checkIfIpInRange(ipRangeList, ipAddress);

		if(isAllowedFlag){
			chain.doFilter(request, response);
			return;
			
		}
		
		throw new IPNotInRangeException("API Not allowed access for the IP : " + ipAddress);
		*/
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		//Required implementation : Hence empty.
	}

	private int convertIptoNumber(String ipAddress) { // NOSONAR
		String[] ipAddressParams = ipAddress.split("\\.");
		if(ipAddressParams.length != 4){
			return 0;
		}
		int fourthIPLayer = Integer.parseInt(ipAddressParams[3].replace("_", ""));
		int thirdIPLayer  = Integer.parseInt(ipAddressParams[2].replace("_", ""));
		int secondIPLayer = Integer.parseInt(ipAddressParams[1].replace("_", ""));
		int firstIPLayer = Integer.parseInt(ipAddressParams[0].replace("_", ""));

		return firstIPLayer*1000000000 + secondIPLayer*1000000 + thirdIPLayer*1000 + fourthIPLayer;
	}



	private String getIpAddressFromClient(ServletRequest request) { // NOSONAR
		HttpServletRequest httpReq = (HttpServletRequest)request;
		String xForwardedForHeader = httpReq.getHeader("X-Forwarded-For");
		if (xForwardedForHeader == null) {
	        return request.getRemoteAddr();
	    } else {
	        return new StringTokenizer(xForwardedForHeader, ",").nextToken().trim();
	    }
	}
	
	private boolean checkIfIpInRange(List<IPAddressRange> ipRangeList, String ipAddress){ // NOSONAR
		boolean isAllowedFlag = false;
		for(IPAddressRange range : ipRangeList){
			
			if(range.getToIpAddress() == null){
				if(range.getFromIpAddress().equals(ipAddress)){
					isAllowedFlag = true;
					break;
					
				}
				else{
					throw new IPNotInRangeException("Invalid IP For Request");
				}
			} 

			else{
				int ipFromNumber = convertIptoNumber(range.getFromIpAddress());
				int ipToNumber = convertIptoNumber(range.getToIpAddress());
				
				int ipFromReq = convertIptoNumber(ipAddress);
				if(ipFromReq == 0){
					
					throw new IPNotInRangeException("Invalid IP For Request");
				}
				if(ipFromNumber < ipFromReq  || ipToNumber > ipFromReq){
					isAllowedFlag = true;
					break;
				}		
			}
			
		}
		return isAllowedFlag;
		
	}
	
	
}
