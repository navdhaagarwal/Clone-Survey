package com.nucleus.web.csrf;

import org.springframework.security.web.csrf.CsrfToken;

import java.util.Objects;

public class NeutrinoCsrfToken implements CsrfToken{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String headerName;
	
	private String parameterName;
	
	private String token;

	public NeutrinoCsrfToken(String csrfHeaderName, String csrfParamName,
			String tokenForSession) {
		this.headerName=csrfHeaderName;
		this.parameterName=csrfParamName;
		this.token=tokenForSession;
	}

	@Override
	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	@Override
	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	@Override
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NeutrinoCsrfToken that = (NeutrinoCsrfToken) o;
		return Objects.equals(token, that.token) && Objects.equals(headerName, that.headerName) &&
				Objects.equals(parameterName, that.parameterName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(headerName, parameterName, token);
	}
}
