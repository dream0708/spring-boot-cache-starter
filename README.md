# spring-boot-cache-starter

#### 项目介绍
spring-boot-cache-starter 基于spring-boot 高效分布式缓存
目前基于redis实现，对代码无任何入侵 ，接入简单方便
后续添加memecached支持
#### 项目主要解决问题
1. SpringCache 无法对不同的接口配置不同的过期时间
2. 支持多种分布式方案（Redis Memecached等）
3. 基于Protobuf等高效的序列化方式
4. 支持异步对象（CompletableFuture）缓存


#### 使用方式

1. 获取spring-boot-cache-starter 源代码
   git clone git@gitee.com:dream0708/spring-boot-cache-starter.git <br>
        或者 git@github.com:dream0708/spring-boot-cache-starter.git
2. 安装到本地 Maven 仓库
   spring-boot-cache-starter/spring-boot-cache-starter 目录下执行 <br>
   mvn clean install
3. 在SpringBoot项目中添加依赖
```
   <dependency>
    <groupId>com.jee</groupId>
    <artifactId>spring-boot-cache-starter</artifactId>
    <version>${your.version}</version>
   </dependency>
```
#### 使用说明

1. 添加Redis支持(暂时只支持redis缓存 ，后续添加Memecached等)
   在application.properties添加redis配置(支持多种Redis方式，参考spring-boot-redis-starter）
   ```
   spring.redis.host=xx.xx.xx.xx
   spring.redis.port=xx
   spring.redis.password=xxx
   spring.redis.maxIdle=10 
   spring.redis.maxTotal=5 
   spring.redis.maxWaitMillis=5000 
   spring.redis.testOnBorrow=false 
   spring.redis.index=11
   ```
2. 开启缓存@EnableAutoCache
```
   @SpringBootApplication
   @EnableAutoCache
   public class StartApplication{
       //启动方法
   }
```
3. 通过注解配置需要缓存的对象
 ```  
   @Cachable(name = "cache:user:" , key = "#userid" , expire = 2000) 
   public User getUserById(String userid)

   @Cachable(name = "cache:user:details" , key = "#user.userid" , expire = 2000) 
   public UserDetail getUserDetails(User userid)

   @Cachable(name = "cache:user:details" , key = "#user.userid" , expire = 2000) 
   public CompletableFuture<UserDetail> getUserDetailsAsync(User userid)
   
   @CacheEvict(name = "cache:test:list" , key = "#id") 
   public void updateUser(String userid)
```

   --name 缓存前缀部分
   --key  基于SpeL表达式不同部分
   --expire 缓存时间单位秒 默认不过期
   --lock = true 添加分布式锁 防止大量不走缓存导致程序雪崩
   
   

#### 参与贡献

1. Fork 本项目
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request



#### 欢迎你的加入


