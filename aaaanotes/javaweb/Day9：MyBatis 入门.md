

## 学习目标

- 理解 ORM 思想与 MyBatis 的定位
- 掌握 MyBatis 核心配置（`mybatis-config.xml`、Mapper XML）
- 掌握 `SqlSessionFactory` / `SqlSession` 的使用
- 熟练使用 Mapper 接口 + XML 完成 CRUD
- 理解 `#{}` 与 `${}` 的区别与安全用法
- 掌握 resultType 与 resultMap 结果映射
- 用 MyBatis 替换 Day8 的手写 JDBC UserDao

---

## 一、ORM 与 MyBatis 概述

### 1. 什么是 ORM

ORM（Object-Relational Mapping，对象关系映射）是一种**把 Java 对象与数据库表自动映射**的技术。

**不用 ORM（手写 JDBC）：**
```java
String sql = "SELECT id, username, nickname FROM sys_user WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setLong(1, id);
ResultSet rs = ps.executeQuery();
User u = null;
if (rs.next()) {
    u = new User();
    u.setId(rs.getLong("id"));
    u.setUsername(rs.getString("username"));
    u.setNickname(rs.getString("nickname"));
    // 每个字段都要手动映射...
}
```

**用 ORM（MyBatis）：**
```java
User u = userMapper.findById(id);
```

**ORM 解决的问题：**
- 免去 JDBC 样板代码（连接、关闭、异常、映射）
- SQL 与 Java 分离，便于维护
- 结果自动映射到对象
- 支持动态 SQL、缓存、插件

### 2. MyBatis 的定位

MyBatis 是一款**半自动 ORM 框架**，与 Hibernate/JPA（全自动）的区别：

| 特性 | MyBatis（半自动） | Hibernate/JPA（全自动） |
|------|-------------------|-------------------------|
| SQL 控制 | 开发者写 SQL | 框架生成 SQL |
| 灵活性 | 高 | 中 |
| 学习成本 | 低 | 高 |
| 性能调优 | 直接可控 | 较难 |
| 上手速度 | 快 | 慢 |
| 适用场景 | 互联网、复杂 SQL | 传统企业、简单 CRUD |

**MyBatis 核心特点：**
- SQL 写在 XML 或注解中，灵活可控
- 结果自动映射到 Java 对象
- 支持动态 SQL
- 支持一级/二级缓存
- 支持插件（分页、分表）

> 💡 国内互联网公司普遍使用 MyBatis（或 MyBatis-Plus），因为 SQL 可控、性能可调。

### 3. MyBatis 架构

```
应用层
    ↓
SqlSessionFactoryBuilder（读取配置）
    ↓
SqlSessionFactory（单例，全局唯一）
    ↓
SqlSession（每次操作创建，线程不安全）
    ↓
Executor（执行器）
    ↓
MappedStatement（SQL 映射）
    ↓
数据库
```

**核心对象：**

| 对象 | 作用 | 生命周期 |
|------|------|----------|
| SqlSessionFactoryBuilder | 读取配置，构建 Factory | 方法级，用完即弃 |
| SqlSessionFactory | 创建 SqlSession | 应用级，单例 |
| SqlSession | 执行 SQL、管理事务 | 请求级，线程不安全 |
| Mapper | 接口代理，调用 SQL | 与 SqlSession 绑定 |

---

## 二、快速开始

### 1. Maven 依赖

```xml
<properties>
    <mybatis.version>3.5.16</mybatis.version>
    <mysql.version>8.3.0</mysql.version>
    <slf4j.version>2.0.13</slf4j.version>
</properties>

<dependencies>
    <!-- MyBatis -->
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis</artifactId>
        <version>${mybatis.version}</version>
    </dependency>

    <!-- MySQL 驱动 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>${mysql.version}</version>
    </dependency>

    <!-- 日志 -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>${slf4j.version}</version>
    </dependency>

    <!-- 测试 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2. 数据库表

沿用 Day7 的 `sys_user` 表：

```sql
CREATE TABLE `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(64) NOT NULL,
    `nickname` VARCHAR(50),
    `age` INT,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3. db.properties

放在 `src/main/resources`：

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/user_mgmt?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=123456
```

