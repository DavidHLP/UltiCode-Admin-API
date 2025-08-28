# 🚀 CodeForge

<div align="center">

![Java 17](https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-6DB33F?logo=spring)
![Vue.js](https://img.shields.io/badge/Vue.js-3.5.17-4FC08D?logo=vuedotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8-3178C6?logo=typescript&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-20.10-2496ED?logo=docker&logoColor=white)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

**一个现代化的在线编程评测系统 | Modern Online Judge Platform**

[功能特性](#-功能特性) • [快速开始](#-快速开始) • [技术架构](#-技术架构) • [项目预览](#-项目预览) • [贡献指南](#-贡献指南)

</div>

---

## 📖 项目简介

**CodeForge** 是一个基于 **Spring Cloud 微服务架构** 的现代化在线编程评测系统，为程序员和算法爱好者提供高效、稳定的编程练习和竞赛平台。

### 🎯 核心亮点

- 🏗️ **微服务架构**：基于 Spring Cloud Alibaba 生态，支持高并发和水平扩展
- 🔒 **安全可靠**：JWT 认证 + Spring Security，多层安全防护
- ⚡ **高性能**：Redis 缓存 + 分布式锁，毫秒级响应
- 🎨 **现代化 UI**：Vue 3 + TypeScript + Element Plus，极致用户体验
- 🐳 **容器化部署**：Docker Compose 一键部署，开箱即用
- 📊 **可观测性**：Zipkin 链路追踪 + Sentinel 流量控制

## 🛠️ 技术架构

### 后端技术栈

<table>
<tr>
<td width="50%">

#### 🏗️ 核心框架
- ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?logo=springboot) 企业级应用开发框架
- ![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-6DB33F?logo=springcloud) 微服务架构解决方案
- ![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2023.0.1.0-6DB33F) 阿里云微服务组件

#### 🗄️ 数据存储
- ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql) 关系型数据库
- ![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.12-000000?logo=mybatis) ORM 框架
- ![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?logo=redis) 缓存/分布式方案
- ![Druid](https://img.shields.io/badge/Alibaba%20Druid-1.2.25-FF6D00?logo=alibabadotcom) 数据库连接池

</td>
<td width="50%">

#### ☁️ 微服务组件
- ![Nacos](https://img.shields.io/badge/Nacos-2.2.3-1E90FF?logo=alibabadotcom) 服务发现与配置中心
- ![Sentinel](https://img.shields.io/badge/Sentinel-1.8.6-1E90FF?logo=alibabadotcom) 流量控制与熔断
- ![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-4.0.6-6DB33F) API 网关
- ![OpenFeign](https://img.shields.io/badge/OpenFeign-4.0.3-6DB33F) 服务间通信

#### 🔧 开发工具
- ![Java](https://img.shields.io/badge/Java-17-007396?logo=java) 编程语言
- ![Maven](https://img.shields.io/badge/Maven-3.9.5-C71A36?logo=apachemaven) 构建工具
- ![JWT](https://img.shields.io/badge/JWT-0.11.5-000000?logo=jsonwebtokens) 认证授权
- ![Zipkin](https://img.shields.io/badge/Zipkin-2.24.3-000000) 链路追踪

</td>
</tr>
</table>

### 前端技术栈

<table>
<tr>
<td width="50%">

#### 🎨 核心框架
- ![Vue.js](https://img.shields.io/badge/Vue.js-3.5.17-4FC08D?logo=vuedotjs) 渐进式 JavaScript 框架
- ![Vite](https://img.shields.io/badge/Vite-7.0.0-646CFF?logo=vite) 下一代前端构建工具
- ![TypeScript](https://img.shields.io/badge/TypeScript-5.8-3178C6?logo=typescript) JavaScript 的超集
- ![Vue Router](https://img.shields.io/badge/Vue%20Router-4.5.1-4FC08D?logo=vuedotjs) 官方路由管理器
- ![Pinia](https://img.shields.io/badge/Pinia-3.0.3-FFD43B?logo=pinia) Vue 状态管理

#### 🎯 UI 组件库
- ![Element Plus](https://img.shields.io/badge/Element%20Plus-2.10.4-409EFF?logo=element) 企业级 UI 组件库
- ![Ant Design Vue](https://img.shields.io/badge/Ant%20Design%20Vue-4.2.6-0170FE?logo=antdesign) 企业级 UI 设计语言
- ![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.4.14-38B2AC?logo=tailwindcss) 实用优先的 CSS 框架

</td>
<td width="50%">

#### ⚙️ 功能组件
- ![Axios](https://img.shields.io/badge/Axios-1.10.0-5A29E4?logo=axios) HTTP 客户端
- ![Monaco Editor](https://img.shields.io/badge/Monaco%20Editor-0.52.2-0078D7?logo=visualstudiocode) VS Code 代码编辑器
- ![Markdown](https://img.shields.io/badge/md--editor--v3-5.8.3-000000?logo=markdown) Markdown 编辑器
- ![ECharts](https://img.shields.io/badge/ECharts-6.0.0-AA344D?logo=apacheecharts) 数据可视化

#### 🔍 开发工具
- ![ESLint](https://img.shields.io/badge/ESLint-8.56.0-4B32C3?logo=eslint) 代码质量检查
- ![Prettier](https://img.shields.io/badge/Prettier-3.2.4-F7B93E?logo=prettier) 代码格式化
- ![Sass](https://img.shields.io/badge/Sass-1.90.0-CC6699?logo=sass) CSS 预处理器

</td>
</tr>
</table>

---

## ✨ 功能特性

### 🎯 用户端功能

| 模块 | 功能描述 | 技术亮点 |
|------|----------|----------|
| 🏠 **首页** | 题目推荐、数据统计、公告展示 | 个性化推荐算法 |
| 📚 **题库** | 题目浏览、分类筛选、难度标记 | 智能分页、实时搜索 |
| 💻 **在线编程** | 多语言支持、实时编译、智能提示 | Monaco Editor 集成 |
| 📊 **提交记录** | 代码提交历史、执行结果、性能分析 | 可视化数据展示 |
| 💡 **题解系统** | 题解发布、点赞评论、Markdown 支持 | 富文本编辑器 |
| 👤 **个人中心** | 用户信息、做题统计、成就系统 | 数据可视化图表 |

### 🛡️ 管理端功能

| 模块 | 功能描述 | 技术亮点 |
|------|----------|----------|
| 👥 **用户管理** | 用户信息维护、权限分配、状态管理 | RBAC 权限模型 |
| 📝 **题目管理** | 题目增删改查、测试用例管理 | 批量操作、数据校验 |
| 🏆 **题解管理** | 题解审核、质量评估、推荐设置 | 内容审核机制 |
| 📈 **数据统计** | 用户活跃度、题目难度分析 | 实时数据大屏 |

### 🏗️ 系统架构特性

- 🔄 **微服务架构**：服务独立部署，支持水平扩展
- 🔐 **安全防护**：JWT 认证 + Spring Security 多层防护
- ⚡ **高性能缓存**：Redis 多级缓存，毫秒级响应
- 📊 **链路追踪**：Zipkin 全链路监控，问题快速定位
- 🎯 **流量控制**：Sentinel 熔断降级，系统稳定性保障
- 🐳 **容器化部署**：Docker Compose 一键部署

## 📸 项目预览

### 🎯 核心功能展示

<table>
<tr>
<td width="50%" align="center">
<img src="images/problem.png" alt="题目页面" width="100%"/>
<br/><strong>📝 题目详情页</strong>
<br/>Monaco Editor 代码编辑器
</td>
<td width="50%" align="center">
<img src="images/home.png" alt="首页" width="100%"/>
<br/><strong>🏠 系统首页</strong>
<br/>数据统计与题目推荐
</td>
</tr>
<tr>
<td width="50%" align="center">
<img src="images/self.png" alt="个人中心" width="100%"/>
<br/><strong>👤 个人中心</strong>
<br/>用户数据可视化
</td>
<td width="50%" align="center">
<img src="images/admin.png" alt="管理后台" width="100%"/>
<br/><strong>🛡️ 管理后台</strong>
<br/>系统管理界面
</td>
</tr>
</table>

### 🎨 用户端界面

> [!TIP]
> 采用现代化设计理念，提供流畅的用户体验

<details>
<summary><strong>📚 题库与编程界面</strong></summary>

#### 题库浏览
![题库界面](images/题库界面.png)
*智能分类筛选，支持多维度搜索*

#### 在线编程
![题目页面](images/题目页面.png)
*集成 Monaco Editor，支持多语言高亮*

</details>

<details>
<summary><strong>💡 题解与讨论系统</strong></summary>

#### 题解浏览
![题解页面](images/题解页面.png)
*Markdown 渲染，支持代码高亮*

#### 题解详情
![题解详细页面](images/题解详细页面.png)
*富文本展示，交互式代码块*

#### 评论系统
![评论系统](images/评论系统.png)
*实时评论，点赞互动*

#### 发布题解
![新增题解](images/新增题解.png)
*所见即所得编辑器*

</details>

<details>
<summary><strong>📊 提交记录与统计</strong></summary>

#### 提交历史
![提交记录页面](images/提交记录页面.png)
*详细的执行结果与性能分析*

#### 提交详情
![提交详细](images/提交详细.png)
*代码回放与错误诊断*

</details>

### 🛡️ 管理端界面

> [!NOTE]
> 基于 RBAC 权限模型，精细化权限控制

<details>
<summary><strong>👥 用户与权限管理</strong></summary>

#### 用户管理
![用户管理](images/用户管理.png)
*用户信息维护与状态管理*

#### 角色管理
![角色管理](images/角色管理.png)
*灵活的角色权限配置*

</details>

<details>
<summary><strong>📝 内容管理</strong></summary>

#### 题目管理
![题目管理](images/题目管理.png)
*题目维护与测试用例管理*

#### 题解管理
![题解管理](images/题解管理.png)
*内容审核与质量控制*

</details>

### TODO
- [ ] 用户个人中心
- [ ] 竞赛
- [ ] 讨论社区