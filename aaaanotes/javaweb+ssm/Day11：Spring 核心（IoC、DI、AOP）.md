#  

## 学习目标

- 理解 Spring 框架的定位与核心思想
- 掌握 IoC（控制反转）与 DI（依赖注入）
- 熟练使用注解配置 Spring 容器
- 掌握 Bean 的作用域与生命周期
- 掌握 `@Autowired`、`@Value`、`@Configuration`、`@Bean`
- 理解 AOP（面向切面编程）及其实战应用
- 为 SSM 整合（Day13）打基础

---

## 一、Spring 概述

### 1. Spring 是什么

Spring 是一个**轻量级的 Java 开发框架**，核心是 **IoC 容器** 和 **AOP**，用于简化企业级开发。

**Spring 解决的问题：**
- 对象创建与依赖管理（IoC/DI）
- 横切关注点（日志、事务、权限）与业务解耦（AOP）
- 声明式事务
- 整合各种框架（MyBatis、Redis、MQ 等）

### 2. Spring 生态

| 项目 | 作用 |
|------|------|
| Spring Framework | 核心框架（IoC、AOP、Web） |
| Spring MVC | Web 层框架 |
| Spring Boot | 快速构建（自动配置） |
| Spring Cloud | 微服务 |
| Spring Data | 数据访问（JPA、Redis、MongoDB） |
| Spring Security | 安全框架 |
| Spring Batch | 批处理 |
| Spring Session | 分布式 Session |

> 💡 本课程重点是 **Spring Framework 核心**（Day11）+ **Spring MVC**（Day12）+ **SSM 整合**（Day13）。Spring Boot 不在本课程范围，但学完后可平滑过渡。

### 3. Spring 核心思想

- **IoC（Inversion of Control，控制反转）**：对象的创建和依赖关系交给容器管理，而不是在代码里 new。
- **DI（Dependency Injection，依赖注入）**：IoC 的具体实现，容器把依赖注入到对象中。
- **AOP（Aspect Oriented Programming，面向切面编程）**：把日志、事务等横切逻辑从业务代码中抽离。

---

## 二、IoC 与 DI

### 1. 传统方式的痛点

```java
public class UserServiceImpl implements UserService {
    // 自己 new，紧耦合
    private UserDao userDao = new UserDaoImpl();

    public User findById(Long id) {
        return userDao.findById(id);
    }
}
```

**问题：**
- 换实现类需改代码（`UserDaoImpl` → `UserDaoMyBatis`）
- 无法替换为 Mock 测试
- DAO 创建依赖连接池等配置，难管理
- 多个类共享单例无法保证

### 2. IoC 思想

**控制反转**：把对象创建的控制权从代码交给容器。

```
传统：UserService → 自己 new UserDao
IoC：UserService ← 容器注入 UserDao
```

**好处：**
- 解耦，换实现不改代码
- 便于测试（可注入 Mock）
- 统一管理生命周期
- 单例、延迟加载等可配置

### 3. DI 的三种方式

**（1）构造器注入（推荐）**
```java
@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }
}
```

优点：依赖不可变（`final`）、非空、便于测试、避免循环依赖。
**（2）Setter 注入**
```java
@Service
public class UserServiceImpl implements UserService {
    private UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
```

优点：灵活，可用于可选依赖。
**（3）字段注入（不推荐）**
```java
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
}
```

缺点：无法加 `final`、难测试、隐藏依赖、容易循环依赖。

> 💡 **Spring 官方推荐构造器注入**，能用 `final` 就用 `final`。

### 4. 一个完整示例

```java
// DAO 接口
public interface UserDao {
    User findById(Long id);
}

// DAO 实现
@Repository
public class UserDaoImpl implements UserDao {
    @Override
    public User findById(Long id) {
        // 模拟
        return new User(id, "张三");
    }
}

// Service 接口
public interface UserService {
    User findById(Long id);
}

// Service 实现
@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User findById(Long id) {
        return userDao.findById(id);
    }
}

// 配置类
@Configuration
@ComponentScan("com.example")
public class AppConfig {}

// 测试
public class Main {
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        UserService service = ctx.getBean(UserService.class);
        System.out.println(service.findById(1L));
    }
}
```

