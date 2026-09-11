

## 学习目标

- 理解 HTTP 协议的本质：请求/响应模型、无状态特性
- 掌握 HTTP 请求方法（GET/POST/PUT/DELETE 等）及语义
- 掌握 HTTP 状态码分类与常见码含义
- 熟练理解 HTTP Header、Cookie、Session 机制
- 理解 RESTful API 设计思想与规范
- 掌握 JSON 数据格式
- 知道前端如何通过 Ajax / fetch 调用后端接口
- 会用 Postman 和浏览器开发者工具抓包分析请求

---

## 一、HTTP 协议概述

### 1. 什么是 HTTP

HTTP（HyperText Transfer Protocol，超文本传输协议）是**应用层**协议，用于客户端和服务器之间传输数据。它是 Web 的基石，几乎所有 Web 通信都基于 HTTP。

**核心特点：**

| 特点 | 说明 |
|------|------|
| 请求/响应模型 | 客户端发请求，服务器返回响应 |
| 无状态 | 服务器不保存客户端状态（需 Cookie/Session 补充） |
| 无连接 | HTTP/1.1 之前每次请求新建连接；1.1 支持 Keep-Alive |
| 明文传输 | HTTP 是明文，HTTPS 加密 |
| 灵活 | 可传输文本、图片、视频、JSON 等任意数据 |
| 简单 | 报文格式简单，易于理解和实现 |

### 2. HTTP 版本演进

| 版本 | 年份 | 特点 |
|------|------|------|
| HTTP/0.9 | 1991 | 只支持 GET，无 Header |
| HTTP/1.0 | 1996 | 支持 POST、Header、状态码，每次请求新建连接 |
| HTTP/1.1 | 1997 | Keep-Alive 长连接、管道化、Host 头，**至今广泛使用** |
| HTTP/2 | 2015 | 二进制分帧、多路复用、头部压缩、服务器推送 |
| HTTP/3 | 2022 | 基于 QUIC（UDP），更低延迟 |

> 💡 面试常问 HTTP/1.1 与 HTTP/2 的区别：HTTP/2 多路复用（一个连接并行多个请求）、头部压缩（HPACK）、二进制传输、服务器推送。

### 3. HTTP 与 HTTPS

| 特性 | HTTP | HTTPS |
|------|------|-------|
| 端口 | 80 | 443 |
| 加密 | 无 | TLS/SSL |
| 证书 | 无 | 需要 CA 证书 |
| 安全性 | 明文，可被窃听/篡改 | 加密，安全 |
| 性能 | 略快 | 略慢（握手开销） |

> 💡 现代网站几乎都用 HTTPS，浏览器对 HTTP 站点标记"不安全"。

---

## 二、HTTP 请求

### 1. 请求报文结构

```
请求行：方法 路径 协议版本\r\n
请求头：Header1: value1\r\n
        Header2: value2\r\n
        \r\n
请求体：数据（可选）
```

**完整示例：**
```http
POST /api/login HTTP/1.1
Host: example.com
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
Content-Type: application/json; charset=UTF-8
Content-Length: 45
Accept: application/json
Cookie: JSESSIONID=ABC123
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

{"username":"tom","password":"123456"}
```

**各部分说明：**
- **请求行**：`POST`（方法）+ `/api/login`（路径）+ `HTTP/1.1`（版本）
- **请求头**：键值对，描述请求元信息
- **空行**：`\r\n` 分隔头和体
- **请求体**：POST/PUT 等携带的数据（GET 一般无体）

### 2. HTTP 请求方法

| 方法 | 语义 | 幂等 | 安全 | 有请求体 | 典型用途 |
|------|------|------|------|----------|----------|
| **GET** | 获取资源 | ✅ | ✅ | ❌ | 查询、列表、详情 |
| **POST** | 创建资源 | ❌ | ❌ | ✅ | 新增、提交表单 |
| **PUT** | 完整更新资源 | ✅ | ❌ | ✅ | 覆盖更新 |
| **PATCH** | 部分更新资源 | ❌ | ❌ | ✅ | 局部更新 |
| **DELETE** | 删除资源 | ✅ | ❌ | ❌ | 删除 |
| **HEAD** | 只返回响应头 | ✅ | ✅ | ❌ | 检查资源是否存在 |
| **OPTIONS** | 查询支持的方法 | ✅ | ✅ | ❌ | CORS 预检 |
| **TRACE** | 回显请求 | ✅ | ✅ | ❌ | 调试（少用） |
| **CONNECT** | 建立隧道 | ❌ | ❌ | ❌ | HTTPS 代理 |

