# 电商系统（eshop）数据库设计文档（PostgreSQL）

> 说明：
>
> * 主键统一使用 `UUID + gen_random_uuid()`
> * 所有时间字段使用 `TIMESTAMPTZ`
> * 文本型扩展信息统一使用 `JSONB`
> * 评论相关表：`t_comment` / `t_comment_topic` / `t_comment_topic_mapping` / `t_comment_summary_daily`

## 1. 设计目标

1. 支撑电商核心业务：用户、商品、订单、支付、发货。
2. 支持用户对商品的评论与多维度分析（评价内容、评分等）。

## 2. 表清单

| 表名                          | 说明                 |
| --------------------------- | ------------------ |
| `t_app_user`                  | 用户表                |
| `t_role`                      | 角色表（管理员、客服等）       |
| `t_user_role`                 | 用户-角色关联表           |
| `t_product_category`          | 商品类目               |
| `t_product`                   | 商品主表               |
| `t_product_category_relation` | 商品-类目多对多关联         |
| `t_order_header`              | 订单主表               |
| `t_order_item`                | 订单明细               |
| `t_payment`                   | 支付记录               |
| `t_shipment`                  | 发货记录               |
| `t_comment_topic`             | 评论话题定义（“物流”、“质量”等） |
| `t_comment`                   | **评论表，核心分析对象**     |
| `t_comment_topic_mapping`     | 评论-话题关联表（多对多）      |
| `t_comment_summary_daily`     | 评论日汇总表，用于加速统计      |
| `t_system_kv`                 | 系统配置 / 开关表         |

---

## 3. 表结构设计（分表说明 + DDL）

### 3.1 用户表 `t_app_user`

**用途**：
存储系统用户（买家、卖家、运营、客服等）的基础信息。

**字段要点**：

* `id`: 用户主键 UUID
* `email`: 登录邮箱，唯一
* `phone`: 手机号
* `display_name`: 显示昵称
* `status`: 用户状态（active / blocked / deleted）
* `metadata`: 扩展信息（渠道、终端等）

```sql
CREATE TABLE t_app_user (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) UNIQUE NOT NULL,
    phone           VARCHAR(32),
    display_name    VARCHAR(128) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'active', -- active / blocked / deleted
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_app_user IS '用户表：存储系统用户的基础信息';
COMMENT ON COLUMN t_app_user.email IS '登录邮箱，唯一';
COMMENT ON COLUMN t_app_user.display_name IS '用户显示昵称';
COMMENT ON COLUMN t_app_user.status IS '用户状态：active / blocked / deleted';
```

### 3.2 角色表 `t_role`

**用途**：

定义系统角色，如管理员、客服、运营等。

```sql
CREATE TABLE t_role (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(64) UNIQUE NOT NULL,
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_role IS '角色表：定义系统中的角色（管理员、客服等）';
```

### 3.3 用户-角色关联表 `t_user_role`

**用途**：
支持一个用户拥有多个角色。

```sql
CREATE TABLE t_user_role (
    user_id     UUID NOT NULL,
    role_id     UUID NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES t_app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES t_role(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_role IS '用户-角色关联表，多对多';
```

### 3.4 商品类目表 `t_product_category`

**用途**：
维护商品的多级分类。

```sql
CREATE TABLE t_product_category (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id       UUID,
    name            VARCHAR(128) NOT NULL,
    code            VARCHAR(64) UNIQUE NOT NULL,
    level           INT NOT NULL DEFAULT 1,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (parent_id) REFERENCES t_product_category(id) ON DELETE SET NULL
);

COMMENT ON TABLE t_product_category IS '商品类目表，支持多级分类';
```

### 3.5 商品表 `t_product`

**用途**：
商品主数据，评论与订单均需要关联到商品。

**字段要点**：

* `rating_avg`: 平均评分，冗余字段
* `rating_count`: 参与评分的评论数量

```sql
CREATE TABLE t_product (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku             VARCHAR(64) UNIQUE NOT NULL,
    name            VARCHAR(255) NOT NULL,
    subtitle        VARCHAR(255),
    description     TEXT,
    price           NUMERIC(12, 2) NOT NULL,
    stock_quantity  INT NOT NULL DEFAULT 0,
    status          VARCHAR(32) NOT NULL DEFAULT 'on_sale', -- on_sale / off_sale / deleted
    rating_avg      NUMERIC(3, 2) DEFAULT 0.0,
    rating_count    INT NOT NULL DEFAULT 0,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_product IS '商品表：商品主数据';
COMMENT ON COLUMN t_product.rating_avg IS '商品平均评分（冗余，便于快速查询）';
```