### 4. 实体类

```java
package com.example.entity;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private Integer age;
    private String email;
    private String phone;
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
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', nickname='" + nickname
                + "', age=" + age + ", email='" + email + "', status=" + status + "}";
    }
}
```

### 5. mybatis-config.xml

放在 `src/main/resources`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

    <!-- 引入外部配置 -->
    <properties resource="db.properties"/>

    <!-- 全局设置 -->
    <settings>
        <!-- 下划线转驼峰：create_time → createTime -->
        <setting name="mapUnderscoreToCamelCase" value="true"/>
        <!-- 日志实现 -->
        <setting name="logImpl" value="STDOUT_LOGGING"/>
        <!-- 开启二级缓存（默认 true） -->
        <setting name="cacheEnabled" value="true"/>
        <!-- 延迟加载 -->
        <setting name="lazyLoadingEnabled" value="true"/>
        <setting name="aggressiveLazyLoading" value="false"/>
        <!-- 返回主键 -->
        <setting name="useGeneratedKeys" value="true"/>
        <!-- 超时时间（秒） -->
        <setting name="defaultStatementTimeout" value="30"/>
    </settings>

    <!-- 类型别名：com.example.entity.User → User -->
    <typeAliases>
        <package name="com.example.entity"/>
    </typeAliases>

    <!-- 环境配置 -->
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${jdbc.driver}"/>
                <property name="url" value="${jdbc.url}"/>
                <property name="username" value="${jdbc.username}"/>
                <property name="password" value="${jdbc.password}"/>
                <!-- 连接池参数 -->
                <property name="poolMaximumActiveConnections" value="10"/>
                <property name="poolMaximumIdleConnections" value="5"/>
            </dataSource>
        </environment>
    </environments>

    <!-- 映射文件 -->
    <mappers>
        <mapper resource="mapper/UserMapper.xml"/>
    </mappers>

</configuration>
```

> 💡 生产环境一般用 Spring 管理数据源（Day13），此处用 MyBatis 自带 POOLED 连接池演示。

### 6. Mapper 接口

```java
package com.example.dao;

import com.example.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    // ============ 查询 ============

    User findById(Long id);

    User findByUsername(String username);

    List<User> findAll();

    List<User> findByPage(@Param("keyword") String keyword,
                          @Param("status") Integer status,
                          @Param("offset") int offset,
                          @Param("size") int size);

    long countByCondition(@Param("keyword") String keyword,
                          @Param("status") Integer status);

    boolean existsUsername(@Param("username") String username,
                           @Param("excludeId") Long excludeId);

    // ============ 增删改 ============

    int insert(User user);

    int update(User user);

    int deleteById(Long id);

    int deleteBatch(@Param("ids") List<Long> ids);
}
```

> 💡 多参数必须用 `@Param` 指定名称，否则 MyBatis 用 `arg0/param1` 命名，易错。

### 7. UserMapper.xml

放在 `src/main/resources/mapper/`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.dao.UserMapper">

    <!-- 通用列（可复用） -->
    <sql id="baseColumns">
        id, username, password, nickname, age, email, phone, status, create_time, update_time
    </sql>

    <!-- 结果映射（字段名与属性名不一致时使用） -->
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

    <!-- ============ 查询 ============ -->

    <select id="findById" resultMap="userResultMap">
        SELECT <include refid="baseColumns"/>
        FROM sys_user
        WHERE id = #{id}
    </select>

    <select id="findByUsername" resultMap="userResultMap">
        SELECT <include refid="baseColumns"/>
        FROM sys_user
        WHERE username = #{username}
    </select>

    <select id="findAll" resultMap="userResultMap">
        SELECT <include refid="baseColumns"/>
        FROM sys_user
        ORDER BY id DESC
    </select>

    <select id="findByPage" resultMap="userResultMap">
        SELECT <include refid="baseColumns"/>
        FROM sys_user
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
        LIMIT #{size} OFFSET #{offset}
    </select>

    <select id="countByCondition" resultType="long">
        SELECT COUNT(*)
        FROM sys_user
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
    </select>

    <select id="existsUsername" resultType="boolean">
        SELECT COUNT(*) > 0
        FROM sys_user
        WHERE username = #{username}
        <if test="excludeId != null">
            AND id != #{excludeId}
        </if>
    </select>

    <!-- ============ 增删改 ============ -->

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

### 8. MyBatis 工具类

```java
package com.example.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MyBatisUtil {

    private static final SqlSessionFactory FACTORY;

    static {
        try (InputStream is = Resources.getResourceAsStream("mybatis-config.xml")) {
            FACTORY = new SqlSessionFactoryBuilder().build(is);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("MyBatis 初始化失败：" + e.getMessage());
        }
    }

    public static SqlSessionFactory getFactory() {
        return FACTORY;
    }

    public static SqlSession openSession() {
        return FACTORY.openSession();          // 手动提交
    }

    public static SqlSession openSession(boolean autoCommit) {
        return FACTORY.openSession(autoCommit); // 自动提交
    }
}
```

### 9. 测试

```java
package com.example;