---

## 三、Spring 容器

### 1. 容器接口

| 接口 | 说明 |
|------|------|
| `BeanFactory` | 基础容器，懒加载 |
| `ApplicationContext` | 高级容器，默认预加载，推荐 |

**ApplicationContext 常用实现：**

| 实现类 | 用途 |
|--------|------|
| `AnnotationConfigApplicationContext` | 注解配置（推荐） |
| `ClassPathXmlApplicationContext` | 类路径 XML |
| `FileSystemXmlApplicationContext` | 文件系统 XML |
| `AnnotationConfigWebApplicationContext` | Web 环境 |

### 2. 获取 Bean

```java
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

// 按类型
UserService service = ctx.getBean(UserService.class);

// 按名称
UserService service2 = (UserService) ctx.getBean("userServiceImpl");

// 按名称+类型
UserService service3 = ctx.getBean("userServiceImpl", UserService.class);

// 获取所有某类型的 Bean
Map<String, UserService> map = ctx.getBeansOfType(UserService.class);

// 判断是否存在
boolean exists = ctx.containsBean("userServiceImpl");
```

### 3. 容器启动过程（简化）

```
1. 读取配置（注解类或 XML）
2. 扫描 @Component 及其派生注解（@Service、@Repository、@Controller）
3. 实例化 Bean（构造器）
4. 依赖注入（@Autowired、@Value）
5. 初始化（@PostConstruct、InitializingBean）
6. Bean 就绪，放入单例池
7. 使用
8. 销毁（@PreDestroy、DisposableBean）
```

---

## 四、Bean 管理

### 1. 声明 Bean 的注解

| 注解 | 用途 | 分层 |
|------|------|------|
| `@Component` | 通用组件 | 通用 |
| `@Controller` | 控制器 | Web 层 |
| `@Service` | 服务 | Service 层 |
| `@Repository` | 数据访问 | DAO 层 |
| `@Configuration` | 配置类 | 配置 |
| `@Bean` | 方法返回 Bean | 配置类中 |

> 💡 `@Controller`、`@Service`、`@Repository` 都是 `@Component` 的派生注解，功能相同但语义清晰，且 `@Repository` 会做数据访问异常转换。

### 2. `@Component` 扫描

```java
@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig {}
```

**指定扫描规则：**
```java
@ComponentScan(
    basePackages = "com.example",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = Controller.class
    )
)
```

**FilterType：**
- `ANNOTATION`：按注解排除
- `ASSIGNABLE_TYPE`：按类型
- `REGEX`：按正则
- `CUSTOM`：自定义

### 3. `@Bean` 方式

用于**第三方类**（无法加注解）或**需要复杂初始化**的 Bean：

```java
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        config.setUsername("root");
        config.setPassword("123456");
        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        return factory.getObject();
    }
}
```

> 💡 `@Bean` 方法名就是 Bean 名。`@Bean` 方法的参数会自动从容器注入。

### 4. `@Component` vs `@Bean`

| 特性 | `@Component` | `@Bean` |
|------|--------------|---------|
| 作用位置 | 类上 | 方法上 |
| 适用 | 自己写的类 | 第三方类/复杂初始化 |
| 是否自动扫描 | 需 `@ComponentScan` | 需 `@Configuration` |
| 灵活性 | 一般 | 高（可编程控制） |
| 方法名 | 类名首字母小写 | 方法名 |

### 5. Bean 名称

- `@Component` 默认 Bean 名为**类名首字母小写**（`UserService` → `userService`）
- `@Service("myService")` 可显式指定
- `@Bean("dataSource")` 或方法名

### 6. Bean 作用域

| 作用域 | 说明 | 适用 |
|--------|------|------|
| `singleton`（默认） | 容器内单例 | 无状态 Bean |
| `prototype` | 每次获取新实例 | 有状态 Bean |
| `request` | 每个 HTTP 请求一个 | Web |
| `session` | 每个会话一个 | Web |
| `application` | 整个 ServletContext 一个 | Web |
| `websocket` | WebSocket 会话一个 | WebSocket |

