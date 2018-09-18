package com.jee.cache.template.redis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cglib.core.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import com.jee.cache.template.CacheTemplate;
import com.jee.cache.util.ProtobufUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class RedisCacheTemplate implements CacheTemplate {

	private RedisTemplate<?, ?> redisTemplate;

	@Override
	public Object getCacheObject(String name, String key) {
		return getCacheObject(name + key);
	}

	@Override
	public Object getCacheObject(String key) {
		return redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection conn) throws DataAccessException {
				byte[] bytes = conn.get(key.getBytes());
				return ProtobufUtils.parseObject(bytes, Object.class);
			}
		});
	}

	@Override
	public boolean setCacheObject(String key, Object obj, int timeout) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection conn) throws DataAccessException {
				conn.set(key.getBytes(), ProtobufUtils.toProtobufBytes(obj), Expiration.seconds(timeout),
						SetOption.SET_IF_ABSENT);
				return true;
			}
		});
	}

	@Override
	public boolean setCacheObject(String key, Object obj) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection conn) throws DataAccessException {
				conn.set(key.getBytes(), ProtobufUtils.toProtobufBytes(obj));
				return true;
			}
		});
	}

	@Override
	public boolean delete(String key) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection conn) throws DataAccessException {
				conn.del(key.getBytes());
				return true;
			}
		});
	}

	@Override
	public boolean delete(List<String> keys) {
		// TODO Auto-generated method stub
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection conn) throws DataAccessException {
				byte[][] arrs = new byte[keys.size()][];
				for (int i = 0; i < 0; i++) {
					arrs[i] = keys.get(i).getBytes();
				}
				conn.del(arrs);
				return true;
			}
		});
	}

	public long currtDistributionTime() {
		return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection conn) throws DataAccessException {
				return conn.time();
			}
		});
	}

	public String get(String key) {
		return redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection conn) throws DataAccessException {
				byte[] bytes = conn.get(key.getBytes());
				if (bytes == null || bytes.length == 0) {
					return null;
				}
				return new String(bytes);
			}
		});
	}

	public byte[] getAndSet(String key, String value) {
		return redisTemplate.execute(new RedisCallback<byte[]>() {
			@Override
			public byte[] doInRedis(RedisConnection conn) throws DataAccessException {
				return conn.getSet(key.getBytes(), value.getBytes());
			}
		});
	}

	public boolean expireMillseconds(String key, long value) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection conn) throws DataAccessException {
				return conn.pExpire(key.getBytes(), value);
			}
		});
	}
	
	public Long del(String key) {
		return redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection conn) throws DataAccessException {
				return conn.del(key.getBytes()) ;
			}
		});
	}

	@Override
	public Long tryLock(String lockKey, long lockTimeout) {
		log.info("...{} 开始执行加锁    ", lockKey);
		// 锁时间
		Long timeout = currtDistributionTime() + lockTimeout + 1;
		if (redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection conn) throws DataAccessException {
				boolean flag = conn.set(lockKey.getBytes(), String.valueOf(timeout).getBytes(),
						Expiration.from(timeout, TimeUnit.MILLISECONDS), SetOption.SET_IF_ABSENT);
				log.info("lock key :{}  SET_IF_ABSENT : {} ", lockKey, flag);
				return flag;
			}
		})) {
			return timeout;
		} else {

			// 未获取锁，尝试重新获取
			// 此处使用double check 的思想，防止多线程同时竞争到锁
			// 1) 先获取上一个锁的过期时间，校验当前是否过期。
			// 2) 如果过期了，尝试使用getset方式获取锁。此处可能存在多个线程同时执行到的情况。
			// 3) getset更新过期时间，并且获取上一个锁的过期时间。
			// 4) 如果getset获取到的oldExpireAt 已过期，说明获取锁成功。
			// 如果和当前比未过期，说明已经有另一个线程提前获取到了锁
			// 这样也没问题，只是短暂的将上一个锁稍微延后一点时间（只有在A和B线程同时执行到getset时，才会出现，延长的时间很短）
			// 获取redis里面的时间
			String result = get(lockKey);
			Long currentLockTimeout = (result == null ? null : Long.parseLong(result));
			// 锁已经失效
			if (currentLockTimeout != null && currentLockTimeout.longValue() < System.currentTimeMillis()) {
				// 判断是否为空，不为空时，说明已经失效，如果被其他线程设置了值，则第二个条件判断无法执行
				// 获取上一个锁到期时间，并设置现在的锁到期时间
				Long oldLockTimeout = null;
				byte[] bytes = getAndSet(lockKey, timeout.toString());
				if (bytes != null && bytes.length > 0) {
					oldLockTimeout = Long.valueOf(new String(bytes));
				}
				if (oldLockTimeout != null && oldLockTimeout.longValue() == currentLockTimeout.longValue()) {
					// 多线程运行时，多个线程签好都到了这里，但只有一个线程的设置值和当前值相同，它才有权利获取锁
					log.info("{}  加锁成功, OLD LOCKTIME equals CURRENT LOCKTIME...", lockKey);
					// 设置超时间，释放内存
					expireMillseconds(lockKey, lockTimeout);
					// 返回加锁时间
					return timeout;
				}
			}
		}

		return null;

	}
	
	
	
	@Override
	public void unlock(String lockKey, long lockValue ) {
		log.info("key:{} , value:{} 执行解锁...", lockKey, lockValue); // 正常直接删除
		String result = get(lockKey) ; // 获取redis中设置的时间
		Long currLockTimeout = (result == null) ? null : Long.valueOf(result);
		// 如果是加锁者，则删除锁， 如果不是，则等待自动过期，重新竞争加锁
		if (currLockTimeout != null && currLockTimeout.longValue() == lockValue) {
			del(lockKey);
			log.info("key:{} , value:{}解锁成功...", lockKey, lockValue);
		}

	}
	
	@Override
	public void unlock(String lockKey) {
		del(lockKey) ;

	}
}
