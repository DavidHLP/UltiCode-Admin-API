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

 Date: 05/08/2025 18:41:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for codetemplate
-- ----------------------------
DROP TABLE IF EXISTS `codetemplate`;
CREATE TABLE `codetemplate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID，主键',
  `problem_id` bigint NOT NULL COMMENT '关联的题目ID',
  `language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '编程语言, 例如: C++, Java, Python',
  `main_wrapper_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '主函数或核心逻辑外的包装代码模板',
  `solution_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '提供给用户的解题函数/类模板',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_problem_language` (`problem_id`,`language`),
  CONSTRAINT `fk_codetemplate_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题目代码模板表';

-- ----------------------------
-- Table structure for problems
-- ----------------------------
DROP TABLE IF EXISTS `problems`;
CREATE TABLE `problems` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID，主键，自动增长',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目标题，不能为空',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目正文描述，不能为空',
  `time_limit` int DEFAULT '1000' COMMENT '时间限制，单位为毫秒，默认为1000ms',
  `memory_limit` int DEFAULT '128' COMMENT '内存限制，单位为MB，默认为128MB',
  `difficulty` enum('Easy','Medium','Hard') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Easy' COMMENT '题目难度，枚举类型，默认为''Easy''',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目标签，建议使用JSON格式的字符串存储，例如：''["数组", "动态规划"]''',
  `solved_count` int DEFAULT '0' COMMENT '成功解答的次数，默认为0',
  `submission_count` int DEFAULT '0' COMMENT '总提交次数，默认为0',
  `created_by` bigint DEFAULT NULL COMMENT '题目创建者的用户ID',
  `is_visible` tinyint(1) DEFAULT '1' COMMENT '题目是否对普通用户可见，默认为TRUE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间，当记录被更新时自动更新为当前时间戳',
  `category` enum('ALGORITHMS','DATABASE','SHELL','MULTI_THREADING','JAVASCRIPT','PANDAS') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目所属的大分类标签',
  PRIMARY KEY (`id`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `problems_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=278 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `status` int DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
  PRIMARY KEY (`id`),
  KEY `idx_solution_id_root_id` (`solution_id`,`root_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `solution_comments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_solution` FOREIGN KEY (`solution_id`) REFERENCES `solutions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题解的评论及回复表（增强版）';

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
  PRIMARY KEY (`id`),
  KEY `problem_id` (`problem_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `solutions_ibfk_1` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE,
  CONSTRAINT `solutions_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题目题解表';

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
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `problem_id` (`problem_id`),
  CONSTRAINT `submissions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `submissions_ibfk_2` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for test_case_inputs
-- ----------------------------
DROP TABLE IF EXISTS `test_case_inputs`;
CREATE TABLE `test_case_inputs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '输入记录的ID，主键',
  `test_case_id` bigint NOT NULL COMMENT '关联的测试用例ID，外键',
  `test_case_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '输入内容名称',
  `input_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '单个输入的内容',
  `order_index` int NOT NULL DEFAULT '0' COMMENT '输入的顺序，从0开始，用于保证多次输入的先后次序',
  PRIMARY KEY (`id`),
  KEY `idx_test_case_id` (`test_case_id`),
  CONSTRAINT `fk_input_test_case` FOREIGN KEY (`test_case_id`) REFERENCES `test_cases` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='测试用例的多个输入表';

-- ----------------------------
-- Table structure for test_cases
-- ----------------------------
DROP TABLE IF EXISTS `test_cases`;
CREATE TABLE `test_cases` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试用例ID，主键，自动增长',
  `problem_id` bigint NOT NULL COMMENT '关联的题目ID，外键，关联到problems表',
  `output` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '期望的输出',
  `score` int DEFAULT '10' COMMENT '该测试点的分值，默认为10',
  `is_sample` tinyint(1) DEFAULT '0' COMMENT '是否为样例测试用例，默认为FALSE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
  PRIMARY KEY (`id`),
  KEY `problem_id` (`problem_id`),
  CONSTRAINT `test_cases_ibfk_1` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `token_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1950784406790893570 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user_content_views
-- ----------------------------
DROP TABLE IF EXISTS `user_content_views`;
CREATE TABLE `user_content_views` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浏览记录ID，主键',
  `user_id` bigint NOT NULL COMMENT '浏览用户的ID，关联到user.user_id',
  `content_id` bigint NOT NULL COMMENT '被浏览内容的ID (可能是题目ID或题解ID)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次浏览时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_content_view` (`user_id`,`content_id`),
  CONSTRAINT `fk_view_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户对内容（题目/题解）的独立浏览记录表';

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
