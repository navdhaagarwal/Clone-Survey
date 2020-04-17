package com.nucleus.sso;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * 
 * This class is for testing Ajax calls to generate Session Timeout on an authentication failure
 * 
 * 
 * @author namrata.varshney
 *
 */
@RestController
public class TestAjaxController {
	
	@RequestMapping(value = "/ssoTestingForAjax")
	@ResponseBody
	public String testAjaxForSso(HttpServletRequest request) {
		return "success";
	}

}
