 

## 学习目标
- 理解 JDBC 的作用与核心 API
- 掌握 JDBC 操作数据库的六步流程
- 熟练使用 PreparedStatement 完成 CRUD
- 理解 SQL 注入原理及防护
- 掌握连接池（HikariCP/Druid）的使用
- 封装通用 DBUtil 工具类

---

## 一、JDBC 概述

### 1. 什么是 JDBC

JDBC（Java Database Connectivity，Java 数据库连接）是 Java 提供的一套**访问数据库的标准 API**。它定义了一组接口，各数据库厂商提供实现（驱动），Java 程序通过 JDBC 接口操作数据库，无需关心底层数据库差异。

**JDBC 架构：**
```
Java 应用
    ↓
JDBC API（java.sql 包）
    ↓
JDBC 驱动（MySQL/Oracle/PostgreSQL 各自实现）
    ↓
数据库
```

**为什么用 JDBC：**
- 统一 API，换数据库只需换驱动
- 标准规范，所有关系型数据库都支持
- 是所有 ORM 框架（MyBatis、Hibernate）的底层基础

### 2. JDBC 核心 API

| 接口/类 | 作用 |
|---------|------|
| `DriverManager` | 管理驱动，获取数据库连接 |
| `Connection` | 数据库连接对象 |
| `Statement` | 执行静态 SQL |
| `PreparedStatement` | 执行预编译 SQL（推荐） |
| `CallableStatement` | 执行存储过程 |
| `ResultSet` | 查询结果集 |
| `SQLException` | 数据库异常 |
| `DataSource` | 数据源（连接池） |

### 3. JDBC 驱动

**MySQL 驱动坐标（Maven）：**
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

> 旧版本是 `mysql:mysql-connector-java`，8.x 后改为 `com.mysql:mysql-connector-j`。

**驱动类名：**
- MySQL 8.x：`com.mysql.cj.jdbc.Driver`
- MySQL 5.x：`com.mysql.jdbc.Driver`

**URL 格式：**
```
jdbc:mysql://主机:端口/数据库名?参数1=值1&参数2=值2
```

**常用参数：**
| 参数 | 说明 |
|------|------|
| `useSSL=false` | 关闭 SSL（本地开发） |
| `serverTimezone=UTC` | 时区设置（8.x 必须） |
| `characterEncoding=utf8` | 字符编码 |
| `useUnicode=true` | 使用 Unicode |
| `allowPublicKeyRetrieval=true` | 允许公钥检索 |
| `rewriteBatchedStatements=true` | 批量操作优化 |

**完整示例：**
```
jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
```

---

## 二、JDBC 六步操作

### 完整流程

```java
// 1. 加载驱动（JDBC 4+ 可省略，SPI 自动加载）
Class.forName("com.mysql.cj.jdbc.Driver");

// 2. 获取连接
String url = "jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
Connection conn = DriverManager.getConnection(url, "root", "123456");

// 3. 创建 PreparedStatement
String sql = "SELECT id, name, age FROM user WHERE age > ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setInt(1, 18);

// 4. 执行 SQL
ResultSet rs = ps.executeQuery();  // 查询用 executeQuery
// int rows = ps.executeUpdate();  // 增删改用 executeUpdate

// 5. 处理结果
while (rs.next()) {
    long id = rs.getLong("id");
    String name = rs.getString("name");
    int age = rs.getInt("age");
    System.out.println(id + " - " + name + " - " + age);
}

// 6. 释放资源（后开先关）
rs.close();
ps.close();
conn.close();
```

### 用 try-with-resources 简化

```java
String sql = "SELECT * FROM user WHERE age > ?";
try (Connection conn = DriverManager.getConnection(url, user, pwd);
     PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 18);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            // 处理结果
        }
    }
} catch (SQLException e) {
    e.printStackTrace();
}
```

> 💡 `Connection`、`Statement`、`ResultSet` 都实现了 `AutoCloseable`，用 try-with-resources 可自动关闭。

---

## 三、CRUD 操作

### 1. 查询（SELECT）

**单条查询：**
```java
public User findById(Long id) throws SQLException {
    String sql = "SELECT id, name, age, email FROM user WHERE id = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        }
    }
    return null;
}
```

