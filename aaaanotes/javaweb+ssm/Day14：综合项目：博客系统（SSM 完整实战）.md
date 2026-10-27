# 第2周 

## 项目目标

用 **SSM（Spring + Spring MVC + MyBatis）** 完整实现一个博客系统，整合两周所学全部知识点，形成可运行、可演示、可扩展的完整项目。

**涉及知识点：**
- HTTP、RESTful、JSON（Day1）
- Servlet 原理、请求响应（Day2-3）
- JSP、EL、JSTL、MVC（Day4）
- JDBC、连接池（Day5）
- MySQL 建表、索引、分页（Day6）
- 综合 CRUD（Day7）
- Maven 多模块、分层架构（Day8）
- MyBatis 基础与进阶（Day9-10）
- Spring IoC/DI/AOP（Day11）
- Spring MVC（Day12）
- SSM 整合、声明式事务（Day13）

---

## 一、需求分析

### 1. 功能需求

| 模块 | 功能 |
|------|------|
| 用户 | 注册、登录、注销、修改密码、查看个人主页 |
| 文章 | 发布、编辑、删除、列表、详情、搜索、分页 |
| 分类 | 新增、编辑、删除、按分类筛选文章 |
| 标签 | 新增、删除、按标签筛选文章（多对多） |
| 评论 | 发表评论、回复评论、删除评论、评论列表 |
| 点赞 | 点赞/取消点赞、点赞数统计 |
| 首页 | 热门文章、最新文章、分类导航、标签云 |
| 后台 | 用户管理、文章管理、分类管理、评论管理 |

### 2. 非功能需求

- RESTful API + JSP 双模式
- 分层架构（Controller / Service / DAO / Entity）
- 声明式事务
- 统一返回 `Result<T>`
- 全局异常处理
- 登录拦截
- 参数校验
- 分页查询
- 防 SQL 注入、防 XSS

### 3. 页面清单

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | `/` | 文章列表 + 分类 + 标签 |
| 文章详情 | `/article/{id}` | 文章内容 + 评论 |
| 登录 | `/login` | 登录表单 |
| 注册 | `/register` | 注册表单 |
| 个人主页 | `/user/{id}` | 用户信息 + 文章列表 |
| 写文章 | `/article/write` | 发布文章（需登录） |
| 后台首页 | `/admin` | 数据统计 |
| 后台文章管理 | `/admin/articles` | 文章 CRUD |
| 后台分类管理 | `/admin/categories` | 分类 CRUD |
| 后台评论管理 | `/admin/comments` | 评论审核 |

---

## 二、数据库设计

### 1. 建表 SQL

```sql
CREATE DATABASE IF NOT EXISTS blog
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE blog;

-- 用户表
CREATE TABLE `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(64) NOT NULL,
    `nickname` VARCHAR(50),
    `avatar` VARCHAR(255),
    `email` VARCHAR(100),
    `bio` VARCHAR(255) COMMENT '个人简介',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
    `status` TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(255),
    `sort` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 标签表