import com.example.dao.UserMapper;
import com.example.entity.User;
import com.example.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class UserMapperTest {

    @Test
    public void testCrud() {
        try (SqlSession session = MyBatisUtil.openSession(true)) { // 自动提交
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 插入
            User u = new User();
            u.setUsername("test01");
            u.setPassword("123456");
            u.setNickname("测试用户");
            u.setAge(25);
            u.setEmail("test01@example.com");
            u.setStatus(1);
            mapper.insert(u);
            System.out.println("插入后 ID = " + u.getId()); // 回填主键

            // 查询
            User found = mapper.findById(u.getId());
            System.out.println("查询：" + found);

            // 更新
            found.setNickname("测试用户-改");
            found.setAge(26);
            mapper.update(found);
            System.out.println("更新后：" + mapper.findById(u.getId()));

            // 列表
            List<User> list = mapper.findAll();
            list.forEach(System.out::println);

            // 条件查询
            System.out.println("搜索 '测试'：");
            mapper.findByPage("测试", null, 0, 10).forEach(System.out::println);

            // 统计
            System.out.println("总数：" + mapper.countByCondition(null, null));

            // 删除
            mapper.deleteById(u.getId());
            System.out.println("删除后总数：" + mapper.countByCondition(null, null));
        }
    }

    @Test
    public void testBatchDelete() {
        try (SqlSession session = MyBatisUtil.openSession(true)) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            mapper.deleteBatch(Arrays.asList(1L, 2L, 3L));
        }
    }
}
```

### 10. 日志输出

配置 `logImpl=STDOUT_LOGGING` 后，控制台会输出：
```
==>  Preparing: SELECT id, username, ... FROM sys_user WHERE id = ?
==> Parameters: 1(Long)
<==      Total: 1
```
方便调试 SQL 和参数。

---

## 三、`#{}` 与 `${}`

### 1. 区别

| 特性 | `#{}` | `${}` |
|------|-------|-------|
| 原理 | 预编译占位符 `?` | 字符串直接拼接 |
| SQL 注入 | 安全 | 有风险 |
| 数据类型 | 自动处理 | 需手动加引号 |
| 用途 | 参数值 | 表名、列名、ORDER BY |
| 示例 | `WHERE id = #{id}` | `ORDER BY ${column}` |

### 2. 原理对比

```xml
<!-- #{} -->
SELECT * FROM user WHERE username = #{username}
<!-- 编译为 -->
SELECT * FROM user WHERE username = ?
<!-- 参数通过 PreparedStatement 设置，防注入 -->

<!-- ${} -->
SELECT * FROM user WHERE username = '${username}'
<!-- 直接拼接，输入 ' OR '1'='1 会被注入 -->
```

### 3. `${}` 的正确用法

**仅用于动态表名、列名、排序字段**，且必须白名单校验：

```xml
<select id="findByOrder" resultMap="userResultMap">
    SELECT * FROM sys_user
    ORDER BY ${orderColumn} ${orderDirection}
</select>
```

```java
// Service 层白名单校验
private static final Set<String> ALLOWED_COLUMNS =
        Set.of("id", "username", "age", "create_time");
private static final Set<String> ALLOWED_DIRECTIONS =
        Set.of("ASC", "DESC");

public List<User> findByOrder(String column, String direction) {
    if (!ALLOWED_COLUMNS.contains(column)) {
        throw new BusinessException("非法排序字段");
    }
    if (!ALLOWED_DIRECTIONS.contains(direction.toUpperCase())) {
        throw new BusinessException("非法排序方向");
    }
    // ...
}
```

