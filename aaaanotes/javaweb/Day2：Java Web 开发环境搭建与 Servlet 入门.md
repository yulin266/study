

## 学习目标

- 理解 Java Web 技术栈与开发环境组成
- 熟练搭建 JDK + Maven + Tomcat + IDEA 环境
- 掌握 Maven Web 项目的创建与配置
- 理解 Servlet 的本质、作用与生命周期
- 熟练编写第一个 Servlet 程序
- 掌握 `HttpServletRequest` 和 `HttpServletResponse` 的核心 API
- 用 Postman 测试 Servlet 接口
- 把 Day1 的 HTTP 知识落地到 Servlet 代码

---

## 一、Java Web 技术栈概述

### 1. 什么是 Java Web

Java Web 是用 Java 技术开发 Web 应用（网站、后台系统、API 服务）的技术体系。

**一次完整的 Web 请求：**
```
浏览器 → HTTP 请求 → Tomcat（Servlet 容器）
                          ↓
                     Servlet（处理请求）
                          ↓
                     数据库 / 业务逻辑
                          ↓
                     HTTP 响应 → 浏览器
```

### 2. Java Web 核心技术

| 技术 | 作用 | 所属规范 |
|------|------|----------|
| Servlet | 处理 HTTP 请求响应 | Jakarta EE（原 Java EE） |
| JSP | 动态网页 | Jakarta Pages |
| Filter | 请求过滤 | Servlet 规范 |
| Listener | 事件监听 | Servlet 规范 |
| JDBC | 数据库访问 | Java SE |
| JSTL | JSP 标签库 | Jakarta Tags |

> 💡 **Servlet 是 Java Web 的核心**，Spring MVC 等框架底层都是 Servlet。

### 3. 开发环境组成

| 组件 | 作用 | 推荐版本 |
|------|------|----------|
| JDK | Java 开发工具包 | JDK 17 / 21（LTS） |
| Maven | 依赖管理与构建 | 3.9.x |
| Tomcat | Servlet 容器（Web 服务器） | 10.1.x（Jakarta EE 10） |
| IDE | 集成开发环境 | IntelliJ IDEA |
| MySQL | 数据库（后续） | 8.0.x |

> ⚠️ **版本对应关系**：
> | Tomcat 版本 | Servlet 版本 | 包名 |
> |-------------|--------------|------|
> | Tomcat 9 | Servlet 4.0 | `javax.servlet.*` |
> | Tomcat 10.1 | Servlet 6.0 | `jakarta.servlet.*` |
> | Tomcat 11 | Servlet 6.1 | `jakarta.servlet.*` |
>
> **从 Tomcat 10 开始，包名从 `javax` 改为 `jakarta`**（Oracle 将 Java EE 捐给 Eclipse 后改名）。本课以 **Tomcat 10 + Jakarta Servlet** 为例。

---

## 二、JDK 安装与配置

### 1. 下载 JDK

**推荐 JDK 17（LTS）或 JDK 21（LTS）**，长期支持，稳定。

下载地址：
- Oracle JDK：https://www.oracle.com/java/technologies/downloads/
- OpenJDK（推荐）：https://adoptium.net/
- 阿里 Dragonwell：https://dragonwell.oss-cn-shanghai.aliyuncs.com/

### 2. 安装

**Windows：**
- 双击 `.msi` 或解压 `.zip`
- 建议安装路径无中文、无空格，如 `D:\dev\jdk-17`

**macOS：**
```bash
brew install openjdk@17
```

**Linux：**
```bash
sudo tar -zxvf jdk-17_linux-x64_bin.tar.gz -C /usr/local/
```

### 3. 配置环境变量

**Windows：**
1. 此电脑 → 属性 → 高级系统设置 → 环境变量
2. 新建 `JAVA_HOME`：`D:\dev\jdk-17`
3. 编辑 `Path`，添加 `%JAVA_HOME%\bin`
4. 可选：`CLASSPATH`：`.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar`

**macOS / Linux：**
```bash
# 编辑 ~/.zshrc 或 ~/.bash_profile
export JAVA_HOME=/usr/local/jdk-17
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

# 生效
source ~/.zshrc
```

### 4. 验证

```bash
java -version
# java version "17.0.10" 2024-01-16 LTS
# Java(TM) SE Runtime Environment ...

javac -version
# javac 17.0.10

echo $JAVA_HOME    # macOS/Linux
echo %JAVA_HOME%   # Windows
```

> 💡 **常见问题**：如果 `java` 能用但 `javac` 不能用，说明只装了 JRE，需要装 JDK。

---

## 三、Maven 安装与配置

### 1. Maven 是什么

Maven 是 Java 项目的**依赖管理**和**构建**工具：
- **依赖管理**：自动下载 jar 包及其传递依赖
- **构建**：编译、测试、打包、部署
- **项目结构**：约定优于配置

### 2. 下载安装

下载：https://maven.apache.org/download.cgi