**幂等（Idempotent）：** 执行一次和执行多次效果相同。
**安全（Safe）：** 不改变服务器状态。

**GET vs POST 详解：**

| 维度 | GET | POST |
|------|-----|------|
| 参数位置 | URL 查询字符串 | 请求体 |
| 参数长度 | 受 URL 长度限制（约 2KB-8KB） | 无限制（服务器配置） |
| 参数可见性 | URL 中可见 | 请求体中，不可见 |
| 缓存 | 可缓存 | 默认不缓存 |
| 书签 | 可收藏 | 不可 |
| 历史记录 | 保留在浏览器历史 | 不保留 |
| 编码 | 只能 URL 编码 | 支持多种（表单、JSON、二进制） |
| 安全性 | 低（参数暴露） | 略高（但仍需 HTTPS） |
| 幂等 | ✅ | ❌ |
| 典型用途 | 查询 | 提交 |

> ⚠️ **常见误区**：
> - GET 不是"只能查询"，POST 也不是"只能提交"，只是语义约定。
> - GET 也可以有请求体，但规范不推荐，很多服务器/代理会丢弃。
> - POST 不是"安全的"，明文传输同样会泄露，必须 HTTPS。

### 3. 请求 URL 组成

```
https://api.example.com:443/api/users/123?name=tom&age=20#section

协议   主机名          端口  路径         查询字符串    片段
```

| 部分 | 说明 |
|------|------|
| 协议 | http / https |
| 主机名 | 域名或 IP |
| 端口 | 默认 80（HTTP）/ 443（HTTPS） |
| 路径 | 资源路径 `/api/users/123` |
| 查询字符串 | `?key1=value1&key2=value2` |
| 片段 | `#section`，仅客户端使用，不发送到服务器 |

**URL 编码：** 特殊字符需转义，如空格 → `%20`，中文 → UTF-8 编码。

```
原：https://example.com/search?q=你好 世界
编：https://example.com/search?q=%E4%BD%A0%E5%A5%BD%20%E4%B8%96%E7%95%8C
```

### 4. 常见请求头

| Header | 说明 | 示例 |
|--------|------|------|
| `Host` | 目标主机（HTTP/1.1 必需） | `example.com` |
| `User-Agent` | 客户端标识 | `Mozilla/5.0 ...` |
| `Accept` | 可接受的响应类型 | `application/json` |
| `Accept-Language` | 期望语言 | `zh-CN,zh;q=0.9` |
| `Accept-Encoding` | 期望压缩方式 | `gzip, deflate, br` |
| `Content-Type` | 请求体格式 | `application/json` |
| `Content-Length` | 请求体字节长度 | `45` |
| `Cookie` | 携带的 Cookie | `JSESSIONID=xxx` |
| `Authorization` | 认证信息 | `Bearer token` |
| `Referer` | 来源页面 | `https://google.com` |
| `Origin` | 跨域来源 | `https://example.com` |
| `If-Modified-Since` | 缓存校验 | 日期 |
| `X-Requested-With` | AJAX 标识 | `XMLHttpRequest` |

**常见 Content-Type：**

| 值 | 说明 |
|----|------|
| `application/x-www-form-urlencoded` | 表单默认，`a=1&b=2` |
| `multipart/form-data` | 文件上传 |
| `application/json` | JSON（现代 API 主流） |
| `text/plain` | 纯文本 |
| `application/xml` | XML |
| `application/octet-stream` | 二进制流 |

---

## 三、HTTP 响应

### 1. 响应报文结构

```
状态行：协议版本 状态码 状态描述\r\n
响应头：Header1: value1\r\n
        \r\n
响应体：数据
```

**完整示例：**
```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8
Content-Length: 68
Date: Mon, 11 Sep 2024 08:00:00 GMT
Server: nginx/1.24.0
Set-Cookie: JSESSIONID=XYZ789; Path=/; HttpOnly
Cache-Control: no-cache

{"code":200,"msg":"success","data":{"id":1,"name":"张三"}}
```

### 2. HTTP 状态码

**分类：**

| 范围 | 类别 | 含义 |
|------|------|------|
| 1xx | 信息提示 | 请求已接收，继续处理 |
| 2xx | 成功 | 请求成功处理 |
| 3xx | 重定向 | 需要进一步操作 |
| 4xx | 客户端错误 | 请求有问题 |
| 5xx | 服务器错误 | 服务器处理失败 |

**常见状态码详解：**

**1xx（信息）**
| 码 | 说明 |
|----|------|
| 100 Continue | 服务器已收到请求头，客户端可继续发送体 |
| 101 Switching Protocols | 切换协议（如 WebSocket） |

