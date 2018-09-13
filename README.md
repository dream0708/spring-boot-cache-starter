# spring-boot-cache-starter

#### 项目介绍
spring-boot-cache-starter 基于spring-boot 高效分布式缓存
目前基于redis实现，对代码无任何入侵 ，接入简单方便

#### 项目主要解决问题
1. SpringCache 无法对不同的接口配置不同的过期时间
2. 支持多种分布式方案（Redis Memecached等）
3. 基于Protobuf高效的序列化方式
4. 支持异步对象（CompletableFuture）缓存


#### 使用方式

1. 获取spring-boot-cache-starter 源代码
   git clone  git@gitee.com:dream0708/spring-boot-cache-starter.git
2. 安装到本地 Maven 仓库
   # spring-boot-cache-starter/spring-boot-cache-starter 目录下执行
   mvn clean install
3. 在SpringBoot项目中添加依赖
   <dependency>
            <groupId>com.jee</groupId>
            <artifactId>spring-boot-cache-starter</artifactId>
            <version>${your.version}</version>
   </dependency>

#### 使用说明

1. 添加Redis支持
   在application.properties添加redis配置(支持多种Redis方式）
   

1. spring.redis.host=xx.xx.xx.xx
1.    spring.redis.port=6379 
1.    spring.redis.password=xxx
1.    spring.redis.maxIdle=10 
1.    spring.redis.maxTotal=5 
1.    spring.redis.maxWaitMillis=5000 
1.    spring.redis.testOnBorrow=false 
1.    spring.redis.index=11
2. 配置@EnableAutoCache
   @SpringBootApplication
   @EnableAutoCache
   public class StartApplication{
       //启动方法
   }

3. 通过注解配置需要缓存的对象

#### 参与贡献

1. Fork 本项目
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request


#### 码云特技

1. 使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2. 码云官方博客 [blog.gitee.com](https://blog.gitee.com)
3. 你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解码云上的优秀开源项目
4. [GVP](https://gitee.com/gvp) 全称是码云最有价值开源项目，是码云综合评定出的优秀开源项目
5. 码云官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6. 码云封面人物是一档用来展示码云会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)