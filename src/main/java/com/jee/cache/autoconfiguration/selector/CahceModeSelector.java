package com.jee.cache.autoconfiguration.selector;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import com.jee.cache.aspect.CacheAspect;
import com.jee.cache.autoconfiguration.annotation.EnableAutoCache;
import com.jee.cache.configuration.RedisCacheTemplateConfiguration;
import com.jee.cache.mode.CacheMode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CahceModeSelector implements ImportSelector{

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		Class<?> annotationType = EnableAutoCache.class;
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
        		importingClassMetadata.getAnnotationAttributes(
                annotationType.getName(), false));
        
        CacheMode mode = (CacheMode) attributes.getEnum("mode") ;
        
        log.info("Cache model seletored , The mode is {} ....." , mode.toString()) ;
        if(mode == CacheMode.REDIS) {
        	return new String[] {  CacheAspect.class.getName() ,RedisCacheTemplateConfiguration.class.getName()  };
        }else {
        	return new String[] {  CacheAspect.class.getName()  ,RedisCacheTemplateConfiguration.class.getName() };
        }
	}

}
