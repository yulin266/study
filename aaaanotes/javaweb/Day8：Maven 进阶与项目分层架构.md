

## 学习目标

- 深入掌握 Maven 依赖管理、生命周期、插件机制
- 掌握多模块项目的拆分与聚合
- 理解分层架构（Controller / Service / DAO / Entity）与依赖方向
- 掌握 `dependencyManagement` 统一版本管理
- 用 Maven 把 Day7 项目改造成多模块结构

---

## 一、Maven 核心回顾与进阶

### 1. Maven 是什么

Maven 是 Java 项目的**构建工具**与**依赖管理工具**，通过 `pom.xml` 描述项目结构、依赖、插件、构建过程，遵循"约定优于配置"。

**核心功能：**
- 依赖管理（自动下载 jar 及其传递依赖）
- 构建（编译、测试、打包、部署）
- 项目信息管理（坐标、版本、描述）
- 多模块聚合与继承

### 2. Maven 坐标

每个构件由三个坐标唯一标识：

```xml
<groupId>com.example</groupId>        <!-- 组织/公司，反向域名 -->
<artifactId>user-management</artifactId> <!-- 项目/模块名 -->
<version>1.0-SNAPSHOT</version>       <!-- 版本 -->
```

**版本规范：**
| 版本 | 含义 |
|------|------|
| `1.0-SNAPSHOT` | 快照版（开发中，可覆盖） |
| `1.0` / `1.0-RELEASE` | 正式版（不可变） |
| `1.0.1` | 补丁版 |
| `1.1.0` | 小版本 |
| `2.0.0` | 大版本（可能不兼容） |

> 💡 SNAPSHOT 版本会每次拉取最新，RELEASE 版本会缓存到本地。

### 3. 仓库

Maven 从仓库获取依赖，分三类：

| 仓库 | 位置 | 说明 |
|------|------|------|
| 本地仓库 | `~/.m2/repository` | 本机缓存，优先查找 |
| 中央仓库 | repo.maven.apache.org | 官方仓库，全球可用 |
| 私服/镜像 | 公司内部或阿里云 | 加速、私有构件 |

**配置阿里云镜像（`~/.m2/settings.xml`）：**
```xml
<mirrors>
    <mirror>
        <id>aliyunmaven</id>
        <mirrorOf>*</mirrorOf>
        <name>阿里云公共仓库</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

**配置本地仓库位置：**
```xml
<localRepository>D:/maven-repo</localRepository>
```

### 4. 依赖范围（scope）

| scope | 编译 | 测试 | 运行 | 打包 | 典型用途 |
|-------|------|------|------|------|----------|
| **compile**（默认） | ✅ | ✅ | ✅ | ✅ | spring-core、commons-lang3 |
| **provided** | ✅ | ✅ | ✅ | ❌ | servlet-api、lombok |
| **runtime** | ❌ | ✅ | ✅ | ✅ | mysql-connector、jdbc 驱动 |
| **test** | ❌ | ✅ | ❌ | ❌ | junit、mockito |
| **system** | ✅ | ✅ | ✅ | ❌ | 本地 jar（不推荐） |
| **import** | - | - | - | - | 仅用于 `dependencyManagement` 导入 BOM |

> 💡 `provided` 用于容器已提供的依赖（如 Tomcat 自带 servlet-api），打包时排除，避免冲突。

### 5. 依赖传递与冲突

**传递依赖：** A 依赖 B，B 依赖 C，则 A 自动获得 C（范围会收窄）。

**冲突解决原则：**
1. **最短路径优先**：A → B → C(1.0) 与 A → C(2.0)，选 C(2.0)
2. **最先声明优先**：路径长度相同时，pom 中先声明的生效
3. **排除依赖**：显式 `<exclusions>` 排除
4. **锁定版本**：`dependencyManagement` 统一指定

**查看依赖树：**
```bash
mvn dependency:tree
mvn dependency:tree -Dincludes=org.slf4j:*
mvn dependency:analyze        # 分析未使用/未声明依赖
```

**排除依赖示例：**
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>6.1.6</version>
    <exclusions>
        <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 6. 依赖管理（dependencyManagement）

**问题：** 多个模块引用同一依赖，版本不一致，升级困难。

**方案：** 父 pom 用 `dependencyManagement` 统一版本，子模块引用时**不写版本号**。

```xml
<!-- 父 pom -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>6.1.6</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

```xml
<!-- 子模块 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <!-- 不写 version，继承父 pom -->
</dependency>
```

> 💡 `dependencyManagement` 只是声明，不引入依赖；`dependencies` 才是真正引入。

