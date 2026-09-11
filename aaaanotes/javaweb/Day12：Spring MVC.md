

## 学习目标

- 理解 Spring MVC 的架构与请求处理流程
- 掌握 DispatcherServlet 与核心组件
- 熟练使用 `@Controller` / `@RestController` / `@RequestMapping` 等注解
- 掌握参数绑定（`@RequestParam`、`@PathVariable`、`@RequestBody`、`@ModelAttribute`）
- 掌握返回值处理（视图、JSON、`ResponseEntity`）
- 掌握拦截器（Interceptor）与全局异常处理
- 编写 RESTful 接口并用 Postman 测试
- 用 Spring MVC 替代 Servlet 重构项目

---

## 一、Spring MVC 概述

### 1. 什么是 Spring MVC

Spring MVC 是 Spring 框架的 **Web 层** 模块，基于 **MVC 设计模式**，用于构建 Web 应用和 RESTful API。

**核心思想：**
- **M（Model）**：数据与业务（Service、DAO）
- **V（View）**：视图（JSP、Thymeleaf、JSON）
- **C（Controller）**：控制器，接收请求、调度

**与 Servlet 的关系：**
- Servlet 是底层规范，Spring MVC 封装了 Servlet
- 一个 `DispatcherServlet` 处理所有请求，分发到各个 `@Controller`
- 开发者只写 Controller，不写 Servlet

### 2. Spring MVC 架构

```
浏览器请求
    ↓
DispatcherServlet（前端控制器，唯一 Servlet）
    ↓
HandlerMapping（找到对应 Controller 方法）
    ↓
HandlerAdapter（调用 Controller）
    ↓
Controller（业务处理，返回 ModelAndView）
    ↓
ViewResolver（解析视图名 → 实际 View）
    ↓
View（渲染 HTML / JSON）
    ↓
响应浏览器
```

**核心组件：**

| 组件 | 作用 |
|------|------|
| DispatcherServlet | 前端控制器，统一调度 |
| HandlerMapping | 请求 URL → Controller 方法 |
| HandlerAdapter | 调用 Controller 方法 |
| HandlerExceptionResolver | 异常处理 |
| ViewResolver | 视图名 → View |
| MultipartResolver | 文件上传 |
| LocaleResolver | 国际化 |
| ThemeResolver | 主题 |
| FlashMapManager | 重定向传参 |

> 💡 开发者只需关注 Controller，其他组件由框架配置。

### 3. 请求处理流程详解

```
1. 用户发送请求 → DispatcherServlet
2. DispatcherServlet 查询 HandlerMapping，找到 Handler（Controller 方法）
3. DispatcherServlet 通过 HandlerAdapter 调用 Handler
4. Handler 调用 Service、DAO，返回 ModelAndView
5. DispatcherServlet 将 ModelAndView 交给 ViewResolver
6. ViewResolver 解析视图名，返回 View
7. DispatcherServlet 渲染 View（填充 Model 数据）
8. 响应返回浏览器
```

**RESTful（返回 JSON）时：**
```
4. Handler 返回对象 → HttpMessageConverter 转换为 JSON → 直接写入响应
（跳过 ViewResolver）
```

---

## 二、环境搭建

### 1. Maven 依赖

```xml
<properties>
    <spring.version>6.1.6</spring.version>
    <servlet.version>6.0.0</servlet.version>
    <jackson.version>2.17.0</jackson.version>
</properties>

<dependencies>
    <!-- Spring MVC -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>${spring.version}</version>
    </dependency>

    <!-- Servlet API（provided） -->
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
        <version>${servlet.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- JSP + JSTL -->
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

    <!-- Jackson：JSON 转换 -->
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

    <!-- 日志 -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>2.0.13</version>
    </dependency>
</dependencies>
```

### 2. 配置方式

**方式一：web.xml + XML 配置（传统）**
**方式二：Java 配置 + 无 web.xml（推荐，Servlet 3.0+）**

本课用**方式二**。

### 3. 项目结构

```
spring-mvc-demo/
├── pom.xml
└── src/main/
    ├── java/com/example/
    │   ├── config/
    │   │   ├── WebAppInitializer.java   初始化
    │   │   ├── RootConfig.java          根容器（Service、DAO）
    │   │   └── WebConfig.java           Web 容器（Controller）
    │   ├── controller/
    │   │   └── UserController.java
    │   ├── service/
    │   │   └── UserService.java
    │   ├── entity/User.java
    │   └── interceptor/LoginInterceptor.java
    └── webapp/
        └── WEB-INF/views/
            ├── user-list.jsp
            └── user-form.jsp
```

