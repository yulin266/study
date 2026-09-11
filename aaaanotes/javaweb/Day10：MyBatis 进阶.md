

## 学习目标

- 精通动态 SQL（`<if>` `<choose>` `<where>` `<set>` `<foreach>` `<trim>` `<sql>`）
- 掌握一对一、一对多、多对多关联查询（`<association>` `<collection>`）
- 理解延迟加载（懒加载）
- 掌握一级缓存、二级缓存原理与配置
- 熟练使用 PageHelper 分页插件
- 完成带关联查询的博客实战

---

## 一、动态 SQL

动态 SQL 是 MyBatis 的核心特性，可以根据条件动态拼接 SQL，避免在 Java 中手动拼接字符串。

### 1. `<if>` 条件判断

**场景：** 多条件搜索，条件可选。

```xml
<select id="search" resultMap="userResultMap">
    SELECT * FROM sys_user
    WHERE 1 = 1
    <if test="keyword != null and keyword != ''">
        AND (username LIKE CONCAT('%', #{keyword}, '%')
             OR nickname LIKE CONCAT('%', #{keyword}, '%'))
    </if>
    <if test="status != null">
        AND status = #{status}
    </if>
    <if test="minAge != null">
        AND age &gt;= #{minAge}
    </if>
    <if test="maxAge != null">
        AND age &lt;= #{maxAge}
    </if>
</select>
```

> 💡 `WHERE 1=1` 是传统写法，后面可用 `<where>` 标签优化。
> 💡 XML 中 `<` `>` 需转义为 `&lt;` `&gt;`，或使用 `<![CDATA[ ... ]]>`。

**test 表达式常用写法：**

| 场景 | 写法 |
|------|------|
| 字符串非空 | `test="name != null and name != ''"` |
| 数字非空 | `test="age != null"` |
| 集合非空 | `test="list != null and list.size() > 0"` |
| 布尔判断 | `test="status == 1"` |
| 字符串比较 | `test="type == 'admin'"`（**注意：字符串用单引号**） |
| 逻辑与 | `test="a != null and b != null"` |
| 逻辑或 | `test="a != null or b != null"` |

> ⚠️ **常见坑**：`test="type == 'A'"` 中，如果 type 是 `Character` 类型，`'A'` 会被当作 char 比较；如果是 String 则正常。推荐用 `test='"A".equals(type)'` 更稳妥。

### 2. `<where>` 智能处理 WHERE

**问题：** 用 `<if>` 时，如果所有条件都不满足，`WHERE` 后无内容会报错；如果第一个条件满足，`AND` 开头也会报错。

**解决：** `<where>` 标签自动处理：
- 如果有条件，自动加 `WHERE`
- 自动去掉开头多余的 `AND` / `OR`

```xml
<select id="search" resultMap="userResultMap">
    SELECT * FROM sys_user
    <where>
        <if test="keyword != null and keyword != ''">
            AND (username LIKE CONCAT('%', #{keyword}, '%')
                 OR nickname LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
        <if test="minAge != null">
            AND age &gt;= #{minAge}
        </if>
    </where>
    ORDER BY id DESC
</select>
```

### 3. `<set>` 智能处理 UPDATE

**问题：** UPDATE 时，只想更新非空字段，但逗号处理麻烦。

**解决：** `<set>` 标签自动：
- 加 `SET`
- 去掉末尾多余的逗号

```xml
<update id="updateSelective">
    UPDATE sys_user
    <set>
        <if test="username != null">username = #{username},</if>
        <if test="nickname != null">nickname = #{nickname},</if>
        <if test="age != null">age = #{age},</if>
        <if test="email != null">email = #{email},</if>
        <if test="phone != null">phone = #{phone},</if>
        <if test="status != null">status = #{status},</if>
    </set>
    WHERE id = #{id}
</update>
```

> ⚠️ 如果所有条件都不满足，SQL 会变成 `UPDATE sys_user WHERE id = ?`，语法错误。应在 Service 层校验至少一个字段非空。

### 4. `<choose>` / `<when>` / `<otherwise>`

**场景：** 多选一，类似 Java 的 `switch`。

```xml
<select id="findByCondition" resultMap="userResultMap">
    SELECT * FROM sys_user
    <where>
        <choose>
            <when test="id != null">
                AND id = #{id}
            </when>
            <when test="username != null and username != ''">
                AND username = #{username}
            </when>
            <when test="email != null and email != ''">
                AND email = #{email}
            </when>
            <otherwise>
                AND status = 1
            </otherwise>
        </choose>
    </where>
</select>
```