CREATE TABLE `tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 文章表
CREATE TABLE `article` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `summary` VARCHAR(500),
    `cover` VARCHAR(255),
    `user_id` BIGINT NOT NULL,
    `category_id` BIGINT,
    `views` INT DEFAULT 0,
    `likes` INT DEFAULT 0,
    `comment_count` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1 COMMENT '1发布 0草稿 2删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status_create` (`status`, `create_time`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 文章标签关联
CREATE TABLE `article_tag` (
    `article_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    PRIMARY KEY (`article_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联';

-- 评论表
CREATE TABLE `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论 ID',
    `content` VARCHAR(1000) NOT NULL,
    `status` TINYINT DEFAULT 1 COMMENT '1正常 0删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_article_id` (`article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 点赞记录表
CREATE TABLE `like_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `article_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录';

-- 初始化数据
INSERT INTO `sys_user` (username, password, nickname, role, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 'ADMIN', 1),
('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三', 'USER', 1),
('lisi', 'e10adc3949ba59abbe56e057f20f883e', '李四', 'USER', 1);

INSERT INTO `category` (name, description, sort) VALUES
('Java', 'Java 相关技术', 1),
('数据库', 'MySQL、Redis 等', 2),
('前端', 'HTML、CSS、JS、Vue', 3),
('算法', '数据结构与算法', 4);

INSERT INTO `tag` (name) VALUES ('Java'), ('Spring'), ('MySQL'), ('Vue'), ('算法');

INSERT INTO `article` (title, content, summary, user_id, category_id, status) VALUES
('Spring IoC 详解', 'IoC 是 Spring 的核心...', 'Spring IoC 入门', 1, 1, 1),
('MySQL 索引原理', 'B+ 树是索引的核心...', '索引原理详解', 1, 2, 1),
('Vue3 组合式 API', 'setup 是 Vue3 的核心...', 'Vue3 入门', 2, 3, 1);

INSERT INTO `article_tag` (article_id, tag_id) VALUES (1,1),(1,2),(2,3),(3,4);
```

---

## 三、项目结构

采用 **Maven 单模块 + 分层包** 结构（Day8 讲过多模块，本项目用单模块简化部署）。

```
blog-system/
├── pom.xml
└── src/main/
    ├── java/com/example/blog/
    │   ├── config/
    │   │   ├── WebAppInitializer.java
    │   │   ├── RootConfig.java
    │   │   └── WebConfig.java
    │   ├── entity/
    │   │   ├── User.java
    │   │   ├── Article.java
    │   │   ├── Category.java
    │   │   ├── Tag.java
    │   │   └── Comment.java
    │   ├── vo/
    │   │   ├── ArticleVO.java
    │   │   ├── CommentVO.java
    │   │   └── ArticleDetailVO.java
    │   ├── dto/
    │   │   ├── ArticleDTO.java
    │   │   ├── CommentDTO.java
    │   │   └── LoginDTO.java
    │   ├── dao/
    │   │   ├── UserMapper.java
    │   │   ├── ArticleMapper.java
    │   │   ├── CategoryMapper.java
    │   │   ├── TagMapper.java
    │   │   └── CommentMapper.java
    │   ├── service/
    │   │   ├── UserService.java
    │   │   ├── ArticleService.java
    │   │   ├── CategoryService.java
    │   │   ├── TagService.java
    │   │   ├── CommentService.java
    │   │   └── impl/...
    │   ├── controller/
    │   │   ├── PageController.java
    │   │   ├── UserController.java
    │   │   ├── ArticleController.java
    │   │   ├── CommentController.java
    │   │   └── api/...
    │   ├── common/
    │   │   ├── Result.java
    │   │   └── PageResult.java
    │   ├── exception/
    │   │   └── BusinessException.java
    │   ├── handler/
    │   │   └── GlobalExceptionHandler.java
    │   ├── interceptor/
    │   │   └── LoginInterceptor.java
    │   └── util/
    │       ├── MD5Util.java
    │       └── MarkdownUtil.java (可选)
    ├── resources/
    │   ├── db.properties
    │   └── mapper/*.xml
    └── webapp/
        ├── WEB-INF/views/
        │   ├── index.jsp
        │   ├── login.jsp
        │   ├── register.jsp
        │   ├── article-detail.jsp
        │   ├── article-write.jsp
        │   ├── user-home.jsp
        │   ├── admin/
        │   │   ├── dashboard.jsp
        │   │   ├── article-list.jsp
        │   │   └── category-list.jsp
        │   └── common/
        │       ├── header.jsp
        │       └── footer.jsp
        └── static/
            ├── css/style.css
            └── js/app.js
```

---

## 四、pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>blog-system</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <spring.version>6.1.6</spring.version>
        <mybatis.version>3.5.16</mybatis.version>
        <mybatis-spring.version>3.0.3</mybatis-spring.version>
        <mysql.version>8.3.0</mysql.version>
        <hikari.version>5.1.0</hikari.version>
        <pagehelper.version>6.1.0</pagehelper.version>
        <servlet.version>6.0.0</servlet.version>
        <jstl.version>3.0.0</jstl.version>
        <jackson.version>2.17.0</jackson.version>
        <validation.version>8.0.1.Final</validation.version>
        <el.version>4.0.2</el.version>
        <annotation.version>2.1.1</annotation.version>
        <slf4j.version>2.0.13</slf4j.version>
    </properties>

    <dependencies>
        <!-- Spring -->
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

        <!-- 分页 -->
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper</artifactId>
            <version>${pagehelper.version}</version>
        </dependency>

        <!-- Web -->
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

        <!-- JSON -->
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

        <!-- 注解 -->
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

    <build>
        <finalName>blog-system</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 五、配置类

### 1. WebAppInitializer

```java
package com.example.blog.config;

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

    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return new Filter[]{filter};
    }
}
```

### 2. RootConfig

```java
package com.example.blog.config;

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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(
        basePackages = "com.example.blog",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = org.springframework.stereotype.Controller.class
        )
)
@MapperScan("com.example.blog.dao")
@EnableTransactionManagement
@PropertySource("classpath:db.properties")
public class RootConfig {

    @Value("${jdbc.driver}") private String driver;
    @Value("${jdbc.url}") private String url;
    @Value("${jdbc.username}") private String username;
    @Value("${jdbc.password}") private String password;
    @Value("${jdbc.maxPoolSize:10}") private int maxPoolSize;
    @Value("${jdbc.minIdle:5}") private int minIdle;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

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
        config.setPoolName("BlogPool");
        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTypeAliasesPackage("com.example.blog.entity");
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mapper/*.xml")
        );

        org.apache.ibatis.session.Configuration configuration =
                new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(true);
        configuration.setLazyLoadingEnabled(true);
        configuration.setAggressiveLazyLoading(false);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        factory.setConfiguration(configuration);

        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties props = new Properties();
        props.setProperty("helperDialect", "mysql");
        props.setProperty("reasonable", "true");
        pageInterceptor.setProperties(props);
        factory.setPlugins(new Interceptor[]{pageInterceptor});

        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
```

### 3. WebConfig

```java
package com.example.blog.config;

import com.example.blog.interceptor.LoginInterceptor;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

@Configuration
@EnableWebMvc
@ComponentScan("com.example.blog.controller")
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/", "/login", "/register", "/logout",
                        "/article", "/article/*", "/category/**", "/tag/**",
                        "/user/*",
                        "/static/**",
                        "/api/public/**"
                );
    }
}
```

### 4. db.properties

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/blog?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=123456
jdbc.maxPoolSize=10
jdbc.minIdle=5
```

---

## 六、Entity

### 1. User

```java
package com.example.blog.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class User implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String bio;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // getter/setter/toString
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
```

### 2. Article

```java
package com.example.blog.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Article implements Serializable {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String cover;
    private Long userId;
    private Long categoryId;
    private Integer views;
    private Integer likes;
    private Integer commentCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联对象
    private User author;
    private Category category;
    private List<Tag> tags;

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }
    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
}
```

### 3. Category、Tag、Comment（略，结构类似）

```java
public class Category {
    private Long id;
    private String name;
    private String description;
    private Integer sort;
    private LocalDateTime createTime;
    // getter/setter
}

public class Tag {
    private Long id;
    private String name;
    private LocalDateTime createTime;
    // getter/setter
}

public class Comment {
    private Long id;
    private Long articleId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
    // 关联
    private User user;
    private User parentUser;
    private Comment parent;
    // getter/setter
}
```

---

## 七、VO 与 DTO

### 1. ArticleVO（列表展示）

```java
package com.example.blog.vo;

import java.time.LocalDateTime;
import java.util.List;

public class ArticleVO {
    private Long id;
    private String title;
    private String summary;
    private String cover;
    private Integer views;
    private Integer likes;
    private Integer commentCount;
    private LocalDateTime createTime;
    private String authorNickname;
    private String authorAvatar;
    private String categoryName;
    private List<String> tags;
    // getter/setter
}
```

### 2. ArticleDTO（新增/编辑）

```java
package com.example.blog.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class ArticleDTO {
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 500)
    private String summary;

    private String cover;
    private Long categoryId;
    private List<Long> tagIds;
    private Integer status;
    // getter/setter
}
```

### 3. CommentDTO

```java
package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommentDTO {
    @NotNull
    private Long articleId;
    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
    // getter/setter
}
```

### 4. LoginDTO

```java
package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
    // getter/setter
}
```

---

## 八、DAO（Mapper）

### 1. ArticleMapper

```java
package com.example.blog.dao;