**2xx（成功）**
| 码 | 说明 | 典型场景 |
|----|------|----------|
| 200 OK | 请求成功 | GET 查询 |
| 201 Created | 资源已创建 | POST 新增 |
| 202 Accepted | 已接受，异步处理 | 提交任务 |
| 204 No Content | 成功但无响应体 | DELETE 删除 |
| 206 Partial Content | 部分内容 | 断点续传 |

**3xx（重定向）**
| 码 | 说明 | 典型场景 |
|----|------|----------|
| 301 Moved Permanently | 永久重定向 | 域名迁移，浏览器缓存 |
| 302 Found | 临时重定向 | 登录跳转 |
| 303 See Other | 用 GET 访问新地址 | PRG 模式 |
| 304 Not Modified | 资源未修改，用缓存 | 缓存优化 |
| 307 Temporary Redirect | 临时重定向（保持方法） | 类似 302 但保留方法 |
| 308 Permanent Redirect | 永久重定向（保持方法） | 类似 301 但保留方法 |

> 💡 **301 vs 302**：301 永久（浏览器会缓存，慎用），302 临时。
> 💡 **302 vs 307**：302 可能把 POST 改为 GET，307 保持原方法。

**4xx（客户端错误）**
| 码 | 说明 | 典型场景 |
|----|------|----------|
| 400 Bad Request | 请求语法错误 | 参数格式错误 |
| 401 Unauthorized | 未认证 | 未登录 |
| 403 Forbidden | 无权限 | 登录了但无权访问 |
| 404 Not Found | 资源不存在 | URL 错误 |
| 405 Method Not Allowed | 方法不允许 | 用 GET 访问只支持 POST 的接口 |
| 406 Not Acceptable | 无法生成客户端接受的内容 | Accept 不匹配 |
| 408 Request Timeout | 请求超时 | 客户端太慢 |
| 409 Conflict | 冲突 | 用户名重复 |
| 410 Gone | 资源已永久删除 | 已下架 |
| 413 Payload Too Large | 请求体过大 | 上传文件超限 |
| 415 Unsupported Media Type | 不支持的 Content-Type | 未用 JSON |
| 429 Too Many Requests | 请求过多 | 限流 |

**5xx（服务器错误）**
| 码 | 说明 | 典型场景 |
|----|------|----------|
| 500 Internal Server Error | 服务器内部错误 | 代码异常 |
| 501 Not Implemented | 未实现 | 不支持的方法 |
| 502 Bad Gateway | 网关错误 | Nginx 后端挂了 |
| 503 Service Unavailable | 服务不可用 | 维护、过载 |
| 504 Gateway Timeout | 网关超时 | 后端响应慢 |

> 💡 **401 vs 403**：401 是"你是谁"（未认证），403 是"你不配"（无权限）。
> 💡 **502 vs 504**：502 是后端连接失败，504 是后端连接成功但响应超时。

### 3. 常见响应头

| Header | 说明 |
|--------|------|
| `Content-Type` | 响应体类型 |
| `Content-Length` | 响应体长度 |
| `Content-Encoding` | 压缩方式（gzip） |
| `Set-Cookie` | 服务器设置 Cookie |
| `Cache-Control` | 缓存策略 |
| `Expires` | 过期时间 |
| `ETag` | 资源版本标识 |
| `Last-Modified` | 最后修改时间 |
| `Location` | 重定向目标 |
| `Access-Control-Allow-Origin` | CORS 允许来源 |
| `Server` | 服务器软件 |
| `Date` | 响应时间 |

**缓存相关头：**
```http
Cache-Control: max-age=3600, public
ETag: "abc123"
Last-Modified: Mon, 11 Sep 2024 08:00:00 GMT
```

**CORS 相关头：**
```http
Access-Control-Allow-Origin: https://example.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Allow-Credentials: true
```

---

## 四、Cookie 与 Session

### 1. 为什么需要 Cookie/Session

HTTP 是**无状态**协议，服务器默认不记得你是谁。为了在多次请求间保持状态（如登录），引入了 Cookie 和 Session。

### 2. Cookie

**定义：** 服务器通过 `Set-Cookie` 响应头下发给客户端的一小段文本，浏览器保存后，每次请求自动在 `Cookie` 请求头中携带。

**工作流程：**
```
1. 客户端请求登录
2. 服务器验证通过，响应 Set-Cookie: JSESSIONID=abc123; Path=/; HttpOnly
3. 浏览器保存 Cookie
4. 后续请求自动携带 Cookie: JSESSIONID=abc123
5. 服务器根据 Cookie 识别用户
```

**Cookie 属性：**

