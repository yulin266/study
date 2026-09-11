# 第2周 

## 学习目标

- 理解 SSM 整合的思路与关键点
- 掌握 Spring 管理 MyBatis（SqlSessionFactory、Mapper 扫描）
- 掌握声明式事务 `@Transactional` 的配置与实战
- 掌握 `@Transactional` 的传播行为与失效场景
- 完成 SSM 项目分层架构
- 编写 RESTful API + JSP 视图
- 为 Day14 博客系统打基础

---

## 一、SSM 整合概述

### 1. 什么是 SSM

SSM = **Spring + Spring MVC + MyBatis**，是经典的 Java Web 开发组合。

| 框架 | 职责 |
|------|------|
| Spring | IoC 容器、DI、AOP、事务管理 |
| Spring MVC | Web 层，处理请求响应 |
| MyBatis | 数据访问层，SQL 映射 |

**整合目标：**
- Spring 统一管理所有 Bean（Service、DAO、Controller）
- MyBatis 的 SqlSessionFactory、Mapper 交给 Spring
- 事务由 Spring 声明式管理（`@Transactional`）
- 消除手动 `openSession` / `commit` / `rollback`

### 2. 整合思路

```
Spring MVC（Controller）
    ↓ 注入
Spring（Service）→ @Transactional 事务
    ↓ 注入
Spring 管理的 Mapper（MyBatis）
    ↓
DataSource（HikariCP）
    ↓
MySQL
```

**关键整合点：**
1. Spring 管理 `DataSource`
2. Spring 管理 `SqlSessionFactory`
3. Spring 扫描 Mapper 接口，生成代理
4. Spring 管理事务管理器
5. `@Transactional` 声明事务

### 3. 依赖

```xml
<properties>
    <spring.version>6.1.6</spring.version>
    <mybatis.version>3.5.16</mybatis.version>
    <mybatis-spring.version>3.0.3</mybatis-spring.version>
    <mysql.version>8.3.0</mysql.version>
    <hikari.version>5.1.0</hikari.version>
    <servlet.version>6.0.0</servlet.version>
    <jstl.version>3.0.0</jstl.version>
    <jackson.version>2.17.0</jackson.version>
    <pagehelper.version>6.1.0</pagehelper.version>
    <slf4j.version>2.0.13</slf4j.version>
    <validation.version>8.0.1.Final</validation.version>
    <el.version>4.0.2</el.version>
    <annotation.version>2.1.1</annotation.version>
</properties>

<dependencies>
    <!-- Spring 核心 -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>${spring.version}</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>${spring.version}</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-jdbc</artifactId>
        <version>${spring.version}</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-tx</artifactId>
        <version>${spring.version}</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-aspects</artifactId>
        <version>${spring.version}</version>
    </dependency>

    <!-- MyBatis -->
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis</artifactId>
        <version>${mybatis.version}</version>
    </dependency>
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis-spring</artifactId>
        <version>${mybatis-spring.version}</version>
    </dependency>

    <!-- 数据库 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>${mysql.version}</version>
    </dependency>
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>${hikari.version}</version>
    </dependency>

    <!-- PageHelper 分页 -->
    <dependency>
        <groupId>com.github.pagehelper</groupId>
        <artifactId>pagehelper</artifactId>
        <version>${pagehelper.version}</version>
    </dependency>

    <!-- Servlet / JSP / JSTL -->
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
        <version>${servlet.version}</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>jakarta.servlet.jsp.jstl</groupId>
        <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
        <version>${jstl.version}</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish.web</groupId>
        <artifactId>jakarta.servlet.jsp.jstl</artifactId>
        <version>3.0.1</version>
    </dependency>

    <!-- Jackson -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>${jackson.version}</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>${jackson.version}</version>
    </dependency>

    <!-- 校验 -->
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>${validation.version}</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish</groupId>
        <artifactId>jakarta.el</artifactId>
        <version>${el.version}</version>
    </dependency>

    <!-- 注解 API -->
    <dependency>
        <groupId>jakarta.annotation</groupId>
        <artifactId>jakarta.annotation-api</artifactId>
        <version>${annotation.version}</version>
    </dependency>

    <!-- 日志 -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>${slf4j.version}</version>
    </dependency>
</dependencies>
```

### 4. 项目结构

