# WebSocket聊天功能 - 部署与测试指南

## 📋 前置要求

- Java 17+
- Node.js 16+
- MySQL 5.7+
- Maven 3.6+
- Git

## 🚀 快速启动

### 第一步：启动后端

```bash
# 进入后端目录
cd e:\code\t1\backend

# 方式1: Maven编译并运行
mvn clean install
mvn spring-boot:run

# 方式2: 使用IDE运行
# 在IDE中打开 LostFoundApplication.java，右键运行
```

**预期输出：**
```
  ____      __    _   _
 / ___|    / /_ _| |_| |__ ___
| |  _   / / _` | __| '_ \/ __| 
| |_| | / / (_| | |_| |_) \__ \
 \____| /_/ \__,_|\__|_.__/|___/

Application startup successful
```

### 第二步：启动前端

```bash
# 进入前端目录
cd e:\code\t1\frontend

# 安装依赖（如果未安装过）
npm install

# 启动开发服务器
npm run dev

# 预期输出：
# VITE v5.x.x 准备在 localhost:5173
```

### 第三步：打开浏览器

访问 `http://localhost:5173`

## 👥 测试场景

### 场景1: 两个浏览器窗口测试

1. **创建测试用户（如果还没有）**
   - 在数据库执行以下SQL创建测试用户
   ```sql
   INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `role`, `status`) VALUES
   ('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '用户1', 'user1@test.com', 0, 1),
   ('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '用户2', 'user2@test.com', 0, 1);
   ```

2. **第一个窗口：用user1登录**
   - 访问 登录页面
   - 用户名: `user1`
   - 密码: `123456`（取决于实际密码设置）
   - 点击登录

3. **第二个窗口：用user2登录**
   - 打开新的浏览器窗口/标签
   - 访问同一个应用
   - 用户名: `user2`
   - 密码: `123456`

4. **开始聊天**
   - 在user1窗口中找到user2并进入聊天
   - 发送消息："你好，这是第一条测试消息"
   - **预期：** 消息立即出现在自己的窗口中
   - 在user2窗口中应该立即收到该消息

5. **user2回复**
   - 在user2的聊天窗口中回复消息
   - **预期：** user1立即收到回复

### 场景2: 刷新页面测试

1. 在user1窗口发送消息
2. 刷新user1的页面 (F5)
   - **预期：** 历史消息自动加载显示
   - WebSocket自动重新连接

3. 在user2窗口继续发送消息
   - **预期：** user1能继续接收新消息

### 场景3: 多个聊天对象测试

1. user1登录后，找到多个不同的用户（user2, user3等）
2. 分别进入聊天，发送不同内容
3. **预期：** 消息正确路由到各自对应的聊天窗口

## 🔍 故障诊断

### 问题1: "WebSocket连接失败"

**症状：** 页面显示 "WebSocket连接失败" 消息

**检查清单：**
1. ✅ 后端是否正在运行？
   ```bash
   # 检查8080端口是否监听
   netstat -an | grep 8080  # Linux/Mac
   netstat -ano | findstr 8080  # Windows
   ```

2. ✅ 检查浏览器控制台错误 (F12 -> Console)
   - 按F12打开开发者工具
   - 查看红色错误信息
   - 复制完整错误信息用于排查

3. ✅ 检查CORS设置
   - 后端WebSocketConfig已配置允许所有origin
   - 如仍有问题，检查SecurityConfig是否阻止

4. ✅ 检查token是否有效
   ```javascript
   // 在浏览器控制台执行
   console.log(localStorage.getItem('token'))
   ```

### 问题2: 消息发送但不显示

**症状：** 发送按钮可点击，但消息没有出现

**检查清单：**
1. ✅ 检查WebSocket连接状态
   - 浏览器F12 -> Network -> WS
   - 查看ws连接是否为绿色（Connected）

2. ✅ 检查后端日志
   ```bash
   # 在后端终端查看日志
   # 应该看到 "收到私聊消息: from=x, to=y"
   ```

3. ✅ 检查数据库
   ```sql
   SELECT * FROM chat_message ORDER BY created_at DESC LIMIT 10;
   ```
   - 消息是否已保存？

### 问题3: 旧消息不显示，仅显示新消息

**症状：** 加入聊天后只能看到新消息

**解决方案：**
1. 点击"加载历史"按钮
   - **预期：** 历史消息加载显示
2. 或使用REST API查询
   ```bash
   curl -H "userId: 1" http://localhost:8080/api/chat/messages/2
   ```

### 问题4: 505错误 "Internal Server Error"

**排查步骤：**
1. 查看后端日志中的完整异常堆栈
2. 常见原因：
   - 数据库连接失败 - 检查MySQL是否运行
   - JPA配置错误 - 检查application.yml的spring.jpa配置
   - 实体类注解错误 - 确保@Entity @Table正确

## 📊 监控和日志

### 查看实时日志

**后端日志关键信息：**
```
// WebSocket连接
用户连接: userId=1, 当前在线人数: 1

// 接收消息
收到私聊消息: from=1, to=2, content=你好

// 断开连接  
用户断开连接: userId=1, 当前在线人数: 0
```

**检查异常日志：**
1. 停止后端: Ctrl+C
2. 查看输出中是否有ERROR行
3. 复制错误堆栈用于排查

### 数据库验证

```sql
-- 查看所有聊天消息
SELECT * FROM chat_message ORDER BY created_at DESC;

-- 查看特定用户的消息
SELECT * FROM chat_message 
WHERE from_user_id = 1 OR to_user_id = 1
ORDER BY created_at DESC;

-- 查看未读消息
SELECT * FROM chat_message 
WHERE to_user_id = 2 AND is_read = 0;

-- 统计消息数量
SELECT COUNT(*) FROM chat_message;
```

## 🧪 高级测试

### 性能测试

```javascript
// 在浏览器控制台发送大量消息
for(let i = 0; i < 100; i++) {
  setTimeout(() => {
    document.querySelector('.el-button--primary').click()
  }, i * 100)
}
```

### 网络丢包测试

1. 打开浏览器DevTools
2. Network标签 -> 右下角三点菜单 -> Throttling
3. 选择 "Offline" - 测试断网重连
4. 选择 "Slow 3G" - 测试低速网络

### 并发用户测试

使用 Apache JMeter 进行WebSocket压力测试：
- JMeter可以模拟多个并发用户
- 导入WebSocket采样器
- 配置目标服务器和消息内容
- 设置线程组数量模拟并发

## ✅ 检查清单

部署前检查：
- [ ] Java版本 >= 17
- [ ] MySQL正在运行
- [ ] 数据库campus_lost_found已创建
- [ ] chat_message表存在
- [ ] 前端依赖已安装 (npm install)

启动检查：
- [ ] 后端启动无异常
- [ ] 前端启动无异常
- [ ] 可以访问登录页面
- [ ] 可以正常登录

功能检查：
- [ ] 两个用户能建立WebSocket连接
- [ ] 能发送和接收消息
- [ ] 消息持久化到数据库
- [ ] 刷新页面能加载历史消息

## 📞 获取帮助

遇到问题时：
1. 查看后端日志输出
2. 按F12打开浏览器开发者工具查看Console和Network
3. 检查MySQL是否正确运行
4. 查看CHAT_IMPLEMENTATION.md中的详细说明

## 🎉 完成！

如果所有测试都通过，说明WebSocket聊天功能已成功部署！

祝使用愉快！ 🚀