### 5. `<foreach>` 遍历集合

**场景：** IN 查询、批量插入、批量删除。

**批量删除：**
```xml
<delete id="deleteBatch">
    DELETE FROM sys_user WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</delete>
```

**批量插入：**
```xml
<insert id="batchInsert">
    INSERT INTO sys_user(username, password, nickname, age, email)
    VALUES
    <foreach collection="users" item="u" separator=",">
        (#{u.username}, #{u.password}, #{u.nickname}, #{u.age}, #{u.email})
    </foreach>
</insert>
```

**IN 查询 + 其他条件：**
```xml
<select id="findByIds" resultMap="userResultMap">
    SELECT * FROM sys_user
    WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
    <if test="status != null">
        AND status = #{status}
    </if>
</select>
```

**`<foreach>` 属性：**

| 属性 | 说明 |
|------|------|
| `collection` | 要遍历的集合，`@Param` 指定的名或 `list`/`array` |
| `item` | 每次迭代的元素名 |
| `index` | 索引（List 是下标，Map 是 key） |
| `open` | 开始符号 |
| `close` | 结束符号 |
| `separator` | 分隔符 |

> ⚠️ **collection 命名规则**：
> - 用 `@Param("ids")` → `collection="ids"`
> - 不用 `@Param`，List → `collection="list"`
> - 不用 `@Param`，数组 → `collection="array"`
> - 强烈推荐用 `@Param` 命名，避免混乱。

### 6. `<trim>` 自定义前后缀

`<where>` 和 `<set>` 本质是 `<trim>` 的封装。

```xml
<!-- 等价于 <where> -->
<trim prefix="WHERE" prefixOverrides="AND |OR ">
    <if test="status != null">AND status = #{status}</if>
    <if test="keyword != null">AND nickname LIKE #{keyword}</if>
</trim>

<!-- 等价于 <set> -->
<trim prefix="SET" suffixOverrides=",">
    <if test="nickname != null">nickname = #{nickname},</if>
    <if test="age != null">age = #{age},</if>
</trim>
```

### 7. `<sql>` 与 `<include>`

**抽取可复用 SQL 片段：**

```xml
<!-- 定义 -->
<sql id="baseColumns">
    id, username, password, nickname, age, email, phone, status, create_time, update_time
</sql>

<sql id="conditionWhere">
    <where>
        <if test="keyword != null and keyword != ''">
            AND nickname LIKE CONCAT('%', #{keyword}, '%')
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
    </where>
</sql>

<!-- 引用 -->
<select id="findById" resultMap="userResultMap">
    SELECT <include refid="baseColumns"/>
    FROM sys_user
    WHERE id = #{id}
</select>

<select id="findByPage" resultMap="userResultMap">
    SELECT <include refid="baseColumns"/>
    FROM sys_user
    <include refid="conditionWhere"/>
    ORDER BY id DESC
</select>
```

**`<include>` 传参：**
```xml
<sql id="orderBy">
    ORDER BY ${column} ${direction}
</sql>

<select id="findAll">
    SELECT * FROM sys_user
    <include refid="orderBy">
        <property name="column" value="id"/>
        <property name="direction" value="DESC"/>
    </include>
</select>
```

### 8. `<bind>` 变量绑定

**场景：** 模糊查询时统一处理 `%`。

```xml
<select id="search" resultMap="userResultMap">
    <bind name="pattern" value="'%' + keyword + '%'"/>
    SELECT * FROM sys_user
    WHERE nickname LIKE #{pattern}
</select>
```

> 💡 比 `CONCAT('%', #{keyword}, '%')` 更灵活，可用于复杂表达式。

### 9. `<script>` 注解中动态 SQL

如果不想写 XML，可用 `<script>` 在注解中写动态 SQL：

```java
@Select("<script>" +
        "SELECT * FROM sys_user " +
        "<where>" +
        "  <if test='keyword != null'>AND nickname LIKE CONCAT('%', #{keyword}, '%')</if>" +
        "  <if test='status != null'>AND status = #{status}</if>" +
        "</where>" +
        "</script>")
List<User> search(@Param("keyword") String keyword, @Param("status") Integer status);
```

> ⚠️ 复杂的动态 SQL 还是推荐 XML，注解只适合简单场景。

---

## 二、关联查询

关联查询是 MyBatis 的重点，涉及多表 JOIN 或分步查询。

