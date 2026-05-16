# 🎓 CampusHub API 规范文档

> **CampusHub** 是一个基于校园场景的互助服务平台，提供需求发布、订单管理、信用评价等核心功能。本文档详细描述了后端 API 接口规范。

| 项目 | 详情 |
| :--- | :--- |
| **基础路径** | `/api/v1` |
| **认证方式** | `JWT Bearer Token` |
| **数据格式** | `JSON` |
| **统一返回** | 见 [附录 A](#a-统一返回格式) |

---

## 📑 目录

1. [🔐 用户认证模块](#1-用户认证模块)
2. [📢 需求发布与浏览模块](#2-需求发布与浏览模块)
3. [📦 订单管理模块](#3-订单管理模块)
4. [⭐ 评价模块](#4-评价模块)
5. [⚠️ 错误码定义](#5-错误码定义)
6. [📎 附录](#附录)

---

## 1. 🔐 用户认证模块

### 1.1 用户注册

> **接口说明**：新用户通过学号或校园邮箱完成注册，系统将自动创建账户并返回访问令牌。

- **URL**: `/api/v1/auth/register`
- **Method**: `POST`
- **Auth**: ❌ 无需认证
- **Limit**: 🛡️ 同一 IP 每分钟最多 **5** 次

#### 📥 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `studentId` | String | ✅ | 学号（8-12位数字） | `"2024010001"` |
| `email` | String | ✅ | 校园邮箱（@xxx.edu.cn） | `"student@xxx.edu.cn"` |
| `password` | String | ✅ | 密码（8-20位，含字母和数字） | `"Abc12345"` |
| `nickname` | String | ✅ | 昵称（2-20字符） | `"小明"` |
| `college` | String | ⭕ | 学院名称 | `"计算机学院"` |

#### 💻 请求示例

{
  "studentId": "2024010001",
  "email": "student@xxx.edu.cn",
  "password": "Abc12345",
  "nickname": "小明",
  "college": "计算机学院"
}

#### 📤 响应示例

**✅ 成功 (200 OK)**

{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "userId": 10001,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "timestamp": 1712345678901
}

**❌ 失败 (400 Bad Request)**

{
  "code": 40001,
  "msg": "学号已存在",
  "data": null,
  "timestamp": 1712345678901
}

---

### 1.2 用户登录

> **接口说明**：已注册用户通过学号/邮箱和密码登录系统，获取访问令牌及基础用户信息。

- **URL**: `/api/v1/auth/login`
- **Method**: `POST`
- **Auth**: ❌ 无需认证
- **Limit**: 🛡️ 同一账号每分钟最多 **3** 次

#### 📥 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `username` | String | ✅ | 学号或校园邮箱 | `"2024010001"` |
| `password` | String | ✅ | 登录密码 | `"Abc12345"` |

#### 💻 请求示例

{
  "username": "2024010001",
  "password": "Abc12345"
}

#### 📤 响应示例

**✅ 成功 (200 OK)**

{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "userId": 10001,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireTime": 7200,
    "userInfo": {
      "nickname": "小明",
      "avatar": "https://cdn.example.com/avatar/10001.jpg",
      "creditScore": 85,
      "isVerified": true
    }
  },
  "timestamp": 1712345678901
}

**❌ 失败 (401 Unauthorized)**

{
  "code": 40101,
  "msg": "用户名或密码错误",
  "data": null,
  "timestamp": 1712345678901
}

---

## 2. 📢 需求发布与浏览模块

### 2.1 发布需求

> **接口说明**：用户发布互助需求或交易需求，需完成校园身份认证。

- **URL**: `/api/v1/demands`
- **Method**: `POST`
- **Auth**: 🔐 需要认证 (Bearer Token)
- **Permission**: ✅ 已完成校园身份认证的用户

#### 📥 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `title` | String | ✅ | 需求标题（5-50字符） | `"代取快递到宿舍"` |
| `description` | String | ✅ | 详细描述（10-500字符） | `"帮忙从南门快递点取两个包裹送到3号楼"` |
| `category` | String | ✅ | 分类枚举值 | `"DELIVERY"` |
| `location` | String | ✅ | 地点描述 | `"南门快递点 -> 3号楼"` |
| `reward` | Decimal | ⭕ | 报酬金额（元），0表示互换 | `15.00` |
| `rewardType` | String | ✅ | 报酬类型：`MONEY`/`EXCHANGE` | `"MONEY"` |
| `deadline` | DateTime | ✅ | 截止时间（ISO 8601） | `"2026-05-20T18:00:00+08:00"` |
| `contactHidden` | Boolean | ⭕ | 是否隐藏联系方式，默认 `true` | `true` |
| `images` | Array[String] | ⭕ | 图片URL列表（最多5张） | `["url1", "url2"]` |

> **Category 枚举参考**: `DELIVERY`(快递), `TUTORING`(辅导), `SECONDHAND`(二手), `ACTIVITY`(活动), `SKILL`(技能), `CARPOOL`(拼车), `OTHER`(其他)

#### 💻 请求示例

{
  "title": "代取快递到宿舍",
  "description": "帮忙从南门快递点取两个包裹送到3号楼，大概2kg左右",
  "category": "DELIVERY",
  "location": "南门快递点 -> 3号楼",
  "reward": 15.00,
  "rewardType": "MONEY",
  "deadline": "2026-05-20T18:00:00+08:00",
  "contactHidden": true,
  "images": []
}

#### 📤 响应示例

**✅ 成功 (201 Created)**

{
  "code": 200,
  "msg": "发布成功",
  "data": {
    "demandId": 50001,
    "status": "PENDING",
    "publishTime": "2026-05-17T16:30:00+08:00"
  },
  "timestamp": 1712345678901
}

**❌ 失败 (400 Bad Request)**

{
  "code": 40002,
  "msg": "请先完成校园身份认证",
  "data": null,
  "timestamp": 1712345678901
}

---

### 2.2 浏览需求列表（含筛选）

> **接口说明**：分页查询需求列表，支持多维度筛选与排序。未登录用户仅可查看公开信息。

- **URL**: `/api/v1/demands`
- **Method**: `GET`
- **Auth**: ⚪ 可选认证

#### 📥 请求参数 (Query Params)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `page` | Integer | ⭕ | 页码，默认 1 | `1` |
| `pageSize` | Integer | ⭕ | 每页数量，默认 20，最大 100 | `20` |
| `category` | String | ⭕ | 分类筛选 | `"DELIVERY"` |
| `status` | String | ⭕ | 状态筛选 | `"PENDING"` |
| `keyword` | String | ⭕ | 关键字搜索（标题/描述） | `"快递"` |
| `location` | String | ⭕ | 地点模糊匹配 | `"南门"` |
| `sortBy` | String | ⭕ | 排序字段：`TIME`/`REWARD`/`CREDIT` | `"TIME"` |
| `sortOrder` | String | ⭕ | 排序方向：`ASC`/`DESC`，默认 `DESC` | `"DESC"` |
| `minReward` | Decimal | ⭕ | 最小报酬 | `10.00` |
| `maxReward` | Decimal | ⭕ | 最大报酬 | `50.00` |

请求示例：

GET /api/v1/demands?page=1&pageSize=20&category=DELIVERY&status=PENDING&sortBy=TIME&sortOrder=DESC

#### 📤 响应示例

**✅ 成功 (200 OK)**

{
"code": 200,
"msg": "查询成功",
"data": {
"total": 156,
"page": 1,
"pageSize": 20,
"list": [
{
"demandId": 50001,
"title": "代取快递到宿舍",
"category": "DELIVERY",
"reward": 15.00,
"rewardType": "MONEY",
"location": "南门快递点 -> 3号楼",
"status": "PENDING",
"publisher": {
"userId": 10001,
"nickname": "小明",
"avatar": "https://cdn.example.com/avatar/10001.jpg",
"creditScore": 85,
"isVerified": true
},
"publishTime": "2026-05-17T16:30:00+08:00",
"deadline": "2026-05-20T18:00:00+08:00",
"viewCount": 23,
"applyCount": 2
}
]
},
"timestamp": 1712345678901
}
---

### 2.3 查看需求详情

> **接口说明**：获取单条需求的完整详细信息，包括发布者信誉、图片及联系信息可见性状态。

- **URL**: `/api/v1/demands/{demandId}`
- **Method**: `GET`
- **Auth**: ⚪ 可选认证（未登录时联系方式不可见）

#### 📥 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `demandId` | Long | ✅ | 需求唯一标识 ID | `50001` |

#### 📤 响应示例

**✅ 成功 (200 OK)**

{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "demandId": 50001,
    "title": "代取快递到宿舍",
    "description": "帮忙从南门快递点取两个包裹送到3号楼，大概2kg左右",
    "category": "DELIVERY",
    "reward": 15.00,
    "rewardType": "MONEY",
    "location": "南门快递点 -> 3号楼",
    "status": "PENDING",
    "publisher": {
      "userId": 10001,
      "nickname": "小明",
      "avatar": "https://cdn.example.com/avatar/10001.jpg",
      "creditScore": 85,
      "isVerified": true,
      "completedOrders": 12,
      "rating": 4.8
    },
    "publishTime": "2026-05-17T16:30:00+08:00",
    "deadline": "2026-05-20T18:00:00+08:00",
    "images": [],
    "viewCount": 23,
    "applyCount": 2,
    "contactInfo": {
      "visible": false,
      "message": "接单后可见联系方式"
    }
  },
  "timestamp": 1712345678901
}

**❌ 失败 (404 Not Found)**

{
  "code": 40401,
  "msg": "需求不存在",
  "data": null,
  "timestamp": 1712345678901
}

---

## 3. 📦 订单管理模块

### 3.1 接单申请

> **接口说明**：服务方对目标需求发起接单申请。系统会自动校验信用分及未完成订单数量。

- **URL**: `/api/v1/orders/apply`
- **Method**: `POST`
- **Auth**: 🔐 需要认证
- **Permission**: ✅ 信用分 ≥ 60 且 未完成订单数 < 5

#### 📥 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `demandId` | Long | ✅ | 需求 ID | `50001` |
| `message` | String | ⭕ | 申请留言（0-200字符） | `"我可以帮你取，现在就在南门附近"` |

#### 💻 请求示例

{
  "demandId": 50001,
  "message": "我可以帮你取，现在就在南门附近"
}

#### 📤 响应示例

**✅ 成功 (200 OK)**

{
  "code": 200,
  "msg": "申请成功，等待发布者确认",
  "data": {
    "applyId": 60001,
    "status": "PENDING_CONFIRM"
  },
  "timestamp": 1712345678901
}

**❌ 失败 (400 Bad Request)**

{
  "code": 40003,
  "msg": "您的信用分不足，无法接单",
  "data": null,
  "timestamp": 1712345678901
}

**❌ 冲突 (409 Conflict)**

{
  "code": 40901,
  "msg": "该需求已被接单",
  "data": null,
  "timestamp": 1712345678901
}

---

### 3.2 确认接单

> **接口说明**：需求发布者从申请者列表中选择一个服务方，正式生成订单。

- **URL**: `/api/v1/orders/confirm/{applyId}`
- **Method**: `POST`
- **Auth**: 🔐 需要认证
- **Permission**: ✅ 仅需求发布者可操作

#### 📥 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `applyId` | Long | ✅ | 申请记录 ID | `60001` |

#### 📤 响应示例

**✅ 成功 (200 OK)**

{
  "code": 200,
  "msg": "接单成功",
  "data": {
    "orderId": 70001,
    "status": "IN_PROGRESS",
    "provider": {
      "userId": 10002,
      "nickname": "小红",
      "creditScore": 92
    },
    "createTime": "2026-05-17T17:00:00+08:00"
  },
  "timestamp": 1712345678901
}

---

### 3.3 查看订单详情

> **接口说明**：查看订单的详细信息、双方信息及状态流转历史。

- **URL**: `/api/v1/orders/{orderId}`
- **Method**: `GET`
- **Auth**: 🔐 需要认证
- **Permission**: ✅ 仅订单参与方（发布者或服务方）可查看

#### 📥 路径参数

| 参数名 | 类型 | 必填 |
| :--- | :---: | :--- |
| `orderId` | Long | ✅ |

#### 📤 响应示例

**✅ 成功 (200 OK)**

{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "orderId": 70001,
    "demandId": 50001,
    "status": "IN_PROGRESS",
    "publisher": {
      "userId": 10001,
      "nickname": "小明",
      "avatar": "https://cdn.example.com/avatar/10001.jpg"
    },
    "provider": {
      "userId": 10002,
      "nickname": "小红",
      "avatar": "https://cdn.example.com/avatar/10002.jpg",
      "creditScore": 92
    },
    "reward": 15.00,
    "createTime": "2026-05-17T17:00:00+08:00",
    "finishTime": null,
    "history": [
      {
        "status": "PENDING_CONFIRM",
        "time": "2026-05-17T16:45:00+08:00",
        "desc": "用户申请接单"
      },
      {
        "status": "IN_PROGRESS",
        "time": "2026-05-17T17:00:00+08:00",
        "desc": "发布者确认接单"
      }
    ]
  },
  "timestamp": 1712345678901
}

---

### 3.4 完成订单

> **接口说明**：服务方完成任务后点击“完成”，进入待评价状态。需双方确认或超时自动确认。

- **URL**: `/api/v1/orders/{orderId}/complete`
- **Method**: `POST`
- **Auth**: 🔐 需要认证
- **Permission**: ✅ 仅服务方可操作

#### 📥 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `orderId` | Long | ✅ | 订单 ID | `70001` |

#### 📤 响应示例

**✅ 成功 (200 OK)**

{
  "code": 200,
  "msg": "订单已完成，等待评价",
  "data": {
    "orderId": 70001,
    "status": "COMPLETED"
  },
  "timestamp": 1712345678901
}

---

## 4. ⭐ 评价模块

### 4.1 提交评价

> **接口说明**：订单完成后，双方可互相进行评分和文字评价。

- **URL**: `/api/v1/reviews`
- **Method**: `POST`
- **Auth**: 🔐 需要认证
- **Permission**: ✅ 仅订单参与方可操作，且订单状态为 `COMPLETED`

#### 📥 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `orderId` | Long | ✅ | 关联订单 ID | `70001` |
| `targetUserId` | Long | ✅ | 被评价人 ID | `10002` |
| `rating` | Integer | ✅ | 评分（1-5星） | `5` |
| `content` | String | ⭕ | 评价内容（0-200字符） | `"非常准时，态度很好"` |
| `tags` | Array[String] | ⭕ | 标签列表 | `["守时", "友好"]` |

#### 💻 请求示例

{
  "orderId": 70001,
  "targetUserId": 10002,
  "rating": 5,
  "content": "非常准时，态度很好",
  "tags": ["守时", "友好"]
}

#### 📤 响应示例

**✅ 成功 (200 OK)**

{
  "code": 200,
  "msg": "评价成功",
  "data": {
    "reviewId": 80001
  },
  "timestamp": 1712345678901
}

**❌ 失败 (400 Bad Request)**

{
  "code": 40005,
  "msg": "您已对该订单进行过评价",
  "data": null,
  "timestamp": 1712345678901
}

---

### 4.2 查看用户评价列表

> **接口说明**：分页查看某用户收到的所有评价。

- **URL**: `/api/v1/reviews/user/{userId}`
- **Method**: `GET`
- **Auth**: ⚪ 可选认证

#### 📥 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `userId` | Long | ✅ | 用户 ID | `10002` |

#### 📥 请求参数 (Query Params)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
| :--- | :--- | :---: | :--- | :--- |
| `page` | Integer | ⭕ | 页码，默认 1 | `1` |
| `pageSize` | Integer | ⭕ | 每页数量，默认 10 | `10` |

#### 📤 响应示例

**✅ 成功 (200 OK)**

{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "total": 12,
    "page": 1,
    "pageSize": 10,
    "list": [
      {
        "reviewId": 80001,
        "orderId": 70001,
        "reviewer": {
          "userId": 10001,
          "nickname": "小明",
          "avatar": "https://cdn.example.com/avatar/10001.jpg"
        },
        "rating": 5,
        "content": "非常准时，态度很好",
        "tags": ["守时", "友好"],
        "createTime": "2026-05-17T18:00:00+08:00"
      }
    ]
  },
  "timestamp": 1712345678901
}

---

## 5. ⚠️ 错误码定义

系统采用统一的错误码规范，格式为 `HTTP状态码 + 两位业务序号`。

| 错误码 | HTTP Status | 说明 | 解决方案 |
| :--- | :---: | :--- | :--- |
| `200` | 200 | 成功 | - |
| `40001` | 400 | 学号/邮箱已存在 | 更换注册信息 |
| `40002` | 400 | 参数校验失败 | 检查请求参数格式 |
| `40003` | 400 | 信用分不足 | 提升信用分或完成实名认证 |
| `40101` | 401 | 认证失败 | 检查 Token 是否有效或过期 |
| `40301` | 403 | 权限不足 | 确认当前用户是否有操作权限 |
| `40401` | 404 | 资源不存在 | 检查 ID 是否正确 |
| `40901` | 409 | 资源冲突 | 例如重复接单、重复评价 |
| `42901` | 429 | 请求过于频繁 | 稍后再试 |
| `50001` | 500 | 服务器内部错误 | 联系管理员 |

---

## 6. 📎 附录

### A. 统一返回格式

所有 API 接口均遵循以下 JSON 返回结构：

{
  "code": 200,          // 业务状态码
  "msg": "success",     // 提示信息
  "data": {},           // 业务数据，失败时为 null
  "timestamp": 1712345678901 // 服务器响应时间戳
}

### B. 数据字典

#### 需求分类 (Category)
- `DELIVERY`: 快递代取
- `TUTORING`: 学业辅导
- `SECONDHAND`: 二手交易
- `ACTIVITY`: 活动组队
- `SKILL`: 技能服务
- `CARPOOL`: 拼车出行
- `OTHER`: 其他

#### 订单状态 (Order Status)
- `PENDING_CONFIRM`: 待确认（已申请，等待发布者选择）
- `IN_PROGRESS`: 进行中（已确认接单）
- `COMPLETED`: 已完成（服务结束，待评价）
- `CANCELLED`: 已取消
- `DISPUTED`: 争议中

#### 报酬类型 (Reward Type)
- `MONEY`: 现金报酬
- `EXCHANGE`: 物品互换/人情互助