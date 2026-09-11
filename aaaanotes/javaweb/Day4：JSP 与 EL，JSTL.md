
## 学习目标
- 理解 JSP 的本质（编译为 Servlet）与执行过程
- 掌握 JSP 基本语法与九大内置对象
- 熟练使用 EL 表达式简化取值
- 掌握 JSTL 核心标签库
- 理解 MVC 模式，用 JSP + Servlet 重构 Day3 的登录示例

---

## 一、JSP 基础

### 1. JSP 是什么

JSP（JavaServer Pages）是一种**动态网页技术**，允许在 HTML 中嵌入 Java 代码。它的本质是**被容器编译成 Servlet 后执行**。

**JSP 执行过程：**
```
浏览器请求 index.jsp
    ↓
Tomcat 检查是否已编译
    ↓ 否
将 index.jsp 翻译成 index_jsp.java（一个 Servlet）
    ↓
编译成 index_jsp.class
    ↓
执行 Servlet，生成 HTML
    ↓
返回给浏览器
```

> 💡 第一次访问 JSP 会慢一些（需要翻译+编译），之后直接执行编译好的 Servlet。可以在 Tomcat 的 `work` 目录下找到生成的 `.java` 文件。

**为什么现在很少直接用 JSP？**
- JSP 把 Java 代码和 HTML 混在一起，难以维护
- 前后端分离成为主流（后端返回 JSON，前端用 Vue/React 渲染）
- 但理解 JSP 有助于理解 MVC 和模板引擎思想

> ⚠️ **学习建议**：JSP 是理解 Java Web 视图层的重要一环，但实际项目中更推荐前后端分离。本课重点掌握 EL/JSTL 和 MVC 思想。

### 2. JSP 基本语法

**（1）注释**
```jsp
<%-- JSP 注释，不会发送到浏览器 --%>
<!-- HTML 注释，会发送到浏览器（查看源码可见） -->
```

**（2）指令（Directive）**
```jsp
<%-- page 指令：设置页面属性 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.example.entity.User" %>
<%@ page errorPage="/error.jsp" %>       <%-- 出错跳转 --%>
<%@ page isErrorPage="true" %>           <%-- 声明为错误页 --%>

<%-- include 指令：静态包含（编译时合并） --%>
<%@ include file="header.jsp" %>

<%-- taglib 指令：引入标签库 --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
```

**（3）声明（Declaration）**
```jsp
<%! 
    // 声明成员变量或方法，翻译到 Servlet 的类体中
    private int count = 0;
    public String greet(String name) {
        return "Hello, " + name;
    }
%>
```

**（4）脚本片段（Scriptlet）**
```jsp
<%
    // 普通 Java 代码，翻译到 _jspService 方法中
    int x = 10;
    int y = 20;
    String result = "结果是：" + (x + y);
%>
```

**（5）表达式（Expression）**
```jsp
<%= result %>          <%-- 输出变量，注意没有分号 --%>
<%= new Date() %>      <%-- 输出对象 --%>
```

> ⚠️ 现代 JSP 开发**不推荐**使用脚本片段和表达式，应使用 EL + JSTL 替代。下面的示例仅为理解语法。

### 3. JSP 九大内置对象

JSP 翻译成 Servlet 后，容器自动创建了 9 个对象，可以直接在 JSP 中使用：

| 对象 | 类型 | 作用域 | 说明 |
|------|------|--------|------|
| `request` | HttpServletRequest | request | 请求对象 |
| `response` | HttpServletResponse | - | 响应对象 |
| `session` | HttpSession | session | 会话对象 |
| `application` | ServletContext | application | 应用对象 |
| `out` | JspWriter | - | 输出流 |
| `pageContext` | PageContext | page | 页面上下文（可访问其他作用域） |
| `config` | ServletConfig | - | Servlet 配置 |
| `page` | Object | - | 当前 Servlet 实例（this） |
| `exception` | Throwable | - | 异常（仅 isErrorPage="true" 可用） |