### 1. 数据准备

```sql
-- 分类表
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章表
CREATE TABLE article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    summary VARCHAR(500),
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    views INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 标签表 + 中间表
CREATE TABLE tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE article_tag (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 2. 实体类设计

**Article 实体（含关联对象）：**
```java
package com.example.entity;

import java.time.LocalDateTime;
import java.util.List;

public class Article {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private Long userId;
    private Long categoryId;
    private Integer views;
    private Integer status;
    private LocalDateTime createTime;

    // 一对一：文章作者
    private User author;
    // 一对一：文章分类
    private Category category;
    // 一对多：文章评论
    private List<Comment> comments;
    // 多对多：文章标签
    private List<Tag> tags;

    // getter/setter 省略
    // ...
}
```

**Category、Comment、Tag 实体类似，略。**

### 3. 一对一：`<association>`

**场景：** 查询文章及其作者。

**方式一：嵌套结果（JOIN 一次查出）**

```xml
<resultMap id="articleWithAuthorMap" type="Article">
    <id column="id" property="id"/>
    <result column="title" property="title"/>
    <result column="content" property="content"/>
    <result column="create_time" property="createTime"/>

    <!-- 一对一：author -->
    <association property="author" javaType="User">
        <id column="u_id" property="id"/>
        <result column="u_username" property="username"/>
        <result column="u_nickname" property="nickname"/>
        <result column="u_avatar" property="avatar"/>
    </association>
</resultMap>

<select id="findArticleWithAuthor" resultMap="articleWithAuthorMap">
    SELECT a.id, a.title, a.content, a.create_time,
           u.id AS u_id, u.username AS u_username,
           u.nickname AS u_nickname, u.avatar AS u_avatar
    FROM article a
    LEFT JOIN sys_user u ON a.user_id = u.id
    WHERE a.id = #{id}
</select>
```

**方式二：嵌套查询（分步查询，可懒加载）**

```xml
<resultMap id="articleWithAuthorLazyMap" type="Article">
    <id column="id" property="id"/>
    <result column="title" property="title"/>
    <result column="user_id" property="userId"/>

    <!-- column 是传给子查询的参数，select 是子查询的语句 id -->
    <association property="author"
                 column="user_id"
                 select="com.example.dao.UserMapper.findById"
                 fetchType="lazy"/>
</resultMap>

<select id="findArticleWithAuthorLazy" resultMap="articleWithAuthorLazyMap">
    SELECT id, title, user_id FROM article WHERE id = #{id}
</select>
```

**UserMapper 中：**
```xml
<select id="findById" resultMap="userResultMap">
    SELECT * FROM sys_user WHERE id = #{id}
</select>
```

**对比：**

| 方式 | 特点 | 适用 |
|------|------|------|
| 嵌套结果（JOIN） | 一次查询，SQL 复杂 | 关联数据固定 |
| 嵌套查询（分步） | 多次查询，可懒加载 | 关联数据按需加载 |

### 4. 一对多：`<collection>`

**场景：** 查询文章及其所有评论。

```xml
<resultMap id="articleWithCommentsMap" type="Article">
    <id column="id" property="id"/>
    <result column="title" property="title"/>

    <collection property="comments" ofType="Comment">
        <id column="c_id" property="id"/>
        <result column="c_content" property="content"/>
        <result column="c_create_time" property="createTime"/>
        <result column="c_user_id" property="userId"/>
    </collection>
</resultMap>

<select id="findArticleWithComments" resultMap="articleWithCommentsMap">
    SELECT a.id, a.title,
           c.id AS c_id, c.content AS c_content,
           c.create_time AS c_create_time, c.user_id AS c_user_id
    FROM article a
    LEFT JOIN comment c ON a.id = c.article_id
    WHERE a.id = #{id}
    ORDER BY c.create_time DESC
</select>
```

> ⚠️ `<collection>` 的 `ofType` 是集合元素类型，不能写 `javaType`。

**分步查询：**
```xml
<resultMap id="articleWithCommentsLazyMap" type="Article">
    <id column="id" property="id"/>
    <result column="title" property="title"/>
    <collection property="comments"
                column="id"
                select="com.example.dao.CommentMapper.findByArticleId"
                fetchType="lazy"/>
</resultMap>
```

### 5. 多对多：中间表关联

**场景：** 查询文章及其所有标签。

**嵌套结果：**
```xml
<resultMap id="articleWithTagsMap" type="Article">
    <id column="id" property="id"/>
    <result column="title" property="title"/>

    <collection property="tags" ofType="Tag">
        <id column="t_id" property="id"/>
        <result column="t_name" property="name"/>
    </collection>
