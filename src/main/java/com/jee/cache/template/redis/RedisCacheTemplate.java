package com.jee.cache.template.redis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import com.jee.cache.template.CacheTemplate;
import com.jee.cache.util.ProtobufUtils;

import lombok.Data;


@Data
public class RedisCacheTemplate implements CacheTemplate {
	
	
	private RedisTemplate<?, ?> redisTemplate ;

	@Override
	public Object getCacheObject(String name, String key) {
		return getCacheObject(name + key ) ;
	}

	@Override
	public Object getCacheObject(String key) {
		return redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection conn) throws DataAccessException {
				byte[] bytes = conn.get( key.getBytes() ) ;
				return  ProtobufUtils.parseObject(bytes, Object.class) ;
			}
		}) ;
	}

	@Override
	public boolean setCacheObject(String key, Object obj, int timeout) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean  doInRedis(RedisConnection conn) throws DataAccessException {
				conn.set(key.getBytes(), ProtobufUtils.toProtobufBytes(obj) ,
						Expiration.seconds(timeout) , SetOption.SET_IF_ABSENT );
				return true ;
			}
		}) ;
	}

	@Override
	public boolean setCacheObject(String key, Object obj) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean  doInRedis(RedisConnection conn) throws DataAccessException {
				conn.set(key.getBytes(), ProtobufUtils.toProtobufBytes(obj) ) ;
				return true ;
			}
		}) ;
	}

	@Override
	public boolean delete(String key) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean  doInRedis(RedisConnection conn) throws DataAccessException {
				conn.del(key.getBytes()) ;
				return true ;
			}
		}) ;
	}

	@Override
	public boolean delete(List<String> keys) {
		// TODO Auto-generated method stub
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean  doInRedis(RedisConnection conn) throws DataAccessException {
				byte[][] arrs = new byte[keys.size()][] ;
				for(int i = 0 ; i < 0 ; i ++) {
					arrs[i] = keys.get(i).getBytes() ;
				}
				conn.del(arrs) ;
				return true ;
			}
		}) ;
	}

	
	
	   

}