### 4. WebAppInitializer

替代 `web.xml`，配置 DispatcherServlet 和 ContextLoaderListener。

```java
package com.example.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    /** 根容器：Service、DAO */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{RootConfig.class};
    }

    /** Web 容器：Controller、拦截器、视图解析器 */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    /** DispatcherServlet 映射路径 */
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}
```

> 💡 继承 `AbstractAnnotationConfigDispatcherServletInitializer` 后，Servlet 3.0+ 容器启动时会自动发现并执行。

### 5. RootConfig

```java
package com.example.config;

import org.springframework.context.annotation.*;

@Configuration
@ComponentScan(
        basePackages = "com.example",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = org.springframework.stereotype.Controller.class
        )
)
public class RootConfig {
    // Service、DAO 由这里扫描
}
```

### 6. WebConfig（核心）

```java
package com.example.config;

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

    /** 视图解析器：返回 "user-list" → /WEB-INF/views/user-list.jsp */
    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    /** 静态资源放行 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/");
    }

    /** 默认 Servlet 处理（可选） */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /** JSON 转换器（处理 LocalDateTime） */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                .indentOutput(true);
        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
    }

    /** 注册拦截器 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/static/**", "/api/public/**");
    }
}
```

---

## 三、控制器

### 1. `@Controller` vs `@RestController`

| 注解 | 说明 |
|------|------|
| `@Controller` | 返回视图名，配合 ViewResolver |
| `@RestController` | = `@Controller` + `@ResponseBody`，返回 JSON |

```java
// 返回 JSP 视图
@Controller
@RequestMapping("/users")
public class UserController {
    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user-list";  // → /WEB-INF/views/user-list.jsp
    }
}

// 返回 JSON
@RestController
@RequestMapping("/api/users")
public class UserApiController {
    @GetMapping
    public List<User> list() {
        return userService.findAll();  // 自动转 JSON
    }
}
```

### 2. `@RequestMapping` 系列

| 注解 | 等价于 |
|------|--------|
| `@GetMapping` | `@RequestMapping(method = GET)` |
| `@PostMapping` | `@RequestMapping(method = POST)` |
| `@PutMapping` | `@RequestMapping(method = PUT)` |
| `@DeleteMapping` | `@RequestMapping(method = DELETE)` |
| `@PatchMapping` | `@RequestMapping(method = PATCH)` |

**用法：**
```java
@RestController
@RequestMapping("/api/users")   // 类级别
public class UserApiController {

    @GetMapping                    // GET /api/users
    public List<User> list() { }

    @GetMapping("/{id}")           // GET /api/users/1
    public User detail(@PathVariable Long id) { }

    @PostMapping                   // POST /api/users
    public User create(@RequestBody User user) { }

    @PutMapping("/{id}")           // PUT /api/users/1
    public User update(@PathVariable Long id, @RequestBody User user) { }

    @DeleteMapping("/{id}")        // DELETE /api/users/1
    public void delete(@PathVariable Long id) { }
}
```

**`@RequestMapping` 属性：**

| 属性 | 说明 |
|------|------|
| value / path | URL 路径（支持数组） |
| method | HTTP 方法 |
| params | 限定请求参数 |
| headers | 限定请求头 |
| consumes | 请求 Content-Type |
| produces | 响应 Content-Type |

```java
@RequestMapping(
    value = "/search",
    method = RequestMethod.GET,
    params = "type=admin",
    produces = "application/json;charset=UTF-8"
)
```

### 3. 路径匹配规则

```java
@GetMapping("/users/{id}")           // 路径变量
@GetMapping("/users/*")              // 单层通配
@GetMapping("/users/**")             // 多层通配
@GetMapping("/users/{id:\\d+}")      // 正则：只匹配数字
@GetMapping({"/list", "/all"})       // 多个路径
```

---

## 四、参数绑定

### 1. `@RequestParam` 请求参数

```java
// GET /search?keyword=java&page=1
@GetMapping("/search")
public String search(
        @RequestParam("keyword") String keyword,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
    // ...
}
```

