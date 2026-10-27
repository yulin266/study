# JavaWeb 14 天笔记 · 精简版

> 把 14 天的原笔记压缩成"每天一页纸"：先讲清这天的**一句话定位**，再列**核心要点**（保留专业术语），每条都配上**生活比喻或例子**帮助记忆。
> 全程用一个"开餐厅"的大比喻串起来：**Web 应用 = 一家餐厅，浏览器 = 顾客，服务器 = 后厨，数据库 = 食材仓库。**

---

## 0. 基本概念（热身篇）

**一句话定位**：搞清楚"动态网站"到底是怎么动起来的。

- **静态 Web**：HTML 写死，谁看都一样。→ 像**打印好的纸质菜单**，内容固定。
- **动态 Web**：根据请求实时生成内容。→ 像**厨师根据客人点菜现做**，同一样菜每桌口味可以不同。
- **C/S 与 B/S**：客户端/服务器 vs 浏览器/服务器。现在主流是 B/S——顾客只需浏览器，不用专门装"点菜 App"。

---

## Day 1｜HTTP 与 Web 基础

**一句话定位**：HTTP 是浏览器（顾客）和服务器（后厨）之间的**点菜暗号**。

- **HTTP 请求**：请求行（方法 + URL + 版本）、请求头、请求体。
  - **GET**：参数写在 URL 上，用来"看菜单"（查询）。像**直接喊菜名**，所有人都听得见，明文。
  - **POST**：参数写在请求体里，用来"提交订单"（新增/修改）。像**填纸质点单**，内容更隐蔽、容量更大。
- **状态码**（后厨回话的语气）：
  - `2xx` 成功 → "菜上好了"（200）
  - `3xx` 跳转 → "请去隔壁窗口"（302 重定向）
  - `4xx` 客户端错 → "您点的这道菜没有"（404）、"没资格点"（403）
  - `5xx` 服务器错 → "后厨炸了"（500）
- **Cookie vs Session**（记住客人的两种方式）：
  - Cookie：存在**浏览器**里，像**手腕上的手环**，每次进店自动亮一下。
  - Session：存在**服务器**上，像**餐厅的会员档案**，手环（sessionId）只负责"对号"。
- **RESTful**：一套"菜单编号规范"——用 URL 表示资源（`/users`），用方法表示动作（GET 查、POST 增、PUT 改、DELETE 删）。
- **JSON**：前后端通用的"标准菜谱格式"，`{"菜名":"鱼香肉丝"}` 谁都看得懂。

**记忆口诀**：GET 喊、POST 递；2 上菜、4 你错、5 我炸；Cookie 在顾客兜里，Session 在店档案里。

---

## Day 2｜环境搭建 + Servlet 入门

**一句话定位**：把"厨房设备"装好（JDK/Maven/Tomcat/IDEA），认识第一个核心角色 **Servlet（大厨）**。

- **环境四件套**：
  - **JDK**：Java 的"锅灶"（运行 + 编译环境）。
  - **Maven**：自动采购员（管依赖 jar、管构建打包）。
  - **Tomcat**：**Servlet 容器**，负责接收 HTTP 请求并调度 Servlet，相当于**餐厅大堂**——顾客（请求）先到它这，再由它安排给大厨。
  - **IDEA**：你的"料理台"（IDE）。
- **Servlet 是什么**：运行在服务器上的 Java 小程序，专门处理请求、返回响应。**大厨角色**：接单（请求）→ 炒菜（业务逻辑）→ 上菜（响应）。
- **生命周期**（大厨的一天）：
  1. `init()`：上岗培训（只做一次，Servlet 单例）。
  2. `service()`：接单炒菜（每次请求调用，内部按方法分给 `doGet/doPost`）。
  3. `destroy()`：下班关火（服务器关闭时）。
- **HttpServletRequest / HttpServletResponse**：大厨的"接单小票"和"上菜托盘"——从小票读参数（`req.getParameter`），往托盘写内容（`resp.getWriter().write`）。
- **注册 Servlet**：用 `@WebServlet("/路径")` 注解，像**给每个窗口挂招牌**。
- **中文乱码**：`req.setCharacterEncoding("UTF-8")` + `resp.setContentType("text/html;charset=UTF-8")`——进门先定好"通用语言"。

