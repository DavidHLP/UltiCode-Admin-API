# CodeForge

![Java 17](https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-6DB33F?logo=spring)
![Vue.js](https://img.shields.io/badge/Vue.js-3.5.17-4FC08D?logo=vuedotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8-3178C6?logo=typescript&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-20.10-2496ED?logo=docker&logoColor=white)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## 项目简介

CodeForge 是一个基于 Spring Cloud 微服务架构的在线编程评测系统，旨在为程序员和算法爱好者提供一个高效、稳定的编程练习和竞赛平台。系统采用前后端分离架构，后端使用 Spring Cloud Alibaba 生态构建，前端采用 Vue 3 技术栈实现。

## 后端技术栈

| 技术领域 | 核心技术 | 版本 / 说明 |
| :--- | :--- |:-------------------------------------------------|
| **核心框架** | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?logo=springboot) | 企业级应用开发框架 |
| | ![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-6DB33F?logo=springcloud) | 微服务架构解决方案 |
| | ![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2023.0.1.0-6DB33F) | 阿里云微服务组件 |
| **数据库与持久层** | ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql) | 关系型数据库 |
| | ![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.12-000000?logo=mybatis) | ORM 框架 |
| | ![Druid](https://img.shields.io/badge/Alibaba%20Druid-1.2.25-FF6D00?logo=alibabadotcom) | 数据库连接池 |
| | ![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?logo=redis) | 缓存/分布式方案 |
| | ![Redisson](https://img.shields.io/badge/Redisson-3.17.6-FF0000) | Redis 客户端 |
| **微服务组件** | ![Nacos](https://img.shields.io/badge/Nacos-2.2.3-1E90FF?logo=alibabadotcom) | 服务发现与配置中心 |
| | ![Sentinel](https://img.shields.io/badge/Sentinel-1.8.6-1E90FF?logo=alibabadotcom) | 流量控制与熔断 |
| | ![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-4.0.6-6DB33F) | API 网关 |
| | ![OpenFeign](https://img.shields.io/badge/OpenFeign-4.0.3-6DB33F) | 服务间 HTTP 客户端 |
| | ![Micrometer](https://img.shields.io/badge/Micrometer-1.11.5-000000) + ![Zipkin](https://img.shields.io/badge/Zipkin-2.24.3-000000) | 链路追踪 |
| **安全与认证** | ![JWT](https://img.shields.io/badge/JWT-0.11.5-000000?logo=jsonwebtokens) | 认证授权 |
| **开发与辅助工具** | ![Java](https://img.shields.io/badge/Java-17-007396?logo=java) | 编程语言 |
| | ![Maven](https://img.shields.io/badge/Maven-3.9.5-C71A36?logo=apachemaven) | 构建工具 |
| | API 文档 | Swagger 3 (SpringDoc) |
| | 常用库 | Lombok, Hutool, Fastjson2 |

## 前端技术栈

| 技术领域 | 核心技术 | 说明 |
| :--- | :--- |:-------------------------------------------------|
| **核心框架与生态** | ![Vue.js](https://img.shields.io/badge/Vue.js-3.5.17-4FC08D?logo=vuedotjs) | 渐进式 JavaScript 框架 |
| | ![Vite](https://img.shields.io/badge/Vite-7.0.0-646CFF?logo=vite) | 下一代前端构建工具 |
| | ![TypeScript](https://img.shields.io/badge/TypeScript-5.8-3178C6?logo=typescript) | JavaScript 的超集 |
| | ![Vue Router](https://img.shields.io/badge/Vue%20Router-4.5.1-4FC08D?logo=vuedotjs) | 官方路由管理器 |
| | ![Pinia](https://img.shields.io/badge/Pinia-3.0.3-FFD43B?logo=pinia) | Vue 状态管理 |
| **UI 与样式** | ![Element Plus](https://img.shields.io/badge/Element%20Plus-2.10.4-409EFF?logo=element) | 企业级 UI 组件库 |
| | ![Ant Design Vue](https://img.shields.io/badge/Ant%20Design%20Vue-4.2.6-0170FE?logo=antdesign) | 企业级 UI 设计语言 |
| | ![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.4.14-38B2AC?logo=tailwindcss) | 实用优先的 CSS 框架 |
| **功能库与工具** | ![Axios](https://img.shields.io/badge/Axios-1.10.0-5A29E4?logo=axios) | 基于 Promise 的 HTTP 客户端 |
| | ![Monaco Editor](https://img.shields.io/badge/Monaco%20Editor-0.52.2-0078D7?logo=visualstudiocode) | VS Code 代码编辑器 |
| | ![Markdown](https://img.shields.io/badge/md--editor--v3-5.8.3-000000?logo=markdown) | Markdown 编辑器组件 |
| | ![ESLint](https://img.shields.io/badge/ESLint-8.56.0-4B32C3?logo=eslint) + ![Prettier](https://img.shields.io/badge/Prettier-3.2.4-F7B93E?logo=prettier) | 代码质量与格式化工具 |

## 项目预览

### 业务端项目预览

#### 1. 题库界面
![img.png](images/题库界面.png)
#### 2. 题目页面
![img.png](images/题目页面.png)
#### 3. 题解页面
![img.png](images/题解页面.png)
![img.png](images/题解详细页面.png)
![img.png](images/评论系统.png)
![img.png](images/新增题解.png)
#### 4. 提交记录页面
![img.png](images/提交记录页面.png)
![img.png](images/提交详细.png)
![img.png](images/编译信息.png)
#### 5. 登录页面
![img.png](images/登录页面.png)

### 管理端项目预览

#### 1. 基本的权限用户管理
![img.png](images/用户管理.png)
![img.png](images/角色管理.png)
#### 2. 题目管理
![img.png](images/题目管理.png)
#### 3. 题解管理
![img.png](images/题解管理.png)
#### 4. 登录页面
![img.png](images/管理端登录页面.png)
