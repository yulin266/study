

## 项目目标

整合 Day1-6 全部所学，完成一个 **Servlet + JSP + JDBC + MySQL** 的用户管理系统。

**涉及知识点：**
- HTTP 请求/响应（Day1）
- Servlet 生命周期、请求响应（Day2）
- 转发/重定向、Session、Filter（Day3）
- JSP、EL、JSTL、MVC（Day4）
- JDBC、PreparedStatement、连接池（Day5）
- MySQL 建表、索引、分页查询（Day6）

---

## 一、需求分析

### 1. 功能需求

| 模块 | 功能 |
|------|------|
| 认证 | 登录、注销、登录拦截 |
| 用户管理 | 列表（分页）、搜索、新增、编辑、删除、批量删除 |
| 首页 | 欢迎信息、在线人数、统计 |

### 2. 非功能需求

- 统一 UTF-8 编码
- 分层架构（Controller / Service / DAO / Entity）
- 连接池管理数据库连接
- 分页查询
- 密码加密存储（MD5 + 盐）
- 防 SQL 注入（PreparedStatement）
- 防 XSS（JSTL escapeXml）

### 3. 页面清单

| 页面 | 路径 | 说明 |
|------|------|------|
| 登录页 | `/WEB-INF/views/login.jsp` | 用户名密码表单 |
| 用户列表 | `/WEB-INF/views/user-list.jsp` | 分页、搜索、操作按钮 |
| 用户表单 | `/WEB-INF/views/user-form.jsp` | 新增/编辑共用 |
| 错误页 | `/WEB-INF/views/error.jsp` | 统一错误展示 |

---

## 二、项目结构

```
user-management/
├── pom.xml
└── src/main/
    ├── java/com/example/
    │   ├── entity/
    │   │   ├── User.java
    │   │   └── PageResult.java
    │   ├── dao/
    │   │   └── UserDao.java
    │   ├── service/
    │   │   └── UserService.java
    │   ├── servlet/
    │   │   ├── LoginServlet.java
    │   │   ├── LogoutServlet.java
    │   │   └── UserServlet.java
    │   ├── filter/
    │   │   ├── EncodingFilter.java
    │   │   └── AuthFilter.java
    │   ├── listener/
    │   │   └── OnlineCountListener.java
    │   └── util/
    │       ├── DBUtil.java
    │       └── MD5Util.java
    ├── resources/
    │   └── db.properties
    └── webapp/
        ├── WEB-INF/
        │   ├── web.xml
        │   └── views/
        │       ├── login.jsp
        │       ├── user-list.jsp
        │       ├── user-form.jsp
        │       └── error.jsp
        ├── static/
        │   └── css/
        │       └── style.css
        └── index.jsp
```

---

## 三、环境与依赖

### 1. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>user-management</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Servlet API -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>

        <!-- JSP API -->
        <dependency>
            <groupId>jakarta.servlet.jsp</groupId>
            <artifactId>jakarta.servlet.jsp-api</artifactId>
            <version>3.1.0</version>
            <scope>provided</scope>
        </dependency>

        <!-- JSTL -->
        <dependency>
            <groupId>jakarta.servlet.jsp.jstl</groupId>
            <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
            <version>3.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.web</groupId>
            <artifactId>jakarta.servlet.jsp.jstl</artifactId>
            <version>3.0.1</version>
        </dependency>

        <!-- MySQL 驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>

        <!-- HikariCP 连接池 -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.1.0</version>
        </dependency>

        <!-- 日志 -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.13</version>
        </dependency>
    </dependencies>

    <build>
        <finalName>user-management</finalName>
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

### 2. 数据库建表

