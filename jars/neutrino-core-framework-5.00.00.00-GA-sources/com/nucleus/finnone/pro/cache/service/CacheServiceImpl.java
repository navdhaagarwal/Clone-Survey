package com.nucleus.finnone.pro.cache.service;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.nucleus.finnone.pro.cache.common.CacheManager;

@Named("cacheService")
public class CacheServiceImpl implements CacheService{

    @Autowired
    private ApplicationContext applicationContext;
    
    public CacheManager getCacheManager(String beanId){
        return (CacheManager)applicationContext.getBean(beanId);
    }
}
