

## 学习目标
- 掌握请求转发（forward）与重定向（redirect）的区别与使用场景
- 实战 Cookie 与 Session，理解会话管理机制
- 理解并编写 Filter（过滤器）与 Listener（监听器）
- 完成一个简易登录会话管理的完整示例

---

## 一、请求转发 vs 重定向

这是 Servlet 开发中最容易混淆的两个概念，务必理解透彻。

### 1. 请求转发（Forward）

**特点：**
- 服务器内部跳转，浏览器地址栏**不变**
- 只有**一次请求**，共享同一个 `request` 对象
- 只能转发到**当前应用内部**的资源
- 状态码为 200

**代码：**
```java
@WebServlet("/forward-demo")
public class ForwardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 存数据到 request 域
        req.setAttribute("msg", "来自 ForwardServlet 的问候");
        req.setAttribute("time", new java.util.Date());

        // 转发到目标资源（可以是 JSP、另一个 Servlet）
        req.getRequestDispatcher("/result.jsp").forward(req, resp);
    }
}
```

**目标 result.jsp：**
```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
    <h2>${msg}</h2>
    <p>时间：${time}</p>
</body>
</html>
```

### 2. 重定向（Redirect）

**特点：**
- 浏览器**重新发起**请求，地址栏**改变**
- **两次请求**，`request` 对象不共享（第一次的 request 数据丢失）
- 可以重定向到**任意 URL**（包括外部网站）
- 状态码为 302

**代码：**
```java
@WebServlet("/redirect-demo")
public class RedirectServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 重定向到登录页（注意加上下文路径）
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }
}
```

### 3. 对比总结

| 特性 | 转发 forward | 重定向 redirect |
|------|-------------|-----------------|
| 地址栏 | 不变 | 改变 |
| 请求次数 | 1 次 | 2 次 |
| request 共享 | ✅ 共享 | ❌ 不共享 |
| 目标范围 | 应用内部 | 任意 URL |
| 能否访问 WEB-INF | ✅ 能 | ❌ 不能 |
| 状态码 | 200 | 302 |
| 效率 | 高 | 低（多一次请求） |
| 代码 | `req.getRequestDispatcher("/x").forward(req, resp)` | `resp.sendRedirect("x")` |

### 4. 使用场景

- **转发**：查询后展示结果页、MVC 中 Controller 转发到 View
- **重定向**：表单提交成功后跳转（防止刷新重复提交，PRG 模式）、登录成功跳首页、退出登录

> 💡 **PRG 模式（Post-Redirect-Get）**：POST 提交后用重定向跳转到 GET 页面，避免用户刷新导致重复提交。

---

## 二、Cookie 实战

### 1. Cookie 是什么
Cookie 是服务器保存在**客户端**的一小段文本数据（≤4KB），每次请求浏览器会自动携带。常用于记住登录状态、用户偏好。

### 2. Cookie 的创建与读取

**创建并发送 Cookie：**
```java
@WebServlet("/set-cookie")
public class SetCookieServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");

        // 创建 Cookie
        Cookie usernameCookie = new Cookie("username", "张三");
        usernameCookie.setMaxAge(7 * 24 * 3600); // 7 天，单位秒
        usernameCookie.setPath("/");             // 整个应用可访问
        // usernameCookie.setHttpOnly(true);      // 禁止 JS 访问，防 XSS
        // usernameCookie.setSecure(true);        // 仅 HTTPS 传输

        Cookie themeCookie = new Cookie("theme", "dark");
        themeCookie.setMaxAge(30 * 24 * 3600);

        resp.addCookie(usernameCookie);
        resp.addCookie(themeCookie);

        resp.getWriter().println("Cookie 已设置");
    }
}
```

**读取 Cookie：**
```java
@WebServlet("/get-cookie")
public class GetCookieServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        Cookie[] cookies = req.getCookies();
        if (cookies == null || cookies.length == 0) {
            out.println("没有 Cookie");
            return;
        }
        for (Cookie c : cookies) {
            out.println(c.getName() + " = " + c.getValue() + "<br>");
        }
    }
}
```

**删除 Cookie：**
```java
Cookie cookie = new Cookie("username", "");
cookie.setMaxAge(0);       // 立即失效
cookie.setPath("/");       // 路径必须与原 Cookie 一致
resp.addCookie(cookie);
```