| 属性 | 说明 |
|------|------|
| `name=value` | 键值对 |
| `Domain` | 作用域名 |
| `Path` | 作用路径 |
| `Expires` / `Max-Age` | 过期时间 |
| `HttpOnly` | 禁止 JS 访问（防 XSS） |
| `Secure` | 仅 HTTPS 传输 |
| `SameSite` | 跨站策略（Strict/Lax/None，防 CSRF） |

**示例：**
```http
Set-Cookie: username=tom; Max-Age=604800; Path=/; HttpOnly; Secure; SameSite=Lax
```

**优点：**
- 客户端存储，服务器无压力
- 可长期保存（设置 Max-Age）

**缺点：**
- 大小限制（约 4KB）
- 数量限制（每域名 20-50 个）
- 明文存储，不安全
- 每次请求都携带，浪费带宽
- 用户可禁用

### 3. Session

**定义：** 服务器端为每个用户创建的会话对象，数据存服务器内存，通过 Cookie 中的 `JSESSIONID` 识别。

**工作流程：**
```
1. 客户端首次访问，服务器创建 Session，生成唯一 JSESSIONID
2. 响应 Set-Cookie: JSESSIONID=abc123
3. 浏览器保存 Cookie
4. 后续请求携带 Cookie，服务器根据 JSESSIONID 找到 Session
5. Session 中保存用户状态（如 loginUser）
6. 超时或注销后 Session 销毁
```

**Session 生命周期：**
- 创建：首次 `getSession()` 时
- 存活：默认 30 分钟（可配置），每次请求刷新超时时间
- 销毁：`invalidate()`、超时、服务器重启（非持久化）

**优点：**
- 数据在服务器，安全
- 无大小限制（受内存）
- 可存复杂对象

**缺点：**
- 服务器内存压力
- 分布式环境需 Session 共享（Redis）
- 依赖 Cookie（禁用则需 URL 重写）

### 4. Cookie vs Session 对比

| 特性 | Cookie | Session |
|------|--------|---------|
| 存储位置 | 客户端 | 服务器 |
| 安全性 | 低 | 高 |
| 容量 | ≤4KB | 受内存限制 |
| 生命周期 | 可长期 | 会话级/超时 |
| 服务器压力 | 无 | 有 |
| 跨域 | 受限 | 依赖 Cookie |
| 分布式 | 天然支持 | 需共享（Redis） |
| 适用 | 偏好、记住我 | 登录状态 |

### 5. 分布式 Session 方案

**问题：** 多台服务器，Session 不共享，用户请求到不同服务器会掉登录。

**方案：**
1. **Session 粘滞**：Nginx ip_hash，同一 IP 固定到一台（不推荐）
2. **Session 复制**：Tomcat 集群间同步（性能差）
3. **Session 集中存储**：Redis 存 Session（推荐）
4. **JWT**：无状态 Token，服务器不存 Session（现代主流）

### 6. Cookie 与 Session 的安全问题

| 问题 | 说明 | 防护 |
|------|------|------|
| XSS | 窃取 Cookie | HttpOnly、输入转义 |
| CSRF | 伪造请求 | SameSite、CSRF Token |
| 会话固定 | 攻击者预设 Session ID | 登录后更换 Session ID |
| 会话劫持 | 窃取 Session ID | HTTPS、定期更换 |

---

## 五、RESTful API

### 1. 什么是 REST

REST（Representational State Transfer，表现层状态转移）是一种**架构风格**，由 Roy Fielding 在 2000 年博士论文中提出。符合 REST 原则的 API 称为 RESTful API。

**核心原则：**

| 原则 | 说明 |
|------|------|
| 资源（Resource） | 一切皆资源，用 URI 标识 |
| 表现层（Representation） | 资源的表示（JSON/XML/HTML） |
| 状态转移 | 通过 HTTP 方法操作资源 |
| 无状态（Stateless） | 每个请求包含全部信息 |
| 统一接口 | 统一的 URL 和方法语义 |
| 分层系统 | 客户端不关心是否直连服务器 |

### 2. RESTful API 设计规范

**URL 设计：**
```
✅ 正确：
GET    /users              # 用户列表
POST   /users              # 创建用户
GET    /users/1            # 用户详情
PUT    /users/1            # 完整更新
PATCH  /users/1            # 部分更新
DELETE /users/1            # 删除
GET    /users/1/articles   # 用户的文章列表

❌ 错误：
GET    /getUsers           # 动词
POST   /createUser         # 动词
GET    /user/list          # 动词
GET    /users/delete/1     # 动词
GET    /get_user_by_id     # 下划线+动词
```