### 7. 生命周期

Maven 有三套生命周期，每套由多个阶段（phase）组成，执行某阶段会自动执行之前的阶段。

**default 生命周期（核心）：**
```
validate → compile → test → package → verify → install → deploy
```

| 阶段 | 作用 |
|------|------|
| validate | 校验项目信息 |
| compile | 编译主代码到 `target/classes` |
| test | 运行单元测试 |
| package | 打包（jar/war）到 `target` |
| verify | 校验包质量 |
| install | 安装到本地仓库 |
| deploy | 部署到远程仓库 |

**clean 生命周期：**
```
pre-clean → clean → post-clean
```

**site 生命周期：** 生成项目文档。

**常用命令：**
```bash
mvn clean                  # 清理 target
mvn compile                # 编译
mvn test                   # 测试
mvn package                # 打包
mvn install                # 安装到本地仓库
mvn clean package          # 清理 + 打包
mvn clean install -DskipTests   # 跳过测试安装
mvn clean package -P prod       # 激活 prod profile
```

### 8. 插件

Maven 通过插件执行任务，常用插件：

| 插件 | 作用 |
|------|------|
| maven-compiler-plugin | 编译，指定 JDK 版本 |
| maven-surefire-plugin | 单元测试 |
| maven-war-plugin | 打 war 包 |
| maven-jar-plugin | 打 jar 包 |
| maven-resources-plugin | 资源处理 |
| maven-assembly-plugin | 打可执行 jar（含依赖） |
| tomcat7-maven-plugin | 内嵌 Tomcat 运行 |

**配置编译插件：**
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <encoding>UTF-8</encoding>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**用属性统一配置（推荐）：**
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

### 9. Profile（环境配置）

不同环境（开发/测试/生产）用不同配置：

```xml
<profiles>
    <profile>
        <id>dev</id>
        <activation><activeByDefault>true</activeByDefault></activation>
        <properties>
            <env>dev</env>
            <jdbc.url>jdbc:mysql://localhost:3306/dev_db</jdbc.url>
        </properties>
    </profile>
    <profile>
        <id>prod</id>
        <properties>
            <env>prod</env>
            <jdbc.url>jdbc:mysql://prod-host:3306/prod_db</jdbc.url>
        </properties>
    </profile>
</profiles>
```

激活：`mvn package -P prod`

---

## 二、多模块项目

### 1. 为什么用多模块

**单模块的问题：**
- 代码量大，编译慢
- 无法复用（其他项目要用只能复制）
- 职责不清，团队协作冲突多
- 无法独立部署部分功能

**多模块的优势：**
- **职责分离**：按层或按功能拆分
- **复用**：公共模块被多个项目引用
- **独立构建**：改一个模块只编译它
- **并行开发**：团队分工清晰

### 2. 父子工程结构

**父工程（packaging=pom）：** 只做依赖管理和模块聚合，不写业务代码。

**子模块：** 继承父工程，各自有职责。

```
user-management-parent/          (父，pom)
├── pom.xml
├── user-common/                 (工具类、通用实体)
│   └── pom.xml
├── user-dao/                    (数据访问)
│   └── pom.xml
├── user-service/                (业务逻辑)
│   └── pom.xml
└── user-web/                    (Controller + JSP，war)
    └── pom.xml
```

**依赖方向：**
```
web → service → dao → common
```
单向依赖，禁止反向，禁止循环。

### 3. 父 pom 示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>user-management-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>user-management-parent</name>
    <description>用户管理系统父工程</description>

    <!-- 聚合子模块 -->
    <modules>
        <module>user-common</module>
        <module>user-dao</module>
        <module>user-service</module>
        <module>user-web</module>
    </modules>

    <!-- 统一属性 -->
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <spring.version>6.1.6</spring.version>
        <mybatis.version>3.5.16</mybatis.version>
        <mysql.version>8.3.0</mysql.version>
        <hikari.version>5.1.0</hikari.version>
        <servlet.version>6.0.0</servlet.version>
        <jstl.version>3.0.0</jstl.version>
        <slf4j.version>2.0.13</slf4j.version>
        <junit.version>5.10.2</junit.version>
    </properties>

    <!-- 统一依赖版本 -->
    <dependencyManagement>
        <dependencies>
            <!-- 内部模块 -->
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>user-common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>user-dao</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>user-service</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- 第三方 -->
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
                <groupId>com.mysql</groupId>
                <artifactId>mysql-connector-j</artifactId>
                <version>${mysql.version}</version>
            </dependency>
            <dependency>
                <groupId>com.zaxxer</groupId>
                <artifactId>HikariCP</artifactId>
                <version>${hikari.version}</version>
            </dependency>
            <dependency>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-simple</artifactId>
                <version>${slf4j.version}</version>
            </dependency>
            <dependency>
                <groupId>org.junit.jupiter</groupId>
                <artifactId>junit-jupiter</artifactId>
                <version>${junit.version}</version>
                <scope>test</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- 所有子模块共用的依赖 -->
    <dependencies>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
        </dependency>
    </dependencies>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.13.0</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-war-plugin</artifactId>
                    <version>3.4.0</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>3.2.5</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

