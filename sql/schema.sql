CREATE DATABASE IF NOT EXISTS campus_lost_found DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_lost_found;

-- 用户表
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像',
  `role` TINYINT DEFAULT 0 COMMENT '角色: 0普通用户 1管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 物品信息表 (失物/招领)
CREATE TABLE `item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '发布者ID',
  `type` TINYINT NOT NULL COMMENT '类型: 0失物 1招领',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `category` VARCHAR(20) NOT NULL COMMENT '分类: 证件/电子产品/书籍/衣物/其他',
  `location` VARCHAR(200) COMMENT '地点',
  `item_time` DATETIME COMMENT '丢失/拾到时间',
  `description` TEXT COMMENT '详细描述',
  `contact` VARCHAR(100) COMMENT '联系方式',
  `images` VARCHAR(1000) COMMENT '图片URL,逗号分隔',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0待审核 1已通过 2已拒绝 3已认领 4已关闭',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 认领申请表
CREATE TABLE `claim` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `item_id` BIGINT NOT NULL COMMENT '物品ID',
  `user_id` BIGINT NOT NULL COMMENT '申请人ID',
  `proof` TEXT COMMENT '证明信息',
  `proof_images` VARCHAR(1000) COMMENT '证明图片',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0待审核 1通过 2拒绝',
  `remark` VARCHAR(500) COMMENT '审核备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`item_id`) REFERENCES `item`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 私聊消息表
CREATE TABLE `chat_message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `from_user_id` BIGINT NOT NULL COMMENT '发送者ID',
  `to_user_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`from_user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`to_user_id`) REFERENCES `user`(`id`),
  INDEX idx_from_to (`from_user_id`, `to_user_id`),
  INDEX idx_to_user (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 消息通知表
CREATE TABLE `notification` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '接收人ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` VARCHAR(500) COMMENT '内容',
  `type` TINYINT COMMENT '类型: 0认领申请 1审核结果 2系统通知',
  `related_id` BIGINT COMMENT '关联ID',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认管理员 (密码: admin123, BCrypt加密)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 1);