| 属性 | 说明 |
|------|------|
| value / name | 参数名 |
| required | 是否必填（默认 true） |
| defaultValue | 默认值 |

**多值参数：**
```java
// GET /ids?ids=1&ids=2&ids=3
@GetMapping("/ids")
public String byIds(@RequestParam("ids") List<Long> ids) { }
```

**Map 接收所有参数：**
```java
@GetMapping("/all")
public String all(@RequestParam Map<String, String> params) { }
```

### 2. `@PathVariable` 路径变量

```java
// GET /api/users/123
@GetMapping("/api/users/{id}")
public User detail(@PathVariable("id") Long userId) {
    return userService.findById(userId);
}

// 多个路径变量
// GET /api/users/1/articles/2
@GetMapping("/api/users/{userId}/articles/{articleId}")
public Article article(@PathVariable Long userId, @PathVariable Long articleId) { }
```

> 💡 路径变量名与方法参数名一致时，`@PathVariable` 可省略 value。

### 3. `@RequestBody` 请求体（JSON）

```java
// POST /api/users
// Body: {"username":"tom","age":25}
@PostMapping
public User create(@RequestBody User user) {
    userService.save(user);
    return user;
}
```

> 💡 需要 Jackson 依赖，Content-Type 为 `application/json`。

**接收 Map：**
```java
@PostMapping("/map")
public Map<String, Object> map(@RequestBody Map<String, Object> body) {
    return body;
}
```

### 4. `@ModelAttribute` 表单绑定

**表单提交（application/x-www-form-urlencoded）：**
```java
// POST /users  Body: username=tom&age=25
@PostMapping("/users")
public String create(User user) {   // 自动绑定到 User
    userService.save(user);
    return "redirect:/users";
}

// 显式
@PostMapping("/users2")
public String create2(@ModelAttribute("user") User user) { }
```

**在方法上使用（预处理）：**
```java
@ModelAttribute
public void init(Model model) {
    model.addAttribute("categories", categoryService.findAll());
}
```

### 5. `@RequestHeader` / `@CookieValue`

```java
@GetMapping("/info")
public String info(
        @RequestHeader("User-Agent") String userAgent,
        @RequestHeader(value = "X-Token", required = false) String token,
        @CookieValue(value = "JSESSIONID", required = false) String sessionId) {
    // ...
}
```

### 6. 类型转换

**自动转换：** String → int / long / boolean / Date / 枚举 等。

**日期格式：**
```java
@GetMapping("/date")
public String date(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) { }
```

**自定义转换器：**
```java
public class StringToUserConverter implements Converter<String, User> {
    @Override
    public User convert(String source) {
        User u = new User();
        u.setId(Long.parseLong(source));
        return u;
    }
}
```

**注册：**
```java
@Override
public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringToUserConverter());
}
```

### 7. 参数校验（JSR-303）

**依赖：**
```xml
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>8.0.1.Final</version>
</dependency>
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>jakarta.el</artifactId>
    <version>4.0.2</version>
</dependency>
```

**实体加注解：**
```java
public class User {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度 3-20")
    private String username;

    @Min(value = 1, message = "年龄最小 1")
    @Max(value = 150, message = "年龄最大 150")
    private Integer age;

    @Email(message = "邮箱格式错误")
    private String email;
}
```

**Controller 中使用：**
```java
@PostMapping
public Result<User> create(@RequestBody @Valid User user, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
        String msg = bindingResult.getFieldError().getDefaultMessage();
        return Result.error(msg);
    }
    userService.save(user);
    return Result.success(user);
}
```

> ⚠️ `BindingResult` 必须紧跟在 `@Valid` 参数后面，否则抛异常。

**常用校验注解：**

| 注解 | 说明 |
|------|------|
| `@NotNull` | 非 null |
| `@NotBlank` | 非空字符串（去空格） |
| `@NotEmpty` | 非空（集合/字符串） |
| `@Size(min, max)` | 长度范围 |
| `@Min` / `@Max` | 数值范围 |
| `@Email` | 邮箱格式 |
| `@Pattern` | 正则 |
| `@Past` / `@Future` | 日期范围 |

---

## 五、返回值处理

### 1. 返回视图名（`@Controller`）