**记忆口诀**：Tomcat 是大堂、Servlet 是大厨、请求是小票、响应是托盘；init 一次、service 每单、destroy 关店。

---

## Day 3｜Servlet 进阶：转发、重定向、Cookie、Session、Filter、Listener

**一句话定位**：学会"内部交接、给客人指路、记住客人、门口安检、店里监控"五种后厨管理手段。

- **转发 vs 重定向**（超高频面试点）：
  | | 转发 forward | 重定向 redirect |
  |--|--|--|
  | 谁执行 | 服务器**内部**把请求转给另一个 Servlet | 服务器告诉浏览器"去新地址" |
  | 地址栏 | **不变** | **变** |
  | 请求次数 | 1 次 | 2 次 |
  | 比喻 | 大堂经理把单子**内部递给**另一个窗口 | 服务员说"**请到隔壁窗口**"，客人自己走过去 |
  | 传数据 | 能带 request 域数据 | 不能，需手动拼 URL |
- **Cookie**：服务器发给浏览器的小数据，存在浏览器端。像**超市积分卡**——"下次来记得打折"。
- **Session**：服务器端保存用户状态（登录状态、购物车），靠 Cookie 里的 sessionId 识别。像**超市会员档案**——卡丢了没关系，报手机号（sessionId）也能调出档案。默认 30 分钟过期，像**会员卡过期作废**。
- **Filter（过滤器）**：在请求到达 Servlet **之前**和响应返回**之后**执行统一逻辑（登录校验、乱码处理）。像**进店安检门**——不管去哪个窗口，先过安检。链式执行，像**多道安检**。
- **Listener（监听器）**：监听应用事件（启动/关闭、Session 创建销毁、属性变化）。像**店里的计数器**——有人进店就 +1（在线人数统计），关门就重置。

**记忆口诀**：转发内部换手、重定向指路重走；Cookie 在顾客兜里、Session 在店档案里；Filter 管进门、Listener 管数人头。

---

## Day 4｜JSP + EL + JSTL

**一句话定位**：让"HTML 菜单页"会说话——在页面里动态展示数据。

- **JSP**：在 HTML 里嵌 Java 的动态页面。本质是**一个会变成 Servlet 的模板**，Tomcat 第一次访问时把 JSP 翻译成 Servlet 再执行。像**印好版式的菜谱**，菜名位置留空，上菜前由后厨（Java）现场填。
- **JSP 组成**：
  - 指令 `<%@ page %>`：页面设置（语言、编码）。
  - 脚本 `<% %>`：写 Java 代码（老式写法，**现在不推荐**在页面里写太多 Java）。
  - 表达式 `<%= %>`：直接输出。
  - 内置对象：`request`、`response`、`session`、`application` 等 9 个，不用 new 直接用。
- **EL 表达式 `${ }`**：从作用域取值的"快捷口语"。`${user.name}` 等价于 `request.getAttribute("user").getName()`。像**扫码看价**——不用翻说明书，一扫就出结果。取不到输出空字符串，不报错。
- **JSTL 标签库**：JSP 的"标准积木"——`<c:if>` 判断、`<c:forEach>` 循环、`<c:choose>` 多分支。像**菜单模板里的循环打印**：`<c:forEach items="${userList}" var="u">` 自动把每个用户打一行。
- **MVC 思想萌芽**：JSP 只做**视图**（展示），Servlet 做**控制**（调度），JavaBean 做**模型**（数据）。别把 SQL 和业务逻辑写进 JSP——那是"把后厨搬进餐厅大堂"。

**记忆口诀**：JSP 是模板、EL 是扫码、JSTL 是积木；页面只展示、Servlet 只调度。

---

## Day 5｜JDBC 基础

**一句话定位**：Java 程序连接数据库的"官方通道"，自己动手的第一版数据存取。

- **JDBC 六步曲**（取货流程）：
  1. 加载驱动 `Class.forName(...)` → 确认"用哪家的货车"。
  2. `DriverManager.getConnection(url, user, pwd)` → **连上仓库**。
  3. 创建 `Statement / PreparedStatement` → 填取货单。
  4. 执行 SQL（`executeQuery` 查 / `executeUpdate` 增删改）→ 取货。
  5. `ResultSet` 遍历结果 → 清点货物。
  6. 关闭资源（倒序关）→ **还车、关门**。