```sql
CREATE DATABASE IF NOT EXISTS user_mgmt
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE user_mgmt;

CREATE TABLE `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(64) NOT NULL COMMENT '密码（MD5加盐）',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `age` INT COMMENT '年龄',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_nickname` (`nickname`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 初始化管理员（密码 123456，MD5 加盐后为固定值）
INSERT INTO sys_user (username, password, nickname, age, email, status)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 30, 'admin@example.com', 1);

-- 批量测试数据
INSERT INTO sys_user (username, password, nickname, age, email) VALUES
('user01', 'e10adc3949ba59abbe56e057f20f883e', '张三', 25, 'user01@example.com'),
('user02', 'e10adc3949ba59abbe56e057f20f883e', '李四', 28, 'user02@example.com'),
('user03', 'e10adc3949ba59abbe56e057f20f883e', '王五', 32, 'user03@example.com'),
('user04', 'e10adc3949ba59abbe56e057f20f883e', '赵六', 22, 'user04@example.com'),
('user05', 'e10adc3949ba59abbe56e057f20f883e', '钱七', 35, 'user05@example.com');
```

### 3. db.properties

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/user_mgmt?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=123456
jdbc.maxPoolSize=10
jdbc.minIdle=5
```

---

## 四、工具类

### 1. DBUtil.java

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
            if (is == null) {
                throw new IOException("找不到 db.properties");
            }
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
            config.setPoolName("UserMgmtPool");

            dataSource = new HikariDataSource(config);
            System.out.println("数据库连接池初始化完成");
        } catch (IOException e) {
            throw new ExceptionInInitializerError("加载数据库配置失败：" + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
```

### 2. MD5Util.java

```java
package com.example.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    private static final String SALT = "user_mgmt_2024";

    public static String encrypt(String password) {
        try {
            String withSalt = password + SALT;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(withSalt.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }

    public static boolean matches(String rawPassword, String encrypted) {
        return encrypt(rawPassword).equals(encrypted);
    }

    public static void main(String[] args) {
        // 验证：123456 加盐后的值
        System.out.println(encrypt("123456"));
    }
}
```

> ⚠️ 实际生产应使用 **BCrypt** 或 **PBKDF2**，MD5 已不安全，仅用于教学演示。

---

## 五、实体类

### 1. User.java

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

    public User() {}

    public User(String username, String nickname, Integer age, String email) {
        this.username = username;
        this.nickname = nickname;
        this.age = age;
        this.email = email;
    }

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

### 2. PageResult.java（分页结果封装）

```java
package com.example.entity;

import java.util.List;

public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public PageResult(List<T> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    public List<T> getList() { return list; }
    public long getTotal() { return total; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotalPages() { return totalPages; }

    public boolean isHasPrev() { return page > 1; }
    public boolean isHasNext() { return page < totalPages; }
    public int getPrevPage() { return Math.max(1, page - 1); }
    public int getNextPage() { return Math.min(totalPages, page + 1); }
}
```

---

## 六、DAO 层

### UserDao.java

```java
package com.example.dao;

import com.example.entity.User;
import com.example.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // ============ 认证 ============

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM sys_user WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ============ 查询 ============

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

    public List<User> findByPage(String keyword, Integer status, int page, int size)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_user WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR nickname LIKE ? OR email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

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

    public long countByCondition(String keyword, Integer status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sys_user WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR nickname LIKE ? OR email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0;
    }

    public boolean existsUsername(String username, Long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sys_user WHERE username = ?"
                + (excludeId != null ? " AND id != ?" : "");
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            if (excludeId != null) ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1) > 0;
            }
        }
        return false;
    }

    // ============ 增删改 ============

    public Long insert(User user) throws SQLException {
        String sql = "INSERT INTO sys_user(username, password, nickname, age, email, phone, status) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNickname());
            ps.setObject(4, user.getAge());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setInt(7, user.getStatus() == null ? 1 : user.getStatus());

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
        String sql = "UPDATE sys_user SET nickname = ?, age = ?, email = ?, phone = ?, status = ? "
                + "WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNickname());
            ps.setObject(2, user.getAge());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setInt(5, user.getStatus() == null ? 1 : user.getStatus());
            ps.setLong(6, user.getId());
            return ps.executeUpdate();
        }
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM sys_user WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }

    public int deleteBatch(List<Long> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return 0;
        StringBuilder sql = new StringBuilder("DELETE FROM sys_user WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setLong(i + 1, ids.get(i));
            }
            return ps.executeUpdate();
        }
    }

    // ============ 结果映射 ============

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setNickname(rs.getString("nickname"));
        u.setAge((Integer) rs.getObject("age"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setStatus(rs.getInt("status"));
        Timestamp ct = rs.getTimestamp("create_time");
        if (ct != null) u.setCreateTime(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("update_time");
        if (ut != null) u.setUpdateTime(ut.toLocalDateTime());
        return u;
    }
}
```