**列表查询：**
```java
public List<User> findAll() throws SQLException {
    String sql = "SELECT id, name, age, email FROM user ORDER BY id";
    List<User> list = new ArrayList<>();
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            list.add(mapRow(rs));
        }
    }
    return list;
}
```

**结果集映射方法：**
```java
private User mapRow(ResultSet rs) throws SQLException {
    User u = new User();
    u.setId(rs.getLong("id"));
    u.setName(rs.getString("name"));
    u.setAge(rs.getInt("age"));
    u.setEmail(rs.getString("email"));
    return u;
}
```

### 2. 插入（INSERT）

```java
public Long insert(User user) throws SQLException {
    String sql = "INSERT INTO user(name, age, email) VALUES(?, ?, ?)";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, user.getName());
        ps.setInt(2, user.getAge());
        ps.setString(3, user.getEmail());

        int rows = ps.executeUpdate();
        if (rows > 0) {
            // 获取自增主键
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return null;
    }
}
```

### 3. 更新（UPDATE）

```java
public int update(User user) throws SQLException {
    String sql = "UPDATE user SET name = ?, age = ?, email = ? WHERE id = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, user.getName());
        ps.setInt(2, user.getAge());
        ps.setString(3, user.getEmail());
        ps.setLong(4, user.getId());
        return ps.executeUpdate();
    }
}
```

### 4. 删除（DELETE）

```java
public int delete(Long id) throws SQLException {
    String sql = "DELETE FROM user WHERE id = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, id);
        return ps.executeUpdate();
    }
}
```

### 5. 统计查询

```java
public int count() throws SQLException {
    String sql = "SELECT COUNT(*) FROM user";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            return rs.getInt(1);
        }
    }
    return 0;
}
```

### 6. 事务操作

```java
public void transfer(Long fromId, Long toId, BigDecimal amount) throws SQLException {
    Connection conn = null;
    try {
        conn = DBUtil.getConnection();
        conn.setAutoCommit(false); // 开启事务

        // 扣款
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE account SET balance = balance - ? WHERE id = ?")) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, fromId);
            ps.executeUpdate();
        }

        // 入账
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE account SET balance = balance + ? WHERE id = ?")) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, toId);
            ps.executeUpdate();
        }

        conn.commit(); // 提交
    } catch (SQLException e) {
        if (conn != null) conn.rollback(); // 回滚
        throw e;
    } finally {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
```

---

## 四、PreparedStatement vs Statement

### 1. Statement 的问题

**问题一：SQL 注入**

```java
// 危险写法！
String username = req.getParameter("username");
String password = req.getParameter("password");
String sql = "SELECT * FROM user WHERE username = '" + username
           + "' AND password = '" + password + "'";
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);
if (rs.next()) {
    // 登录成功
}
```

**攻击示例：** 用户在密码框输入 `' OR '1'='1`，SQL 变成：
```sql
SELECT * FROM user WHERE username = 'admin' AND password = '' OR '1'='1'
```
`'1'='1'` 恒真，直接绕过验证登录成功！

**问题二：性能差**
- Statement 每次执行都要编译 SQL
- PreparedStatement 预编译后缓存，重复执行更快

**问题三：数据类型处理麻烦**
- Statement 需手动拼接字符串、日期格式
- PreparedStatement 用 `setXxx()` 自动处理

### 2. PreparedStatement 优势

```java
String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, username);
ps.setString(2, password);
ResultSet rs = ps.executeQuery();
```

即使输入 `' OR '1'='1`，也会被当作**普通字符串**处理，无法改变 SQL 结构。

### 3. 对比总结

| 特性 | Statement | PreparedStatement |
|------|-----------|-------------------|
| SQL 注入 | ❌ 有风险 | ✅ 预编译防注入 |
| 性能 | 每次编译 | 预编译缓存 |
| 参数 | 字符串拼接 | `setXxx()` |
| 可读性 | 差 | 好 |
| 类型安全 | 无 | 有 |
| 二进制数据 | 麻烦 | `setBytes()` |
| 推荐度 | ❌ 不推荐 | ✅ 强烈推荐 |

