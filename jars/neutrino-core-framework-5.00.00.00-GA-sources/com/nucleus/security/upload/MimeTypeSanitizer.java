package com.nucleus.security.upload;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * Created by gajendra.jatav on 10/18/2019.
 */
public interface MimeTypeSanitizer {

    public void sanitize(CommonsMultipartFile multipartFile);
}
