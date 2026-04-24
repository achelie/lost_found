# 校园失物招领平台

面向校园场景的失物与招领信息发布平台：用户可发布「丢失」或「拾到」信息、上传图片、申请认领、站内私信与系统通知；管理员可审核帖子、管理用户。

## 新手阅读导航

- 建议先阅读：`ONBOARDING_READING_GUIDE.md`（按 30~90 分钟上手路径整理，含文件顺序与阅读目标）

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot **3.2.5**、Spring Security、JWT（jjwt **0.12.x**）、MyBatis-Plus **3.5.5**、Spring Data JPA（`ddl-auto: validate`，与表结构校验配合）、MySQL |
| 实时通信 | Spring WebSocket + **STOMP** + SockJS |
| 工具 | Lombok、Hutool |
| 前端 | **Vue 3**、Vue Router、Pinia、**Element Plus**、Axios、**Vite 5**、sockjs-client、stompjs |

---

## 功能概览

- **账号**：注册、登录（JWT，请求头 `Authorization: Bearer <token>`）
- **物品**：发布失物/招领、列表分页、详情、关键词与分类筛选、我的发布、删除自己的帖子、统计等（以后端实现为准）
- **媒体**：多图上传（受 `multipart` 大小限制）
- **认领**：提交申请与证明、发布者审核认领
- **通知**：站内通知、未读数量、标记已读
- **私信**：聊天相关 REST 接口 + WebSocket 推送（见下文）
- **管理后台**（`role = 1`）：全站帖子列表与审核、删除帖子、用户列表、删除用户（不可删除当前登录管理员自身）

---

## 仓库结构

```
lost_found/
├── backend/                 # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/campus/lostfound/
│       ├── config/          # Security、JWT、CORS、MyBatis-Plus、WebMvc、WebSocket 等
│       ├── controller/      # Auth、Item、Claim、Notification、Chat、File、User、Admin
│       ├── dto/、entity/、mapper/、service/、utils/
│   └── src/main/resources/application.yml
├── frontend/                # Vue 3 + Vite
│   ├── vite.config.js       # 开发代理 /api、/uploads → 8080
│   └── src/
│       ├── api/、components/、router/、stores/、views/
├── sql/
│   ├── drop_and_rebuild.sql # 删表并重建全库结构（慎用）
│   └── test_notifications.sql
├── render.yaml              # 示例部署配置（启动命令中的 jar 名需与打包产物一致）
└── README.md
```

---

## 环境要求

- **JDK** 17+
- **Maven** 3.8+
- **Node.js** 18+（建议配合 npm）
- **MySQL** 8.0+

---

## 数据库初始化

1. 创建数据库（若尚未创建）：

```sql
CREATE DATABASE IF NOT EXISTS campus_lost_found
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 在**该库**中执行结构脚本（会 **DROP 旧表**，仅适用于空库或能接受清空的场景）：

```bash
mysql -u root -p campus_lost_found < sql/drop_and_rebuild.sql
```

脚本内含用户、物品、认领、聊天、通知及扩展表等定义，**不包含默认管理员账号**；请先通过前端注册，再在库中将对应用户 `role` 改为 `1` 作为管理员。

```sql
UPDATE `user` SET `role` = 1 WHERE `username` = '你的用户名';
```

> Spring Data JPA 使用 `validate`：表结构需与实体一致，请保持以 `sql/drop_and_rebuild.sql` 为权威结构来源。

---

## 配置说明

编辑 `backend/src/main/resources/application.yml`：

- **数据源**：`spring.datasource.url` / `username` / `password`，库名需与上文一致（默认示例为 `campus_lost_found`）。
- **上传目录**：`upload.path` 指向本机可写目录；开发环境下若沿用仓库内示例路径，请改为你机器上的路径，并保证目录存在。
- **JWT**：生产环境务必更换 `jwt.secret` 与合理过期时间 `jwt.expiration`。

前端开发时依赖 Vite 代理访问后端，一般**无需**改 `frontend/src/api/request.js` 的 `baseURL`（默认 `/`）。

---

## 本地运行

**1. 启动后端**

```bash
cd backend
mvn spring-boot:run
```

默认：<http://localhost:8080>  
静态上传访问路径（若已配置资源映射）：`/uploads/**`

**2. 启动前端**

```bash
cd frontend
npm install
npm run dev
```

默认：<http://localhost:5173>（`vite.config.js` 中已配置 `/api`、`/uploads` 代理到 8080）

**3. 构建前端（可选）**

```bash
cd frontend
npm run build
npm run preview
```

---

## WebSocket（聊天）

- **SockJS 端点**：`/ws`
- **STOMP**：应用前缀 `/app`，代理前缀 `/topic`、`/user`（与 `WebSocketConfig` 一致）
- Security 已对 `/ws/**` 放行；具体订阅与发送格式见前端 `src/api/websocket.js` 及 `ChatController` / `ChatWebSocketController`

---

## HTTP API 速查

以下为控制器级路由前缀，除标明「公开」外均需登录；**管理员**接口另需 `ROLE_ADMIN`（用户表 `role = 1`）。

| 前缀 | 说明 |
|------|------|
| `POST /api/auth/login`、`POST /api/auth/register` | 登录、注册（公开） |
| `GET /api/items/list`、`GET /api/items/detail/{id}` | 列表、详情（公开） |
| `POST /api/items/publish`、`GET /api/items/my`、`GET /api/items/stats`、`DELETE /api/items/{id}` | 发布、我的、统计、删除（需登录） |
| `POST /api/claims/submit`、`GET /api/claims/item/{itemId}`、`POST /api/claims/audit/{claimId}` | 认领与审核 |
| `GET /api/notifications`、`POST /api/notifications/read/{id}`、`GET /api/notifications/unread-count` | 通知 |
| `POST /api/file/upload` | 文件上传 |
| `GET`/`PUT /api/user/profile`、`GET /api/user/{userId}` | 用户资料 |
| `POST /api/chat/send`、`GET /api/chat/messages/{otherUserId}`、`GET /api/chat/conversations`、`GET /api/chat/unread`、`PUT /api/chat/mark-read/{fromUserId}` | 聊天 REST |
| `GET /api/admin/items`、`POST /api/admin/items/{id}/audit`、`DELETE /api/admin/items/{id}` | 管理帖子 |
| `GET /api/admin/users`、`DELETE /api/admin/users/{id}` | 管理用户 |

统一响应体一般为项目内 `Result` 封装（如 `code`、`message`、`data`）；具体字段以后端 DTO 为准。