> 💡 **例外**：表名、列名、ORDER BY 字段不能用 `?` 占位，只能用字符串拼接（需白名单校验）。

---

## 五、连接池

### 1. 为什么用连接池

**不用连接池的问题：**
- 每次操作都创建/关闭连接，开销大（TCP 握手、认证）
- 高并发时连接数暴涨，数据库扛不住
- 连接创建耗时（几十到几百毫秒）

**连接池的作用：**
- 预先创建一批连接放入池中
- 用时从池中取，用完归还
- 复用连接，提升性能
- 控制最大连接数，保护数据库

**连接池工作流程：**
```
应用启动 → 创建 N 个连接放入池
    ↓
请求 → 从池中借连接 → 使用 → 归还
    ↓
池满 → 等待或拒绝
```

### 2. HikariCP（推荐）

**特点：** 性能最快、轻量、Spring Boot 默认连接池。

**依赖：**
```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>2.0.13</version>
</dependency>
```

**配置使用：**
```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8");
config.setUsername("root");
config.setPassword("123456");
config.setDriverClassName("com.mysql.cj.jdbc.Driver");

// 连接池参数
config.setMaximumPoolSize(10);      // 最大连接数
config.setMinimumIdle(5);           // 最小空闲连接
config.setConnectionTimeout(30000); // 连接超时（毫秒）
config.setIdleTimeout(600000);      // 空闲超时
config.setMaxLifetime(1800000);     // 连接最大存活时间

HikariDataSource ds = new HikariDataSource(config);

// 使用
try (Connection conn = ds.getConnection()) {
    // 操作数据库
}
```

### 3. Druid（阿里）

**特点：** 功能丰富，自带监控页面。