```
ssm-demo/
├── pom.xml
└── src/main/
    ├── java/com/example/
    │   ├── config/
    │   │   ├── WebAppInitializer.java
    │   │   ├── RootConfig.java         Spring 核心（Service、DAO、事务）
    │   │   └── WebConfig.java          Spring MVC（Controller、视图、拦截器）
    │   ├── entity/User.java
    │   ├── dao/UserMapper.java
    │   ├── service/
    │   │   ├── UserService.java
    │   │   └── impl/UserServiceImpl.java
    │   ├── controller/
    │   │   ├── UserController.java      JSP 视图
    │   │   └── UserApiController.java   RESTful
    │   ├── common/Result.java
    │   ├── exception/BusinessException.java
    │   ├── handler/GlobalExceptionHandler.java
    │   └── interceptor/LoginInterceptor.java
    ├── resources/
    │   ├── db.properties
    │   └── mapper/UserMapper.xml
    └── webapp/
        ├── WEB-INF/views/
        │   ├── login.jsp
        │   ├── user-list.jsp
        │   └── user-form.jsp
        └── static/css/style.css
```

---

## 二、RootConfig（Spring + MyBatis + 事务）

RootConfig 是整合的核心，负责 DataSource、SqlSessionFactory、Mapper 扫描、事务管理。

```java
package com.example.config;

import com.github.pagehelper.PageInterceptor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(
        basePackages = "com.example",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = org.springframework.stereotype.Controller.class
        )
)
@MapperScan("com.example.dao")
@EnableTransactionManagement
@PropertySource("classpath:db.properties")
public class RootConfig {

    @Value("${jdbc.driver}")
    private String driver;

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

    @Value("${jdbc.maxPoolSize:10}")
    private int maxPoolSize;

    @Value("${jdbc.minIdle:5}")
    private int minIdle;

    /** 处理 ${} 占位符 */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /** 数据源 */
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("SSMPool");
        return new HikariDataSource(config);
    }

    /** SqlSessionFactory */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        // 类型别名包
        factory.setTypeAliasesPackage("com.example.entity");

        // Mapper XML 位置
        factory.setMapperLocations(
                new org.springframework.core.io.support.PathMatchingResourcePatternResolver()
                        .getResources("classpath:mapper/*.xml")
        );

        // MyBatis 全局设置
        org.apache.ibatis.session.Configuration configuration =
                new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(true);
        configuration.setLazyLoadingEnabled(true);
        configuration.setAggressiveLazyLoading(false);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        factory.setConfiguration(configuration);

        // 分页插件
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties props = new Properties();
        props.setProperty("helperDialect", "mysql");
        props.setProperty("reasonable", "true");
        pageInterceptor.setProperties(props);
        factory.setPlugins(new Interceptor[]{pageInterceptor});

        return factory.getObject();
    }

    /** 事务管理器 */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
```

**关键点：**
- `@MapperScan("com.example.dao")`：自动扫描 Mapper 接口，生成代理 Bean
- `@EnableTransactionManagement`：开启声明式事务
- `@PropertySource` + `PropertySourcesPlaceholderConfigurer`：读取 properties
- `SqlSessionFactoryBean`：Spring 提供的工厂 Bean
- `setMapperLocations`：指定 Mapper XML 位置
- `DataSourceTransactionManager`：事务管理器

---

## 三、WebConfig（Spring MVC）

```java
package com.example.config;

import com.example.interceptor.LoginInterceptor;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

@Configuration
@EnableWebMvc
@ComponentScan("com.example.controller")
public class WebConfig implements WebMvcConfigurer {

    /** 视图解析器 */
    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    /** 静态资源 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/");
    }

    /** JSON 转换器（支持 LocalDateTime 格式化） */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                .indentOutput(false);
        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
    }

    /** 拦截器 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login", "/logout",
                        "/static/**",
                        "/api/public/**"
                );
    }
}
```

---

## 四、WebAppInitializer

```java
package com.example.config;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import jakarta.servlet.Filter;

public class WebAppInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{RootConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    /** 注册字符编码 Filter */
    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return new Filter[]{filter};
    }
}
```

---

## 五、db.properties

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/user_mgmt?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=123456
jdbc.maxPoolSize=10
jdbc.minIdle=5
```

---

## 六、Entity

```java
package com.example.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.*;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度 3-20")
    private String username;

    private String password;

    @Size(max = 50, message = "昵称最长 50")
    private String nickname;

    @Min(value = 1, message = "年龄最小 1")
    @Max(value = 150, message = "年龄最大 150")
    private Integer age;

    @Email(message = "邮箱格式错误")
    private String email;

    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // getter/setter
}
```

---

## 七、Mapper（DAO）

### 1. UserMapper 接口

```java
package com.example.dao;