import com.example.blog.entity.Article;
import com.example.blog.vo.ArticleVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ArticleMapper {

    /** 首页列表（含作者、分类） */
    List<ArticleVO> findArticleList(@Param("keyword") String keyword,
                                    @Param("categoryId") Long categoryId,
                                    @Param("tagId") Long tagId,
                                    @Param("userId") Long userId,
                                    @Param("status") Integer status);

    /** 详情（含作者、分类、标签） */
    Article findDetailById(Long id);

    /** 标签列表 */
    List<String> findTagNamesByArticleId(Long articleId);

    int insert(Article article);

    int update(Article article);

    int deleteById(Long id);

    int incrementViews(Long id);

    int incrementLikes(Long id);

    int decrementLikes(Long id);

    int updateCommentCount(@Param("id") Long id, @Param("delta") int delta);

    long countByCondition(@Param("keyword") String keyword,
                          @Param("categoryId") Long categoryId,
                          @Param("userId") Long userId,
                          @Param("status") Integer status);
}
```

### 2. ArticleMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.blog.dao.ArticleMapper">

    <!-- 列表：用子查询统计评论数、标签 -->
    <select id="findArticleList" resultType="com.example.blog.vo.ArticleVO">
        SELECT
            a.id, a.title, a.summary, a.cover, a.views, a.likes,
            a.comment_count AS commentCount,
            a.create_time AS createTime,
            u.nickname AS authorNickname,
            u.avatar   AS authorAvatar,
            c.name     AS categoryName
        FROM article a
        LEFT JOIN sys_user u ON a.user_id = u.id
        LEFT JOIN category c ON a.category_id = c.id
        <where>
            a.status = 1
            <if test="keyword != null and keyword != ''">
                AND (a.title LIKE CONCAT('%', #{keyword}, '%')
                     OR a.content LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="categoryId != null">
                AND a.category_id = #{categoryId}
            </if>
            <if test="userId != null">
                AND a.user_id = #{userId}
            </if>
            <if test="tagId != null">
                AND EXISTS (
                    SELECT 1 FROM article_tag at
                    WHERE at.article_id = a.id AND at.tag_id = #{tagId}
                )
            </if>
            <if test="status != null">
                AND a.status = #{status}
            </if>
        </where>
        ORDER BY a.create_time DESC
    </select>

    <!-- 详情 -->
    <resultMap id="articleDetailMap" type="com.example.blog.entity.Article">
        <id column="id" property="id"/>
        <result column="title" property="title"/>
        <result column="content" property="content"/>
        <result column="summary" property="summary"/>
        <result column="cover" property="cover"/>
        <result column="views" property="views"/>
        <result column="likes" property="likes"/>
        <result column="comment_count" property="commentCount"/>
        <result column="create_time" property="createTime"/>
        <association property="author" javaType="com.example.blog.entity.User">
            <id column="u_id" property="id"/>
            <result column="u_username" property="username"/>
            <result column="u_nickname" property="nickname"/>
            <result column="u_avatar" property="avatar"/>
        </association>
        <association property="category" javaType="com.example.blog.entity.Category">
            <id column="c_id" property="id"/>
            <result column="c_name" property="name"/>
        </association>
    </resultMap>

    <select id="findDetailById" resultMap="articleDetailMap">
        SELECT a.*,
               u.id AS u_id, u.username AS u_username,
               u.nickname AS u_nickname, u.avatar AS u_avatar,
               c.id AS c_id, c.name AS c_name
        FROM article a
        LEFT JOIN sys_user u ON a.user_id = u.id
        LEFT JOIN category c ON a.category_id = c.id
        WHERE a.id = #{id}
    </select>

    <select id="findTagNamesByArticleId" resultType="string">
        SELECT t.name
        FROM tag t
        INNER JOIN article_tag at ON t.id = at.tag_id
        WHERE at.article_id = #{articleId}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO article(title, content, summary, cover, user_id, category_id, status)
        VALUES(#{title}, #{content}, #{summary}, #{cover}, #{userId}, #{categoryId}, #{status})
    </insert>

    <update id="update">
        UPDATE article
        <set>
            <if test="title != null">title = #{title},</if>
            <if test="content != null">content = #{content},</if>
            <if test="summary != null">summary = #{summary},</if>
            <if test="cover != null">cover = #{cover},</if>
            <if test="categoryId != null">category_id = #{categoryId},</if>
            <if test="status != null">status = #{status},</if>
        </set>
        WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM article WHERE id = #{id}
    </delete>

    <update id="incrementViews">
        UPDATE article SET views = views + 1 WHERE id = #{id}
    </update>

    <update id="incrementLikes">
        UPDATE article SET likes = likes + 1 WHERE id = #{id}
    </update>

    <update id="decrementLikes">
        UPDATE article SET likes = likes - 1 WHERE id = #{id} AND likes &gt; 0
    </update>

    <update id="updateCommentCount">
        UPDATE article SET comment_count = comment_count + #{delta} WHERE id = #{id}
    </update>

    <select id="countByCondition" resultType="long">
        SELECT COUNT(*) FROM article a
        <where>
            <if test="status != null">a.status = #{status}</if>
            <if test="keyword != null and keyword != ''">
                AND (a.title LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="categoryId != null">AND a.category_id = #{categoryId}</if>
            <if test="userId != null">AND a.user_id = #{userId}</if>
        </where>
    </select>

</mapper>
```

