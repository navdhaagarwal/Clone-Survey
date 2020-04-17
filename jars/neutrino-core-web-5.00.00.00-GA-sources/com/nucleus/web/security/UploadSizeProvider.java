package com.nucleus.web.security;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by gajendra.jatav on 9/15/2019.
 */
public interface UploadSizeProvider {

    public long getMaxAllowedSize(HttpServletRequest httpServletRequest);
}
