-- 创建测试通知数据
-- 首先获取一个真实存在的用户 ID
SET @userId = (SELECT id FROM user LIMIT 1);

-- 如果用户存在，创建测试通知
IF @userId IS NOT NULL THEN
  -- 创建系统消息示例1
  INSERT INTO notification (user_id, title, content, type, is_read, created_at)
  VALUES (@userId, '新的认领申请', '您发布的"红色钥匙"收到了新的认领申请', 0, 0, NOW());
  
  -- 创建系统消息示例2
  INSERT INTO notification (user_id, title, content, type, is_read, created_at)
  VALUES (@userId, '认领申请被拒绝', '您对"蓝色手机"的认领申请已被拒绝', 1, 0, NOW() - INTERVAL 1 HOUR);
  
  -- 创建系统消息示例3  
  INSERT INTO notification (user_id, title, content, type, is_read, created_at)
  VALUES (@userId, '认领申请被批准', '您对"黑色钱包"的认领申请已被批准', 1, 0, NOW() - INTERVAL 2 HOUR);

END IF;

-- 验证插入的数据
SELECT * FROM notification WHERE user_id = @userId ORDER BY created_at DESC;