```java
@Controller
@RequestMapping("/users")
public class UserController {

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user-list";  // → /WEB-INF/views/user-list.jsp
    }

    @PostMapping
    public String create(User user) {
        userService.save(user);
        return "redirect:/users";   // 重定向
    }

    @GetMapping("/forward")
    public String forward() {
        return "forward:/users";     // 转发
    }
}
```

**Model vs ModelMap vs ModelAndView：**

| 类型 | 说明 |
|------|------|
| `Model` | 接口，最常用 |
| `ModelMap` | 实现类，链式 |
| `ModelAndView` | 数据 + 视图 |

```java
// ModelAndView
@GetMapping("/mv")
public ModelAndView mv() {
    ModelAndView mav = new ModelAndView();
    mav.addObject("msg", "hello");
    mav.setViewName("result");
    return mav;
}
```

### 2. 返回 JSON（`@RestController` 或 `@ResponseBody`）

```java
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @GetMapping
    public List<User> list() {
        return userService.findAll();  // 自动转 JSON
    }

    @GetMapping("/{id}")
    public User detail(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

**`@Controller` + `@ResponseBody`：**
```java
@Controller
@RequestMapping("/api/users")
public class UserApiController {

    @GetMapping
    @ResponseBody
    public List<User> list() {
        return userService.findAll();
    }
}
```

### 3. `ResponseEntity`（控制状态码和头）

```java
@GetMapping("/{id}")
public ResponseEntity<User> detail(@PathVariable Long id) {
    User user = userService.findById(id);
    if (user == null) {
        return ResponseEntity.notFound().build();  // 404
    }
    return ResponseEntity.ok(user);  // 200 + body
}

@PostMapping
public ResponseEntity<User> create(@RequestBody User user) {
    userService.save(user);
    return ResponseEntity
            .status(HttpStatus.CREATED)   // 201
            .header("X-Custom", "value")
            .body(user);
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();  // 204
}
```

### 4. 统一返回结果

**Result 类：**
```java
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

**Controller：**
```java
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @GetMapping
    public Result<List<User>> list() {
        return Result.success(userService.findAll());
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.findById(id);
        return user != null ? Result.success(user) : Result.error(404, "用户不存在");
    }
}
```

---

## 六、拦截器（Interceptor）

### 1. 与 Filter 的区别

| 特性 | Filter | Interceptor |
|------|--------|-------------|
| 规范 | Servlet | Spring MVC |
| 作用范围 | 所有请求 | Controller 请求 |
| 能否获取 Spring Bean | ❌ | ✅ |
| 能否获取 Handler | ❌ | ✅ |
| 执行时机 | Servlet 前后 | Controller 前后 |
| 常用 | 编码、CORS | 登录、权限、日志 |

### 2. 定义拦截器

```java
package com.example.interceptor;

import jakarta.servlet.http.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class LoginInterceptor implements HandlerInterceptor {

    /** Controller 方法执行前 */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 放行静态资源
        String uri = request.getRequestURI();
        if (uri.startsWith("/static/")) return true;

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            // 未登录
            if (isAjax(request)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"未登录\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return false;  // 中断
        }
        return true;  // 放行
    }

    /** Controller 方法执行后，视图渲染前 */
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        // 可修改 ModelAndView
    }

    /** 视图渲染后 */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        // 资源清理、日志
    }

    private boolean isAjax(HttpServletRequest request) {
        String xhr = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equals(xhr)
                || (accept != null && accept.contains("application/json"));
    }
}
```

### 3. 注册拦截器

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login", "/logout",
                        "/static/**",
                        "/api/public/**"
                )
                .order(1);

        registry.addInterceptor(new LogInterceptor())
                .addPathPatterns("/api/**")
                .order(2);
    }
}
```

### 4. 拦截器执行顺序

```
请求 → Interceptor1.preHandle → Interceptor2.preHandle
     → Controller 方法
     → Interceptor2.postHandle → Interceptor1.postHandle
     → 视图渲染
     → Interceptor2.afterCompletion → Interceptor1.afterCompletion
```

---

## 七、全局异常处理

### 1. `@ExceptionHandler`（类内）

```java
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.error(500, "系统异常：" + e.getMessage());
    }
}
```

### 2. `@ControllerAdvice`（全局，推荐）

```java
package com.example.handler;