**四大作用域对比：**

| 作用域 | 对象 | 生命周期 | 范围 |
|--------|------|----------|------|
| page | pageContext | 当前页面 | 最小 |
| request | request | 一次请求 | 转发内共享 |
| session | session | 一次会话 | 跨请求 |
| application | application | 应用启动到关闭 | 整个应用 |

**作用域数据操作（以 request 为例）：**
```jsp
<%
    request.setAttribute("key", "value");
    Object v = request.getAttribute("key");
    request.removeAttribute("key");
%>
```

### 4. JSP 与 Servlet 的区别

| 特性 | JSP | Servlet |
|------|-----|---------|
| 侧重点 | 视图展示 | 逻辑处理 |
| 编写方式 | HTML 中嵌 Java | Java 中输出 HTML |
| 编译 | 翻译成 Servlet | 直接编译 |
| 适用场景 | 页面展示 | 控制器、业务 |

> 💡 **经验**：Servlet 负责处理请求和业务逻辑，JSP 负责展示数据（MVC 中的 V）。

---

## 二、EL 表达式

### 1. EL 是什么

EL（Expression Language，表达式语言）用于**简化 JSP 中的取值操作**，语法为 `${表达式}`。

**传统方式 vs EL：**
```jsp
<%-- 传统方式：繁琐，需要强转 --%>
<%= ((User) request.getAttribute("user")).getName() %>

<%-- EL 方式：简洁 --%>
${user.name}
```

### 2. EL 基本语法

```jsp
<%-- 1. 直接取值（自动按作用域查找） --%>
${user.name}

<%-- 2. 指定作用域 --%>
${pageScope.user}
${requestScope.user}
${sessionScope.user}
${applicationScope.user}

<%-- 3. 取 Map 或数组 --%>
${map['key']}
${map.key}
${arr[0]}

<%-- 4. 算术运算 --%>
${1 + 2}      <%-- 3 --%>
${10 / 3}     <%-- 3.333... --%>
${10 % 3}     <%-- 1 --%>

<%-- 5. 比较运算 --%>
${user.age > 18}
${user.age >= 18 and user.age <= 60}
${user.age == 18 or user.age == 20}
${not empty user}

<%-- 6. 三元运算 --%>
${user.age >= 18 ? '成年' : '未成年'}

<%-- 7. 判空 --%>
${empty user}          <%-- null 或空字符串/空集合 为 true --%>
${not empty userList}

<%-- 8. 隐式对象 --%>
${param.name}              <%-- 等价 request.getParameter("name") --%>
${paramValues.hobby[0]}    <%-- 多值参数 --%>
${header['User-Agent']}    <%-- 请求头 --%>
${cookie.JSESSIONID.value} <%-- Cookie --%>
${pageContext.request.contextPath} <%-- 上下文路径（常用） --%>
```

### 3. EL 查找顺序

当不指定作用域时，EL 按以下顺序查找：
```
pageScope → requestScope → sessionScope → applicationScope
```
找到即返回，找不到返回 null（不报错）。

### 4. EL 访问对象属性

EL 通过**反射**调用 getter 方法：
```jsp
${user.name}      <%-- 调用 user.getName() --%>
${user.age}       <%-- 调用 user.getAge() --%>
${user['name']}   <%-- 等价 --%>
```

**要求实体类必须有 getter 方法**，否则报错。

### 5. 常用上下文路径技巧

```jsp
<%-- 避免硬编码应用名 --%>
<a href="${pageContext.request.contextPath}/login">登录</a>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
```

或者在页面顶部定义变量：
```jsp
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<a href="${ctx}/login">登录</a>
```

---

## 三、JSTL 标签库

### 1. JSTL 是什么

JSTL（JSP Standard Tag Library，JSP 标准标签库）提供了一组标准标签，用于**替代 JSP 脚本片段**，实现逻辑控制、循环、格式化等。

