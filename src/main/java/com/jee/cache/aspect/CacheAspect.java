package com.jee.cache.aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.jee.cache.annotation.CacheEvict;
import com.jee.cache.annotation.CacheEvicts;
import com.jee.cache.annotation.Cacheable;
import com.jee.cache.template.CacheTemplate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Aspect
//@ConditionalOnBean(CacheTemplate.class)
public class CacheAspect {

	@Autowired
	private CacheTemplate cacheTemplate;

	@Value("${cache.default.value:Cache}")
	private String prefixCacheName = "Cache";
	
	@Around("@annotation(com.jee.cache.annotation.Cacheable)")
	public Object cacheSet(ProceedingJoinPoint pjp) throws Throwable {
		Object result = null;
		try {
			Method method = getMethod(pjp);
			if (null == method) {
				return pjp.proceed();
			}
			final Cacheable cacheable = method.getAnnotation(Cacheable.class);
			if (null == cacheable) {
				return pjp.proceed();
			}
			String key = parseKey(cacheable.key(), method, pjp.getArgs());
			if (StringUtils.isEmpty(key)) { //空对象空值 不走缓存
				return pjp.proceed();
			}
			boolean condition = parseCondition(cacheable.condition(), method, pjp.getArgs()); // 满足要求
			if (!condition) { // false不走缓存判断 true走缓存判断(默认走缓存判断)
				return pjp.proceed();
			}
			Class<?> returnType = ((MethodSignature) pjp.getSignature()).getReturnType(); // 获取方法的返回类型,让缓存可以返回正确的类型
			if (returnType == Void.class || returnType == void.class) {
				return pjp.proceed();
			}
			String name = StringUtils.isEmpty(cacheable.name()) ? 
					prefixCacheName + ":" + method.getName() + ":" : cacheable.name() ;
			try {
				result = cacheTemplate.getCacheObject(name + key);
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				log.error(
						"[find cache ] result from cache error , invoke the target method now , "
								+ "method:{} , name :{} , key :{} , exception :{}",
						method.getName(), name, key, ex.getClass());
				result = pjp.proceed();
			}
			if (null == result) { // 正常从缓存拿到的result为空对象
				if (cacheable.lock()) { // 分布式锁 同一时间只允许一个进程去执行该方法 防止雪崩

				}
				log.info("no cache found , invoke the target method[{}] begin... ", method.getName());
				Long current = System.currentTimeMillis();
				result = pjp.proceed();
				log.info("no cache found , invoke the target method[{}] end , cost:{}:ms ", method.getName(),
						System.currentTimeMillis() - current);
				if (result == null) {
					return result;
				}
				if (returnType == CompletableFuture.class) { // CF对象
					CompletableFuture<?> cf = (CompletableFuture<?>) result;
					return cf.thenApply(data -> {
						if (data == null) {
							return data;
						}
						try {
							int expire = cacheable.expire();
							if (expire > 0) {
								cacheTemplate.setCacheObject(name + key, data, expire);
							} else {
								cacheTemplate.setCacheObject(name + key, data);
							}
							return data;
						} catch (Exception ex) {
							log.error(ex.getMessage(), ex);
							log.error(
									" [cache async] result to cache error ****** method:{} ,  key :{} , timeout :{} ,  exception :{} ",
									method.getName(), name + key, cacheable.expire(), ex.getMessage());
							return data;
						}
					});

				} else { // 同步对象
					try {
						int expire = cacheable.expire();
						if (expire > 0) {
							cacheTemplate.setCacheObject(name + key, result, expire);
						} else {
							cacheTemplate.setCacheObject(name + key, result);
						}
						return result;
					} catch (Exception ex) {
						log.error(
								" [cache sync ] result to cache error ****** method:{} , key : {} , timeout : {} ,  exception :{} ",
								method.getName(), name + key, cacheable.expire(), ex.getMessage());
						return result;
					}
				}
			}
			if (returnType == CompletableFuture.class && !(result instanceof CompletableFuture)) {
				return CompletableFuture.completedFuture(result);
			}
			return result;
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			throw e;
		}

	}

