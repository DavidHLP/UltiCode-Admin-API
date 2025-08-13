/*
 Navicat Premium Dump SQL

 Source Server         : 129.204.227.158
 Source Server Type    : MySQL
 Source Server Version : 80406 (8.4.6)
 Source Schema         : spring_oj

 Target Server Type    : MySQL
 Target Server Version : 80406 (8.4.6)
 File Encoding         : 65001

 Date: 13/08/2025 15:07:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for problems
-- ----------------------------
DROP TABLE IF EXISTS `problems`;
CREATE TABLE `problems` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID，主键，自动增长',
                            `problem_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目类型',
                            `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目标题，不能为空',
                            `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目正文描述，不能为空',
                            `solution_function_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '运行方法名称',
                            `time_limit` int DEFAULT '1000' COMMENT '时间限制，单位为毫秒，默认为1000ms',
                            `memory_limit` int DEFAULT '128' COMMENT '内存限制，单位为MB，默认为128MB',
                            `difficulty` enum('EASY','MEDIUM','HARD') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'EASY' COMMENT '题目难度，枚举类型，默认为''EASY''',
                            `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目标签，建议使用JSON格式的字符串存储，例如：''["数组", "动态规划"]''',
                            `solved_count` int DEFAULT '0' COMMENT '成功解答的次数，默认为0',
                            `submission_count` int DEFAULT '0' COMMENT '总提交次数，默认为0',
                            `created_by` bigint DEFAULT NULL COMMENT '题目创建者的用户ID',
                            `is_visible` tinyint(1) DEFAULT '1' COMMENT '题目是否对普通用户可见，默认为TRUE',
                            `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
                            `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间，当记录被更新时自动更新为当前时间戳',
                            `category` enum('ALGORITHMS','DATABASE','SHELL','MULTI_THREADING','JAVASCRIPT','PANDAS') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目所属的大分类标签',
                            `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                            `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            `create_at` bigint DEFAULT NULL,
                            `update_at` bigint DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            KEY `created_by` (`created_by`),
                            CONSTRAINT `problems_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=279 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of problems
-- ----------------------------
BEGIN;
INSERT INTO `problems` (`id`, `problem_type`, `title`, `description`, `solution_function_name`, `time_limit`, `memory_limit`, `difficulty`, `tags`, `solved_count`, `submission_count`, `created_by`, `is_visible`, `created_at`, `updated_at`, `category`) VALUES (278, 'ACM', '最长回文子串', '给你一个字符串 `s`，找到 `s` 中最长的回文子串。\n\n\n#### 示例 1:\n> 输入: s = \"babad\"  \n输出: \"bab\"  \n解释: \"aba\" 同样是符合题意的答案。  \n\n\n#### 示例 2:\n>输入: s = \"cbbd\"  \n输出: \"bb\"  \n\n\n#### 提示:\n- \\( 1 \\leq s.length \\leq 1000 \\)  \n- `s` 仅由数字和英文字母组成  ', 'longestPalindrome', 1000, 128, 'MEDIUM', '[\"双指针\",\"字符串\",\"动态规划\"]', 0, 0, NULL, 1, '2025-08-09 12:58:49', '2025-08-09 12:58:49', 'ALGORITHMS');
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
                        `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                        `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `create_at` bigint DEFAULT NULL,
                        `update_at` bigint DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of role
-- ----------------------------
BEGIN;
INSERT INTO `role` (`id`, `role_name`, `status`, `remark`, `create_time`, `update_time`) VALUES (1, 'ADMIN', 1, '管理员角色', '2025-07-18 09:10:58', '2025-07-18 22:26:27');
INSERT INTO `role` (`id`, `role_name`, `status`, `remark`, `create_time`, `update_time`) VALUES (2, 'MANAGE', 1, '管理员', '2025-07-20 23:07:24', '2025-07-20 23:11:32');
INSERT INTO `role` (`id`, `role_name`, `status`, `remark`, `create_time`, `update_time`) VALUES (3, 'USER', 1, '普通用户角色', '2025-07-17 20:10:58', '2025-07-19 21:11:35');
COMMIT;

-- ----------------------------
-- Table structure for solution_comments
-- ----------------------------
DROP TABLE IF EXISTS `solution_comments`;
CREATE TABLE `solution_comments` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID，主键',
                                     `solution_id` bigint NOT NULL COMMENT '被评论的题解ID，关联到solutions.id',
                                     `user_id` bigint NOT NULL COMMENT '评论发表者的用户ID，关联到user.user_id',
                                     `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容，支持Markdown',
                                     `parent_id` bigint DEFAULT NULL COMMENT '回复的父评论ID，为NULL表示顶层评论',
                                     `root_id` bigint DEFAULT NULL COMMENT '所属的根评论ID，用于快速拉取整个评论树',
                                     `reply_to_user_id` bigint DEFAULT NULL COMMENT '被回复用户的ID，用于前端显示@某人',
                                     `upvotes` int DEFAULT '0' COMMENT '评论的点赞数',
                                     `downvotes` int DEFAULT '0' COMMENT '评论的点踩数',
                                     `status` enum('Pending','Approved','Rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Pending' COMMENT '评论状态，用于审核',
                                     `meta` json DEFAULT NULL COMMENT '元数据，可存储IP、User-Agent等信息',
                                     `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                                     `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
                                     `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                                     `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     `create_at` bigint DEFAULT NULL,
                                     `update_at` bigint DEFAULT NULL,
                                     PRIMARY KEY (`id`),
                                     KEY `idx_solution_id_root_id` (`solution_id`,`root_id`),
                                     KEY `idx_user_id` (`user_id`),
                                     KEY `idx_parent_id` (`parent_id`),
                                     CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `solution_comments` (`id`) ON DELETE CASCADE,
                                     CONSTRAINT `fk_comment_solution` FOREIGN KEY (`solution_id`) REFERENCES `solutions` (`id`) ON DELETE CASCADE,
                                     CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题解的评论及回复表（增强版）';

-- ----------------------------
-- Records of solution_comments
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for solutions
-- ----------------------------
DROP TABLE IF EXISTS `solutions`;
CREATE TABLE `solutions` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题解ID，主键，自动增长',
                             `problem_id` bigint NOT NULL COMMENT '对应的题目ID，关联到problems表',
                             `user_id` bigint NOT NULL COMMENT '题解作者的用户ID，关联到user表',
                             `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题解标题',
                             `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题解内容，使用Markdown格式存储',
                             `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标签',
                             `language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题解中代码示例所用的编程语言',
                             `views` int NOT NULL DEFAULT '0' COMMENT '浏览量',
                             `upvotes` int NOT NULL DEFAULT '0' COMMENT '点赞数',
                             `downvotes` int NOT NULL DEFAULT '0' COMMENT '点踩数',
                             `comments` int NOT NULL DEFAULT '0' COMMENT '评论数',
                             `status` enum('Pending','Approved','Rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Pending' COMMENT '题解状态，默认为''Pending''，用于审核',
                             `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                             `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
                             `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                             `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             `create_at` bigint DEFAULT NULL,
                             `update_at` bigint DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             KEY `problem_id` (`problem_id`),
                             KEY `user_id` (`user_id`),
                             CONSTRAINT `solutions_ibfk_1` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE,
                             CONSTRAINT `solutions_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题目题解表';

-- ----------------------------
-- Records of solutions
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for submissions
-- ----------------------------
DROP TABLE IF EXISTS `submissions`;
CREATE TABLE `submissions` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交记录ID，主键，自动增长',
                               `user_id` bigint NOT NULL COMMENT '提交用户的ID，关联到users表',
                               `problem_id` bigint NOT NULL COMMENT '题目的ID，关联到problems表',
                               `language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '编程语言，例如：''cpp'', ''java'', ''python''',
                               `source_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户提交的源代码',
                               `status` enum('Pending','Judging','Accepted','Wrong Answer','Time Limit Exceeded','Memory Limit Exceeded','Runtime Error','Compile Error','System Error') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Pending' COMMENT '判题状态，默认为''Pending''',
                               `score` int DEFAULT '0' COMMENT '得分，默认为0',
                               `time_used` int DEFAULT '0' COMMENT '程序执行耗时，单位为毫秒',
                               `memory_used` int DEFAULT '0' COMMENT '程序执行内存消耗，单位为KB',
                               `compile_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '编译错误时的详细信息',
                               `judge_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '判题的详细信息，建议使用JSON格式存储',
                               `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
                               `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                               `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               `create_at` bigint DEFAULT NULL,
                               `update_at` bigint DEFAULT NULL,
                               PRIMARY KEY (`id`),
                               KEY `user_id` (`user_id`),
                               KEY `problem_id` (`problem_id`),
                               CONSTRAINT `submissions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
                               CONSTRAINT `submissions_ibfk_2` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of submissions
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for test_case_inputs
-- ----------------------------
DROP TABLE IF EXISTS `test_case_inputs`;
CREATE TABLE `test_case_inputs` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '输入记录的ID，主键',
                                    `test_case_output_id` bigint NOT NULL COMMENT '关联的测试用例ID，外键',
                                    `test_case_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '输入内容名称',
                                    `input_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '输入类型',
                                    `input_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '单个输入的内容',
                                    `order_index` int NOT NULL DEFAULT '0' COMMENT '输入的顺序，从0开始，用于保证多次输入的先后次序',
                                    `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                                    `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    `create_at` bigint DEFAULT NULL,
                                    `update_at` bigint DEFAULT NULL,
                                    PRIMARY KEY (`id`),
                                    KEY `idx_test_case_id` (`test_case_output_id`),
                                    CONSTRAINT `fk_input_test_case` FOREIGN KEY (`test_case_output_id`) REFERENCES `test_cases_outputs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='测试用例的多个输入表';

-- ----------------------------
-- Records of test_case_inputs
-- ----------------------------
BEGIN;
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`) VALUES (18, 39, 's', 'STRING', '\"cbbd\"', 0);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`) VALUES (19, 40, 's', 'STRING', '\"bb\"', 0);
COMMIT;

-- ----------------------------
-- Table structure for test_cases_outputs
-- ----------------------------
DROP TABLE IF EXISTS `test_cases_outputs`;
CREATE TABLE `test_cases_outputs` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试用例ID，主键，自动增长',
                                      `problem_id` bigint NOT NULL COMMENT '关联的题目ID，外键，关联到problems表',
                                      `output` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '期望的输出',
                                      `output_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '输出类型',
                                      `score` int DEFAULT '10' COMMENT '该测试点的分值，默认为10',
                                      `is_sample` tinyint(1) DEFAULT '0' COMMENT '是否为样例测试用例，默认为FALSE',
                                      `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
                                      `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                                      `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      `create_at` bigint DEFAULT NULL,
                                      `update_at` bigint DEFAULT NULL,
                                      PRIMARY KEY (`id`),
                                      KEY `problem_id` (`problem_id`),
                                      CONSTRAINT `test_cases_outputs_ibfk_1` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of test_cases_outputs
-- ----------------------------
BEGIN;
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`) VALUES (39, 278, '\"bb\"', 'STRING', 10, 1, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`) VALUES (40, 278, '\"cbbd\"', 'STRING', 10, 1, '2025-08-12 15:03:40');
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
                         `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                         `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         `create_at` bigint DEFAULT NULL,
                         `update_at` bigint DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         KEY `user_id` (`user_id`),
                         CONSTRAINT `token_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1955492480759951363 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of token
-- ----------------------------
BEGIN;
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1949672146643267586, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1MzY3MzAzNywiZXhwIjoxNzUzNzU5NDM3fQ.FZAMifVSL8iXuKGKHdFjN_ZwoJT92CtZWoY8iLKlw80', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1949821332223180802, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1MzcwODYwNiwiZXhwIjoxNzUzNzk1MDA2fQ.QXs_IqHbpx3tnM_GWCFTEJ66UO2ZxBoeXXjEV78NTzQ', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1950078105987358722, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzc2OTgyNSwiZXhwIjoxNzUzODU2MjI1fQ.OV4lm_y08ZuwsC0Kqdi4ReCo4fuwrHm-cDzdxcbmyMI', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1950089039300546562, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzc3MjQzMiwiZXhwIjoxNzUzODU4ODMyfQ.NhnECn9H567ZYoOH_ItmsPQLgbOK21ICdxanBQs7SFQ', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1950184660170756098, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzc5NTIzMCwiZXhwIjoxNzUzODgxNjMwfQ.lo4JbDoH6jh0l1u1jw_66bh3waHeR_m223lxQUFMWLU', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1950379196176154625, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzg0MTYxMSwiZXhwIjoxNzUzOTI4MDExfQ.52906g-OkAwv4zs2-ucSWtjF2FGC48UHgTPBUCkpTIo', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1950453344705478658, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzg1OTI4OSwiZXhwIjoxNzUzOTQ1Njg5fQ.H5QIhsFOUJ253smwWy3WP4fAmo_xqB__Hsqt5YJRbh8', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1950549107661946881, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzg4MjEyMSwiZXhwIjoxNzUzOTY4NTIxfQ.BX93CyKDzlX2EbELjerFZcru8uXF8D5DWs4ikA6TdR0', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1950784406790893569, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1MzkzODIyMSwiZXhwIjoxNzU0MDI0NjIxfQ.K3yBfxZVcObJhkerSlP4eqRHgpVTWOOwy0Hs0Bil-ME', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1954476403015872513, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgxODQ2MSwiZXhwIjoxNzU0OTA0ODYxfQ.PDqMM6yAnT7lOmBGmvoMFzKv8gCq4zc2EkU02uPHYU8', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1954478844348264449, 2, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZEhMUCIsImlhdCI6MTc1NDgxOTA0MywiZXhwIjoxNzU0OTA1NDQzfQ.JNzGyw2c4zzJTvGtHLr1ULfcKHPQvznbrTOyTfGRRmA', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1954495667114405889, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgyMzA1NCwiZXhwIjoxNzU0OTA5NDU0fQ.FzcRG3YdZuEhImVSe4tgSSzrnG2oNWbMxLBUDUGF7qs', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1954495701910351874, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgyMzA2MiwiZXhwIjoxNzU0OTA5NDYyfQ.qNbx0TGpHdJEqxsM4pmX8oFBGvwFq3bePKwIeQWKshc', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1954495738316910594, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgyMzA3MSwiZXhwIjoxNzU0OTA5NDcxfQ.69rtCD-MHtKG1gqMSI5JTQxjzLs8uCITUQjeQffviEg', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1954495886057029634, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgyMzEwNiwiZXhwIjoxNzU0OTA5NTA2fQ.pB3L8qplAVqdxVS6xsSCRZ-GuQQvfv083sA1oBHM2_E', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1954497512851390466, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgyMzQ5NCwiZXhwIjoxNzU0OTA5ODk0fQ.kofEpDAQealhyfxNZScXJ-raseKTsiBI1WDhAqXU8Vg', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1954567456779739137, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDg0MDE3MCwiZXhwIjoxNzU0OTI2NTcwfQ.kBOq6-NzPeoq5v7TYoNYh0hvMWd-qNt2XOgujg-yYko', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955096741126930433, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2NjM2MSwiZXhwIjoxNzU1MDUyNzYxfQ.iL9eklmybjYr0GhtREB28Y4N20la-9pVKymra-fNicE', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955097221261492226, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2NjQ3NiwiZXhwIjoxNzU1MDUyODc2fQ.q44u5lv2aoOI2EVBL8-CvRv8uNO2PLS22LC_mxaTgiE', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955097803753848833, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2NjYxNCwiZXhwIjoxNzU1MDUzMDE0fQ.XsK4aEYtMYtufyLkUTNT5N0-HXr4LsX47WNFp8CQEmw', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955097935006203905, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2NjY0NiwiZXhwIjoxNzU1MDUzMDQ2fQ.Gd9wFQY-DAZh8gPWQxf4jRh2WtOZc5ALI9QvFHsiaHo', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955098025213100034, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2NjY2NywiZXhwIjoxNzU1MDUzMDY3fQ.HuvUkIFE-Az143t_K8Axx-EMdow_utmVGGf6Pfx3mB0', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955098775989321730, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2Njg0NiwiZXhwIjoxNzU1MDUzMjQ2fQ.k0ZtMBVImeP_WleOE55S5edBXEQ-3s_ABWIQX9Byrxc', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955098911524057090, 2, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZEhMUCIsImlhdCI6MTc1NDk2Njg3OSwiZXhwIjoxNzU1MDUzMjc5fQ.2tU31skR51OsmldtlVVSKYXoyMLtHqnB2tWHzlR9_PI', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955116929373179905, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk3MTE3NCwiZXhwIjoxNzU1MDU3NTc0fQ.KnZScyB-N7gPsvwYMpsGqKX9Ob3BpBqWzdY2rFKbczQ', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955145369107333122, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk3Nzk1NSwiZXhwIjoxNzU1MDY0MzU1fQ.XlVJJ8B_8UIh5tSVAazoBU1FHTKrXtiYQ5zQCYsZEjI', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955146796525457409, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk3ODI5NSwiZXhwIjoxNzU1MDY0Njk1fQ.bSv6omOX9XGf4DEb1sjODVtcPI9lUTQyw_nJosgCDXg', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955151001885659138, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk3OTI5OCwiZXhwIjoxNzU1MDY1Njk4fQ.uGrcUBWcS7s_DP1Uu56mGklt8OWy_psJ-Dw8sJQI6_A', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955168579152564225, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk4MzQ4OSwiZXhwIjoxNzU1MDY5ODg5fQ.icX8bWeXLUkv2oqwGsg846SJ8-k1BMxMFvM_OfgKzoY', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955170039403683841, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk4MzgzNywiZXhwIjoxNzU1MDcwMjM3fQ.ngs24p4kQOvoVKWPXPsBEcyl-6qd_Q_LQdRvF3WMoM0', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955183033911140354, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk4NjkzNSwiZXhwIjoxNzU1MDczMzM1fQ.Kt228huASH4YB8JiOlDbu-juVfXpG15p-nG8fcBCXy0', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955462113214656513, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NTA1MzQ3MywiZXhwIjoxNzU1MTM5ODczfQ.tErwGdKx3BUVCXFmLoQMuZtqPq9xQD71sQHw0wN3tfc', 'ACCESS', 0, 0);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`) VALUES (1955492480759951362, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NTA2MDcxMywiZXhwIjoxNzU1MTQ3MTEzfQ.TB4UmSE0Bmz1FwuF0Ycho4LvgLTbrH0R8RzLmMX5NOA', 'ACCESS', 0, 0);
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
                        `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                        `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `create_at` bigint DEFAULT NULL,
                        `update_at` bigint DEFAULT NULL,
                        PRIMARY KEY (`user_id`),
                        UNIQUE KEY `username` (`username`),
                        UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`) VALUES (1, 'David', 'lysf15520112973@163.com', '$2a$10$G3KNWvjNkRKrExgQsA6ppOD2qkYW.RiCN9HhGYiVMdchwPSoPxUwG', NULL, '用户未填写', '用户未填写', 1, NULL, NULL, '2025-07-12 00:00:00');
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`) VALUES (2, 'DavidHLP', '1372998589@qq.com', '$2a$10$v.rEgO20nH7DHW0nOnDPTOkxIdL4D44arGzBrHhWmamKPSSGuTQ2G', NULL, '用户未填写', '用户未填写', 1, NULL, NULL, '2025-07-26 16:32:50');
COMMIT;

-- ----------------------------
-- Table structure for user_content_views
-- ----------------------------
DROP TABLE IF EXISTS `user_content_views`;
CREATE TABLE `user_content_views` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浏览记录ID，主键',
                                      `user_id` bigint NOT NULL COMMENT '浏览用户的ID，关联到user.user_id',
                                      `content_id` bigint NOT NULL COMMENT '被浏览内容的ID (可能是题目ID或题解ID)',
                                      `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次浏览时间',
                                      `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                                      `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      `create_at` bigint DEFAULT NULL,
                                      `update_at` bigint DEFAULT NULL,
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_user_content_view` (`user_id`,`content_id`),
                                      CONSTRAINT `fk_view_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户对内容（题目/题解）的独立浏览记录表';

-- ----------------------------
-- Records of user_content_views
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
                             `user_id` bigint NOT NULL,
                             `role_id` bigint NOT NULL,
                             `create_time` timestamp DEFAULT CURRENT_TIMESTAMP,
                             `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 2);
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 3);
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (2, 3);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;