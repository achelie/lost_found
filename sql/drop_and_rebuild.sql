-- 校园失物招领系统 - 数据库重建脚本
-- 警告: 此脚本将删除所有现有数据！

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET SESSION COLLATION_CONNECTION = 'utf8mb4_unicode_ci';

-- 关闭外键约束
SET FOREIGN_KEY_CHECKS=0;

-- 删除所有表
DROP TABLE IF EXISTS `user_block`;
DROP TABLE IF EXISTS `item_view_log`;
DROP TABLE IF EXISTS `admin_log`;
DROP TABLE IF EXISTS `feedback`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `claim`;
DROP TABLE IF EXISTS `item`;
DROP TABLE IF EXISTS `user`;

-- 启用外键约束
SET FOREIGN_KEY_CHECKS=1;

-- ===================================
-- 新建数据库和用户表
-- ===================================

-- 用户表
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，用于登录',
  `password` VARCHAR(300) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` VARCHAR(50) COMMENT '昵称，用于展示',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `role` TINYINT DEFAULT 0 COMMENT '角色: 0普通用户 1管理员',
  `status` TINYINT DEFAULT 1 COMMENT '账户状态: 0禁用 1正常',
  `bio` VARCHAR(255) COMMENT '个人简介',
  `last_login` DATETIME COMMENT '最后登录时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_username (`username`),
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基本信息表';

-- 物品信息表
CREATE TABLE `item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '物品ID',
  `user_id` BIGINT NOT NULL COMMENT '发布者ID',
  `type` TINYINT NOT NULL COMMENT '类型: 0失物 1招领',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `category` VARCHAR(30) NOT NULL COMMENT '分类: 证件/电子产品/书籍/衣物/钥匙/卡券/其他',
  `location` VARCHAR(200) COMMENT '地点',
  `item_time` DATETIME COMMENT '丢失/拾到时间',
  `description` TEXT COMMENT '详细描述',
  `contact` VARCHAR(100) COMMENT '联系方式',
  `images` VARCHAR(1000) COMMENT '图片URL,逗号分隔',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0待审核 1已通过 2已拒绝 3已认领 4已关闭',
  `reject_reason` VARCHAR(500) COMMENT '拒绝理由（仅当status=2时）',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `claim_count` INT DEFAULT 0 COMMENT '认领申请数',
  `claimed_user_id` BIGINT COMMENT '认领成功的用户ID',
  `claimed_at` DATETIME COMMENT '认领成功时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`claimed_user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX idx_user_id (`user_id`),
  INDEX idx_type (`type`),
  INDEX idx_category (`category`),
  INDEX idx_status (`status`),
  INDEX idx_created_at (`created_at`),
  FULLTEXT INDEX ft_search (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='失物/招领物品信息表';

-- 认领申请表
CREATE TABLE `claim` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '认领ID',
  `item_id` BIGINT NOT NULL COMMENT '物品ID',
  `user_id` BIGINT NOT NULL COMMENT '申请人ID',
  `proof` TEXT COMMENT '证明信息/理由',
  `proof_images` VARCHAR(1000) COMMENT '证明图片URL,逗号分隔',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0待审核 1通过 2拒绝',
  `remark` VARCHAR(500) COMMENT '审核备注',
  `result_at` DATETIME COMMENT '审核时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`item_id`) REFERENCES `item`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  INDEX idx_item_id (`item_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_status (`status`),
  UNIQUE KEY uk_item_user (`item_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品认领申请表';

-- 聊天消息表
CREATE TABLE `chat_message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
  `from_user_id` BIGINT NOT NULL COMMENT '发送者ID',
  `to_user_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` INT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`from_user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`to_user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  INDEX idx_from_to (`from_user_id`, `to_user_id`),
  INDEX idx_to_user (`to_user_id`),
  INDEX idx_created_at (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私聊消息表';

-- 消息通知表
CREATE TABLE `notification` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收人ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` VARCHAR(500) COMMENT '内容',
  `type` TINYINT COMMENT '类型: 0认领申请 1审核结果 2系统消息 3公告',
  `related_id` BIGINT COMMENT '关联ID(物品ID或认领ID)',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
  `read_at` DATETIME COMMENT '已读时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  INDEX idx_user_id (`user_id`),
  INDEX idx_type (`type`),
  INDEX idx_is_read (`is_read`),
  INDEX idx_created_at (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 物品浏览日志表
CREATE TABLE `item_view_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  `item_id` BIGINT NOT NULL COMMENT '物品ID',
  `user_id` BIGINT COMMENT '用户ID（可为空-游客）',
  `ip` VARCHAR(50) COMMENT 'IP地址',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  FOREIGN KEY (`item_id`) REFERENCES `item`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX idx_item_id (`item_id`),
  INDEX idx_created_at (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品浏览日志表';

-- 用户黑名单表
CREATE TABLE `user_block` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '黑名单ID',
  `user_id` BIGINT NOT NULL COMMENT '被拉黑用户ID',
  `reason` VARCHAR(500) COMMENT '拉黑原因',
  `blocked_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  UNIQUE KEY uk_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户黑名单表';

-- 用户反馈/投诉表
CREATE TABLE `feedback` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` BIGINT COMMENT '提交者ID',
  `type` TINYINT COMMENT '类型: 0反馈 1投诉 2建议',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `related_item_id` BIGINT COMMENT '关联物品ID',
  `related_user_id` BIGINT COMMENT '关联用户ID（投诉时使用）',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0未处理 1已处理 2已解决',
  `reply` TEXT COMMENT '回复内容',
  `replied_at` DATETIME COMMENT '回复时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`related_item_id`) REFERENCES `item`(`id`) ON DELETE SET NULL,
  FOREIGN KEY (`related_user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL,
  INDEX idx_status (`status`),
  INDEX idx_type (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈/投诉表';

-- 管理员操作日志表
CREATE TABLE `admin_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` BIGINT NOT NULL COMMENT '操作管理员ID',
  `operation` VARCHAR(50) NOT NULL COMMENT '操作: 审核物品/驳回申请/拉黑用户等',
  `target_type` VARCHAR(30) COMMENT '目标类型: item/claim/user',
  `target_id` BIGINT COMMENT '目标ID',
  `detail` VARCHAR(500) COMMENT '操作详情',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  FOREIGN KEY (`admin_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  INDEX idx_admin_id (`admin_id`),
  INDEX idx_created_at (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- ===================================
-- 插入初始数据
-- ===================================

-- 用户通过前端注册功能创建，暂不插入默认用户
-- 如需创建测试用户，请通过前端的注册页面创建