```java
@Component
@Scope("prototype")
public class MyBean {}

// 或
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MyBean {}
```

> ⚠️ **单例 Bean 不要有可变状态**，否则线程不安全。

### 7. 懒加载

```java
@Component
@Lazy
public class HeavyBean {}
```

或全局配置：
```java
@Configuration
@Lazy
public class AppConfig {}
```

> 💡 单例默认在容器启动时创建（预加载），`@Lazy` 改为首次使用时创建。

### 8. 生命周期回调

**方式一：JSR-250 注解（推荐）**
```java
@Component
public class MyBean {

    @PostConstruct
    public void init() {
        System.out.println("初始化");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("销毁");
    }
}
```

**方式二：Spring 接口**
```java
@Component
public class MyBean implements InitializingBean, DisposableBean {
    @Override
    public void afterPropertiesSet() {
        System.out.println("初始化");
    }

    @Override
    public void destroy() {
        System.out.println("销毁");
    }
}
```

**方式三：`@Bean` 的 initMethod/destroyMethod**
```java
@Bean(initMethod = "init", destroyMethod = "close")
public MyBean myBean() {
    return new MyBean();
}
```

**完整生命周期：**
```
构造器 → 属性注入 → @PostConstruct → InitializingBean.afterPropertiesSet → 自定义 init
→ 使用 → @PreDestroy → DisposableBean.destroy → 自定义 destroy
```

---

## 五、依赖注入注解

### 1. `@Autowired`

按**类型**自动注入，Spring 提供。

```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;   // 字段注入

    @Autowired
    public UserServiceImpl(UserDao userDao) {  // 构造器注入
        this.userDao = userDao;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {  // Setter 注入
        this.userDao = userDao;
    }
}
```

**注入规则：**
1. 按类型找唯一 Bean，直接注入
2. 找到多个同类型 Bean，按字段名/参数名匹配 Bean 名
3. 找不到，报 `NoSuchBeanDefinitionException`
4. `@Autowired(required = false)` 允许为 null

### 2. `@Qualifier`

当有多个同类型 Bean 时，指定 Bean 名。

```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    @Qualifier("userDaoMyBatis")
    private UserDao userDao;
}
```

**示例：**
```java
@Repository("userDaoJdbc")
public class UserDaoJdbcImpl implements UserDao {}

@Repository("userDaoMyBatis")
public class UserDaoMyBatisImpl implements UserDao {}

@Service
public class UserServiceImpl {
    @Autowired
    @Qualifier("userDaoMyBatis")
    private UserDao userDao;  // 注入 MyBatis 版本
}
```

### 3. `@Primary`

同类型多个 Bean 时，标记默认优先。

```java
@Repository
@Primary
public class UserDaoMyBatisImpl implements UserDao {}
```

`@Primary` 优先级低于 `@Qualifier`。

### 4. `@Resource`

JSR-250 提供，**默认按名称**注入，找不到再按类型。

```java
@Service
public class UserServiceImpl {
    @Resource(name = "userDaoMyBatis")
    private UserDao userDao;
}
```

**`@Autowired` vs `@Resource`：**

| 特性 | `@Autowired` | `@Resource` |
|------|--------------|-------------|
| 来源 | Spring | JSR-250 |
| 默认 | 按类型 | 按名称 |
| 可指定 | `@Qualifier` | `name` |
| 推荐 | Spring 项目 | 通用 |

### 5. `@Value`

注入普通值（字符串、数字、布尔）。

```java
@Component
public class AppConfig {

    @Value("Hello Spring")
    private String message;

    @Value("${jdbc.url}")       // 从配置文件读取
    private String jdbcUrl;

    @Value("${jdbc.maxPoolSize:10}")  // 默认值 10
    private int maxPoolSize;
}
```

**引入 properties 文件：**
```java
@Configuration
@PropertySource("classpath:db.properties")
public class AppConfig {

    @Value("${jdbc.url}")
    private String url;
}
```

> 💡 `@PropertySource` 默认不解析 `${}` 占位符？错，Spring 会自动处理。若在非 `@Configuration` 类中使用，需先注册 `PropertySourcesPlaceholderConfigurer`。

