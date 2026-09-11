# 第1周 

## 学习目标
- 掌握 SQL 分类与核心语法
- 熟练编写 DDL 建表语句与约束
- 掌握多表查询（连接、子查询、聚合）
- 理解索引原理与优化（最左前缀、索引失效）
- 理解事务 ACID 与隔离级别
- 掌握数据库设计规范（三大范式）
- 完成博客系统的数据库设计

---

## 一、SQL 概述

### 1. SQL 是什么

SQL（Structured Query Language，结构化查询语言）是用于操作关系型数据库的标准语言。几乎所有关系型数据库（MySQL、Oracle、PostgreSQL、SQL Server）都支持 SQL。

### 2. SQL 分类

| 类别 | 全称 | 作用 | 核心语句 |
|------|------|------|----------|
| **DDL** | Data Definition Language | 定义数据库结构 | CREATE、ALTER、DROP、TRUNCATE |
| **DML** | Data Manipulation Language | 操作数据 | INSERT、UPDATE、DELETE |
| **DQL** | Data Query Language | 查询数据 | SELECT |
| **DCL** | Data Control Language | 权限控制 | GRANT、REVOKE |
| **TCL** | Transaction Control Language | 事务控制 | COMMIT、ROLLBACK、SAVEPOINT |

### 3. MySQL 数据类型

**数值类型：**
| 类型 | 字节 | 范围 | 用途 |
|------|------|------|------|
| TINYINT | 1 | -128~127 | 状态、布尔 |
| SMALLINT | 2 | -32768~32767 | 小整数 |
| INT | 4 | ±21 亿 | 常用整数 |
| BIGINT | 8 | 极大 | 主键 ID |
| DECIMAL(M,D) | - | 精确小数 | 金额 |
| FLOAT/DOUBLE | 4/8 | 浮点 | 科学计算 |

> 💡 **金额必须用 DECIMAL**，FLOAT/DOUBLE 有精度问题。

**字符串类型：**
| 类型 | 说明 |
|------|------|
| CHAR(n) | 定长，最多 255，适合固定长度（如性别、MD5） |
| VARCHAR(n) | 变长，最多 65535，常用 |
| TEXT | 长文本，最多 64KB |
| MEDIUMTEXT | 16MB |
| LONGTEXT | 4GB |
| BLOB | 二进制大对象（图片、文件） |

> 💡 VARCHAR 长度按需设置，过长浪费索引空间；超长文本用 TEXT。

**日期时间类型：**
| 类型 | 格式 | 范围 |
|------|------|------|
| DATE | YYYY-MM-DD | 1000-01-01 ~ 9999-12-31 |
| TIME | HH:MM:SS | -838:59:59 ~ 838:59:59 |
| DATETIME | YYYY-MM-DD HH:MM:SS | 1000 ~ 9999 |
| TIMESTAMP | 时间戳 | 1970 ~ 2038 |
| YEAR | YYYY | 1901 ~ 2155 |

> 💡 DATETIME 与时区无关，TIMESTAMP 随时区变化。推荐 DATETIME。

---

## 二、DDL 数据定义

### 1. 数据库操作

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS mydb
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE mydb;

-- 查看所有数据库
SHOW DATABASES;

-- 查看建库语句
SHOW CREATE DATABASE mydb;

-- 删除数据库（危险！）
DROP DATABASE IF EXISTS mydb;

-- 修改字符集
ALTER DATABASE mydb CHARACTER SET utf8mb4;
```

> 💡 **utf8mb4 vs utf8**：MySQL 的 `utf8` 最多 3 字节，存不了 emoji；`utf8mb4` 是真正的 UTF-8（4 字节），**推荐使用**。

### 2. 表操作

```sql
-- 创建表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    age INT COMMENT '年龄',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 查看表结构
DESC user;
SHOW CREATE TABLE user;

-- 修改表
ALTER TABLE user ADD COLUMN phone VARCHAR(20) AFTER email;
ALTER TABLE user MODIFY COLUMN age TINYINT;
ALTER TABLE user CHANGE COLUMN phone mobile VARCHAR(20);
ALTER TABLE user DROP COLUMN mobile;
ALTER TABLE user RENAME TO sys_user;

-- 删除表
DROP TABLE IF EXISTS user;

