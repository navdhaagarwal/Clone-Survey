/**
 * 
 */
package com.nucleus.web.filters;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.accesslog.entity.AccessLog;
import com.nucleus.core.accesslog.entity.DynamicURILogicalNameMapper;
import com.nucleus.core.accesslog.entity.WebUriRepositoryPopulator;
import com.nucleus.core.accesslog.service.AccessLogService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;

public class WebAccessLogFilter implements Filter {
	private AccessLogService accessLogService;
	private boolean accessLogEnabled = true;
	private String moduleCode;
	private String serverIp = null;
	private HashMap<String, Object> webUriRepositoryMap = new HashMap<>();
	private final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";

	private DynamicURILogicalNameMapper dynamicURILogicalNameMapper;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws ServletException, IOException {

		if (!accessLogEnabled) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpSession session = request.getSession(false);

		boolean isUserAuthenticated = isUserAuthenticated(session);
		if (!isUserAuthenticated) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		AccessLog accessLog = new AccessLog();
		accessLog.setRequestDateTime(LocalDateTime.now());
		String uri = request.getServletPath();
		if (request.getPathInfo() != null) {
			uri = uri.concat(request.getPathInfo());
		}

		String logicalFunctionNameId = null;

		if (webUriRepositoryMap.containsKey(uri)) {
			logicalFunctionNameId = String.valueOf(webUriRepositoryMap.get(uri));
		} else {
			logicalFunctionNameId = searchLogicalFunctionNameIdForUri(uri);
		}

		accessLog.setWebUriRepository(logicalFunctionNameId);
		accessLog.setUri(uri);

		accessLog.setQueryString(request.getQueryString());
		accessLog.setMethod(request.getMethod());
		ResponseCodeRevealingResponseWrapper responseWrapper = new ResponseCodeRevealingResponseWrapper(response,
				accessLog);

		try {
			filterChain.doFilter(request, responseWrapper);
		} finally {
			String authenticatedUserName = null;

			if (session != null) {
				accessLog.setSessionId(session.getId());
				authenticatedUserName = (String) session.getAttribute("authenticatedUser");
			}

			String remoteIP = null;
			remoteIP = request.getHeader("x-forwarded-for");
			if (remoteIP == null || "".equals(remoteIP)) {
				remoteIP = request.getHeader("x-forwarded-by");
			}
			if (remoteIP == null || "".equals(remoteIP)) {
				remoteIP = request.getRemoteHost();
			}
			accessLog.setRemotehost(remoteIP);
			accessLog.setUserName(authenticatedUserName);
			accessLog.setServerIp(serverIp);
			accessLog.setModule(moduleCode);

		}
	}

	private String searchLogicalFunctionNameIdForUri(String uri) {
		return dynamicURILogicalNameMapper.getUriFunctionNameId(uri);
	}

	/**
	 * Initialize AccessLog service and whether it is enabled or disabled.
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		accessLogService = NeutrinoSpringAppContextUtil.getBeanByName("accessLogService", AccessLogService.class);
		dynamicURILogicalNameMapper = NeutrinoSpringAppContextUtil.getBeanByName("dynamicURILogicalNameMapper",
				DynamicURILogicalNameMapper.class);

		WebUriRepositoryPopulator uriRepositoryPopulator = NeutrinoSpringAppContextUtil
				.getBeanByName("webUriRepositoryPopulator", WebUriRepositoryPopulator.class);
		webUriRepositoryMap = uriRepositoryPopulator.getWebUriRepositoryMap();

		accessLogEnabled = accessLogService.isAccessLogEnabled();
		moduleCode = ProductInformationLoader.getProductCode();
		try {
			serverIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			BaseLoggers.exceptionLogger.error("Server IP could not be traced", e);
		}

	}

	@Override
	public void destroy() {
		// do not do anything

	}

	private class ResponseCodeRevealingResponseWrapper extends HttpServletResponseWrapper {

		private int statusCode;
		private AccessLog accessLog;
		private ServletOutputStream servletOutputStream;
		private boolean statusCodeUpdateCaptured;

		public ResponseCodeRevealingResponseWrapper(HttpServletResponse response, AccessLog accessLog) {
			super(response);
			this.accessLog = accessLog;
		}

		/**
		 * The default behavior of this method is to call setStatus(int sc, String sm)
		 * on the wrapped response object.
		 */
		@Override
		public void setStatus(int sc, String sm) {
			statusCode = sc;
			super.setStatus(sc, sm);

			logResponseCodeForAccessLog();
		}

		@Override
		public void setStatus(int sc) {
			statusCode = sc;
			super.setStatus(sc);
			logResponseCodeForAccessLog();
		}