### 6. `@PropertySource` + `Environment`

```java
@Configuration
@PropertySource("classpath:db.properties")
public class AppConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env.getProperty("jdbc.url"));
        config.setUsername(env.getProperty("jdbc.username"));
        config.setPassword(env.getProperty("jdbc.password"));
        return new HikariDataSource(config);
    }
}
```

---

## 六、AOP 面向切面编程

### 1. 什么是 AOP

AOP（Aspect Oriented Programming）把**横切关注点**（日志、事务、权限、性能监控）从业务代码中抽离，通过**代理**在方法执行前后织入。

**没有 AOP：**
```java
public void save(User user) {
    long start = System.currentTimeMillis();
    System.out.println("开始 save");
    try {
        // 业务逻辑
        userDao.insert(user);
    } catch (Exception e) {
        System.out.println("异常：" + e.getMessage());
        throw e;
    } finally {
        System.out.println("结束，耗时：" + (System.currentTimeMillis() - start));
    }
}
```
每个方法都要写重复代码。

**有 AOP：**
```java
public void save(User user) {
    userDao.insert(user);   // 只写业务
}
// 日志、耗时、异常处理由切面统一处理
```

### 2. AOP 核心概念

| 概念 | 英文 | 说明 |
|------|------|------|
| 切面 | Aspect | 横切逻辑的模块（如日志切面） |
| 连接点 | JoinPoint | 可被拦截的点（方法调用） |
| 切点 | Pointcut | 匹配连接点的表达式 |
| 通知 | Advice | 切面执行的动作（前置/后置/环绕等） |
| 织入 | Weaving | 把切面应用到目标对象 |
| 目标对象 | Target | 被代理的对象 |
| 代理 | Proxy | 生成的代理对象 |

### 3. 通知类型

| 通知 | 注解 | 执行时机 |
|------|------|----------|
| 前置通知 | `@Before` | 方法执行前 |
| 后置通知 | `@After` | 方法执行后（无论异常） |
| 返回通知 | `@AfterReturning` | 方法正常返回后 |
| 异常通知 | `@AfterThrowing` | 方法抛异常后 |
| 环绕通知 | `@Around` | 方法前后（最强大） |

### 4. 快速开始

**依赖：**
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>6.1.6</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
    <version>6.1.6</version>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.21</version>
</dependency>
```

**开启 AOP：**
```java
@Configuration
@ComponentScan("com.example")
@EnableAspectJAutoProxy
public class AppConfig {}
```

**切面类：**
```java
package com.example.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {

    // 切点：匹配 UserService 所有方法
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void servicePointcut() {}

    @Before("servicePointcut()")
    public void before() {
        System.out.println(">>> 方法开始");
    }

    @AfterReturning(pointcut = "servicePointcut()", returning = "result")
    public void afterReturning(Object result) {
        System.out.println(">>> 方法返回：" + result);
    }

    @AfterThrowing(pointcut = "servicePointcut()", throwing = "ex")
    public void afterThrowing(Exception ex) {
        System.out.println(">>> 方法异常：" + ex.getMessage());
    }

    @After("servicePointcut()")
    public void after() {
        System.out.println(">>> 方法结束");
    }

    @Around("servicePointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String method = pjp.getSignature().getName();
        try {
            Object result = pjp.proceed();  // 执行目标方法
            System.out.println(">>> " + method + " 耗时：" + (System.currentTimeMillis() - start) + "ms");
            return result;
        } catch (Throwable t) {
            System.out.println(">>> " + method + " 异常：" + t.getMessage());
            throw t;
        }
    }
}
```

> ⚠️ `@Around` 必须调用 `pjp.proceed()`，否则目标方法不会执行。

### 5. 切点表达式

**`execution` 语法：**
```
execution(修饰符? 返回类型 包.类.方法(参数) 异常?)
```

**常用示例：**
```java
// service 包下所有方法
execution(* com.example.service.*.*(..))

// service 包及子包下所有方法
execution(* com.example.service..*.*(..))

