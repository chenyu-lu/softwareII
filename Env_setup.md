# 开发环境配置文档

## 1. 项目概述
本项目采用 Git + GitHub + GitHub Actions 进行项目管理与自动化构建，基于 Java + Maven 技术栈开发。

---

## 2. 环境要求
### 2.1 基础环境
- JDK 17
- Maven 3.6+
- Git 2.0+


### 2.2 开发工具
- IntelliJ IDEA 
- VS Code

---

## 3. 项目仓库信息
- 代码仓库：GitHub
- 默认主分支：`main`
- 开发集成分支：`dev`
- 功能开发分支：`feature/login`

---

## 4. 分支规范（必须遵守）
- **main**：生产稳定分支，仅用于版本发布
- **dev**：开发集成分支，所有功能合并到此
- **feature/login**：功能分支，从 dev 拉出，开发完成后合并回 dev

---

## 5. 环境搭建步骤
### 5.1 克隆项目
```bash
git clone 仓库地址
cd 项目目录