### 3. CommentMapper（含评论树）

```java
public interface CommentMapper {
    List<CommentVO> findByArticleId(Long articleId);
    Comment findById(Long id);
    int insert(Comment comment);
    int deleteById(Long id);
    int deleteByArticleId(Long articleId);
}
```

**CommentMapper.xml：**
```xml
<select id="findByArticleId" resultType="com.example.blog.vo.CommentVO">
    SELECT c.id, c.article_id AS articleId, c.user_id AS userId,
           c.parent_id AS parentId, c.content, c.create_time AS createTime,
           u.nickname AS userNickname, u.avatar AS userAvatar,
           pu.nickname AS parentNickname
    FROM comment c
    LEFT JOIN sys_user u ON c.user_id = u.id
    LEFT JOIN comment p ON c.parent_id = p.id
    LEFT JOIN sys_user pu ON p.user_id = pu.id
    WHERE c.article_id = #{articleId} AND c.status = 1
    ORDER BY c.create_time ASC
</select>
```

---

## 九、Service 层（含事务）

### 1. ArticleServiceImpl

```java
package com.example.blog.service.impl;

import com.example.blog.common.PageResult;
import com.example.blog.dao.ArticleMapper;
import com.example.blog.dao.ArticleTagMapper;
import com.example.blog.dao.TagMapper;
import com.example.blog.dto.ArticleDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.Tag;
import com.example.blog.exception.BusinessException;
import com.example.blog.service.ArticleService;
import com.example.blog.vo.ArticleVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Autowired
    public ArticleServiceImpl(ArticleMapper articleMapper,
                              TagMapper tagMapper,
                              ArticleTagMapper articleTagMapper) {
        this.articleMapper = articleMapper;
        this.tagMapper = tagMapper;
        this.articleTagMapper = articleTagMapper;
    }

    @Override
    public PageResult<ArticleVO> findPage(String keyword, Long categoryId,
                                          Long tagId, Long userId,
                                          Integer status, int page, int size) {
        PageHelper.startPage(page, size);
        List<ArticleVO> list = articleMapper.findArticleList(
                keyword, categoryId, tagId, userId, status);

        // 补充标签
        for (ArticleVO vo : list) {
            vo.setTags(articleMapper.findTagNamesByArticleId(vo.getId()));
        }

        PageInfo<ArticleVO> info = new PageInfo<>(list);
        return PageResult.of(info);
    }

    @Override
    public Article findDetail(Long id) {
        Article article = articleMapper.findDetailById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        article.setTags(tagMapper.findByArticleId(id));
        return article;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(ArticleDTO dto, Long userId) {
        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        article.setUserId(userId);
        if (article.getStatus() == null) article.setStatus(1);

        articleMapper.insert(article);

        // 保存标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            articleTagMapper.batchInsert(article.getId(), dto.getTagIds());
        }
        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ArticleDTO dto, Long userId) {
        Article article = articleMapper.findDetailById(dto.getId());
        if (article == null) throw new BusinessException("文章不存在");
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权编辑他人文章");
        }

        Article update = new Article();
        BeanUtils.copyProperties(dto, update);
        articleMapper.update(update);

        // 更新标签：先删后插
        articleTagMapper.deleteByArticleId(dto.getId());
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            articleTagMapper.batchInsert(dto.getId(), dto.getTagIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        Article article = articleMapper.findDetailById(id);
        if (article == null) throw new BusinessException("文章不存在");
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人文章");
        }
        articleTagMapper.deleteByArticleId(id);
        articleMapper.deleteById(id);
    }

    @Override
    public void incrementViews(Long id) {
        articleMapper.incrementViews(id);
    }
}
```