> 💡 `pluginManagement` 与 `dependencyManagement` 类似，声明插件版本，子模块引用时无需写版本。

### 4. user-common 模块

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>user-management-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>user-common</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <!-- 通用工具依赖 -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>
```

**内容：**
- `entity`（User、PageResult）
- `util`（DBUtil、MD5Util）
- `exception`（自定义异常）
- `constant`（常量）

### 5. user-dao 模块

```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>user-management-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>

<artifactId>user-dao</artifactId>
<packaging>jar</packaging>

<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>user-common</artifactId>
    </dependency>
</dependencies>
```

**内容：** `UserDao`

### 6. user-service 模块

```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>user-management-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>

<artifactId>user-service</artifactId>
<packaging>jar</packaging>

<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>user-dao</artifactId>
    </dependency>
</dependencies>
```

**内容：** `UserService`

### 7. user-web 模块

```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>user-management-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>

<artifactId>user-web</artifactId>
<packaging>war</packaging>

<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>user-service</artifactId>
    </dependency>

    <!-- Web 相关 -->
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>jakarta.servlet.jsp.jstl</groupId>
        <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
    </dependency>
    <dependency>
        <groupId>org.glassfish.web</groupId>
        <artifactId>jakarta.servlet.jsp.jstl</artifactId>
        <version>3.0.1</version>
    </dependency>
</dependencies>

<build>
    <finalName>user-management</finalName>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-war-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

**内容：** Servlet、Filter、Listener、JSP

### 8. 构建顺序

Maven 会按依赖顺序自动构建：
```
user-common → user-dao → user-service → user-web
```

**构建命令：**
```bash
# 在父工程根目录执行
mvn clean install              # 构建所有模块并安装到本地仓库
mvn clean package              # 构建所有模块打 war/jar
mvn clean install -pl user-web -am   # 只构建 user-web 及其依赖模块
```

> 💡 `-pl`（project list）指定模块，`-am`（also make）同时构建其依赖。

### 9. IDEA 中操作

- 导入父工程，IDEA 自动识别子模块
- 每个子模块独立编译
- 修改 common 后，需重新 install 才能被其他模块使用（IDEA 会自动处理）

---

## 三、分层架构

### 1. 为什么分层

**不分层的问题：**
- 代码混在一起，牵一发而动全身
- 业务逻辑与 SQL 混杂，难测试
- 换数据库、换框架成本高

**分层的好处：**
- **单一职责**：每层只做一件事
- **可替换**：换数据库只改 DAO，换视图只改 JSP
- **可测试**：Service 可脱离 Web 容器测试
- **可复用**：Service 可被多种 Controller 调用

### 2. 分层职责

```
┌─────────────────────────────────────┐
│  Controller / Servlet（Web 层）      │  接收请求、参数校验、返回响应
├─────────────────────────────────────┤
│  Service（业务层）                    │  业务逻辑、事务边界、组合 DAO
├─────────────────────────────────────┤
│  DAO / Mapper（数据访问层）           │  SQL 执行、结果映射
├─────────────────────────────────────┤
│  Entity / Model（实体层）             │  数据载体，跨层传递
└─────────────────────────────────────┘
        ↓
      Database
```

**各层职责与禁忌：**

| 层 | 该做 | 不该做 |
|----|------|--------|
| Controller | 参数接收/校验、调用 Service、返回视图/JSON | 写业务逻辑、直接操作 DB、拼 SQL |
| Service | 业务规则、事务、调用多个 DAO、DTO 转换 | 直接操作 HttpServletRequest、拼 SQL |
| DAO | SQL 执行、ResultSet 映射 | 业务判断、事务控制 |
| Entity | 数据字段、getter/setter | 业务方法（简单校验可） |

### 3. DTO / VO / PO / BO 辨析

| 名称 | 全称 | 用途 |
|------|------|------|
| **PO** | Persistent Object | 与数据库表一一对应 |
| **DTO** | Data Transfer Object | 层间传输数据 |
| **VO** | View Object | 返回给前端的视图对象 |
| **BO** | Business Object | 业务对象，含业务方法 |