		/**
		 * The default behavior of this method is to call sendError(int sc) on the
		 * wrapped response object.
		 */
		@Override
		public void sendError(int sc) throws IOException {
			statusCode = sc;
			super.sendError(sc);
			logResponseCodeForAccessLog();
		}

		@Override
		public final void sendError(int sc, String msg) throws IOException {
			statusCode = sc;
			super.sendError(sc, msg);
			logResponseCodeForAccessLog();
		}

		@Override
		public final void sendRedirect(String location) throws IOException {
			statusCode = HttpServletResponse.SC_MOVED_TEMPORARILY;
			super.sendRedirect(location);
			logResponseCodeForAccessLog();
		}

		public int getStatusCode() {
			return statusCode;
		}

		/**
		 * The default behavior of this method is to return getOutputStream() on the
		 * wrapped response object.
		 */
		public ServletOutputStream getOutputStream() throws IOException {
			if (servletOutputStream == null) {
				servletOutputStream = new ResponseCodeRevealingServletOutputStream(super.getOutputStream());
			}
			return servletOutputStream;
		}

		private void logResponseCodeForAccessLog() {

			if (statusCodeUpdateCaptured) {
				return;// status code already updated
			}

			if (statusCode == 0) {
				statusCode = HttpServletResponse.SC_OK;
			}
			accessLog.setStatusCode(statusCode);
			accessLogService.createAccessLog(accessLog);
			statusCodeUpdateCaptured = true;
		}

		private class ResponseCodeRevealingServletOutputStream extends ServletOutputStream {

			ServletOutputStream localServletOutputStream;

			public ResponseCodeRevealingServletOutputStream(ServletOutputStream servletOutputStream) {
				localServletOutputStream = servletOutputStream;
			}

			@Override
			public void print(String s) throws IOException {
				localServletOutputStream.print(s);
			}

			@Override
			public void print(boolean b) throws IOException {
				localServletOutputStream.print(b);
			}

			@Override
			public void print(char c) throws IOException {
				localServletOutputStream.print(c);
			}

			@Override
			public void print(int i) throws IOException {
				localServletOutputStream.print(i);
			}

			@Override
			public void print(long l) throws IOException {
				localServletOutputStream.print(l);
			}

			@Override
			public void print(float f) throws IOException {
				localServletOutputStream.print(f);
			}

			@Override
			public void print(double d) throws IOException {
				localServletOutputStream.print(d);
			}

			@Override
			public void println() throws IOException {
				localServletOutputStream.println();
			}

			@Override
			public void println(String s) throws IOException {
				localServletOutputStream.println(s);
			}

			@Override
			public void println(boolean b) throws IOException {
				localServletOutputStream.println(b);
			}

			@Override
			public void println(char c) throws IOException {
				localServletOutputStream.println(c);
			}

			@Override
			public void println(int i) throws IOException {
				localServletOutputStream.println(i);
			}

			@Override
			public void println(long l) throws IOException {
				localServletOutputStream.println(l);
			}

			@Override
			public void println(float f) throws IOException {
				localServletOutputStream.println(f);
			}

			@Override
			public void println(double d) throws IOException {
				localServletOutputStream.println(d);
			}

			@Override
			public void write(int b) throws IOException {
				localServletOutputStream.write(b);

			}

			@Override
			public void write(byte[] b) throws IOException {
				localServletOutputStream.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				localServletOutputStream.write(b, off, len);
			}

			@Override
			public void flush() throws IOException {
				localServletOutputStream.flush();
				logResponseCodeForAccessLog();
			}

			@Override
			public void close() throws IOException {
				localServletOutputStream.close();
				logResponseCodeForAccessLog();
			}

			@Override
			public boolean isReady() {
				if (servletOutputStream != null) {
					return servletOutputStream.isReady();
				}
				return true;
			}

			@Override
			public void setWriteListener(WriteListener writeListener) {
				if (servletOutputStream != null) {
					servletOutputStream.setWriteListener(writeListener);
				}
			}

		}
	}

	private boolean isUserAuthenticated(HttpSession session) {

		boolean isUserAuthenticated = false;

		if (session != null) {
			if (session.getAttribute("isAuthenticated") != null) {
				return true;
			}

			if (session.getAttribute(SPRING_SECURITY_CONTEXT) != null) {
				Authentication authentication = ((SecurityContext) session.getAttribute(SPRING_SECURITY_CONTEXT))
						.getAuthentication();
				if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
					isUserAuthenticated = true;
					session.setAttribute("isAuthenticated", Boolean.valueOf(true));
					session.setAttribute("authenticatedUser", ((UserInfo) authentication.getPrincipal()).getUsername());
				}
			}
		}

		return isUserAuthenticated;
	}

}
