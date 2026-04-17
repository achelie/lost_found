# 数据库SQL参考指南

## 聊天功能所需表结构

### 主要表：chat_message

```sql
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
```

该表已在 `sql/schema.sql` 中定义，无需手动执行。

## 测试数据准备

### 创建测试用户

如果需要创建测试用户，执行以下SQL：

```sql
-- 创建两个测试用户
-- 密码都是: 123456 (BCrypt加密后)
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `phone`, `role`, `status`, `created_at`, `updated_at`) VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张三', 'user1@example.com', '13800138001', 0, 1, NOW(), NOW()),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李四', 'user2@example.com', '13800138002', 0, 1, NOW(), NOW()),
('user3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '王五', 'user3@example.com', '13800138003', 0, 1, NOW(), NOW());
```

**密码说明：** 上述用户的密码都是 `password123`（已加密）

### 插入测试聊天消息

```sql
-- 插入user1和user2之间的测试消息
INSERT INTO `chat_message` (`from_user_id`, `to_user_id`, `content`, `is_read`, `created_at`) VALUES
(1, 2, '你好，今天天气不错', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 1, '是啊，一起去散步吗？', 1, DATE_SUB(NOW(), INTERVAL 1.5 HOUR)),
(1, 2, '好的，我们一起去', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 1, '那我们现在出发', 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- 插入user1和user3之间的测试消息
INSERT INTO `chat_message` (`from_user_id`, `to_user_id`, `content`, `is_read`, `created_at`) VALUES
(1, 3, '有没有看到我丢的手机', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 1, '没有呢，什么时候丢的', 1, DATE_SUB(NOW(), INTERVAL 23 HOUR));
```

## 常用查询

### 1. 查看所有聊天消息

```sql
SELECT 
  m.id,
  m.from_user_id,
  u1.nickname AS from_user_name,
  m.to_user_id,
  u2.nickname AS to_user_name,
  m.content,
  m.is_read,
  m.created_at
FROM chat_message m
LEFT JOIN user u1 ON m.from_user_id = u1.id
LEFT JOIN user u2 ON m.to_user_id = u2.id
ORDER BY m.created_at DESC;
```

### 2. 查查询两个用户之间的消息记录

```sql
-- 查询user1和user2之间的所有消息
SELECT * FROM chat_message 
WHERE (from_user_id = 1 AND to_user_id = 2) 
   OR (from_user_id = 2 AND to_user_id = 1)
ORDER BY created_at ASC;
```

### 3. 查询某个用户的所有消息

```sql
-- 查询user1的所有消息（包括接收和发送）
SELECT * FROM chat_message 
WHERE from_user_id = 1 OR to_user_id = 1
ORDER BY created_at DESC;
```

### 4. 查询未读消息

```sql
-- 查询某个用户收到的所有未读消息
SELECT * FROM chat_message 
WHERE to_user_id = 1 AND is_read = 0
ORDER BY created_at DESC;

-- 统计某个用户的未读消息数
SELECT COUNT(*) AS unread_count FROM chat_message 
WHERE to_user_id = 1 AND is_read = 0;
```

### 5. 查询最近的会话列表

```sql
-- 查询user1最近与谁聊过天（最后一条消息时间）
SELECT DISTINCT
  CASE 
    WHEN from_user_id = 1 THEN to_user_id 
    ELSE from_user_id 
  END AS other_user_id,
  u.nickname AS other_user_name,
  MAX(created_at) AS last_message_time,
  (SELECT content FROM chat_message 
   WHERE (from_user_id = 1 AND to_user_id = u.id) 
      OR (from_user_id = u.id AND to_user_id = 1)
   ORDER BY created_at DESC LIMIT 1) AS last_message
FROM chat_message AS m
JOIN user u ON u.id != 1
WHERE from_user_id = 1 OR to_user_id = 1
GROUP BY other_user_id, other_user_name
ORDER BY last_message_time DESC;
```

### 6. 消息统计

```sql
-- 总消息数
SELECT COUNT(*) FROM chat_message;

-- 每个用户发送的消息数
SELECT from_user_id, COUNT(*) as sent_count FROM chat_message 
GROUP BY from_user_id;

-- 按日期统计消息数
SELECT DATE(created_at) as chat_date, COUNT(*) as count 
FROM chat_message 
GROUP BY DATE(created_at)
ORDER BY chat_date DESC;

-- 消息内容长度统计
SELECT 
  LENGTH(content) as content_length,
  COUNT(*) as count
FROM chat_message
GROUP BY content_length
ORDER BY count DESC;
```

## 数据库维护

### 1. 标记消息为已读

```sql
-- 标记user1收到的所有user2发送的消息为已读
UPDATE chat_message 
SET is_read = 1 
WHERE from_user_id = 2 AND to_user_id = 1;
```

### 2. 删除特定消息

```sql
-- 删除某条消息
DELETE FROM chat_message WHERE id = 123;

-- 删除某个用户的所有消息
DELETE FROM chat_message 
WHERE from_user_id = 1 OR to_user_id = 1;

-- 删除某个时间之前的旧消息（如7天前）
DELETE FROM chat_message 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
```

### 3. 清空所有消息（仅测试用）

```sql
-- 警告：此操作将删除所有消息，生产环境慎用！
TRUNCATE TABLE chat_message;
```

### 4. 数据库备份和恢复

```bash
# 备份chat_message表
mysqldump -u root -p campus_lost_found chat_message > chat_message_backup.sql

# 从备份恢复
mysql -u root -p campus_lost_found < chat_message_backup.sql
```

## 索引优化

现有索引：
- `idx_from_to` - 用于快速查询某两个用户之间的消息
- `idx_to_user` - 用于快速查询接收者的消息

查看索引使用情况：

```sql
-- 查看表的索引
SHOW INDEX FROM chat_message;

-- 分析查询性能
EXPLAIN SELECT * FROM chat_message 
WHERE from_user_id = 1 AND to_user_id = 2;

-- 显示索引统计信息
SHOW STATISTICS FOR table chat_message;
```

## 外键完整性check

```sql
-- 检查外键约束
SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'chat_message';

-- 检查是否有孤立的消息记录（指向不存在的用户）
SELECT * FROM chat_message m
WHERE NOT EXISTS (SELECT 1 FROM user WHERE id = m.from_user_id)
   OR NOT EXISTS (SELECT 1 FROM user WHERE id = m.to_user_id);
```

## 常见问题

### Q: 消息表变得很大怎么办？
**A:** 可以：
1. 定期清理旧消息
2. 使用分区表存储历史数据
3. 添加物理删除或软删除标记

### Q: 如何处理消息的加密存储？
**A:** 可以：
1. 在应用层加密/解密
2. 使用MySQL的AES加密函数
3. 考虑使用端到端加密方案

### Q: 消息查询性能变慢？
**A:** 检查：
1. 是否使用了正确的索引
2. 查询语句是否优化
3. 是否需要分页查询

## 性能优化建议

```sql
-- 1. 检查表大小
SELECT 
  table_name,
  ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb
FROM information_schema.tables 
WHERE table_name = 'chat_message';

-- 2. 定期优化表
OPTIMIZE TABLE chat_message;

-- 3. 分析表统计信息
ANALYZE TABLE chat_message;

-- 4. 检查和修复表
CHECK TABLE chat_message;
REPAIR TABLE chat_message;
```