// 所有 public 方法
execution(public * *(..))

// 以 find 开头的方法
execution(* com.example.service.*.find*(..))

// 返回 User 的方法
execution(com.example.entity.User com.example.service.*.*(..))

// 只有一个 Long 参数
execution(* com.example.service.*.*(Long))

// 任意参数
execution(* com.example.service.*.*(..))
```

**其他切点指示符：**
```java
// 匹配注解
@annotation(com.example.annotation.Log)

// 匹配指定类型的 Bean
@within(org.springframework.stereotype.Service)

// 匹配 Bean 名称
bean(*Service)

// 组合
execution(* com.example.service.*.*(..)) && @annotation(com.example.annotation.Log)
```

### 6. 获取方法信息

```java
@Before("servicePointcut()")
public void before(JoinPoint jp) {
    String methodName = jp.getSignature().getName();
    Object[] args = jp.getArgs();
    Object target = jp.getTarget();
    System.out.println("方法：" + methodName + "，参数：" + Arrays.toString(args));
}
```

### 7. 实战：自定义注解 + AOP 实现日志

**自定义注解：**
```java
package com.example.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String value() default "";
}
```

**切面：**
```java
@Aspect
@Component
public class LogAnnotationAspect {

    @Around("@annotation(logAnno)")
    public Object around(ProceedingJoinPoint pjp, Log logAnno) throws Throwable {
        long start = System.currentTimeMillis();
        String method = pjp.getSignature().toShortString();
        try {
            Object result = pjp.proceed();
            System.out.printf("[LOG] %s 耗时 %dms，描述：%s%n",
                    method, System.currentTimeMillis() - start, logAnno.value());
            return result;
        } catch (Throwable t) {
            System.out.printf("[LOG] %s 异常：%s%n", method, t.getMessage());
            throw t;
        }
    }
}
```

**使用：**
```java
@Service
public class UserServiceImpl implements UserService {

    @Log("查询用户")
    public User findById(Long id) {
        return userDao.findById(id);
    }
}
```

### 8. AOP 底层原理

**两种代理：**

| 代理 | 条件 | 说明 |
|------|------|------|
| JDK 动态代理 | 目标实现接口 | 基于接口生成代理 |
| CGLIB 代理 | 目标无接口 | 生成子类继承目标 |

Spring 默认：有接口用 JDK，无接口用 CGLIB。
Spring Boot 2.x+ 默认全部用 CGLIB。

**JDK 动态代理（简化）：**
```java
UserService proxy = (UserService) Proxy.newProxyInstance(
    target.getClass().getClassLoader(),
    target.getClass().getInterfaces(),
    (p, method, args) -> {
        System.out.println("before");
        Object result = method.invoke(target, args);
        System.out.println("after");
        return result;
    }
);
```

**CGLIB（简化）：**
```java
Enhancer enhancer = new Enhancer();
enhancer.setSuperclass(UserServiceImpl.class);
enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
    System.out.println("before");
    Object result = proxy.invokeSuper(obj, args);
    System.out.println("after");
    return result;
});
UserServiceImpl proxy = (UserServiceImpl) enhancer.create();
```

### 9. AOP 注意事项

| 问题 | 原因 | 解决 |
|------|------|------|
| 切面不生效 | 未加 `@EnableAspectJAutoProxy` | 加注解 |
| 切面不生效 | 目标未被 Spring 管理 | 加 `@Service` 等 |
| 自调用不生效 | 同类内方法调用绕过代理 | 注入自身或改设计 |
| `@Around` 不执行目标 | 未调用 `proceed()` | 必须调用 |
| 循环依赖 | 构造器互相注入 | 改 Setter 或 `@Lazy` |
| 代理类型转换异常 | JDK 代理只能转接口 | 用接口或改 CGLIB |

---

## 七、Spring 事务（概念引入）

Day13 会详细讲，这里先了解。

### 1. 编程式事务

```java
@Autowired
private TransactionTemplate transactionTemplate;