### 2. CommentServiceImpl（含事务）

```java
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;

    @Autowired
    public CommentServiceImpl(CommentMapper commentMapper, ArticleMapper articleMapper) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
    }

    @Override
    public List<CommentVO> findByArticleId(Long articleId) {
        return commentMapper.findByArticleId(articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(CommentDTO dto, Long userId) {
        Comment c = new Comment();
        c.setArticleId(dto.getArticleId());
        c.setParentId(dto.getParentId());
        c.setContent(dto.getContent());
        c.setUserId(userId);
        c.setStatus(1);
        commentMapper.insert(c);

        // 评论数 +1
        articleMapper.updateCommentCount(dto.getArticleId(), 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        Comment c = commentMapper.findById(id);
        if (c == null) throw new BusinessException("评论不存在");
        if (!c.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人评论");
        }
        commentMapper.deleteById(id);
        articleMapper.updateCommentCount(c.getArticleId(), -1);
    }
}
```

### 3. 点赞 Service（用唯一索引防重复）

```java
@Service
public class LikeServiceImpl implements LikeService {

    private final LikeRecordMapper likeMapper;
    private final ArticleMapper articleMapper;

    @Autowired
    public LikeServiceImpl(LikeRecordMapper likeMapper, ArticleMapper articleMapper) {
        this.likeMapper = likeMapper;
        this.articleMapper = articleMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long userId, Long articleId) {
        // 尝试插入
        try {
            likeMapper.insert(userId, articleId);
            articleMapper.incrementLikes(articleId);
            return true;
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 已点赞，取消
            likeMapper.delete(userId, articleId);
            articleMapper.decrementLikes(articleId);
            return false;
        }
    }
}
```

---

## 十、Controller 层

### 1. PageController（首页 + 静态页面）

```java
@Controller
public class PageController {

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @Autowired
    public PageController(ArticleService articleService,
                          CategoryService categoryService,
                          TagService tagService) {
        this.articleService = articleService;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) Long tagId,
                        @RequestParam(defaultValue = "1") int page,
                        Model model) {
        PageResult<ArticleVO> pageResult = articleService.findPage(
                keyword, categoryId, tagId, null, 1, page, 10);
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("tags", tagService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("tagId", tagId);
        return "index";
    }

    @GetMapping("/article/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Article article = articleService.findDetail(id);
        articleService.incrementViews(id);
        model.addAttribute("article", article);
        model.addAttribute("comments", commentService.findByArticleId(id));
        return "article-detail";
    }

    @GetMapping("/article/write")
    public String writePage(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("tags", tagService.findAll());
        return "article-write";
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @GetMapping("/user/{id}")
    public String userHome(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        PageResult<ArticleVO> pageResult = articleService.findPage(
                null, null, null, id, 1, 1, 10);
        model.addAttribute("pageResult", pageResult);
        return "user-home";
    }
}
```

### 2. ArticleApiController（RESTful）

```java
@RestController
@RequestMapping("/api/articles")
public class ArticleApiController {

    private final ArticleService articleService;

    @Autowired
    public ArticleApiController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public Result<PageResult<ArticleVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(articleService.findPage(
                keyword, categoryId, null, null, 1, page, size));
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(articleService.findDetail(id));
    }

    @PostMapping
    public Result<Long> publish(@RequestBody @Valid ArticleDTO dto,
                                HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return Result.success(articleService.publish(dto, user.getId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid ArticleDTO dto,
                               HttpSession session) {
        dto.setId(id);
        User user = (User) session.getAttribute("loginUser");
        articleService.update(dto, user.getId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        articleService.delete(id, user.getId());
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<Boolean> like(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return Result.success(likeService.toggle(user.getId(), id));
    }
}
```

### 3. CommentApiController

```java
@RestController
@RequestMapping("/api/comments")
public class CommentApiController {

    private final CommentService commentService;

    @Autowired
    public CommentApiController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/article/{articleId}")
    public Result<List<CommentVO>> list(@PathVariable Long articleId) {
        return Result.success(commentService.findByArticleId(articleId));
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid CommentDTO dto, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        commentService.add(dto, user.getId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        commentService.delete(id, user.getId());
        return Result.success();
    }
}
```

### 4. UserApiController（登录注册）

```java
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    @Autowired
    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody @Valid LoginDTO dto, HttpSession session) {
        User user = userService.login(dto.getUsername(), dto.getPassword());
        if (user == null) return Result.error(401, "用户名或密码错误");
        session.setAttribute("loginUser", user);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }

    @GetMapping("/me")
    public Result<User> me(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? Result.success(user) : Result.error(401, "未登录");
    }
}
```

---

## 十一、拦截器 + 全局异常

### 1. LoginInterceptor

```java
package com.example.blog.interceptor;

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
        Object user = session == null ? null : session.getAttribute("loginUser");

        if (user == null) {
            if (isAjax(request)) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return false;
        }
        return true;
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
                || (request.getHeader("Accept") != null
                    && request.getHeader("Accept").contains("application/json"));
    }
}
```

### 2. GlobalExceptionHandler

```java
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
        return Result.error(500, "系统异常：" + e.getMessage());
    }
}
```

---

## 十二、JSP 视图

