package com.nucleus.jsMessageResource.service;

import com.nucleus.service.BaseService;

public interface JsMessageResourceService extends BaseService {

    public String getPropertyForKey(String key);
    public String getPropertyForKeyAndLocale(String key,String locale);
    public String getAppendedPropertyForKeys(String localKey,String globalKey);
    public String getAppendedPropertyForKeysAndLocale(String localKey,String globalKey,String locale);
}
