# oxygen
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/vip.justlive/oxygen/badge.svg)](https://maven-badges.herokuapp.com/maven-central/vip.justlive/oxygen/)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)


轻量级Java框架



## 介绍

一个轻量级Java框架

- oxygen-core
  - 配置管理，支持${attrs.key:defaultValue}表达式获取配置
  - 加解密管理，提供加解密服务内置基础加密实现，例如SHA-1、SHA-256、MD5
  - 异常管理，提供异常包装，统一异常编码，便于国际化
  - i18n国际化
  - 资源文件加载，提供file,jar,classpath等文件加载
  - 类扫描器
  - 部分工具类
  - retry重试

- oxygen-ioc
  - 基于构造器的轻量级依赖注入

- oxygen-aop
  - 基于cglib实现的切面
  - 轻巧简单，可单独使用
  - 可使用注解`Aspect`或直接实现`Interceptor`编写切面

- oxygen-cache
  - 内置Ehcache和LocalCache缓存
  - 提供基于注解`Cacheable`的方法缓存

- oxygen-job
  - 提供基于注解`Scheduled`的定时任务

- oxygen-jdbc
  - 小巧简单的jdbc实现，纯jdk实现，无第三方jar
  - 支持多数据源
  - 基于sql进行crud，不提供类似Hibernate的链式方法（原因：sql作为数据库领域的DSL，已经很自然优雅，Less is more）

- oxygen-web
  - 轻量级web框架支持注解声明和函数式编程
  - 支持Servlet3.0 `ServletContainerInitializer` 自动加载，省略web.xml
  - 支持i18n动态切换
  - 提供`WebHook`进行请求拦截处理
  - 支持自定义全局异常处理
  


## 特性

* 轻量级，使用简单
* 支持插件扩展
* 函数式编程
* 流式风格


## 快速开始

创建`Maven`项目

```
<!-- 使用内嵌tomcat启动 -->
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>oxygen-web-tomcat</artifactId>
    <version>${oxygen.version}</version>
</dependency>
```

或`Gradle`

```
compile 'vip.justlive:oxygen-web-tomcat:$oxygenVersion'
```

> 不需要`webapp`项目框架，支持Servlet3.0

编写 `main` 函数写一个 `Hello World`

```
public static void main(String[] args) {
  Router.router().path("/").handler(ctx -> ctx.response().write("hello world"));
  Server.server().listen(8080);
}
```

用浏览器打开 http://localhost:8080 这样就可以看到 `hello world` 了！


## 联系信息

E-mail: qq11419041@163.com

QQ群: 950216299