解压到无中文路径，如 `D:\dev\apache-maven-3.9.6`

### 3. 配置环境变量

**Windows：**
- 新建 `MAVEN_HOME`：`D:\dev\apache-maven-3.9.6`
- 编辑 `Path`，添加 `%MAVEN_HOME%\bin`

**macOS / Linux：**
```bash
export MAVEN_HOME=/usr/local/apache-maven-3.9.6
export PATH=$MAVEN_HOME/bin:$PATH
```

**验证：**
```bash
mvn -v
# Apache Maven 3.9.6
# Maven home: D:\dev\apache-maven-3.9.6
# Java version: 17.0.10
```

### 4. 配置本地仓库与镜像

**编辑 `conf/settings.xml`（Maven 安装目录下）：**

**（1）配置本地仓库位置**
```xml
<localRepository>D:/maven-repo</localRepository>
```

**（2）配置阿里云镜像（加速下载）**
在 `<mirrors>` 标签内添加：
```xml
<mirror>
    <id>aliyunmaven</id>
    <mirrorOf>*</mirrorOf>
    <name>阿里云公共仓库</name>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

**（3）配置 JDK 版本（可选）**
在 `<profiles>` 标签内添加：
```xml
<profile>
    <id>jdk-17</id>
    <activation>
        <activeByDefault>true</activeByDefault>
        <jdk>17</jdk>
    </activation>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.compiler.compilerVersion>17</maven.compiler.compilerVersion>
    </properties>
</profile>
```

### 5. Maven 目录结构

```
apache-maven-3.9.6/
├── bin/              # mvn 命令
├── boot/             # 类加载器
├── conf/
│   └── settings.xml  # 全局配置
├── lib/              # Maven 自身依赖
└── LICENSE
```

**本地仓库结构：**
```
D:/maven-repo/
└── org/springframework/spring-core/6.1.6/
    ├── spring-core-6.1.6.jar
    ├── spring-core-6.1.6.pom
    └── ...
```

### 6. Maven 常用命令

```bash
mvn clean               # 清理 target
mvn compile             # 编译
mvn test                # 测试
mvn package             # 打包（jar/war）
mvn install             # 安装到本地仓库
mvn deploy              # 部署到远程仓库
mvn clean package       # 清理 + 打包
mvn clean install -DskipTests   # 跳过测试安装
mvn dependency:tree     # 查看依赖树
mvn help:system         # 查看系统信息
```

---

## 四、Tomcat 安装与配置

### 1. Tomcat 是什么

Tomcat 是 Apache 开源的 **Servlet 容器**，实现了 Jakarta EE 的 Servlet、JSP 等规范。

**核心功能：**
- 解析 HTTP 请求
- 管理 Servlet 生命周期
- 生成 HTTP 响应
- 静态资源服务

### 2. 下载安装

下载：https://tomcat.apache.org/download-10.cgi

选择 **Windows zip** 或 **tar.gz**，解压到无中文路径，如 `D:\dev\apache-tomcat-10.1.24`

### 3. 目录结构

```
apache-tomcat-10.1.24/
├── bin/                    # 启动脚本
│   ├── startup.bat/.sh
│   ├── shutdown.bat/.sh
│   └── catalina.bat/.sh
├── conf/                   # 配置文件
│   ├── server.xml          # 主配置（端口等）
│   ├── web.xml             # 全局 web 配置
│   ├── context.xml
│   └── tomcat-users.xml    # 管理用户
├── lib/                    # 全局 jar
├── logs/                   # 日志
├── temp/                   # 临时文件
├── webapps/                # 部署目录
│   ├── ROOT/               # 根应用
│   ├── docs/               # 文档
│   └── examples/           # 示例
└── work/                   # JSP 编译输出
```

### 4. 配置环境变量（可选）

```bash
export CATALINA_HOME=/usr/local/apache-tomcat-10.1.24
export PATH=$CATALINA_HOME/bin:$PATH
```

### 5. 启动与停止

**Windows：**
```cmd
cd D:\dev\apache-tomcat-10.1.24\bin
startup.bat     # 启动
shutdown.bat    # 停止
```

**macOS / Linux：**
```bash
cd /usr/local/apache-tomcat-10.1.24/bin
chmod +x *.sh   # 首次赋权
./startup.sh    # 启动
./shutdown.sh   # 停止
```

**验证：** 浏览器访问 http://localhost:8080，看到 Tomcat 欢迎页即成功。

### 6. 修改端口

编辑 `conf/server.xml`：
```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443" />
```
把 `8080` 改为其他端口（如 `8081`），避免冲突。

### 7. 修改编码

编辑 `conf/server.xml` 的 Connector，添加：
```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443"
           URIEncoding="UTF-8" />
