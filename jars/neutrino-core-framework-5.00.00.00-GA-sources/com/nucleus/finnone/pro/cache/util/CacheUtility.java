package com.nucleus.finnone.pro.cache.util;


public class CacheUtility {
    
	public static final String CACHE_DELIMETER = "$$";
	public static final String CACHE_NULL_KEY = "NULL_KEY";
	
	private CacheUtility(){
    
    }

	public static String removeLastCacheDelimeter(String cacheKey){
		return cacheKey.substring(0,cacheKey.lastIndexOf(CacheUtility.CACHE_DELIMETER));
	}
	
	public static StringBuilder removeLastCacheDelimeter(StringBuilder cacheKey){
		return new StringBuilder(cacheKey.substring(0,cacheKey.lastIndexOf(CacheUtility.CACHE_DELIMETER)));
	}
	
	public static StringBuilder replaceNullwithNullKey(StringBuilder cacheKey,Object keyValue){
		if(keyValue==null){
			cacheKey.append(CacheUtility.CACHE_NULL_KEY).append(CacheUtility.CACHE_DELIMETER);
		}else{
			cacheKey.append(keyValue).append(CacheUtility.CACHE_DELIMETER);
		}
		return cacheKey;
	}
	
}
