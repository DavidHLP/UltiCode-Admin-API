/*
 Navicat Premium Dump SQL

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 80406 (8.4.6)
 Source Host           : 127.0.0.1:3306
 Source Schema         : spring_oj

 Target Server Type    : MySQL
 Target Server Version : 80406 (8.4.6)
 File Encoding         : 65001

 Date: 13/10/2025 13:32:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of categories
-- ----------------------------
BEGIN;
INSERT INTO `categories` (`id`, `code`, `name`) VALUES (1, '94101', '算法');
COMMIT;

-- ----------------------------
-- Table structure for difficulties
-- ----------------------------
DROP TABLE IF EXISTS `difficulties`;
CREATE TABLE `difficulties` (
  `id` tinyint NOT NULL,
  `code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_key` tinyint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of difficulties
-- ----------------------------
BEGIN;
INSERT INTO `difficulties` (`id`, `code`, `sort_key`) VALUES (1, 'EASY', 1);
INSERT INTO `difficulties` (`id`, `code`, `sort_key`) VALUES (2, 'MEDIUM', 2);
INSERT INTO `difficulties` (`id`, `code`, `sort_key`) VALUES (3, 'HARD', 3);
COMMIT;

-- ----------------------------
-- Table structure for problem_language_configs
-- ----------------------------
DROP TABLE IF EXISTS `problem_language_configs`;
CREATE TABLE `problem_language_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `problem_id` bigint NOT NULL,
  `language` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `function_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `starter_code` mediumtext COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_problem_language` (`problem_id`,`language`),
  CONSTRAINT `fk_plc_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of problem_language_configs
-- ----------------------------
BEGIN;
INSERT INTO `problem_language_configs` (`id`, `problem_id`, `language`, `function_name`, `starter_code`) VALUES (1, 2, 'java', 'twoSum', 'class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        \n    }\n}');
COMMIT;

-- ----------------------------
-- Table structure for problem_locales
-- ----------------------------
DROP TABLE IF EXISTS `problem_locales`;
CREATE TABLE `problem_locales` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `problem_id` bigint NOT NULL,
  `lang_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description_md` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `constraints_md` mediumtext COLLATE utf8mb4_unicode_ci,
  `examples_md` mediumtext COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_problem_lang` (`problem_id`,`lang_code`),
  CONSTRAINT `fk_pl_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of problem_locales
-- ----------------------------
BEGIN;
INSERT INTO `problem_locales` (`id`, `problem_id`, `lang_code`, `title`, `description_md`, `constraints_md`, `examples_md`, `created_at`, `updated_at`) VALUES (1, 2, 'zh-CN', '两数之和', '# 1. 两数之和\n\n**难度：简单**\n\n**相关标签：** 数组、哈希表\n\n-----\n\n## 题目描述\n\n给定一个整数数组 `nums` 和一个整数目标值 `target`，请你在该数组中找出 **和为目标值** *`target`* 的那 **两个** 整数，并返回它们的数组下标。\n\n你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。\n\n你可以按任意顺序返回答案。', '* $2 \\le \\text{nums.length} \\le 10^4$\n* $-10^9 \\le \\text{nums[i]} \\le 10^9$\n* $-10^9 \\le \\text{target} \\le 10^9$\n* **只会存在一个有效答案**\n', '### 示例 1：\n\n```\n输入：nums = [2,7,11,15], target = 9\n输出：[0,1]\n解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。\n```\n\n### 示例 2：\n\n```\n输入：nums = [3,2,4], target = 6\n输出：[1,2]\n```\n\n### 示例 3：\n\n```\n输入：nums = [3,3], target = 6\n输出：[0,1]\n```\n\n-----\n\n## 提示', '2025-10-12 10:24:20', '2025-10-12 10:24:20');
COMMIT;

-- ----------------------------
-- Table structure for problem_stats
-- ----------------------------
DROP TABLE IF EXISTS `problem_stats`;
CREATE TABLE `problem_stats` (
  `problem_id` bigint NOT NULL,
  `solved_count` int NOT NULL DEFAULT '0',
  `submission_count` int NOT NULL DEFAULT '0',
  `likes_count` int NOT NULL DEFAULT '0',
  `dislikes_count` int NOT NULL DEFAULT '0',
  `acceptance_rate` decimal(5,2) GENERATED ALWAYS AS ((case when (`submission_count` = 0) then NULL else round(((`solved_count` / `submission_count`) * 100),2) end)) VIRTUAL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`problem_id`),
  CONSTRAINT `fk_ps_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of problem_stats
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for problem_tags
-- ----------------------------
DROP TABLE IF EXISTS `problem_tags`;
CREATE TABLE `problem_tags` (
  `problem_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`problem_id`,`tag_id`),
  KEY `fk_pt_tag` (`tag_id`),
  CONSTRAINT `fk_pt_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pt_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of problem_tags
-- ----------------------------
BEGIN;
INSERT INTO `problem_tags` (`problem_id`, `tag_id`) VALUES (2, 1);
INSERT INTO `problem_tags` (`problem_id`, `tag_id`) VALUES (2, 2);
COMMIT;

-- ----------------------------
-- Table structure for problems
-- ----------------------------
DROP TABLE IF EXISTS `problems`;
CREATE TABLE `problems` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `slug` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '人类可读短链接/编号，如 two-sum / 1-two-sum',
  `problem_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '题目类型（coding/SQL/多线程/…）',
  `difficulty_id` tinyint NOT NULL,
  `category_id` smallint DEFAULT NULL,
  `solution_entry` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '运行/判题入口函数名（语言无关的逻辑名）',
  `time_limit_ms` int DEFAULT NULL,
  `memory_limit_kb` int DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `is_visible` tinyint(1) NOT NULL DEFAULT '1',
  `meta_json` json DEFAULT NULL COMMENT '自由扩展元数据（公司、频次、来源等）',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `fk_problem_creator` (`created_by`),
  KEY `idx_problems_visibility` (`is_visible`),
  KEY `idx_problems_difficulty` (`difficulty_id`,`id`),
  KEY `idx_problems_category` (`category_id`,`id`),
  CONSTRAINT `fk_problem_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `fk_problem_creator` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_problem_difficulty` FOREIGN KEY (`difficulty_id`) REFERENCES `difficulties` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of problems
-- ----------------------------
BEGIN;
INSERT INTO `problems` (`id`, `slug`, `problem_type`, `difficulty_id`, `category_id`, `solution_entry`, `time_limit_ms`, `memory_limit_kb`, `created_by`, `is_visible`, `meta_json`, `created_at`, `updated_at`) VALUES (2, 'two-sum', 'ALGO', 1, 1, NULL, 1000, 512, NULL, 1, NULL, '2025-10-12 10:20:26', '2025-10-12 10:20:26');
COMMIT;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `status` int DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of role
-- ----------------------------
BEGIN;
INSERT INTO `role` (`id`, `role_name`, `status`, `remark`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (8, 'ADMIN', 1, '超级管理员', '2025-08-21 10:28:37', '2025-10-11 20:21:59', NULL, NULL);
INSERT INTO `role` (`id`, `role_name`, `status`, `remark`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (9, 'USER', 1, '基本用户', '2025-08-21 23:28:51', '2025-08-21 23:28:51', NULL, NULL);
INSERT INTO `role` (`id`, `role_name`, `status`, `remark`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (10, 'MANAGE', 1, '管理员', '2025-08-20 21:29:05', '2025-08-20 21:29:05', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `slug` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of tags
-- ----------------------------
BEGIN;
INSERT INTO `tags` (`id`, `slug`, `name`, `created_at`, `updated_at`) VALUES (1, 'array', '数组', '2025-10-13 12:54:01', '2025-10-13 12:54:01');
INSERT INTO `tags` (`id`, `slug`, `name`, `created_at`, `updated_at`) VALUES (2, 'hash-table', '哈希表', '2025-10-13 12:54:01', '2025-10-13 12:54:01');
COMMIT;

-- ----------------------------
-- Table structure for testcase_groups
-- ----------------------------
DROP TABLE IF EXISTS `testcase_groups`;
CREATE TABLE `testcase_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `problem_id` bigint NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_sample` tinyint(1) NOT NULL DEFAULT '0',
  `weight` int NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tcg_problem` (`problem_id`,`is_sample`),
  CONSTRAINT `fk_tcg_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of testcase_groups
-- ----------------------------
BEGIN;
INSERT INTO `testcase_groups` (`id`, `problem_id`, `name`, `is_sample`, `weight`, `created_at`, `updated_at`) VALUES (15, 2, 'Samples', 1, 1, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcase_groups` (`id`, `problem_id`, `name`, `is_sample`, `weight`, `created_at`, `updated_at`) VALUES (16, 2, 'Basics', 0, 2, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcase_groups` (`id`, `problem_id`, `name`, `is_sample`, `weight`, `created_at`, `updated_at`) VALUES (17, 2, 'Ambiguity', 0, 2, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcase_groups` (`id`, `problem_id`, `name`, `is_sample`, `weight`, `created_at`, `updated_at`) VALUES (18, 2, 'Edge Cases', 0, 3, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcase_groups` (`id`, `problem_id`, `name`, `is_sample`, `weight`, `created_at`, `updated_at`) VALUES (19, 2, 'Performance', 0, 4, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcase_groups` (`id`, `problem_id`, `name`, `is_sample`, `weight`, `created_at`, `updated_at`) VALUES (20, 2, 'No Solution', 0, 1, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
COMMIT;

-- ----------------------------
-- Table structure for testcase_steps
-- ----------------------------
DROP TABLE IF EXISTS `testcase_steps`;
CREATE TABLE `testcase_steps` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `testcase_id` bigint NOT NULL,
  `step_index` int NOT NULL DEFAULT '0',
  `input_content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `expected_output` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tc_step` (`testcase_id`,`step_index`),
  CONSTRAINT `fk_tcs_tc` FOREIGN KEY (`testcase_id`) REFERENCES `testcases` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of testcase_steps
-- ----------------------------
BEGIN;
INSERT INTO `testcase_steps` (`id`, `testcase_id`, `step_index`, `input_content`, `expected_output`, `created_at`, `updated_at`) VALUES (1, 35, 0, 'nums=[2,7,11,15]', NULL, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcase_steps` (`id`, `testcase_id`, `step_index`, `input_content`, `expected_output`, `created_at`, `updated_at`) VALUES (2, 35, 1, 'target=9', NULL, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcase_steps` (`id`, `testcase_id`, `step_index`, `input_content`, `expected_output`, `created_at`, `updated_at`) VALUES (3, 35, 2, '请输出两个索引（升序）', '[0,1]', '2025-10-13 13:14:29', '2025-10-13 13:14:29');
COMMIT;

-- ----------------------------
-- Table structure for testcases
-- ----------------------------
DROP TABLE IF EXISTS `testcases`;
CREATE TABLE `testcases` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL,
  `order_index` int NOT NULL DEFAULT '0',
  `input_json` json NOT NULL,
  `output_json` json NOT NULL,
  `output_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `score` int NOT NULL DEFAULT '10',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tc_group_order` (`group_id`,`order_index`),
  CONSTRAINT `fk_tc_group` FOREIGN KEY (`group_id`) REFERENCES `testcase_groups` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of testcases
-- ----------------------------
BEGIN;
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (35, 15, 0, '{\"nums\": [2, 7, 11, 15], \"target\": 9}', '{\"indices\": [0, 1]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (36, 15, 1, '{\"nums\": [3, 3], \"target\": 6}', '{\"indices\": [0, 1]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (37, 15, 2, '{\"nums\": [0, 4, 3, 0], \"target\": 0}', '{\"indices\": [0, 3]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (38, 16, 0, '{\"nums\": [-1, 1], \"target\": 0}', '{\"indices\": [0, 1]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (39, 16, 1, '{\"nums\": [1, 2, 5, 6], \"target\": 7}', '{\"indices\": [1, 2]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (40, 16, 2, '{\"nums\": [1, 1, 2, 3], \"target\": 4}', '{\"indices\": [1, 3]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (41, 17, 0, '{\"nums\": [1, 2, 3, 4, 2], \"target\": 4}', '{\"indices\": [0, 2]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (42, 17, 1, '{\"nums\": [2, 2, 2, 2], \"target\": 4}', '{\"indices\": [0, 1]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (43, 18, 0, '{\"nums\": [2147483640, 7, -2147483641, 1], \"target\": -1}', '{\"indices\": [2, 3]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (44, 18, 1, '{\"nums\": [0, 0, 0, 1], \"target\": 0}', '{\"indices\": [0, 1]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (45, 18, 2, '{\"nums\": [5, 5, 5], \"target\": 20}', '{\"indices\": [-1, -1]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (46, 19, 0, '{\"nums\": [7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 1, 2, 3, 4, 5, 6, 8, 9, 10, 0, 123456, -123449], \"target\": 7}', '{\"indices\": [60, 61]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (47, 19, 1, '{\"nums\": [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 6, 7, 8, 9, 123456, -123446], \"target\": 10}', '{\"indices\": [48, 49]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
INSERT INTO `testcases` (`id`, `group_id`, `order_index`, `input_json`, `output_json`, `output_type`, `score`, `created_at`, `updated_at`) VALUES (48, 20, 0, '{\"nums\": [1, 2, 5, 10], \"target\": 100}', '{\"indices\": [-1, -1]}', 'ordered_array', 10, '2025-10-13 13:14:29', '2025-10-13 13:14:29');
COMMIT;

-- ----------------------------
-- Table structure for token
-- ----------------------------
DROP TABLE IF EXISTS `token`;
CREATE TABLE `token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `token_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `expired` tinyint(1) NOT NULL,
  `revoked` tinyint(1) NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `token_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1977599465630244867 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of token
-- ----------------------------
BEGIN;
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1960945044687233026, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjM2MDcwNSwiZXhwIjoxNzU2NDQ3MTA1fQ.PjA_RD-i0sGyMlizyR1kG2WmJQuANV1tlGhhM5-F0FM', 'ACCESS', 0, 0, '2025-08-28 13:58:28', '2025-08-28 13:58:28', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1960945222437642241, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjM2MDc0OCwiZXhwIjoxNzU2NDQ3MTQ4fQ.DjHPzkRg8AWxOamlZtu3zBnwIxkqHyQBTxOlHFMr_Cs', 'ACCESS', 0, 0, '2025-08-28 13:59:10', '2025-08-28 13:59:10', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1960946983781081089, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjM2MTE2OCwiZXhwIjoxNzU2NDQ3NTY4fQ.GSn2UngHy1oPVrQZ9TF1ayIe_edY69syYNGB0KGqxoY', 'ACCESS', 0, 0, '2025-08-28 14:06:10', '2025-08-28 14:06:10', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1960947691557298178, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjM2MTMzNiwiZXhwIjoxNzU2NDQ3NzM2fQ.afb19uGw2XeiwhAC0i8zJ93dVwNYGzS0F5qlNrx1cew', 'ACCESS', 0, 0, '2025-08-28 14:08:59', '2025-08-28 14:08:59', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961311138463629313, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjQ0Nzk4OSwiZXhwIjoxNzU2NTM0Mzg5fQ.lC4NGovERJkgXDxTGjNt6fRZeco0Qd_nzvB2I5PhqUo', 'ACCESS', 0, 0, '2025-08-29 14:13:09', '2025-08-29 14:13:09', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961318837783941121, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjQ0OTgyNSwiZXhwIjoxNzU2NTM2MjI1fQ.RBQ06G46FMAyAfPEtmpzs3b5k23mRjBSci8wT3GLZCI', 'ACCESS', 0, 0, '2025-08-29 14:43:45', '2025-08-29 14:43:45', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961318860219273218, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjQ0OTgzMCwiZXhwIjoxNzU2NTM2MjMwfQ.Agfvz9N82p0IL_AKOd0RiFsMgsBagRcesEUqAW-T8Yc', 'ACCESS', 0, 0, '2025-08-29 14:43:50', '2025-08-29 14:43:50', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961361525501628417, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjQ2MDAwMiwiZXhwIjoxNzU2NTQ2NDAyfQ.nZrEkA1ps8SuB835clYv7GjthkEuKikYwCuuGMHG9g0', 'ACCESS', 0, 0, '2025-08-29 17:33:22', '2025-08-29 17:33:22', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961364154051006466, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjQ2MDYyOSwiZXhwIjoxNzU2NTQ3MDI5fQ.0Tm9UFf_xgwYwimRptCbtqIUGjB838rbICktqqTEMWc', 'ACCESS', 0, 0, '2025-08-29 17:43:49', '2025-08-29 17:43:49', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961397333516394497, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjQ2ODUzOSwiZXhwIjoxNzU2NTU0OTM5fQ.8mWypMqpKGQw0QngmulawWdsXho-nJ-U51g3Ar6y8k0', 'ACCESS', 0, 0, '2025-08-29 19:55:40', '2025-08-29 19:55:40', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961742790914531330, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjU1MDkwMywiZXhwIjoxNzU2NjM3MzAzfQ.xTQdlkG3gzYLjeUckISVJmpRlNNjtVOgwdCn2EjgmAo', 'ACCESS', 0, 0, '2025-08-30 18:48:24', '2025-08-30 18:48:24', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961742852092649473, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjU1MDkxNywiZXhwIjoxNzU2NjM3MzE3fQ.uLBrob6xZDPhLLJcfyPsT-IHucKI0TT8XQNcUfkQ10I', 'ACCESS', 0, 0, '2025-08-30 18:48:39', '2025-08-30 18:48:39', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961791511501033474, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjU2MjUxOSwiZXhwIjoxNzU2NjQ4OTE5fQ._mwU50DDGw1Ser1ScbIFYR0MXQYg86Y45kIOyUCa998', 'ACCESS', 0, 0, '2025-08-30 22:01:59', '2025-08-30 22:01:59', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961794877656449025, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjU2MzMyMSwiZXhwIjoxNzU2NjQ5NzIxfQ.1TPz8iCsSk-pksLeTaUWet9bjbfaCFmlVr2AZqJ9Smw', 'ACCESS', 0, 0, '2025-08-30 22:15:21', '2025-08-30 22:15:21', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961794926239072258, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjU2MzMzMywiZXhwIjoxNzU2NjQ5NzMzfQ.Il_M-C-Z_7HqJBrfevSIjm-3_U8op1ybpEI9toGqi8Q', 'ACCESS', 0, 0, '2025-08-30 22:15:33', '2025-08-30 22:15:33', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961794996766294017, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjU2MzM1MCwiZXhwIjoxNzU2NjQ5NzUwfQ.w4vB9FrbDX5R4OYEd9SqrTqFQGat1IEncPjk9Oe9jYQ', 'ACCESS', 0, 0, '2025-08-30 22:15:50', '2025-08-30 22:15:50', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1961796134508969985, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjU2MzYyMSwiZXhwIjoxNzU2NjUwMDIxfQ.tK3kQXrDFBzmwO3sgYDrcBzChDxGctwIwOvsuU-XQSc', 'ACCESS', 0, 0, '2025-08-30 22:20:21', '2025-08-30 22:20:21', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1962003578925408257, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjYxMzA4MCwiZXhwIjoxNzU2Njk5NDgwfQ.mckSk73SYLV-scehPuiLUsAvwDQlSfCJ6ts2tgja948', 'ACCESS', 0, 0, '2025-08-31 12:04:40', '2025-08-31 12:04:40', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1962005838032044033, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NjYxMzYxOCwiZXhwIjoxNzU2NzAwMDE4fQ.hPB6pItsUN5D-zcvrL0tu7jsFlZvHNk0WJm3-qYlKGk', 'ACCESS', 0, 0, '2025-08-31 12:13:38', '2025-08-31 12:13:38', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1976626914770862082, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDA5OTU1NSwiZXhwIjoxNzYwMTg1OTU1fQ.o21Z9aPhq6WV_lD6gEoDVOUqr45d50D-bfBTtjC-MZE', 'ACCESS', 0, 0, '2025-10-10 20:32:35', '2025-10-10 20:32:35', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1976638150791200770, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDEwMjIzMywiZXhwIjoxNzYwMTg4NjMzfQ.rKeUewRCn6whqXvCwu183KqAbfxybHqFACCatvhLoz8', 'ACCESS', 0, 0, '2025-10-10 21:17:13', '2025-10-10 21:17:13', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1976649174747455489, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDEwNDg2MiwiZXhwIjoxNzYwMTkxMjYyfQ.BMjSJy89nSmv9C4PP6-M45_GV52a13nqbETO1jN4Yq8', 'ACCESS', 0, 0, '2025-10-10 22:01:02', '2025-10-10 22:01:02', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1976666817856573442, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDEwOTA2OCwiZXhwIjoxNzYwMTk1NDY4fQ.jsW0VQJCnt7JWk13YbjWUtY4zydgWqXAWXfc4mm4b_s', 'ACCESS', 0, 0, '2025-10-10 23:11:08', '2025-10-10 23:11:08', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1976666910252896258, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDEwOTA5MCwiZXhwIjoxNzYwMTk1NDkwfQ.FI-3BUZwi0iMgGgn1jCmMyklQsuZtVF3BKd3oj1CpNY', 'ACCESS', 0, 0, '2025-10-10 23:11:30', '2025-10-10 23:11:30', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1976667004058505217, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDEwOTExMywiZXhwIjoxNzYwMTk1NTEzfQ.y_WQ2EegnwZ786RFQV0ING6e6ANxyDLs5NG5yAxqc9o', 'ACCESS', 0, 0, '2025-10-10 23:11:53', '2025-10-10 23:11:53', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1976676623086637057, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDExMTQwNiwiZXhwIjoxNzYwMTk3ODA2fQ.5CytupomNAsqtX4nlhoazorBcDItfYuH4KA0Ok5L4W0', 'ACCESS', 0, 0, '2025-10-10 23:50:06', '2025-10-10 23:50:06', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1977194525275492353, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDIzNDg4MywiZXhwIjoxNzYwMzIxMjgzfQ.2NrivqTw5KIXx_KltqXr0fJ-x4D1rs8bCI8OM3lwb-0', 'ACCESS', 0, 0, '2025-10-12 10:08:03', '2025-10-12 10:08:03', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1977404136637157377, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDI4NDg1OSwiZXhwIjoxNzYwMzcxMjU5fQ.9-4BVTh5kPiun_zQEXvtAl_bqydGfFgFvracjIsFKys', 'ACCESS', 0, 0, '2025-10-13 00:00:59', '2025-10-13 00:00:59', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1977588819022880769, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDMyODg5MCwiZXhwIjoxNzYwNDE1MjkwfQ.sFsHDx4A_ZaiT3l5d3sLQ_JFhj2WcYM9hBT93DkaTUs', 'ACCESS', 0, 0, '2025-10-13 12:14:50', '2025-10-13 12:14:50', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1977599465630244866, 3, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJseXNmMTU1MjAxMTI5NzNAMTYzLmNvbSIsImlhdCI6MTc2MDMzMTQyOSwiZXhwIjoxNzYwNDE3ODI5fQ.j4fjjsKlssZV0ugUvRGLneeUWg0E7utjEfxj5KtTzqM', 'ACCESS', 0, 0, '2025-10-13 12:57:09', '2025-10-13 12:57:09', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `introduction` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '用户未填写',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '用户未填写',
  `status` int DEFAULT '1',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `last_login` datetime DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (3, 'David', 'lysf15520112973@163.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '中国', '中国', 1, NULL, NULL, '2025-08-05 17:27:53', '2025-10-11 18:47:31', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (6, 'user1', 'user1@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (7, 'user2', 'user2@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (8, 'user3', 'user3@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (9, 'user4', 'user4@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (10, 'user5', 'user5@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (11, 'user6', 'user6@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (12, 'user7', 'user7@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (13, 'user8', 'user8@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (14, 'user9', 'user9@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (15, 'user10', 'user10@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (16, 'user11', 'user11@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (17, 'user12', 'user12@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (18, 'user13', 'user13@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (19, 'user14', 'user14@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (20, 'user15', 'user15@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (21, 'user16', 'user16@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (22, 'user17', 'user17@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (23, 'user18', 'user18@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (24, 'user19', 'user19@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (25, 'user20', 'user20@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (26, 'user21', 'user21@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (27, 'user22', 'user22@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (28, 'user23', 'user23@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (29, 'user24', 'user24@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (30, 'user25', 'user25@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (31, 'user26', 'user26@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (32, 'user27', 'user27@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (33, 'user28', 'user28@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (34, 'user29', 'user29@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (35, 'user30', 'user30@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (36, 'user31', 'user31@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (37, 'user32', 'user32@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (38, 'user33', 'user33@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (39, 'user34', 'user34@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (40, 'user35', 'user35@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (41, 'user36', 'user36@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (42, 'user37', 'user37@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (43, 'user38', 'user38@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (44, 'user39', 'user39@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (45, 'user40', 'user40@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (46, 'user41', 'user41@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (47, 'user42', 'user42@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (48, 'user43', 'user43@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (49, 'user44', 'user44@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (50, 'user45', 'user45@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (51, 'user46', 'user46@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (52, 'user47', 'user47@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (53, 'user48', 'user48@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (54, 'user49', 'user49@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 18:58:10', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (55, 'user50', 'user50@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (56, 'user51', 'user51@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (57, 'user52', 'user52@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (58, 'user53', 'user53@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (59, 'user54', 'user54@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (60, 'user55', 'user55@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (61, 'user56', 'user56@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (62, 'user57', 'user57@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (63, 'user58', 'user58@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (64, 'user59', 'user59@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (65, 'user60', 'user60@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (66, 'user61', 'user61@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (67, 'user62', 'user62@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (68, 'user63', 'user63@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (69, 'user64', 'user64@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (70, 'user65', 'user65@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (71, 'user66', 'user66@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (72, 'user67', 'user67@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (73, 'user68', 'user68@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (74, 'user69', 'user69@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (75, 'user70', 'user70@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (76, 'user71', 'user71@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (77, 'user72', 'user72@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (78, 'user73', 'user73@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (79, 'user74', 'user74@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (80, 'user75', 'user75@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (81, 'user76', 'user76@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (82, 'user77', 'user77@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (83, 'user78', 'user78@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (84, 'user79', 'user79@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (85, 'user80', 'user80@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (86, 'user81', 'user81@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (87, 'user82', 'user82@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (88, 'user83', 'user83@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (89, 'user84', 'user84@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (90, 'user85', 'user85@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (91, 'user86', 'user86@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (92, 'user87', 'user87@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (93, 'user88', 'user88@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (94, 'user89', 'user89@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (95, 'user90', 'user90@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (96, 'user91', 'user91@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (97, 'user92', 'user92@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (98, 'user93', 'user93@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (99, 'user94', 'user94@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (100, 'user95', 'user95@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (101, 'user96', 'user96@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (102, 'user97', 'user97@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (103, 'user98', 'user98@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (104, 'user99', 'user99@example.com', '$2a$10$cE9svxT91fhbP0wWuYuhaO.kmHwi.YQxajjG7Z1rKT47fMKunL6w6', NULL, '用户未填写', '用户未填写', 1, NULL, '2025-10-11 17:30:43', '2025-10-11 17:30:43', '2025-10-11 17:30:43', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of user_role
-- ----------------------------
BEGIN;
INSERT INTO `user_role` (`user_id`, `role_id`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (3, 8, '2025-10-11 19:17:05', '2025-10-11 19:17:05', NULL, NULL);
INSERT INTO `user_role` (`user_id`, `role_id`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (3, 9, '2025-10-11 19:17:05', '2025-10-11 19:17:05', NULL, NULL);
INSERT INTO `user_role` (`user_id`, `role_id`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (3, 10, '2025-10-11 19:17:05', '2025-10-11 19:17:05', NULL, NULL);
INSERT INTO `user_role` (`user_id`, `role_id`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (54, 9, '2025-10-11 18:58:18', '2025-10-11 18:58:18', NULL, NULL);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
