package com.jee.cache.configuration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.jee.cache.template.CacheTemplate;
import com.jee.cache.template.redis.RedisCacheTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisCacheTemplateConfiguration {
	
	
	
	@ConditionalOnClass(RedisTemplate.class)
	@Bean
	public CacheTemplate getRedisCacheTemplate(
			RedisTemplate<? , ?> redisTemplate 
			) {
		log.info("redis template cache template config ");
		RedisCacheTemplate template = new RedisCacheTemplate() ;
		template.setRedisTemplate(redisTemplate); 
		return template ;
	}
	


}