### 1. 公共头 common/header.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<header class="site-header">
    <div class="container">
        <a href="${ctx}/" class="logo">我的博客</a>
        <nav>
            <a href="${ctx}/">首页</a>
            <c:forEach items="${categories}" var="c" varStatus="st">
                <c:if test="${st.index < 5}">
                    <a href="${ctx}/?categoryId=${c.id}">${c.name}</a>
                </c:if>
            </c:forEach>
        </nav>
        <div class="user-area">
            <c:choose>
                <c:when test="${not empty sessionScope.loginUser}">
                    <a href="${ctx}/article/write">写文章</a>
                    <a href="${ctx}/user/${sessionScope.loginUser.id}">
                        ${sessionScope.loginUser.nickname}
                    </a>
                    <a href="${ctx}/logout">退出</a>
                </c:when>
                <c:otherwise>
                    <a href="${ctx}/login">登录</a>
                    <a href="${ctx}/register">注册</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>
```

### 2. 首页 index.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>我的博客</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<jsp:include page="common/header.jsp"/>

<div class="container main-content">
    <div class="article-list">
        <h2>最新文章</h2>
        <c:choose>
            <c:when test="${empty pageResult.list}">
                <p class="empty">暂无文章</p>
            </c:when>
            <c:otherwise>
                <c:forEach items="${pageResult.list}" var="a">
                    <div class="article-card">
                        <h3>
                            <a href="${ctx}/article/${a.id}">${a.title}</a>
                        </h3>
                        <p class="summary">${a.summary}</p>
                        <div class="meta">
                            <span>作者：${a.authorNickname}</span>
                            <span>分类：${a.categoryName}</span>
                            <span>浏览：${a.views}</span>
                            <span>点赞：${a.likes}</span>
                            <span>评论：${a.commentCount}</span>
                            <span>
                                <fmt:formatDate value="${a.createTime}"
                                    pattern="yyyy-MM-dd HH:mm"/>
                            </span>
                        </div>
                        <div class="tags">
                            <c:forEach items="${a.tags}" var="t">
                                <span class="tag">${t}</span>
                            </c:forEach>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>

        <!-- 分页 -->
        <c:if test="${pageResult.pages > 1}">
            <div class="pagination">
                <c:if test="${pageResult.pageNum > 1}">
                    <a href="${ctx}/?page=${pageResult.pageNum - 1}">上一页</a>
                </c:if>
                <span>第 ${pageResult.pageNum} / ${pageResult.pages} 页</span>
                <c:if test="${pageResult.pageNum < pageResult.pages}">
                    <a href="${ctx}/?page=${pageResult.pageNum + 1}">下一页</a>
                </c:if>
            </div>
        </c:if>
    </div>

    <aside class="sidebar">
        <div class="widget">
            <h3>分类</h3>
            <ul>
                <c:forEach items="${categories}" var="c">
                    <li><a href="${ctx}/?categoryId=${c.id}">${c.name}</a></li>
                </c:forEach>
            </ul>
        </div>
        <div class="widget">
            <h3>标签</h3>
            <div class="tag-cloud">
                <c:forEach items="${tags}" var="t">
                    <a href="${ctx}/?tagId=${t.id}" class="tag">${t.name}</a>
                </c:forEach>
            </div>
        </div>
    </aside>
</div>
</body>
</html>
```

### 3. 文章详情 article-detail.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${article.title}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<jsp:include page="common/header.jsp"/>

<div class="container">
    <article class="article-detail">
        <h1>${article.title}</h1>
        <div class="meta">
            <span>作者：${article.author.nickname}</span>
            <span>分类：${article.category.name}</span>
            <span>浏览：${article.views}</span>
            <span>点赞：${article.likes}</span>
            <span>
                <fmt:formatDate value="${article.createTime}"
                    pattern="yyyy-MM-dd HH:mm"/>
            </span>
        </div>
        <div class="tags">
            <c:forEach items="${article.tags}" var="t">
                <span class="tag">${t.name}</span>
            </c:forEach>
        </div>

        <div class="content">${article.content}</div>

        <div class="actions">
            <button id="likeBtn" data-id="${article.id}">
                点赞 (<span id="likeCount">${article.likes}</span>)
            </button>
        </div>
    </article>

    <section class="comments">
        <h2>评论 (${article.commentCount})</h2>
        <c:if test="${not empty sessionScope.loginUser}">
            <form id="commentForm">
                <input type="hidden" name="articleId" value="${article.id}">
                <textarea name="content" placeholder="说点什么..." required></textarea>
                <button type="submit">发表评论</button>
            </form>
        </c:if>
        <c:if test="${empty sessionScope.loginUser}">
            <p><a href="${ctx}/login">登录</a> 后发表评论</p>
        </c:if>

        <ul class="comment-list">
            <c:forEach items="${comments}" var="c">
                <li class="comment">
                    <div class="comment-header">
                        <strong>${c.userNickname}</strong>
                        <c:if test="${not empty c.parentNickname}">
                            回复 <strong>${c.parentNickname}</strong>
                        </c:if>
                        <span>
                            <fmt:formatDate value="${c.createTime}"
                                pattern="yyyy-MM-dd HH:mm"/>
                        </span>
                    </div>
                    <p>${c.content}</p>
                </li>
            </c:forEach>
        </ul>
    </section>
</div>