### 3. Cookie 常用方法

| 方法 | 说明 |
|------|------|
| `getName()` | 获取名称 |
| `getValue()` | 获取值 |
| `setMaxAge(int)` | 有效期（秒），负数为会话级，0 为删除 |
| `setPath(String)` | 作用路径 |
| `setDomain(String)` | 作用域名 |
| `setHttpOnly(boolean)` | 禁止 JS 访问（防 XSS） |
| `setSecure(boolean)` | 仅 HTTPS 传输 |

### 4. Cookie 的限制
- 单个 ≤4KB，每个域名数量有限（约 20-50 个）
- 存储在客户端，**不安全**（可被篡改、查看）
- 用户可以禁用 Cookie
- 中文需 URL 编码：`URLEncoder.encode(value, "UTF-8")`

---

## 三、Session 实战

### 1. Session 是什么
Session 是服务器端为每个用户创建的会话对象，数据存在**服务器内存**中。服务器通过 Cookie 中的 `JSESSIONID` 来识别用户对应的 Session。

### 2. Session 基本操作

```java
@WebServlet("/session-demo")
public class SessionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");

        // 获取或创建 Session
        HttpSession session = req.getSession();
        System.out.println("Session ID: " + session.getId());

        // 存数据
        session.setAttribute("user", "张三");
        session.setAttribute("loginTime", new Date());

        // 取数据
        String user = (String) session.getAttribute("user");

        // 删除某个属性
        session.removeAttribute("user");

        // 手动失效
        // session.invalidate();

        resp.getWriter().println("Session ID: " + session.getId());
    }
}
```

### 3. Session 常用方法

| 方法 | 说明 |
|------|------|
| `getSession()` | 获取或创建 Session |
| `getSession(false)` | 获取 Session，不存在返回 null |
| `setAttribute(name, value)` | 存数据 |
| `getAttribute(name)` | 取数据 |
| `removeAttribute(name)` | 删除属性 |
| `getId()` | 获取 Session ID |
| `invalidate()` | 使 Session 失效 |
| `setMaxInactiveInterval(int)` | 超时时间（秒） |
| `isNew()` | 是否新建 |

### 4. Session 超时配置

**方式一：web.xml**
```xml
<session-config>
    <session-timeout>30</session-timeout>  <!-- 单位：分钟 -->
</session-config>
```

**方式二：代码**
```java
session.setMaxInactiveInterval(30 * 60); // 30 分钟，单位秒
```

### 5. Cookie vs Session 对比

| 特性 | Cookie | Session |
|------|--------|---------|
| 存储位置 | 客户端 | 服务器 |
| 安全性 | 低（可篡改） | 高 |
| 容量 | 4KB | 受服务器内存限制 |
| 生命周期 | 可长期保存 | 会话级/超时 |
| 服务器压力 | 无 | 有（内存占用） |
| 跨域 | 支持 | 依赖 Cookie 传 ID |
| 适用场景 | 记住我、偏好 | 登录状态、购物车 |

### 6. Session 的底层原理
1. 首次调用 `req.getSession()`，服务器创建 Session 对象，生成唯一 `JSESSIONID`
2. 服务器通过 `Set-Cookie: JSESSIONID=xxx` 下发到浏览器
3. 后续请求浏览器自动携带 `Cookie: JSESSIONID=xxx`
4. 服务器据此找到对应 Session

> 💡 **"记住我"功能**：把登录凭证（如 token）存入持久 Cookie（`setMaxAge` 较长），下次访问自动登录。Session 默认随浏览器关闭失效。

---

## 四、Filter 过滤器

### 1. Filter 是什么
Filter 可以在请求到达 Servlet **之前**、响应返回客户端**之后**进行拦截处理。常见用途：编码统一、登录校验、日志记录、权限控制。

### 2. Filter 生命周期

| 阶段 | 方法 | 说明 |
|------|------|------|
| 初始化 | `init(FilterConfig)` | Web 应用启动时调用一次 |
| 过滤 | `doFilter(req, resp, chain)` | 每次匹配请求调用 |
| 销毁 | `destroy()` | 应用关闭时调用一次 |

### 3. 第一个 Filter：统一编码