- **Statement vs PreparedStatement**（重点）：
  - `PreparedStatement` 用 `?` 占位符 + `setXxx` 赋值，**预编译**。像**先印好空白单据再填内容**，格式固定，谁也塞不进"额外指令"——**天然防 SQL 注入**。
  - `Statement` 字符串拼接 SQL，像**把顾客原话直接写进取货单**，遇到 `' OR '1'='1` 这种就翻车。
- **连接池（连接复用）**：每次开连接很贵（像每次用车都新买一辆）。连接池 = **出租车队**，用完还车（归还连接），下次再租，还顺手管数量。常用：HikariCP（快）、Druid（阿里，监控全）。
- **事务**：多条 SQL 要么全成、要么全败。`conn.setAutoCommit(false)` → 业务 → `commit()` / 异常 `rollback()`。像**转账**：扣钱和加钱必须同时成功，否则全退回。

**记忆口诀**：驱动→连接→预编译→执行→遍历→关闭；PreparedStatement 防注入、连接池管复用、事务保原子。

---

## Day 6｜数据库基础（MySQL）

**一句话定位**：学会"仓库管理"——建表、增删改查、索引、事务、设计规范。

- **SQL 四大家族**：
  - **DDL**（建库建表）：`CREATE / ALTER / DROP`——**盖仓库、改货架**。
  - **DML**（操作数据）：`INSERT / UPDATE / DELETE`——**上货、改价、下架**。
  - **DQL**（查询）：`SELECT`——**盘点**。
  - **DCL**（权限）：`GRANT`——**发门禁卡**。
- **常用 DQL**：`WHERE` 筛选、`ORDER BY` 排序、`LIMIT` 分页、`LIKE` 模糊查（`%` 任意多个、`_` 一个）、`GROUP BY` + `HAVING` 分组统计、`JOIN` 连表。
- **约束**（货架规矩）：`PRIMARY KEY` 主键（每个货品唯一编号）、`UNIQUE` 唯一（用户名不能重）、`NOT NULL` 非空、`FOREIGN KEY` 外键（关联另一张表）。
- **索引**：查得快、写变慢、占空间。像**书的目录**——按目录翻书秒到，但目录本身要占页数，书每次更新目录也得跟着改。常用：主键索引、唯一索引、普通索引；`EXPLAIN` 看 SQL 是否走索引。
- **事务 ACID**（一笔生意的四原则）：
  - **原子性**：全成或全败（转账）。
  - **一致性**：账始终平。
  - **隔离性**：各顾客的订单互不干扰（可配隔离级别）。
  - **持久性**：落账后断电也不丢。
- **三大范式**（仓库整理规范）：
  - **1NF**：字段不可再分——**每个格子只放一样东西**（别把"手机号1,手机号2"塞一个格）。
  - **2NF**：非主键字段完全依赖主键——**每件货都挂在主编号下**（别出现跟主键无关的字段）。
  - **3NF**：消除传递依赖——**不存"能从别处推出来的冗余信息"**（用户表里别存"部门经理电话"，那是部门表的事）。

**记忆口诀**：DDL 盖仓库、DML 上货、DQL 盘点；索引像目录、事务保四性、范式管整洁。

---

## Day 7｜综合实战：用户管理系统

**一句话定位**：第一次把前 6 天攒的功夫**做成一桌完整宴席**——一个能跑的用户管理系统。

- **整体结构**：`建表(MySQL) → DAO(JDBC) → Service(业务) → Servlet(控制) → JSP(展示)`，五层各司其职。
- **功能清单**：用户登录、用户增删改查、列表分页、关键字搜索、密码 MD5 加密、账号禁用、在线人数统计。
- **关键技术点**：
  - **登录流程**：查用户名 → 校验状态（禁用则拒）→ MD5 比对密码 → 把用户对象放进 Session（标记"已登录"）。像**刷卡进门后发手环**。
  - **MD5 加密**：密码不存明文，存哈希。像**保险柜密码只记指纹不记数字**，数据库被偷也解不出原密码。
  - **分页**：`LIMIT offset, size` + 总条数 COUNT，算出总页数。像**菜单分页**：一页 10 道菜，翻页看。
  - **EncodingFilter**：统一处理中文乱码（进店先定通用语言）。
  - **AuthFilter**：未登录访问受保护页面 → 拦截并踢回登录页（**进后厨要先亮手环**）。
  - **OnlineCountListener**：Session 创建/销毁时统计在线人数。