**规范要点：**
- 用**名词**（复数）表示资源，不用动词
- 用 HTTP 方法表示操作
- 用**路径变量**表示资源 ID
- 用小写字母，多个单词用 `-`（短横线）或 `/`（层级）
- 避免深层嵌套（一般不超过 2 层）

**状态码使用：**
```
GET    /users          → 200 OK
POST   /users          → 201 Created
GET    /users/1        → 200 / 404
PUT    /users/1        → 200 / 204
DELETE /users/1        → 204 No Content
参数错误               → 400 Bad Request
未登录                 → 401 Unauthorized
无权限                 → 403 Forbidden
```

**版本控制：**
```
方式一：URL 版本
https://api.example.com/v1/users
https://api.example.com/v2/users

方式二：Header 版本
Accept: application/vnd.example.v1+json

方式三：参数版本
https://api.example.com/users?version=1
```

**过滤、排序、分页：**
```
GET /users?status=1&role=admin         # 过滤
GET /users?sort=age,desc               # 排序
GET /users?page=1&size=10              # 分页
GET /users?fields=id,name,email        # 字段选择
```

### 3. RESTful 示例对比

**非 RESTful（传统）：**
```
GET  /getUserList
POST /addUser
POST /updateUser
POST /deleteUser?id=1
```

**RESTful：**
```
GET    /users
POST   /users
PUT    /users/1
DELETE /users/1
```

---

## 六、JSON 数据格式

### 1. 什么是 JSON

JSON（JavaScript Object Notation）是一种**轻量级数据交换格式**，易于人阅读、机器解析。现代 API 几乎都采用 JSON。

**特点：**
- 纯文本，跨语言
- 结构清晰
- 体积小
- 解析快

### 2. JSON 语法

**数据类型：**

| 类型 | 示例 |
|------|------|
| 字符串 | `"hello"`（**必须双引号**） |
| 数字 | `123`、`3.14`、`-10`、`1e5` |
| 布尔 | `true`、`false` |
| null | `null` |
| 对象 | `{"key": value}` |
| 数组 | `[value1, value2]` |

**基本规则：**
- 键必须用**双引号**
- 值可以是上述任意类型
- 键值对用 `:` 分隔，多个用 `,`
- 最后一个成员后**不能有逗号**
- 不支持注释
- 不支持函数、undefined

### 3. JSON 示例

**简单对象：**
```json
{
  "id": 1,
  "name": "张三",
  "age": 25,
  "isActive": true,
  "email": null
}
```

**嵌套对象：**
```json
{
  "id": 1,
  "name": "张三",
  "address": {
    "province": "北京",
    "city": "北京",
    "district": "朝阳区"
  }
}
```

**数组：**
```json
{
  "roles": ["admin", "user"],
  "scores": [90, 85, 88]
}
```

**对象数组：**
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {"id": 1, "name": "张三"},
    {"id": 2, "name": "李四"}
  ]
}
```

**分页响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {"id": 1, "name": "张三"},
      {"id": 2, "name": "李四"}
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  }
}
```

### 4. JSON 与 Java 的对应

| JSON | Java |
|------|------|
| `{}` | `Map` / POJO |
| `[]` | `List` / 数组 |
| `"str"` | `String` |
| `123` | `Integer` / `Long` |
| `3.14` | `Double` / `BigDecimal` |
| `true` | `Boolean` |
| `null` | `null` |

**Jackson 常用注解：**
```java
public class User {
    @JsonProperty("user_name")
    private String username;          // JSON: user_name

    @JsonIgnore
    private String password;          // 不参与序列化

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;             // null 时不输出
}
```

### 5. JSON vs XML

| 特性 | JSON | XML |
|------|------|-----|
| 体积 | 小 | 大 |
| 可读性 | 好 | 一般 |
| 解析 | 简单 | 复杂 |
| 数据类型 | 支持 | 都是字符串 |
| 命名空间 | 无 | 有 |
| 注释 | 不支持 | 支持 |
| 现代主流 | ✅ | 逐渐淘汰 |

---

## 七、前端如何调用后端接口

### 1. XMLHttpRequest（传统 Ajax）

```javascript
// GET
const xhr = new XMLHttpRequest();
xhr.open('GET', 'https://api.github.com/users/octocat', true);
xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
        if (xhr.status === 200) {
            const data = JSON.parse(xhr.responseText);
            console.log(data);
        } else {
            console.error('请求失败：', xhr.status);
        }
    }
};
xhr.send();

// POST
const xhr2 = new XMLHttpRequest();
xhr2.open('POST', '/api/login', true);
xhr2.setRequestHeader('Content-Type', 'application/json');
xhr2.onload = () => console.log(xhr2.responseText);
xhr2.send(JSON.stringify({username: 'tom', password: '123456'}));
```