import com.example.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    User findById(Long id);

    User findByUsername(String username);

    List<User> search(@Param("keyword") String keyword,
                      @Param("status") Integer status);

    long countByCondition(@Param("keyword") String keyword,
                          @Param("status") Integer status);

    boolean existsUsername(@Param("username") String username,
                           @Param("excludeId") Long excludeId);

    int insert(User user);

    int update(User user);

    int deleteById(Long id);

    int deleteBatch(@Param("ids") List<Long> ids);
}
```

### 2. UserMapper.xml

放在 `src/main/resources/mapper/`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.dao.UserMapper">

    <sql id="baseColumns">
        id, username, password, nickname, age, email, phone, status, create_time, update_time
    </sql>

    <resultMap id="userResultMap" type="User">
        <id column="id" property="id"/>
        <result column="username" property="username"/>
        <result column="password" property="password"/>
        <result column="nickname" property="nickname"/>
        <result column="age" property="age"/>
        <result column="email" property="email"/>
        <result column="phone" property="phone"/>
        <result column="status" property="status"/>
        <result column="create_time" property="createTime"/>
        <result column="update_time" property="updateTime"/>
    </resultMap>

    <select id="findById" resultMap="userResultMap">
        SELECT <include refid="baseColumns"/> FROM sys_user WHERE id = #{id}
    </select>

    <select id="findByUsername" resultMap="userResultMap">
        SELECT <include refid="baseColumns"/> FROM sys_user WHERE username = #{username}
    </select>

    <select id="search" resultMap="userResultMap">
        SELECT <include refid="baseColumns"/> FROM sys_user
        <where>
            <if test="keyword != null and keyword != ''">
                AND (username LIKE CONCAT('%', #{keyword}, '%')
                     OR nickname LIKE CONCAT('%', #{keyword}, '%')
                     OR email LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
        </where>
        ORDER BY id DESC
    </select>

    <select id="countByCondition" resultType="long">
        SELECT COUNT(*) FROM sys_user
        <where>
            <if test="keyword != null and keyword != ''">
                AND (username LIKE CONCAT('%', #{keyword}, '%')
                     OR nickname LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
        </where>
    </select>

    <select id="existsUsername" resultType="boolean">
        SELECT COUNT(*) > 0 FROM sys_user WHERE username = #{username}
        <if test="excludeId != null">
            AND id != #{excludeId}
        </if>
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO sys_user(username, password, nickname, age, email, phone, status)
        VALUES(#{username}, #{password}, #{nickname}, #{age}, #{email}, #{phone}, #{status})
    </insert>

    <update id="update">
        UPDATE sys_user
        <set>
            <if test="nickname != null">nickname = #{nickname},</if>
            <if test="age != null">age = #{age},</if>
            <if test="email != null">email = #{email},</if>
            <if test="phone != null">phone = #{phone},</if>
            <if test="status != null">status = #{status},</if>
        </set>
        WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM sys_user WHERE id = #{id}
    </delete>

    <delete id="deleteBatch">
        DELETE FROM sys_user WHERE id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
    </delete>

</mapper>
```

> 💡 **使用 `@MapperScan` 后，无需在 mybatis-config.xml 中配置 `<mappers>`**，Spring 会自动加载接口对应 XML。

---

## 八、Service 层（含事务）

### 1. UserService 接口

```java
package com.example.service;

import com.example.common.PageResult;
import com.example.entity.User;

import java.util.List;

public interface UserService {

    User login(String username, String password);

    User findById(Long id);

    PageResult<User> findByPage(String keyword, Integer status, int page, int size);

    void save(User user);

    void update(User user);

    void delete(Long id);

    void deleteBatch(List<Long> ids);

    void transfer(Long fromId, Long toId, java.math.BigDecimal amount);
}
```

### 2. UserServiceImpl

