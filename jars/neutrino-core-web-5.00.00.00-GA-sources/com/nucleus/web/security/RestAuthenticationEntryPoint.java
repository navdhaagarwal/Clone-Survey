package com.nucleus.web.security;

import java.io.IOException;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

@Named("restAuthenticationEntryPoint")
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence( HttpServletRequest request, HttpServletResponse response, 
     AuthenticationException authException ) throws IOException{
       response.sendError( HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized" );
    }
}


