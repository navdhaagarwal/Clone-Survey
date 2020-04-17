package com.nucleus.web.security;

import com.nucleus.core.security.entities.UriUploadLimitConfiguration;
import com.nucleus.core.security.service.UriUploadLimitDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gajendra.jatav on 9/15/2019.
 */
@Named("uploadSizeProvider")
public class UploadSizeProviderImpl implements UploadSizeProvider{

    private Map<String,Long> uriMaxUploadLimitMap;

    @Value("${core.web.config.commonsMultipartResolver.maxUploadSize.value}")
    private Long globalUploadLimit;

    @Named("uriUploadLimitDao")
    @Inject
    private UriUploadLimitDao uriUploadLimitDao;

    @Override
    public long getMaxAllowedSize(HttpServletRequest httpServletRequest) {
        String requestUri = httpServletRequest.getRequestURI();
        for (Map.Entry<String,Long> uriUploadLimit:uriMaxUploadLimitMap.entrySet()){
            if(requestUri.contains(uriUploadLimit.getKey())){
                return uriUploadLimit.getValue();
            }
        }
        return globalUploadLimit;
    }

    @Transactional
    @PostConstruct
    public void init(){
        uriMaxUploadLimitMap = new HashMap<>();
        List<UriUploadLimitConfiguration> uriUploadLimitConfigurations =
                uriUploadLimitDao.getAllUploadLimitConfiguration();
        if (uriUploadLimitConfigurations!=null && !uriUploadLimitConfigurations.isEmpty()){
            uriUploadLimitConfigurations.forEach(uriUploadLimitConfiguration ->
                    uriMaxUploadLimitMap.put(uriUploadLimitConfiguration.getUri(),
                            uriUploadLimitConfiguration.getUploadLimit()));
        }
    }
}
