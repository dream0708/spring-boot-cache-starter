package com.jee.cache.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONArray;

import io.protostuff.JsonIOUtil;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

public class ProtobufUtils {

	// 缓存 schema 对象的 map
	private static Map<Class<?>, RuntimeSchema<?>> cachedSchema = new ConcurrentHashMap<>();

	/**
	 * 根据获取相应类型的schema方法
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private static <T> RuntimeSchema<T> getSchema(Class<T> clazz) {
		RuntimeSchema<T> schema = (RuntimeSchema<T>) cachedSchema.get(clazz);
		if (schema == null) {
			schema = RuntimeSchema.createFrom(clazz);
			cachedSchema.put(clazz, schema);
		}
		return schema;
	}

	/**
	 * 序列化方法，将对象序列化为字节数组（对象 ---> 字节数组）
	 */
	@SuppressWarnings("unchecked")
	private static <T> byte[] serialize(T obj) {
		Class<T> clazz = (Class<T>) obj.getClass();
		RuntimeSchema<T> schema = getSchema(clazz);
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
	}

	/**
	 * 反序列化方法，将字节数组反序列化为对象（字节数组 ---> 对象）
	 */
	private static <T> T deserialize(byte[] data, Class<T> clazz) {
		RuntimeSchema<T> schema = RuntimeSchema.createFrom(clazz);
		T message = schema.newMessage();
		ProtostuffIOUtil.mergeFrom(data, message, schema);
		return message;
	}
	
	public static <T> byte[] toProtobufBytes(T obj) {
		return serialize(new ProtobufData<T>().result(obj)) ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(byte[] bytes , Class<T> clazz) {
		if(bytes == null || bytes.length == 0) {
			return null ;
		}
		ProtobufData<T> data = deserialize(bytes , ProtobufData.class) ;
		return data.getResult();
	}
	
	public static Object parseObject(byte[] bytes ) {
		
		@SuppressWarnings("unchecked")
		ProtobufData<Object> data = deserialize(bytes , ProtobufData.class) ;
		return data.getResult();
	}
	
	

	public static class ProtobufData<T> {

		private T result ;

		public  T getResult() {
			return  result;
		}

		public void setResult(T result) {
			this.result = result;
		}
		
		public ProtobufData<T> result(T result) {
			setResult(result) ;
			return this ;
		}
	}
	
	
	
}
