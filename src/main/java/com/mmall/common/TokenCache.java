package com.mmall.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TokenCache {

    private static org.slf4j.Logger logger=LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX="token_";

    private static LoadingCache<String,String> loadingCache=CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12,TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
              //默认的数据加载实现，当调用的key没有值，就调用此方法
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    public static void setKey(String key,String value){
        loadingCache.put(key,value);
    }

    public static String getKey(String key){
        String value=null;
        try {
            value=loadingCache.get(key);
            if("null".equals(value)){
                return  null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error",e);
        }
        return null;
    }

}