**依赖：**
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.2.22</version>
</dependency>
```

**配置使用：**
```java
DruidDataSource ds = new DruidDataSource();
ds.setUrl("jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8");
ds.setUsername("root");
ds.setPassword("123456");
ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
ds.setInitialSize(5);
ds.setMaxActive(10);
ds.setMinIdle(5);
ds.setMaxWait(30000);
```

### 4. HikariCP vs Druid

| 特性 | HikariCP | Druid |
|------|----------|-------|
| 性能 | 最快 | 优秀 |
| 体积 | 极小 | 较大 |
| 监控 | 无内置 | 有监控页面 |
| SQL 防火墙 | 无 | 有 |
| Spring Boot 默认 | ✅ 是 | 否 |
| 推荐场景 | 追求性能 | 需要监控 |

> 💡 一般项目推荐 HikariCP，需要 SQL 监控和防火墙时用 Druid。

### 5. 连接池参数说明

| 参数 | 说明 | 建议值 |
|------|------|--------|
| `maximumPoolSize` | 最大连接数 | CPU 核数 × 2 + 磁盘数 |
| `minimumIdle` | 最小空闲连接 | 与 maximumPoolSize 接近 |
| `connectionTimeout` | 获取连接超时 | 30000ms |
| `idleTimeout` | 空闲连接回收 | 600000ms |
| `maxLifetime` | 连接最大寿命 | 比数据库超时略短 |

---

## 六、封装 DBUtil 工具类

### 1. 基础版（读取配置文件）

**db.properties（放在 `src/main/resources`）：**
```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
jdbc.username=root
jdbc.password=123456
jdbc.maxPoolSize=10
jdbc.minIdle=5
```

**DBUtil.java：**
```java
package com.example.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static HikariDataSource dataSource;

    static {
        try (InputStream is = DBUtil.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            props.load(is);

            HikariConfig config = new HikariConfig();
            config.setDriverClassName(props.getProperty("jdbc.driver"));
            config.setJdbcUrl(props.getProperty("jdbc.url"));
            config.setUsername(props.getProperty("jdbc.username"));
            config.setPassword(props.getProperty("jdbc.password"));
            config.setMaximumPoolSize(
                    Integer.parseInt(props.getProperty("jdbc.maxPoolSize", "10")));
            config.setMinimumIdle(
                    Integer.parseInt(props.getProperty("jdbc.minIdle", "5")));
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("加载数据库配置失败：" + e.getMessage());
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭连接池（应用关闭时调用）
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
```

### 2. 通用查询/更新工具方法（可选）

```java
public class JdbcTemplate {

    /**
     * 通用查询（返回 List<Map>）
     */
    public static List<Map<String, Object>> query(String sql, Object... params)
            throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    result.add(row);
                }
            }
        }
        return result;
    }

    /**
     * 通用更新（增删改）
     */
    public static int update(String sql, Object... params) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        }
    }

    private static void setParams(PreparedStatement ps, Object... params)
            throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }
}
```

### 3. 关闭资源工具

```java
public static void close(AutoCloseable... resources) {
    if (resources == null) return;
    for (AutoCloseable r : resources) {
        if (r != null) {
            try {
                r.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
```

---

## 七、完整 UserDao 实现

### 1. 实体类

```java
package com.example.entity;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String name;
    private Integer age;
    private String email;
    private LocalDateTime createTime;

    // 无参构造
    public User() {}

    // 全参构造
    public User(Long id, String name, Integer age, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
    }

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', age=" + age
                + ", email='" + email + "', createTime=" + createTime + "}";
    }
}
```

### 2. UserDao

```java
package com.example.dao;

import com.example.entity.User;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // ============ 查询 ============

    public User findById(Long id) throws SQLException {
        String sql = "SELECT id, name, age, email, create_time FROM user WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, name, age, email, create_time FROM user ORDER BY id";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** 分页查询 */
    public List<User> findByPage(int page, int size) throws SQLException {
        String sql = "SELECT id, name, age, email, create_time FROM user " +
                     "ORDER BY id LIMIT ? OFFSET ?";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** 条件查询 */
    public List<User> search(String name, Integer minAge, Integer maxAge) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, name, age, email, create_time FROM user WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + name + "%");
        }
        if (minAge != null) {
            sql.append(" AND age >= ?");
            params.add(minAge);
        }
        if (maxAge != null) {
            sql.append(" AND age <= ?");
            params.add(maxAge);
        }
        sql.append(" ORDER BY id");

        List<User> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ============ 增删改 ============

    public Long insert(User user) throws SQLException {
        String sql = "INSERT INTO user(name, age, email) VALUES(?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setInt(2, user.getAge());
            ps.setString(3, user.getEmail());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getLong(1);
                }
            }
        }
        return null;
    }

    public int update(User user) throws SQLException {
        String sql = "UPDATE user SET name = ?, age = ?, email = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setInt(2, user.getAge());
            ps.setString(3, user.getEmail());
            ps.setLong(4, user.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(Long id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }

    /** 批量插入 */
    public int batchInsert(List<User> users) throws SQLException {
        String sql = "INSERT INTO user(name, age, email) VALUES(?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (User u : users) {
                ps.setString(1, u.getName());
                ps.setInt(2, u.getAge());
                ps.setString(3, u.getEmail());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            conn.commit();
            return results.length;
        }
    }

    // ============ 结果映射 ============

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setName(rs.getString("name"));
        u.setAge(rs.getInt("age"));
        u.setEmail(rs.getString("email"));
        Timestamp ts = rs.getTimestamp("create_time");
        if (ts != null) u.setCreateTime(ts.toLocalDateTime());
        return u;
    }
}
```

### 3. 建表 SQL

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    age INT,
    email VARCHAR(100) UNIQUE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 4. 测试类

```java
package com.example;