---

## 七、Service 层

### UserService.java

```java
package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.PageResult;
import com.example.entity.User;
import com.example.util.MD5Util;

import java.sql.SQLException;
import java.util.List;

public class UserService {

    private final UserDao userDao = new UserDao();

    // ============ 认证 ============

    /**
     * 登录校验
     * @return 成功返回 User，失败返回 null
     */
    public User login(String username, String password) throws SQLException {
        User user = userDao.findByUsername(username);
        if (user == null) return null;
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!MD5Util.matches(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    // ============ 查询 ============

    public User findById(Long id) throws SQLException {
        return userDao.findById(id);
    }

    public PageResult<User> findByPage(String keyword, Integer status, int page, int size)
            throws SQLException {
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        List<User> list = userDao.findByPage(keyword, status, page, size);
        long total = userDao.countByCondition(keyword, status);
        return new PageResult<>(list, total, page, size);
    }

    // ============ 增删改 ============

    public void save(User user) throws SQLException {
        // 校验用户名唯一
        if (userDao.existsUsername(user.getUsername(), null)) {
            throw new RuntimeException("用户名已存在");
        }
        // 密码加密
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        user.setPassword(MD5Util.encrypt(user.getPassword()));
        userDao.insert(user);
    }

    public void update(User user) throws SQLException {
        User exist = userDao.findById(user.getId());
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }
        // 用户名不可修改（简单处理）
        userDao.update(user);
    }

    public void delete(Long id) throws SQLException {
        User user = userDao.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException("管理员账号不可删除");
        }
        userDao.deleteById(id);
    }

    public int deleteBatch(List<Long> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return 0;
        // 过滤 admin
        User admin = userDao.findByUsername("admin");
        if (admin != null) {
            ids.removeIf(id -> id.equals(admin.getId()));
        }
        return userDao.deleteBatch(ids);
    }
}
```

---

## 八、Filter 与 Listener

### 1. EncodingFilter.java

```java
package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/*")
public class EncodingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        chain.doFilter(req, resp);
    }
}
```

### 2. AuthFilter.java

```java
package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

@WebFilter("/*")
public class AuthFilter implements Filter {

    // 白名单：不需要登录
    private static final Set<String> WHITE_LIST = Set.of(
            "/", "/index.jsp", "/login", "/logout",
            "/static", "/favicon.ico"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String path = request.getRequestURI()
                .substring(request.getContextPath().length());

        // 白名单放行
        if (isWhiteList(path)) {
            chain.doFilter(req, resp);
            return;
        }

        // 检查 Session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        chain.doFilter(req, resp);
    }

    private boolean isWhiteList(String path) {
        if (path.isEmpty() || "/".equals(path)) return true;
        for (String w : WHITE_LIST) {
            if (path.startsWith(w)) return true;
        }
        // 静态资源
        return path.startsWith("/static/") || path.equals("/favicon.ico");
    }
}
```

### 3. OnlineCountListener.java

```java
package com.example.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.*;

@WebListener
public class OnlineCountListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        ServletContext ctx = se.getSession().getServletContext();
        Integer count = (Integer) ctx.getAttribute("onlineCount");
        ctx.setAttribute("onlineCount", count == null ? 1 : count + 1);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        ServletContext ctx = se.getSession().getServletContext();
        Integer count = (Integer) ctx.getAttribute("onlineCount");
        if (count != null && count > 0) {
            ctx.setAttribute("onlineCount", count - 1);
        }
    }
}
```

---

## 九、Servlet 层

### 1. LoginServlet.java