-- 清空表（保留结构，不可回滚，自增重置）
TRUNCATE TABLE user;
```

> ⚠️ **DROP vs TRUNCATE vs DELETE**：
> | 操作 | 类型 | 可回滚 | 自增重置 | 速度 |
> |------|------|--------|----------|------|
> | DELETE | DML | ✅ | ❌ | 慢（逐行） |
> | TRUNCATE | DDL | ❌ | ✅ | 快 |
> | DROP | DDL | ❌ | - | 最快（删表） |

### 3. 约束

| 约束 | 关键字 | 作用 |
|------|--------|------|
| 主键 | PRIMARY KEY | 唯一标识，非空且唯一 |
| 自增 | AUTO_INCREMENT | 主键自动递增 |
| 非空 | NOT NULL | 不允许为 NULL |
| 唯一 | UNIQUE | 值唯一，可为 NULL |
| 默认 | DEFAULT | 默认值 |
| 检查 | CHECK | 值满足条件（MySQL 8.0.16+ 生效） |
| 外键 | FOREIGN KEY | 关联另一张表 |

**完整示例：**
```sql
CREATE TABLE article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    views INT DEFAULT 0 CHECK (views >= 0),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    -- 外键约束
    CONSTRAINT fk_article_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES category(id),
    -- 唯一约束
    CONSTRAINT uk_title UNIQUE (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**外键的级联操作：**
```sql
FOREIGN KEY (user_id) REFERENCES user(id)
    ON DELETE CASCADE      -- 主表删除，子表也删
    ON UPDATE CASCADE      -- 主表更新，子表跟随
```

> 💡 **互联网公司常禁用外键**：外键影响性能、不利分库分表，通常在应用层保证一致性。

### 4. 三大范式

**第一范式（1NF）：字段原子性**
- 每个字段不可再分
- ❌ 反例：`address = "北京市朝阳区xxx"` 拆成省市区？不一定，看需求

**第二范式（2NF）：消除部分依赖**
- 在 1NF 基础上，非主键字段完全依赖主键
- ❌ 反例：订单明细表 `(订单ID, 商品ID, 商品名, 数量)`，商品名只依赖商品ID，不依赖订单ID

**第三范式（3NF）：消除传递依赖**
- 在 2NF 基础上，非主键字段不依赖其他非主键字段
- ❌ 反例：`(员工ID, 部门ID, 部门名)`，部门名依赖部门ID（传递依赖）

**反范式设计：**
- 为性能可适度冗余（如文章表冗余作者名）
- 阿里规范："**适度冗余，以空间换时间**"

### 5. 表关系设计

**一对一：** 用户表 + 用户详情表，主键相同或外键唯一
**一对多：** 用户 → 文章，文章表存 `user_id`
**多对多：** 文章 ↔ 标签，需要中间表 `article_tag(article_id, tag_id)`

```sql
-- 多对多中间表
CREATE TABLE article_tag (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES article(id),
    FOREIGN KEY (tag_id) REFERENCES tag(id)
);
```

---

## 三、DML 数据操作

### 1. 插入

```sql
-- 单条插入
INSERT INTO user (username, age, email) VALUES ('张三', 25, 'zs@example.com');

-- 省略字段（按顺序全填）
INSERT INTO user VALUES (NULL, '李四', 30, 'ls@example.com', 1, NOW(), NOW());

-- 批量插入（推荐，性能高）
INSERT INTO user (username, age, email) VALUES
    ('王五', 28, 'ww@example.com'),
    ('赵六', 35, 'zl@example.com'),
    ('钱七', 22, 'qq@example.com');

-- 插入或更新（主键/唯一键冲突时更新）
INSERT INTO user (id, username, age) VALUES (1, '张三', 26)
    ON DUPLICATE KEY UPDATE age = VALUES(age);

-- 忽略冲突
INSERT IGNORE INTO user (username, email) VALUES ('张三', 'zs@example.com');
```

### 2. 更新

```sql
-- 更新单条
UPDATE user SET age = 26 WHERE id = 1;

-- 更新多条
UPDATE user SET status = 0 WHERE age > 60;

-- 更新多个字段
UPDATE user SET age = 30, email = 'new@example.com' WHERE id = 1;

-- 使用表达式
UPDATE article SET views = views + 1 WHERE id = 100;

-- ⚠️ 不加 WHERE 会更新全表！
```

### 3. 删除

```sql
-- 删除指定行
DELETE FROM user WHERE id = 1;

-- 删除多条
DELETE FROM user WHERE status = 0 AND create_time < '2024-01-01';

-- ⚠️ 不加 WHERE 会删除全表！
```

### 4. 安全建议

- 执行 UPDATE/DELETE 前先用 SELECT 验证条件
- 生产环境开启 `sql_safe_updates`（无 WHERE 不允许更新）
- 重要操作先备份
- 用事务包裹，验证后再提交

---

## 四、DQL 查询

### 1. 基础查询

```sql
-- 查询所有列
SELECT * FROM user;

-- 查询指定列
SELECT id, username, age FROM user;

-- 别名
SELECT username AS name, age AS 年龄 FROM user;

-- 去重
SELECT DISTINCT status FROM user;

-- 限制行数
SELECT * FROM user LIMIT 10;              -- 前 10 条
SELECT * FROM user LIMIT 10 OFFSET 20;    -- 跳过 20，取 10（第 3 页，每页 10）
SELECT * FROM user LIMIT 20, 10;          -- 等价写法
```

### 2. WHERE 条件

```sql
-- 比较
SELECT * FROM user WHERE age > 18;
SELECT * FROM user WHERE age >= 18 AND age <= 60;
SELECT * FROM user WHERE age BETWEEN 18 AND 60;

-- 逻辑
SELECT * FROM user WHERE age > 18 AND status = 1;
SELECT * FROM user WHERE age < 18 OR age > 60;
SELECT * FROM user WHERE NOT status = 0;

-- IN / NOT IN
SELECT * FROM user WHERE id IN (1, 2, 3);
SELECT * FROM user WHERE status NOT IN (0, -1);

-- NULL 判断（不能用 =）
SELECT * FROM user WHERE email IS NULL;
SELECT * FROM user WHERE email IS NOT NULL;

-- 模糊查询
SELECT * FROM user WHERE username LIKE '张%';    -- 以张开头的，走索引
SELECT * FROM user WHERE username LIKE '%张';    -- 包含张，不走索引
SELECT * FROM user WHERE username LIKE '%张%';   -- 不走索引
SELECT * FROM user WHERE username LIKE '_三';    -- _ 匹配单个字符

-- 正则（性能差，慎用）
SELECT * FROM user WHERE username REGEXP '^张';
```

### 3. 排序

```sql
SELECT * FROM user ORDER BY age;             -- 升序（默认 ASC）
SELECT * FROM user ORDER BY age DESC;        -- 降序
SELECT * FROM user ORDER BY age DESC, id ASC; -- 多字段
SELECT * FROM user ORDER BY age DESC LIMIT 10; -- Top 10
```

### 4. 聚合函数

| 函数 | 作用 |
|------|------|
| COUNT() | 计数 |
| SUM() | 求和 |
| AVG() | 平均 |
| MAX() | 最大 |
| MIN() | 最小 |

```sql
SELECT COUNT(*) FROM user;                    -- 总行数
SELECT COUNT(email) FROM user;                -- 非 NULL 的 email 数
SELECT SUM(views) FROM article;               -- 总浏览量
SELECT AVG(age) FROM user;                    -- 平均年龄
SELECT MAX(age), MIN(age) FROM user;          -- 最大最小
```

> 💡 **COUNT(*) vs COUNT(1) vs COUNT(字段)**：
> - `COUNT(*)` 和 `COUNT(1)` 统计所有行
> - `COUNT(字段)` 不统计 NULL
> - InnoDB 中 `COUNT(*)` 有优化，性能不差

### 5. 分组

```sql
-- 按状态分组统计
SELECT status, COUNT(*) AS cnt
FROM user
GROUP BY status;

-- 分组后过滤（HAVING）
SELECT status, COUNT(*) AS cnt
FROM user
GROUP BY status
HAVING cnt > 5;

-- 多字段分组
SELECT status, YEAR(create_time) AS y, COUNT(*)
FROM user
GROUP BY status, y;

-- ⚠️ SELECT 的字段必须在 GROUP BY 中，或是聚合函数
```

> 💡 **WHERE vs HAVING**：
> - WHERE 在分组**前**过滤，不能用于聚合函数
> - HAVING 在分组**后**过滤，可用聚合函数

### 6. 连接查询

**内连接（INNER JOIN）：** 两表都匹配才返回
```sql
SELECT u.username, a.title
FROM user u
INNER JOIN article a ON u.id = a.user_id;
```

**左连接（LEFT JOIN）：** 左表全返回，右表无匹配填 NULL
```sql
SELECT u.username, a.title
FROM user u
LEFT JOIN article a ON u.id = a.user_id;
-- 结果：没有文章的用户也会出现，title 为 NULL
```

**右连接（RIGHT JOIN）：** 右表全返回
```sql
SELECT u.username, a.title
FROM user u
RIGHT JOIN article a ON u.id = a.user_id;
```

**全连接（FULL JOIN）：** MySQL 不直接支持，用 UNION 模拟
```sql
SELECT u.username, a.title FROM user u LEFT JOIN article a ON u.id = a.user_id
UNION
SELECT u.username, a.title FROM user u RIGHT JOIN article a ON u.id = a.user_id;
```

**自连接：** 表与自身连接（如查员工及其上级）
```sql
SELECT e.name AS 员工, m.name AS 上级
FROM employee e
LEFT JOIN employee m ON e.manager_id = m.id;
```

**连接对比：**
| 类型 | 说明 |
|------|------|
| INNER JOIN | 交集 |
| LEFT JOIN | 左表全部 + 交集 |
| RIGHT JOIN | 右表全部 + 交集 |
| FULL JOIN | 并集 |

### 7. 子查询

```sql
-- 标量子查询（返回单值）
SELECT * FROM user WHERE age > (SELECT AVG(age) FROM user);

-- 列子查询（IN）
SELECT * FROM user WHERE id IN (
    SELECT user_id FROM article WHERE views > 1000
);

-- 表子查询（FROM 中）
SELECT t.username, t.cnt
FROM (
    SELECT u.username, COUNT(a.id) AS cnt
    FROM user u LEFT JOIN article a ON u.id = a.user_id
    GROUP BY u.id
) t
WHERE t.cnt > 5;

-- EXISTS（相关子查询）
SELECT * FROM user u
WHERE EXISTS (
    SELECT 1 FROM article a WHERE a.user_id = u.id
);
```

### 8. UNION

```sql
-- UNION：去重合并
SELECT username FROM user WHERE age < 18
UNION
SELECT username FROM user WHERE age > 60;

-- UNION ALL：不去重，性能好
SELECT username FROM user WHERE age < 18
UNION ALL
SELECT username FROM user WHERE age > 60;
```

---

## 五、索引

### 1. 索引是什么

索引是**帮助 MySQL 高效获取数据的数据结构**（通常是 B+ 树），类似书的目录。

**优点：** 加速查询、加速排序和分组
**缺点：** 占空间、降低写入速度（增删改需维护索引）

### 2. 索引类型

| 类型 | 关键字 | 说明 |
|------|--------|------|
| 主键索引 | PRIMARY KEY | 唯一且非空，一张表一个 |
| 唯一索引 | UNIQUE | 值唯一，可为 NULL |
| 普通索引 | INDEX / KEY | 加速查询 |
| 联合索引 | INDEX(a, b, c) | 多列组合 |
| 全文索引 | FULLTEXT | 文本搜索（中文需 ngram） |

### 3. 索引操作

```sql
-- 创建索引
CREATE INDEX idx_username ON user(username);
CREATE UNIQUE INDEX uk_email ON user(email);
CREATE INDEX idx_name_age ON user(username, age);  -- 联合索引

-- 建表时创建
CREATE TABLE user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50),
    INDEX idx_username (username)
);

-- 修改表添加
ALTER TABLE user ADD INDEX idx_age (age);

-- 查看索引
SHOW INDEX FROM user;

-- 删除索引
DROP INDEX idx_username ON user;
ALTER TABLE user DROP INDEX idx_age;
```

### 4. 联合索引与最左前缀

**联合索引 `(a, b, c)` 相当于创建了：**
- `(a)`
- `(a, b)`
- `(a, b, c)`

**最左前缀原则：**
```sql
-- ✅ 走索引
WHERE a = 1
WHERE a = 1 AND b = 2
WHERE a = 1 AND b = 2 AND c = 3

-- ❌ 不走索引（跳过 a）
WHERE b = 2
WHERE c = 3
WHERE b = 2 AND c = 3

-- ⚠️ 部分走索引
WHERE a = 1 AND c = 3   -- 只用到 a
WHERE a = 1 AND b > 2 AND c = 3  -- 范围后失效，c 用不到
```

> 💡 联合索引中，**范围查询后的字段索引失效**。

### 5. 索引失效场景

| 场景 | 示例 | 原因 |
|------|------|------|
| 函数操作 | `WHERE YEAR(create_time) = 2024` | 索引列被计算 |
| 运算 | `WHERE age + 1 = 20` | 同上 |
| 类型转换 | `WHERE phone = 13800138000`（phone 是 varchar） | 隐式转换 |
| 前导通配 | `WHERE name LIKE '%张'` | 无法定位 |
| OR 连接非索引列 | `WHERE a = 1 OR b = 2`（b 无索引） | 全表扫描 |
| 不等于 | `WHERE status != 1` | 优化器判断全表更快 |
| IS NULL / IS NOT NULL | 视情况 | 取决于 NULL 比例 |
| 使用 SELECT * | 覆盖索引失效 | 需回表 |

**优化建议：**
```sql
-- ❌ 函数
WHERE YEAR(create_time) = 2024
-- ✅ 范围
WHERE create_time >= '2024-01-01' AND create_time < '2025-01-01'

-- ❌ 前导通配
WHERE name LIKE '%张'
-- ✅ 后缀通配（走索引）
WHERE name LIKE '张%'

-- ❌ 类型转换
WHERE phone = 13800138000
-- ✅
WHERE phone = '13800138000'
```

### 6. EXPLAIN 执行计划

```sql
EXPLAIN SELECT * FROM user WHERE username = '张三';
```

**关键字段：**
| 字段 | 说明 |
|------|------|
| id | 查询序号，越大越先执行 |
| select_type | 查询类型（SIMPLE、PRIMARY、SUBQUERY） |
| table | 表名 |
| type | 访问类型（**关键**） |
| possible_keys | 可能用到的索引 |
| key | 实际用的索引 |
| key_len | 索引使用长度 |
| rows | 预估扫描行数 |
| Extra | 额外信息 |

**type 从好到坏：**
```
system > const > eq_ref > ref > range > index > ALL
```
- `const`：主键/唯一索引等值查询
- `ref`：普通索引等值查询
- `range`：范围查询
- `index`：全索引扫描
- `ALL`：**全表扫描，需要优化**

**Extra 关键值：**
- `Using index`：覆盖索引，好
- `Using where`：需回表过滤
- `Using filesort`：需额外排序，考虑加索引
- `Using temporary`：用临时表，性能差

### 7. 覆盖索引

**覆盖索引：** 查询的字段全部在索引中，无需回表。
```sql
-- 索引 (username, age)
SELECT username, age FROM user WHERE username = '张三';  -- 覆盖索引
SELECT * FROM user WHERE username = '张三';              -- 需回表
```

### 8. 索引设计原则

1. **WHERE、ORDER BY、GROUP BY 涉及的列**考虑建索引
2. **区分度高的列**优先（如手机号 vs 性别）
3. **联合索引把等值查询放前面**，范围放后面
4. **避免过多索引**（一般不超过 5-6 个）
5. **频繁更新的列慎建索引**
6. **小表不需要索引**（全表扫描更快）
7. **用覆盖索引减少回表**

---

## 六、事务

### 1. 事务是什么

事务是一组**原子性**的 SQL 操作，要么全部成功，要么全部失败。

**经典例子：** 转账
```sql
START TRANSACTION;
UPDATE account SET balance = balance - 100 WHERE id = 1;
UPDATE account SET balance = balance + 100 WHERE id = 2;
COMMIT;
-- 若中途出错，ROLLBACK
```

### 2. ACID 特性

| 特性 | 说明 | 实现 |
|------|------|------|
| **A 原子性** | 要么全成功，要么全失败 | undo log |
| **C 一致性** | 事务前后数据一致 | 由其他三个保证 |
| **I 隔离性** | 并发事务互不干扰 | 锁 + MVCC |
| **D 持久性** | 提交后永久保存 | redo log |

### 3. 事务操作

```sql
-- 开启事务
START TRANSACTION;
-- 或
BEGIN;

-- 提交
COMMIT;

-- 回滚
ROLLBACK;

-- 保存点
SAVEPOINT sp1;
-- ... 操作 ...
ROLLBACK TO sp1;  -- 回滚到保存点

-- 查看自动提交状态
SHOW VARIABLES LIKE 'autocommit';
-- 关闭自动提交
SET autocommit = 0;
```

### 4. 并发问题

| 问题 | 说明 |
|------|------|
| **脏读** | 读到其他事务未提交的数据 |
| **不可重复读** | 同一事务内两次读同一行，结果不同（其他事务修改并提交） |
| **幻读** | 同一事务内两次范围查询，行数不同（其他事务插入并提交） |

### 5. 隔离级别

| 隔离级别 | 脏读 | 不可重复读 | 幻读 |
|----------|------|------------|------|
| READ UNCOMMITTED | ✅ | ✅ | ✅ |
| READ COMMITTED | ❌ | ✅ | ✅ |
| REPEATABLE READ（MySQL 默认） | ❌ | ❌ | ✅（MySQL 用间隙锁解决） |
| SERIALIZABLE | ❌ | ❌ | ❌ |

```sql
-- 查看隔离级别
SELECT @@transaction_isolation;

-- 设置隔离级别
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

**选择建议：**
- 大多数场景用 **REPEATABLE READ**（MySQL 默认，安全）
- 高并发场景可用 **READ COMMITTED**（如 Oracle 默认）

### 6. MVCC 简介

MVCC（Multi-Version Concurrency Control，多版本并发控制）通过**版本链 + undo log + ReadView**实现：
- 读操作不加锁，读历史版本
- 写操作加锁
- 实现"读写不阻塞"

**当前读 vs 快照读：**
- 快照读：普通 SELECT，读 MVCC 快照
- 当前读：`SELECT ... FOR UPDATE`、`UPDATE`、`DELETE`，读最新版本并加锁

### 7. 锁

**按粒度：**
- 表锁：开销小，并发低
- 行锁：开销大，并发高（InnoDB 支持）

**按类型：**
- 共享锁（S 锁）：`SELECT ... LOCK IN SHARE MODE`
- 排他锁（X 锁）：`SELECT ... FOR UPDATE`、`UPDATE`、`DELETE`

**间隙锁（Gap Lock）：** RR 级别下防止幻读，锁住范围间隙。

**死锁：** 两个事务互相等待对方持有的锁。MySQL 会自动检测并回滚其中一个。

**避免死锁：**
- 按固定顺序访问资源
- 事务尽量短小
- 减少锁范围

---

## 七、实战：博客系统数据库设计

### 1. 需求分析

**功能：**
- 用户注册登录
- 发布文章（含分类、标签）
- 评论文章
- 文章点赞、收藏

### 2. E-R 关系

```
用户 1 ──── N 文章
用户 1 ──── N 评论
分类 1 ──── N 文章
文章 N ──── N 标签（中间表）
文章 1 ──── N 评论
```

### 3. 建表 SQL

```sql
-- 数据库
CREATE DATABASE IF NOT EXISTS blog
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE blog;

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像 URL',
    `email` VARCHAR(100) COMMENT '邮箱',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 标签表
CREATE TABLE `tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 文章表
CREATE TABLE `article` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` LONGTEXT NOT NULL COMMENT '内容',
    `summary` VARCHAR(500) COMMENT '摘要',
    `cover` VARCHAR(255) COMMENT '封面图',
    `user_id` BIGINT NOT NULL COMMENT '作者 ID',
    `category_id` BIGINT COMMENT '分类 ID',
    `views` INT DEFAULT 0 COMMENT '浏览量',
    `likes` INT DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1发布 0草稿 2删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status_create` (`status`, `create_time`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 文章标签关联表（多对多）
CREATE TABLE `article_tag` (
    `article_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`article_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- 评论表（支持嵌套评论）
CREATE TABLE `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL COMMENT '文章 ID',
    `user_id` BIGINT NOT NULL COMMENT '评论者 ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论 ID（NULL 为顶层）',
    `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1正常 0删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_article_id` (`article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 点赞表（用户对文章的点赞，防重复）
CREATE TABLE `like_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `article_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';
```

### 4. 初始化数据

```sql
INSERT INTO category (name, sort) VALUES
    ('Java', 1), ('数据库', 2), ('前端', 3), ('算法', 4);

INSERT INTO `user` (username, password, nickname) VALUES
    ('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员'),
    ('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三');

INSERT INTO article (title, content, summary, user_id, category_id) VALUES
    ('Java 入门', 'Java 是一门...', 'Java 基础介绍', 1, 1),
    ('MySQL 索引原理', 'B+ 树...', '索引详解', 1, 2),
    ('Vue 实战', 'Vue3...', 'Vue 教程', 2, 3);

INSERT INTO tag (name) VALUES ('Java'), ('MySQL'), ('Vue'), ('入门');
INSERT INTO article_tag (article_id, tag_id) VALUES (1, 1), (1, 4), (2, 2), (3, 3);
```

### 5. 常用业务查询

**查询文章列表（带作者、分类）：**
```sql
SELECT a.id, a.title, a.summary, a.views, a.create_time,
       u.nickname AS author, c.name AS category
FROM article a
LEFT JOIN `user` u ON a.user_id = u.id
LEFT JOIN category c ON a.category_id = c.id
WHERE a.status = 1
ORDER BY a.create_time DESC
LIMIT 10 OFFSET 0;
```

**查询文章详情（带标签）：**
```sql
SELECT a.*, u.nickname AS author, c.name AS category,
       GROUP_CONCAT(t.name) AS tags
FROM article a
LEFT JOIN `user` u ON a.user_id = u.id
LEFT JOIN category c ON a.category_id = c.id
LEFT JOIN article_tag at ON a.id = at.article_id
LEFT JOIN tag t ON at.tag_id = t.id
WHERE a.id = 1
GROUP BY a.id;
```

**查询每篇文章的评论数：**
```sql
SELECT a.id, a.title, COUNT(c.id) AS comment_count
FROM article a
LEFT JOIN comment c ON a.id = c.article_id AND c.status = 1
WHERE a.status = 1
GROUP BY a.id
ORDER BY comment_count DESC;
```

**查询热门文章（按点赞数）：**
```sql
SELECT a.id, a.title, a.likes, a.views
FROM article a
WHERE a.status = 1
ORDER BY a.likes DESC, a.views DESC
LIMIT 10;
```

**查询评论树（含回复）：**
```sql
SELECT c.*, u.nickname AS commenter,
       p.content AS parent_content, pu.nickname AS parent_user
FROM comment c
LEFT JOIN `user` u ON c.user_id = u.id
LEFT JOIN comment p ON c.parent_id = p.id
LEFT JOIN `user` pu ON p.user_id = pu.id
WHERE c.article_id = 1 AND c.status = 1
ORDER BY c.create_time;
```

**统计每个分类的文章数：**
```sql
SELECT c.id, c.name, COUNT(a.id) AS article_count
FROM category c
LEFT JOIN article a ON c.id = a.category_id AND a.status = 1
GROUP BY c.id
ORDER BY c.sort;
```

**搜索文章（标题或内容）：**
```sql
-- 普通 LIKE（小数据量）
SELECT id, title, summary FROM article
WHERE status = 1 AND (title LIKE '%Java%' OR content LIKE '%Java%')
ORDER BY create_time DESC;

-- 全文索引（大数据量，中文需 ngram）
SELECT id, title, MATCH(title, content) AGAINST('Java' IN NATURAL LANGUAGE MODE) AS score
FROM article
WHERE MATCH(title, content) AGAINST('Java' IN NATURAL LANGUAGE MODE)
ORDER BY score DESC;
```

---

## 八、常见问题与优化

### 1. 慢查询排查

```sql
-- 查看慢查询日志是否开启
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- 开启慢查询（临时）
SET GLOBAL slow_query_log = ON;
SET GLOBAL long_query_time = 1; -- 超过 1 秒记录

-- 查看慢查询日志位置
SHOW VARIABLES LIKE 'slow_query_log_file';
```

### 2. SQL 优化技巧

| 技巧 | 说明 |
|------|------|
| 用 EXPLAIN 分析 | 关注 type、key、rows、Extra |
| 避免 SELECT * | 只查需要的列，可用覆盖索引 |
| 用 LIMIT 限制 | 避免返回大量数据 |
| 用 JOIN 代替子查询 | 通常 JOIN 更快 |
| 用 EXISTS 代替 IN | 大表时 EXISTS 更优 |
| 批量操作 | 用批量 INSERT 代替循环单条 |
| 分页优化 | 大 OFFSET 用 `WHERE id > ?` 代替 |
| 避免函数操作索引列 | 如 `WHERE DATE(create_time) = ...` |

**分页优化示例：**
```sql
-- ❌ 慢：OFFSET 很大时扫描大量行
SELECT * FROM article ORDER BY id LIMIT 1000000, 10;

-- ✅ 快：记住上一页最后 ID
SELECT * FROM article WHERE id > 1000000 ORDER BY id LIMIT 10;
```

### 3. 常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| 中文乱码 | 字符集不一致 | 库、表、连接都用 utf8mb4 |
| 连接超时 | 连接泄漏 | 用连接池，及时关闭 |
| 死锁 | 加锁顺序不一致 | 固定顺序，短事务 |
| 主从延迟 | 复制异步 | 关键读走主库 |
| 索引失效 | 函数、类型转换 | 参考索引失效场景 |
| 数据量过大 | 单表千万级 | 分库分表、归档 |

---

## 九、实践练习

### 练习 1：建表与约束
1. 创建 `student` 表（id, name, age, gender, class_id）
2. 创建 `class` 表（id, name, teacher）
3. 添加外键约束
4. 插入测试数据

### 练习 2：基础查询
- 查询年龄 > 18 的学生
- 按年龄降序排列
- 统计每个班级的学生数
- 查询平均年龄

### 练习 3：多表连接
- 查询学生及其班级名
- 查询没有学生的班级
- 查询每个班级年龄最大的学生

### 练习 4：索引优化
1. 给 `student.name` 建索引
2. 用 EXPLAIN 查看 `WHERE name = 'xxx'` 的执行计划
3. 对比加索引前后的 type 和 rows
4. 尝试让索引失效（用函数），观察变化

### 练习 5：事务验证
1. 创建 `account` 表
2. 开启事务，转账，中途 ROLLBACK，验证数据未变
3. 模拟并发，观察不同隔离级别的现象

### 练习 6：博客系统完善
- 在博客系统基础上，增加"收藏"功能表
- 增加"关注用户"功能表（多对多）
- 编写查询：某用户关注的人的最新文章

---

## 十、总结与拓展

### 今日总结
- **SQL 分五类**：DDL、DML、DQL、DCL、TCL
- **数据类型**：整数用 BIGINT，金额用 DECIMAL，字符串用 VARCHAR，长文本用 TEXT
- **约束**：主键、外键、唯一、非空、默认、检查
- **三大范式**：原子性、消除部分依赖、消除传递依赖，可适度反范式
- **查询**：WHERE、ORDER BY、GROUP BY、HAVING、JOIN、子查询、UNION
- **索引**：加速查询，注意最左前缀和失效场景，用 EXPLAIN 分析
- **事务**：ACID，四种隔离级别，MySQL 默认 RR
- **设计**：合理选型、适度冗余、规范命名、按需索引

### 核心记忆点
```
建表：CREATE TABLE ... ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
查询：SELECT ... FROM ... JOIN ... WHERE ... GROUP BY ... HAVING ... ORDER BY ... LIMIT
索引：联合索引 (a,b,c) 支持 a、ab、abc，不支持 bc
失效：函数、运算、类型转换、前导通配
事务：START TRANSACTION → COMMIT / ROLLBACK
隔离：RU < RC < RR（默认） < SERIALIZABLE
优化：EXPLAIN 看 type（避免 ALL）、key、rows、Extra
分页：大 OFFSET 用 WHERE id > ?
```

### 拓展阅读
- [MySQL 官方文档](https://dev.mysql.com/doc/)
- [MySQL 索引原理（B+ 树）](https://dev.mysql.com/doc/refman/8.0/en/innodb-index-types.html)
- [《高性能 MySQL》](https://book.douban.com/subject/23008813/)
- [阿里 Java 开发手册（数据库部分）](https://github.com/alibaba/p3c)

### 明日预告
Day 7 将进行 **综合实战：用户管理系统**，整合 Day1-6 所学（Servlet + JSP + JDBC + MySQL），完成一个带分页、搜索、登录拦截的完整 CRUD 项目。