> ⚠️ **永远不要用 `${}` 接收用户输入的值**，只用它处理表名、列名等无法用 `?` 占位的地方。

---

## 四、参数传递详解

### 1. 单个参数

```java
User findById(Long id);
```
```xml
<select id="findById" resultMap="userResultMap">
    SELECT * FROM sys_user WHERE id = #{id}
    <!-- 单参数时 #{任意名} 都能取到值 -->
</select>
```

### 2. 多个参数

**方式一：`@Param`（推荐）**
```java
List<User> findByPage(@Param("keyword") String keyword,
                      @Param("status") Integer status,
                      @Param("offset") int offset,
                      @Param("size") int size);
```
```xml
<select id="findByPage" resultMap="userResultMap">
    SELECT * FROM sys_user
    WHERE 1=1
    <if test="keyword != null and keyword != ''">
        AND nickname LIKE CONCAT('%', #{keyword}, '%')
    </if>
    <if test="status != null">
        AND status = #{status}
    </if>
    LIMIT #{size} OFFSET #{offset}
</select>
```

**方式二：不用 `@Param`（不推荐）**
```java
List<User> findByPage(String keyword, Integer status, int offset, int size);
```
```xml
<!-- 只能用 arg0/param1 等默认名 -->
AND nickname LIKE CONCAT('%', #{arg0}, '%')
AND status = #{arg1}
```

### 3. 对象参数

```java
int insert(User user);
```
```xml
<insert id="insert">
    INSERT INTO sys_user(username, nickname, age)
    VALUES(#{username}, #{nickname}, #{age})
    <!-- 直接写对象属性名 -->
</insert>
```

### 4. Map 参数

```java
List<User> search(Map<String, Object> params);
```
```xml
<select id="search" resultMap="userResultMap">
    SELECT * FROM sys_user
    WHERE nickname = #{nickname}
      AND age = #{age}
</select>
```

### 5. 集合参数

```java
int deleteBatch(@Param("ids") List<Long> ids);
```
```xml
<delete id="deleteBatch">
    DELETE FROM sys_user WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</delete>
```

---

## 五、结果映射

### 1. resultType（自动映射）

**规则：**
- 字段名与属性名**一致**，自动映射
- 开启 `mapUnderscoreToCamelCase` 后，`create_time` → `createTime`

```xml
<!-- 开启驼峰后，可直接用 resultType -->
<select id="findById" resultType="User">
    SELECT * FROM sys_user WHERE id = #{id}
</select>
```

**返回基本类型或 Map：**
```xml
<select id="count" resultType="long">
    SELECT COUNT(*) FROM sys_user
</select>

<select id="findMap" resultType="map">
    SELECT id, username FROM sys_user WHERE id = #{id}
</select>
```

### 2. resultMap（手动映射）

**字段名与属性名不一致**，或**复杂关联**时使用。

```xml
<resultMap id="userResultMap" type="User">
    <id column="id" property="id"/>
    <result column="user_name" property="username"/>
    <result column="create_time" property="createTime"/>
</resultMap>

<select id="findById" resultMap="userResultMap">
    SELECT id, user_name, create_time FROM sys_user WHERE id = #{id}
</select>
```

**resultMap 子标签：**

| 标签 | 作用 |
|------|------|
| `<id>` | 主键映射 |
| `<result>` | 普通字段映射 |
| `<association>` | 一对一关联 |
| `<collection>` | 一对多关联 |
| `<discriminator>` | 鉴别器（少用） |

### 3. 主键回填

```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO sys_user(username, password) VALUES(#{username}, #{password})
</insert>
```
```java
User u = new User();
u.setUsername("test");
mapper.insert(u);
System.out.println(u.getId()); // 自动回填
```

**数据库不支持自增（如 Oracle 序列）：**
```xml
<insert id="insert">
    <selectKey keyProperty="id" resultType="long" order="BEFORE">
        SELECT SEQ_USER.NEXTVAL FROM DUAL
    </selectKey>
    INSERT INTO sys_user(id, username) VALUES(#{id}, #{username})
</insert>
```

