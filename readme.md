# CampusHub - 校园互助服务平台

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue.svg" alt="Java Version">
  <img src="https://img.shields.io/badge/Maven-3.6+-orange.svg" alt="Maven Version">
  <img src="https://img.shields.io/badge/GitHub%20Actions-CI-green.svg" alt="CI Status">
  <img src="https://img.shields.io/badge/License-MIT-lightgrey.svg" alt="License">
</p>

---

## 📖 项目简介
大学校园中，学生之间存在大量日常互助需求：快递代取、学习辅导、二手物品交换、活动组队等。目前这些需求散落在微信群、QQ 群、贴吧等平台，信息碎片化、匹配效率低、缺乏信任机制。

CampusHub 旨在提供一个统一的校园互助服务平台，让学生能够方便地发布需求、发现匹配、建立信任、完成协作，解决校园场景下的信息不对称问题。

---

## 👥 用户角色
| 角色 | 描述 |
|------|------|
| 需求方 | 发布互助需求的学生（如“需要人帮忙取快递”“寻求高数辅导”） |
| 服务方 | 接单并提供服务的学生（如“我可以帮忙取快递”“可提供Python入门辅导”） |
| 管理员 | 平台管理人员，负责内容审核、用户管理、数据统计与系统维护 |

> 说明：同一个用户可以同时注册并担任需求方和服务方两种角色。

---

## 🛠️ 技术栈
### 后端
- 语言：Java 17
- 构建工具：Apache Maven 3.6+
- 版本控制：Git + GitHub
- 自动化CI：GitHub Actions（代码编译+规范检查）

### 开发工具
- IDE：IntelliJ IDEA / VS Code
- 协作工具：GitHub Issues、Projects

---

## 👨‍💻 团队成员
| 角色 | 姓名 | 职责 |
|------|------|------|
| 组长 | XXX | 项目整体设计、进度管理、核心模块开发 |
| 后端开发 | XXX | 用户模块、需求发布模块开发 |
| 后端开发 | XXX | 服务匹配、订单流程模块开发 |
| 测试与文档 | XXX | 测试用例编写、接口文档维护 |

---

## 📁 项目结构
campusHub/
├── src/
│ └── main/
│ └── java/com/example/campushub/
│ └── Main.java # 项目入口
├── pom.xml # Maven 核心配置文件
├── .github/
│ └── workflows/
│ └── ci.yml # GitHub Actions CI 流水线配置
├── README.md # 项目说明文档
├── ENV_SETUP.md # 开发环境配置文档
└── .gitignore # Git 忽略文件配置