<script>
    const ctx = '${pageContext.request.contextPath}';

    // 点赞
    document.getElementById('likeBtn').addEventListener('click', async () => {
        const id = document.getElementById('likeBtn').dataset.id;
        const res = await fetch(`${ctx}/api/articles/${id}/like`, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });
        const data = await res.json();
        if (data.code === 200) {
            const delta = data.data ? 1 : -1;
            const span = document.getElementById('likeCount');
            span.textContent = parseInt(span.textContent) + delta;
        } else {
            alert(data.msg);
        }
    });

    // 评论
    const form = document.getElementById('commentForm');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(form);
            const body = Object.fromEntries(formData);
            const res = await fetch(`${ctx}/api/comments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify(body)
            });
            const data = await res.json();
            if (data.code === 200) {
                location.reload();
            } else {
                alert(data.msg);
            }
        });
    }
</script>
</body>
</html>
```

### 4. 写文章 article-write.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>写文章</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<jsp:include page="common/header.jsp"/>

<div class="container">
    <h1>写文章</h1>
    <form id="articleForm">
        <div class="form-group">
            <label>标题</label>
            <input type="text" name="title" required maxlength="200">
        </div>
        <div class="form-group">
            <label>摘要</label>
            <input type="text" name="summary" maxlength="500">
        </div>
        <div class="form-group">
            <label>分类</label>
            <select name="categoryId">
                <option value="">请选择</option>
                <c:forEach items="${categories}" var="c">
                    <option value="${c.id}">${c.name}</option>
                </c:forEach>
            </select>
        </div>
        <div class="form-group">
            <label>标签（可多选）</label>
            <c:forEach items="${tags}" var="t">
                <label class="checkbox">
                    <input type="checkbox" name="tagIds" value="${t.id}"> ${t.name}
                </label>
            </c:forEach>
        </div>
        <div class="form-group">
            <label>内容</label>
            <textarea name="content" rows="20" required></textarea>
        </div>
        <button type="submit">发布</button>
    </form>
</div>

<script>
    const ctx = '${pageContext.request.contextPath}';
    document.getElementById('articleForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        const tagIds = fd.getAll('tagIds').map(Number);
        const body = {
            title: fd.get('title'),
            summary: fd.get('summary'),
            categoryId: fd.get('categoryId') ? Number(fd.get('categoryId')) : null,
            content: fd.get('content'),
            tagIds: tagIds
        };
        const res = await fetch(`${ctx}/api/articles`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(body)
        });
        const data = await res.json();
        if (data.code === 200) {
            location.href = `${ctx}/article/${data.data}`;
        } else {
            alert(data.msg);
        }
    });
</script>
</body>
</html>
```

---

## 十三、测试与验收

### 1. 验收清单

| 序号 | 功能 | 验收标准 |
|------|------|----------|
| 1 | 首页 | 展示文章列表，含作者、分类、标签、浏览量、评论数 |
| 2 | 分页 | 分页正确，URL 带 page 参数 |
| 3 | 分类筛选 | 点击分类显示该分类下文章 |
| 4 | 标签筛选 | 点击标签显示该标签下文章 |
| 5 | 搜索 | 关键词搜索标题/内容 |
| 6 | 文章详情 | 显示内容、作者、分类、标签、评论 |
| 7 | 浏览量 | 每次访问 +1 |
| 8 | 登录 | 登录成功，Session 保存 |
| 9 | 注册 | 用户名唯一校验，密码加密 |
| 10 | 写文章 | 登录后可发布，标签关联正确 |
| 11 | 编辑文章 | 仅作者可编辑 |
| 12 | 删除文章 | 仅作者可删除 |
| 13 | 评论 | 登录后可评论，评论数联动 |
| 14 | 点赞 | 可点赞/取消，防重复 |
| 15 | 拦截器 | 未登录访问写文章跳登录页 |
| 16 | RESTful | 所有接口返回统一 `Result` |
| 17 | 异常 | 业务异常返回友好提示 |
| 18 | 事务 | 发布文章含标签，任一失败回滚 |
| 19 | 中文 | 全站无乱码 |
| 20 | 防注入 | 参数用 `#{}`，无 SQL 注入 |

### 2. Postman 测试

**注册：**
```
POST http://localhost:8080/api/users/register
Content-Type: application/json

{"username":"newuser","password":"123456","nickname":"新用户","email":"new@example.com"}
```

**登录：**
```
POST http://localhost:8080/api/users/login
Content-Type: application/json

{"username":"newuser","password":"123456"}
```

**发布文章：**
```
POST http://localhost:8080/api/articles
Content-Type: application/json
Cookie: JSESSIONID=xxx

{"title":"测试文章","content":"内容","summary":"摘要","categoryId":1,"tagIds":[1,2]}
```

**获取文章列表：**
```
GET http://localhost:8080/api/articles?page=1&size=5
```

**点赞：**
```
POST http://localhost:8080/api/articles/1/like
Cookie: JSESSIONID=xxx
```

### 3. 事务验证

在 `ArticleServiceImpl.publish` 中，插入 article 后手动抛异常，验证：
- article 未插入（回滚）
- article_tag 未插入（回滚）

---

## 十四、项目亮点

### 1. 架构规范
- 分层清晰：Controller / Service / DAO / Entity / VO / DTO
- 面向接口编程：Service 接口 + 实现
- 配置分离：RootConfig（业务）+ WebConfig（Web）
- 无 web.xml（Servlet 3.0+）