</resultMap>

<select id="findArticleWithTags" resultMap="articleWithTagsMap">
    SELECT a.id, a.title, t.id AS t_id, t.name AS t_name
    FROM article a
    LEFT JOIN article_tag at ON a.id = at.article_id
    LEFT JOIN tag t ON at.tag_id = t.id
    WHERE a.id = #{id}
</select>
```

**分步查询：**
```xml
<resultMap id="articleWithTagsLazyMap" type="Article">
    <id column="id" property="id"/>
    <result column="title" property="title"/>
    <collection property="tags"
                column="id"
                select="com.example.dao.TagMapper.findByArticleId"
                fetchType="lazy"/>
</resultMap>
```

**TagMapper：**
```xml
<select id="findByArticleId" resultType="Tag">
    SELECT t.* FROM tag t
    INNER JOIN article_tag at ON t.id = at.tag_id
    WHERE at.article_id = #{articleId}
</select>
```

### 6. 多重关联

**场景：** 文章 + 作者 + 分类 + 标签。

```xml
<resultMap id="articleDetailMap" type="Article">
    <id column="id" property="id"/>
    <result column="title" property="title"/>
    <result column="content" property="content"/>
    <result column="summary" property="summary"/>
    <result column="views" property="views"/>
    <result column="create_time" property="createTime"/>

    <!-- 作者 -->
    <association property="author" javaType="User">
        <id column="u_id" property="id"/>
        <result column="u_username" property="username"/>
        <result column="u_nickname" property="nickname"/>
        <result column="u_avatar" property="avatar"/>
    </association>

    <!-- 分类 -->
    <association property="category" javaType="Category">
        <id column="c_id" property="id"/>
        <result column="c_name" property="name"/>
    </association>

    <!-- 标签 -->
    <collection property="tags" ofType="Tag">
        <id column="t_id" property="id"/>
        <result column="t_name" property="name"/>
    </collection>
</resultMap>

<select id="findArticleDetail" resultMap="articleDetailMap">
    SELECT a.id, a.title, a.content, a.summary, a.views, a.create_time,
           u.id AS u_id, u.username AS u_username,
           u.nickname AS u_nickname, u.avatar AS u_avatar,
           c.id AS c_id, c.name AS c_name,
           t.id AS t_id, t.name AS t_name
    FROM article a
    LEFT JOIN sys_user u ON a.user_id = u.id
    LEFT JOIN category c ON a.category_id = c.id
    LEFT JOIN article_tag at ON a.id = at.article_id
    LEFT JOIN tag t ON at.tag_id = t.id
    WHERE a.id = #{id}
</select>
```

> ⚠️ **注意**：JOIN 多表时会产生笛卡尔积，一篇文章有 3 个标签会返回 3 行，MyBatis 会自动合并。数据量大时性能差，考虑分步查询。

---

## 三、延迟加载

### 1. 什么是延迟加载

延迟加载（懒加载）：**用到关联数据时才查询**，不用则不查，节省资源。

**开启配置：**
```xml
<settings>
    <setting name="lazyLoadingEnabled" value="true"/>
    <setting name="aggressiveLazyLoading" value="false"/>
    <!-- 按需加载指定属性，而不是加载对象就查所有 -->
    <setting name="lazyLoadTriggerMethods" value=""/>
</settings>
```

**配置说明：**
- `lazyLoadingEnabled=true`：开启全局延迟加载
- `aggressiveLazyLoading=false`：按需加载（3.4.1+ 默认 false）
- `lazyLoadTriggerMethods`：触发加载的方法（如 `equals`、`hashCode`、`toString`），设置为空避免误触发

### 2. 局部控制

用 `fetchType` 覆盖全局配置：

```xml
<!-- 即使全局关闭，这个也懒加载 -->
<association property="author" column="user_id"
             select="com.example.dao.UserMapper.findById"
             fetchType="lazy"/>

<!-- 即使全局开启，这个也立即加载 -->
<collection property="tags" column="id"
            select="com.example.dao.TagMapper.findByArticleId"
            fetchType="eager"/>