```

### 8. 配置管理用户

编辑 `conf/tomcat-users.xml`，在 `<tomcat-users>` 内添加：
```xml
<role rolename="manager-gui"/>
<role rolename="admin-gui"/>
<user username="admin" password="admin" roles="manager-gui,admin-gui"/>
```

访问 http://localhost:8080/manager/html 可管理应用。

### 9. 常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| 启动闪退 | JAVA_HOME 未配置 | 配置 `JAVA_HOME` |
| 8080 端口被占用 | 其他程序占用 | 改端口或结束占用进程 |
| 访问 404 | 应用未部署 | 检查 webapps 目录 |
| 中文乱码 | 编码未配 | Connector 加 `URIEncoding="UTF-8"` |
| 启动很慢 | 熵源不足 | 加 `-Djava.security.egd=file:/dev/./urandom` |

---

## 五、IDEA 安装与配置

### 1. 下载安装

- 下载：https://www.jetbrains.com/idea/download/
- **Ultimate 版**：支持 Java Web、Spring，学生可免费申请
- **Community 版**：免费，但 Web 支持弱

### 2. 配置 Maven

File → Settings → Build, Execution, Deployment → Build Tools → Maven

- **Maven home path**：`D:\dev\apache-maven-3.9.6`
- **User settings file**：勾选 Override，指向 `conf/settings.xml`
- **Local repository**：自动识别

### 3. 配置 JDK

File → Project Structure → SDKs → 添加 JDK 17

### 4. 配置 Tomcat

Run → Edit Configurations → `+` → Tomcat Server → Local

- **Application server**：指向 Tomcat 目录
- **JRE**：选择 JDK 17
- **HTTP port**：8080
- **Deployment** 标签页：`+` → Artifact → 选择 `xxx:war exploded`
- **Application context**：`/` 或 `/myapp`

### 5. 常用快捷键

| 操作 | 快捷键 |
|------|--------|
| 运行 | Shift+F10 |
| 调试 | Shift+F9 |
| 重命名 | Shift+F6 |
| 生成代码 | Alt+Insert |
| 格式化 | Ctrl+Alt+L |
| 全局搜索 | Ctrl+Shift+F |
| 打开类 | Ctrl+N |
| 查看定义 | Ctrl+B |

---

## 六、创建第一个 Maven Web 项目

### 1. 创建项目

**IDEA → New Project → Maven → 选择 maven-archetype-webapp**

填写：
- **GroupId**：`com.example`
- **ArtifactId**：`my-web-app`
- **Version**：`1.0-SNAPSHOT`

### 2. 项目结构

```
my-web-app/
├── pom.xml
└── src/
    └── main/
        ├── java/                 # Java 源代码
        ├── resources/            # 配置文件
        └── webapp/               # Web 资源
            ├── WEB-INF/
            │   └── web.xml       # 部署描述符
            └── index.jsp
```

> ⚠️ `maven-archetype-webapp` 默认没有 `java` 和 `resources` 目录，需手动创建并在 IDEA 中标记为 Sources Root / Resources Root。

### 3. 配置 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-web-app</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Servlet API，scope 为 provided，因为 Tomcat 已自带 -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>my-web-app</finalName>
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

> 💡 **Tomcat 9 用户**（`javax` 包）：
> ```xml
> <dependency>
>     <groupId>javax.servlet</groupId>
>     <artifactId>javax.servlet-api</artifactId>
>     <version>4.0.1</version>
>     <scope>provided</scope>
> </dependency>
> ```

### 4. web.xml（Servlet 6.0）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
         https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0"
         metadata-complete="false">

    <display-name>My Web App</display-name>

    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>

</web-app>
```

> ⚠️ `metadata-complete="false"` 表示启用注解扫描（`@WebServlet` 等）。若设为 `true`，注解失效。

### 5. 部署到 Tomcat

**（1）IDEA 配置 Tomcat**
- Run → Edit Configurations → `+` → Tomcat Server → Local
- Application server 选择 Tomcat 目录
- Deployment → `+` → Artifact → `my-web-app:war exploded`
- Application context：`/my-web-app`

**（2）运行**
- 点击运行按钮，浏览器访问 `http://localhost:8080/my-web-app/`

**（3）验证**
看到 `index.jsp` 内容即部署成功。

---

## 七、Servlet 基础

### 1. 什么是 Servlet

**Servlet** 是运行在服务器端的 Java 程序，用于：
- 接收客户端（浏览器）的 HTTP 请求
- 处理业务逻辑
- 生成 HTTP 响应返回客户端

**Servlet 与 HTTP 的对应关系：**
```
Day1 学的：  请求报文 → 服务器处理 → 响应报文
Servlet 中： HttpServletRequest → doGet/doPost → HttpServletResponse
```

### 2. Servlet 规范与体系

```
jakarta.servlet.Servlet（接口）
    ↑
jakarta.servlet.GenericServlet（通用 Servlet，协议无关）
    ↑
jakarta.servlet.http.HttpServlet（HTTP Servlet，常用）
    ↑
你的 Servlet
```

**核心接口/类：**

