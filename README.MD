+基于SpringBoot缓存框架
+基于SpringMVC
+三个部分
+restful-base 基础架构
+   基于注解入参校验
+   统一异常处理
+   统一权限配置
+   错误码定义与扩展
+   同步请求日志请求以及埋点
+   异步请求日志请求以及埋点
+   会话管理
+   基于Redis会话管理
+   跨线程埋点MDC
+   XSF拦截器
+   分布式锁
+   基于注解的分布式缓存
+restful-async 异步核心框
+   基于CompletableFuture 并行和串行框架 
+   异步拦截器
+   异步分布式缓存
+restful-resposne 返回报文
+   个性化报文格式设置