---

## 六、用 MyBatis 替换手写 JDBC

### 1. 对比

| 方面 | 手写 JDBC（Day5/Day8） | MyBatis |
|------|------------------------|---------|
| 连接管理 | 手动获取/关闭 | 框架管理 |
| SQL | Java 字符串拼接 | XML 配置 |
| 参数设置 | `ps.setXxx()` 逐个 | `#{}` 自动 |
| 结果映射 | 手动 `mapRow` | 自动/resultMap |
| 动态 SQL | StringBuilder 拼接 | `<if>` `<where>` 等标签 |
| 主键回填 | `getGeneratedKeys` | `useGeneratedKeys` |
| 分页 | 手写 LIMIT | 手写或 PageHelper |
| 代码量 | 多 | 少 |
| 可维护性 | 差 | 好 |

### 2. UserDao 改造

**原来（Day5 手写 JDBC）：**
```java
public User findById(Long id) throws SQLException {
    String sql = "SELECT * FROM sys_user WHERE id = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return mapRow(rs);
        }
    }
    return null;
}
```

**现在（MyBatis）：**
```java
// 接口
User findById(Long id);

// XML
<select id="findById" resultMap="userResultMap">
    SELECT * FROM sys_user WHERE id = #{id}
</select>

// 调用
User user = mapper.findById(id);
```

### 3. Service 层改造

```java
package com.example.service.impl;

import com.example.dao.UserMapper;
import com.example.entity.PageResult;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.service.UserService;
import com.example.util.MD5Util;
import com.example.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class UserServiceImpl implements UserService {

    @Override
    public User login(String username, String password) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            User user = mapper.findByUsername(username);
            if (user == null) return null;
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new BusinessException("账号已被禁用");
            }
            if (!MD5Util.matches(password, user.getPassword())) return null;
            return user;
        }
    }

    @Override
    public PageResult<User> findByPage(String keyword, Integer status, int page, int size) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            int offset = (page - 1) * size;
            List<User> list = mapper.findByPage(keyword, status, offset, size);
            long total = mapper.countByCondition(keyword, status);
            return new PageResult<>(list, total, page, size);
        }
    }

    @Override
    public void save(User user) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            try {
                UserMapper mapper = session.getMapper(UserMapper.class);
                if (mapper.existsUsername(user.getUsername(), null)) {
                    throw new BusinessException("用户名已存在");
                }
                if (user.getPassword() == null || user.getPassword().isEmpty()) {
                    throw new BusinessException("密码不能为空");
                }
                user.setPassword(MD5Util.encrypt(user.getPassword()));
                mapper.insert(user);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw e;
            }
        }
    }

    // 其他方法类似...
}
```

> 💡 目前每个方法都要 `openSession` / `commit` / `rollback`，很啰嗦。Day13 引入 Spring 后会自动管理 SqlSession 和事务。

### 4. 事务处理（当前阶段）

```java
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    try (SqlSession session = MyBatisUtil.openSession()) {
        try {
            UserMapper mapper = session.getMapper(UserMapper.class);
            // 多个操作
            mapper.deduct(fromId, amount);
            mapper.add(toId, amount);
            session.commit();
        } catch (Exception e) {
            session.rollback();
            throw e;
        }
    }
}
```

---

## 七、常见问题与排错

| 问题 | 原因 | 解决 |
|------|------|------|
| `Invalid bound statement` | XML namespace 或 id 不匹配 | 检查 namespace=接口全限定名，id=方法名 |
| `Mapper not found` | XML 未注册 | `mybatis-config.xml` 的 `<mappers>` 添加 |
| 参数找不到 | 多参数没用 `@Param` | 加 `@Param` 注解 |
| 字段映射为 null | 字段名与属性名不一致 | 开启 `mapUnderscoreToCamelCase` 或用 resultMap |
| 主键未回填 | 未设 `useGeneratedKeys` | 加 `useGeneratedKeys="true" keyProperty="id"` |
| 中文乱码 | URL 未设编码 | 加 `characterEncoding=utf8` |
| 时区错误 | 未设 serverTimezone | 加 `serverTimezone=Asia/Shanghai` |
| 增删改无效 | 未提交事务 | `session.commit()` 或用 `openSession(true)` |
| SQL 日志不输出 | 未配 logImpl | 加 `<setting name="logImpl" value="STDOUT_LOGGING"/>` |
| 资源未关闭 | 未 try-with-resources | 用 try-with-resources 包裹 SqlSession |

