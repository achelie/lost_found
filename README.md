# 校园失物招领平台

## 技术栈
- **后端**: Spring Boot 3.2 + MyBatis-Plus + MySQL + JWT + Spring Security
- **前端**: Vue 3 + Vue Router + Pinia + Element Plus + Axios

## 项目结构
```
campus-lost-found/
├── backend/                  # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/campus/lostfound/
│       │   ├── config/       # 配置类 (安全、JWT过滤器、CORS、MyBatis)
│       │   ├── controller/   # 控制器 (Auth、Item、Claim、Notification、Admin、File)
│       │   ├── dto/          # 数据传输对象
│       │   ├── entity/       # 实体类
│       │   ├── mapper/       # MyBatis-Plus Mapper
│       │   ├── service/      # 服务层
│       │   └── utils/        # 工具类 (JWT)
│       └── resources/
│           └── application.yml
├── frontend/                 # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/              # API 请求封装
│       ├── components/       # 公共组件
│       ├── router/           # 路由配置
│       ├── stores/           # Pinia 状态管理
│       └── views/            # 页面组件
│           ├── auth/         # 登录、注册
│           ├── item/         # 物品列表、详情、发布
│           ├── claim/        # 认领申请
│           ├── notification/ # 消息通知
│           ├── profile/      # 个人中心
│           └── admin/        # 管理后台
└── sql/
    └── schema.sql            # 建表SQL
```

## 快速开始

### 1. 准备环境
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### 2. 创建数据库
```bash
mysql -u root -p < sql/schema.sql
```
> 默认管理员账号: admin，密码需重新生成（见下方说明）

### 3. 修改数据库配置
编辑 `backend/src/main/resources/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_lost_found
    username: root
    password: 你的密码
```

### 4. 启动后端
```bash
cd backend
mvn spring-boot:run
```
后端将在 http://localhost:8080 启动

### 5. 启动前端
```bash
cd frontend
npm install
npm run dev
```
前端将在 http://localhost:5173 启动

### 6. 创建管理员账号
SQL中的默认管理员密码hash是示例值，启动后请通过注册接口创建账号，然后在数据库中修改role为1：
```sql
UPDATE user SET role = 1 WHERE username = '你的用户名';
```

## 功能列表

| 功能 | 说明 |
|------|------|
| ✅ 用户注册/登录 | JWT Token 认证 |
| ✅ 发布失物信息 | 丢失物品、时间、地点、描述、联系方式 |
| ✅ 发布招领信息 | 拾到物品、时间、地点、描述、联系方式 |
| ✅ 上传图片 | 支持多图上传 |
| ✅ 浏览信息列表 | 分页展示失物/招领信息 |
| ✅ 关键词搜索 | 按名称、地点搜索 |
| ✅ 分类筛选 | 证件、电子产品、书籍、衣物、其他 |
| ✅ 查看详情 | 失物/招领详情页 |
| ✅ 认领申请 | 提交证明信息和图片 |
| ✅ 审核认领 | 发布者审核认领申请（通过/拒绝） |
| ✅ 消息通知 | 认领申请、审核结果通知 |
| ✅ 个人中心 | 查看发布信息、修改资料 |
| ✅ 管理后台 | 审核帖子、删除违规内容、管理用户 |

## API 接口

| 接口 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| /api/auth/login | POST | 登录 | 无 |
| /api/auth/register | POST | 注册 | 无 |
| /api/items/list | GET | 物品列表 | 无 |
| /api/items/detail/{id} | GET | 物品详情 | 无 |
| /api/items/publish | POST | 发布物品 | 用户 |
| /api/items/my | GET | 我的发布 | 用户 |
| /api/claims/submit | POST | 提交认领 | 用户 |
| /api/claims/item/{id} | GET | 物品的认领列表 | 用户 |
| /api/claims/audit/{id} | POST | 审核认领 | 用户 |
| /api/notifications | GET | 通知列表 | 用户 |
| /api/notifications/read/{id} | POST | 标记已读 | 用户 |
| /api/notifications/unread-count | GET | 未读数量 | 用户 |
| /api/user/profile | GET/PUT | 个人信息 | 用户 |
| /api/file/upload | POST | 上传文件 | 用户 |
| /api/admin/items | GET | 管理帖子 | 管理员 |
| /api/admin/items/{id}/audit | POST | 审核帖子 | 管理员 |
| /api/admin/items/{id} | DELETE | 删除帖子 | 管理员 |
| /api/admin/users | GET | 用户列表 | 管理员 |
| /api/admin/users/{id}/status | POST | 禁用/启用用户 | 管理员 |