### 2. fetch（现代推荐）

```javascript
// GET
fetch('https://api.github.com/users/octocat')
    .then(response => {
        if (!response.ok) throw new Error('HTTP ' + response.status);
        return response.json();
    })
    .then(data => console.log(data))
    .catch(err => console.error(err));

// POST
fetch('/api/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({username: 'tom', password: '123456'})
})
    .then(res => res.json())
    .then(data => console.log(data));
```

**async/await 版本：**
```javascript
async function login() {
    try {
        const res = await fetch('/api/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username: 'tom', password: '123456'})
        });
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const data = await res.json();
        console.log(data);
    } catch (err) {
        console.error(err);
    }
}
```

### 3. axios（第三方库，最流行）

```javascript
// 引入：<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

// GET
axios.get('/api/users', {params: {page: 1, size: 10}})
    .then(res => console.log(res.data))
    .catch(err => console.error(err));

// POST
axios.post('/api/users', {username: 'tom', age: 25})
    .then(res => console.log(res.data));

// 拦截器（统一处理 Token、错误）
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = 'Bearer ' + token;
    return config;
});

axios.interceptors.response.use(
    res => res.data,
    err => {
        if (err.response?.status === 401) location.href = '/login';
        return Promise.reject(err);
    }
);
```

### 4. 三者对比

| 特性 | XMLHttpRequest | fetch | axios |
|------|----------------|-------|-------|
| 原生 | ✅ | ✅ | ❌（第三方） |
| Promise | ❌ | ✅ | ✅ |
| 拦截器 | ❌ | ❌ | ✅ |
| 自动 JSON | ❌ | ❌ | ✅ |
| 超时 | 手动 | 手动 | ✅ |
| 取消请求 | ✅ | AbortController | ✅ |
| 浏览器兼容 | 好 | 好（旧版需 polyfill） | 好 |
| 推荐度 | 低 | 中 | 高 |

### 5. 跨域问题（CORS）

**什么是跨域：** 浏览器同源策略限制，协议、域名、端口任一不同即为跨域。

```
当前页面：https://example.com
请求地址：https://api.example.com   → 跨域（域名不同）
请求地址：http://example.com        → 跨域（协议不同）
请求地址：https://example.com:8080  → 跨域（端口不同）
请求地址：https://example.com/api   → 同源
```

**解决方式：**

**方式一：服务端设置 CORS 头**
```java
response.setHeader("Access-Control-Allow-Origin", "https://example.com");
response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
response.setHeader("Access-Control-Allow-Credentials", "true");
```

**方式二：Nginx 反向代理**
```nginx
location /api/ {
    proxy_pass http://backend:8080/;
}
```

**方式三：开发环境代理（Vue/React）**
```javascript
// vite.config.js
export default {
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    }
}
```

**预检请求（OPTIONS）：** 复杂请求（如带自定义头、PUT/DELETE）会先发 OPTIONS 预检，服务器需正确响应。

---

## 八、实战练习：用 Postman 和浏览器抓包

### 练习 1：用 Postman 请求 GitHub API