```java
@WebFilter("/*")  // 拦截所有请求
public class EncodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("EncodingFilter 初始化");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        // 请求前处理
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        // 放行（传递给下一个 Filter 或 Servlet）
        chain.doFilter(req, resp);

        // 响应后处理（一般不用）
    }

    @Override
    public void destroy() {
        System.out.println("EncodingFilter 销毁");
    }
}
```

> ⚠️ `chain.doFilter()` 是必须调用的，否则请求会被中断，无法到达 Servlet。

### 4. 登录校验 Filter（重点）

```java
@WebFilter("/admin/*")
public class AuthFilter implements Filter {

    // 白名单：无需登录的路径
    private static final Set<String> WHITE_LIST = Set.of("/login", "/register");

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String path = request.getRequestURI()
                .substring(request.getContextPath().length());

        // 白名单放行
        if (WHITE_LIST.contains(path)) {
            chain.doFilter(req, resp);
            return;
        }

        // 检查 Session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            // 未登录，重定向到登录页
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // 已登录，放行
        chain.doFilter(req, resp);
    }
}
```

### 5. Filter 链
多个 Filter 会组成一条链，按注册顺序（`@WebFilter` 按类名，web.xml 按配置顺序）依次执行：

```
请求 → Filter1 → Filter2 → Filter3 → Servlet
响应 ← Filter1 ← Filter2 ← Filter3 ← Servlet
```

### 6. Filter 配置方式

**注解方式：**
```java
@WebFilter(urlPatterns = "/*", filterName = "encodingFilter")
```

**web.xml 方式：**
```xml
<filter>
    <filter-name>encodingFilter</filter-name>
    <filter-class>com.example.filter.EncodingFilter</filter-class>
    <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>encodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

### 7. Filter 常见应用
- 统一编码（UTF-8）
- 登录/权限校验
- 请求日志（记录 URL、IP、耗时）
- 敏感词过滤
- 跨域处理（CORS）

---

## 五、Listener 监听器

### 1. Listener 是什么
Listener 用于监听 Web 应用中的事件（如应用启动、Session 创建/销毁），并在事件发生时执行相应代码。

### 2. 常用监听器

| 监听器接口 | 监听对象 | 常用场景 |
|-----------|----------|----------|
| `ServletContextListener` | 应用启动/关闭 | 初始化数据源、加载配置 |
| `HttpSessionListener` | Session 创建/销毁 | 统计在线人数 |
| `ServletRequestListener` | 请求创建/销毁 | 请求日志 |
| `ServletContextAttributeListener` | 应用属性变化 | 监控配置变更 |
| `HttpSessionAttributeListener` | Session 属性变化 | 监控用户状态 |

### 3. ServletContextListener 示例

```java
@WebListener
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== 应用启动 ===");
        // 初始化数据库连接池
        // 加载全局配置
        ServletContext ctx = sce.getServletContext();
        ctx.setAttribute("appName", "用户管理系统");
        ctx.setAttribute("startTime", new Date());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== 应用关闭 ===");
        // 释放资源（关闭连接池等）
    }
}
```

### 4. HttpSessionListener 示例：统计在线人数

```java
@WebListener
public class OnlineCountListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        ServletContext ctx = se.getSession().getServletContext();
        Integer count = (Integer) ctx.getAttribute("onlineCount");
        if (count == null) count = 0;
        ctx.setAttribute("onlineCount", count + 1);
        System.out.println("新用户上线，当前在线：" + (count + 1));
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        ServletContext ctx = se.getSession().getServletContext();
        Integer count = (Integer) ctx.getAttribute("onlineCount");
        if (count == null) count = 0;
        if (count > 0) {
            ctx.setAttribute("onlineCount", count - 1);
        }
        System.out.println("用户下线，当前在线：" + (count - 1));
    }
}
```

> 💡 Session 销毁的两种方式：手动 `invalidate()` 或超时。

### 5. 在 JSP 中读取应用级数据
```jsp
<p>应用名称：${applicationScope.appName}</p>
<p>当前在线人数：${applicationScope.onlineCount}</p>
```

---

## 六、实战：简易登录会话管理

### 功能
1. 登录页面输入用户名密码
2. 登录成功写入 Session，跳转后台首页
3. 未登录访问后台被拦截，跳转登录页
4. 退出登录清除 Session

### 项目结构
```
login-demo/
└── src/main/
    ├── java/com/example/
    │   ├── servlet/
    │   │   ├── LoginServlet.java
    │   │   └── LogoutServlet.java
    │   └── filter/AuthFilter.java
    └── webapp/
        ├── login.jsp
        ├── admin/
        │   └── index.jsp
        └── WEB-INF/web.xml