| 类型 | 说明 |
|------|------|
| `Servlet` | 顶层接口，定义生命周期方法 |
| `GenericServlet` | 抽象类，协议无关 |
| `HttpServlet` | 抽象类，处理 HTTP，重写 doGet/doPost |
| `ServletRequest` | 通用请求 |
| `HttpServletRequest` | HTTP 请求，提供 getParameter、getSession 等 |
| `ServletResponse` | 通用响应 |
| `HttpServletResponse` | HTTP 响应，提供 getWriter、sendRedirect 等 |
| `ServletConfig` | Servlet 配置 |
| `ServletContext` | 应用上下文（全局） |

### 3. Servlet 生命周期

Servlet 由**容器（Tomcat）**管理，生命周期分四个阶段：

```
1. 加载与实例化 → 构造器（一次）
2. 初始化       → init(ServletConfig)（一次）
3. 服务         → service() → doGet/doPost（每次请求）
4. 销毁         → destroy()（一次，应用关闭时）
```

| 阶段 | 方法 | 调用次数 | 说明 |
|------|------|----------|------|
| 实例化 | 构造器 | 1 次 | 容器创建 Servlet 实例 |
| 初始化 | `init(ServletConfig)` | 1 次 | 读取配置、初始化资源 |
| 服务 | `service(req, resp)` | N 次 | 每次请求调用，分发到 doXxx |
| 销毁 | `destroy()` | 1 次 | 释放资源 |

**关键点：**
- Servlet **默认是单例**，容器中只有一个实例
- 因此 **Servlet 中不要定义可变成员变量**（线程不安全）
- `service()` 方法在 `HttpServlet` 中已实现，根据请求方法自动调用 `doGet`、`doPost`、`doPut`、`doDelete` 等
- 我们通常只需重写 `doGet` / `doPost`

**service 分发逻辑（简化）：**
```java
protected void service(HttpServletRequest req, HttpServletResponse resp) {
    String method = req.getMethod();
    if ("GET".equals(method)) doGet(req, resp);
    else if ("POST".equals(method)) doPost(req, resp);
    else if ("PUT".equals(method)) doPut(req, resp);
    else if ("DELETE".equals(method)) doDelete(req, resp);
    // ...
}
```

### 4. 编写第一个 Servlet

**方式一：继承 HttpServlet（推荐）**

```java
package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("HelloServlet 初始化");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 设置响应类型和编码
        resp.setContentType("text/html;charset=UTF-8");

        // 获取输出流
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Hello</title></head><body>");
        out.println("<h1>Hello, Servlet!</h1>");
        out.println("<p>当前时间：" + new Date() + "</p>");
        out.println("<p>请求方法：" + req.getMethod() + "</p>");
        out.println("<p>请求 URI：" + req.getRequestURI() + "</p>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);  // 简单复用
    }

    @Override
    public void destroy() {
        System.out.println("HelloServlet 销毁");
    }
}
```

**方式二：web.xml 配置（传统）**

如果不使用 `@WebServlet`，可在 `web.xml` 中配置：

```xml
<servlet>
    <servlet-name>HelloServlet</servlet-name>
    <servlet-class>com.example.servlet.HelloServlet</servlet-class>
    <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>HelloServlet</servlet-name>
    <url-pattern>/hello</url-pattern>
</servlet-mapping>
```

**两种方式对比：**

| 方式 | 优点 | 缺点 |
|------|------|------|
| `@WebServlet` | 简洁，无需 XML | 分散在代码中 |
| web.xml | 集中管理 | 冗长 |

> 现代开发推荐注解方式。

### 5. @WebServlet 属性

```java
@WebServlet(
    name = "HelloServlet",           // Servlet 名
    urlPatterns = {"/hello", "/hi"}, // 访问路径（可多个）
    loadOnStartup = 1,                // 启动时加载（默认 -1，首次访问才加载）
    initParams = {                    // 初始化参数
        @WebInitParam(name = "encoding", value = "UTF-8")
    }
)
public class HelloServlet extends HttpServlet { }
```

**`loadOnStartup` 说明：**
- `-1`（默认）：首次访问时创建
- `0` 或正整数：应用启动时创建，数字越小越早

### 6. 运行与测试

**访问：** `http://localhost:8080/my-web-app/hello`

**用 Postman 测试：**
- 方法：GET
- URL：`http://localhost:8080/my-web-app/hello`
- 观察响应头 `Content-Type: text/html;charset=UTF-8`
- 观察响应体 HTML

**观察日志：**
```
HelloServlet 初始化      ← 首次访问时
（多次访问，init 不再打印）
（关闭 Tomcat）
HelloServlet 销毁
```

---

## 八、HttpServletRequest 详解

`HttpServletRequest` 封装了客户端的所有请求信息（对应 Day1 的请求报文）。

### 1. 获取请求行信息