**步骤：**
1. 下载 [Postman](https://www.postman.com/downloads/) 或使用网页版
2. 新建请求，方法选 `GET`
3. URL 输入 `https://api.github.com/users/octocat`
4. 点击 Send

**观察：**
- **状态码**：200 OK
- **响应头**：`Content-Type: application/json; charset=utf-8`、`X-RateLimit-Limit` 等
- **响应体**：JSON 对象，含 `login`、`id`、`avatar_url`、`followers` 等

**尝试其他请求：**

| 请求 | 说明 |
|------|------|
| `GET https://api.github.com/users/octocat/repos` | 用户仓库列表（数组） |
| `GET https://api.github.com/repos/octocat/Hello-World` | 仓库详情 |
| `GET https://api.github.com/search/repositories?q=java` | 搜索 Java 仓库 |
| `GET https://api.github.com/users/not_exist_xxx` | 观察 404 |
| `GET https://api.github.com/rate_limit` | 查看速率限制 |

**分析响应头：**
```
X-RateLimit-Limit: 60              未认证每小时 60 次
X-RateLimit-Remaining: 55          剩余次数
X-RateLimit-Reset: 1694419200      重置时间（Unix 时间戳）
```

### 练习 2：用浏览器开发者工具抓包

**步骤：**
1. 打开 Chrome，访问 `https://api.github.com/users/octocat`
2. 按 `F12` 打开开发者工具
3. 切换到 **Network** 标签
4. 刷新页面，看到请求列表

**查看：**
- **Headers**：
  - General：Request URL、Method、Status Code
  - Response Headers：Content-Type、Cache-Control 等
  - Request Headers：User-Agent、Accept 等
- **Preview**：JSON 格式化预览
- **Response**：原始响应文本
- **Timing**：各阶段耗时（DNS、TCP、TTFB）

**在 Console 中执行 fetch：**
```javascript
fetch('https://api.github.com/users/octocat')
    .then(res => {
        console.log('状态码：', res.status);
        console.log('Content-Type：', res.headers.get('Content-Type'));
        return res.json();
    })
    .then(data => console.log(data))
    .catch(err => console.error(err));
```

### 练习 3：模拟一个完整的登录流程

**目标：** 理解 Cookie 如何在请求间传递。

**步骤：**
1. 用浏览器访问一个带登录的网站（如 GitHub 登录页）
2. 打开开发者工具 Network
3. 登录时观察：
   - POST 请求携带表单数据
   - 响应 `Set-Cookie` 下发 Session ID
   - 后续请求自动携带 `Cookie`
4. 在 Application → Cookies 中查看所有 Cookie

**思考：**
- 为什么刷新页面不会掉登录？（Cookie 携带 JSESSIONID）
- 清除 Cookie 后会怎样？（登录失效）
- 关闭浏览器再打开呢？（取决于 Cookie 是否持久化）

### 练习 4：分析状态码

用 Postman 访问以下 URL，记录状态码和含义：

| URL | 预期状态码 |
|-----|-----------|
| `https://api.github.com/users/octocat` | 200 |
| `https://api.github.com/users/not_exist_xyz` | 404 |
| `https://api.github.com/user`（未认证） | 401 |
| `https://httpbin.org/status/500` | 500 |
| `https://httpbin.org/status/301` | 301 |
| `https://httpbin.org/delay/10` | 可能超时 |

### 练习 5：理解方法语义

用 `https://httpbin.org` 测试各种方法：

| 请求 | 说明 |
|------|------|
| `GET https://httpbin.org/get?name=tom` | 观察 query 参数回显 |
| `POST https://httpbin.org/post`（Body JSON） | 观察 body 回显 |
| `PUT https://httpbin.org/put` | 观察方法 |
| `DELETE https://httpbin.org/delete` | 观察方法 |
| `GET https://httpbin.org/headers` | 观察请求头 |
| `GET https://httpbin.org/cookies` | 观察 Cookie |
| `GET https://httpbin.org/status/418` | 彩蛋状态码（I'm a teapot） |

---

## 九、常见问题与排错

| 问题 | 原因 | 解决 |
|------|------|------|
| 404 Not Found | URL 错误或资源不存在 | 检查 URL、路径拼写 |
| 401 Unauthorized | 未认证 | 加 Token 或登录 |
| 403 Forbidden | 无权限 | 检查权限 |
| 405 Method Not Allowed | 方法不匹配 | 换 GET/POST 等 |
| 415 Unsupported Media Type | Content-Type 错误 | 加 `Content-Type: application/json` |
| CORS 错误 | 跨域未放行 | 服务端加 CORS 头 |
| 请求超时 | 网络慢或服务器慢 | 加超时、检查网络 |
| 中文乱码 | 编码不一致 | 统一 UTF-8 |
| Cookie 不生效 | Domain/Path 不匹配 | 检查 Cookie 属性 |
| Session 丢失 | 未携带 Cookie 或超时 | 检查 Cookie、延长超时 |
| POST 参数收不到 | Content-Type 不对 | 表单用 `x-www-form-urlencoded`，JSON 用 `application/json` |
| 重定向循环 | Location 指向自身 | 检查重定向逻辑 |

---

## 十、总结与拓展

### 今日总结

- **HTTP 是请求/响应协议**，无状态，通过 Cookie/Session 补充状态
- **请求方法**：GET 查询、POST 创建、PUT 更新、DELETE 删除，注意幂等和安全
- **状态码**：2xx 成功、3xx 重定向、4xx 客户端错误、5xx 服务器错误
- **Header**：请求头（Accept、Content-Type、Cookie、Authorization）和响应头（Set-Cookie、Cache-Control）
- **Cookie**：客户端存储，自动携带，有大小限制，HttpOnly/Secure 增强安全
- **Session**：服务器端存储，通过 JSESSIONID 识别，分布式需共享
- **RESTful**：资源 + HTTP 方法 + 状态码，URL 用名词不用动词
- **JSON**：轻量级数据格式，双引号，支持对象/数组/基本类型
- **前端调用**：XHR → fetch → axios，现代推荐 axios
- **跨域**：同源策略限制，用 CORS 或代理解决

### 核心记忆点

```
请求：方法 URL 版本 + Header + Body
响应：版本 状态码 描述 + Header + Body
方法：GET 查 / POST 增 / PUT 改 / DELETE 删
状态码：200 成功 / 201 创建 / 204 无内容
        301 永久 / 302 临时 / 304 未修改
        400 参数错 / 401 未认证 / 403 无权限 / 404 不存在
        500 服务器错 / 502 网关错 / 503 不可用
Cookie：客户端，Set-Cookie 下发，自动携带，4KB
Session：服务器，JSESSIONID 识别，可存对象
RESTful：名词 + 复数 + HTTP 方法
JSON：双引号，对象 {}，数组 []
跨域：协议/域名/端口任一不同，CORS 解决
```

### 关键对比

| 对比 | 结论 |
|------|------|
| GET vs POST | GET 查询可缓存，POST 提交不可缓存 |
| 401 vs 403 | 401 未认证，403 无权限 |
| 301 vs 302 | 301 永久，302 临时 |
| 502 vs 504 | 502 后端连不上，504 后端超时 |
| Cookie vs Session | Cookie 客户端，Session 服务器 |
| JSON vs XML | JSON 轻量，XML 臃肿 |

### 拓展阅读

- [MDN HTTP 文档](https://developer.mozilla.org/zh-CN/docs/Web/HTTP)
- [RFC 7231（HTTP/1.1 语义）](https://datatracker.ietf.org/doc/html/rfc7231)
- [RESTful API 设计指南](https://restfulapi.net/)
- [JSON 官方介绍](https://www.json.org/json-zh.html)
- [HTTP 状态码大全](https://httpstatuses.com/)
- [GitHub REST API 文档](https://docs.github.com/rest)
- [《图解 HTTP》](https://book.douban.com/subject/25863515/)

### 明日预告

Day2 将学习 **Java Web 环境搭建与 Servlet 入门**：
- 搭建 JDK + Maven + Tomcat + IDEA 环境
- 理解 Servlet 生命周期
- 编写第一个 Servlet
- 掌握 `HttpServletRequest` 和 `HttpServletResponse`
- 把今天的 HTTP 知识落地到 Java 代码中

---

## 附录：常用工具与命令

### 1. curl 命令

```bash
# GET
curl https://api.github.com/users/octocat

# 带 Header
curl -H "Accept: application/json" https://api.github.com/users/octocat

# POST JSON
curl -X POST https://httpbin.org/post \
     -H "Content-Type: application/json" \
     -d '{"username":"tom","password":"123456"}'

# 查看响应头
curl -i https://api.github.com/users/octocat

# 只显示响应头
curl -I https://api.github.com/users/octocat

# 保存 Cookie
curl -c cookies.txt https://example.com/login -d "username=tom&password=123"

# 携带 Cookie
curl -b cookies.txt https://example.com/profile

# 跟随重定向
curl -L https://httpbin.org/redirect/3

# 显示详细过程
curl -v https://api.github.com/users/octocat
```

### 2. 浏览器快捷键

| 操作 | Chrome | Firefox |
|------|--------|---------|
| 开发者工具 | F12 / Ctrl+Shift+I | F12 |
| Network | Ctrl+Shift+I → Network | 同 |
| Console | Ctrl+Shift+J | 同 |
| 强制刷新 | Ctrl+Shift+R | 同 |
| 查看源码 | Ctrl+U | 同 |

### 3. Postman 技巧

- **环境变量**：`{{baseUrl}}/api/users`
- **集合**：把相关请求组织成 Collection
- **测试脚本**：
```javascript
pm.test("状态码为 200", () => pm.response.to.have.status(200));
pm.test("返回 JSON", () => pm.response.to.be.json);
pm.test("包含 data 字段", () => {
    const json = pm.response.json();
    pm.expect(json).to.have.property('data');
});
```
- **环境切换**：开发/测试/生产
- **导入导出**：分享集合

### 4. 在线测试站点

| 站点 | 用途 |
|------|------|
| [httpbin.org](https://httpbin.org/) | HTTP 请求回显 |
| [jsonplaceholder.typicode.com](https://jsonplaceholder.typicode.com/) | 模拟 RESTful API |
| [api.github.com](https://api.github.com/) | 真实 API 练习 |
| [reqres.in](https://reqres.in/) | 模拟用户 API |
| [webhook.site](https://webhook.site/) | 接收请求测试 |
| [httpstatuses.com](https://httpstatuses.com/) | 状态码查询 |

---

**Day1 完 · 建议动手完成所有练习，用 Postman 和浏览器把 HTTP 的每个概念都验证一遍，为后续 Java Web 开发打好基础。**