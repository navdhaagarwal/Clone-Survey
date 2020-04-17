package com.nucleus.jsMessageResource.service;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.jsMessageResource.entity.JsMessageResourceValue;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named("jsMessageResourceService")
public class JsMessageResourceServiceImpl extends BaseServiceImpl implements JsMessageResourceService  {

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Override
    public String getPropertyForKeyAndLocale(String key,String locale){
        NeutrinoValidator.notNull(key, "Key can not be null to get property value");
        NeutrinoValidator.notNull(locale, "Locale can not be null to get property value");
        NamedQueryExecutor<String> messageResource = new NamedQueryExecutor<String>("JsMessageResourceValue.getPropertyForKeyAndLocale")
                .addParameter("key", key).addParameter("locale", locale);
        messageResource.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQueryForSingleValue(messageResource);
    }

    private List<JsMessageResourceValue> getPropertiesForKeysAndLocale(List <String> keys, String locale){
        NeutrinoValidator.notNull(keys, "Keys can not be null to get property value");
        NeutrinoValidator.notNull(locale, "Locale can not be null to get property value");
        NamedQueryExecutor<JsMessageResourceValue> messageResources = new NamedQueryExecutor<JsMessageResourceValue>(
                "JsMessageResourceValue.getPropertiesForKeysAndLocale").addParameter("key", keys).addParameter("locale", locale);
        messageResources.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<JsMessageResourceValue> jsMessageResourceValues = entityDao.executeQuery(messageResources);
        return jsMessageResourceValues;
    }

    @Override
    public String getPropertyForKey(String key){
        return getPropertyForKeyAndLocale(key,getUserLocale().toString());
    }

    @Override
    public String getAppendedPropertyForKeys(String localKey,String globalKey){
        return getAppendedPropertyForKeysAndLocale(localKey,globalKey,getUserLocale().toString());
    }

    @Override
    public String getAppendedPropertyForKeysAndLocale(String localKey,String globalKey,String locale){
        List <String>keyList=new ArrayList<>();
        keyList.add(localKey);
        keyList.add(globalKey);
        String localProperty=null;
        String globalProperty=null;
        List<JsMessageResourceValue> jsMessageResourceValues=getPropertiesForKeysAndLocale(keyList,locale);
        for(JsMessageResourceValue jsmrv:jsMessageResourceValues){
            if(localKey.equals(jsmrv.getKey())){
                localProperty=jsmrv.getValue();
            }else if( globalKey.equals(jsmrv.getKey()) ){
                globalProperty=jsmrv.getValue();
            }
        }
        StringBuilder str=new StringBuilder(globalProperty);
        str.insert(2,localProperty);
        return str.toString();
    }

}