```java
package com.example.service.impl;

import com.example.common.PageResult;
import com.example.dao.UserMapper;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.service.UserService;
import com.example.util.MD5Util;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) return null;
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!MD5Util.matches(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public PageResult<User> findByPage(String keyword, Integer status, int page, int size) {
        PageHelper.startPage(page, size);
        List<User> list = userMapper.search(keyword, status);
        PageInfo<User> info = new PageInfo<>(list);
        return PageResult.of(info);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(User user) {
        if (userMapper.existsUsername(user.getUsername(), null)) {
            throw new BusinessException("用户名已存在");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        user.setPassword(MD5Util.encrypt(user.getPassword()));
        if (user.getStatus() == null) user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User user) {
        User exist = userMapper.findById(user.getId());
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.update(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("管理员账号不可删除");
        }
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        User admin = userMapper.findByUsername("admin");
        if (admin != null) {
            ids.removeIf(id -> id.equals(admin.getId()));
        }
        if (!ids.isEmpty()) {
            userMapper.deleteBatch(ids);
        }
    }

    /** 事务示例：转账（模拟） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        // 扣款
        // userMapper.deduct(fromId, amount);
        // 入账
        // userMapper.add(toId, amount);
        // 若中间抛异常，自动回滚
    }
}
```

**关键点：**
- `@Service` 注册为 Bean
- 构造器注入 `UserMapper`
- `@Transactional(rollbackFor = Exception.class)` 保证任何异常都回滚
- 无需手动 `SqlSession`，Spring 自动管理

---

## 九、Controller 层

### 1. UserApiController（RESTful）

```java
package com.example.controller;

import com.example.common.PageResult;
import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    @Autowired
    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<PageResult<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.findByPage(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.findById(id);
        return user != null ? Result.success(user) : Result.error(404, "用户不存在");
    }

    @PostMapping
    public Result<User> create(@RequestBody @Valid User user) {
        userService.save(user);
        return Result.success(user);
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody @Valid User user) {
        user.setId(id);
        userService.update(user);
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }
}
```

### 2. UserController（JSP 视图）

```java
package com.example.controller;

import com.example.common.PageResult;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        PageResult<User> pageResult = userService.findByPage(keyword, status, page, 10);
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "user-list";
    }

    @GetMapping("/add")
    public String addForm() {
        return "user-form";
    }

    @GetMapping("/edit")
    public String editForm(@RequestParam Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        return "user-form";
    }

    @PostMapping("/save")
    public String save(User user, Model model) {
        try {
            userService.save(user);
            return "redirect:/users";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "user-form";
        }
    }

    @PostMapping("/update")
    public String update(User user) {
        userService.update(user);
        return "redirect:/users";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam Long id) {
        userService.delete(id);
        return "redirect:/users";
    }
}
```

### 3. LoginController

```java
package com.example.controller;

import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.login(username, password);
            if (user == null) {
                model.addAttribute("error", "用户名或密码错误");
                model.addAttribute("username", username);
                return "login";
            }
            session.setAttribute("loginUser", user);
            return "redirect:/users";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
```

---

## 十、声明式事务详解

### 1. `@Transactional` 属性

```java
@Transactional(
    propagation = Propagation.REQUIRED,
    isolation = Isolation.DEFAULT,
    timeout = 30,
    readOnly = false,
    rollbackFor = Exception.class,
    noRollbackFor = BusinessException.class
)
public void doBusiness() { }
```

| 属性 | 说明 | 默认值 |
|------|------|--------|
| propagation | 传播行为 | REQUIRED |
| isolation | 隔离级别 | DEFAULT |
| timeout | 超时（秒） | -1（无限制） |
| readOnly | 只读 | false |
| rollbackFor | 触发回滚的异常 | RuntimeException |
| noRollbackFor | 不回滚的异常 | - |

### 2. 传播行为

| 传播 | 说明 |
|------|------|
| **REQUIRED**（默认） | 有则加入，无则新建 |
| **REQUIRES_NEW** | 总是新建，挂起当前 |
| **NESTED** | 嵌套事务（可部分回滚） |
| SUPPORTS | 有则加入，无则非事务 |
| NOT_SUPPORTED | 非事务执行 |
| MANDATORY | 必须有事务，否则抛异常 |
| NEVER | 必须无事务，否则抛异常 |

**示例：**
```java
@Service
public class OrderService {

    @Autowired
    private LogService logService;

    @Transactional
    public void createOrder(Order order) {
        orderMapper.insert(order);
        // 无论 createOrder 是否回滚，日志都要记录
        logService.saveLog("创建订单：" + order.getId());
    }
}

@Service
public class LogService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(String msg) {
        logMapper.insert(new Log(msg));
    }
}
```

### 3. 隔离级别

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
```

| 级别 | 说明 |
|------|------|
| DEFAULT | 用数据库默认（MySQL 是 REPEATABLE_READ） |
| READ_UNCOMMITTED | 读未提交 |
| READ_COMMITTED | 读已提交 |
| REPEATABLE_READ | 可重复读 |
| SERIALIZABLE | 串行化 |

### 4. `@Transactional` 失效场景

**（1）方法非 public**
```java
@Transactional
private void doSomething() { }  // 不生效
```

**（2）自调用（同类内方法调用）**
```java
@Service
public class UserService {

