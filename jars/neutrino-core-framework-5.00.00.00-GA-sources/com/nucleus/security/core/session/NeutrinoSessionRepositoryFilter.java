package com.nucleus.security.core.session;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.logging.BaseLoggers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NeutrinoSessionRepositoryFilter<S extends Session> extends SessionRepositoryFilter<S> {

    private static final String LOAD_BALANCER_HEALTH_CHECK_URI = "loadBalancerHealthCheck";

    @Value("${session.failover.enabled:false}")
    private boolean isSessionFailoverEnabled;

    @Value("${session.failover.cookie.suffix:ROUTE}")
    private String stickySessionCookieSuffix;

    @Inject
    @Named("coreUtility")
    private CoreUtility coreUtility;

    @Value("${session.failover.route.id:DUMMY}")
    private String stickySessionRouteId;



    private Cookie cookie;


    public NeutrinoSessionRepositoryFilter(SessionRepository sessionRepository) {
        super(sessionRepository);
    }

    @PostConstruct
    public void init() {
        if (stickySessionRouteId.equals("DUMMY")) {
            try {
                String completeIpAddress = InetAddress.getLocalHost().getHostAddress();
                String[] ipFields = completeIpAddress.split("\\.");
                stickySessionRouteId = ipFields[ipFields.length - 1];
                BaseLoggers.flowLogger.info("IP Address Bound to application :" + completeIpAddress);
            } catch (UnknownHostException e) {
                BaseLoggers.exceptionLogger.error("Server IP could not be traced", e);
                throw new SystemException(e);
            }
        }
        BaseLoggers.flowLogger.info("Route Id to be configured in LB :" + stickySessionRouteId);
        cookie = new Cookie(ProductInformationLoader.getProductCode() + "_" + stickySessionCookieSuffix, "." + stickySessionRouteId);
        cookie.setPath("/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith(LOAD_BALANCER_HEALTH_CHECK_URI)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        } else if (isSessionFailoverEnabled) {
            response.addCookie(cookie);
            super.doFilterInternal(request, response, filterChain);
        } else {
            filterChain.doFilter(request, response);
        }
    }

}