```

### 3. 测试延迟加载

```java
try (SqlSession session = MyBatisUtil.openSession()) {
    ArticleMapper mapper = session.getMapper(ArticleMapper.class);
    Article article = mapper.findArticleWithAuthorLazy(1L);
    System.out.println("只查了文章：" + article.getTitle());
    // 此时不会查询 user

    System.out.println("作者：" + article.getAuthor().getNickname());
    // 此时才触发查询 user
}
```

**日志观察：**
```
==>  Preparing: SELECT id, title, user_id FROM article WHERE id = ?
==> Parameters: 1(Long)
<==      Total: 1
只查了文章：Java 入门
==>  Preparing: SELECT * FROM sys_user WHERE id = ?
==> Parameters: 1(Long)
<==      Total: 1
作者：管理员
```

### 4. 延迟加载的注意事项

- 延迟加载依赖 **SqlSession 未关闭**，如果 Session 关闭后再访问关联属性会报错
- Web 环境下需配置 `OpenSessionInViewFilter`（Day13 详解）
- 或改为立即加载，避免踩坑

---

## 四、缓存

### 1. 一级缓存（SqlSession 级）

**特点：**
- 默认开启，无需配置
- 作用范围：同一个 `SqlSession`
- 同一 SQL、同一参数，第二次直接走缓存
- Session 关闭或 commit/rollback 后缓存清空

**测试：**
```java
try (SqlSession session = MyBatisUtil.openSession()) {
    UserMapper mapper = session.getMapper(UserMapper.class);
    User u1 = mapper.findById(1L);
    User u2 = mapper.findById(1L);
    System.out.println(u1 == u2); // true（同一对象）
}
```
日志只输出一次 SQL。

**缓存失效场景：**
- 不同 SqlSession
- 执行了增删改（commit）
- 手动 `session.clearCache()`
- 查询条件不同
- 执行了 `flushCache=true` 的查询

### 2. 二级缓存（Mapper 级）

**特点：**
- 需手动开启
- 作用范围：同一 namespace（Mapper）
- 跨 SqlSession 共享
- 实体需实现 `Serializable`

**配置：**

**（1）全局开启（默认 true）：**
```xml
<settings>
    <setting name="cacheEnabled" value="true"/>
</settings>
```

**（2）Mapper 中声明：**
```xml
<mapper namespace="com.example.dao.UserMapper">
    <cache eviction="LRU"
           flushInterval="60000"
           size="512"
           readOnly="true"/>
    <!-- ... -->
</mapper>
```

**`<cache>` 属性：**

| 属性 | 说明 | 可选值 |
|------|------|--------|
| eviction | 回收策略 | LRU（默认）、FIFO、SOFT、WEAK |
| flushInterval | 刷新间隔（毫秒） | 默认无，仅调用时刷新 |
| size | 最多缓存对象数 | 默认 1024 |
| readOnly | 只读 | true 返回同一对象（快，不安全）；false 返回副本（安全，慢） |

**（3）实体实现 Serializable：**
```java
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    // ...
}
```

**（4）单个语句控制：**
```xml
<!-- 禁用二级缓存 -->
<select id="findById" useCache="false" ...>

<!-- 执行后清空缓存 -->
<insert id="insert" flushCache="true" ...>
```

**测试：**
```java
// 第一个 Session
try (SqlSession s1 = MyBatisUtil.openSession()) {
    UserMapper m1 = s1.getMapper(UserMapper.class);
    m1.findById(1L); // 查数据库
}

// 第二个 Session
try (SqlSession s2 = MyBatisUtil.openSession()) {
    UserMapper m2 = s2.getMapper(UserMapper.class);
    m2.findById(1L); // 走二级缓存，不查库
}
```

### 3. 一级 vs 二级缓存

| 特性 | 一级缓存 | 二级缓存 |
|------|----------|----------|
| 范围 | SqlSession | Mapper（namespace） |
| 默认 | 开启 | 关闭 |
| 跨 Session | ❌ | ✅ |
| 失效 | commit/rollback/close | 增删改/超时 |
| 存储 | 内存 | 内存/可配第三方 |
| 适用 | 同会话重复查询 | 跨会话热点数据 |

### 4. 缓存的坑

- **多表关联**：更新了 user 表，article 的关联缓存不会自动失效，导致脏数据
- **分布式环境**：多台服务器各自缓存，数据不一致
- **建议**：简单查询可用缓存，复杂关联慎用，生产环境常用 Redis 替代

---

## 五、PageHelper 分页插件

### 1. 为什么用 PageHelper

**手写分页的问题：**
- 每个查询都要写 `LIMIT` 和 `COUNT`
- 分页参数处理繁琐
- 不同数据库语法不同

**PageHelper 优势：**
- 一行代码实现分页
- 自动生成 COUNT 查询
- 支持多种数据库
- 返回丰富的分页信息

### 2. 引入依赖

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>6.1.0</version>
</dependency>
```