- **为什么分层**：如果全写在一个 Servlet 里，就像**洗菜、切菜、炒菜、端菜全是一个人**——累死且改一处全乱。分层后每层只干一件事，可替换、可测试。

**记忆口诀**：五层一条链：表→DAO→Service→Servlet→JSP；Filter 管安检、Listener 管计数、Session 管登录态。

---

## Day 8｜Maven 进阶 + 项目分层架构

**一句话定位**：把"单兵作战"升级成"正规军"——依赖管理、多模块拆分、分层架构。

- **Maven 坐标**（构件身份证）：`groupId（公司域名反写）+ artifactId（项目名）+ version（版本）`。
- **版本语义**：`1.0-SNAPSHOT` 快照（开发中、可覆盖）；`1.0` 正式版（不可变）。像**草稿纸 vs 印刷品**。
- **依赖范围 scope**（食材归属）：
  - `compile`：全程参与（spring-core）。
  - `provided`：**容器自带，打包不带**（servlet-api，Tomcat 里有，别重复打包）。
  - `runtime`：编译不需要、运行需要（MySQL 驱动）。
  - `test`：只测试用（JUnit）。
- **依赖冲突**：版本打架时，**最短路径优先**（离你最近的那条算），路径相同**先声明优先**；可用 `<exclusions>` 排除、`dependencyManagement` 统一锁版本。
- **生命周期**：`clean → compile → test → package → install → deploy`，执行后面会自动执行前面。像**流水线**：洗菜→切菜→炒菜→装盘→上桌。
- **多模块（父子工程）**：父工程 `packaging=pom` 只做"版本管理 + 模块聚合"，子模块各管一摊：
  ```
  web（页面）→ service（业务）→ dao（数据）→ common（公共）
  ```
  依赖**单向向下**，禁止循环——像**食堂打饭只能从窗口往外拿，不能绕回去**。
- **分层职责与禁忌**：
  | 层 | 该做 | 不该做 |
  |--|--|--|
  | Controller | 接参、校验、调 Service、返回 | 写业务、拼 SQL |
  | Service | 业务规则、**事务边界**、组合 DAO | 碰 HttpServletRequest、拼 SQL |
  | DAO | SQL 执行、结果映射 | 业务判断、管事务 |
  | Entity | 数据载体 | 业务方法 |
- **PO / DTO / VO**（不同岗位的交接单）：
  - **PO**：和表字段一一对应（含密码）。
  - **DTO**：层与层之间传的"标准表单"。
  - **VO**：返回给前端看的"菜单"（**不含密码**）。
  → 别把数据库原样甩给前端，像**别把进货单直接当菜单给客人看**。
- **接口与实现分离**：`UserService`（接口）+ `UserServiceImpl`（实现）。方便替换、Mock 测试、后续 Spring AOP 代理。像**订餐只认"窗口"，不认具体哪个厨师**——换人不换窗口。

**记忆口诀**：坐标三要素、scope 定归属、最短路径解冲突；web→service→dao→common 单向分层，事务放 Service。

---

## Day 9｜MyBatis 入门

**一句话定位**：把 Day5 手写的 JDBC 换成"半自动点货系统"——SQL 你写，其他（连接、映射、参数）框架包办。

- **ORM 思想**：Java 对象 ↔ 数据库表 自动映射。手写 JDBC 要一行行 `rs.getString` 手动装对象，MyBatis 一行 `mapper.findById(id)` 搞定。像**之前要人工盘点入库，现在扫码自动上架**。
- **MyBatis 定位**：**半自动 ORM**——SQL 自己写（灵活可控）；Hibernate/JPA 是全自动（框架生成 SQL，学起来重）。国内互联网公司主流用 MyBatis，像**私房菜师傅坚持自己颠勺**，不搞中央厨房全自动化。
- **核心对象链**：
  ```
  SqlSessionFactoryBuilder（读配置，用完即弃）
    → SqlSessionFactory（全局单例）
      → SqlSession（每次操作一个，线程不安全）
        → Mapper（接口代理，调用 SQL）
  ```