```java
String method = req.getMethod();              // GET / POST
String uri = req.getRequestURI();             // /my-web-app/hello
StringBuffer url = req.getRequestURL();       // http://localhost:8080/my-web-app/hello
String protocol = req.getProtocol();          // HTTP/1.1
String queryString = req.getQueryString();    // name=tom&age=20
String contextPath = req.getContextPath();    // /my-web-app
String servletPath = req.getServletPath();    // /hello
String pathInfo = req.getPathInfo();          // null（或 /users/1 中的 /1）
```

### 2. 获取请求头

```java
String userAgent = req.getHeader("User-Agent");
String contentType = req.getHeader("Content-Type");
String accept = req.getHeader("Accept");

// 获取所有头名
Enumeration<String> headerNames = req.getHeaderNames();
while (headerNames.hasMoreElements()) {
    String name = headerNames.nextElement();
    String value = req.getHeader(name);
    System.out.println(name + ": " + value);
}
```

### 3. 获取请求参数

**单个参数：**
```java
String name = req.getParameter("name");
String age = req.getParameter("age");  // 返回 String，需手动转换
```

**多值参数（复选框、多选）：**
```java
String[] hobbies = req.getParameterValues("hobby");
```

**所有参数名：**
```java
Enumeration<String> paramNames = req.getParameterNames();
while (paramNames.hasMoreElements()) {
    String name = paramNames.nextElement();
    String value = req.getParameter(name);
    System.out.println(name + " = " + value);
}
```

**参数 Map：**
```java
Map<String, String[]> paramMap = req.getParameterMap();
```

**完整示例：**
```java
@WebServlet("/greet")
public class GreetServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String name = req.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            name = "陌生人";
        }
        out.println("你好，" + name + "！");
        out.println("请求方法：" + req.getMethod());
        out.println("请求 URI：" + req.getRequestURI());
        out.println("查询字符串：" + req.getQueryString());
    }
}
```

访问 `http://localhost:8080/my-web-app/greet?name=张三`，输出"你好，张三！"。

### 4. 请求编码处理

**GET 请求：**
- Tomcat 8+ 默认 UTF-8，一般无需处理
- 如乱码，可在 `server.xml` 的 Connector 加 `URIEncoding="UTF-8"`

**POST 请求：**
- 默认 ISO-8859-1，**必须手动设置**
- `req.setCharacterEncoding("UTF-8")` 必须在**第一次获取参数之前**调用

```java
@Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
    req.setCharacterEncoding("UTF-8");   // 必须放最前面
    String username = req.getParameter("username");
    // ...
}
```

### 5. 获取 Cookie

```java
Cookie[] cookies = req.getCookies();
if (cookies != null) {
    for (Cookie c : cookies) {
        System.out.println(c.getName() + " = " + c.getValue());
    }
}
```

### 6. 获取 Session

```java
HttpSession session = req.getSession();          // 不存在则创建
HttpSession session2 = req.getSession(false);    // 不存在返回 null
```

### 7. 获取请求体（JSON 等）

```java
BufferedReader reader = req.getInputStream() ... 
// 或
ServletInputStream is = req.getInputStream();
```

**读取 JSON 请求体（示例）：**
```java
StringBuilder sb = new StringBuilder();
try (BufferedReader reader = req.getReader()) {
    String line;
    while ((line = reader.readLine()) != null) {
        sb.append(line);
    }
}
String json = sb.toString();
// 用 Jackson/Gson 解析
```

### 8. 请求转发

```java
req.setAttribute("msg", "hello");
req.getRequestDispatcher("/result.jsp").forward(req, resp);
```

### 9. 常用方法速查表

| 方法 | 说明 |
|------|------|
| `getMethod()` | 请求方法 |
| `getRequestURI()` | 请求 URI |
| `getRequestURL()` | 完整 URL |
| `getContextPath()` | 应用上下文 |
| `getServletPath()` | Servlet 路径 |
| `getPathInfo()` | 路径信息 |
| `getQueryString()` | 查询字符串 |
| `getParameter(name)` | 获取参数 |
| `getParameterValues(name)` | 获取多值参数 |
| `getParameterMap()` | 参数 Map |
| `getHeader(name)` | 请求头 |
| `getHeaderNames()` | 所有头名 |
| `getCookies()` | Cookie 数组 |
| `getSession()` | 获取 Session |
| `setAttribute(k, v)` | 存数据到请求域 |
| `getAttribute(k)` | 取数据 |
| `getRequestDispatcher(path)` | 获取转发器 |

---

## 九、HttpServletResponse 详解

`HttpServletResponse` 用于构建返回给客户端的响应（对应 Day1 的响应报文）。

### 1. 设置状态码

```java
resp.setStatus(200);                     // 设置状态码
resp.setStatus(HttpServletResponse.SC_NOT_FOUND);  // 404
resp.sendError(404, "资源不存在");       // 发送错误
resp.sendError(500);
```

### 2. 设置响应头

```java
resp.setHeader("Content-Type", "application/json");
resp.setHeader("Cache-Control", "no-cache");
resp.addHeader("X-Custom", "value");     // 添加（不覆盖）
resp.setDateHeader("Expires", System.currentTimeMillis() + 3600_000);
```

