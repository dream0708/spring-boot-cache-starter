package com.jee.cache.template;

import java.util.List;

public interface CacheTemplate {
	
	
	public Object getCacheObject(String name , String key ) ;	
	
	public Object getCacheObject(String key ) ;	
	
	public boolean   setCacheObject(String key , Object obj , int timeout) ;
	
	public boolean   setCacheObject(String name ,  Object obj  ) ;
	
	public boolean   delete(String key) ;
	
	public boolean   delete(List<String> keys) ;
	
	public long currtDistributionTime() ;
	
	public Long tryLock(String lockKey, long lockTimeout) ;
	
	public void unlock(String lockKey, long lockValue ) ;
	
	public void unlock(String lockKey ) ;

}