```java
package com.example.servlet;

import com.example.entity.User;
import com.example.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            req.setAttribute("error", "用户名和密码不能为空");
            req.setAttribute("username", username);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        try {
            User user = userService.login(username.trim(), password);
            if (user == null) {
                req.setAttribute("error", "用户名或密码错误");
                req.setAttribute("username", username);
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }
            // 登录成功
            HttpSession session = req.getSession();
            session.setAttribute("loginUser", user);
            resp.sendRedirect(req.getContextPath() + "/users");
        } catch (RuntimeException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("username", username);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("登录失败", e);
        }
    }
}
```

### 2. LogoutServlet.java

```java
package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
```

### 3. UserServlet.java（核心控制器）

```java
package com.example.servlet;

import com.example.entity.PageResult;
import com.example.entity.User;
import com.example.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/users/*")
public class UserServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo) || "/list".equals(pathInfo)) {
                list(req, resp);
            } else if ("/add".equals(pathInfo)) {
                showForm(req, resp, null);
            } else if ("/edit".equals(pathInfo)) {
                showForm(req, resp, parseLong(req.getParameter("id")));
            } else if ("/delete".equals(pathInfo)) {
                delete(req, resp);
            } else {
                resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if ("/save".equals(pathInfo)) {
                save(req, resp);
            } else if ("/update".equals(pathInfo)) {
                update(req, resp);
            } else if ("/batchDelete".equals(pathInfo)) {
                batchDelete(req, resp);
            } else {
                resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ============ 列表 ============

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        int page = parseInt(req.getParameter("page"), 1);
        int size = parseInt(req.getParameter("size"), 10);
        String keyword = req.getParameter("keyword");
        Integer status = parseInteger(req.getParameter("status"));

        PageResult<User> pageResult = userService.findByPage(keyword, status, page, size);

        req.setAttribute("pageResult", pageResult);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.getRequestDispatcher("/WEB-INF/views/user-list.jsp").forward(req, resp);
    }

    // ============ 表单 ============

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Long id)
            throws SQLException, ServletException, IOException {
        if (id != null) {
            User user = userService.findById(id);
            if (user == null) {
                resp.sendError(404, "用户不存在");
                return;
            }
            req.setAttribute("user", user);
        }
        req.getRequestDispatcher("/WEB-INF/views/user-form.jsp").forward(req, resp);
    }

    // ============ 新增 ============

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        User user = buildUserFromRequest(req, false);
        try {
            userService.save(user);
            resp.sendRedirect(req.getContextPath() + "/users");
        } catch (RuntimeException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("user", user);
            req.getRequestDispatcher("/WEB-INF/views/user-form.jsp").forward(req, resp);
        }
    }

    // ============ 编辑 ============

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Long id = parseLong(req.getParameter("id"));
        if (id == null) {
            resp.sendError(400, "缺少 ID");
            return;
        }
        User user = buildUserFromRequest(req, true);
        user.setId(id);
        try {
            userService.update(user);
            resp.sendRedirect(req.getContextPath() + "/users");
        } catch (RuntimeException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("user", user);
            req.getRequestDispatcher("/WEB-INF/views/user-form.jsp").forward(req, resp);
        }
    }

    // ============ 删除 ============

    private void delete(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Long id = parseLong(req.getParameter("id"));
        if (id == null) {
            resp.sendError(400, "缺少 ID");
            return;
        }
        try {
            userService.delete(id);
        } catch (RuntimeException e) {
            // 可选：把错误信息存到 session，重定向后展示
        }
        resp.sendRedirect(req.getContextPath() + "/users");
    }

    // ============ 批量删除 ============

    private void batchDelete(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        String[] idArr = req.getParameterValues("ids");
        if (idArr == null || idArr.length == 0) {
            resp.sendRedirect(req.getContextPath() + "/users");
            return;
        }
        List<Long> ids = Arrays.stream(idArr)
                .filter(s -> s != null && !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
        try {
            userService.deleteBatch(ids);
        } catch (RuntimeException ignored) {
        }
        resp.sendRedirect(req.getContextPath() + "/users");
    }

    // ============ 工具方法 ============

    private User buildUserFromRequest(HttpServletRequest req, boolean isEdit) {
        User user = new User();
        user.setUsername(req.getParameter("username"));
        user.setPassword(req.getParameter("password"));
        user.setNickname(req.getParameter("nickname"));
        user.setAge(parseInteger(req.getParameter("age")));
        user.setEmail(req.getParameter("email"));
        user.setPhone(req.getParameter("phone"));
        user.setStatus(parseInt(req.getParameter("status"), 1));
        return user;
    }

    private int parseInt(String s, int defaultValue) {
        if (s == null || s.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer parseInteger(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
```