public void transfer() {
    transactionTemplate.execute(status -> {
        try {
            accountDao.deduct(1L, 100);
            accountDao.add(2L, 100);
            return null;
        } catch (Exception e) {
            status.setRollbackOnly();
            throw e;
        }
    });
}
```

### 2. 声明式事务（推荐）

```java
@Configuration
@EnableTransactionManagement
public class TxConfig {
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

@Service
public class UserServiceImpl {

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        accountDao.deduct(fromId, amount);
        accountDao.add(toId, amount);
    }
}
```

**`@Transactional` 属性：**

| 属性 | 说明 | 默认 |
|------|------|------|
| propagation | 传播行为 | REQUIRED |
| isolation | 隔离级别 | DEFAULT |
| timeout | 超时（秒） | -1 |
| readOnly | 只读 | false |
| rollbackFor | 回滚异常 | RuntimeException |
| noRollbackFor | 不回滚异常 | - |

**传播行为：**

| 传播 | 说明 |
|------|------|
| REQUIRED | 有则加入，无则新建（默认） |
| REQUIRES_NEW | 总是新建，挂起当前 |
| NESTED | 嵌套事务 |
| SUPPORTS | 有则加入，无则非事务 |
| NOT_SUPPORTED | 非事务执行 |
| MANDATORY | 必须存在事务，否则抛异常 |
| NEVER | 必须无事务，否则抛异常 |

**`@Transactional` 失效场景：**
- 方法非 public
- 自调用（同类内方法调用）
- 异常被 catch 未抛出
- 默认只回滚 `RuntimeException`，检查异常需 `rollbackFor = Exception.class`
- 数据库引擎不支持事务（如 MyISAM）

---

## 八、完整示例：Spring 管理 UserService

### 1. 依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>6.1.6</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-aspects</artifactId>
        <version>6.1.6</version>
    </dependency>
    <dependency>
        <groupId>org.aspectj</groupId>
        <artifactId>aspectjweaver</artifactId>
        <version>1.9.21</version>
    </dependency>
    <dependency>
        <groupId>jakarta.annotation</groupId>
        <artifactId>jakarta.annotation-api</artifactId>
        <version>2.1.1</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>2.0.13</version>
    </dependency>
</dependencies>
```

### 2. 代码结构

```
src/main/java/com/example/
├── AppConfig.java
├── entity/User.java
├── dao/
│   ├── UserDao.java
│   └── impl/UserDaoImpl.java
├── service/
│   ├── UserService.java
│   └── impl/UserServiceImpl.java
└── aspect/LogAspect.java
```

### 3. AppConfig

```java
package com.example;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan("com.example")
@EnableAspectJAutoProxy
@PropertySource("classpath:app.properties")
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
```

### 4. UserDaoImpl

```java
package com.example.dao.impl;

import com.example.dao.UserDao;
import com.example.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl implements UserDao {

    @Override
    public User findById(Long id) {
        System.out.println("查询数据库 id=" + id);
        return new User(id, "张三");
    }
}
```

### 5. UserServiceImpl

```java
package com.example.service.impl;

import com.example.dao.UserDao;
import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Value("${app.name:Demo}")
    private String appName;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User findById(Long id) {
        System.out.println("应用名：" + appName);
        return userDao.findById(id);
    }
}
```

### 6. LogAspect

```java
package com.example.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {

    @Around("execution(* com.example.service..*.*(..))")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String method = pjp.getSignature().toShortString();
        try {
            Object result = pjp.proceed();
            System.out.printf("[AOP] %s 耗时 %dms%n", method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            System.out.printf("[AOP] %s 异常：%s%n", method, t.getMessage());
            throw t;
        }
    }
}
```

### 7. 测试

```java
public class Main {
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        UserService service = ctx.getBean(UserService.class);
        User user = service.findById(1L);
        System.out.println(user);
        ((AnnotationConfigApplicationContext) ctx).close();
    }
}
```

**输出：**
```
应用名：Demo
查询数据库 id=1
[AOP] UserServiceImpl.findById(..) 耗时 5ms
User{id=1, name='张三'}
```

### 8. app.properties

```properties
app.name=用户管理系统
```

---

## 九、常见问题与排错