---

## 八、实践练习

### 练习 1：基础 CRUD
1. 按本文档搭建 MyBatis 环境
2. 编写 UserMapper 接口和 XML
3. 完成增删改查测试

### 练习 2：条件查询
1. 用 `<if>` `<where>` 实现多条件搜索
2. 支持 keyword（用户名/昵称/邮箱）+ status
3. 用 `<foreach>` 实现批量删除

### 练习 3：结果映射
1. 建一张字段名带下划线的表（如 `user_name`）
2. 分别用 resultType（开驼峰）和 resultMap 映射
3. 对比两种方式

### 练习 4：事务验证
1. 实现转账方法（两个 UPDATE）
2. 中间抛异常，验证回滚
3. 观察 commit/rollback 的效果

### 练习 5：替换 Day8 项目
1. 把 user-dao 模块的 UserDao 改为 MyBatis Mapper
2. 删除手写的 JDBC 代码
3. 保留 DBUtil 用于 Spring 整合前的过渡

### 练习 6（进阶）：注解方式
```java
@Select("SELECT * FROM sys_user WHERE id = #{id}")
@Results({
    @Result(column = "create_time", property = "createTime")
})
User findById(Long id);

@Insert("INSERT INTO sys_user(username, password) VALUES(#{username}, #{password})")
@Options(useGeneratedKeys = true, keyProperty = "id")
int insert(User user);
```
用注解实现同样的功能，对比 XML 方式。

---

## 九、总结与拓展

### 今日总结
- **ORM** 把 Java 对象与数据库表映射，MyBatis 是半自动 ORM
- **MyBatis 核心对象**：SqlSessionFactoryBuilder → SqlSessionFactory → SqlSession → Mapper
- **配置**：`mybatis-config.xml` 全局配置，Mapper XML 写 SQL
- **参数**：单参数 `#{任意名}`，多参数用 `@Param`
- **`#{}` vs `${}`**：`#{}` 预编译防注入（值），`${}` 字符串拼接（表名/列名）
- **结果映射**：resultType（自动）vs resultMap（手动/关联）
- **动态 SQL**：`<if>` `<where>` `<set>` `<foreach>` 等（Day10 详解）
- **主键回填**：`useGeneratedKeys` + `keyProperty`
- **事务**：手动 `commit/rollback`，Day13 交给 Spring

### 核心记忆点
```
配置：mybatis-config.xml + mapper/*.xml
接口 + XML：namespace=接口全限定名，id=方法名
参数：@Param("name") + #{name}
取值：#{值} 安全，${表名/列名} 需白名单
自动映射：mapUnderscoreToCamelCase=true
主键回填：useGeneratedKeys="true" keyProperty="id"
事务：openSession() → commit()/rollback()
动态 SQL：<if> <where> <set> <foreach>
```

### MyBatis 的不足
- XML 与接口分离，需保持同步
- 复杂关联映射配置繁琐
- 分页需手写或引入 PageHelper
- 事务管理需手动或 Spring 托管

> 这些将在 Day10（动态 SQL、关联查询、PageHelper）和 Day13（Spring 整合）中解决。

### 拓展阅读
- [MyBatis 官方文档](https://mybatis.org/mybatis-3/zh/index.html)
- [MyBatis 动态 SQL](https://mybatis.org/mybatis-3/zh/dynamic-sql.html)
- [MyBatis-Spring 整合](https://mybatis.org/spring/zh/index.html)
- [MyBatis-Plus](https://baomidou.com/)（MyBatis 增强工具）

### 明日预告
Day10 将学习 **MyBatis 进阶**：动态 SQL 详解（`<if>` `<choose>` `<foreach>` `<sql>`）、一对一/一对多关联查询、延迟加载、一级/二级缓存、PageHelper 分页插件，并完成一个带关联查询的实战。