### 3. 配置插件

**在 mybatis-config.xml 中：**
```xml
<plugins>
    <plugin interceptor="com.github.pagehelper.PageInterceptor">
        <property name="helperDialect" value="mysql"/>
        <property name="reasonable" value="true"/>
        <property name="supportMethodsArguments" value="true"/>
        <property name="params" value="count=countSql"/>
    </plugin>
</plugins>
```

**属性说明：**

| 属性 | 说明 |
|------|------|
| helperDialect | 数据库方言（mysql/oracle/postgresql） |
| reasonable | 页码合理化（pageNum<1 查第一页，>总页数查最后一页） |
| supportMethodsArguments | 支持从方法参数自动识别分页 |
| params | 参数映射 |

### 4. 使用

**最简用法：**
```java
// 紧跟在 startPage 后的第一个查询会被分页
PageHelper.startPage(1, 10);
List<User> users = userMapper.findAll();

PageInfo<User> pageInfo = new PageInfo<>(users);
System.out.println("总记录数：" + pageInfo.getTotal());
System.out.println("总页数：" + pageInfo.getPages());
System.out.println("当前页：" + pageInfo.getPageNum());
System.out.println("每页数量：" + pageInfo.getPageSize());
System.out.println("结果：" + pageInfo.getList());
```

**⚠️ 注意事项：**
- `startPage` 必须**紧邻**要分页的查询，中间不能有其他查询
- `startPage` 只对**第一个**查询生效
- 用 `PageHelper.clearPage()` 清除

**`PageInfo` 常用属性：**

| 属性 | 说明 |
|------|------|
| pageNum | 当前页 |
| pageSize | 每页数量 |
| total | 总记录数 |
| pages | 总页数 |
| list | 当前页数据 |
| hasPreviousPage | 是否有上一页 |
| hasNextPage | 是否有下一页 |
| isFirstPage | 是否第一页 |
| isLastPage | 是否最后一页 |
| navigatePages | 导航页码数 |
| navigatepageNums | 导航页码数组 |

### 5. 封装到 Service

```java
public PageInfo<User> findByPage(String keyword, Integer status, int page, int size) {
    try (SqlSession session = MyBatisUtil.openSession()) {
        UserMapper mapper = session.getMapper(UserMapper.class);
        PageHelper.startPage(page, size);
        List<User> list = mapper.search(keyword, status);
        return new PageInfo<>(list);
    }
}
```

**Mapper 中不需要写 LIMIT：**
```xml
<select id="search" resultMap="userResultMap">
    SELECT * FROM sys_user
    <where>
        <if test="keyword != null and keyword != ''">
            AND nickname LIKE CONCAT('%', #{keyword}, '%')
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
    </where>
    ORDER BY id DESC
</select>
```

PageHelper 会自动改写为 `SELECT COUNT(*)` 和 `LIMIT ?, ?`。

### 6. 自定义 PageResult

如果不想暴露 PageInfo 的所有字段，可以转换：

```java
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;

    public static <T> PageResult<T> of(PageInfo<T> pageInfo) {
        PageResult<T> r = new PageResult<>();
        r.list = pageInfo.getList();
        r.total = pageInfo.getTotal();
        r.pageNum = pageInfo.getPageNum();
        r.pageSize = pageInfo.getPageSize();
        r.pages = pageInfo.getPages();
        return r;
    }
    // getter/setter
}
```

### 7. 分页原理

PageHelper 通过 **MyBatis 拦截器（Interceptor）** 实现：
1. `startPage` 把分页参数存入 `ThreadLocal`
2. 执行查询时，拦截器读取参数
3. 先执行 `COUNT` 查询获取总数
4. 改写 SQL 添加 `LIMIT`
5. 清空 `ThreadLocal`

> 💡 正因为用 ThreadLocal，`startPage` 后必须紧跟查询，否则参数会错乱。

---

## 六、实战：博客文章列表（关联 + 分页）

### 1. 需求

- 分页展示文章列表
- 每篇文章显示：标题、摘要、作者昵称、分类名、标签、评论数、浏览量
- 支持按分类筛选、关键词搜索
- 按创建时间倒序

### 2. 实体类