### 2. 技术亮点
- **MyBatis 动态 SQL**：多条件搜索、标签筛选
- **关联查询**：文章 + 作者 + 分类（association），文章 + 标签（collection）
- **PageHelper 分页**：一行代码分页
- **声明式事务**：`@Transactional` 保证标签关联的一致性
- **统一返回**：`Result<T>` + `PageResult<T>`
- **全局异常**：`@RestControllerAdvice`
- **参数校验**：JSR-303 + `@Valid`
- **拦截器**：登录校验，区分 AJAX 和页面请求
- **防重复点赞**：唯一索引 + DuplicateKeyException

### 3. 安全措施
- PreparedStatement / `#{}` 防 SQL 注入
- JSTL `escapeXml` 防 XSS
- MD5 加盐存储密码
- 拦截器防未登录访问
- 权限校验（只能编辑/删除自己的文章）

### 4. 用户体验
- 文章列表卡片式布局
- 标签云、分类导航
- 点赞、评论实时交互
- 分页导航

---

## 十五、拓展方向

### 1. 功能扩展
- **Markdown 编辑**：集成 Editor.md，服务端用 CommonMark 转 HTML
- **富文本编辑**：集成 WangEditor
- **文件上传**：文章封面、用户头像
- **全文搜索**：Elasticsearch 替代 MySQL FULLTEXT
- **消息通知**：评论/点赞通知
- **关注/粉丝**：多对多关系
- **草稿箱**：status=0 的文章
- **文章归档**：按年月归档
- **数据统计**：访问量图表（ECharts）
- **后台审核**：评论审核、文章审核

### 2. 技术升级
- **前后端分离**：Vue/React + axios 调用 RESTful API
- **JWT 认证**：替代 Session，支持分布式
- **Redis 缓存**：热点文章、分类、标签
- **Elasticsearch**：全文搜索
- **Spring Boot**：替代 SSM 配置，自动装配
- **MyBatis-Plus**：简化 CRUD
- **Docker**：容器化部署
- **Nginx**：反向代理 + 静态资源

### 3. 性能优化
- **数据库索引优化**：EXPLAIN 分析慢查询
- **分页优化**：大 OFFSET 用 `WHERE id > ?`
- **缓存**：Redis 缓存热点数据
- **异步**：评论通知用 MQ
- **CDN**：静态资源加速

### 4. 代码质量
- **单元测试**：JUnit 5 + Mockito
- **集成测试**：Spring Test
- **代码规范**：阿里 P3C 插件
- **日志**：Logback + 分级输出
- **监控**：Actuator + Prometheus

---

## 十六、两周课程总结

### 1. 知识地图

```
第1周：Java Web 基础
├── Day1  HTTP/RESTful/JSON
├── Day2  Servlet 入门
├── Day3  Servlet 进阶（转发/Session/Filter/Listener）
├── Day4  JSP/EL/JSTL/MVC
├── Day5  JDBC/连接池
├── Day6  MySQL（SQL/索引/事务/设计）
└── Day7  综合实战：用户管理系统（Servlet + JSP + JDBC）

第2周：主流框架
├── Day8  Maven 进阶 + 分层架构
├── Day9  MyBatis 入门
├── Day10 MyBatis 进阶（动态 SQL/关联/缓存/PageHelper）
├── Day11 Spring 核心（IoC/DI/AOP）
├── Day12 Spring MVC
├── Day13 SSM 整合 + 声明式事务
└── Day14 综合项目：博客系统（SSM 完整实战）
```

### 2. 核心能力

学完两周，你具备：
- ✅ 独立搭建 Java Web 项目
- ✅ 掌握 Servlet/JSP 底层原理
- ✅ 熟练使用 MySQL 设计与优化
- ✅ 精通 MyBatis（动态 SQL、关联、分页）
- ✅ 掌握 Spring IoC/DI/AOP
- ✅ 掌握 Spring MVC（RESTful、参数绑定、拦截器）
- ✅ 掌握 SSM 整合与声明式事务
- ✅ 完成完整项目开发

### 3. 下一步学习方向

| 方向 | 技术 |
|------|------|
| 快速开发 | Spring Boot + MyBatis-Plus |
| 微服务 | Spring Cloud + Nacos + Gateway |
| 分布式 | Redis + MQ + 分库分表 |
| 前端 | Vue3 + Element Plus + Vite |
| 中间件 | Redis、RabbitMQ、Elasticsearch |
| 运维 | Docker、K8s、Jenkins |

### 4. 推荐资源

- **书籍**：《Spring 实战》《MyBatis 从入门到精通》《高性能 MySQL》
- **文档**：Spring、MyBatis、MySQL 官方文档
- **规范**：阿里巴巴 Java 开发手册
- **实战**：慕课网、尚硅谷、黑马程序员视频

---

## 结语

两周课程从 HTTP 基础到 SSM 完整项目，覆盖了 Java Web 开发的核心知识。**Day14 博客系统**把所有知识点串联起来，是一个可运行、可演示、可扩展的完整项目。

建议：
1. **动手敲**：每行代码都自己写一遍
2. **做笔记**：整理知识脑图
3. **多调试**：遇到问题看日志、用 Postman 排查
4. **持续迭代**：在博客系统上不断加功能
5. **学框架**：接下来学习 Spring Boot，效率会大幅提升

祝学习顺利！🎉