### 3.6 商品-类目关联表 `t_product_category_relation`

**用途**：
商品与类目多对多关系。

```sql
CREATE TABLE t_product_category_relation (
    product_id      UUID NOT NULL,
    category_id     UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (product_id, category_id),
    FOREIGN KEY (product_id) REFERENCES t_product(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES t_product_category(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_product_category_relation IS '商品-类目多对多关联';
```

### 3.7 订单主表 `t_order_header`

**用途**：
电商订单信息，用于关联评论（比如"仅允许已购买用户评论"）。

```sql
CREATE TABLE t_order_header (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_no        VARCHAR(64) UNIQUE NOT NULL,
    user_id         UUID NOT NULL,
    status          VARCHAR(32) NOT NULL, -- created / paid / shipped / completed / canceled
    total_amount    NUMERIC(12, 2) NOT NULL,
    pay_amount      NUMERIC(12, 2) NOT NULL,
    currency        VARCHAR(16) NOT NULL DEFAULT 'CNY',
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES t_app_user(id) ON DELETE RESTRICT
);

COMMENT ON TABLE t_order_header IS '订单主表';
```

### 3.8 订单明细表 `t_order_item`

**用途**：
记录每个订单中的具体商品明细。评论可以关联到 `t_order_item`，方便分析「针对哪次购买的反馈」。

```sql
CREATE TABLE t_order_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL,
    product_id      UUID NOT NULL,
    product_name    VARCHAR(255) NOT NULL,
    sku             VARCHAR(64) NOT NULL,
    unit_price      NUMERIC(12, 2) NOT NULL,
    quantity        INT NOT NULL,
    line_amount     NUMERIC(12, 2) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (order_id) REFERENCES t_order_header(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES t_product(id) ON DELETE RESTRICT
);

COMMENT ON TABLE t_order_item IS '订单明细表';
```

### 3.9 支付记录表 `t_payment`

**用途**：
记录订单支付情况。

```sql
CREATE TABLE t_payment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL,
    pay_channel     VARCHAR(32) NOT NULL, -- alipay / wechat / stripe / paypal ...
    pay_status      VARCHAR(32) NOT NULL, -- pending / success / failed / refund
    amount          NUMERIC(12, 2) NOT NULL,
    transaction_no  VARCHAR(128),
    paid_at         TIMESTAMPTZ,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (order_id) REFERENCES t_order_header(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_payment IS '支付记录表';
```

### 3.10 发货记录表 `t_shipment`

**用途**：
记录订单发货信息，可作为评论分析时的上下文（例如，物流慢导致差评）。

```sql
CREATE TABLE t_shipment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL,
    carrier         VARCHAR(64) NOT NULL,  -- 物流公司
    tracking_no     VARCHAR(128),
    status          VARCHAR(32) NOT NULL,  -- created / shipped / delivered / lost
    shipped_at      TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (order_id) REFERENCES t_order_header(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_shipment IS '发货记录表';
```

### 3.11 评论话题表 `t_comment_topic`

**用途**：
系统预定义的「评论话题」，例如：物流、包装、质量、服务态度、性价比等。

```sql
CREATE TABLE t_comment_topic (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(64) UNIQUE NOT NULL,
    name            VARCHAR(128) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_comment_topic IS '评论话题定义表，例如：物流、质量、包装等';
```

### 3.12 评论表 `t_comment`

**用途**：
存储用户对商品的评论。

**字段要点**：

* `product_id`: 评论所属商品
* `order_item_id`: 可选，评论来自哪一条订单明细
* `rating`: 评分（1~5）
* `aspect_tags`: 方面标签（如 json 数组：["物流", "包装"]），可作为粗粒度分析
* `content`: 评论原文
* `reply_to_comment_id`: 支持多级回复/追评
* `source`: 评论来源（app/web/客服代录等）

