/*
 优化后的数据库约束和外键设置（完善注释版）
 系统说明：在线编程题库系统数据库设计

 优化原则：
 1. 权限与用户相关内容不使用级联删除（使用 RESTRICT 或 SET NULL），保护核心数据
 2. 其他业务数据使用级联删除（CASCADE），保证数据一致性
 3. 添加必要的索引以提升查询性能
 4. 完善字段注释，提高代码可维护性
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表：存储系统用户的基本信息和登录状态
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识ID，主键，雪花算法生成',
                        `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名，用于登录，全局唯一，3-20个字符',
                        `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户邮箱，用于登录和找回密码，全局唯一',
                        `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户密码，BCrypt加密存储，至少8位',
                        `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户头像URL地址，支持本地存储和第三方CDN',
                        `introduction` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '用户未填写' COMMENT '用户个人简介，最多255个字符',
                        `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '用户未填写' COMMENT '用户地址信息，用于个人资料展示',
                        `status` int DEFAULT '1' COMMENT '用户状态：1-正常，0-禁用，-1-删除（软删除）',
                        `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后一次登录IP地址，用于安全审计',
                        `last_login` datetime DEFAULT NULL COMMENT '最后一次登录时间，用于用户活跃度统计',
                        `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，自动生成，不可修改',
                        `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间，自动维护',
                        `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                        `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                        PRIMARY KEY (`user_id`) COMMENT '主键索引',
                        UNIQUE KEY `uk_username` (`username`) COMMENT '用户名唯一索引，保证用户名不重复',
                        UNIQUE KEY `uk_email` (`email`) COMMENT '邮箱唯一索引，保证邮箱不重复',
                        KEY `idx_status` (`status`) COMMENT '状态索引，用于快速查询正常用户',
                        KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引，用于用户注册时间统计'
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户基础信息表，存储用户账户信息和基本资料';

-- ----------------------------
-- 角色表：定义系统中的角色类型，用于权限管理
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色唯一标识ID，主键',
                        `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称，如：管理员、普通用户、VIP用户等，全局唯一',
                        `status` int DEFAULT NULL COMMENT '角色状态：1-启用，0-禁用，用于角色的启用和停用',
                        `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '角色描述，说明角色的用途和权限范围',
                        `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '角色创建时间，自动生成',
                        `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '角色最后更新时间，自动维护',
                        `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                        `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                        PRIMARY KEY (`id`) COMMENT '主键索引',
                        UNIQUE KEY `uk_role_name` (`role_name`) COMMENT '角色名称唯一索引，防止角色名重复',
                        KEY `idx_status` (`status`) COMMENT '状态索引，用于快速查询启用的角色'
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统角色定义表，用于RBAC权限控制体系';

-- ----------------------------
-- 用户角色关联表：多对多关系，一个用户可以拥有多个角色
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
                             `user_id` bigint NOT NULL COMMENT '用户ID，关联user表的user_id字段',
                             `role_id` bigint NOT NULL COMMENT '角色ID，关联role表的id字段',
                             `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关联关系创建时间，记录用户获得角色的时间',
                             `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '关联关系更新时间，自动维护',
                             `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                             `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                             PRIMARY KEY (`user_id`,`role_id`) COMMENT '联合主键，防止重复分配相同角色',
                             KEY `idx_role_id` (`role_id`) COMMENT '角色ID索引，用于查询拥有某角色的所有用户',
                             KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，用于查询用户的所有角色',
    -- 用户删除时，用户角色关联记录级联删除，保证数据完整性
    -- 角色删除时，用户角色关联记录级联删除，自动清理失效关联
                             CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                             CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表，实现用户与角色的多对多映射关系';

-- ----------------------------
-- 认证令牌表：存储用户的JWT令牌信息，用于会话管理
-- ----------------------------
DROP TABLE IF EXISTS `token`;
CREATE TABLE `token` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Token记录唯一标识ID，主键',
                         `user_id` bigint NOT NULL COMMENT '关联的用户ID，标识token属于哪个用户',
                         `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'JWT令牌字符串，用于用户身份验证',
                         `token_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '令牌类型：ACCESS_TOKEN-访问令牌，REFRESH_TOKEN-刷新令牌',
                         `expired` tinyint(1) NOT NULL COMMENT '是否已过期：1-已过期，0-未过期',
                         `revoked` tinyint(1) NOT NULL COMMENT '是否已撤销：1-已撤销，0-未撤销，用于主动注销',
                         `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Token创建时间，用于计算过期时间',
                         `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Token状态更新时间',
                         `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                         `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                         PRIMARY KEY (`id`) COMMENT '主键索引',
                         KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，用于查询用户的所有token',
                         KEY `idx_token` (`token`) COMMENT 'Token索引，用于快速验证token有效性',
                         KEY `idx_expired_revoked` (`expired`, `revoked`) COMMENT '过期和撤销状态联合索引，快速筛选有效token',
    -- Token随用户删除而级联删除，清理用户相关的认证信息
                         CONSTRAINT `fk_token_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1958440676415025154 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户认证令牌表，用于JWT身份验证和会话管理';

-- ----------------------------
-- 编程题目表：存储算法编程题的详细信息
-- ----------------------------
DROP TABLE IF EXISTS `problems`;
CREATE TABLE `problems` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目唯一标识ID，主键，自动增长',
                            `problem_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目类型：ACM-竞赛模式，OI-信息学竞赛模式',
                            `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目标题，简洁描述题目要求，不超过255字符',
                            `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目详细描述，包含背景、输入输出格式、样例等，支持Markdown格式',
                            `solution_function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '解题函数名称，用于在线判题系统调用用户代码',
                            `difficulty` enum('EASY','MEDIUM','HARD') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'EASY' COMMENT '题目难度等级：EASY-简单，MEDIUM-中等，HARD-困难',
                            `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目标签，JSON数组格式存储，如["数组","双指针","排序"]，用于分类检索',
                            `solved_count` int DEFAULT '0' COMMENT '题目被成功解决的总次数，用于统计题目难度和热度',
                            `submission_count` int DEFAULT '0' COMMENT '题目总提交次数，包含成功和失败的提交',
                            `created_by` bigint DEFAULT NULL COMMENT '题目创建者的用户ID，关联user表，用于标识题目来源',
                            `is_visible` tinyint(1) DEFAULT '1' COMMENT '题目可见性：1-对所有用户可见，0-仅管理员可见，用于题目审核',
                            `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '题目创建时间，自动生成时间戳',
                            `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '题目最后更新时间，修改时自动更新',
                            `category` enum('ALGORITHMS','DATABASE','SHELL','MULTI_THREADING','JAVASCRIPT','PANDAS') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目所属大分类：算法、数据库、Shell、多线程、JavaScript、Pandas',
                            `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（兼容字段）',
                            `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（兼容字段）',
                            `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                            `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                            PRIMARY KEY (`id`) COMMENT '主键索引',
                            KEY `idx_created_by` (`created_by`) COMMENT '创建者索引，用于查询某用户创建的题目',
                            KEY `idx_difficulty` (`difficulty`) COMMENT '难度索引，用于按难度筛选题目',
                            KEY `idx_category` (`category`) COMMENT '分类索引，用于按分类检索题目',
                            KEY `idx_is_visible` (`is_visible`) COMMENT '可见性索引，快速过滤可见题目',
                            KEY `idx_create_time` (`created_at`) COMMENT '创建时间索引，用于按时间排序',
    -- 题目创建者删除时，将created_by设置为NULL，保留题目数据
                            CONSTRAINT `fk_problems_creator` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=281 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='编程题目主表，存储算法题、数据库题等各类编程挑战';

-- ----------------------------
-- 测试用例输出表：存储每个题目的测试用例和期望输出
-- ----------------------------
DROP TABLE IF EXISTS `test_cases_outputs`;
CREATE TABLE `test_cases_outputs` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试用例唯一标识ID，主键',
                                      `problem_id` bigint NOT NULL COMMENT '关联的题目ID，外键引用problems表，标识测试用例属于哪个题目',
                                      `output` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '测试用例的期望输出结果，用于判题系统对比',
                                      `output_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '输出数据类型：string-字符串，int-整数，array-数组等，用于类型检查',
                                      `score` int DEFAULT '10' COMMENT '该测试用例的分值权重，用于部分分判题，默认每个测试点10分',
                                      `is_sample` tinyint(1) DEFAULT '0' COMMENT '是否为样例测试用例：1-样例（对用户可见），0-隐藏测试用例',
                                      `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '测试用例创建时间',
                                      `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（兼容字段）',
                                      `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（兼容字段）',
                                      `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                                      `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                                      PRIMARY KEY (`id`) COMMENT '主键索引',
                                      KEY `idx_problem_id` (`problem_id`) COMMENT '题目ID索引，用于查询题目的所有测试用例',
                                      KEY `idx_is_sample` (`is_sample`) COMMENT '样例标识索引，快速区分样例和隐藏测试用例',
    -- 题目删除时，所有相关测试用例级联删除，保证数据一致性
                                      CONSTRAINT `fk_test_cases_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='测试用例输出表，存储题目的标准答案和评分标准';

-- ----------------------------
-- 测试用例输入表：存储测试用例的输入数据，支持多参数输入
-- ----------------------------
DROP TABLE IF EXISTS `test_case_inputs`;
CREATE TABLE `test_case_inputs` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '输入记录唯一标识ID，主键',
                                    `test_case_output_id` bigint NOT NULL COMMENT '关联的测试用例ID，外键引用test_cases_outputs表',
                                    `test_case_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '输入参数名称，如nums、target等，用于函数参数映射',
                                    `input_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '输入数据类型：int、string、array、matrix等，用于数据解析',
                                    `input_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '具体的输入数据内容，JSON格式存储复杂数据结构',
                                    `order_index` int NOT NULL DEFAULT '0' COMMENT '输入参数的顺序索引，从0开始，保证多参数函数的参数顺序正确',
                                    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '输入数据创建时间',
                                    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '输入数据更新时间',
                                    `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                                    `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                                    PRIMARY KEY (`id`) COMMENT '主键索引',
                                    KEY `idx_test_case_output_id` (`test_case_output_id`) COMMENT '测试用例ID索引，用于查询测试用例的所有输入',
                                    KEY `idx_order_index` (`order_index`) COMMENT '顺序索引，确保输入参数的正确排序',
    -- 测试用例删除时，所有相关输入数据级联删除
                                    CONSTRAINT `fk_test_inputs_case` FOREIGN KEY (`test_case_output_id`) REFERENCES `test_cases_outputs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='测试用例输入表，存储测试用例的输入参数，支持多参数和复杂数据类型';

-- ----------------------------
-- 代码提交记录表：存储用户的代码提交和判题结果
-- ----------------------------
DROP TABLE IF EXISTS `submissions`;
CREATE TABLE `submissions` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交记录唯一标识ID，主键',
                               `user_id` bigint NOT NULL COMMENT '提交用户ID，关联user表，标识代码提交者',
                               `problem_id` bigint NOT NULL COMMENT '题目ID，关联problems表，标识提交的是哪道题',
                               `language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '编程语言：java、python、cpp、javascript等',
                               `source_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户提交的完整源代码',
                               `status` enum('PENDING','JUDGING','ACCEPTED','CONTINUE','WRONG_ANSWER','TIME_LIMIT_EXCEEDED','MEMORY_LIMIT_EXCEEDED','OUTPUT_LIMIT_EXCEEDED','RUNTIME_ERROR','COMPILE_ERROR','SYSTEM_ERROR','PRESENTATION_ERROR','SECURITY_ERROR') COLLATE utf8mb4_general_ci DEFAULT 'PENDING' COMMENT '判题状态：PENDING-等待判题，JUDGING-判题中，ACCEPTED-通过，WRONG_ANSWER-答案错误等',
                               `error_test_case_id` bigint DEFAULT NULL COMMENT '出错的测试用例ID，用于错误定位和调试提示',
                               `error_test_case_output` text COLLATE utf8mb4_general_ci COMMENT '出错时用户代码的实际输出，用于对比分析',
                               `error_test_case_expect_output` text COLLATE utf8mb4_general_ci COMMENT '出错测试用例的期望输出，用于错误提示',
                               `score` int DEFAULT '0' COMMENT '本次提交得分，满分通常为100分，支持部分分判题',
                               `time_used` int DEFAULT '0' COMMENT '程序执行耗时，单位毫秒，用于性能统计',
                               `memory_used` int DEFAULT '0' COMMENT '程序执行内存消耗，单位KB，用于内存限制检查',
                               `compile_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '编译错误详细信息，帮助用户定位语法错误',
                               `judge_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '详细判题信息，JSON格式存储各测试点的执行结果',
                               `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间，记录代码提交的确切时刻',
                               `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（兼容字段）',
                               `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（兼容字段）',
                               `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                               `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                               PRIMARY KEY (`id`) COMMENT '主键索引',
                               KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，用于查询用户的提交历史',
                               KEY `idx_problem_id` (`problem_id`) COMMENT '题目ID索引，用于查询题目的提交统计',
                               KEY `idx_status` (`status`) COMMENT '状态索引，用于筛选通过的提交',
                               KEY `idx_language` (`language`) COMMENT '编程语言索引，用于语言使用统计',
                               KEY `idx_created_at` (`created_at`) COMMENT '提交时间索引，用于时间排序',
                               KEY `idx_user_problem` (`user_id`, `problem_id`) COMMENT '用户题目联合索引，快速查询用户对特定题目的提交',
    -- 用户删除时，提交记录级联删除，清理用户相关数据
    -- 题目删除时，提交记录级联删除，保证数据完整性
                               CONSTRAINT `fk_submissions_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                               CONSTRAINT `fk_submissions_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=161 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码提交记录表，存储用户代码提交和判题结果的完整信息';

-- ----------------------------
-- 题解表：存储用户分享的解题思路和代码
-- ----------------------------
DROP TABLE IF EXISTS `solutions`;
CREATE TABLE `solutions` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题解唯一标识ID，主键',
                             `problem_id` bigint NOT NULL COMMENT '对应的题目ID，关联problems表，标识题解解答的题目',
                             `user_id` bigint NOT NULL COMMENT '题解作者用户ID，关联user表，标识题解的贡献者',
                             `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题解标题，简洁概括解题思路或方法',
                             `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题解详细内容，Markdown格式，包含思路分析、代码实现、复杂度分析等',
                             `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题解标签，JSON数组格式，如["动态规划","空间优化"]，便于分类检索',
                             `language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题解中主要代码示例使用的编程语言',
                             `views` int NOT NULL DEFAULT '0' COMMENT '题解浏览次数，用于热度排序和推荐算法',
                             `comments` int NOT NULL DEFAULT '0' COMMENT '题解评论数量，用于互动热度统计',
                             `status` enum('PENDING','APPROVED','REJECTED') COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题解审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
                             `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '题解创建时间，记录发布时刻',
                             `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '题解最后更新时间，修改时自动更新',
                             `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                             `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                             PRIMARY KEY (`id`) COMMENT '主键索引',
                             KEY `idx_problem_id` (`problem_id`) COMMENT '题目ID索引，用于查询题目的所有题解',
                             KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，用于查询用户的题解贡献',
                             KEY `idx_status` (`status`) COMMENT '状态索引，快速筛选已审核通过的题解',
                             KEY `idx_language` (`language`) COMMENT '编程语言索引，按语言分类题解',
                             KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引，用于时间排序',
    -- 题目删除时，相关题解级联删除，保证数据一致性
    -- 用户删除时，用户的题解级联删除
                             CONSTRAINT `fk_solutions_problem` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                             CONSTRAINT `fk_solutions_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题目解答表，存储用户分享的解题思路、代码实现和技术分析';

-- ----------------------------
-- 题解评论表：支持多层嵌套的评论和回复系统
-- ----------------------------
DROP TABLE IF EXISTS `solution_comments`;
CREATE TABLE `solution_comments` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论唯一标识ID，主键',
                                     `solution_id` bigint NOT NULL COMMENT '被评论的题解ID，关联solutions表，标识评论所属的题解',
                                     `user_id` bigint NOT NULL COMMENT '评论发表者用户ID，关联user表，标识评论作者',
                                     `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论具体内容，支持Markdown格式，可包含代码片段',
                                     `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，NULL表示顶层评论，非NULL表示对某评论的回复',
                                     `root_id` bigint DEFAULT NULL COMMENT '根评论ID，用于快速定位评论树的根节点，便于拉取完整讨论串',
                                     `reply_to_user_id` bigint DEFAULT NULL COMMENT '被回复用户ID，用于@功能和消息通知，提升用户互动体验',
                                     `status` enum('Pending','Approved','Rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Pending' COMMENT '评论审核状态：Pending-待审核，Approved-已通过，Rejected-已拒绝',
                                     `meta` json DEFAULT NULL COMMENT '评论元数据，JSON格式存储IP地址、User-Agent、设备信息等，用于安全审计',
                                     `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论发表时间，记录评论创建的确切时刻',
                                     `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '评论最后更新时间，编辑时自动更新',
                                     `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（兼容字段）',
                                     `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（兼容字段）',
                                     `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                                     `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                                     PRIMARY KEY (`id`) COMMENT '主键索引',
                                     KEY `idx_solution_id_root_id` (`solution_id`,`root_id`) COMMENT '题解和根评论联合索引，快速获取评论树',
                                     KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，查询用户的评论历史',
                                     KEY `idx_parent_id` (`parent_id`) COMMENT '父评论ID索引，快速查找子评论',
                                     KEY `idx_reply_to_user_id` (`reply_to_user_id`) COMMENT '被回复用户索引，用于消息通知查询',
                                     KEY `idx_status` (`status`) COMMENT '状态索引，筛选已审核通过的评论',
                                     KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引，用于评论时间排序',
    -- 题解删除时，所有相关评论级联删除，保证数据完整性
    -- 用户删除时，用户发表的评论级联删除
    -- 父评论删除时，子评论级联删除，避免孤儿评论
    -- 被回复用户删除时，将reply_to_user_id设为NULL，保留评论内容
                                     CONSTRAINT `fk_comment_solution` FOREIGN KEY (`solution_id`) REFERENCES `solutions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                     CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                     CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `solution_comments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                     CONSTRAINT `fk_comment_reply_to_user` FOREIGN KEY (`reply_to_user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题解评论表，支持多层嵌套回复的评论系统，提供完整的讨论功能';

-- ----------------------------
-- 用户内容浏览记录表：记录用户对题目和题解的浏览历史
-- ----------------------------
DROP TABLE IF EXISTS `user_content_views`;
CREATE TABLE `user_content_views` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浏览记录唯一标识ID，主键',
                                      `user_id` bigint NOT NULL COMMENT '浏览用户ID，关联user表，标识浏览者身份',
                                      `content_id` bigint NOT NULL COMMENT '被浏览内容的ID，可能是题目ID或题解ID，根据content_type区分',
                                      `content_type` enum('SOLUTION','PROBLEM') COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '内容类型：SOLUTION-题解，PROBLEM-题目，用于区分浏览对象',
                                      `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次浏览时间，记录用户第一次访问该内容的时间',
                                      `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（兼容字段）',
                                      `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（兼容字段）',
                                      `create_at` bigint DEFAULT NULL COMMENT '创建时间戳（毫秒），兼容旧系统',
                                      `update_at` bigint DEFAULT NULL COMMENT '更新时间戳（毫秒），兼容旧系统',
                                      PRIMARY KEY (`id`) COMMENT '主键索引',
                                      UNIQUE KEY `uk_user_content_view` (`user_id`,`content_id`,`content_type`) COMMENT '用户内容类型联合唯一索引，防止重复浏览记录',
                                      KEY `idx_content_id_type` (`content_id`, `content_type`) COMMENT '内容ID和类型联合索引，统计内容浏览量',
                                      KEY `idx_created_at` (`created_at`) COMMENT '浏览时间索引，用于浏览趋势分析',
    -- 用户删除时，浏览记录级联删除，清理用户相关数据
                                      CONSTRAINT `fk_view_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户内容浏览记录表，统计用户对题目和题解的独立访问情况，支持浏览量统计和个性化推荐';

-- ----------------------------
-- 点赞点踩记录表：记录用户对各类内容的点赞和点踩操作
-- ----------------------------
DROP TABLE IF EXISTS `like_dislike_record`;
CREATE TABLE `like_dislike_record` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞点踩记录唯一标识ID，主键',
                                       `user_id` bigint NOT NULL COMMENT '操作用户ID，关联user表，标识是谁进行的点赞或点踩',
                                       `target_type` enum('ARTICLE','COMMENT','REPLY','SOLUTION') NOT NULL COMMENT '目标内容类型：ARTICLE-文章，COMMENT-评论，REPLY-回复，SOLUTION-题解',
                                       `target_id` bigint NOT NULL COMMENT '目标内容ID，被点赞或点踩的具体内容标识，根据target_type解释',
                                       `action_type` enum('LIKE','DISLIKE') NOT NULL COMMENT '操作类型：LIKE-点赞（支持），DISLIKE-点踩（反对）',
                                       `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间，记录点赞或点踩的确切时刻',
                                       `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间，用户改变态度时自动更新',
                                       PRIMARY KEY (`id`) COMMENT '主键索引',
                                       UNIQUE KEY `uk_user_target` (`user_id`,`target_type`,`target_id`) COMMENT '用户目标类型内容联合唯一索引，确保用户对同一内容只能有一个操作状态',
                                       KEY `idx_target` (`target_type`,`target_id`) COMMENT '目标类型和ID联合索引，快速统计某内容的点赞点踩数量',
                                       KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，查询用户的所有点赞点踩历史',
                                       KEY `idx_created_at` (`created_at`) COMMENT '操作时间索引，用于热度趋势分析',
    -- 用户删除时，点赞点踩记录级联删除，清理用户行为数据
                                       CONSTRAINT `fk_like_dislike_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞点踩记录表，统一管理用户对各类内容的态度表达，支持点赞数统计和内容质量评估';

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 性能优化索引：针对常用查询场景添加复合索引
-- ----------------------------

-- 用户提交历史查询优化：按用户、状态、时间排序
ALTER TABLE `submissions` ADD INDEX `idx_user_status_time` (`user_id`, `status`, `created_at`) COMMENT '用户提交状态时间复合索引，优化个人提交历史查询';

-- 题目题解查询优化：按题目、审核状态、发布时间排序
ALTER TABLE `solutions` ADD INDEX `idx_problem_status_time` (`problem_id`, `status`, `create_time`) COMMENT '题目状态时间复合索引，优化题目题解列表查询';

-- 题解评论查询优化：按题解、审核状态、发表时间排序
ALTER TABLE `solution_comments` ADD INDEX `idx_solution_status_time` (`solution_id`, `status`, `created_at`) COMMENT '题解状态时间复合索引，优化评论列表查询';

-- ----------------------------
-- 全文搜索索引：支持题目和题解的内容搜索（可选功能）
-- 注意：全文索引会影响写入性能，建议根据实际需求开启
-- ----------------------------

-- 题目标题和描述全文搜索索引
-- ALTER TABLE `problems` ADD FULLTEXT INDEX `ft_title_description` (`title`, `description`) COMMENT '题目标题描述全文索引，支持题目内容搜索功能';

-- 题解标题和内容全文搜索索引
-- ALTER TABLE `solutions` ADD FULLTEXT INDEX `ft_title_content` (`title`, `content`) COMMENT '题解标题内容全文索引，支持题解内容搜索功能';

-- ----------------------------
-- 数据库设计总结说明
-- ----------------------------
/*
本数据库设计遵循以下原则：

1. 【数据完整性】
   - 所有关键业务数据都有适当的外键约束
   - 权限相关表使用SET NULL，保护核心用户数据
   - 业务数据表使用CASCADE，确保数据一致性

2. 【性能优化】
   - 为高频查询字段添加单列索引
   - 为复杂查询场景添加复合索引
   - 预留全文搜索索引接口

3. 【可扩展性】
   - 使用枚举类型定义状态值，便于扩展
   - JSON字段存储标签和元数据，支持灵活扩展
   - 预留时间戳字段，兼容不同时间格式

4. 【安全性】
   - 用户敏感操作有完整审计日志
   - 支持软删除和状态管理
   - IP和设备信息记录，便于安全分析

5. 【业务支持】
   - 完整的权限管理体系（RBAC）
   - 丰富的统计字段，支持数据分析
   - 多层级评论系统，提升用户互动
   - 点赞点踩系统，内容质量评估
*/