| 问题 | 原因 | 解决 |
|------|------|------|
| `NoSuchBeanDefinitionException` | Bean 未注册或包未扫描 | 加 `@Component` 或 `@ComponentScan` |
| `NoUniqueBeanDefinitionException` | 同类型多个 Bean | 用 `@Qualifier` 或 `@Primary` |
| 循环依赖 | A 依赖 B，B 依赖 A | 改 Setter 或 `@Lazy` |
| AOP 不生效 | 未开启 `@EnableAspectJAutoProxy` | 加注解 |
| AOP 自调用不生效 | 同类内方法调用 | 注入自身或改设计 |
| `@Value` 取不到值 | 未配 `@PropertySource` | 加注解或 `PropertySourcesPlaceholderConfigurer` |
| 构造器注入循环依赖 | 无法延迟 | 改 Setter 注入 |
| `@Transactional` 不生效 | 方法非 public 或自调用 | 改为 public，拆分方法 |
| Bean 名冲突 | 同名类 | 显式指定 Bean 名 |

---

## 十、实践练习

### 练习 1：搭建 Spring 容器
1. 用注解配置 `AppConfig`
2. 定义 `UserDao`、`UserService` 接口和实现
3. 用 `@Service`、`@Repository` 注册
4. 从容器获取 Bean 并调用

### 练习 2：依赖注入
1. 用构造器注入 `UserDao` 到 `UserService`
2. 用 `@Value` 注入配置
3. 用 `@Qualifier` 解决多实现冲突
4. 对比字段注入和构造器注入

### 练习 3：Bean 管理
1. 测试单例和原型作用域
2. 用 `@PostConstruct` / `@PreDestroy` 观察生命周期
3. 用 `@Lazy` 测试懒加载

### 练习 4：AOP 日志
1. 编写切面，拦截 Service 层方法
2. 输出方法名、参数、返回值、耗时
3. 用 `@Around` 实现统一异常处理

### 练习 5：自定义注解 + AOP
1. 定义 `@Log` 注解
2. 编写切面拦截 `@Log` 方法
3. 记录操作日志

### 练习 6（进阶）：改造 Day9 项目
1. 把 MyBatis 的 UserMapper 注册为 Spring Bean
2. UserService 用 Spring 管理
3. 用 Spring 事务管理 Service 方法
4. 为 Day13 SSM 整合做准备

---

## 十一、总结与拓展

### 今日总结
- **IoC**：对象创建交给容器；**DI**：容器注入依赖
- **三种注入方式**：构造器（推荐）、Setter、字段
- **容器**：`ApplicationContext`（`AnnotationConfigApplicationContext`）
- **Bean 声明**：`@Component` / `@Service` / `@Repository` / `@Controller` / `@Bean`
- **Bean 作用域**：singleton（默认）、prototype、request、session
- **生命周期**：`@PostConstruct` / `@PreDestroy`
- **依赖注入**：`@Autowired`（按类型）、`@Qualifier`（按名）、`@Resource`（按名）、`@Value`
- **AOP**：切面、切点、通知（`@Before`、`@After`、`@Around` 等）
- **AOP 原理**：JDK 动态代理（有接口）、CGLIB（无接口）
- **声明式事务**：`@Transactional` + `@EnableTransactionManagement`（Day13 详解）

### 核心记忆点
```
配置类：@Configuration + @ComponentScan + @PropertySource
声明 Bean：@Service / @Repository / @Controller / @Bean
注入：@Autowired（类型）→ @Qualifier（名字）
读配置：@Value("${key:default}")
生命周期：@PostConstruct / @PreDestroy
作用域：@Scope("prototype")
AOP 开启：@EnableAspectJAutoProxy
切面：@Aspect + @Component
切点：execution(* com.example.service.*.*(..))
环绕：@Around，必须 pjp.proceed()
事务：@Transactional（Day13 详解）
```

### 拓展阅读
- [Spring 官方文档](https://docs.spring.io/spring-framework/reference/)
- [Spring IoC 容器](https://docs.spring.io/spring-framework/reference/core/beans.html)
- [Spring AOP](https://docs.spring.io/spring-framework/reference/core/aop.html)
- [《Spring 实战》](https://book