```java
public class ArticleVO {
    private Long id;
    private String title;
    private String summary;
    private Integer views;
    private LocalDateTime createTime;
    private String authorNickname;
    private String categoryName;
    private Integer commentCount;
    private List<String> tags;
    // getter/setter
}
```

### 3. Mapper

```java
public interface ArticleMapper {
    List<ArticleVO> findArticleList(@Param("keyword") String keyword,
                                    @Param("categoryId") Long categoryId);

    Article findArticleDetail(@Param("id") Long id);
}
```

### 4. XML

```xml
<!-- 列表：用子查询统计评论数，GROUP_CONCAT 拼接标签 -->
<select id="findArticleList" resultType="ArticleVO">
    SELECT
        a.id, a.title, a.summary, a.views, a.create_time,
        u.nickname AS author_nickname,
        c.name AS category_name,
        (SELECT COUNT(*) FROM comment cm WHERE cm.article_id = a.id) AS comment_count,
        (SELECT GROUP_CONCAT(t.name)
         FROM article_tag at
         INNER JOIN tag t ON at.tag_id = t.id
         WHERE at.article_id = a.id) AS tag_str
    FROM article a
    LEFT JOIN sys_user u ON a.user_id = u.id
    LEFT JOIN category c ON a.category_id = c.id
    <where>
        a.status = 1
        <if test="keyword != null and keyword != ''">
            AND (a.title LIKE CONCAT('%', #{keyword}, '%')
                 OR a.content LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test="categoryId != null">
            AND a.category_id = #{categoryId}
        </if>
    </where>
    ORDER BY a.create_time DESC
</select>

<!-- 详情：用 association + collection -->
<resultMap id="articleDetailMap" type="Article">
    <id column="id" property="id"/>
    <result column="title" property="title"/>
    <result column="content" property="content"/>
    <result column="summary" property="summary"/>
    <result column="views" property="views"/>
    <result column="create_time" property="createTime"/>

    <association property="author" javaType="User">
        <id column="u_id" property="id"/>
        <result column="u_nickname" property="nickname"/>
        <result column="u_avatar" property="avatar"/>
    </association>

    <association property="category" javaType="Category">
        <id column="c_id" property="id"/>
        <result column="c_name" property="name"/>
    </association>

    <collection property="tags" ofType="Tag">
        <id column="t_id" property="id"/>
        <result column="t_name" property="name"/>
    </collection>
</resultMap>

<select id="findArticleDetail" resultMap="articleDetailMap">
    SELECT a.id, a.title, a.content, a.summary, a.views, a.create_time,
           u.id AS u_id, u.nickname AS u_nickname, u.avatar AS u_avatar,
           c.id AS c_id, c.name AS c_name,
           t.id AS t_id, t.name AS t_name
    FROM article a
    LEFT JOIN sys_user u ON a.user_id = u.id
    LEFT JOIN category c ON a.category_id = c.id
    LEFT JOIN article_tag at ON a.id = at.article_id
    LEFT JOIN tag t ON at.tag_id = t.id
    WHERE a.id = #{id}
</select>
```

### 5. Service

```java
public class ArticleServiceImpl implements ArticleService {

    @Override
    public PageResult<ArticleVO> findPage(String keyword, Long categoryId,
                                          int page, int size) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            ArticleMapper mapper = session.getMapper(ArticleMapper.class);
            PageHelper.startPage(page, size);
            List<ArticleVO> list = mapper.findArticleList(keyword, categoryId);
            PageInfo<ArticleVO> pageInfo = new PageInfo<>(list);

            // 处理 tag_str → List<String>
            for (ArticleVO vo : pageInfo.getList()) {
                if (vo.getTagStr() != null) {
                    vo.setTags(Arrays.asList(vo.getTagStr().split(",")));
                }
            }
            return PageResult.of(pageInfo);
        }
    }

    @Override
    public Article findDetail(Long id) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            ArticleMapper mapper = session.getMapper(ArticleMapper.class);
            return mapper.findArticleDetail(id);
        }
    }
}
```

> ⚠️ 上面用 `resultType="ArticleVO"` 时，`tag_str` 需要手动转成 `List<String>`。如果字段名与属性名映射不上，可考虑用自定义 TypeHandler 或改 XML 中的 resultMap。

---

## 七、常见问题与排错