**示例：**
```java
// PO：对应 sys_user 表
public class UserPO {
    private Long id;
    private String username;
    private String password; // 数据库里的密文
    // ...
}

// VO：返回给前端，不含密码
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    // 无 password
}

// DTO：接收前端提交
public class UserDTO {
    private String username;
    private String password;
    private String nickname;
    // ...
}
```

**转换：** 通常在 Service 层完成 PO → VO 的转换。

> 💡 小项目可以简化为一个 User 类，但规范项目应区分，防止密码等敏感字段泄露。

### 4. 接口与实现分离

**规范做法：** Service 定义接口，实现类单独放。

```
service/
├── UserService.java           (接口)
└── impl/
    └── UserServiceImpl.java   (实现)
```

**好处：**
- 面向接口编程，降低耦合
- 便于替换实现
- 便于 AOP 代理（Spring 事务）
- 便于 Mock 测试

```java
public interface UserService {
    User login(String username, String password);
    PageResult<User> findByPage(String keyword, Integer status, int page, int size);
    void save(User user);
}

@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao = new UserDao();
    // ...
}
```

### 5. 事务边界

**事务应加在 Service 层**，而不是 DAO 或 Controller。

**原因：**
- 一个业务操作可能调用多个 DAO，需保证整体原子性
- Controller 只负责调度，不应关心事务
- DAO 只负责单条 SQL，无法覆盖多表操作

```java
@Service
public class UserServiceImpl implements UserService {

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        accountDao.deduct(fromId, amount);
        accountDao.add(toId, amount);
        // 抛异常自动回滚
    }
}
```

---

## 四、实战：把 Day7 项目改造成多模块

### 1. 拆分思路

| 原单模块 | 拆分后 | 内容 |
|----------|--------|------|
| `com.example.entity` | user-common | User、PageResult |
| `com.example.util` | user-common | DBUtil、MD5Util |
| `com.example.dao` | user-dao | UserDao |
| `com.example.service` | user-service | UserService、UserServiceImpl |
| `com.example.servlet` | user-web | LoginServlet、UserServlet |
| `com.example.filter` | user-web | EncodingFilter、AuthFilter |
| `com.example.listener` | user-web | OnlineCountListener |
| `db.properties` | user-common/resources | 数据库配置 |
| JSP、web.xml | user-web | 视图层 |

### 2. 创建步骤

**（1）新建父工程**
- IDEA → New Project → Maven → 不选 archetype
- 命名 `user-management-parent`
- 删除 `src` 目录（父工程不需要）
- packaging 改为 `pom`

**（2）添加子模块**
- 右键父工程 → New → Module → Maven
- 依次创建 user-common、user-dao、user-service、user-web
- 每个子模块的 parent 自动指向父工程

**（3）user-web 需要 war 打包**
- 添加 `maven-war-plugin`
- 添加 webapp 目录（IDEA 可能需手动配置 Facet）

**（4）迁移代码**
- 按上表把原代码移到对应模块
- 注意包名和 import

**（5）配置依赖**
- common 无内部依赖
- dao 依赖 common
- service 依赖 dao
- web 依赖 service + servlet + jstl

**（6）构建验证**
```bash
mvn clean install
```
成功后在 `~/.m2/repository/com/example/` 下能看到各模块 jar。

**（7）部署**
- Tomcat 部署 `user-web/target/user-management.war`
- 或 IDEA 配置 Tomcat，选择 user-web 的 war exploded

### 3. 分层改造：引入接口

**UserService 接口：**
```java
package com.example.service;

import com.example.entity.PageResult;
import com.example.entity.User;
import java.util.List;

public interface UserService {
    User login(String username, String password);
    User findById(Long id);
    PageResult<User> findByPage(String keyword, Integer status, int page, int size);
    void save(User user);
    void update(User user);
    void delete(Long id);
    int deleteBatch(List<Long> ids);
}
```

**UserServiceImpl：**
```java
package com.example.service.impl;

import com.example.dao.UserDao;
import com.example.entity.PageResult;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.util.MD5Util;

import java.util.List;

public class UserServiceImpl implements UserService {

    private final UserDao userDao = new UserDao();

    @Override
    public User login(String username, String password) {
        try {
            User user = userDao.findByUsername(username);
            if (user == null) return null;
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new RuntimeException("账号已被禁用");
            }
            if (!MD5Util.matches(password, user.getPassword())) return null;
            return user;
        } catch (Exception e) {
            throw new RuntimeException("登录失败：" + e.getMessage(), e);
        }
    }

    // ... 其他方法类似
}
```