    public void a() {
        b();  // 直接调用，不走代理，事务失效
    }

    @Transactional
    public void b() { }
}
```

**解决：**
```java
// 方式一：注入自身
@Autowired
private UserService self;

public void a() {
    self.b();  // 走代理
}

// 方式二：拆分到不同类
// 方式三：用 AopContext.currentProxy()
```

**（3）异常被 catch 未抛出**
```java
@Transactional
public void save() {
    try {
        // 出错
    } catch (Exception e) {
        e.printStackTrace();  // 吞了异常，事务不回滚
    }
}
```

**解决：** 抛出或 `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()`

**（4）默认只回滚 RuntimeException**
```java
@Transactional
public void save() throws IOException {
    throw new IOException();  // 检查异常，默认不回滚
}
```
**解决：** `@Transactional(rollbackFor = Exception.class)`

**（5）数据库引擎不支持事务**
- MySQL MyISAM 不支持事务，改用 InnoDB

**（6）未开启事务管理**
- 缺少 `@EnableTransactionManagement` 或事务管理器 Bean

### 5. 只读事务

```java
@Transactional(readOnly = true)
public List<User> findAll() {
    return userMapper.findAll();
}
```

**作用：**
- 优化性能（只读提示数据库）
- 防止误写
- 一些数据库可路由到从库

### 6. 事务超时

```java
@Transactional(timeout = 10)  // 超过 10 秒回滚
public void longBusiness() { }
```

---

## 十一、全局异常与统一返回

### 1. Result

```java
package com.example.common;

public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200; r.msg = "success"; r.data = data;
        return r;
    }

    public static <T> Result<T> success() { return success(null); }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.code = code; r.msg = msg;
        return r;
    }

    public static <T> Result<T> error(String msg) { return error(500, msg); }

    // getter/setter
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
```

### 2. PageResult

```java
package com.example.common;

import com.github.pagehelper.PageInfo;
import java.util.List;

public class PageResult<T> {
    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;

    public static <T> PageResult<T> of(PageInfo<T> info) {
        PageResult<T> r = new PageResult<>();
        r.list = info.getList();
        r.total = info.getTotal();
        r.pageNum = info.getPageNum();
        r.pageSize = info.getPageSize();
        r.pages = info.getPages();
        return r;
    }

    // getter/setter
}
```

### 3. BusinessException

```java
package com.example.exception;

public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        this(500, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }
}
```

### 4. GlobalExceptionHandler

```java
package com.example.handler;

import com.example.common.Result;
import com.example.exception.BusinessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(500, "系统异常，请联系管理员");
    }
}
```

---

## 十二、拦截器

```java
package com.example.interceptor;

import jakarta.servlet.http.*;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (uri.startsWith("/static/")) return true;

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            if (isAjax(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"未登录\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return false;
        }
        return true;
    }

    private boolean isAjax(HttpServletRequest request) {
        String xhr = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equals(xhr)
                || (accept != null && accept.contains("application/json"));
    }
}
```

---

## 十三、测试

### 1. 启动 Tomcat

配置 IDEA 的 Tomcat，部署 `ssm-demo:war exploded`，启动。

### 2. Postman 测试

**登录：**
```
POST http://localhost:8080/login
Content-Type: application/x-www-form-urlencoded

username=admin&password=123456
```

**查询列表：**
```
GET http://localhost:8080/api/users?page=1&size=5
```

**新增：**
```
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "username": "newuser",
  "password": "123456",
  "nickname": "新用户",
  "age": 25,
  "email": "new@example.com"
}
```

**校验失败：**
```
POST http://localhost:8080/api/users
Content-Type: application/json

{"username": "a"}

