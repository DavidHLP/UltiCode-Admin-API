/*
 Navicat Premium Dump SQL

 Source Server         : WSL2-Mysql
 Source Server Type    : MySQL
 Source Server Version : 80406 (8.4.6)
 Source Host           : localhost:3306
 Source Schema         : spring_oj

 Target Server Type    : MySQL
 Target Server Version : 80406 (8.4.6)
 File Encoding         : 65001

 Date: 17/10/2025 18:18:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for auth_tokens
-- ----------------------------
DROP TABLE IF EXISTS `auth_tokens`;
CREATE TABLE `auth_tokens`  (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL,
                                `token` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                `kind` enum('access','refresh','api') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                `revoked` tinyint(1) NOT NULL DEFAULT 0,
                                `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `expires_at` timestamp NULL DEFAULT NULL,
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_auth_tokens_token`(`token` ASC) USING BTREE,
                                INDEX `idx_auth_tokens_user`(`user_id` ASC, `kind` ASC, `revoked` ASC) USING BTREE,
                                CONSTRAINT `fk_auth_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of auth_tokens
-- ----------------------------
INSERT INTO `auth_tokens` VALUES (71, 1, '29efc2bd1c0a44f1965cc4950af33d85d7283d0f4d22de652df317137a4d4fb8', 'access', 0, '2025-10-20 10:00:00', '2025-10-27 10:00:00');
INSERT INTO `auth_tokens` VALUES (72, 1, '7ab1a6d4c1d78e5ff62ab3b7c4c685d7b19d9506b477a1f9c3b0c261d5bc4a21', 'refresh', 0, '2025-10-20 10:00:00', '2025-11-19 10:00:00');

-- ----------------------------
-- Table structure for bookmarks
-- ----------------------------
DROP TABLE IF EXISTS `bookmarks`;
CREATE TABLE `bookmarks`  (
                              `user_id` bigint NOT NULL,
                              `entity_type` enum('problem','contest') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              `entity_id` bigint NOT NULL,
                              `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`user_id`, `entity_type`, `entity_id`) USING BTREE,
                              INDEX `idx_bookmarks_entity`(`entity_type` ASC, `entity_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bookmarks
-- ----------------------------
INSERT INTO `bookmarks` VALUES (1, 'problem', 1, '2025-10-20 09:12:00');
INSERT INTO `bookmarks` VALUES (1, 'contest', 1, '2025-10-20 09:12:30');

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
                               `id` smallint NOT NULL AUTO_INCREMENT,
                               `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                               `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `uk_categories_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, 'algorithms', 'Algorithms');
INSERT INTO `categories` VALUES (2, 'data-structures', 'Data Structures');

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `entity_type` enum('problem','contest','submission') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                             `entity_id` bigint NOT NULL,
                             `user_id` bigint NOT NULL,
                             `content_md` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                             `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_comments_entity`(`entity_type` ASC, `entity_id` ASC, `created_at` ASC) USING BTREE,
                             INDEX `idx_comments_user`(`user_id` ASC, `created_at` ASC) USING BTREE,
                             CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comments
-- ----------------------------
INSERT INTO `comments` VALUES (1, 'problem', 1, 1, '这是一道很经典的题目，谢谢分享！', '2025-10-20 09:15:00', '2025-10-20 09:15:00');

-- ----------------------------
-- Table structure for contest_participants
-- ----------------------------
DROP TABLE IF EXISTS `contest_participants`;
CREATE TABLE `contest_participants`  (
                                         `contest_id` bigint NOT NULL,
                                         `user_id` bigint NOT NULL,
                                         `registered_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`contest_id`, `user_id`) USING BTREE,
                                         INDEX `idx_cpart_user`(`user_id` ASC, `contest_id` ASC) USING BTREE,
                                         CONSTRAINT `fk_cpart_contest` FOREIGN KEY (`contest_id`) REFERENCES `contests` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                         CONSTRAINT `fk_cpart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of contest_participants
-- ----------------------------
INSERT INTO `contest_participants` VALUES (1, 1, '2025-10-20 09:05:00');

-- ----------------------------
-- Table structure for contest_problems
-- ----------------------------
DROP TABLE IF EXISTS `contest_problems`;
CREATE TABLE `contest_problems`  (
                                     `contest_id` bigint NOT NULL,
                                     `problem_id` bigint NOT NULL,
                                     `alias` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                     `points` int NULL DEFAULT NULL,
                                     `order_no` int NULL DEFAULT 0,
                                     PRIMARY KEY (`contest_id`, `problem_id`) USING BTREE,
                                     INDEX `idx_cp_order`(`contest_id` ASC, `order_no` ASC) USING BTREE,
                                     INDEX `fk_cp_problem`(`problem_id` ASC) USING BTREE,
                                     CONSTRAINT `fk_cp_contest` FOREIGN KEY (`contest_id`) REFERENCES `contests` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                     CONSTRAINT `fk_cp_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of contest_problems
-- ----------------------------
INSERT INTO `contest_problems` VALUES (1, 1, 'A', 100, 1);
INSERT INTO `contest_problems` VALUES (1, 2, 'B', 100, 2);

-- ----------------------------
-- Table structure for contests
-- ----------------------------
DROP TABLE IF EXISTS `contests`;
CREATE TABLE `contests`  (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                             `description_md` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                             `kind` enum('icpc','oi','ioi','cf','acm','custom') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'icpc',
                             `start_time` datetime NOT NULL,
                             `end_time` datetime NOT NULL,
                             `is_visible` tinyint(1) NOT NULL DEFAULT 1,
                             `created_by` bigint NULL DEFAULT NULL,
                             `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_contest_time`(`start_time` ASC, `end_time` ASC) USING BTREE,
                             INDEX `fk_contest_creator`(`created_by` ASC) USING BTREE,
                             CONSTRAINT `fk_contest_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of contests
-- ----------------------------
INSERT INTO `contests` VALUES (1, '秋季热身赛', '一次针对初学者的热身赛，包含两道基础题。', 'icpc', '2025-11-01 09:00:00', '2025-11-01 12:00:00', 1, 1, '2025-10-20 09:00:00', '2025-10-20 09:00:00');

-- ----------------------------
-- Table structure for datasets
-- ----------------------------
DROP TABLE IF EXISTS `datasets`;
CREATE TABLE `datasets`  (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `problem_id` bigint NOT NULL,
                             `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                             `is_active` tinyint(1) NOT NULL DEFAULT 0,
                             `checker_type` enum('text','float','custom') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'text',
                             `checker_file_id` bigint NULL DEFAULT NULL,
                             `float_abs_tol` double NULL DEFAULT NULL,
                             `float_rel_tol` double NULL DEFAULT NULL,
                             `created_by` bigint NULL DEFAULT NULL,
                             `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_ds_problem`(`problem_id` ASC, `is_active` ASC) USING BTREE,
                             INDEX `fk_ds_checker_file`(`checker_file_id` ASC) USING BTREE,
                             INDEX `fk_ds_creator`(`created_by` ASC) USING BTREE,
                             CONSTRAINT `fk_ds_checker_file` FOREIGN KEY (`checker_file_id`) REFERENCES `files` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                             CONSTRAINT `fk_ds_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                             CONSTRAINT `fk_ds_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of datasets
-- ----------------------------
INSERT INTO `datasets` VALUES (1, 1, 'v1', 1, 'text', NULL, NULL, NULL, 1, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `datasets` VALUES (2, 2, 'v1', 1, 'text', NULL, NULL, NULL, 1, '2025-10-17 16:53:25', '2025-10-17 16:53:25');

-- ----------------------------
-- Table structure for difficulties
-- ----------------------------
DROP TABLE IF EXISTS `difficulties`;
CREATE TABLE `difficulties`  (
                                 `id` tinyint NOT NULL,
                                 `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                 `sort_key` tinyint NOT NULL,
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `uk_difficulties_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of difficulties
-- ----------------------------
INSERT INTO `difficulties` VALUES (1, 'easy', 1);
INSERT INTO `difficulties` VALUES (2, 'medium', 2);
INSERT INTO `difficulties` VALUES (3, 'hard', 3);

-- ----------------------------
-- Table structure for files
-- ----------------------------
DROP TABLE IF EXISTS `files`;
CREATE TABLE `files`  (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `storage_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `sha256` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `mime_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                          `size_bytes` bigint NULL DEFAULT NULL,
                          `created_by` bigint NULL DEFAULT NULL,
                          `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `uk_files_sha`(`sha256` ASC) USING BTREE,
                          INDEX `idx_files_creator`(`created_by` ASC) USING BTREE,
                          CONSTRAINT `fk_files_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of files
-- ----------------------------
INSERT INTO `files` VALUES (1, 'submissions/2025/10/17/1.cpp', '0f4b6cba72f787b8581fbc5eac109d3328d01fefbe17f4ad6d4b5d3c7f27e9a1', 'text/x-c++src', 428, 1, '2025-10-17 17:44:00');
INSERT INTO `files` VALUES (2, 'submissions/2025/10/17/1.log', '31ed948a9920ba63dd1d8c2f954ed7be16ceb2068aec1a51ad8f7c837f0e9b77', 'text/plain', 182, 1, '2025-10-17 17:45:05');

-- ----------------------------
-- Table structure for judge_jobs
-- ----------------------------
DROP TABLE IF EXISTS `judge_jobs`;
CREATE TABLE `judge_jobs`  (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `submission_id` bigint NOT NULL,
                               `node_id` bigint NULL DEFAULT NULL,
                               `status` enum('queued','running','finished','failed','canceled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'queued',
                               `priority` tinyint NOT NULL DEFAULT 0,
                               `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               `started_at` timestamp NULL DEFAULT NULL,
                               `finished_at` timestamp NULL DEFAULT NULL,
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `uk_job_submission`(`submission_id` ASC) USING BTREE,
                               INDEX `idx_jobs_status`(`status` ASC, `priority` ASC, `created_at` ASC) USING BTREE,
                               INDEX `fk_jobs_node`(`node_id` ASC) USING BTREE,
                               CONSTRAINT `fk_jobs_node` FOREIGN KEY (`node_id`) REFERENCES `judge_nodes` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                               CONSTRAINT `fk_jobs_submission` FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of judge_jobs
-- ----------------------------
INSERT INTO `judge_jobs` VALUES (1, 1, 1, 'finished', 0, '2025-10-17 17:46:00', '2025-10-17 17:46:05', '2025-10-17 17:46:07');

-- ----------------------------
-- Table structure for judge_nodes
-- ----------------------------
DROP TABLE IF EXISTS `judge_nodes`;
CREATE TABLE `judge_nodes`  (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                `status` enum('online','offline','busy','draining') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'online',
                                `runtime_info` json NULL,
                                `last_heartbeat` timestamp NULL DEFAULT NULL,
                                `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_jnode_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of judge_nodes
-- ----------------------------
INSERT INTO `judge_nodes` VALUES (1, 'node-1', 'online', '{"cpu":"16c","mem_gb":32,"os":"Ubuntu 24.04"}', '2025-10-17 17:50:00', '2025-10-15 08:00:00');

-- ----------------------------
-- Table structure for languages
-- ----------------------------
DROP TABLE IF EXISTS `languages`;
CREATE TABLE `languages`  (
                              `id` smallint NOT NULL AUTO_INCREMENT,
                              `code` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              `display_name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              `runtime_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                              `is_active` tinyint(1) NOT NULL DEFAULT 1,
                              PRIMARY KEY (`id`) USING BTREE,
                              UNIQUE INDEX `uk_languages_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of languages
-- ----------------------------
INSERT INTO `languages` VALUES (1, 'cpp17', 'C++17', NULL, 1);
INSERT INTO `languages` VALUES (2, 'python3.11', 'Python 3.11', NULL, 1);
INSERT INTO `languages` VALUES (3, 'java17', 'Java 17', NULL, 1);

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uk_permissions_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permissions
-- ----------------------------
INSERT INTO `permissions` VALUES (1, 'problem.view', '查看题目', '2025-10-14 08:00:00');
INSERT INTO `permissions` VALUES (2, 'problem.manage', '管理题目', '2025-10-14 08:00:00');
INSERT INTO `permissions` VALUES (3, 'contest.view', '查看比赛', '2025-10-14 08:00:00');
INSERT INTO `permissions` VALUES (4, 'contest.manage', '管理比赛', '2025-10-14 08:00:00');
INSERT INTO `permissions` VALUES (5, 'user.manage', '管理用户', '2025-10-14 08:00:00');

-- ----------------------------
-- Table structure for problem_language_configs
-- ----------------------------
DROP TABLE IF EXISTS `problem_language_configs`;
CREATE TABLE `problem_language_configs`  (
                                             `id` bigint NOT NULL AUTO_INCREMENT,
                                             `problem_id` bigint NOT NULL,
                                             `language_id` smallint NOT NULL,
                                             `function_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                             `starter_code` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                                             PRIMARY KEY (`id`) USING BTREE,
                                             UNIQUE INDEX `uk_problem_language`(`problem_id` ASC, `language_id` ASC) USING BTREE,
                                             INDEX `idx_plc_problem`(`problem_id` ASC) USING BTREE,
                                             INDEX `fk_plc_language`(`language_id` ASC) USING BTREE,
                                             CONSTRAINT `fk_plc_language` FOREIGN KEY (`language_id`) REFERENCES `languages` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                             CONSTRAINT `fk_plc_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2113 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of problem_language_configs
-- ----------------------------
INSERT INTO `problem_language_configs` VALUES (2101, 2, 1, 'reverseString', '// LeetCode-style C++17 (method only, returns string)\r\n     // For in-place version: void reverseString(vector<char>& s)\r\n     #include <bits/stdc++.h>\r\n     using namespace std;\r\n     class Solution {\r\n     public:\r\n         string reverseString(string s) {\r\n             reverse(s.begin(), s.end());\r\n             return s;\r\n         }\r\n     };');
INSERT INTO `problem_language_configs` VALUES (2102, 2, 2, 'reverseString', '# LeetCode-style Python 3.11 (method only, returns string)\r\n     class Solution:\r\n         def reverseString(self, s: str) -> str:\r\n             return s[::-1]\r\n\r\n     # In-place version:\r\n     # from typing import List\r\n     # class Solution:\r\n     #     def reverseString(self, s: List[str]) -> None:\r\n     #         s.reverse()\r\n     ');
INSERT INTO `problem_language_configs` VALUES (2103, 2, 3, 'reverseString', '// LeetCode-style Java 17 (method only, returns string)\r\n     // In-place version: void reverseString(char[] s)\r\n     class Solution {\r\n         public String reverseString(String s) {\r\n             return new StringBuilder(s).reverse().toString();\r\n         }\r\n     }');
INSERT INTO `problem_language_configs` VALUES (2110, 1, 1, 'twoSum', '// LeetCode-style C++17 (method only)\r\n     #include <bits/stdc++.h>\r\n     using namespace std;\r\n     class Solution {\r\n     public:\r\n         vector<int> twoSum(vector<int>& nums, int target) {\r\n             unordered_map<int,int> mp;\r\n             for (int i = 0; i < (int)nums.size(); ++i) {\r\n                 int need = target - nums[i];\r\n                 if (mp.count(need)) return {mp[need], i};\r\n                 mp[nums[i]] = i;\r\n             }\r\n             return {};\r\n         }\r\n     };');
INSERT INTO `problem_language_configs` VALUES (2111, 1, 2, 'twoSum', '# LeetCode-style Python 3.11 (method only)\r\n     from typing import List\r\n\r\n     class Solution:\r\n         def twoSum(self, nums: List[int], target: int) -> List[int]:\r\n             mp = {}\r\n             for i, x in enumerate(nums):\r\n                 if target - x in mp:\r\n                     return [mp[target - x], i]\r\n                 mp[x] = i\r\n             return []\r\n     ');
INSERT INTO `problem_language_configs` VALUES (2112, 1, 3, 'twoSum', '// LeetCode-style Java 17 (method only)\r\n     import java.util.*;\r\n     class Solution {\r\n         public int[] twoSum(int[] nums, int target) {\r\n             Map<Integer, Integer> mp = new HashMap<>();\r\n             for (int i = 0; i < nums.length; i++) {\r\n                 int need = target - nums[i];\r\n                 if (mp.containsKey(need)) return new int[]{mp.get(need), i};\r\n                 mp.put(nums[i], i);\r\n             }\r\n             return new int[0];\r\n         }\r\n     }');

-- ----------------------------
-- Table structure for problem_statements
-- ----------------------------
DROP TABLE IF EXISTS `problem_statements`;
CREATE TABLE `problem_statements`  (
                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                       `problem_id` bigint NOT NULL,
                                       `lang_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                       `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                       `description_md` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                       `constraints_md` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                                       `examples_md` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                                       `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       PRIMARY KEY (`id`) USING BTREE,
                                       UNIQUE INDEX `uk_problem_lang`(`problem_id` ASC, `lang_code` ASC) USING BTREE,
                                       CONSTRAINT `fk_ps_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 209 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of problem_statements
-- ----------------------------
INSERT INTO `problem_statements` VALUES (201, 2, 'en', '344. Reverse String', 'Write a function that returns the reversed string of **s**.\r\n\r\n  > Note: The original LeetCode version modifies `char[]/List[str]` in-place. Here we use a return-value variant for judging.', '- 1 ≤ |s| ≤ 10^5', '**Example**\nInput: s = \"hello\"\nOutput: \"olleh\"', '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `problem_statements` VALUES (202, 2, 'zh-CN', '344. 反转字符串', '编写函数返回字符串 **s** 的反转结果。\r\n  > 注：LeetCode 原题为原地修改字符数组，这里改为返回值版本以便判题。', '- 1 ≤ |s| ≤ 10^5', '**示例**\n输入：s = \"hello\"\n输出：\"olleh\"', '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `problem_statements` VALUES (207, 1, 'en', '1. Two Sum', 'Given an integer array **nums** and an integer **target**, return the indices of the two numbers such that they add up to **target**.', '- 2 ≤ |nums| ≤ 10^5\n- -10^9 ≤ nums[i], target ≤ 10^9\n- Exactly one solution exists.', '**Example**\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]', '2025-10-17 18:06:54', '2025-10-17 18:06:54');
INSERT INTO `problem_statements` VALUES (208, 1, 'zh-CN', '1. 两数之和', '给定整数数组 **nums** 与整数 **target**，请返回使得 `nums[i] + nums[j] = target` 的两个下标（i ≠ j）。', '- 2 ≤ |nums| ≤ 10^5\n- -10^9 ≤ nums[i], target ≤ 10^9\n- 题目保证恰好存在一个解。', '**示例**\n输入：nums = [2,7,11,15], target = 9\n输出：[0,1]', '2025-10-17 18:06:54', '2025-10-17 18:06:54');

-- ----------------------------
-- Table structure for problem_tags
-- ----------------------------
DROP TABLE IF EXISTS `problem_tags`;
CREATE TABLE `problem_tags`  (
                                 `problem_id` bigint NOT NULL,
                                 `tag_id` bigint NOT NULL,
                                 PRIMARY KEY (`problem_id`, `tag_id`) USING BTREE,
                                 INDEX `idx_pt_tag`(`tag_id` ASC) USING BTREE,
                                 CONSTRAINT `fk_pt_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                 CONSTRAINT `fk_pt_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of problem_tags
-- ----------------------------
INSERT INTO `problem_tags` VALUES (2, 3);
INSERT INTO `problem_tags` VALUES (1, 10);
INSERT INTO `problem_tags` VALUES (1, 11);

-- ----------------------------
-- Table structure for problems
-- ----------------------------
DROP TABLE IF EXISTS `problems`;
CREATE TABLE `problems`  (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `slug` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                             `problem_type` enum('coding','sql','shell','concurrency','interactive','output-only') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'coding',
                             `difficulty_id` tinyint NOT NULL,
                             `category_id` smallint NULL DEFAULT NULL,
                             `creator_id` bigint NULL DEFAULT NULL,
                             `solution_entry` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                             `time_limit_ms` int NULL DEFAULT NULL,
                             `memory_limit_kb` int NULL DEFAULT NULL,
                             `is_public` tinyint(1) NOT NULL DEFAULT 1,
                             `meta_json` json NULL,
                             `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             `active_dataset_id` bigint NULL DEFAULT NULL,
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `uk_problems_slug`(`slug` ASC) USING BTREE,
                             INDEX `idx_problems_visibility`(`is_public` ASC, `id` ASC) USING BTREE,
                             INDEX `idx_problems_diff`(`difficulty_id` ASC, `id` ASC) USING BTREE,
                             INDEX `idx_problems_cat`(`category_id` ASC, `id` ASC) USING BTREE,
                             INDEX `fk_problem_creator`(`creator_id` ASC) USING BTREE,
                             INDEX `fk_problem_active_dataset`(`active_dataset_id` ASC) USING BTREE,
                             CONSTRAINT `fk_problem_active_dataset` FOREIGN KEY (`active_dataset_id`) REFERENCES `datasets` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                             CONSTRAINT `fk_problem_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                             CONSTRAINT `fk_problem_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                             CONSTRAINT `fk_problem_difficulty` FOREIGN KEY (`difficulty_id`) REFERENCES `difficulties` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of problems
-- ----------------------------
INSERT INTO `problems` VALUES (1, '1-two-sum', 'coding', 1, 1, 1, 'twoSum', 1000, 262144, 1, '{\"companies\": [\"Google\", \"Amazon\", \"Facebook\"], \"frequency\": 0.53, \"paid_only\": false, \"frontend_id\": 1, \"leetcode_style\": true}', '2025-10-17 16:53:25', '2025-10-17 18:06:54', 1);
INSERT INTO `problems` VALUES (2, '344-reverse-string', 'coding', 1, 1, 1, 'reverseString', 1000, 262144, 1, '{\"companies\": [\"Microsoft\", \"Amazon\"], \"frequency\": 0.27, \"paid_only\": false, \"frontend_id\": 344, \"leetcode_style\": true}', '2025-10-17 16:53:25', '2025-10-17 16:53:25', 2);

-- ----------------------------
-- Table structure for reactions
-- ----------------------------
DROP TABLE IF EXISTS `reactions`;
CREATE TABLE `reactions`  (
                              `user_id` bigint NOT NULL,
                              `entity_type` enum('problem','comment') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              `entity_id` bigint NOT NULL,
                              `kind` enum('like','dislike') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`user_id`, `entity_type`, `entity_id`, `kind`) USING BTREE,
                              INDEX `idx_reactions_entity`(`entity_type` ASC, `entity_id` ASC, `kind` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reactions
-- ----------------------------
INSERT INTO `reactions` VALUES (1, 'problem', 1, 'like', '2025-10-20 09:16:00');

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
                                     `role_id` bigint NOT NULL,
                                     `perm_id` bigint NOT NULL,
                                     PRIMARY KEY (`role_id`, `perm_id`) USING BTREE,
                                     INDEX `idx_role_permissions_perm`(`perm_id` ASC) USING BTREE,
                                     CONSTRAINT `fk_role_permissions_perm` FOREIGN KEY (`perm_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                     CONSTRAINT `fk_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permissions
-- ----------------------------
INSERT INTO `role_permissions` VALUES (2, 1);
INSERT INTO `role_permissions` VALUES (2, 3);
INSERT INTO `role_permissions` VALUES (3, 1);
INSERT INTO `role_permissions` VALUES (3, 2);
INSERT INTO `role_permissions` VALUES (3, 3);
INSERT INTO `role_permissions` VALUES (3, 4);
INSERT INTO `role_permissions` VALUES (3, 5);

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                          `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `uk_roles_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (2, 'user', '普通用户', '默认权限', '2025-10-15 08:47:45', '2025-10-17 14:15:54');
INSERT INTO `roles` VALUES (3, 'admin', '管理员', '最高管理权限', '2025-10-17 12:28:09', '2025-10-17 14:15:41');

-- ----------------------------
-- Table structure for submission_artifacts
-- ----------------------------
DROP TABLE IF EXISTS `submission_artifacts`;
CREATE TABLE `submission_artifacts`  (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `submission_id` bigint NOT NULL,
                                         `kind` enum('compile_log','run_log','stderr','stdout','diff','system') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                         `file_id` bigint NOT NULL,
                                         `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`id`) USING BTREE,
                                         INDEX `idx_sa_submission`(`submission_id` ASC, `kind` ASC) USING BTREE,
                                         INDEX `fk_sa_file`(`file_id` ASC) USING BTREE,
                                         CONSTRAINT `fk_sa_file` FOREIGN KEY (`file_id`) REFERENCES `files` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                         CONSTRAINT `fk_sa_submission` FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of submission_artifacts
-- ----------------------------
INSERT INTO `submission_artifacts` VALUES (1, 1, 'compile_log', 2, '2025-10-17 17:45:05');

-- ----------------------------
-- Table structure for submission_tests
-- ----------------------------
DROP TABLE IF EXISTS `submission_tests`;
CREATE TABLE `submission_tests`  (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `submission_id` bigint NOT NULL,
                                     `testcase_id` bigint NOT NULL,
                                     `group_id` bigint NOT NULL,
                                     `verdict` enum('AC','WA','TLE','MLE','RE','OLE','PE','SKIP','IE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                     `time_ms` int NULL DEFAULT NULL,
                                     `memory_kb` int NULL DEFAULT NULL,
                                     `score` int NULL DEFAULT NULL,
                                     `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `uk_submission_test`(`submission_id` ASC, `testcase_id` ASC) USING BTREE,
                                     INDEX `idx_st_group`(`group_id` ASC) USING BTREE,
                                     INDEX `fk_st_testcase`(`testcase_id` ASC) USING BTREE,
                                     CONSTRAINT `fk_st_group` FOREIGN KEY (`group_id`) REFERENCES `testcase_groups` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                     CONSTRAINT `fk_st_submission` FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                     CONSTRAINT `fk_st_testcase` FOREIGN KEY (`testcase_id`) REFERENCES `testcases` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of submission_tests
-- ----------------------------
INSERT INTO `submission_tests` VALUES (1, 1, 1, 1, 'AC', 5, 1024, 10, NULL);
INSERT INTO `submission_tests` VALUES (2, 1, 2, 1, 'AC', 6, 1100, 10, NULL);
INSERT INTO `submission_tests` VALUES (3, 1, 3, 2, 'AC', 7, 1152, 10, NULL);
INSERT INTO `submission_tests` VALUES (4, 1, 4, 2, 'AC', 8, 1200, 10, NULL);

-- ----------------------------
-- Table structure for submissions
-- ----------------------------
DROP TABLE IF EXISTS `submissions`;
CREATE TABLE `submissions`  (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL,
                                `problem_id` bigint NOT NULL,
                                `dataset_id` bigint NOT NULL,
                                `language_id` smallint NOT NULL,
                                `source_file_id` bigint NOT NULL,
                                `code_bytes` int NULL DEFAULT NULL,
                                `verdict` enum('PD','AC','WA','TLE','MLE','RE','CE','OLE','PE','IE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PD',
                                `score` int NULL DEFAULT NULL,
                                `time_ms` int NULL DEFAULT NULL,
                                `memory_kb` int NULL DEFAULT NULL,
                                `judge_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                                `ip_addr` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                `contest_id` bigint NULL DEFAULT NULL,
                                `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `idx_submissions_user`(`user_id` ASC, `created_at` ASC) USING BTREE,
                                INDEX `idx_submissions_problem`(`problem_id` ASC, `created_at` ASC) USING BTREE,
                                INDEX `idx_submissions_contest`(`contest_id` ASC, `created_at` ASC) USING BTREE,
                                INDEX `idx_submissions_verdict`(`verdict` ASC, `created_at` ASC) USING BTREE,
                                INDEX `fk_sub_dataset`(`dataset_id` ASC) USING BTREE,
                                INDEX `fk_sub_language`(`language_id` ASC) USING BTREE,
                                INDEX `fk_sub_source`(`source_file_id` ASC) USING BTREE,
                                CONSTRAINT `fk_sub_dataset` FOREIGN KEY (`dataset_id`) REFERENCES `datasets` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                CONSTRAINT `fk_sub_language` FOREIGN KEY (`language_id`) REFERENCES `languages` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                CONSTRAINT `fk_sub_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                CONSTRAINT `fk_sub_source` FOREIGN KEY (`source_file_id`) REFERENCES `files` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                CONSTRAINT `fk_sub_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of submissions
-- ----------------------------
INSERT INTO `submissions` VALUES (1, 1, 1, 1, 1, 1, 428, 'AC', 100, 12, 4096, '所有测试用例通过', '127.0.0.1', 1, '2025-10-17 17:45:00');

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags`  (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `slug` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                         `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                         `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `uk_tags_slug`(`slug` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tags
-- ----------------------------
INSERT INTO `tags` VALUES (3, 'string', 'String', '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `tags` VALUES (10, 'array', 'Array', '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `tags` VALUES (11, 'hash-table', 'Hash Table', '2025-10-17 16:53:25', '2025-10-17 16:53:25');

-- ----------------------------
-- Table structure for testcase_groups
-- ----------------------------
DROP TABLE IF EXISTS `testcase_groups`;
CREATE TABLE `testcase_groups`  (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `dataset_id` bigint NOT NULL,
                                    `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                    `is_sample` tinyint(1) NOT NULL DEFAULT 0,
                                    `weight` int NOT NULL DEFAULT 1,
                                    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`) USING BTREE,
                                    INDEX `idx_tcg_dataset`(`dataset_id` ASC, `is_sample` ASC) USING BTREE,
                                    CONSTRAINT `fk_tcg_dataset` FOREIGN KEY (`dataset_id`) REFERENCES `datasets` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of testcase_groups
-- ----------------------------
INSERT INTO `testcase_groups` VALUES (1, 1, 'samples', 1, 1, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `testcase_groups` VALUES (2, 1, 'hidden', 0, 1, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `testcase_groups` VALUES (3, 2, 'samples', 1, 1, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `testcase_groups` VALUES (4, 2, 'hidden', 0, 1, '2025-10-17 16:53:25', '2025-10-17 16:53:25');

-- ----------------------------
-- Table structure for testcases
-- ----------------------------
DROP TABLE IF EXISTS `testcases`;
CREATE TABLE `testcases`  (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `group_id` bigint NOT NULL,
                              `order_index` int NOT NULL DEFAULT 0,
                              `input_file_id` bigint NULL DEFAULT NULL,
                              `output_file_id` bigint NULL DEFAULT NULL,
                              `input_json` json NULL,
                              `output_json` json NULL,
                              `output_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                              `score` int NOT NULL DEFAULT 10,
                              `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`) USING BTREE,
                              INDEX `idx_tc_group_order`(`group_id` ASC, `order_index` ASC) USING BTREE,
                              INDEX `fk_tc_input_file`(`input_file_id` ASC) USING BTREE,
                              INDEX `fk_tc_output_file`(`output_file_id` ASC) USING BTREE,
                              CONSTRAINT `fk_tc_group` FOREIGN KEY (`group_id`) REFERENCES `testcase_groups` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                              CONSTRAINT `fk_tc_input_file` FOREIGN KEY (`input_file_id`) REFERENCES `files` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                              CONSTRAINT `fk_tc_output_file` FOREIGN KEY (`output_file_id`) REFERENCES `files` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of testcases
-- ----------------------------
INSERT INTO `testcases` VALUES (1, 1, 1, NULL, NULL, '{\"nums\": [2, 7, 11, 15], \"target\": 9}', '[0, 1]', 'json', 10, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `testcases` VALUES (2, 1, 2, NULL, NULL, '{\"nums\": [3, 2, 4], \"target\": 6}', '[1, 2]', 'json', 10, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `testcases` VALUES (3, 2, 1, NULL, NULL, '{\"nums\": [-1, -2, -3, -4, -5], \"target\": -8}', '[2, 4]', 'json', 10, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `testcases` VALUES (4, 2, 2, NULL, NULL, '{\"nums\": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], \"target\": 19}', '[8, 9]', 'json', 10, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `testcases` VALUES (5, 3, 1, NULL, NULL, '{\"s\": \"hello\"}', '\"olleh\"', 'json', 10, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `testcases` VALUES (6, 3, 2, NULL, NULL, '{\"s\": \"ab\"}', '\"ba\"', 'json', 10, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `testcases` VALUES (7, 4, 1, NULL, NULL, '{\"s\": \"racecar\"}', '\"racecar\"', 'json', 10, '2025-10-17 16:53:25', '2025-10-17 16:53:25');
INSERT INTO `testcases` VALUES (8, 4, 2, NULL, NULL, '{\"s\": \"OpenAI GPT\"}', '\"TPG IAnepO\"', 'json', 10, '2025-10-17 16:53:25', '2025-10-17 16:53:25');

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
                               `user_id` bigint NOT NULL,
                               `role_id` bigint NOT NULL,
                               `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
                               INDEX `idx_user_roles_role`(`role_id` ASC) USING BTREE,
                               CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                               CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (1, 2, '2025-10-17 13:13:45');
INSERT INTO `user_roles` VALUES (1, 3, '2025-10-17 13:13:57');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `email` varchar(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                          `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                          `status` tinyint NOT NULL DEFAULT 1,
                          `last_login_at` datetime NULL DEFAULT NULL,
                          `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                          `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `uk_users_username`(`username` ASC) USING BTREE,
                          UNIQUE INDEX `uk_users_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'DavidHLP', 'lysf15520112973@163.com', '$2a$12$YGHyAEo6oMy/uZzrgY.MBe2UP40qD62lMGlUU4UgH6dhwRE4r3D/W', NULL, NULL, 1, '2025-10-17 18:06:33', '127.0.0.1', '2025-10-15 08:47:45', '2025-10-17 18:06:33');

-- ----------------------------
-- View structure for vw_problem_stats
-- ----------------------------
DROP VIEW IF EXISTS `vw_problem_stats`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `vw_problem_stats` AS select `p`.`id` AS `problem_id`,count(`s`.`id`) AS `submission_count`,sum((case when (`s`.`verdict` = 'AC') then 1 else 0 end)) AS `solved_count`,round((case when (count(`s`.`id`) = 0) then NULL else ((sum((case when (`s`.`verdict` = 'AC') then 1 else 0 end)) / count(`s`.`id`)) * 100) end),2) AS `acceptance_rate`,max(`s`.`created_at`) AS `last_submission_at` from (`problems` `p` left join `submissions` `s` on((`s`.`problem_id` = `p`.`id`))) group by `p`.`id`;

-- ----------------------------
-- View structure for vw_user_problem_best
-- ----------------------------
DROP VIEW IF EXISTS `vw_user_problem_best`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `vw_user_problem_best` AS select `s`.`user_id` AS `user_id`,`s`.`problem_id` AS `problem_id`,min((case when (`s`.`verdict` = 'AC') then `s`.`created_at` else NULL end)) AS `first_ac_time`,max(`s`.`score`) AS `best_score` from `submissions` `s` group by `s`.`user_id`,`s`.`problem_id`;

SET FOREIGN_KEY_CHECKS = 1;
