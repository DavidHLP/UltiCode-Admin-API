/*
 Navicat Premium Dump SQL

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : 127.0.0.1:3306
 Source Schema         : spring_oj

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 21/07/2025 14:58:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for problems
-- ----------------------------
DROP TABLE IF EXISTS `problems`;
CREATE TABLE `problems` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID，主键，自动增长',
  `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目标题，不能为空',
  `description` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目正文描述，不能为空',
  `time_limit` int DEFAULT '1000' COMMENT '时间限制，单位为毫秒，默认为1000ms',
  `memory_limit` int DEFAULT '128' COMMENT '内存限制，单位为MB，默认为128MB',
  `difficulty` enum('Easy','Medium','Hard') COLLATE utf8mb4_general_ci DEFAULT 'Easy' COMMENT '题目难度，枚举类型，默认为''Easy''',
  `category` enum('Algorithms','Database','Shell','Multi-threading','JavaScript','Pandas') DEFAULT 'Algorithms' COMMENT '题目所属的大分类标签',
  `tags` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目标签，建议使用JSON格式的字符串存储，例如：''["数组", "动态规划"]''',
  `solved_count` int DEFAULT '0' COMMENT '成功解答的次数，默认为0',
  `submission_count` int DEFAULT '0' COMMENT '总提交次数，默认为0',
  `created_by` bigint DEFAULT NULL COMMENT '题目创建者的用户ID',
  `is_visible` tinyint(1) DEFAULT '1' COMMENT '题目是否对普通用户可见，默认为TRUE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间，当记录被更新时自动更新为当前时间戳',
  PRIMARY KEY (`id`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `problems_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `status` int DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for submissions
-- ----------------------------
DROP TABLE IF EXISTS `submissions`;
CREATE TABLE `submissions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交记录ID，主键，自动增长',
  `user_id` bigint NOT NULL COMMENT '提交用户的ID，关联到users表',
  `problem_id` bigint NOT NULL COMMENT '题目的ID，关联到problems表',
  `language` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '编程语言，例如：''cpp'', ''java'', ''python''',
  `source_code` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户提交的源代码',
  `status` enum('Pending','Judging','Accepted','Wrong Answer','Time Limit Exceeded','Memory Limit Exceeded','Runtime Error','Compile Error','System Error') COLLATE utf8mb4_general_ci DEFAULT 'Pending' COMMENT '判题状态，默认为''Pending''',
  `score` int DEFAULT '0' COMMENT '得分，默认为0',
  `time_used` int DEFAULT '0' COMMENT '程序执行耗时，单位为毫秒',
  `memory_used` int DEFAULT '0' COMMENT '程序执行内存消耗，单位为KB',
  `compile_info` text COLLATE utf8mb4_general_ci COMMENT '编译错误时的详细信息',
  `judge_info` text COLLATE utf8mb4_general_ci COMMENT '判题的详细信息，建议使用JSON格式存储',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `problem_id` (`problem_id`),
  CONSTRAINT `submissions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `submissions_ibfk_2` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for test_cases
-- ----------------------------
DROP TABLE IF EXISTS `test_cases`;
CREATE TABLE `test_cases` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试用例ID，主键，自动增长',
  `problem_id` bigint NOT NULL COMMENT '关联的题目ID，外键，关联到problems表',
  `input` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '输入',
  `output` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '期望的输出',
  `score` int DEFAULT '10' COMMENT '该测试点的分值，默认为10',
  `is_sample` tinyint(1) DEFAULT '0' COMMENT '是否为样例测试用例，默认为FALSE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
  PRIMARY KEY (`id`),
  KEY `problem_id` (`problem_id`),
  CONSTRAINT `test_cases_ibfk_1` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for token
-- ----------------------------
DROP TABLE IF EXISTS `token`;
CREATE TABLE `token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `token_type` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `expired` tinyint(1) NOT NULL,
  `revoked` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `token_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1946895459710959619 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `introduction` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '用户未填写',
  `address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '用户未填写',
  `status` int DEFAULT '1',
  `last_login_ip` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `last_login` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
