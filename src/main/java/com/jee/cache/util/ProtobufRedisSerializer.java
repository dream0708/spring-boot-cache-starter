package com.jee.cache.util;


import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;


public class ProtobufRedisSerializer<T> implements RedisSerializer<T> {

	public byte[] serialize(T obj) throws SerializationException {
		return ProtobufUtils.toProtobufBytes(obj)  ;
	}

	@SuppressWarnings("unchecked")
	public T deserialize(byte[] arg) throws SerializationException {
		return (T) ProtobufUtils.parseObject(arg , Object.class ) ;
	}

}