```sql
CREATE TABLE t_comment (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL,
    product_id              UUID NOT NULL,
    order_item_id           UUID,
    reply_to_comment_id     UUID,
    rating                  INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title                   VARCHAR(255),
    content                 TEXT NOT NULL,
    aspect_tags             TEXT[],        -- 粗粒度方面标签，如 {"物流","质量"}
    is_visible              BOOLEAN NOT NULL DEFAULT TRUE,
    source                  VARCHAR(32) NOT NULL DEFAULT 'app', -- app/web/service/...
    metadata                JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES t_app_user(id) ON DELETE RESTRICT,
    FOREIGN KEY (product_id) REFERENCES t_product(id) ON DELETE RESTRICT,
    FOREIGN KEY (order_item_id) REFERENCES t_order_item(id) ON DELETE SET NULL,
    FOREIGN KEY (reply_to_comment_id) REFERENCES t_comment(id) ON DELETE SET NULL
);

COMMENT ON TABLE t_comment IS '评论表：用户针对商品/订单的评价';
COMMENT ON COLUMN t_comment.aspect_tags IS '评论涉及的方面标签，如 物流/质量/包装 等';
COMMENT ON COLUMN t_comment.metadata IS '评论的结构化分析结果，例如情感分数、主题分布等';
```

### 3.13 评论-话题关联表 `t_comment_topic_mapping`

**用途**：
精细化把一条评论关联到多个话题。

```sql
CREATE TABLE t_comment_topic_mapping (
    comment_id      UUID NOT NULL,
    topic_id        UUID NOT NULL,
    weight          NUMERIC(4, 3) DEFAULT 1.0, -- 话题权重，0~1
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (comment_id, topic_id),
    FOREIGN KEY (comment_id) REFERENCES t_comment(id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES t_comment_topic(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_comment_topic_mapping IS '评论与话题的多对多关联表，用于精细化主题分析';
```

### 3.14 评论日汇总表 `t_comment_summary_daily`

**用途**：
为加速统计查询，按「日期 + 商品 + 话题」维度进行评论聚合。

```sql
CREATE TABLE t_comment_summary_daily (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stat_date               DATE NOT NULL,
    product_id              UUID NOT NULL,
    topic_id                UUID, -- 可选：按话题细分，也可存 NULL 代表 overall
    comment_count           INT NOT NULL,
    rating_avg              NUMERIC(3, 2),
    positive_count          INT NOT NULL DEFAULT 0,
    neutral_count           INT NOT NULL DEFAULT 0,
    negative_count          INT NOT NULL DEFAULT 0,
    metadata                JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    FOREIGN KEY (product_id) REFERENCES t_product(id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES t_comment_topic(id) ON DELETE SET NULL
);

COMMENT ON TABLE t_comment_summary_daily IS '评论日汇总表，用于趋势和统计分析';
```

### 3.15 系统配置表 `t_system_kv`

**用途**：
保存一些系统参数、分桶配置等。

```sql
CREATE TABLE t_system_kv (
    key         VARCHAR(128) PRIMARY KEY,
    value       TEXT NOT NULL,
    description TEXT,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_system_kv IS '系统配置 KV 表，例如 AI 分析开关、模型版本等';
```

## 4. 索引设计

```sql
-- 用户
CREATE INDEX idx_app_user_email ON t_app_user (email);

-- 商品
CREATE INDEX idx_product_status ON t_product (status);
CREATE INDEX idx_product_sku ON t_product (sku);

-- 订单
CREATE INDEX idx_order_user_created ON t_order_header (user_id, created_at);
CREATE INDEX idx_order_status_created ON t_order_header (status, created_at);

-- 评论：按商品 + 时间查询最近评论
CREATE INDEX idx_comment_product_created ON t_comment (product_id, created_at DESC);
CREATE INDEX idx_comment_user_created ON t_comment (user_id, created_at DESC);
CREATE INDEX idx_comment_sentiment ON t_comment (sentiment);
CREATE INDEX idx_comment_aspect_tags_gin ON t_comment USING GIN (aspect_tags);

-- 评论话题关联
CREATE INDEX idx_comment_topic_mapping_topic ON t_comment_topic_mapping (topic_id);

-- 评论日汇总
CREATE UNIQUE INDEX uniq_comment_summary_daily ON t_comment_summary_daily (stat_date, product_id, topic_id);
```