**Servlet 中改为面向接口：**
```java
public class UserServlet extends HttpServlet {
    private final UserService userService = new UserServiceImpl();
    // ...
}
```

> 💡 后续引入 Spring 后，用 `@Autowired` 注入接口，实现类可替换，测试可 Mock。

### 4. 自定义异常

**user-common 中：**
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

**Service 中抛业务异常：**
```java
if (userDao.existsUsername(user.getUsername(), null)) {
    throw new BusinessException("用户名已存在");
}
```

**Servlet 统一捕获：**
```java
try {
    userService.save(user);
    resp.sendRedirect(...);
} catch (BusinessException e) {
    req.setAttribute("error", e.getMessage());
    req.getRequestDispatcher("/WEB-INF/views/user-form.jsp").forward(req, resp);
}
```

### 5. 统一返回结果（为前后端分离做准备）

```java
package com.example.common;

public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        return r;
    }

    // getter/setter
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
```

---

## 五、Maven 常见问题与排错

| 问题 | 原因 | 解决 |
|------|------|------|
| 依赖下载失败 | 网络/镜像问题 | 配置阿里云镜像，删 `*.lastUpdated` 重试 |
| 依赖冲突 | 传递依赖版本不一致 | `mvn dependency:tree` 分析，`<exclusions>` 排除 |
| 子模块找不到父 | relativePath 错误 | 默认 `../pom.xml`，或显式 `<relativePath>` |
| 子模块找不到内部依赖 | 未 install common | 先 `mvn install`，或用 `-am` |
| 打包缺依赖 | scope 错误 | runtime/compile 才会打包 |
| war 包无 class | 编译失败但未报错 | 检查 `target/classes` |
| JDK 版本不匹配 | 编译器配置 | 用 `maven.compiler.source/target` |
| 中文乱码 | 编码未设 | `project.build.sourceEncoding=UTF-8` |
| IDEA 不识别模块 | 未刷新 | 右键 pom → Maven → Reload |
| SNAPSHOT 不更新 | 本地缓存 | `mvn clean install -U` 强制更新 |

---

## 六、实践练习

### 练习 1：搭建多模块骨架
1. 创建父工程 `user-management-parent`
2. 创建 common、dao、service、web 四个子模块
3. 配置父子关系和依赖
4. `mvn clean install` 验证构建成功

### 练习 2：迁移 Day7 代码
1. 按分层把代码迁移到对应模块
2. 引入 Service 接口与实现分离
3. 引入 BusinessException
4. 部署到 Tomcat，验证功能正常

### 练习 3：依赖管理优化
1. 父 pom 用 `dependencyManagement` 统一版本
2. 子模块引用不写版本号
3. 用 `mvn dependency:tree` 检查冲突

### 练习 4：Profile 配置
1. 配置 dev / prod 两个 profile
2. 不同 profile 用不同数据库
3. 用 `mvn package -P prod` 验证

### 练习 5：单元测试
1. 在 service 模块引入 JUnit 5
2. 编写 UserService 的测试（Mock UserDao）
3. 运行 `mvn test`

---

## 七、总结与拓展

### 今日总结
- **Maven 坐标**由 groupId + artifactId + version 组成
- **scope** 控制依赖的传递和打包（compile/provided/runtime/test）
- **依赖冲突**按最短路径和最先声明解决，可用 `<exclusions>` 排除
- **dependencyManagement** 统一版本，子模块引用不写版本
- **生命周期**：clean → compile → test → package → install → deploy
- **多模块**：父 pom 聚合 + 子模块分层，依赖单向
- **分层架构**：Controller → Service → DAO → Entity，各司其职
- **接口与实现分离**：面向接口编程，便于替换和测试
- **事务边界**在 Service 层
- **DTO/VO/PO** 按需区分，防止敏感字段泄露

### 核心记忆点
```
父 pom：packaging=pom + modules + dependencyManagement
子模块：<parent> + artifactId + packaging
依赖方向：web → service → dao → common（单向）
常用命令：mvn clean install、mvn dependency:tree
分层职责：Controller 调度、Service 业务、DAO 数据、Entity 载体
接口分离：UserService（接口）+ UserServiceImpl（实现）
```

### 拓展阅读
- [Maven 官方文档](https://maven.apache.org/guides/)
- [Maven 依赖机制](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)
- [阿里云 Maven 镜像](https://developer.aliyun.com/mirror/maven)
- [《Maven 实战》](https://book.douban.com/subject/5345682/)

