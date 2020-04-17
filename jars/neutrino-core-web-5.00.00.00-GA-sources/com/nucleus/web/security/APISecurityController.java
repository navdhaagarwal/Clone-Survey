package com.nucleus.web.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.nucleus.api.security.APISecurityService;
import com.nucleus.core.initialization.ProductInformationLoader;

@RestController
@RequestMapping("/externalService")
public class APISecurityController {

	@Inject
	@Named("apiSecurityService")
	APISecurityService apiSecurityService;
	
	@Value("${core.web.config.proxy.cookie.secure}")
	private String secureCookie;

	@GetMapping("/getProxyCookie")
	public void createCookie(@RequestParam(value = "proxyKey") String proxyId, HttpServletResponse response) {

		if (StringUtils.isEmpty(proxyId)) {
			return;
		}

		String cookieId = apiSecurityService.getCookieIdBasedOnProxyId(proxyId);

		if (StringUtils.isEmpty(cookieId)) {
			return;
		}

		Cookie cookie = new Cookie(ProductInformationLoader.getProductCode()+"_SECURITY", cookieId);
		cookie.setHttpOnly(true);
		cookie.setSecure(isCookieSecure());
		cookie.setPath("/");
		cookie.setMaxAge(86400);
		response.addCookie(cookie);

		apiSecurityService.deleteProxyKeyFromCache(proxyId);
	}
	
	private boolean isCookieSecure() {
		return "true".equalsIgnoreCase(secureCookie);
	}

}
