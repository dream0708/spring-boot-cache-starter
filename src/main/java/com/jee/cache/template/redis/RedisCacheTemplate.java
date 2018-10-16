package com.jee.cache.template.redis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.connection.ReturnType;
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
				return conn.del(key.getBytes());
			}
		});
	}

	@Override
	public Long tryLock(String lockKey, long lockTimeout) {
		// Long timeout = currtDistributionTime();
		log.info("开始执行加锁  lock key :{}  value : {}  ", lockKey, lockTimeout); // 锁时间
		if (redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection conn) throws DataAccessException {
				boolean flag = conn.set(lockKey.getBytes(), String.valueOf(lockTimeout).getBytes(),
						Expiration.from(lockTimeout, TimeUnit.MILLISECONDS), SetOption.SET_IF_ABSENT);
				log.info("lock key :{} , value : {} ,  SET_IF_ABSENT : {} ", lockKey, lockTimeout, flag);
				return flag;
			}
		})) {
			return lockTimeout;
		}
		return null;
	}

	@Override
	public boolean unlock(String lockKey, long lockValue) {
		String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

		boolean flag = redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection conn) throws DataAccessException {
				byte[][] bs = new byte[2][];
				bs[0] = lockKey.getBytes();
				bs[1] = String.valueOf(lockValue).getBytes();
				return conn.eval(script.getBytes(), ReturnType.BOOLEAN, 2, bs);
			}

		});

		return flag;

	}

	@Override
	public boolean unlock(String lockKey) {
		del(lockKey);
		return true;

	}
}