import com.example.dao.UserDao;
import com.example.entity.User;
import com.example.util.DBUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class UserDaoTest {
    public static void main(String[] args) throws SQLException {
        UserDao dao = new UserDao();

        // 插入
        User u = new User();
        u.setName("张三");
        u.setAge(25);
        u.setEmail("zhangsan@example.com");
        Long id = dao.insert(u);
        System.out.println("插入成功，ID = " + id);

        // 查询单条
        User found = dao.findById(id);
        System.out.println("查询结果：" + found);

        // 更新
        found.setAge(26);
        dao.update(found);
        System.out.println("更新后：" + dao.findById(id));

        // 列表
        System.out.println("总数：" + dao.count());
        dao.findAll().forEach(System.out::println);

        // 条件查询
        System.out.println("搜索 '张'：");
        dao.search("张", null, null).forEach(System.out::println);

        // 批量插入
        List<User> batch = Arrays.asList(
                new User(null, "李四", 30, "lisi@example.com"),
                new User(null, "王五", 28, "wangwu@example.com")
        );
        dao.batchInsert(batch);
        System.out.println("批量插入后总数：" + dao.count());

        // 删除
        dao.delete(id);
        System.out.println("删除后总数：" + dao.count());

        // 关闭连接池
        DBUtil.close();
    }
}
```

---

## 八、常见问题与排错

| 问题 | 原因 | 解决 |
|------|------|------|
| `ClassNotFoundException` | 驱动未引入 | 添加 mysql-connector 依赖 |
| `No suitable driver found` | URL 格式错误 | 检查 URL 前缀 `jdbc:mysql://` |
| `Access denied` | 用户名密码错误 | 检查配置 |
| `Public Key Retrieval is not allowed` | MySQL 8 安全限制 | URL 加 `allowPublicKeyRetrieval=true` |
| 时区错误 | 未设 serverTimezone | URL 加 `serverTimezone=Asia/Shanghai` |
| 中文乱码 | 编码不一致 | URL 加 `characterEncoding=utf8` |
| 连接泄漏 | 未关闭连接 | 用 try-with-resources |
| `Too many connections` | 连接数超限 | 用连接池，控制 maxPoolSize |
| `ResultSet closed` | 提前关闭 | 检查资源关闭顺序 |
| 事务未回滚 | 未调用 rollback | catch 中显式 rollback |

---

## 九、实践练习

### 练习 1：基础 CRUD
1. 建表 `user(id, name, age, email, create_time)`
2. 编写 `UserDao`，实现增删改查
3. 编写测试类验证每个方法

### 练习 2：条件查询
- 实现 `search(name, minAge, maxAge)` 方法
- 支持任意组合条件（都可为 null）
- 用 `StringBuilder` 动态拼接 SQL + 参数列表

### 练习 3：分页查询
- 实现 `findByPage(page, size)`
- 配合 `count()` 计算总页数
- 打印"第 X 页，共 Y 页"

### 练习 4：事务验证
- 创建 `account(id, name, balance)` 表
- 实现转账方法，模拟异常验证回滚
- 观察异常时两边余额是否都没变

### 练习 5：连接池改造
- 把 `DriverManager` 换成 `HikariCP`
- 用 `db.properties` 管理配置
- 对比改造前后 1000 次查询的耗时

### 练习 6（进阶）：通用 JdbcTemplate
- 封装 `query(sql, params)` 返回 `List<Map>`
- 封装 `update(sql, params)` 返回影响行数
- 用可变参数处理任意数量的参数

---

## 十、总结与拓展

### 今日总结
- **JDBC** 是 Java 访问数据库的标准 API，六步操作为核心
- **PreparedStatement** 预编译防 SQL 注入，性能好，必须使用
- **连接池**（HikariCP/Druid）复用连接，提升性能
- **DBUtil** 封装配置加载和连接获取
- **UserDao** 分层封装数据库操作，业务代码与 SQL 分离
- **事务**通过 `setAutoCommit(false)` + `commit/rollback` 控制

### 核心记忆点
```
六步：加载驱动 → 获取连接 → 创建 PS → 执行 → 处理结果 → 关闭
查询：ps.executeQuery() → ResultSet → while(rs.next())
增删改：ps.executeUpdate() → 返回影响行数
自增主键：Statement.RETURN_GENERATED_KEYS + getGeneratedKeys()
事务：conn.setAutoCommit(false) → commit() / rollback()
防注入：用 ? 占位，ps.setXxx() 赋值
```

### JDBC 的不足
- 样板代码多（连接、关闭、异常处理）
- SQL 与 Java 混在一起，维护难
- 结果映射繁琐（手动 mapRow）
- 无缓存、无懒加载

> 这些不足正是 MyBatis 等 ORM 框架要解决的问题（Day9-10 会学）。

### 拓展阅读
- [Oracle JDBC 官方教程](https://docs.oracle.com/javase/tutorial/jdbc/)
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
- [Druid GitHub](https://github.com/alibaba/druid)
- [MySQL Connector/J 文档](https://dev.mysql.com/doc/connector-j/en/)