---

## 十、JSP 视图

### 1. login.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>登录 - 用户管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body class="login-page">
    <div class="login-box">
        <h2>用户管理系统</h2>
        <c:if test="${not empty error}">
            <p class="error">${error}</p>
        </c:if>
        <form action="${pageContext.request.contextPath}/login" method="post">
            <div class="form-group">
                <label>用户名</label>
                <input type="text" name="username" value="${username}" required autofocus>
            </div>
            <div class="form-group">
                <label>密码</label>
                <input type="password" name="password" required>
            </div>
            <button type="submit" class="btn btn-primary btn-block">登录</button>
        </form>
        <p class="tip">测试账号：admin / 123456</p>
    </div>
</body>
</html>
```

### 2. user-list.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>用户列表</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<div class="container">
    <header class="header">
        <h1>用户管理系统</h1>
        <div class="user-info">
            <span>欢迎，${sessionScope.loginUser.nickname}</span>
            <span>在线：${applicationScope.onlineCount}</span>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm">退出</a>
        </div>
    </header>

    <!-- 搜索栏 -->
    <form action="${pageContext.request.contextPath}/users" method="get" class="search-form">
        <input type="text" name="keyword" value="${keyword}" placeholder="用户名/昵称/邮箱">
        <select name="status">
            <option value="">全部状态</option>
            <option value="1" ${status == 1 ? 'selected' : ''}>正常</option>
            <option value="0" ${status == 0 ? 'selected' : ''}>禁用</option>
        </select>
        <button type="submit" class="btn btn-primary">搜索</button>
        <a href="${pageContext.request.contextPath}/users/add" class="btn btn-success">新增用户</a>
    </form>

    <!-- 用户表格 -->
    <form action="${pageContext.request.contextPath}/users/batchDelete" method="post" id="batchForm">
        <table class="data-table">
            <thead>
                <tr>
                    <th><input type="checkbox" id="checkAll"></th>
                    <th>ID</th>
                    <th>用户名</th>
                    <th>昵称</th>
                    <th>年龄</th>
                    <th>邮箱</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty pageResult.list}">
                        <tr><td colspan="9" class="empty">暂无数据</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${pageResult.list}" var="u">
                            <tr>
                                <td><input type="checkbox" name="ids" value="${u.id}"></td>
                                <td>${u.id}</td>
                                <td>${u.username}</td>
                                <td>${u.nickname}</td>
                                <td>${u.age}</td>
                                <td>${u.email}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${u.status == 1}">
                                            <span class="badge badge-success">正常</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-danger">禁用</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <fmt:formatDate value="${u.createTime}"
                                        pattern="yyyy-MM-dd HH:mm" />
                                </td>
                                <td class="actions">
                                    <a href="${pageContext.request.contextPath}/users/edit?id=${u.id}"
                                       class="btn btn-sm btn-primary">编辑</a>
                                    <a href="javascript:void(0)"
                                       onclick="confirmDelete(${u.id})"
                                       class="btn btn-sm btn-danger">删除</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
        <div class="batch-actions">
            <button type="button" class="btn btn-danger" onclick="batchDelete()">批量删除</button>
        </div>
    </form>

    <!-- 分页 -->
    <c:if test="${pageResult.totalPages > 1}">
        <div class="pagination">
            <span>共 ${pageResult.total} 条 / ${pageResult.totalPages} 页</span>

            <c:if test="${pageResult.hasPrev}">
                <a href="${pageContext.request.contextPath}/users?page=${pageResult.prevPage}&keyword=${keyword}&status=${status}">上一页</a>
            </c:if>

            <c:forEach begin="1" end="${pageResult.totalPages}" var="i">
                <c:choose>
                    <c:when test="${i == pageResult.page}">
                        <span class="current">${i}</span>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/users?page=${i}&keyword=${keyword}&status=${status}">${i}</a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <c:if test="${pageResult.hasNext}">
                <a href="${pageContext.request.contextPath}/users?page=${pageResult.nextPage}&keyword=${keyword}&status=${status}">下一页</a>
            </c:if>
        </div>
    </c:if>
</div>

<script>
    // 全选
    document.getElementById('checkAll').addEventListener('change', function() {
        document.querySelectorAll('input[name="ids"]').forEach(cb => cb.checked = this.checked);
    });

    // 单个删除
    function confirmDelete(id) {
        if (confirm('确认删除该用户？')) {
            location.href = '${pageContext.request.contextPath}/users/delete?id=' + id;
        }
    }

    // 批量删除
    function batchDelete() {
        const checked = document.querySelectorAll('input[name="ids"]:checked');
        if (checked.length === 0) {
            alert('请至少选择一条记录');
            return;
        }
        if (confirm('确认删除选中的 ' + checked.length + ' 条记录？')) {
            document.getElementById('batchForm').submit();
        }
    }
</script>
</body>
</html>
```