import com.example.common.Result;
import com.example.exception.BusinessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 参数校验异常 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, msg);
    }

    /** 兜底 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(500, "系统异常，请联系管理员");
    }
}
```

**`@ControllerAdvice` vs `@RestControllerAdvice`：**

| 注解 | 返回 |
|------|------|
| `@ControllerAdvice` | 视图或 JSON（配合 `@ResponseBody`） |
| `@RestControllerAdvice` | JSON（= `@ControllerAdvice` + `@ResponseBody`） |

---

## 八、RESTful API 实战

### 1. 需求

- `GET /api/users` 分页查询用户
- `GET /api/users/{id}` 查询单个
- `POST /api/users` 新增
- `PUT /api/users/{id}` 更新
- `DELETE /api/users/{id}` 删除
- 统一返回 `Result<T>`
- 全局异常处理
- 参数校验

### 2. Controller

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

    /** 分页列表 */
    @GetMapping
    public Result<PageResult<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.findByPage(keyword, status, page, size));
    }

    /** 详情 */
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) return Result.error(404, "用户不存在");
        return Result.success(user);
    }

    /** 新增 */
    @PostMapping
    public Result<User> create(@RequestBody @Valid User user) {
        userService.save(user);
        return Result.success(user);
    }

    /** 更新 */
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody @Valid User user) {
        user.setId(id);
        userService.update(user);
        return Result.success(user);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    /** 批量删除 */
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }
}
```

### 3. Service（用 Day11 的 Spring 管理）

```java
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<User> findByPage(String keyword, Integer status, int page, int size) {
        PageHelper.startPage(page, size);
        List<User> list = userMapper.search(keyword, status);
        PageInfo<User> info = new PageInfo<>(list);
        return PageResult.of(info);
    }

    // 其他方法...
}
```

### 4. 测试（Postman）

**GET 分页：**
```
GET http://localhost:8080/api/users?page=1&size=5&status=1
```

**响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {"id": 1, "username": "admin", "nickname": "管理员", "age": 30}
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 5,
    "pages": 1
  }
}
```

**POST 新增：**
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

---

## 九、用 Spring MVC 重构 Day7 项目

### 1. 对比

| 方面 | Day7 Servlet | Spring MVC |
|------|--------------|------------|
| 接收请求 | 继承 HttpServlet，重写 doGet/doPost | 注解方法 |
| URL 映射 | `@WebServlet("/users/*")` | `@RequestMapping` |
| 参数获取 | `req.getParameter()` | `@RequestParam` / `@PathVariable` |
| 参数解析 | 手动 parseInt | 自动转换 |
| 返回值 | 手动 forward / sendRedirect | 返回 String 视图名 |
| JSON | 手动写 JSON 字符串 | `@RestController` 自动 |
| 异常处理 | 每个 Servlet try-catch | `@ControllerAdvice` 统一 |
| 拦截 | Filter（无 Spring Bean） | Interceptor（可注入 Bean） |
| 对象管理 | 手动 new | Spring 注入 |

### 2. 重构后结构

```
spring-mvc-user/
├── config/
│   ├── WebAppInitializer
│   ├── RootConfig
│   └── WebConfig
├── controller/
│   ├── LoginController
│   ├── UserController       (JSP 视图)
│   └── UserApiController    (RESTful JSON)
├── service/
│   ├── UserService
│   └── impl/UserServiceImpl
├── dao/UserMapper
├── entity/User、PageResult
├── common/Result
├── exception/BusinessException
├── interceptor/LoginInterceptor
└── handler/GlobalExceptionHandler
```

### 3. 登录 Controller

```java
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

### 4. 用户 Controller（JSP 视图）

```java
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

### 5. JSP 视图（同 Day7，略）

视图层与 Day7 几乎一致，只需把链接改为 Spring MVC 的 URL。

---

## 十、常见问题与排错