- **两大配置文件**：
  - `mybatis-config.xml`：全局配置（数据源、`mapUnderscoreToCamelCase` 驼峰映射、别名、注册 Mapper）。
  - `mapper/UserMapper.xml`：SQL 本体。
- **接口 + XML 绑定规则**（必须对号）：`namespace = 接口全限定名`，`id = 方法名`。像**每个窗口挂的招牌和菜名**，对不上就报 `Invalid bound statement`。
- **`#{}` vs `${}`**（面试必问）：
  - `#{}`：**预编译占位符 `?`**，PreparedStatement 赋值，**防 SQL 注入**。→ 填标准空白单据。
  - `${}`：**字符串直接拼接**，有注入风险。→ 顾客原话直接写进货单，`' OR '1'='1` 就翻车。
  - `${}` 只用于**表名、列名、ORDER BY** 这种不能占位的地方，且必须**白名单校验**。
- **多参数必须 `@Param`**：`List<User> findByPage(@Param("keyword") String kw, ...)`，否则只能叫 `arg0/param1`，容易错。
- **结果映射**：
  - `resultType`：字段名和属性名**一致**（或开了驼峰映射 `create_time→createTime`）时自动映射。
  - `resultMap`：名字对不上或**关联对象**时手动指定。`<id>` 主键、`<result>` 普通、`<association>` 一对一、`<collection>` 一对多。
- **主键回填**：`useGeneratedKeys="true" keyProperty="id"`，插入后 `user.getId()` 直接有值。像**上完货自动把新编号贴回去**。
- **事务**：目前手动 `session.commit()/rollback()`（Day13 交给 Spring 自动管）。

**记忆口诀**：namespace 对接口、id 对方法；`#{}` 安全填单、`${}` 危险拼接；@Param 起名、resultMap 管映射、useGeneratedKeys 回填主键。

---

## Day 10｜MyBatis 进阶

**一句话定位**：把 MyBatis 用熟——动态拼 SQL、多表关联、懒加载、缓存、自动分页。

- **动态 SQL**（核心中的核心，像"智能点单"）：
  - `<if test="...">`：有条件才拼——**有就说，没有就不说**。
  - `<where>`：自动加 WHERE、**自动去掉开头多余的 AND/OR**（救了你手动 `1=1` 的土办法）。
  - `<set>`：UPDATE 时自动加 SET、**去掉末尾多余逗号**。
  - `<choose>/<when>/<otherwise>`：**多选一**（像 switch，查 id 就按 id，没有就查用户名，再没有就查状态）。
  - `<foreach>`：遍历集合拼 `IN (?,?,?)`、批量插入、批量删除。
  - `<trim>`：`<where>/<set>` 的底层原理（自定义前后缀）。
  - `<sql>/<include>`：抽取公共列名/条件复用——像**常用配料的预制包**。
  - `<bind>`：先算好模糊查询的 `%keyword%` 再拼。
- **关联查询**（连表取货）：
  - **一对一 `<association>`**：文章 → 作者。
  - **一对多 `<collection>`**：文章 → 评论列表（`ofType` 是集合元素类型）。
  - **多对多**：中间表（文章 ↔ 标签，`article_tag` 做桥梁）。
  - **两种实现**：
    - **嵌套结果（JOIN 一次查）**：一次 SQL 全查出来，适合关联固定。像**点套餐**，菜一次上齐。
    - **嵌套查询（分步查）**：先查文章，用到作者时再查 `select="...findById"`，可配懒加载。像**先上主菜，客人要点配菜才去取**。
- **延迟加载（懒加载）**：`lazyLoadingEnabled=true` + 关联处 `fetchType="lazy"`——**用到才查，不用不查**，省资源。坑：SqlSession 关闭后再访问关联属性会报错，Web 环境要配 OpenSessionInView（Day13）。
- **缓存**：
  - **一级缓存**：默认开启，**同一个 SqlSession** 内相同查询第二次走缓存。像**本柜台自己的保温柜**，session 关了就清。
  - **二级缓存**：手动开启 `<cache/>`，**同一个 Mapper（namespace）跨 Session 共享**，实体要 `implements Serializable`。像**全店共用的热菜柜**。
  - 坑：多表关联时缓存可能**脏数据**（改了 user，article 的关联缓存不知道）；生产大项目常用 Redis。