### 3. user-form.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>${empty user.id ? '新增' : '编辑'}用户</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
<div class="container">
    <header class="header">
        <h1>${empty user.id ? '新增用户' : '编辑用户'}</h1>
        <a href="${pageContext.request.contextPath}/users" class="btn">返回列表</a>
    </header>

    <c:if test="${not empty error}">
        <p class="error">${error}</p>
    </c:if>

    <form action="${pageContext.request.contextPath}/users/${empty user.id ? 'save' : 'update'}"
          method="post" class="form">
        <c:if test="${not empty user.id}">
            <input type="hidden" name="id" value="${user.id}">
        </c:if>

        <div class="form-group">
            <label>用户名 <span class="required">*</span></label>
            <input type="text" name="username" value="${user.username}"
                   ${empty user.id ? 'required' : 'readonly'} maxlength="50">
        </div>

        <c:if test="${empty user.id}">
            <div class="form-group">
                <label>密码 <span class="required">*</span></label>
                <input type="password" name="password" required minlength="6" maxlength="50">
            </div>
        </c:if>

        <div class="form-group">
            <label>昵称</label>
            <input type="text" name="nickname" value="${user.nickname}" maxlength="50">
        </div>

        <div class="form-group">
            <label>年龄</label>
            <input type="number" name="age" value="${user.age}" min="1" max="150">
        </div>

        <div class="form-group">
            <label>邮箱</label>
            <input type="email" name="email" value="${user.email}" maxlength="100">
        </div>

        <div class="form-group">
            <label>手机号</label>
            <input type="text" name="phone" value="${user.phone}" maxlength="20">
        </div>

        <div class="form-group">
            <label>状态</label>
            <select name="status">
                <option value="1" ${user.status == 1 or empty user.status ? 'selected' : ''}>正常</option>
                <option value="0" ${user.status == 0 ? 'selected' : ''}>禁用</option>
            </select>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn btn-primary">保存</button>
            <a href="${pageContext.request.contextPath}/users" class="btn">取消</a>
        </div>
    </form>
</div>
</body>
</html>
```

### 4. error.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head><title>出错了</title></head>
<body>
    <h1>哎呀，出错了</h1>
    <p>错误信息：${pageContext.exception.message}</p>
    <a href="${pageContext.request.contextPath}/users">返回首页</a>
</body>
</html>
```

### 5. style.css

