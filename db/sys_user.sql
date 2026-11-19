-- 注册用户表：手机号 + 密码哈希（与员工表 sys_employee 独立）
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    phone         VARCHAR(20)     NOT NULL                COMMENT '手机号',
    password_hash VARCHAR(255)    NOT NULL                COMMENT '密码哈希，禁止存储明文密码',
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';