- **PageHelper 分页**：一行 `PageHelper.startPage(page, size)`，紧跟的第一个查询自动被改写为 COUNT + LIMIT，再用 `PageInfo` 拿总页数等信息。原理是 **MyBatis 拦截器 + ThreadLocal**——所以 `startPage` 后**必须紧邻查询**，中间不能插别的查询，否则参数串台（像**自动翻页器**，翻完马上清空）。

**记忆口诀**：动态 SQL 靠 if/where/set/foreach；一对一 association、一对多 collection；懒加载用 fetchType=lazy；一级缓存跟 session、二级缓存跟 Mapper；分页 startPage 必须紧跟查询。

---

## Day 11｜Spring 核心：IoC、DI、AOP

**一句话定位**：Spring 是"中央总管"——对象的创建、装配、代理、事务全交给它管，程序员不再手动 `new`。

- **IoC（控制反转）**：以前 `UserService service = new UserServiceImpl()`，对象自己 new、自己管，像**每个厨师要自己下楼领食材**。现在把对象交给 **Spring 容器**统一创建和管理，谁要用谁申请。像**中央厨房统一采购、统一配菜**——你只写"我要什么"，不写"我去哪领"。
- **DI（依赖注入）**：IoC 的落地手段。用 `@Autowired` / 构造器注入，Spring 直接把依赖对象"递到你手上"。像**主管把食材直接塞你手里**，你不用跑腿。
- **Bean 管理**：
  - 注解：`@Component`（通用）、`@Service`（业务层）、`@Repository`（数据层）、`@Controller`（控制层）——**贴标签报名入库**。
  - 作用域：默认**单例**（全店一个招牌厨师，够用且省）；`@Scope("prototype")` 每次新实例（每桌一个专属服务员）。
  - 生命周期：创建 → 初始化 → 使用 → 销毁，Spring 接管（像**厨师上岗培训、下班交接**）。
- **AOP（面向切面编程）**：在不改原代码的情况下，给方法"加横切逻辑"（日志、权限、事务、性能监控）。
  - 专业词：切面 `@Aspect`、切点 `@Pointcut`（在哪些方法上动手）、通知 `@Before/@After/@Around`（动手时机）、代理（Spring 动态代理实现）。
  - 比喻：像**后厨的监控+门禁系统**——你不改炒菜流程，系统自动在"进厨房前"和"出菜后"做记录。或者像**电梯刷卡**：每个人都要过闸，但没人需要自己装闸机。
  - 和继承/装饰器的区别：**不用改任何业务代码**，横切逻辑集中在一处，像**给整条街统一装路灯**，而不是每家店自己拉电线。
- **声明式事务 `@Transactional`**：加在 Service 方法上，Spring 自动开/关/回滚事务——不用再手动 `session.commit()`。像**总管统一锁门开门**，出岔子自动全退。

**记忆口诀**：IoC 反转创建权、DI 递到手上、AOP 不动代码加切面、@Transactional 自动管事务；Bean 默认单例。

---

## Day 12｜Spring MVC

**一句话定位**：给 Web 层装上"智能前台"——所有请求先到总调度台，再分给对应的方法处理。

- **核心：DispatcherServlet（前端控制器）**：所有请求先进入这一个 Servlet，它负责**路由**（找谁处理）、**调参**（参数绑定）、**返显**（找哪个页面/JSON）。像**餐厅领位台**：客人进门先到总台，总台安排到对应包间。
- **处理流程**：`请求 → DispatcherServlet → HandlerMapping（找处理方法）→ Controller 方法 → 返回 ModelAndView → ViewResolver（找页面）→ 响应`。像**领位 → 点菜 → 炒菜 → 传菜 → 上桌**。
- **常用注解**：
  - `@Controller`：标记这是控制层类。
  - `@RequestMapping("/路径")`：URL 和方法的"对号入座表"（`@GetMapping/@PostMapping` 是简写）。
  - `@RequestParam`：取请求参数（`?name=x`）。
  - `@PathVariable`：取路径参数（`/users/{id}` 里的 id）。
  - `@RequestBody`：把请求体 JSON 自动转成 Java 对象（**自动反序列化**）。
  - `@ResponseBody`：把 Java 对象自动转成 JSON 返回（**自动序列化**）。→ 前后端分离的基石。