### 2. 引入 JSTL

**Jakarta EE 10（Tomcat 10+）依赖：**
```xml
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
```

**Tomcat 9 及以下（javax）：**
```xml
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>jstl</artifactId>
    <version>1.2</version>
</dependency>
```

**页面引入：**
```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>       <%-- 核心标签 --%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>      <%-- 格式化 --%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %> <%-- 函数 --%>
```

> 注意：Tomcat 10 用 `jakarta.tags.core`，Tomcat 9 用 `http://java.sun.com/jsp/jstl/core`。

### 3. 核心标签（c 标签）

**（1）`<c:out>` 输出**
```jsp
<c:out value="${user.name}" default="匿名"/>
<c:out value="${user.name}" escapeXml="true"/>  <%-- 默认转义 HTML，防 XSS --%>
```

**（2）`<c:set>` / `<c:remove>` 设置/删除变量**
```jsp
<c:set var="name" value="张三" scope="request"/>
<c:set var="age" value="${user.age}"/>
<c:remove var="name" scope="request"/>
```

**（3）`<c:if>` 条件**
```jsp
<c:if test="${user.age >= 18}">
    <p>已成年</p>
</c:if>
```

**（4）`<c:choose>` / `<c:when>` / `<c:otherwise>` 多分支**
```jsp
<c:choose>
    <c:when test="${score >= 90}">优秀</c:when>
    <c:when test="${score >= 80}">良好</c:when>
    <c:when test="${score >= 60}">及格</c:when>
    <c:otherwise>不及格</c:otherwise>
</c:choose>
```

**（5）`<c:forEach>` 循环（最常用）**
```jsp
<%-- 遍历集合 --%>
<c:forEach items="${userList}" var="u" varStatus="status">
    <tr>
        <td>${status.index + 1}</td>      <%-- 索引，从 0 开始 --%>
        <td>${status.count}</td>          <%-- 计数，从 1 开始 --%>
        <td>${u.name}</td>
        <td>${u.age}</td>
        <td>${status.first ? '首行' : ''}</td>
        <td>${status.last ? '末行' : ''}</td>
    </tr>
</c:forEach>

<%-- 数字循环 --%>
<c:forEach begin="1" end="10" step="1" var="i">
    ${i}
</c:forEach>

<%-- 遍历 Map --%>
<c:forEach items="${map}" var="entry">
    ${entry.key} = ${entry.value}
</c:forEach>
```

**（6）`<c:url>` / `<c:param>` 构建 URL**
```jsp
<c:url value="/user/detail" var="detailUrl">
    <c:param name="id" value="${user.id}"/>
    <c:param name="from" value="list"/>
</c:url>
<a href="${detailUrl}">查看详情</a>
<%-- 生成：/app/user/detail?id=1&from=list --%>
```

**（7）`<c:redirect>` 重定向**
```jsp
<c:redirect url="/login.jsp"/>
```

**（8）`<c:catch>` 捕获异常**
```jsp
<c:catch var="ex">
    <% int x = 1 / 0; %>
</c:catch>
<c:if test="${not empty ex}">
    异常：${ex.message}
</c:if>
```

### 4. 格式化标签（fmt 标签）

```jsp
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- 日期格式化 --%>
<fmt:formatDate value="${user.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>

<%-- 数字格式化 --%>
<fmt:formatNumber value="${price}" pattern="#,##0.00"/>
<fmt:formatNumber value="${rate}" type="percent" maxFractionDigits="2"/>

<%-- 国际化 --%>
<fmt:setLocale value="zh_CN"/>
<fmt:setBundle basename="messages"/>
<fmt:message key="login.title"/>
```

### 5. 函数标签（fn 标签）