### 3. 设置内容类型与编码

```java
resp.setContentType("text/html;charset=UTF-8");
// 或分开
resp.setContentType("application/json");
resp.setCharacterEncoding("UTF-8");
```

**常用 Content-Type：**
| 值 | 说明 |
|----|------|
| `text/html;charset=UTF-8` | HTML |
| `text/plain;charset=UTF-8` | 纯文本 |
| `application/json;charset=UTF-8` | JSON |
| `application/xml` | XML |
| `image/png` | 图片 |

### 4. 输出响应体

**字符流（文本）：**
```java
PrintWriter out = resp.getWriter();
out.println("<h1>Hello</h1>");
out.println("{\"code\":200}");
```

**字节流（二进制）：**
```java
ServletOutputStream os = resp.getOutputStream();
os.write(bytes);
```

> ⚠️ **`getWriter()` 和 `getOutputStream()` 不能同时使用**，会抛 `IllegalStateException`。

### 5. 返回 JSON

```java
@WebServlet("/api/user")
public class UserApiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("{\"id\":1,\"name\":\"张三\",\"age\":25}");
    }
}
```

访问 `/api/user`，返回 JSON，正好对应 Day1 中 GitHub API 的响应形式。

### 6. 重定向

```java
// 重定向到登录页
resp.sendRedirect(req.getContextPath() + "/login.jsp");

// 重定向到外部 URL
resp.sendRedirect("https://www.baidu.com");
```

**重定向 vs 转发（Day3 详解）：**
- 重定向：两次请求，地址栏变，状态码 302
- 转发：一次请求，地址栏不变，状态码 200

### 7. 设置 Cookie

```java
Cookie cookie = new Cookie("username", "tom");
cookie.setMaxAge(7 * 24 * 3600);   // 7 天
cookie.setPath("/");
cookie.setHttpOnly(true);           // 防 XSS
resp.addCookie(cookie);
```

### 8. 常用方法速查表

| 方法 | 说明 |
|------|------|
| `setStatus(int)` | 设置状态码 |
| `sendError(int, String)` | 发送错误 |
| `setHeader(name, value)` | 设置响应头 |
| `addHeader(name, value)` | 添加响应头 |
| `setContentType(type)` | 设置内容类型 |
| `setCharacterEncoding(enc)` | 设置编码 |
| `getWriter()` | 获取字符输出流 |
| `getOutputStream()` | 获取字节输出流 |
| `sendRedirect(url)` | 重定向 |
| `addCookie(cookie)` | 添加 Cookie |
| `setContentLength(len)` | 设置内容长度 |

---

## 十、实战：处理 POST 请求与表单

### 1. HTML 表单

```html
<!-- login.html -->
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>登录</title></head>
<body>
    <h2>用户登录</h2>
    <form action="login" method="post">
        用户名：<input type="text" name="username"><br><br>
        密码：<input type="password" name="password"><br><br>
        <input type="submit" value="登录">
    </form>
</body>
</html>
```

### 2. LoginServlet

```java
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // GET 转发到登录页
        req.getRequestDispatcher("/login.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 1. 设置请求编码（必须在获取参数前）
        req.setCharacterEncoding("UTF-8");
        // 2. 设置响应编码
        resp.setContentType("text/html;charset=UTF-8");

        // 3. 获取参数
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // 4. 处理逻辑
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        if ("admin".equals(username) && "123456".equals(password)) {
            out.println("<h2>登录成功，欢迎 " + username + "</h2>");
        } else {
            out.println("<h2>登录失败</h2>");
            out.println("<p>用户名或密码错误</p>");
        }
        out.println("</body></html>");
    }
}
```

### 3. 测试

**用浏览器：** 填写表单提交，观察结果。

**用 Postman：**
- 方法：POST
- URL：`http://localhost:8080/my-web-app/login`
- Body → `x-www-form-urlencoded`
- 参数：`username=admin`、`password=123456`
- 观察响应

**用 curl：**
```bash
curl -X POST http://localhost:8080/my-web-app/login \
     -d "username=admin&password=123456"
```

### 4. 处理多值参数

```html
<form action="register" method="post">
    <input type="checkbox" name="hobby" value="reading"> 阅读
    <input type="checkbox" name="hobby" value="music"> 音乐
    <input type="checkbox" name="hobby" value="sports"> 运动
    <input type="submit" value="注册">
</form>
```

```java
String[] hobbies = req.getParameterValues("hobby");
if (hobbies != null) {
    for (String h : hobbies) {
        System.out.println("爱好：" + h);
    }
}
```

---

## 十一、实战：模拟 RESTful 接口

用 Servlet 实现一个简易的 RESTful 风格 UserServlet。

### 1. 代码