响应：{"code":400,"msg":"用户名长度 3-20","data":null}
```

### 3. 事务验证

在 `save` 中模拟异常（如插入后抛异常），观察数据库未插入。

---

## 十四、常见问题与排错

| 问题 | 原因 | 解决 |
|------|------|------|
| Mapper 注入失败 | 未 `@MapperScan` | 加注解 |
| `Invalid bound statement` | XML 位置或 namespace 错 | 检查 `setMapperLocations` 和 namespace |
| 事务不生效 | 未 `@EnableTransactionManagement` | 加注解 |
| 事务不生效 | 方法非 public 或自调用 | 改为 public，拆分方法 |
| 事务不回滚 | 异常被 catch | 抛出或 `setRollbackOnly` |
| 检查异常不回滚 | 默认只回滚 RuntimeException | `rollbackFor = Exception.class` |
| 数据源初始化失败 | 配置错误 | 检查 db.properties |
| 中文乱码 | 编码不一致 | 加 CharacterEncodingFilter |
| JSON 日期格式错 | 未配 Jackson | 加 `jackson-datatype-jsr310` |
| 静态资源 404 | DispatcherServlet 拦截 | `addResourceHandlers` |
| 循环依赖 | 构造器互相注入 | 改 Setter 或 `@Lazy` |
| PageHelper 不生效 | startPage 后未紧跟查询 | 确保中间无其他查询 |

---

## 十五、实践练习

### 练习 1：搭建 SSM 环境
1. 按本文档配置 RootConfig、WebConfig、WebAppInitializer
2. 创建 User 实体、UserMapper、UserMapper.xml
3. 启动 Tomcat，验证无报错

### 练习 2：CRUD + 事务
1. 实现 UserService 的增删改查
2. 用 `@Transactional` 保证事务
3. 在 save 中故意抛异常，验证回滚

### 练习 3：RESTful API
1. 编写 UserApiController
2. 统一返回 Result
3. Postman 测试所有接口

### 练习 4：参数校验
1. 实体加校验注解
2. Controller 用 `@Valid`
3. 全局异常处理校验失败

### 练习 5：拦截器 + 登录
1. 编写 LoginController
2. 编写 LoginInterceptor
3. 未登录拦截，登录后放行

### 练习 6：事务进阶
1. 实现转账方法
2. 测试传播行为（REQUIRED vs REQUIRES_NEW）
3. 测试只读事务

### 练习 7（进阶）：完整重构 Day7
1. 用 SSM 重构 Day7 用户管理系统
2. 用 `@Controller` 替代 Servlet
3. 用 `@Transactional` 替代手动事务
4. 用 MyBatis 替代 JDBC

---

## 十六、总结与拓展

### 今日总结
- **SSM 整合关键点**：
  - Spring 管理 DataSource（HikariCP）
  - Spring 管理 SqlSessionFactory（`SqlSessionFactoryBean`）
  - `@MapperScan` 自动扫描 Mapper
  - `@EnableTransactionManagement` 开启事务
  - `DataSourceTransactionManager` 事务管理器
- **配置类**：
  - `RootConfig`：Service、DAO、事务
  - `WebConfig`：Controller、视图、拦截器
  - `WebAppInitializer`：替代 web.xml
- **声明式事务**：
  - `@Transactional` 加在 Service 层
  - 传播行为、隔离级别、回滚规则
  - 失效场景：非 public、自调用、异常被吞、检查异常
- **分层架构**：
  - Controller（`@Controller` / `@RestController`）
  - Service（`@Service` + `@Transactional`）
  - DAO（Mapper 接口 + XML）
  - Entity（POJO）

### 核心记忆点
```
RootConfig：
  @Configuration
  @ComponentScan（排除 Controller）
  @MapperScan("com.example.dao")
  @EnableTransactionManagement
  @PropertySource("classpath:db.properties")
  @Bean DataSource（HikariCP）
  @Bean SqlSessionFactory（SqlSessionFactoryBean + PageInterceptor）
  @Bean PlatformTransactionManager（DataSourceTransactionManager）

WebConfig：
  @Configuration + @EnableWebMvc + @ComponentScan("controller")
  ViewResolver + ResourceHandler + Interceptor + MessageConverter

事务：
  @Transactional(rollbackFor = Exception.class)
  加在 Service 层 public 方法
  异常必须抛出，不能吞

失效：
  非 public / 自调用 / 异常被吞 / 检查异常 / 未开启事务管理
```

### 拓展阅读
- [MyBatis-Spring 官方文档](https://mybatis.org/spring/zh/index.html)
- [Spring 事务管理](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [PageHelper 文档](https://pagehelper.github.io/)
- [Spring 声明式事务](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative.html)

### 明日预告
Day14 是**综合项目：博客系统**，用 SSM 完整实现：
- 用户注册/登录
- 文章 CRUD + 分页 + 搜索
- 分类、标签、评论
- 关联查询（文章 + 作者 + 分类 + 标签）
- RESTful API + JSP 视图
- 事务管理、全局异常、拦截器
- 完整的前后端交互

两周课程收官，将把所有知识点串联成一个可运行的项目。