```jsp
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

${fn:length(userList)}              <%-- 集合长度 --%>
${fn:toUpperCase(user.name)}        <%-- 转大写 --%>
${fn:toLowerCase(user.name)}        <%-- 转小写 --%>
${fn:trim(user.name)}               <%-- 去空格 --%>
${fn:substring(user.name, 0, 3)}    <%-- 截取 --%>
${fn:contains(user.name, '张')}     <%-- 包含 --%>
${fn:replace(user.name, 'a', 'b')}  <%-- 替换 --%>
${fn:split('a,b,c', ',')[0]}        <%-- 分割 --%>
${fn:join(arr, '-')}                <%-- 连接 --%>
${fn:escapeXml(user.content)}       <%-- HTML 转义（防 XSS） --%>
```

### 6. JSTL 使用注意事项

- JSTL 标签**不能嵌套在 HTML 属性中直接使用**，需配合 `<c:set>` 或 `<c:url>`
- `<c:forEach>` 遍历的集合需要在作用域中存在（request/session 等）
- EL 中不能直接调用方法（如 `${user.getName()}` 会报错），应写 `${user.name}`
- JSTL 表达式中的字符串用**单引号**，外层是双引号

---

## 四、MVC 模式

### 1. 什么是 MVC

MVC（Model-View-Controller）是一种设计模式，将应用分为三层：

| 层 | 职责 | 技术实现 |
|----|------|----------|
| Model（模型） | 数据与业务逻辑 | JavaBean、Service、DAO |
| View（视图） | 展示数据 | JSP、HTML、Thymeleaf |
| Controller（控制器） | 接收请求、调度 | Servlet |

**MVC 请求流程：**
```
用户请求
    ↓
Controller (Servlet)
    ↓ 调用
Model (Service/DAO) → 数据库
    ↓ 返回数据存入 request/session
View (JSP) 渲染
    ↓
返回 HTML 给浏览器
```

### 2. 为什么用 MVC

- **职责分离**：Java 代码和 HTML 分离，便于维护
- **可复用**：Model 可被多个 View 复用
- **可测试**：业务逻辑独立于视图
- **团队协作**：前端写 JSP，后端写 Servlet

### 3. JSP 存放位置

**推荐放在 `WEB-INF/views/` 下**，原因：
- `WEB-INF` 下的资源**不能被浏览器直接访问**
- 只能通过 Controller 转发访问，保证所有请求都经过 Controller 处理
- 防止用户绕过逻辑直接访问 JSP

**目录结构：**
```
webapp/
├── WEB-INF/
│   ├── web.xml
│   └── views/
│       ├── login.jsp
│       ├── user-list.jsp
│       └── user-form.jsp
├── static/
│   ├── css/
│   └── js/
└── index.jsp
```

---

## 五、实战：用 MVC 重构 Day3 登录示例

### 目标
把 Day3 的登录功能改用 MVC 模式：Servlet 处理逻辑，JSP 只负责展示，用 EL + JSTL 替代脚本片段。

### 项目结构
```
login-mvc/
└── src/main/
    ├── java/com/example/
    │   ├── entity/User.java
    │   ├── servlet/
    │   │   ├── LoginServlet.java
    │   │   └── LogoutServlet.java
    │   └── filter/AuthFilter.java
    └── webapp/
        ├── WEB-INF/
        │   └── views/
        │       ├── login.jsp
        │       └── admin.jsp
        └── index.jsp
```

### 1. 实体类 User.java

```java
package com.example.entity;

public class User {
    private String username;
    private String nickname;
    private Integer age;

    public User() {}
    public User(String username, String nickname, Integer age) {
        this.username = username;
        this.nickname = nickname;
        this.age = age;
    }

    // getter/setter（EL 需要 getter）
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
```

### 2. LoginServlet.java

```java
package com.example.servlet;

import com.example.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Date;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 直接转发到登录页（View）
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // 模拟校验（真实项目查数据库）
        if ("admin".equals(username) && "123456".equals(password)) {
            // 构造 Model
            User user = new User(username, "管理员", 30);

            // 存入 Session
            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            session.setAttribute("loginTime", new Date());

            // 重定向到后台（PRG 模式）
            resp.sendRedirect(req.getContextPath() + "/admin");
        } else {
            // 失败：把错误信息存入 request，转发回登录页
            req.setAttribute("error", "用户名或密码错误");
            req.setAttribute("username", username); // 回显用户名
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}
```