```java
@WebServlet("/users/*")
public class UserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || "/".equals(pathInfo)) {
            // GET /users  → 列表
            resp.getWriter().println("[{\"id\":1,\"name\":\"张三\"},{\"id\":2,\"name\":\"李四\"}]");
        } else {
            // GET /users/1  → 详情
            String id = pathInfo.substring(1);
            resp.getWriter().println("{\"id\":" + id + ",\"name\":\"用户" + id + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        // POST /users → 创建
        resp.setStatus(201);  // Created
        resp.getWriter().println("{\"code\":201,\"msg\":\"创建成功\"}");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();
        resp.getWriter().println("{\"code\":200,\"msg\":\"更新成功\",\"id\":\""
                + pathInfo.substring(1) + "\"}");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(204);  // No Content
    }
}
```

### 2. 测试（Postman）

| 请求 | URL | 预期 |
|------|-----|------|
| GET | `/users` | 200 + 列表 JSON |
| GET | `/users/1` | 200 + 详情 JSON |
| POST | `/users` | 201 |
| PUT | `/users/1` | 200 |
| DELETE | `/users/1` | 204 |

> 💡 观察 `req.getPathInfo()` 的值：访问 `/users/1` 时为 `/1`。

---

## 十二、Servlet 进阶话题

### 1. Servlet 是单例的

**验证：**
```java
@WebServlet("/singleton")
public class SingletonServlet extends HttpServlet {
    private int count = 0;   // ⚠️ 危险：成员变量

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        count++;  // 并发不安全！
        resp.getWriter().println("访问次数：" + count);
    }
}
```

**问题：** 多线程并发访问，`count++` 非原子操作，结果不准。

**解决：**
- 不在 Servlet 中定义可变成员变量
- 用局部变量
- 必须共享时用 `AtomicInteger` 或加锁

### 2. ServletConfig 与 ServletContext

**ServletConfig（Servlet 级）：**
```java
String value = getServletConfig().getInitParameter("encoding");
String servletName = getServletConfig().getServletName();
```

**ServletContext（应用级）：**
```java
ServletContext ctx = getServletContext();
ctx.setAttribute("onlineCount", 100);
Object count = ctx.getAttribute("onlineCount");
String appName = ctx.getInitParameter("appName");
String realPath = ctx.getRealPath("/WEB-INF/views");
```

**作用域对比：**

| 作用域 | 对象 | 范围 |
|--------|------|------|
| page | pageContext | 当前页面（JSP） |
| request | HttpServletRequest | 一次请求 |
| session | HttpSession | 一次会话 |
| application | ServletContext | 整个应用 |

### 3. 获取项目真实路径

```java
String realPath = getServletContext().getRealPath("/");
// D:\dev\apache-tomcat-10.1.24\webapps\my-web-app\
```

### 4. 读取资源文件

```java
// 从 classpath 读取
InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties");

// 从 webapp 读取
InputStream is2 = getServletContext().getResourceAsStream("/WEB-INF/config.properties");
```

### 5. Servlet 线程安全

**结论：** Servlet 是单例多线程，成员变量不安全，方法内局部变量安全。

**线程安全写法：**
```java
@WebServlet("/safe")
public class SafeServlet extends HttpServlet {
    // ✅ 无状态或不可变成员
    private final String appName = "MyApp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // ✅ 局部变量线程安全
        int count = 0;
        count++;
        resp.getWriter().println(count);
    }
}
```

---

## 十三、常见问题与排错

| 问题 | 原因 | 解决 |
|------|------|------|
| 404 Not Found | URL 错误或 Servlet 未注册 | 检查 `@WebServlet` 和访问路径 |
| 405 Method Not Allowed | 未重写对应 doXxx | 实现 doGet/doPost |
| 500 Internal Server Error | 代码抛异常 | 查看 Tomcat 日志 `logs/catalina.out` |
| 中文乱码（POST） | 未设请求编码 | `req.setCharacterEncoding("UTF-8")` |
| 中文乱码（响应） | 未设响应编码 | `resp.setContentType("text/html;charset=UTF-8")` |
| `ClassNotFoundException: jakarta.servlet.*` | 依赖 scope 或版本不对 | `provided` scope，Tomcat 10+ |
| `ClassNotFoundException: javax.servlet.*` | 用了 Tomcat 10 但代码是 javax | 换 Tomcat 9 或改包名 |
| 修改代码无变化 | 未重新部署 | IDEA 点 Redeploy 或重启 |
| `getWriter() has already been called` | Writer 和 OutputStream 混用 | 只用一种 |
| `@WebServlet` 不生效 | web.xml `metadata-complete="true"` | 改为 false 或删除该属性 |
| 参数为 null | 参数名不匹配 | 检查表单 name 与 getParameter |
| Servlet 实例变量错乱 | 单例多线程 | 不用可变成员变量 |
| Tomcat 启动慢 | 熵源不足 | 加 `-Djava.security.egd=file:/dev/./urandom` |
| 端口被占用 | 8080 冲突 | 改端口或结束占用进程 |

---

## 十四、实践练习

### 练习 1：Hello Servlet
1. 创建 Maven Web 项目
2. 编写 `HelloServlet`，返回 HTML
3. 部署到 Tomcat 访问
4. 观察 init/service/destroy 日志