```css
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, "Microsoft YaHei", sans-serif; background: #f5f5f5; color: #333; }

.container { max-width: 1200px; margin: 0 auto; padding: 20px; }

.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header h1 { font-size: 24px; color: #2c3e50; }
.user-info { display: flex; gap: 15px; align-items: center; }

.search-form { display: flex; gap: 10px; margin-bottom: 20px; background: #fff; padding: 15px; border-radius: 6px; }
.search-form input, .search-form select { padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; }

.data-table { width: 100%; background: #fff; border-collapse: collapse; border-radius: 6px; overflow: hidden; }
.data-table th, .data-table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.data-table th { background: #f8f9fa; font-weight: 600; }
.data-table tr:hover { background: #f8f9fa; }
.data-table .empty { text-align: center; color: #999; padding: 40px; }

.badge { padding: 3px 8px; border-radius: 3px; font-size: 12px; }
.badge-success { background: #d4edda; color: #155724; }
.badge-danger { background: #f8d7da; color: #721c24; }

.btn { display: inline-block; padding: 8px 16px; border: 1px solid #ddd; border-radius: 4px;
       background: #fff; color: #333; text-decoration: none; cursor: pointer; font-size: 14px; }
.btn:hover { background: #f0f0f0; }
.btn-primary { background: #007bff; color: #fff; border-color: #007bff; }
.btn-primary:hover { background: #0056b3; }
.btn-success { background: #28a745; color: #fff; border-color: #28a745; }
.btn-success:hover { background: #1e7e34; }
.btn-danger { background: #dc3545; color: #fff; border-color: #dc3545; }
.btn-danger:hover { background: #b21f2d; }
.btn-sm { padding: 4px 10px; font-size: 12px; }
.btn-block { width: 100%; }

.batch-actions { margin: 15px 0; }

.pagination { display: flex; gap: 5px; align-items: center; margin-top: 20px; }
.pagination a, .pagination span { padding: 6px 12px; border: 1px solid #ddd; border-radius: 4px;
                                   text-decoration: none; color: #333; background: #fff; }
.pagination .current { background: #007bff; color: #fff; border-color: #007bff; }

.form { background: #fff; padding: 30px; border-radius: 6px; max-width: 600px; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 500; }
.form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
.required { color: #dc3545; }
.form-actions { display: flex; gap: 10px; margin-top: 20px; }

.error { color: #dc3545; background: #f8d7da; padding: 10px; border-radius: 4px; margin-bottom: 15px; }

/* 登录页 */
.login-page { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f0f2f5; }
.login-box { background: #fff; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 360px; }
.login-box h2 { text-align: center; margin-bottom: 30px; color: #2c3e50; }
.login-box .form-group { margin-bottom: 20px; }
.login-box .tip { text-align: center; color: #999; font-size: 12px; margin-top: 15px; }
```

---

## 十一、web.xml（可选）

如果全部用注解，`web.xml` 可以只保留基本配置：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
         https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <display-name>用户管理系统</display-name>

    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>

    <session-config>
        <session-timeout>30</session-timeout>
    </session-config>

    <error-page>
        <error-code>404</error-code>
        <location>/WEB-INF/views/error.jsp</location>
    </error-page>
    <error-page>
        <error-code>500</error-code>
        <location>/WEB-INF/views/error.jsp</location>
    </error-page>
    <error-page>
        <exception-type>java.lang.Throwable</exception-type>
        <location>/WEB-INF/views/error.jsp</location>
    </error-page>
</web-app>
```

### index.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<% response.sendRedirect(request.getContextPath() + "/users"); %>
```

---

## 十二、测试与验收

### 1. 启动流程

1. 建库建表，插入初始数据
2. 修改 `db.properties` 中的数据库连接信息
3. IDEA 配置 Tomcat，部署 `user-management:war exploded`
4. 启动 Tomcat，访问 `http://localhost:8080/user-management/`

### 2. 验收清单

| 序号 | 功能 | 验收标准 |
|------|------|----------|
| 1 | 未登录访问 | 自动跳转登录页 |
| 2 | 登录失败 | 显示"用户名或密码错误"，用户名回显 |
| 3 | 登录成功 | 跳转用户列表，显示昵称 |
| 4 | 用户列表 | 分页正常，显示所有字段 |
| 5 | 搜索 | 按用户名/昵称/邮箱模糊查询，状态筛选 |
| 6 | 新增用户 | 用户名重复提示，密码加密存储 |
| 7 | 编辑用户 | 数据回显，用户名不可改 |
| 8 | 删除用户 | 二次确认，admin 不可删 |
| 9 | 批量删除 | 全选/多选，批量删除 |
| 10 | 在线人数 | 不同浏览器打开，人数变化 |
| 11 | 退出登录 | Session 失效，跳登录页 |
| 12 | 中文不乱码 | 所有页面、输入输出正常 |
| 13 | SQL 注入 | 输入 `' OR '1'='1` 无法登录 |