- **返回类型**：`String`（视图名）、`ModelAndView`（视图+数据）、`@ResponseBody`（JSON）、`RedirectView`（重定向）。
- **拦截器 Interceptor vs 过滤器 Filter**：
  - Filter：Servlet 规范，**所有请求**都过（包括静态资源），在 DispatcherServlet 之前。→ **店门口安检**。
  - Interceptor：Spring MVC 专属，只拦**进入 Controller 的请求**，更细（能拿到 Handler 对象）。→ **包间门口查预约**。
- **统一异常/结果**：`@ControllerAdvice` 全局异常处理 + 统一 `Result<T>` 返回体（code/msg/data），让前端永远收到标准格式。

**记忆口诀**：DispatcherServlet 是总调度；@RequestMapping 对号入座；@ResponseBody 出 JSON；Filter 管店门、Interceptor 管包间。

---

## Day 13｜SSM 整合（Spring + Spring MVC + MyBatis）

**一句话定位**：三大件"签合同"合成一套系统——**Spring 当总管，Spring MVC 当前台，MyBatis 当库房**，从此告别手动管理连接和事务。

- **为什么整合**：之前 MyBatis 每写一个方法都要 `openSession → commit → rollback`，很啰嗦。整合后：**数据源、SqlSession、事务全部交给 Spring 容器托管**，代码里只管业务。
- **整合要点**：
  1. **数据源交给 Spring**：用连接池（HikariCP/Druid）替代 MyBatis 自带的 POOLED。
  2. **SqlSessionFactoryBean**：由 Spring 创建 SqlSessionFactory（读 mybatis 配置 + 数据源）。
  3. **MapperScannerConfigurer**：自动扫描包，把 `UserMapper` 接口生成代理对象放进容器——**接口直接 `@Autowired` 注入**，不用再 `session.getMapper()`。
  4. **事务交给 Spring**：`@Transactional` 注解即可，自动提交/回滚。
  5. **web.xml 装配**：`ContextLoaderListener`（启动 Spring 容器）+ `DispatcherServlet`（启动 Spring MVC 容器）。
- **典型分层包结构**：
  ```
  controller（收请求）→ service（业务+事务）→ mapper（SQL）→ entity（实体）
  ```
- **XML vs 注解**：早期用大量 XML 拼配置（像**签一堆合同**），后来逐渐用注解 + 配置类简化。
- **经典面试问法**："Spring 容器和 Spring MVC 容器是父子容器关系"——MVC 容器能访问 Spring 容器的 Bean（Service），反过来不行。像**包间服务生能叫得动后厨，后厨不用知道包间是谁**。

**记忆口诀**：整合三件事——数据源归 Spring、Mapper 自动扫描注入、事务 @Transactional 托管；Web 层靠 ContextLoaderListener + DispatcherServlet 双启动。

---

## Day 14｜综合项目：博客系统（SSM 完整实战）

**一句话定位**：**毕业验收宴**——用 14 天学的全部功夫，独立做一个完整的博客系统。

- **功能全景**：
  - **用户模块**：注册（用户名查重 + 密码加密）、登录（校验 + Session 保持）、退出登录。
  - **文章模块**：发布、编辑、删除、列表、详情（浏览量 +1）。
  - **分类模块**：文章按分类管理。
  - **标签模块**：文章 ↔ 标签多对多（中间表）。
  - **评论模块**：文章 ↔ 评论一对多。
  - **通用能力**：分页（PageHelper）、关键字搜索、登录拦截器（未登录不能发文章/评论）。
- **技术栈串联**（相当于把整家店完整跑通）：
  ```
  浏览器 → DispatcherServlet → Controller → Service(@Transactional)
       → Mapper(XML 动态 SQL) → MySQL
       → JSP/JSTL 渲染 → 浏览器
  ```
- **项目中的设计亮点**（面试可讲）：
  - 密码用 **MD5 加盐**存储，不存明文。
  - 登录拦截器：未登录访问受保护 URL → 重定向登录页。
  - 文章列表用 `association` 带出作者昵称、分类名，用**子查询/GROUP_CONCAT** 带出评论数和标签。
  - 分页查询统一 `PageInfo → PageResult` 封装。
  - 统一返回 `Result<T>`，异常统一处理。
