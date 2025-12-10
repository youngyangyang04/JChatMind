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

CREATE TABLE t_role (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(64) UNIQUE NOT NULL,
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_role IS '角色表：定义系统中的角色（管理员、客服等）';

CREATE TABLE t_user_role (
    user_id     UUID NOT NULL,
    role_id     UUID NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES t_app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES t_role(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_user_role IS '用户-角色关联表，多对多';

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

CREATE TABLE t_product_category_relation (
    product_id      UUID NOT NULL,
    category_id     UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (product_id, category_id),
    FOREIGN KEY (product_id) REFERENCES t_product(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES t_product_category(id) ON DELETE CASCADE
);

COMMENT ON TABLE t_product_category_relation IS '商品-类目多对多关联';

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

CREATE TABLE t_comment_topic (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(64) UNIQUE NOT NULL,
    name            VARCHAR(128) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_comment_topic IS '评论话题定义表，例如：物流、质量、包装等';

-- 为了方便造数据，去除 t_comment 表的外键约束
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
    source                  VARCHAR(32) NOT NULL DEFAULT 'app', -- app/web
    metadata                JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_comment IS '评论表：用户针对商品/订单的评价，AI 分析的核心数据源';
COMMENT ON COLUMN t_comment.aspect_tags IS '评论涉及的方面标签，如 物流/质量/包装 等';
COMMENT ON COLUMN t_comment.metadata IS '评论的结构化分析结果，例如情感分数、主题分布等';

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

CREATE TABLE t_system_kv (
    key         VARCHAR(128) PRIMARY KEY,
    value       TEXT NOT NULL,
    description TEXT,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_system_kv IS '系统配置 KV 表、模型版本等';