	@Around("@annotation(com.jee.cache.annotation.CacheEvict)")
	public Object cacheEvict(ProceedingJoinPoint pjp) throws Throwable {
		Object result = null;

		try {
			Method method = getMethod(pjp);
			if (null == method) {
				result = pjp.proceed();
				return result;
			}
			CacheEvict cacheable = method.getAnnotation(CacheEvict.class);
			if (cacheable == null) {
				return pjp.proceed();
			}
			String key = parseKey(cacheable.key(), method, pjp.getArgs());
			if (StringUtils.isEmpty(key)) {
				key = "" ;  // 空对象空值删除
			}
			boolean condition = parseCondition(cacheable.condition(), method, pjp.getArgs()); // 满足要求
			if (!condition) {
				return pjp.proceed();
			}
			String name = StringUtils.isEmpty(cacheable.name()) ? 
					prefixCacheName + ":" + method.getName() + ":" : cacheable.name() ;
			try {
				cacheTemplate.delete(name + key);
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				log.error("remove cache error, caches: {} ", name + key); // 后续完善自动扫描功能
			}
			return pjp.proceed();
			

		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			throw e;
		}

	}

	@Around("@annotation(com.jee.cache.annotation.CacheEvicts)")
	public Object cacheEvicts(ProceedingJoinPoint pjp) throws Throwable {
	
		try {
			Method method = getMethod(pjp);
			if (null == method) {
				return pjp.proceed();
			}
			CacheEvicts cacheables = method.getAnnotation(CacheEvicts.class);
			if (cacheables == null || cacheables.value().length == 0) {
				return  pjp.proceed();
			}
			List<String> list = new ArrayList<String>();
			for (CacheEvict cacheable : cacheables.value()) {
				String key = parseKey(cacheable.key(), method, pjp.getArgs());
				boolean condition = parseCondition(cacheable.condition(), method, pjp.getArgs()); // 满足要求
				if (condition) {
					String name = StringUtils.isEmpty(cacheable.name()) ? 
							prefixCacheName + ":" + method.getName() + ":" : cacheable.name() ;
					list.add(name + key);
				}
			}
			if (!CollectionUtils.isEmpty(list)) {
				try {
					cacheTemplate.delete(list);
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
					log.error("remove cache batch error, caches: {} ", list); // 后续完善自动扫描功能
				}
			}
			return pjp.proceed();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			throw e;
		}

	}

	/**
	 * 获取被拦截方法对象
	 * 
	 * MethodSignature.getMethod() 获取的是顶层接口或者父类的方法对象 而缓存的注解在实现类的方法上
	 * 所以应该使用反射获取当前对象的方法对象
	 */
	public Method getMethod(ProceedingJoinPoint pjp) {
		Method method = null;
		try {
			MethodSignature msg = (MethodSignature) pjp.getSignature();
			method = pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(), msg.getParameterTypes());
		} catch (NoSuchMethodException e) {
			log.error(e.getMessage(), e);
		} catch (SecurityException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return method;

	}

	private boolean parseCondition(String key, Method method, Object[] args) {
		if (StringUtils.isEmpty(key)) {
			return true;
		}
		// 获取被拦截方法参数名列表(使用Spring支持类库)
		LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
		String[] paraNameArr = u.getParameterNames(method);
		// 使用SPEL进行key的解析
		ExpressionParser parser = new SpelExpressionParser();
		// SPEL上下文
		StandardEvaluationContext context = new StandardEvaluationContext();
		// 把方法参数放入SPEL上下文中
		for (int i = 0; i < paraNameArr.length; i++) {
			context.setVariable(paraNameArr[i], args[i]);
		}
		try {
			return parser.parseExpression(key).getValue(context, boolean.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}

	}

	/**
	 * 获取缓存的key key 定义在注解上，支持SPEL表达式
	 * 
	 * @param pjp
	 * @return
	 */
	private String parseKey(String key, Method method, Object[] args) {
		if(StringUtils.isEmpty(key)) {
			return key  ;
		}
		// 获取被拦截方法参数名列表(使用Spring支持类库)
		LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
		String[] paraNameArr = u.getParameterNames(method);
		// 使用SPEL进行key的解析
		ExpressionParser parser = new SpelExpressionParser();
		// SPEL上下文
		StandardEvaluationContext context = new StandardEvaluationContext();
		// 把方法参数放入SPEL上下文中
		for (int i = 0; i < paraNameArr.length; i++) {
			context.setVariable(paraNameArr[i], args[i]);
		}
		try {
			return parser.parseExpression(key).getValue(context, String.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return key;
		}

	}

}