| 问题 | 原因 | 解决 |
|------|------|------|
| 404 | URL 不匹配 | 检查 `@RequestMapping` 和上下文路径 |
| 405 | 方法不允许 | 检查 `@GetMapping` / `@PostMapping` |
| 400 | 参数绑定失败 | 检查参数类型、`@RequestParam` 必填 |
| 415 | Content-Type 不支持 | 加 `@RequestBody` + JSON 头 |
| 406 | 无法生成响应 | 检查 `produces` 和 Jackson 依赖 |
| 返回视图 404 | 视图解析器前缀错误 | 检查 `prefix` / `suffix` |
| JSON 中文乱码 | 编码问题 | `produces = "application/json;charset=UTF-8"` |
| `@RequestBody` 为 null | 缺 Jackson 或 Content-Type | 加依赖和请求头 |
| 拦截器不生效 | 未注册或路径不匹配 | 检查 `addInterceptors` |
| 静态资源 404 | DispatcherServlet 拦截 | `addResourceHandlers` 放行 |
| 循环依赖 | 构造器互相注入 | 改 Setter 或 `@Lazy` |
| 日期格式错误 | 未配 `@DateTimeFormat` | 加注解或全局配置 |

---

## 十一、实践练习

### 练习 1：搭建 Spring MVC
1. 用 Java 配置方式搭建（无 web.xml）
2. 编写 HelloController 返回 JSP
3. 编写 HelloApiController 返回 JSON
4. 用 Postman 测试

### 练习 2：参数绑定
1. `@RequestParam` 接收查询参数
2. `@PathVariable` 接收路径变量
3. `@RequestBody` 接收 JSON
4. `@ModelAttribute` 接收表单

### 练习 3：RESTful API
1. 实现用户 CRUD 接口
2. 统一返回 `Result<T>`
3. 用 `ResponseEntity` 控制状态码
4. Postman 测试各个接口

### 练习 4：拦截器
1. 编写登录拦截器
2. 未登录跳登录页（页面请求）
3. 未登录返回 401 JSON（API 请求）
4. 白名单放行静态资源

### 练习 5：全局异常
1. 定义 `BusinessException`
2. 用 `@RestControllerAdvice` 统一处理
3. 处理参数校验异常
4. 处理兜底异常

### 练习 6：参数校验
1. 引入 hibernate-validator
2. 实体加校验注解
3. Controller 用 `@Valid` + `BindingResult`
4. 全局异常处理校验失败

### 练习 7（进阶）：重构 Day7 项目
1. 用 Spring MVC 替代 Servlet
2. 登录用 `@Controller`
3. 用户管理用 `@Controller` + JSP
4. 增加 RESTful API 接口（`@RestController`）
5. 拦截器替代 Filter

---

## 十二、总结与拓展

### 今日总结
- **Spring MVC 架构**：DispatcherServlet 统一调度，HandlerMapping 找方法，HandlerAdapter 调用，ViewResolver 解析视图
- **配置方式**：`AbstractAnnotationConfigDispatcherServletInitializer`（无 web.xml）
- **核心注解**：`@Controller` / `@RestController` / `@RequestMapping` / `@GetMapping` 等
- **参数绑定**：`@RequestParam`、`@PathVariable`、`@RequestBody`、`@ModelAttribute`、`@RequestHeader`、`@CookieValue`
- **返回值**：String（视图）、对象（JSON）、`ResponseEntity`、`ModelAndView`
- **参数校验**：JSR-303 + `@Valid` + `BindingResult`
- **拦截器**：`HandlerInterceptor`（preHandle / postHandle / afterCompletion）
- **全局异常**：`@RestControllerAdvice` + `@ExceptionHandler`
- **RESTful API**：`GET/POST/PUT/DELETE` + 统一返回 + 全局异常

### 核心记忆点
```
配置：WebAppInitializer + RootConfig + WebConfig
控制器：@Controller（视图）/ @RestController（JSON）
映射：@GetMapping / @PostMapping / @PutMapping / @DeleteMapping
参数：@RequestParam / @PathVariable / @RequestBody / @ModelAttribute
返回：String（视图）/ 对象（JSON）/ ResponseEntity
校验：@Valid + BindingResult + JSR-303 注解
拦截：HandlerInterceptor + WebMvcConfigurer.addInterceptors
异常：@RestControllerAdvice + @ExceptionHandler
```

### 拓展阅读
- [Spring MVC 官方文档](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Spring MVC 请求流程](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html)
- [Jackson 注解](https://github.com/FasterXML/jackson-annotations/wiki/Jackson-Annotations)

### 明日预告
Day13 将学习 **Spring + MyBatis 整合（SSM）**：Spring 管理 SqlSessionFactory、Mapper 自动扫描、声明式事务、`@Transactional` 实战、SSM 项目结构，把 Day9-12 的知识串起来，为 Day14 博客系统做准备。