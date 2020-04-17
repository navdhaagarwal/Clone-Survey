package com.nucleus.web.csrf;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

/**

 * 
 * @author Nucleus Software.
 */

/*
 * This class is made public so that its method can be accessed for retrieving
 * the token from the session
 */
public final class CSRFTokenManager implements CsrfTokenRepository{

    /**
     * The token parameter name
     */
    static final String         CSRF_PARAM_NAME                  = "CSRFToken";
    static final String         CSRF_HEADER_NAME                  = "CSRFToken";

    /**
     * The location on the session which stores the token
     */
    private final static String CSRF_TOKEN_FOR_SESSION_ATTR_NAME = CSRFTokenManager.class.getName() + ".tokenval";

    static public String getTokenForSession(HttpServletRequest request) {
    	
        return createToken(request).getToken();
    }

    /**
     * Extracts the token value from the session
     * 
     * @param request
     * @return
     */
    static String getTokenFromRequest(HttpServletRequest request) {

        if (request.getParameter(CSRF_PARAM_NAME) != null) {
            return request.getParameter(CSRF_PARAM_NAME);
        } else if (request.getAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME) != null) {
            return request.getAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME).toString();
        } else {
            return request.getHeader(CSRF_PARAM_NAME);
        }
    }

    public CSRFTokenManager() {
    }

	@Override
	public CsrfToken generateToken(HttpServletRequest request) {
		return createToken(request);
	}
	private static NeutrinoCsrfToken createToken(HttpServletRequest request){
		NeutrinoCsrfToken tokenObject = null;
        HttpSession session = request.getSession();
        // current synchronization on session, a better implementation to
        // synchronize on an attribute in session.
        synchronized (session) {
            tokenObject = (NeutrinoCsrfToken) session.getAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME);            
            if (null == tokenObject) {
                tokenObject = new NeutrinoCsrfToken(CSRF_HEADER_NAME, CSRF_PARAM_NAME,
        				UUID.randomUUID().toString());
                session.setAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME, tokenObject);
                session.setAttribute("CSRF_TOKEN_FOR_SESSION_ATTR_NAME", tokenObject.getToken());
                request.setAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME, tokenObject.getToken());
            }
        }

        return tokenObject;
	}
	@Override
	public void saveToken(CsrfToken token, HttpServletRequest request,
			HttpServletResponse response) {
		if (token == null) {
			HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME) != null) {
				session.removeAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME);
			}
		}
		else {
			HttpSession session = request.getSession();
            CsrfToken currToken = (CsrfToken) session.getAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME);
            if (currToken == null || !currToken.equals(token)){
                session.setAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME, token);
            }
		}		
	}

	@Override
	public CsrfToken loadToken(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}
		return (CsrfToken) session.getAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME);
	};
}