```

### 1. 登录页面 login.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>登录</title></head>
<body>
    <h2>用户登录</h2>
    <%-- 显示错误信息 --%>
    <c:if test="${not empty error}">
        <p style="color:red">${error}</p>
    </c:if>
    <form action="${pageContext.request.contextPath}/login" method="post">
        用户名：<input type="text" name="username"><br><br>
        密码：<input type="password" name="password"><br><br>
        <button type="submit">登录</button>
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
        // GET 请求转发到登录页
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 设置编码
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // 模拟校验（真实项目查数据库）
        if ("admin".equals(username) && "123456".equals(password)) {
            // 登录成功：存入 Session
            HttpSession session = req.getSession();
            session.setAttribute("user", username);
            session.setAttribute("loginTime", new Date());

            // 重定向到后台首页（PRG 模式）
            resp.sendRedirect(req.getContextPath() + "/admin/index.jsp");
        } else {
            // 登录失败：转发回登录页并提示
            req.setAttribute("error", "用户名或密码错误");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
```

### 3. LogoutServlet

```java
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate(); // 使 Session 失效
        }
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }
}
```

### 4. AuthFilter 登录拦截

```java
@WebFilter("/admin/*")
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        chain.doFilter(req, resp);
    }
}
```

### 5. 后台首页 admin/index.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
    <h2>后台首页</h2>
    <p>欢迎，${sessionScope.user}！</p>
    <p>登录时间：${sessionScope.loginTime}</p>
    <a href="${pageContext.request.contextPath}/logout">退出登录</a>
</body>
</html>
```

### 6. 测试流程
1. 访问 `http://localhost:8080/login-demo/admin/index.jsp` → 被拦截，跳转登录页
2. 输入 admin / 123456 → 登录成功，跳转后台首页
3. 直接访问后台页面 → 正常显示
4. 点击退出 → Session 失效，跳转登录页
5. 再次访问后台 → 又被拦截

### 7. 进阶练习
- 添加"记住我"复选框：勾选后用 Cookie 保存用户名
- 添加验证码功能
- 用监听器统计在线人数，在后台首页显示
- 记录每个用户的登录日志（用 Session 存历史）

---

## 七、常见问题与排错

| 问题 | 原因 | 解决 |
|------|------|------|
| 转发后 404 | 路径写错或目标在 WEB-INF 外 | 转发路径以 `/` 开头，相对应用根 |
| 重定向后 404 | 忘记加 contextPath | 用 `req.getContextPath() + "/xxx"` |
| Filter 未生效 | urlPattern 不匹配 | 检查 `@WebFilter` 路径 |
| Filter 中 request 强转失败 | 未判断类型 | 用 `instanceof` 判断后再转 |
| Session 取不到值 | 跨请求未带 Cookie 或 Session 失效 | 检查浏览器是否禁用 Cookie |
| 中文乱码 | 未设置编码 | Filter 统一设置 UTF-8 |
| 重定向丢失 request 数据 | 重定向是两次请求 | 改用转发或存入 Session |

---

## 八、总结与拓展

### 今日总结
- **转发**是服务器内部跳转（1 次请求），**重定向**是浏览器重新请求（2 次请求）
- **Cookie** 存客户端，**Session** 存服务器，各有权衡
- **Filter** 拦截请求，用于编码、登录校验、日志
- **Listener** 监听事件，用于初始化资源、统计在线
- 完成了登录会话管理完整示例（Session + Filter + 转发/重定向）

### 核心记忆点
```
转发：req.getRequestDispatcher("/x").forward(req, resp)
重定向：resp.sendRedirect(req.getContextPath() + "/x")
Session：req.getSession().setAttribute("user", ...)
Filter：chain.doFilter(req, resp) 必须调用
```

### 拓展阅读
- [Jakarta Servlet Filter 规范](https://jakarta.ee/specifications/servlet/)
- [MDN HTTP Cookies](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Cookies)
- [Servlet Session 机制详解](https://tomcat.apache.org/tomcat-10.1-doc/config/context.html)