### 练习 2：参数处理
1. 编写 `GreetServlet`，读取 `name` 参数
2. 未提供时显示"陌生人"
3. 返回 JSON 格式：`{"message":"Hello, xxx","time":"..."}`
4. 用 Postman 测试

### 练习 3：简易计算器
创建 `CalcServlet`，接受 `a`、`b`、`op`（add/sub/mul/div）：
- `/calc?a=10&b=5&op=add` → `{"result":15}`
- 除零返回 400 和错误信息
- 参数非法返回 400

### 练习 4：登录表单
1. 编写 `login.html` 表单
2. 编写 `LoginServlet` 处理 POST
3. 校验用户名密码（admin/123456）
4. 成功返回欢迎页，失败返回错误

### 练习 5：RESTful 接口
1. 编写 `UserServlet` 映射 `/users/*`
2. GET `/users` → 列表
3. GET `/users/1` → 详情
4. POST `/users` → 创建（201）
5. DELETE `/users/1` → 删除（204）
6. 用 Postman 测试所有方法

### 练习 6：请求头分析
编写 `HeaderServlet`：
1. 打印所有请求头
2. 读取 `User-Agent` 判断浏览器
3. 读取 `Accept-Language` 返回对应语言问候

### 练习 7（进阶）：文件下载
编写 `DownloadServlet`：
1. 设置 `Content-Type: application/octet-stream`
2. 设置 `Content-Disposition: attachment; filename="xxx"`
3. 用 `getOutputStream()` 输出文件字节

---

## 十五、总结与拓展

### 今日总结

- **开发环境**：JDK + Maven + Tomcat + IDEA，各司其职
- **Maven**：依赖管理 + 构建，配置阿里云镜像加速
- **Tomcat**：Servlet 容器，管理 Servlet 生命周期
- **Servlet**：
  - 本质是运行在服务器端的 Java 程序
  - 生命周期：实例化 → init → service → destroy
  - **单例多线程**，注意线程安全
  - 继承 `HttpServlet`，重写 `doGet` / `doPost`
- **HttpServletRequest**：封装请求信息（方法、URI、Header、参数、Cookie、Session）
- **HttpServletResponse**：构建响应（状态码、Header、Body、重定向、Cookie）
- **编码处理**：POST 请求设 `setCharacterEncoding`，响应设 `setContentType`
- **RESTful 落地**：用 `getPathInfo()` + `getMethod()` 分发

### 核心记忆点

```
环境：JDK + Maven + Tomcat + IDEA
Tomcat 10+ 用 jakarta.servlet.*，Tomcat 9 用 javax.servlet.*
Servlet 生命周期：构造 → init → service → destroy
Servlet 是单例，成员变量不安全
HttpServlet 根据方法分发到 doGet/doPost
请求：getMethod / getParameter / getHeader / getSession
响应：setContentType / getWriter / sendRedirect / addCookie
POST 编码：req.setCharacterEncoding("UTF-8") 放最前
响应编码：resp.setContentType("text/html;charset=UTF-8")
```

### 关键对比

| 对比 | 结论 |
|------|------|
| `@WebServlet` vs web.xml | 注解简洁，web.xml 集中 |
| doGet vs doPost | 浏览器地址栏是 GET，表单提交可 POST |
| ServletConfig vs ServletContext | Servlet 级 vs 应用级 |
| 转发 vs 重定向 | Day3 详解 |
| getWriter vs getOutputStream | 字符流 vs 字节流，不能混用 |

### 拓展阅读

- [Jakarta Servlet 官方规范](https://jakarta.ee/specifications/servlet/)
- [Tomcat 官方文档](https://tomcat.apache.org/tomcat-10.1-doc/index.html)
- [Maven 官方指南](https://maven.apache.org/guides/)
- [MDN HTTP 请求方法](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Methods)

### 明日预告

Day3 将学习 **Servlet 进阶**：
- 请求转发与重定向的区别与实战
- Cookie 与 Session 深度使用
- Filter（过滤器）：编码过滤、登录校验
- Listener（监听器）：在线人数统计
- 完成一个简易登录会话管理完整示例

# 我学到的
1.放在 webapp 下面的 html、css、js、图片，是可以直接通过浏览器访问到的资源。
2.
1. **最高优先级（天天用）**：`src/main/java`（写逻辑）、`src/main/webapp`（写页面/配置）、`pom.xml`（加依赖）。
    
2. **次高优先级（需要配置）**：`src/main/resources`（放配置文件）。
    
3. **最低优先级（知道就行）**：`.idea`、`.smarttomcat`、`.iml`、`.gitignore`（这些是工具自动生成的，不用你操心）。
    

**一句话总结**：只需要在 `java` 里写代码，在 `webapp` 里写页面，在 `pom.xml` 里找依赖，在 `resources` 里写配置，其他的交给 IDEA 和 Maven 自动处理