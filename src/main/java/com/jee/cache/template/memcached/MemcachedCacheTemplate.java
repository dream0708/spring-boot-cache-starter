package com.jee.cache.template.memcached;

import com.jee.cache.template.CacheTemplate;

import java.util.List;

/**
 * @author yaomengke
 * @create 2018- 10 - 11 - 15:44
 */
public class MemcachedCacheTemplate  implements CacheTemplate {
    @Override
    public Object getCacheObject(String name, String key) {
        return null;
    }

    @Override
    public Object getCacheObject(String key) {
        return null;
    }

    @Override
    public boolean setCacheObject(String key, Object obj, int timeout) {
        return false;
    }

    @Override
    public boolean setCacheObject(String name, Object obj) {
        return false;
    }

    @Override
    public boolean delete(String key) {
        return false;
    }

    @Override
    public boolean delete(List<String> keys) {
        return false;
    }

    @Override
    public long currtDistributionTime() {
        return 0;
    }

    @Override
    public Long tryLock(String lockKey, long lockTimeout) {
        return null;
    }

    @Override
    public boolean unlock(String lockKey, long lockValue) {
        return false;
    }

    @Override
    public boolean unlock(String lockKey) {
        return false;
    }
}