| 问题 | 原因 | 解决 |
|------|------|------|
| `<if>` 不生效 | test 表达式错误 | 检查字符串用单引号，属性名拼写 |
| `<where>` 后无内容 | 所有条件为空 | 保证至少一个条件或去掉 where |
| `<foreach>` 报错 collection | 集合名不匹配 | 用 `@Param("ids")` 或用 `list`/`array` |
| 关联对象为 null | 字段别名冲突 | 别名加前缀（u_id、c_id） |
| 一对多结果重复 | JOIN 笛卡尔积 | MyBatis 自动合并，或改分步查询 |
| 二级缓存不生效 | 未实现 Serializable | 实体实现 `Serializable` |
| 二级缓存脏数据 | 多表关联更新 | 慎用或改用 Redis |
| PageHelper 不生效 | startPage 后未紧跟查询 | 确保中间无其他查询 |
| PageHelper 页码错乱 | ThreadLocal 未清 | 用 `PageHelper.clearPage()` |
| 延迟加载报错 | Session 已关闭 | 用 OpenSessionInView 或改立即加载 |

---

## 八、实践练习

### 练习 1：动态 SQL
1. 实现多条件搜索（keyword + status + 年龄范围）
2. 用 `<where>` `<if>` 动态拼接
3. 实现批量删除、批量插入（`<foreach>`）
4. 实现选择性更新（`<set>`）

### 练习 2：关联查询
1. 实现"查询文章及其作者"（一对一，association）
2. 实现"查询文章及其评论"（一对多，collection）
3. 实现"查询文章及其标签"（多对多，中间表）
4. 分别用嵌套结果和分步查询实现

### 练习 3：延迟加载
1. 开启延迟加载
2. 观察查询文章时是否立即查作者
3. 用 `fetchType` 局部覆盖

### 练习 4：缓存
1. 测试一级缓存（同 Session 两次查询）
2. 开启二级缓存，测试跨 Session
3. 更新数据后验证缓存失效

### 练习 5：PageHelper
1. 引入 PageHelper
2. 实现文章分页列表
3. 用 PageInfo 获取总页数等信息
4. 封装成 PageResult

### 练习 6（进阶）：博客首页
综合以上，完成博客首页：
- 分页展示文章
- 显示作者、分类、标签、评论数
- 支持搜索和分类筛选
- 用 JSTL 渲染到 JSP

---

## 九、总结与拓展

### 今日总结
- **动态 SQL**：
  - `<if>` 条件判断
  - `<where>` 智能 WHERE（去多余 AND）
  - `<set>` 智能 SET（去多余逗号）
  - `<choose>/<when>/<otherwise>` 多选一
  - `<foreach>` 遍历（IN 查询、批量操作）
  - `<trim>` 自定义前后缀
  - `<sql>/<include>` 复用 SQL 片段
  - `<bind>` 变量绑定
- **关联查询**：
  - `<association>` 一对一
  - `<collection>` 一对多
  - 嵌套结果（JOIN）vs 嵌套查询（分步）
- **延迟加载**：`lazyLoadingEnabled` + `fetchType`
- **缓存**：
  - 一级缓存（SqlSession，默认开启）
  - 二级缓存（Mapper，需配置 + Serializable）
- **PageHelper**：`startPage` + `PageInfo`，一行代码分页

### 核心记忆点
```
动态 SQL：<if> <where> <set> <foreach> <choose> <sql> <bind>
一对一：<association property="author" javaType="User">
一对多：<collection property="comments" ofType="Comment">
分步查询：column="user_id" select="xxx.findById"
延迟加载：fetchType="lazy" + lazyLoadingEnabled=true
一级缓存：SqlSession 内自动
二级缓存：<cache/> + implements Serializable
分页：PageHelper.startPage(page, size) → PageInfo
```

### MyBatis 待改进
- 手动管理 SqlSession 和事务
- 配置繁琐（XML 多）
- 与 Spring 整合后可解决（Day13）

### 拓展阅读
- [MyBatis 动态 SQL 官方文档](https://mybatis.org/mybatis-3/zh/dynamic-sql.html)
- [MyBatis 缓存机制](https://mybatis.org/mybatis-3/zh/sqlmap-xml.html#cache)
- [PageHelper 官方文档](https://pagehelper.github.io/)
- [MyBatis-Plus](https://baomidou.com/)（简化 CRUD）

### 明日预告
Day11 将学习 **Spring 核心**：IoC（控制反转）、DI（依赖注入）、Bean 管理、注解配置、AOP（面向切面），理解 Spring 如何管理对象和事务，为 SSM 整合打基础。**