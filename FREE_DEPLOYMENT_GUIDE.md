# 免费服务器部署完整指南 ✨

本项目可以完全免费部署在云平台上，无需任何费用！

## 🎯 部署方案选择

| 组件 | 推荐免费平台 | 说明 |
|------|-------------|------|
| 前端 | Vercel | ✅ 全球CDN，自动部署，国内访问快，完全免费 |
| 后端 | Render.com | ✅ 免费Java运行环境，支持Spring Boot |
| 数据库 | PlanetScale | ✅ 免费MySQL数据库，无需信用卡 |
| 替代方案 | Railway | 每月500小时免费运行时间 |

---

## 📋 第一步：部署前端到 Vercel

### 1. 前端已经配置好vercel.json
项目中 `frontend/vercel.json` 已经完整配置，只需推送到GitHub即可一键部署。

### 2. 部署步骤：
1. 注册 Vercel 账号: https://vercel.com/signup
2. 导入你的GitHub仓库
3. 选择 `frontend` 目录作为根目录
4. 配置环境变量：
   ```
   VITE_API_BASE_URL = https://你的后端地址.onrender.com
   ```
5. 点击部署，等待30秒即可完成！

✅ 前端部署完成后会获得免费域名: `your-project.vercel.app`

---

## 🗄️ 第二步：创建免费MySQL数据库 (PlanetScale)

PlanetScale 提供完全免费的MySQL数据库，无需信用卡：

1. 注册账号: https://planetscale.com/
2. 创建新数据库，选择区域 `ap-northeast` (东京，国内访问快)
3. 点击 "Connect" 按钮，选择 "Java" 连接方式
4. 获得数据库连接信息：
   ```
   主机: aws.connect.psdb.cloud
   用户名: xxxxxxxx
   密码: pscale_pw_xxxxxx
   数据库名: your-db-name
   ```

5. 导入SQL结构：
   ```bash
   # 使用PlanetScale CLI或者在线控制台执行
   # 执行 sql/schema.sql 创建所有表
   # 执行 sql/mock_items.sql 导入测试数据
   ```

---

## ⚙️ 第三步：部署后端到 Render.com

Render 提供免费的Java Spring Boot运行环境：

### 1. 配置后端部署文件
在项目根目录创建 `render.yaml`：
```yaml
services:
  - type: web
    name: lost-found-backend
    env: java
    buildCommand: cd backend && mvn clean package -DskipTests
    startCommand: java -jar backend/target/lost-found-0.0.1-SNAPSHOT.jar
    envVars:
      - key: SPRING_DATASOURCE_URL
        value: jdbc:mysql://aws.connect.psdb.cloud/campus_lost_found?useSSL=true&requireSSL=true&serverTimezone=Asia/Shanghai
      - key: SPRING_DATASOURCE_USERNAME
        value: 你的PlanetScale用户名
      - key: SPRING_DATASOURCE_PASSWORD
        value: 你的PlanetScale密码
      - key: JWT_SECRET
        value: campusLostFoundSecretKey2024CampusLostFoundPlatform
      - key: UPLOAD_PATH
        value: /tmp/uploads/
    plan: free
```

### 2. 部署步骤：
1. 注册 Render 账号: https://render.com/
2. 选择 "New Web Service"
3. 导入你的GitHub仓库
4. 配置：
   - Root Directory: `backend`
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/lost-found-0.0.1-SNAPSHOT.jar`
5. 粘贴上面的环境变量
6. 点击部署，第一次部署约需要3-5分钟

✅ 后端部署完成后会获得地址: `https://lost-found-backend.onrender.com`

---

## 🔄 第四步：更新前端API地址

部署完后端后，更新前端的API配置：

1. 在 Vercel 后台进入项目设置
2. 找到 Environment Variables
3. 添加：
   ```
   VITE_API_BASE_URL = https://lost-found-backend.onrender.com
   ```
4. 重新部署前端项目

---

## 🎉 部署完成！

现在你有了完全免费运行的完整应用：
- ✅ 前端: https://your-project.vercel.app
- ✅ 后端: https://lost-found-backend.onrender.com
- ✅ 数据库: PlanetScale MySQL

所有服务都是免费的，无需信用卡，可以永久使用！

---

## ⚠️ 注意事项

### Render 免费层限制：
- 15分钟无请求会自动休眠
- 首次访问需要等待10-30秒唤醒
- 每月750小时运行时间（足够24/7运行）
- 512MB内存

### 优化建议：
1. 使用UptimeRobot每5分钟ping一次后端，防止休眠
2. 前端添加加载动画提示用户后端正在唤醒
3. 数据库每月10GB存储，10亿行读取足够使用

---

## 🚀 一键部署按钮

你可以添加这个README徽章，其他人可以一键部署：

```
[![Deploy to Vercel](https://vercel.com/button)](https://vercel.com/new/clone?repository-url=你的仓库地址)
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=你的仓库地址)
```

---

## ✅ 部署后检查清单

- [ ] 可以打开前端页面
- [ ] 可以注册和登录账号
- [ ] 可以发布物品
- [ ] 可以上传图片
- [ ] WebSocket聊天功能正常
- [ ] 消息可以实时收发
- [ ] 刷新页面历史消息正确加载
- [ ] 多用户同时在线测试通过

恭喜！你的项目已经成功部署在免费服务器上！ 🚀