### 3. AdminServlet.java（替代直接访问 JSP）

```java
package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 数据已在 Session 中，直接转发到 View
        req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, resp);
    }
}
```

### 4. LogoutServlet.java

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

### 5. AuthFilter.java（拦截后台）

```java
package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/admin")
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        chain.doFilter(req, resp);
    }
}
```

### 6. login.jsp（纯 EL + JSTL，无 Java 代码）

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>登录</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
</head>
<body>
    <div class="login-box">
        <h2>用户登录</h2>

        <%-- 显示错误信息 --%>
        <c:if test="${not empty error}">
            <p class="error">${error}</p>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <div>
                <label>用户名：</label>
                <input type="text" name="username" value="${username}" required>
            </div>
            <div>
                <label>密码：</label>
                <input type="password" name="password" required>
            </div>
            <button type="submit">登录</button>
        </form>
    </div>
</body>
</html>
```

### 7. admin.jsp（用 EL 取 Session 数据）

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>后台首页</title>
</head>
<body>
    <h2>后台首页</h2>

    <%-- 用 EL 取 Session 中的 User 对象 --%>
    <p>欢迎，${sessionScope.user.nickname}（${sessionScope.user.username}）！</p>
    <p>年龄：${sessionScope.user.age}</p>

    <%-- 格式化日期 --%>
    <p>登录时间：
        <fmt:formatDate value="${sessionScope.loginTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
    </p>

    <a href="${pageContext.request.contextPath}/logout">退出登录</a>
</body>
</html>
```

### 8. index.jsp（首页跳转）

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<% response.sendRedirect(request.getContextPath() + "/login"); %>
```

> 更规范的做法是写一个 `IndexServlet` 处理 `/`，而不是在 JSP 里写脚本。

### 9. 测试流程
1. 访问 `http://localhost:8080/login-mvc/` → 跳转登录页
2. 输入错误密码 → 登录页显示错误，用户名回显
3. 输入 admin/123456 → 跳转后台，显示昵称、年龄、格式化时间
4. 直接访问 `/admin`（未登录）→ 被 Filter 拦截，跳登录页
5. 点击退出 → 清除 Session，跳登录页
6. 查看页面源码，确认 JSP 中**没有 Java 代码**

### 10. 重构前后对比

| 方面 | Day3 版本 | Day4 MVC 版本 |
|------|-----------|---------------|
| JSP 中 Java 代码 | 有 `<% %>` | 无，全用 EL/JSTL |
| JSP 存放位置 | webapp 根目录 | WEB-INF/views/ |
| 数据传递 | 直接读写 | request/session + EL |
| 访问方式 | 直接访问 JSP | 通过 Servlet 转发 |
| 安全性 | 可绕过登录 | 无法直接访问 JSP |
| 可维护性 | 低 | 高 |

---

## 六、JSP 进阶技巧与注意事项

### 1. 静态包含 vs 动态包含

```jsp
<%-- 静态包含：编译时合并，共享变量 --%>
<%@ include file="header.jsp" %>

<%-- 动态包含：运行时包含，独立编译 --%>
<jsp:include page="header.jsp"/>
```

| 特性 | 静态 include | 动态 include |
|------|-------------|-------------|
| 时机 | 编译时 | 运行时 |
| 生成文件 | 1 个 Servlet | 2 个 Servlet |
| 变量共享 | 共享 | 不共享 |
| 适用 | 固定内容 | 动态内容 |

### 2. 防 XSS 攻击

用户输入直接输出到页面可能导致 XSS（跨站脚本攻击）：
```jsp
<%-- 危险：直接输出 --%>
${user.content}

<%-- 安全：转义 HTML --%>
<c:out value="${user.content}"/>
${fn:escapeXml(user.content)}
```