### 3. 常见问题排查

| 问题 | 原因 | 解决 |
|------|------|------|
| 404 | 路径错误 | 检查 `@WebServlet` 和链接 |
| 500 | 空指针/数据库异常 | 看 Tomcat 日志 |
| 中文乱码 | 编码不一致 | 检查 Filter、JSP、数据库字符集 |
| 数据库连不上 | 配置错误 | 检查 `db.properties` |
| 静态资源 404 | Filter 拦截 | 白名单放行 `/static/` |
| 分页不生效 | 参数解析错误 | 检查 `parseInt` 默认值 |
| 删除后跳转 404 | 重定向路径错误 | 加 `contextPath` |
| 登录后仍被拦截 | Session 未正确写入 | 检查 `session.setAttribute` |
| JSTL 标签不识别 | 缺依赖或 uri 错 | 检查 pom 和 taglib |

---

## 十三、优化与拓展

### 1. 代码优化
- 抽取 BaseServlet 统一处理转发、参数解析
- 用 `@WebServlet` 的 `loadOnStartup` 提前初始化
- 用 Filter 统一处理异常，避免每个 Servlet try-catch
- 用反射封装通用 CRUD

### 2. 功能拓展
- **角色权限**：区分管理员、普通用户，不同角色不同权限
- **导出 Excel**：用 POI 导出用户列表
- **文件上传**：用户头像上传
- **修改密码**：用户自己改密码
- **操作日志**：记录谁在何时做了什么
- **软删除**：加 `deleted` 字段，不物理删除

### 3. 前后端分离改造
- Controller 改为 `@RestController` 返回 JSON
- 前端用 Vue/React + axios 调用接口
- 用 Token（JWT）替代 Session

### 4. 引入框架
- 用 **MyBatis** 替代手写 JDBC（Day9）
- 用 **Spring** 管理 Bean 和事务（Day11）
- 用 **Spring MVC** 替代 Servlet（Day12）
- 用 **SSM 整合** 重构整个项目（Day13）

---

## 十四、总结

### 本项目整合的知识点

| Day | 知识点 | 应用 |
|-----|--------|------|
| Day1 | HTTP、RESTful、JSON | 请求响应、状态码 |
| Day2 | Servlet 生命周期 | `@WebServlet`、doGet/doPost |
| Day3 | 转发/重定向、Session、Filter | 登录跳转、会话管理、拦截器 |
| Day4 | JSP、EL、JSTL、MVC | 视图层、EL 取值、JSTL 循环 |
| Day5 | JDBC、PreparedStatement、连接池 | DAO 层、防注入、HikariCP |
| Day6 | MySQL、索引、分页 | 建表、分页查询、模糊搜索 |

### 项目亮点
- ✅ **分层清晰**：Servlet → Service → DAO → Entity
- ✅ **安全**：PreparedStatement 防注入、MD5 加盐、JSTL 转义防 XSS
- ✅ **健壮**：连接池管理、参数校验、异常处理
- ✅ **规范**：统一返回、统一编码、RESTful 风格 URL
- ✅ **完整**：登录、CRUD、分页、搜索、批量操作、在线统计

### 下周预告
第2周将进入**主流框架**：
- Day8：Maven 进阶与项目分层
- Day9：MyBatis 入门（替代手写 JDBC）
- Day10：MyBatis 进阶（动态 SQL、关联查询）
- Day11：Spring 核心（IoC、DI、AOP）
- Day12：Spring MVC（替代 Servlet）
- Day13：SSM 整合（Spring + Spring MVC + MyBatis）
- Day14：综合项目：博客系统

用框架重构今天的项目，代码量会大幅减少，维护性大幅提升。