- **Debug 心法**：从前到后查——页面（JSP/JS）→ Controller（参数对没对上）→ Service（业务/事务）→ Mapper（SQL 对没对上、日志里看 PreparedStatement）。

**记忆口诀**：注册登录保安全、文章分类标签评论一套全；拦截器护驾、PageHelper 翻页、SSM 全家桶一次跑通。

---

## 附：14 天全貌速览表

| 阶段 | 天数 | 主题 | 生活比喻 | 一句话记忆 |
|--|--|--|--|--|
| 热身 | 0 | Web 基础 | 纸质菜单 vs 现做 | 动态网站 = 现点现做 |
| 地基 | Day1 | HTTP 协议 | 点菜暗号 | GET 喊、POST 递、2/4/5 状态码 |
| 地基 | Day2 | 环境 + Servlet | 大堂 + 大厨 | Tomcat 接单、Servlet 炒菜 |
| 进阶 | Day3 | 转发/重定向/Cookie/Session/Filter/Listener | 内部交接、安检、计数 | 转发不变地址、重定向换地址 |
| 进阶 | Day4 | JSP/EL/JSTL | 会填空的菜单模板 | EL 扫码取值、JSTL 循环积木 |
| 数据 | Day5 | JDBC | 取货通道 + 出租车队 | 预编译防注入、连接池复用 |
| 数据 | Day6 | MySQL | 仓库管理 | 索引像目录、事务保四性 |
| 实战 | Day7 | 用户管理系统 | 第一桌宴席 | 五层一条链、Filter 护驾 |
| 工程 | Day8 | Maven + 分层 | 正规军 + 食堂打饭 | 单向分层、事务放 Service |
| 数据 | Day9 | MyBatis 入门 | 半自动点货系统 | #{} 安全、${} 危险 |
| 数据 | Day10 | MyBatis 进阶 | 智能点单 + 保温柜 | 动态 SQL、懒加载、PageHelper |
| 框架 | Day11 | Spring IoC/DI/AOP | 中央厨房总管 | 反转创建、注入到手、切面不动代码 |
| 框架 | Day12 | Spring MVC | 领位总台 | DispatcherServlet 路由一切 |
| 框架 | Day13 | SSM 整合 | 三家签合同 | 数据源/事务全托管 |
| 框架 | Day14 | 博客系统 | 毕业验收宴 | 全套技术一次跑通 |

---

## 附：14 天高频易错点（考前小抄）

1. **转发 vs 重定向**：转发地址栏不变、1 次请求、能带 request 数据；重定向地址栏变、2 次请求。
2. **`#{}` vs `${}`**：`#{}` 预编译防注入；`${}` 拼接只用于表名/列名并要白名单。
3. **PreparedStatement 防注入**：先占位后赋值，`Statement` 字符串拼接危险。
4. **多参数不加 @Param**：MyBatis 里只能用 `arg0/param1`，易错。
5. **Mapper 绑定报错**：`namespace` 必须等于接口全限定名，`id` 必须等于方法名。
6. **中文乱码**：请求/响应都要设 UTF-8；JDBC URL 加 `characterEncoding=utf8`。
7. **MyBatis 增删改不生效**：没 `commit()`（要么手动提交，要么 `openSession(true)`）。
8. **PageHelper 串台**：`startPage` 后必须紧跟要分页的那条查询。
9. **一级缓存**跟 Session，**二级缓存**跟 Mapper（要 Serializable），多表关联慎用缓存。
10. **事务边界**放 Service 层，不放 DAO/Controller。
11. **PO/DTO/VO 别混**：返回前端用 VO，别把带密码的 PO 直接甩出去。
12. **Filter 管全部请求**，**Interceptor 只管 Controller**，别混用场景。
13. **session 30 分钟默认过期**，登录态放 Session 不是 Cookie。
14. **Cookie 存浏览器、Session 存服务器**，Cookie 别存密码等敏感信息。

---

*本精简版根据 `aaaanotes/javaweb` 目录下 0.基本 与 Day1~Day14 原始笔记整理，保留全部核心知识点与专业术语，删去重复的代码细节与练习步骤，如需某天完整代码可回看原笔记。*