### 3. JSP 常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| EL 不生效 | web.xml 版本过低或 `isELIgnored` | 用 Servlet 4.0+ 的 web.xml |
| `${user.name}` 报错 | 没有 getter | 生成 getter 方法 |
| JSTL 标签不识别 | 缺依赖或 uri 错误 | 检查依赖和 taglib uri |
| 中文乱码 | 未设编码 | `<%@ page contentType="text/html;charset=UTF-8" %>` |
| 直接访问 JSP 绕过登录 | JSP 放在 webapp 下 | 移到 WEB-INF/views/ |
| 修改 JSP 不生效 | 缓存或未重新部署 | 清理浏览器缓存、Redeploy |

### 4. JSP 已过时？

**现状：**
- 新项目**很少**直接用 JSP，主流是前后端分离（Vue/React + RESTful API）
- 模板引擎替代方案：Thymeleaf、FreeMarker、Velocity
- 但**老项目维护**仍需掌握 JSP
- 理解 JSP 有助于理解模板引擎和 MVC 思想

**建议：**
- 掌握 EL/JSTL 和 MVC 思想（面试常问）
- 了解 Thymeleaf（Spring Boot 推荐）
- 实际项目优先前后端分离

---

## 七、实践练习

### 练习 1：用户列表展示
- 创建 `UserListServlet`，构造 `List<User>` 存入 request
- 编写 `user-list.jsp`，用 `<c:forEach>` 渲染表格
- 表头：序号、用户名、昵称、年龄、操作
- 奇偶行不同颜色（用 `${status.index % 2}`）

### 练习 2：条件展示
- 如果 `userList` 为空，显示"暂无数据"
- 如果用户年龄 ≥ 18 显示"成年"，否则"未成年"
- 用 `<c:choose>` 实现

### 练习 3：用户详情页
- `UserDetailServlet` 根据 id 查用户
- `user-detail.jsp` 用 EL 展示所有字段
- 用 `<fmt:formatDate>` 格式化创建时间

### 练习 4：把 Day3 登录示例完全重构
- 按本课 MVC 模式重构
- 确保 JSP 中无 `<% %>` 脚本
- 用 Filter 统一编码

### 练习 5（进阶）：分页展示
- 实现 `page`、`size` 参数
- 用 EL 计算总页数、当前页
- 用 `<c:forEach begin="1" end="${totalPages}" var="i">` 生成页码链接

---

## 八、总结与拓展

### 今日总结
- **JSP 本质**是编译为 Servlet，9 大内置对象可直接使用
- **EL** 用 `${}` 简化取值，支持运算、判空、隐式对象
- **JSTL** 用标签替代脚本，核心是 `<c:forEach>` 和 `<c:if>`
- **MVC 模式**：Servlet 做 Controller，JSP 做 View，JavaBean 做 Model
- JSP 放 `WEB-INF/views/` 下，只能通过 Servlet 转发访问
- 现代开发不推荐在 JSP 中写 Java 代码

### 核心记忆点
```
EL 取值：       ${user.name}、${sessionScope.user}
EL 判空：       ${empty list}
JSTL 循环：     <c:forEach items="${list}" var="u"> ... </c:forEach>
JSTL 条件：     <c:if test="${user.age >= 18}"> ... </c:if>
上下文路径：    ${pageContext.request.contextPath}
MVC 流程：      Servlet 存数据到 request → 转发 JSP → EL 取值展示
```

### 拓展阅读
- [Jakarta Pages (JSP) 规范](https://jakarta.ee/specifications/pages/)
- [JSTL 官方文档](https://jakarta.ee/specifications/tags/)
- [MDN: MVC 模式](https://developer.mozilla.org/zh-CN/docs/Glossary/MVC)
- [Thymeleaf 官网](https://www.thymeleaf.org/)（Spring Boot 推荐的替代方案）

