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

 Date: 17/08/2025 14:58:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for like_dislike_record
-- ----------------------------
DROP TABLE IF EXISTS `like_dislike_record`;
CREATE TABLE `like_dislike_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（谁点的）',
  `target_type` enum('ARTICLE','COMMENT','REPLY','SOLUTION') NOT NULL COMMENT '目标类型（1:文章 2:评论 3:回复等）',
  `target_id` bigint NOT NULL COMMENT '目标ID（被点赞/点踩的内容ID）',
  `action_type` enum('LIKE','DISLIKE') NOT NULL COMMENT '操作类型（1:点赞 2:点踩）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_type`,`target_id`) COMMENT '确保用户对同一内容只能有一个状态',
  KEY `idx_target` (`target_type`,`target_id`) COMMENT '用于统计某内容的点赞/点踩数量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞点踩记录表';

-- ----------------------------
-- Records of like_dislike_record
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for problems
-- ----------------------------
DROP TABLE IF EXISTS `problems`;
CREATE TABLE `problems` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID，主键，自动增长',
  `problem_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目类型',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目标题，不能为空',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目正文描述，不能为空',
  `solution_function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '运行方法名称',
  `difficulty` enum('EASY','MEDIUM','HARD') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'EASY' COMMENT '题目难度，枚举类型，默认为''EASY''',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目标签，建议使用JSON格式的字符串存储，例如：''["数组", "动态规划"]''',
  `solved_count` int DEFAULT '0' COMMENT '成功解答的次数，默认为0',
  `submission_count` int DEFAULT '0' COMMENT '总提交次数，默认为0',
  `created_by` bigint DEFAULT NULL COMMENT '题目创建者的用户ID',
  `is_visible` tinyint(1) DEFAULT '1' COMMENT '题目是否对普通用户可见，默认为TRUE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间，当记录被更新时自动更新为当前时间戳',
  `category` enum('ALGORITHMS','DATABASE','SHELL','MULTI_THREADING','JAVASCRIPT','PANDAS') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '题目所属的大分类标签',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `created_by` (`created_by`),
  CONSTRAINT `problems_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=281 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of problems
-- ----------------------------
BEGIN;
INSERT INTO `problems` (`id`, `problem_type`, `title`, `description`, `solution_function_name`, `difficulty`, `tags`, `solved_count`, `submission_count`, `created_by`, `is_visible`, `created_at`, `updated_at`, `category`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (278, 'ACM', '5. 最长回文子串', '给你一个字符串 `s`，找到 `s` 中最长的回文子串。\n\n\n#### 示例 1:\n> 输入: s = \"babad\"  \n输出: \"bab\"  \n解释: \"aba\" 同样是符合题意的答案。  \n\n\n#### 示例 2:\n>输入: s = \"cbbd\"  \n输出: \"bb\"  \n\n\n#### 提示:\n- \\( 1 \\leq s.length \\leq 1000 \\)  \n- `s` 仅由数字和英文字母组成  ', 'longestPalindrome', 'MEDIUM', '[\"双指针\",\"字符串\",\"动态规划\"]', 0, 0, NULL, 1, '2025-08-08 23:58:49', '2025-08-08 23:58:49', 'ALGORITHMS', '2025-08-13 15:40:56', '2025-08-16 20:01:32', NULL, NULL);
INSERT INTO `problems` (`id`, `problem_type`, `title`, `description`, `solution_function_name`, `difficulty`, `tags`, `solved_count`, `submission_count`, `created_by`, `is_visible`, `created_at`, `updated_at`, `category`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (279, 'ACM', '2660. 保龄球游戏的获胜者', '### 2660. 保龄球游戏的获胜者\n\n给你两个从 **0 开始** 的整数数组 `player1` 和 `player2` ，分别表示玩家 1 和玩家 2 击中的瓶数。  \n\n保龄球比赛由 `n` 轮 **回合** 组成，每轮的瓶数恰好为 `10` 。  \n\n假设玩家在第 `i` 轮中击中 `xi` 个瓶子，那么玩家的得分 `S` 为：  \n- 如果玩家在该轮的前两轮中击中了 `10` 个瓶子，则得分为 `2xi` 。  \n- 否则，得分为 `xi` 。  \n\n玩家的得分是其 `n` 轮得分的总和。  \n\n返回：  \n- 如果玩家 1 的得分高于玩家 2 的得分，则为 `1`；  \n- 如果玩家 2 的得分高于玩家 1 的得分，则为 `2`；  \n- 如果平局，则为 `0` 。  \n\n\n**示例 1：**  \n> 输入：player1 = [10,3,7], player2 = [5,7,2]  \n输出：1  \n解释：  \n玩家 1 的得分：  \n第 0 轮：10（前两轮无，得 10）  \n第 1 轮：3（前一轮不是 10，得 3）  \n第 2 轮：7（前一轮不是 10，得 7）  \n总分：10 + 3 + 7 = 20  \n>\n> 玩家 2 的得分：  \n第 0 轮：5（前两轮无，得 5）  \n第 1 轮：7（前一轮不是 10，得 7）  \n第 2 轮：2（前一轮不是 10，得 2）  \n总分：5 + 7 + 2 = 14  \n>\n> 玩家 1 得分更高，返回 1  \n\n**示例 2：**  \n> 输入：player1 = [3,5,7,6], player2 = [8,10,10,2]  \n输出：2  \n解释：  \n玩家 1 的得分：  \n第 0 轮：3（前两轮无，得 3）  \n第 1 轮：5（前一轮不是 10，得 5）  \n第 2 轮：7（前一轮不是 10，得 7）  \n第 3 轮：6（前一轮不是 10，得 6）  \n总分：3 + 5 + 7 + 6 = 21  \n>\n> 玩家 2 的得分：  \n第 0 轮：8（前两轮无，得 8）  \n第 1 轮：10（前一轮不是 10，得 10）  \n第 2 轮：10（前一轮是 10，得 2*10=20）  \n第 3 轮：2（前两轮都是 10，得 2*2=4）  \n总分：8 + 10 + 20 + 4 = 42  \n>\n> 玩家 2 得分更高，返回 2  \n\n**示例 3：**  \n>输入：player1 = [2,3], player2 = [4,1]  \n输出：0  \n解释：  \n玩家 1 的得分：2 + 3 = 5  \n玩家 2 的得分：4 + 1 = 5  \n平局，返回 0  \n\n\n**提示：**  \n- `n == player1.length == player2.length`  \n- `1 <= n <= 1000`  \n- `0 <= player1[i], player2[i] <= 10`  ', 'isWinner', 'EASY', '[\"数组\",\"模拟\"]', 0, 0, NULL, 1, '2025-08-16 19:58:29', '2025-08-16 19:58:29', 'ALGORITHMS', '2025-08-16 19:58:29', '2025-08-16 19:58:29', NULL, NULL);
INSERT INTO `problems` (`id`, `problem_type`, `title`, `description`, `solution_function_name`, `difficulty`, `tags`, `solved_count`, `submission_count`, `created_by`, `is_visible`, `created_at`, `updated_at`, `category`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (280, 'ACM', '2734. 执行子串操作后的字典序最小字符串', '### 题目描述\n给你一个仅由小写英文字母组成的字符串 `s` 。在一步操作中，你可以完成以下行为:\n- 选择 `s` 的任一非空子字符串，可能是整个字符串，接着将字符串中的每一个字符替换为英文字母表中的前一个字符。例如，\'b\' 用 \'a\' 替换，\'a\' 用 \'z\' 替换。\n\n返回执行上述操作**恰好一次**后可以获得的字典序最小的字符串。\n\n**子字符串** 是字符串中的一个连续字符序列。\n\n现有长度相同的两个字符串 `x` 和字符串 `y`，在满足 `x[i] != y[i]` 的第一个位置 `i` 上，如果 `x[i]` 在字母表中先于 `y[i]` 出现，则认为字符串 `x` 比字符串 `y` 字典序更小。\n\n**示例 1:**\n>- 输入: `s = \"cbabc\"`\n>- 输出: `\"baabc\"` \n>- 解释: 我们选择从下标 0 开始、到下标 1 结束的子字符串执行操作。可以证明最终得到的字符串是字典序最小的。\n\n**示例 2:**\n>- 输入: `s = \"acbbc\"` \n>- 输出: `\"abaab\"` \n>- 解释: 我们选择从下标 1 开始、到下标 4 结束的子字符串执行操作。可以证明最终得到的字符串是字典序最小的。\n\n**示例 3:**\n>- 输入: `s = \"leetcode\"` \n>- 输出: `\"kddsbncd\"` \n>- 解释: 我们选择整个字符串执行操作。可以证明最终得到的字符串是字典序最小的。\n\n**提示**\n- $1 <= s.length <= 3 * 10^5$ \n- `s` 仅由小写英文字母组成 ', 'smallestString', 'MEDIUM', '[\"贪心\",\"字符串\"]', 0, 0, NULL, 1, '2025-08-16 07:22:45', '2025-08-16 07:22:45', 'ALGORITHMS', '2025-08-16 20:22:45', '2025-08-16 20:25:23', NULL, NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of role
-- ----------------------------
BEGIN;
INSERT INTO `role` (`id`, `role_name`, `status`, `remark`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1, 'ADMIN', 1, '管理员角色', '2025-07-18 09:10:58', '2025-07-18 22:26:27', NULL, NULL);
INSERT INTO `role` (`id`, `role_name`, `status`, `remark`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (2, 'MANAGE', 1, '管理员', '2025-07-20 23:07:24', '2025-07-20 23:11:32', NULL, NULL);
INSERT INTO `role` (`id`, `role_name`, `status`, `remark`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (3, 'USER', 1, '普通用户角色', '2025-07-17 20:10:58', '2025-07-19 21:11:35', NULL, NULL);
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
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_solution_id_root_id` (`solution_id`,`root_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `solution_comments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_solution` FOREIGN KEY (`solution_id`) REFERENCES `solutions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题解的评论及回复表（增强版）';

-- ----------------------------
-- Records of solution_comments
-- ----------------------------
BEGIN;
INSERT INTO `solution_comments` (`id`, `solution_id`, `user_id`, `content`, `parent_id`, `root_id`, `reply_to_user_id`, `upvotes`, `downvotes`, `status`, `meta`, `created_at`, `updated_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1, 26, 1, '测试', NULL, NULL, NULL, 0, 0, 'Pending', NULL, '2025-08-13 22:56:44', '2025-08-13 22:56:44', '2025-08-13 22:56:44', '2025-08-13 22:56:44', NULL, NULL);
INSERT INTO `solution_comments` (`id`, `solution_id`, `user_id`, `content`, `parent_id`, `root_id`, `reply_to_user_id`, `upvotes`, `downvotes`, `status`, `meta`, `created_at`, `updated_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (2, 26, 1, '回复测试', 1, NULL, 1, 0, 0, 'Pending', NULL, '2025-08-13 23:03:01', '2025-08-13 23:03:01', '2025-08-13 23:03:01', '2025-08-13 23:03:01', NULL, NULL);
INSERT INTO `solution_comments` (`id`, `solution_id`, `user_id`, `content`, `parent_id`, `root_id`, `reply_to_user_id`, `upvotes`, `downvotes`, `status`, `meta`, `created_at`, `updated_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (3, 26, 1, '测试多root回复', NULL, NULL, NULL, 0, 0, 'Pending', NULL, '2025-08-13 23:03:15', '2025-08-13 23:03:15', '2025-08-13 23:03:15', '2025-08-13 23:03:15', NULL, NULL);
INSERT INTO `solution_comments` (`id`, `solution_id`, `user_id`, `content`, `parent_id`, `root_id`, `reply_to_user_id`, `upvotes`, `downvotes`, `status`, `meta`, `created_at`, `updated_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (4, 26, 1, '测试回复第二个root', 3, NULL, 1, 0, 0, 'Pending', NULL, '2025-08-13 23:03:30', '2025-08-13 23:03:30', '2025-08-13 23:03:30', '2025-08-13 23:03:30', NULL, NULL);
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
  `status` enum('PENDING','APPROVED','REJECTED') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `problem_id` (`problem_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `solutions_ibfk_1` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE,
  CONSTRAINT `solutions_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题目题解表';

-- ----------------------------
-- Records of solutions
-- ----------------------------
BEGIN;
INSERT INTO `solutions` (`id`, `problem_id`, `user_id`, `title`, `content`, `tags`, `language`, `views`, `upvotes`, `downvotes`, `comments`, `status`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (26, 278, 1, '测试题解', '## **第一部分：`Reference`类与垃圾回收器的底层**\n\n**架构性考量**: 虚引用配合 `Cleaner` API 是 中 `finalize()` 方法的正确替代方案。自 Java 9 起，`java.lang.ref.Cleaner` API 作为对传统 `虚引用+队列+线程` 最佳实践的官方封装，是管理**堆外资源**（如 JNI 内存、`DirectByteBuffer`、本地文件句柄、GPU 内存等）的**唯一推荐方式**。在 的生态中，它与 **Foreign Function & Memory API (孵化特性)** 协同工作，为现代 Java 应用提供了安全、高效的资源管理模式。`Cleaner` 通过强制分离清理任务与被清理对象，保证了安全性与可靠性，并提供了显式 `close()` 和兜底 GC 清理的\"双重保障\"模式。作协议。\n\n所有引用类型的行为均源于 `java.lang.ref.Reference` 抽象类与垃圾回收器之间的一套精密且底层的协作协议。\n\n### **`Reference` 对象的生命周期状态机**\n\n`Reference` 对象的生命周期是其与 GC 交互过程的体现，可划分为四个明确的阶段。理解此状态机是掌握引用机制的前提：\n\n1.  **Active (活跃态)**: `Reference` 对象的初始及常规状态。此时，通过其 `get()` 方法可成功获取其指代的对象（Referent）。\n2.  **Pending (待决态)**: 对象生命周期的临界状态。当 GC 完成可达性分析，确认一个对象的可达性不再满足其引用类型的要求时（例如，不再强可达），GC 将以原子操作将对应的 `Reference` 对象添加至一个内部的待处理列表（`discovered` 链表）。\n3.  **Enqueued (入队态)**: 回收通知的发出阶段。JVM 内部一个关键的守护线程——`Reference-Handler`——将轮询上述待处理列表。它会取出待处理的 `Reference` 对象，将其内部的 `referent` 字段置为 `null`，随后将该 `Reference` 对象本身置入其构造时关联的 `ReferenceQueue`。\n4.  **Inactive (失活态)**: 生命周期的终结。当应用程序从 `ReferenceQueue` 中显式移除了该 `Reference` 对象，或对于一个未关联队列的引用，其指代对象已被完全回收后，该 `Reference` 对象即进入此最终状态。\n\n此流程揭示了一个核心设计思想：Java 的引用处理是一种高度解耦的、异步的事件通知模型。\n\n### **中的底层源码剖析：`java.lang.ref.Reference.java`**\n\n```java\n// java.lang.ref.Reference.java (版本核心字段解读)\npublic abstract class Reference<T> {\n\n    // 核心指代：指向被引用的对象。\n    // 在 中，使用 VarHandle 进行原子性、线程安全地访问，\n    // 以应对 ZGC、G1GC 等现代并发 GC 的挑战。GC 通过内存屏障和特权指令直接操作此字段。\n    private T referent;\n\n    // VarHandle 实例，JDK 9+ 引入，用于原子操作\n    private static final VarHandle REFERENT;\n\n    static {\n        try {\n            MethodHandles.Lookup l = MethodHandles.lookup();\n            REFERENT = l.findVarHandle(Reference.class, \"referent\", Object.class);\n        } catch (ReflectiveOperationException e) {\n            throw new ExceptionInInitializerError(e);\n        }---\ntitle: Java17 引用类型详解：从 `Reference` 四大引用\npublished: 2025-07-12\ndescription: 深入理解 Java 四种引用类型的底层机制、应用场景与架构设计哲学，掌握 JDK 17 中的引用处理协议。\ntags: [Java, 引用类型, 垃圾回收]\ncategory: Java\ndraft: false\n---\n\n    }\n\n    // 异步通知队列：当指代对象被回收，此Reference对象会被放入该队列。\n    // volatile 保证了多线程（应用线程与GC/Reference-Handler线程）之间的可见性。\n    volatile ReferenceQueue<? super T> queue;\n\n    // GC内部工作链表指针：用于将待处理的Reference对象链接成一个对开发者透明的内部链表。\n    @SuppressWarnings(\"rawtypes\")\n    volatile Reference next;\n\n    // 待处理列表头指针：一个静态字段，作为所有待处理Reference对象链表的入口。\n    private static Reference<Object> pending = null;\n\n    // 构造函数与核心方法\n    Reference(T referent, ReferenceQueue<? super T> queue) {\n        this.referent = referent;\n        this.queue = (queue == null) ? ReferenceQueue.NULL : queue;\n    }\n\n    // 中的 get() 方法，使用 VarHandle 确保内存一致性\n    @SuppressWarnings(\"unchecked\")\n    public T get() {\n        return (T) REFERENT.getAcquire(this);\n    }\n\n    // 提供的原子性清除方法\n    public void clear() {\n        REFERENT.setRelease(this, null);\n    }\n}\n```\n\n---\n\n## **第二部分：四种引用类型的深度剖析与架构应用**\n\n本部分将逐一分析每种引用类型，融合其底层行为、应用实践与架构性考量。\n\n### **强引用 (Strong Reference)**\n\n- **底层行为**: Java 的默认引用模式，通过 `new`、`astore` 等字节码指令实现。只要从 GC Root 到对象存在强引用路径，垃圾回收器就**绝不**会回收该对象，即使系统因内存耗尽而抛出 `OutOfMemoryError`。在 中，ZGC 和 G1GC 的并发特性进一步优化了强引用的处理性能。\n\n- **应用实践**:\n\n  ```java\n  import java.lang.ref.Cleaner;\n\n  /**\n   * 强引用示例 - 使用 Cleaner API 替代已废弃的 finalize\n   * 运行环境: JDK 17+ with ZGC: -XX:+UseZGC -XX:+UnlockExperimentalVMOptions\n   */\n  public class StrongReferenceExample {\n\n      // 推荐使用 Cleaner 替代 finalize\n      private static final Cleaner cleaner = Cleaner.create();\n\n      public static void main(String[] args) throws InterruptedException {\n          // myObject 是一个强引用，指向 MyResource 实例\n          MyResource myObject = new MyResource(\"StrongResource\");\n          System.out.println(\"对象已创建 -> \" + myObject);\n\n          // 将强引用设置为null，切断从GC Root到对象的唯一强引用路径\n          myObject = null;\n          System.out.println(\"强引用已置null，建议GC...\");\n\n          // 中更推荐明确的垃圾回收请求\n          System.gc();\n          Thread.sleep(1000); // 给 Cleaner 线程足够时间执行\n\n          System.out.println(\"程序结束\");\n      }\n\n      /**\n       * 推荐的资源管理方式：使用 Cleaner API\n       * Record 类是 的预览特性，这里使用常规类演示\n       */\n      static class MyResource {\n          private final String name;\n          private final Cleaner.Cleanable cleanable;\n\n          public MyResource(String name) {\n              this.name = name;\n              // 注册清理动作，避免 this 引用逃逸\n              this.cleanable = cleaner.register(this, new CleanupAction(name));\n          }\n\n          // 实现 AutoCloseable 以支持 try-with-resources\n          public void close() {\n              cleanable.clean();\n          }\n\n          @Override\n          public String toString() {\n              return \"MyResource{name=\'\" + name + \"\'}\";\n          }\n\n          // 清理动作必须是静态类，避免持有外部类引用\n          private static class CleanupAction implements Runnable {\n              private final String resourceName;\n\n              CleanupAction(String resourceName) {\n                  this.resourceName = resourceName;\n              }\n\n              @Override\n              public void run() {\n                  System.out.println(\"!!! 资源对象 [\" + resourceName + \"] 已被 Cleaner 清理 !!!\");\n              }\n          }\n      }\n  }\n  ```\n\n- **执行结果**:\n  ```text\n  对象已创建 -> MyResource{name=\'StrongResource\'}\n  强引用已置null，建议GC...\n  !!! 资源对象 [StrongResource] 已被 Cleaner 清理 !!!\n  程序结束\n  ```\n- **架构性考量**: 内存泄漏的根本原因在于**对象逻辑生命周期与其实际持有的强引用生命周期不匹配**。在 的现代架构设计中，须特别警惕因 `ModuleLayer`、静态集合、Lambda 表达式捕获、监听器等长生命周期实体持有短生命周期对象引用而引发的内存泄漏问题。的 **JFR (Java Flight Recorder)** 和 **Application Class Data Sharing** 特性为内存治理提供了更强大的工具支持。建立内存基线并将其纳入持续集成流程，配合现代 APM 工具，是主动进行内存治理的有效策略。\n\n### **软引用 (Soft Reference)**\n\n- **底层行为**: `SoftReference` 的回收行为由 JVM 内部策略决定，在 中可通过 HotSpot 的 `-XX:SoftRefLRUPolicyMSPerMB` 参数进行调优。配合 ZGC 或 G1GC，其回收时机与系统内存压力及对象最近被访问的时间相关联，但行为本质上仍是**非确定性**的。\n\n- **应用实践**:\n\n  ```java\n  import java.lang.ref.SoftReference;\n  import java.util.ArrayList;\n  import java.util.List;\n\n  /**\n   * 软引用示例\n   * 运行参数: -Xmx32m -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=1\n   */\n  public class SoftReferenceExample {\n\n      static class MyResource {\n          private final String name;\n          private final byte[] data; // 占用内存\n\n          public MyResource(String name) {\n              this.name = name;\n              this.data = new byte[1024 * 1024]; // 1MB\n          }\n\n          @Override\n          public String toString() {\n              return \"MyResource{name=\'\" + name + \"\', size=1MB}\";\n          }\n      }\n\n      public static void main(String[] args) {\n          SoftReference<MyResource> softRef = new SoftReference<>(\n              new MyResource(\"SoftResource\")\n          );\n\n          System.out.println(\"初始状态 -> \" + softRef.get());\n\n          // 在 中使用更精确的内存压力测试\n          System.out.println(\"开始施加内存压力...\");\n          try {\n              List<byte[]> memoryConsumers = new ArrayList<>();\n              int allocatedMB = 0;\n\n              while (softRef.get() != null && allocatedMB < 50) {\n                  memoryConsumers.add(new byte[1024 * 1024]);\n                  allocatedMB++;\n\n                  if (allocatedMB % 5 == 0) {\n                      System.out.printf(\"已分配 %d MB，软引用状态: %s%n\",\n                          allocatedMB, softRef.get() != null ? \"存活\" : \"已回收\");\n                  }\n              }\n          } catch (OutOfMemoryError e) {\n              System.out.println(\"!!! 捕获到 OutOfMemoryError !!!\");\n          }\n\n          System.out.println(\"最终软引用状态 -> \" + softRef.get());\n\n          // 增强的内存信息\n          Runtime runtime = Runtime.getRuntime();\n          long totalMemory = runtime.totalMemory();\n          long freeMemory = runtime.freeMemory();\n          long usedMemory = totalMemory - freeMemory;\n\n          System.out.printf(\"内存使用情况: 已用 %d MB / 总计 %d MB%n\",\n              usedMemory / (1024 * 1024), totalMemory / (1024 * 1024));\n      }\n  }\n  ```\n\n- **执行结果**:\n  ```text\n  初始状态 -> MyResource{name=\'SoftResource\', size=1MB}\n  开始施加内存压力...\n  已分配 5 MB，软引用状态: 存活\n  已分配 10 MB，软引用状态: 存活\n  已分配 15 MB，软引用状态: 已回收\n  最终软引用状态 -> null\n  内存使用情况: 已用 28 MB / 总计 32 MB\n  ```\n- **架构性考量**: `SoftReference` 因其回收时机的不可预测性，在追求高性能、低延迟的严肃系统中应被视为一种**反模式**。它可能导致系统性能的非预期抖动或引发长时间的 Full GC。在 生态中，现代高性能缓存框架（如 **Caffeine 3.x**、**Chronicle Map**）采用**确定性的淘汰算法**（如 W-TinyLFU、LRU 的变体）配合堆外存储，是更为优越的解决方案。的 **Foreign Function & Memory API (预览特性)** 为构建高效的堆外缓存提供了原生支持。\n\n### **弱引用 (Weak Reference)**\n\n- **底层行为**: `WeakReference` 的回收策略具有高度确定性：只要垃圾回收器发现一个对象仅被弱引用指向，**无论当前内存资源是否充裕，该对象都将在下一次垃圾回收过程中被回收**。在 的 ZGC 和 G1GC 中，弱引用的处理得到了进一步优化。\n\n- **应用实践 (`WeakHashMap` + `WeakReference`)**:\n\n  ```java\n  import java.lang.ref.WeakReference;\n  import java.util.Map;\n  import java.util.WeakHashMap;\n  import java.util.concurrent.ConcurrentHashMap;\n\n  /**\n   * 弱引用示例 - 展示现代架构中的最佳实践\n   * 运行环境: JDK 17+ with G1GC: -XX:+UseG1GC -XX:MaxGCPauseMillis=10\n   */\n  public class WeakReferenceExample {\n\n      static class MyResource {\n          private final String name;\n          private final long timestamp;\n\n          public MyResource(String name) {\n              this.name = name;\n              this.timestamp = System.nanoTime();\n          }\n\n          @Override\n          public String toString() {\n              return String.format(\"MyResource{name=\'%s\', id=%d}\", name, timestamp);\n          }\n      }\n\n      public static void main(String[] args) throws InterruptedException {\n          demonstrateWeakHashMap();\n          System.out.println(\"---\");\n          demonstrateWeakReference();\n      }\n\n      /**\n       * 演示 WeakHashMap 的自动清理能力\n       */\n      private static void demonstrateWeakHashMap() throws InterruptedException {\n          Map<MyResource, String> weakMap = new WeakHashMap<>();\n          MyResource key = new MyResource(\"WeakMapKey\");\n\n          weakMap.put(key, \"关联的元数据\");\n          System.out.println(\"WeakHashMap 初始大小: \" + weakMap.size());\n          System.out.println(\"存储的数据: \" + weakMap.get(key));\n\n          // 移除强引用\n          key = null;\n\n          // 中推荐的 GC 触发方式\n          for (int i = 0; i < 3; i++) {\n              System.gc();\n              Thread.sleep(100);\n\n              // WeakHashMap 会在访问时自动清理失效的条目\n              System.out.printf(\"第 %d 次 GC 后，WeakHashMap 大小: %d%n\",\n                  i + 1, weakMap.size());\n          }\n      }\n\n      /**\n       * 演示弱引用在缓存场景中的应用\n       */\n      private static void demonstrateWeakReference() throws InterruptedException {\n          // 模拟一个使用弱引用的智能缓存\n          Map<String, WeakReference<MyResource>> resourceCache = new ConcurrentHashMap<>();\n\n          // 创建资源并缓存\n          MyResource resource = new MyResource(\"CachedResource\");\n          resourceCache.put(\"key1\", new WeakReference<>(resource));\n\n          System.out.println(\"缓存中的资源: \" + resourceCache.get(\"key1\").get());\n\n          // 移除强引用，模拟资源不再被主业务逻辑使用\n          resource = null;\n\n          // 触发 GC\n          System.gc();\n          Thread.sleep(200);\n\n          // 检查缓存状态\n          WeakReference<MyResource> cachedRef = resourceCache.get(\"key1\");\n          if (cachedRef != null && cachedRef.get() == null) {\n              System.out.println(\"检测到资源已被 GC 回收，从缓存中移除过期条目\");\n              resourceCache.remove(\"key1\");\n          }\n\n          System.out.println(\"清理后缓存大小: \" + resourceCache.size());\n      }\n  }\n  ```\n\n- **执行结果**:\n  ```text\n  WeakHashMap 初始大小: 1\n  存储的数据: 关联的元数据\n  第 1 次 GC 后，WeakHashMap 大小: 0\n  第 2 次 GC 后，WeakHashMap 大小: 0\n  第 3 次 GC 后，WeakHashMap 大小: 0\n  ---\n  缓存中的资源: MyResource{name=\'CachedResource\', id=81403362586300}\n  检测到资源已被 GC 回收，从缓存中移除过期条目\n  清理后缓存大小: 0\n  ```\n- **架构性考量**: 弱引用是在不干涉对象主生命周期的前提下，为其附加元数据或建立关联关系的理想工具。在现代微服务架构中，可用于构建自愈合系统（如自动清理失效的远程连接代理）、实现非侵入式监控。**`ThreadLocal` 的键是弱引用，但其值是强引用，在 的虚拟线程 (Project Loom) 环境中，正确的 `ThreadLocal` 管理变得更加重要。** 在 `finally` 块中调用 `remove()` 是保证线程池状态纯洁性、防止内存泄漏的必要规约，这在高并发的虚拟线程场景下尤为关键。\n\n### **虚引用 (Phantom Reference) & `Cleaner`**\n\n- **底层行为**: `PhantomReference` 的 `get()` 方法永远返回 `null`，从而彻底杜绝了对象被复活的可能性。它**必须**与 `ReferenceQueue` 联合使用，其唯一作用是在指代对象被垃圾回收器确认回收后，提供一个可靠的“死亡通知”。\n- **应用实践 (推荐的 `Cleaner` API)**:\n\n  ```java\n  import java.lang.ref.Cleaner;\n  import java.nio.ByteBuffer;\n  import java.util.concurrent.atomic.AtomicBoolean;\n\n  /**\n   * Cleaner API 最佳实践 - 管理堆外资源\n   * 这是替代 finalize() 的现代化、高性能解决方案\n   */\n  public class CleanerExample implements AutoCloseable {\n\n      // 中 Cleaner 是线程安全的单例\n      private static final Cleaner cleaner = Cleaner.create();\n\n      // 模拟需要清理的堆外资源\n      private final ByteBuffer directBuffer;\n      private final Cleaner.Cleanable cleanable;\n      private final AtomicBoolean closed = new AtomicBoolean(false);\n\n      public CleanerExample(String resourceName, int bufferSize) {\n          // 分配堆外内存\n          this.directBuffer = ByteBuffer.allocateDirect(bufferSize);\n\n          // 注册清理动作 - 注意：CleanupAction 不能持有 this 引用\n          this.cleanable = cleaner.register(this,\n              new CleanupAction(resourceName, directBuffer));\n\n          System.out.printf(\"创建资源 [%s]，分配 %d 字节堆外内存%n\",\n              resourceName, bufferSize);\n      }\n\n      /**\n       * 显式关闭资源 - 推荐的资源管理方式\n       */\n      @Override\n      public void close() {\n          if (closed.compareAndSet(false, true)) {\n              cleanable.clean(); // 立即执行清理\n              System.out.println(\"资源已显式关闭\");\n          }\n      }\n\n      /**\n       * 静态清理动作类 - 关键：不能持有外部类的引用\n       * 中推荐使用 Record 来简化此类静态数据载体\n       */\n      private static final class CleanupAction implements Runnable {\n          private final String resourceName;\n          private final ByteBuffer buffer;\n\n          CleanupAction(String resourceName, ByteBuffer buffer) {\n              this.resourceName = resourceName;\n              this.buffer = buffer;\n          }\n\n          @Override\n          public void run() {\n              // 执行实际的清理工作\n              if (buffer.isDirect()) {\n                  // 在真实场景中，这里会调用 Unsafe.freeMemory()\n                  // 或其他堆外资源释放方法\n                  System.out.printf(\"!!! Cleaner 清理堆外资源 [%s] !!!%n\", resourceName);\n              }\n          }\n      }\n\n      public static void main(String[] args) throws InterruptedException {\n          System.out.println(\"=== 演示显式清理 ===\");\n          demonstrateExplicitCleanup();\n\n          System.out.println(\"\\n=== 演示 GC 触发的清理 ===\");\n          demonstrateGcTriggeredCleanup();\n      }\n\n      /**\n       * 演示显式资源清理（推荐方式）\n       */\n      private static void demonstrateExplicitCleanup() {\n          try (CleanerExample resource = new CleanerExample(\"ExplicitResource\", 1024 * 1024)) {\n              // 使用资源...\n              System.out.println(\"正在使用资源...\");\n          } // try-with-resources 自动调用 close()\n      }\n\n      /**\n       * 演示 GC 触发的清理（兜底机制）\n       */\n      private static void demonstrateGcTriggeredCleanup() throws InterruptedException {\n          CleanerExample resource = new CleanerExample(\"GcResource\", 2 * 1024 * 1024);\n\n          // 移除强引用，让对象变为仅由 Cleaner 跟踪\n          resource = null;\n\n          // 触发 GC，让 Cleaner 执行清理\n          for (int i = 0; i < 3; i++) {\n              System.gc();\n              Thread.sleep(200);\n          }\n\n          System.out.println(\"GC 清理演示完成\");\n      }\n  }\n  ```\n\n- **执行结果**:\n\n  ```text\n    === 演示显式清理 ===\n    创建资源 [ExplicitResource]，分配 1048576 字节堆外内存\n    正在使用资源...\n    !!! Cleaner 清理堆外资源 [ExplicitResource] !!!\n    资源已显式关闭\n\n    === 演示 GC 触发的清理 ===\n    创建资源 [GcResource]，分配 2097152 字节堆外内存\n    !!! Cleaner 清理堆外资源 [GcResource] !!!\n    GC 清理演示完成\n  ```\n\n- **架构性考量**: 虚引用是 `finalize()` 方法的正确替代方案。自 Java 9 起，`java.lang.ref.Cleaner` API 是对此 `虚引用+队列+线程` 最佳实践的官方封装，是管理**堆外资源**（如 JNI 内存、`DirectByteBuffer`、本地文件句柄等）的**唯一推荐方式**。它通过强制分离清理任务与被清理对象，保证了安全性与可靠性，并提供了显式 `close()` 和兜底 GC 清理的“双重保障”模式。\n\n---\n\n## **第三部分：架构师决策矩阵与系统设计哲学**\n\n### **3.1 引用类型决策矩阵**\n\n| 维度/关注点    | 强引用 (Strong)       | 软引用 (Soft)             | 弱引用 (Weak)                | 虚引用/Cleaner (Phantom) |\n| :------------- | :-------------------- | :------------------------ | :--------------------------- | :----------------------- |\n| **行为确定性** | **极高** (永不回收)   | **极低** (依赖 JVM 策略)  | **高** (下次 GC 时回收)      | **高** (对象回收后通知)  |\n| **特性**       | 配合 ZGC/G1GC 优化    | 与现代 GC 算法不匹配      | 支持虚拟线程环境             | Cleaner API 成熟稳定     |\n| **核心应用**   | 对象生命周期主线      | **(已废弃)** 内存敏感缓存 | 元数据关联、缓存键、防止泄漏 | 堆外/本地资源的安全回收  |\n| **架构角色**   | **生命线 (Lifeline)** | **定时炸弹 (Time Bomb)**  | **解耦器 (Decoupler)**       | **守护神 (Guardian)**    |\n| **推荐度**     | ⭐⭐⭐⭐⭐            | ❌ (避免使用)             | ⭐⭐⭐⭐                     | ⭐⭐⭐⭐⭐ (堆外资源)    |\n\n### **3.2 时代：从内存控制到系统设计哲学**\n\n对于精通 的卓越架构师而言，这四种引用类型已超越技术细节，上升为一种融合现代 Java 特性的系统设计哲学观：\n\n1.  **确定性优先原则 (Principle of Determinism)**: 在 的 ZGC、G1GC 等现代垃圾回收器环境中，优先采用行为确定的强引用和弱引用，彻底规避软引用带来的不可预测性，构建行为稳定的系统。\n\n2.  **生命周期对齐原则 (Principle of Lifecycle Alignment)**: 架构的核心任务之一，在于确保数据对象的持有周期与业务逻辑的生命周期严格对齐。在虚拟线程 (Project Loom) 环境中，任何通过强引用导致的生命周期错位，都将在高并发场景下被放大为严重的资源泄漏。\n\n3.  **现代资源管理原则 (Principle of Modern Resource Management)**: 的 `Cleaner` API 配合 Foreign Function & Memory API，为堆外资源管理提供了企业级解决方案。资源的分配者应负责定义其清理规则，通过将清理逻辑与资源本身解耦，可以构建出更具韧性的云原生系统。\n\n4.  **异步解耦原则 (Principle of Asynchrony and Decoupling)**: 引用队列机制在本质上是一种强大的异步事件模型，在 的响应式编程范式中，可资借鉴用于构建基于事件驱动的微服务架构。\n\n5.  **性能观测原则 (Principle of Performance Observability)**: 利用 增强的 JFR (Java Flight Recorder) 和 Application Class Data Sharing 特性，建立引用类型使用的性能基线，将内存治理纳入 DevOps 流程，实现从开发到生产的全链路内存可观测性。\n\n---\n\n## **最佳实践总结**\n\n在 LTS 的现代 Java 开发中：\n\n- **强引用**: 仍是对象生命周期管理的基石，配合 ZGC 等低延迟 GC 提供卓越性能\n- **软引用**: 已被现代缓存解决方案（Caffeine、Chronicle Map）完全替代，应避免使用\n- **弱引用**: 在微服务、虚拟线程等现代架构中发挥关键作用，是解耦设计的重要工具\n- **虚引用/Cleaner**: 中堆外资源管理的黄金标准，与 Foreign Function & Memory API 协同工作\n', '[\"测试\",\"题解\"]', 'java', 1, 0, 0, 0, 'PENDING', '2025-08-13 22:37:01', '2025-08-17 14:10:48', NULL, NULL);
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
  `status` enum('PENDING','JUDGING','ACCEPTED','CONTINUE','WRONG_ANSWER','TIME_LIMIT_EXCEEDED','MEMORY_LIMIT_EXCEEDED','OUTPUT_LIMIT_EXCEEDED','RUNTIME_ERROR','COMPILE_ERROR','SYSTEM_ERROR','PRESENTATION_ERROR','SECURITY_ERROR') COLLATE utf8mb4_general_ci DEFAULT 'PENDING' COMMENT '判题状态，默认为''Pending''',
  `error_test_case_id` bigint DEFAULT NULL,
  `error_test_case_output` text COLLATE utf8mb4_general_ci,
  `error_test_case_expect_output` text COLLATE utf8mb4_general_ci,
  `score` int DEFAULT '0' COMMENT '得分，默认为0',
  `time_used` int DEFAULT '0' COMMENT '程序执行耗时，单位为毫秒',
  `memory_used` int DEFAULT '0' COMMENT '程序执行内存消耗，单位为KB',
  `compile_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '编译错误时的详细信息',
  `judge_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '判题的详细信息，建议使用JSON格式存储',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `problem_id` (`problem_id`),
  CONSTRAINT `submissions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `submissions_ibfk_2` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=155 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of submissions
-- ----------------------------
BEGIN;
INSERT INTO `submissions` (`id`, `user_id`, `problem_id`, `language`, `source_code`, `status`, `error_test_case_id`, `error_test_case_output`, `error_test_case_expect_output`, `score`, `time_used`, `memory_used`, `compile_info`, `judge_info`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (148, 1, 278, 'JAVA', 'class Solution {\n    public String longestPalindrome(String str) {\n        return maxLcpsLength(manacherString(str));\n    }\n\n    public char[] manacherString(String str){\n      char[] ch = str.toCharArray();\n      char[] res = new char[2 * ch.length + 1];\n      int index = 0;\n      for(int i = 0 ; i < res.length ; i++) {\n    	  res[i] = i%2 == 0 ?\'#\':ch[index];\n    	  if(i%2 == 1) index++;\n      }\n      return res;\n    }\n\n    public String maxLcpsLength(char[] str){\n        int[] arr = new int[str.length];\n        \n        int mid = -1;\n        int max = Integer.MIN_VALUE;\n        int r = -1;\n        int rei = -1;\n        \n        for(int i = 0 ; i < str.length ; i++) {\n        	arr[i] = r > i ? Math.min(arr[2*mid - i], r - i): 1;\n        	\n        	while(arr[i] + i < str.length && i - arr[i] > -1) {\n        		if(str[arr[i] + i] == str[i - arr[i]]) arr[i]++;\n        		else break;\n        	}\n        	\n        	if(i + arr[i] > r) {\n        		r = arr[i] + i;\n        		mid = i;\n        	}\n        	\n        	if(max < arr[i]) {\n        		max = arr[i];\n        		rei = i;\n        	}\n        }\n        \n        return new String(str,rei - max + 1 , 2*max - 1).replaceAll(\"#\", \"\");\n        \n    }\n}', 'ACCEPTED', NULL, NULL, NULL, 0, 155, 86016, '编译成功', NULL, '2025-08-17 11:27:39', '2025-08-17 11:27:39', '2025-08-17 11:27:41', NULL, NULL);
INSERT INTO `submissions` (`id`, `user_id`, `problem_id`, `language`, `source_code`, `status`, `error_test_case_id`, `error_test_case_output`, `error_test_case_expect_output`, `score`, `time_used`, `memory_used`, `compile_info`, `judge_info`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (149, 1, 279, 'JAVA', 'class Solution {\n    public int isWinner(int[] player1, int[] player2) {\n        int sum1 = 0;\n        int sum2 = 0;\n        int len = player1.length;\n\n        for (int i = 0; i < len; i++) {\n            sum1 += player1[i];\n            sum2 += player2[i];\n\n            if (i == 1){\n                if (player1[0] == 10) sum1 += player1[i];\n                if (player2[0] == 10) sum2 += player2[i];\n            }\n\n            if (i >= 2){\n                if (player1[i -1] == 10 || player1[i - 2] == 10) sum1 += player1[i];\n                if (player2[i -1] == 10 || player2[i - 2] == 10) sum2 += player2[i];\n            }\n        }\n        if (sum1 > sum2) return 1;\n        if (sum1 < sum2) return 2;\n        return 0;\n    }\n}', 'ACCEPTED', NULL, NULL, NULL, 0, 148, 39936, '编译成功', NULL, '2025-08-17 11:28:26', '2025-08-17 11:28:26', '2025-08-17 11:28:27', NULL, NULL);
INSERT INTO `submissions` (`id`, `user_id`, `problem_id`, `language`, `source_code`, `status`, `error_test_case_id`, `error_test_case_output`, `error_test_case_expect_output`, `score`, `time_used`, `memory_used`, `compile_info`, `judge_info`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (152, 1, 280, 'JAVA', 'class Solution {\n    public String smallestString(String S) {\n        char[] s = S.toCharArray();\n        int n = s.length;\n        for (int i = 0; i < n; i++) {\n            if (s[i] > \'a\') {\n                for (; i < n && s[i] > \'a\'; i++)\n                    s[i]--;\n                return new String(s);\n            }\n        }\n        s[n - 1] = \'z\';\n        return new String(s);\n    }\n}', 'ACCEPTED', NULL, NULL, NULL, 0, 153, 65536, '编译成功', NULL, '2025-08-17 11:31:48', '2025-08-17 11:31:48', '2025-08-17 11:31:49', NULL, NULL);
INSERT INTO `submissions` (`id`, `user_id`, `problem_id`, `language`, `source_code`, `status`, `error_test_case_id`, `error_test_case_output`, `error_test_case_expect_output`, `score`, `time_used`, `memory_used`, `compile_info`, `judge_info`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (153, 1, 278, 'JAVA', 'class Solution {\n    public String longestPalindrome(String str) {\n        return maxLcpsLength(manacherString(str));\n    }\n\n    public char[] manacherString(String str){\n      char[] ch = str.toCharArray();\n      char[] res = new char[2 * ch.length + 1];\n      int index = 0;\n      for(int i = 0 ; i < res.length ; i++) {\n    	  res[i] = i%2 == 0 ?\'#\':ch[index];\n    	  if(i%2 == 1) index++;\n      }\n      return res;\n    }\n\n    public String maxLcpsLength(char[] str){\n        int[] arr = new int[str.length];\n        \n        int mid = -1;\n        int max = Integer.MIN_VALUE;\n        int r = -1;\n        int rei = -1;\n        \n        for(int i = 0 ; i < str.length ; i++) {\n        	int len = r > i ? Math.max(arr[2*mid - i], r - i): 1;\n        	\n        	while(len + i < str.length && i - len > -1) {\n        		if(str[len + i] == str[i - len]) len++;\n        		else break;\n        	}\n        	\n        	arr[i] = len;\n        	\n        	if(i + len > r) {\n        		r = len + i;\n        		mid = i;\n        	}\n        	\n        	if(max < len) {\n        		max = len;\n        		rei = i;\n        	}\n        }\n        \n        return new String(str,rei - max + 1 , 2*max - 1).replaceAll(\"#\", \"\");\n        \n    }\n}', 'WRONG_ANSWER', 43, '\"acaa\"', '\"aca\"', 0, 152, 39936, '编译成功', NULL, '2025-08-17 12:09:48', '2025-08-17 12:09:48', '2025-08-17 12:09:49', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for test_case_inputs
-- ----------------------------
DROP TABLE IF EXISTS `test_case_inputs`;
CREATE TABLE `test_case_inputs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '输入记录的ID，主键',
  `test_case_output_id` bigint NOT NULL COMMENT '关联的测试用例ID，外键',
  `test_case_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '输入内容名称',
  `input_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '输入类型',
  `input_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '单个输入的内容',
  `order_index` int NOT NULL DEFAULT '0' COMMENT '输入的顺序，从0开始，用于保证多次输入的先后次序',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_test_case_id` (`test_case_output_id`),
  CONSTRAINT `fk_input_test_case` FOREIGN KEY (`test_case_output_id`) REFERENCES `test_cases_outputs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='测试用例的多个输入表';

-- ----------------------------
-- Records of test_case_inputs
-- ----------------------------
BEGIN;
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (18, 39, 's', 'STRING', '\"cbbd\"', 0, '2025-08-13 15:40:56', '2025-08-13 15:40:56', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (19, 40, 's', 'STRING', '\"babad\"', 0, '2025-08-13 15:40:56', '2025-08-16 16:02:52', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (25, 43, 's', 'STRING', '\"aacabdkacaa\"', 0, '2025-08-16 16:27:46', '2025-08-16 16:27:46', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (27, 45, 's', 'STRING', '\"aacabdkacaa\"', 0, '2025-08-16 16:28:18', '2025-08-16 16:28:18', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (28, 46, 's', 'STRING', '\"tattarrattat\"', 0, '2025-08-16 18:33:08', '2025-08-16 18:33:08', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (29, 47, 'player1', 'INT_ARRAY', '[5,10,3,2]', 0, '2025-08-16 19:59:59', '2025-08-16 19:59:59', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (30, 47, 'player2', 'INT_ARRAY', '[6,5,7,3]', 1, '2025-08-16 19:59:59', '2025-08-16 19:59:59', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (31, 48, 'player1', 'INT_ARRAY', '[3,5,7,6]', 0, '2025-08-16 20:00:15', '2025-08-16 20:00:15', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (32, 48, 'player2', 'INT_ARRAY', '[8,10,10,2]', 1, '2025-08-16 20:00:15', '2025-08-16 20:00:15', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (33, 49, 'player1', 'INT_ARRAY', '[2,3]', 0, '2025-08-16 20:00:29', '2025-08-16 20:00:29', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (34, 49, 'player2', 'INT_ARRAY', '[4,1]', 1, '2025-08-16 20:00:29', '2025-08-16 20:00:29', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (35, 50, 'player1', 'INT_ARRAY', '[1,1,1,10,10,10,10]', 0, '2025-08-16 20:00:42', '2025-08-16 20:00:42', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (36, 50, 'player2', 'INT_ARRAY', '[10,10,10,10,1,1,1]', 1, '2025-08-16 20:00:42', '2025-08-16 20:00:42', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (37, 51, 'player1', 'INT_ARRAY', '[10,5,2]', 0, '2025-08-16 20:02:55', '2025-08-16 20:02:55', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (38, 51, 'player2', 'INT_ARRAY', '[9,10,6]', 1, '2025-08-16 20:02:55', '2025-08-16 20:02:55', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (39, 52, 'player1', 'INT_ARRAY', '[9,10,0]', 0, '2025-08-16 20:03:17', '2025-08-16 20:03:17', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (40, 52, 'player2', 'INT_ARRAY', '[10,0,6]', 1, '2025-08-16 20:03:17', '2025-08-16 20:03:17', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (41, 53, 'player1', 'INT_ARRAY', '[4,7,9,2,4,8,8,0,4,5,10,0,1,1,0,0,4,10,10,10,7,3,1,10,9,0,4,4,6,9,9,8,6,1,2,7,3,2,1,10,5,2,2,10,4,3,0,2,8,8,6,0,9,0,3,1,5,5,3,7,1,1,0,4,2,5,2,1,8,2,2,1,3,8,10,9,10,0,8,0,6,0,4,0,9,6,0,10,0,10,10,2,6,8,4,0,10,0,6,6,9,9,1,5,4,3,3,3,0,10,3,5,6,1,7,6,6,2,9,4,4,6,8,2,6,9,7,3,3,10,6,3,1,10,7,7,5,0,6,5,4,4,3,9,0,3,9,10,8,4,6,1,1,1,4,9,10,2,4,1,6,10,9,1,2,10,3,6,4,8,0,1,1,6,8,5,6,10,5,2,8,4,3,2,9,5,9,6,0,7,9,4,3,0,3,2,8,3,9,10,8,5,9,3,3,3,5,6,7,3,5,6,6,2,9,9,3,1,9,2,7,2,2,6,2,0,3,7,5,9,4,9,8,8,5,10,10,0,1,7,0,3,4,0,7,10,10,10,8,7,2,9,0,7,0,0,1,5,9,10,7,7,7,7,0,1,7,10,4,5,6,6,5,7,1,10,8,6,1,4,4,6,0,3,8,4,1,2,10,7,1,5,6,2,2,3,4,2,7,4,8,4,9,7,8,2,9,3,5,7,2,8,10,2,1,2,3,6,5,0,1,5,3,10,4,10,1,1,6,10,0,3,9,10,10,6,9,0,10,4,8,8,9,5,10,8,5,8,6,9,3,10,7,7,0,9,7,1,9,7,10,7,3,1,8,4,5,2,5,9,3,4,10,6,3,4,10,2,7,7,7,6,2,3,7,6,2,6,10,5,7,2,3,0,7,6,5,9,2,0,2,4,8,3,6,4,4,1,4,6,1,6,0,10,7,6,0,4,5,8,10,4,4,0,9,3,0,3,8,6,4,5,3,8,7,0,10,1,7,7,0,7,1,9,8,10,6,2,10,3,10,3,3,6,9,8,6,10,6,3,2,6,5,0,3,3,8,7,6,3,10,2,2,10,3,4,3,2,10,10,5,6,3,8,4,9,3,0,10,5,6,5,7,8,9,1,10,10,8,3,4,9,5,0,9,4,8,4,0,1,0,4,7,4,2,10,9,3,1,2,5,0,4,8,7,4,5,1,1,6,0,3,10,10,2,8,4,10,7,2,8,0,0,2,4,2,9,9,5,3,10,1,0,10,1,5,1,10,2,0,9,7,8,2,10,9,5,10,1,2,8,1,8,10,2,1,4,3,1,1,10,2,6,4,7,10,9,10,4,1,6,1,1,5,2,3,9,6,4,10,0,10,10,6,3,5,10,6,7,0,2,3,7,3,0,5,2,7,7,1,10,8,3,3,10,3,9,4,9,10,6,3,4,3,8,9,2,9,0,6,3,0,0,10,3,8,3,4,10,5,9,9,5,5,5,5,3,10,5,5,6,2,9,1,0,3,2,1,3,1,6,9,7,2,7,5,1,9,10,7,0,1,0,1,2,7,0,4,6,1,2,10,8,9,6,7,10,10,0,7,0,5,9,5,1,10,6,5,1,9,3,0,10,9,10,5,4,6,3,6,1,1,6,0,6,7,10,7,3,7,4,0,0,5,0,3,10,9,3,10,4,4,8,0,6,9,10,0,1,6,5,10,2,5,2,4,9,8,2,10,5,2,2,3,9,4,6,5,10,9,3,6,8,4,1,9,9,8,9,5,7,1,1,0,7,2,2,8,0,3,1,6,3,4,0,7,5,5,8,8,1,9,6,7,5,9,5,6,10,7,10,8,10,3,2,5,8,8,6,7,5,7,3,0,5,5,4,8,0,2,10,8,9,8,10,7,5,1,1,5,9,7,4,5,0,0,6,8,5,3,2,7,7,10,8,9,3,5,6,10,8,1,2,3,9,7,3,2,8,2,2,8,9,4,4,9,6,5,4,5,3,4,0,3,3,3,1,1,0,5,5,6,6,3,1,10,4,10,7,9,5,1,7,2,3,10,4,1,9,9,6,8,6,9,3,10,2,6,1,8,4,7,6,6,1,0,6,3,5,1,8,4,7,9,8,4,9,0,3,5,6,4,4,0,6,1,7,8,0,5,2,9,9,8,8,6,4,0,6,5,0,8,10,5,6,6,4,0,6,0,4,5,8,2,5,5,5,8,8,9,10,3,2,8,8,9,9,1,8,3,2,2,5,1,7,7,10,10,10,6]', 0, '2025-08-16 20:03:44', '2025-08-16 20:03:44', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (42, 53, 'player2', 'INT_ARRAY', '[1,1,10,4,6,10,3,7,9,1,4,7,3,0,1,3,10,6,5,6,4,5,3,3,8,5,3,5,10,8,3,8,7,6,5,5,10,6,4,5,7,6,3,7,9,4,1,7,2,6,1,10,8,0,4,8,8,7,4,9,10,0,9,3,0,7,4,6,0,3,2,6,4,2,4,10,9,9,5,5,0,4,8,1,3,8,1,4,4,4,3,4,0,5,9,8,5,9,6,4,2,7,5,1,10,4,3,6,9,4,1,3,0,2,9,8,1,3,2,1,8,5,6,5,0,0,7,1,7,7,8,2,4,0,6,4,2,3,4,6,1,6,9,1,6,8,6,2,9,9,4,10,9,0,3,10,3,2,1,9,1,1,1,2,7,1,8,8,3,10,1,7,10,1,4,2,3,0,10,6,9,9,2,4,1,5,1,0,10,2,6,7,2,6,3,0,4,4,2,9,3,1,4,7,8,5,1,0,4,3,4,1,0,3,7,0,7,7,3,3,10,9,6,7,3,3,2,4,9,6,9,0,10,6,10,8,10,8,1,0,9,1,9,3,7,7,0,0,2,2,6,8,6,6,9,4,6,7,4,9,8,10,2,8,4,5,0,10,8,4,10,7,6,3,10,5,9,9,0,2,10,0,9,4,7,7,3,1,10,10,3,0,4,3,8,6,10,0,7,6,8,9,0,1,9,2,2,4,0,2,2,9,6,10,10,8,5,10,6,10,8,5,3,9,10,8,1,9,10,6,8,6,6,5,3,0,8,0,1,5,9,3,8,4,3,3,7,6,0,4,9,9,1,7,1,5,5,0,3,6,2,8,5,4,6,0,4,0,10,5,2,1,1,4,2,1,8,9,9,8,3,4,0,5,4,6,9,9,4,9,4,1,8,3,9,7,6,8,8,2,9,6,9,0,2,4,9,8,2,1,3,4,4,3,1,4,4,0,4,4,9,3,0,4,9,5,4,0,6,4,3,3,1,0,1,0,1,5,6,2,0,7,0,6,1,10,9,9,7,4,1,7,8,5,7,0,8,9,8,8,4,2,6,5,1,0,6,9,1,6,3,3,9,9,1,0,5,2,6,6,2,3,10,7,5,9,5,5,0,9,0,9,4,10,8,8,7,0,9,3,9,10,0,1,4,7,3,1,4,5,1,7,2,0,2,3,7,7,8,8,9,0,10,0,4,8,0,4,6,5,7,3,8,5,7,0,9,10,9,5,4,6,10,0,9,10,2,4,8,8,5,8,9,0,5,7,3,9,5,0,8,4,1,3,3,1,1,5,6,7,0,8,10,6,2,4,6,4,6,9,2,4,4,10,8,10,4,7,9,7,3,4,3,6,8,2,2,10,3,4,3,9,9,0,8,6,9,8,9,10,0,6,7,3,10,10,7,5,8,1,4,3,6,6,0,3,1,7,3,2,1,10,7,2,9,6,2,7,5,1,5,8,5,10,6,10,4,6,8,0,9,5,7,9,6,4,1,0,9,5,9,4,0,2,6,9,6,6,9,3,1,1,3,4,3,1,5,8,6,6,6,6,5,2,10,10,7,4,8,9,6,2,9,6,7,10,2,4,4,10,8,8,1,5,4,1,0,9,6,10,6,10,5,7,5,6,10,10,3,5,9,1,7,2,7,3,8,2,6,9,6,3,4,2,0,8,7,5,0,7,7,9,5,9,8,9,4,1,5,3,1,1,7,10,5,1,7,9,6,8,7,3,2,3,2,3,5,8,10,4,6,5,4,3,4,1,2,8,5,3,1,3,2,0,0,5,4,0,0,2,4,5,6,1,10,3,1,8,0,4,7,2,2,8,8,8,8,8,3,5,3,4,8,7,0,2,4,1,3,7,7,8,9,5,4,8,0,8,5,7,6,9,10,6,8,5,2,10,4,0,7,1,7,0,3,10,7,7,10,9,6,1,6,6,9,9,6,7,10,7,2,7,2,4,3,8,5,0,3,10,10,5,8,8,10,10,10,4,5,3,5,1,10,7,1,5,6,4,3,9,0,5,4,0,5,6,0,0,2,7,1,5,1,10,6,5,6,1,1,8,7,7,2,0,7,5,5,2,2,0,4,2,10,7,4,6,8,1,3,6,3,9,1,1,7,4,8,0,3,4,8,0,2,10,3,6,2,6,2,10,1,6,4,9,7,1,9,7,5,3,7,8,7,9,4,7,0,0,9,4,2,0,10,7,10,3,8,7,9,4,5,3,7,6,5,4,7,8,8,9,10,2,10,9,4,3,0,5,8,4]', 1, '2025-08-16 20:03:44', '2025-08-16 20:03:44', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (43, 54, 'player1', 'INT_ARRAY', '[7,6,6,3,9,7,5,9,5,9,1,0,0,4,3,1,2]', 0, '2025-08-16 20:04:06', '2025-08-16 20:04:06', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (44, 54, 'player2', 'INT_ARRAY', '[5,0,7,10,4,1,4,2,4,0,1,5,0,10,9,0,4]', 1, '2025-08-16 20:04:06', '2025-08-16 20:04:06', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (45, 55, 'player1', 'INT_ARRAY', '[2,7,4]', 0, '2025-08-16 20:04:23', '2025-08-16 20:04:23', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (46, 55, 'player2', 'INT_ARRAY', '[9,1,10]', 1, '2025-08-16 20:04:23', '2025-08-16 20:04:23', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (47, 56, 'player1', 'INT_ARRAY', '[3,10,9,9]', 0, '2025-08-16 20:04:46', '2025-08-16 20:04:46', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (48, 56, 'player2', 'INT_ARRAY', '[8,10,2,10]', 1, '2025-08-16 20:04:46', '2025-08-16 20:04:46', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (49, 57, 'player1', 'INT_ARRAY', '[6,10,4]', 0, '2025-08-16 20:05:03', '2025-08-16 20:05:03', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (50, 57, 'player2', 'INT_ARRAY', '[5,9,2]', 1, '2025-08-16 20:05:03', '2025-08-16 20:05:03', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (51, 58, 'S', 'STRING', '\"cbabc\"', 0, '2025-08-16 20:23:17', '2025-08-16 20:23:17', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (52, 59, 'S', 'STRING', '\"aa\"', 0, '2025-08-16 20:24:19', '2025-08-16 20:24:19', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (53, 60, 'S', 'STRING', '\"acbbc\"', 0, '2025-08-16 20:24:36', '2025-08-16 20:24:36', NULL, NULL);
INSERT INTO `test_case_inputs` (`id`, `test_case_output_id`, `test_case_name`, `input_type`, `input_content`, `order_index`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (54, 61, 'S', 'STRING', '\"leetcode\"', 0, '2025-08-16 20:25:03', '2025-08-16 20:25:03', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for test_cases_outputs
-- ----------------------------
DROP TABLE IF EXISTS `test_cases_outputs`;
CREATE TABLE `test_cases_outputs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试用例ID，主键，自动增长',
  `problem_id` bigint NOT NULL COMMENT '关联的题目ID，外键，关联到problems表',
  `output` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '期望的输出',
  `output_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '输出类型',
  `score` int DEFAULT '10' COMMENT '该测试点的分值，默认为10',
  `is_sample` tinyint(1) DEFAULT '0' COMMENT '是否为样例测试用例，默认为FALSE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `problem_id` (`problem_id`),
  CONSTRAINT `test_cases_outputs_ibfk_1` FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of test_cases_outputs
-- ----------------------------
BEGIN;
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (39, 278, '\"bb\"', 'STRING', 10, 1, NULL, '2025-08-13 15:40:56', '2025-08-13 15:40:56', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (40, 278, '\"bab\"', 'STRING', 10, 1, '2025-08-12 15:03:40', '2025-08-13 15:40:56', '2025-08-16 16:03:40', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (43, 278, '\"aca\"', 'STRING', 10, 0, '2025-08-16 16:27:46', '2025-08-16 16:27:46', '2025-08-16 16:27:46', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (45, 278, '\"aca\"', 'STRING', 10, 0, '2025-08-16 16:28:18', '2025-08-16 16:28:18', '2025-08-16 16:28:18', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (46, 278, '\"tattarrattat\"', 'STRING', 10, 0, '2025-08-16 18:33:08', '2025-08-16 18:33:08', '2025-08-16 18:33:08', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (47, 279, '1', 'INT', 10, 1, '2025-08-16 19:59:59', '2025-08-16 19:59:59', '2025-08-16 20:00:55', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (48, 279, '2', 'INT', 10, 1, '2025-08-16 20:00:15', '2025-08-16 20:00:15', '2025-08-16 20:00:58', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (49, 279, '0', 'INT', 10, 1, '2025-08-16 20:00:29', '2025-08-16 20:00:29', '2025-08-16 20:01:02', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (50, 279, '2', 'INT', 10, 1, '2025-08-16 20:00:42', '2025-08-16 20:00:42', '2025-08-16 20:01:05', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (51, 279, '2', 'INT', 10, 0, '2025-08-16 20:02:55', '2025-08-16 20:02:55', '2025-08-16 20:02:55', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (52, 279, '2', 'INT', 10, 0, '2025-08-16 20:03:17', '2025-08-16 20:03:17', '2025-08-16 20:03:17', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (53, 279, '1', 'INT', 10, 0, '2025-08-16 20:03:44', '2025-08-16 20:03:44', '2025-08-16 20:03:44', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (54, 279, '2', 'INT', 10, 0, '2025-08-16 20:04:06', '2025-08-16 20:04:06', '2025-08-16 20:04:06', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (55, 279, '2', 'INT', 10, 0, '2025-08-16 20:04:23', '2025-08-16 20:04:23', '2025-08-16 20:04:23', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (56, 279, '1', 'INT', 10, 0, '2025-08-16 20:04:46', '2025-08-16 20:04:46', '2025-08-16 20:04:46', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (57, 279, '1', 'INT', 10, 0, '2025-08-16 20:05:03', '2025-08-16 20:05:03', '2025-08-16 20:05:03', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (58, 280, '\"baabc\"', 'STRING', 10, 1, '2025-08-16 20:23:17', '2025-08-16 20:23:17', '2025-08-16 20:24:24', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (59, 280, '\"az\"', 'STRING', 10, 1, '2025-08-16 20:24:19', '2025-08-16 20:24:19', '2025-08-16 20:24:19', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (60, 280, '\"abaab\"', 'STRING', 10, 1, '2025-08-16 20:24:36', '2025-08-16 20:24:36', '2025-08-16 20:24:39', NULL, NULL);
INSERT INTO `test_cases_outputs` (`id`, `problem_id`, `output`, `output_type`, `score`, `is_sample`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (61, 280, '\"kddsbncd\"', 'STRING', 10, 1, '2025-08-16 20:25:03', '2025-08-16 20:25:03', '2025-08-16 20:25:06', NULL, NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=1956628531243175939 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of token
-- ----------------------------
BEGIN;
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1949672146643267586, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1MzY3MzAzNywiZXhwIjoxNzUzNzU5NDM3fQ.FZAMifVSL8iXuKGKHdFjN_ZwoJT92CtZWoY8iLKlw80', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1949821332223180802, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1MzcwODYwNiwiZXhwIjoxNzUzNzk1MDA2fQ.QXs_IqHbpx3tnM_GWCFTEJ66UO2ZxBoeXXjEV78NTzQ', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1950078105987358722, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzc2OTgyNSwiZXhwIjoxNzUzODU2MjI1fQ.OV4lm_y08ZuwsC0Kqdi4ReCo4fuwrHm-cDzdxcbmyMI', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1950089039300546562, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzc3MjQzMiwiZXhwIjoxNzUzODU4ODMyfQ.NhnECn9H567ZYoOH_ItmsPQLgbOK21ICdxanBQs7SFQ', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1950184660170756098, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzc5NTIzMCwiZXhwIjoxNzUzODgxNjMwfQ.lo4JbDoH6jh0l1u1jw_66bh3waHeR_m223lxQUFMWLU', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1950379196176154625, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzg0MTYxMSwiZXhwIjoxNzUzOTI4MDExfQ.52906g-OkAwv4zs2-ucSWtjF2FGC48UHgTPBUCkpTIo', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1950453344705478658, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzg1OTI4OSwiZXhwIjoxNzUzOTQ1Njg5fQ.H5QIhsFOUJ253smwWy3WP4fAmo_xqB__Hsqt5YJRbh8', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1950549107661946881, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1Mzg4MjEyMSwiZXhwIjoxNzUzOTY4NTIxfQ.BX93CyKDzlX2EbELjerFZcru8uXF8D5DWs4ikA6TdR0', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1950784406790893569, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1MzkzODIyMSwiZXhwIjoxNzU0MDI0NjIxfQ.K3yBfxZVcObJhkerSlP4eqRHgpVTWOOwy0Hs0Bil-ME', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1954476403015872513, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgxODQ2MSwiZXhwIjoxNzU0OTA0ODYxfQ.PDqMM6yAnT7lOmBGmvoMFzKv8gCq4zc2EkU02uPHYU8', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1954478844348264449, 2, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZEhMUCIsImlhdCI6MTc1NDgxOTA0MywiZXhwIjoxNzU0OTA1NDQzfQ.JNzGyw2c4zzJTvGtHLr1ULfcKHPQvznbrTOyTfGRRmA', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1954495667114405889, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgyMzA1NCwiZXhwIjoxNzU0OTA5NDU0fQ.FzcRG3YdZuEhImVSe4tgSSzrnG2oNWbMxLBUDUGF7qs', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1954495701910351874, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgyMzA2MiwiZXhwIjoxNzU0OTA5NDYyfQ.qNbx0TGpHdJEqxsM4pmX8oFBGvwFq3bePKwIeQWKshc', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1954495738316910594, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgyMzA3MSwiZXhwIjoxNzU0OTA5NDcxfQ.69rtCD-MHtKG1gqMSI5JTQxjzLs8uCITUQjeQffviEg', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1954495886057029634, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgyMzEwNiwiZXhwIjoxNzU0OTA5NTA2fQ.pB3L8qplAVqdxVS6xsSCRZ-GuQQvfv083sA1oBHM2_E', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1954497512851390466, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDgyMzQ5NCwiZXhwIjoxNzU0OTA5ODk0fQ.kofEpDAQealhyfxNZScXJ-raseKTsiBI1WDhAqXU8Vg', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1954567456779739137, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDg0MDE3MCwiZXhwIjoxNzU0OTI2NTcwfQ.kBOq6-NzPeoq5v7TYoNYh0hvMWd-qNt2XOgujg-yYko', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955096741126930433, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2NjM2MSwiZXhwIjoxNzU1MDUyNzYxfQ.iL9eklmybjYr0GhtREB28Y4N20la-9pVKymra-fNicE', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955097221261492226, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2NjQ3NiwiZXhwIjoxNzU1MDUyODc2fQ.q44u5lv2aoOI2EVBL8-CvRv8uNO2PLS22LC_mxaTgiE', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955097803753848833, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2NjYxNCwiZXhwIjoxNzU1MDUzMDE0fQ.XsK4aEYtMYtufyLkUTNT5N0-HXr4LsX47WNFp8CQEmw', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955097935006203905, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2NjY0NiwiZXhwIjoxNzU1MDUzMDQ2fQ.Gd9wFQY-DAZh8gPWQxf4jRh2WtOZc5ALI9QvFHsiaHo', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955098025213100034, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2NjY2NywiZXhwIjoxNzU1MDUzMDY3fQ.HuvUkIFE-Az143t_K8Axx-EMdow_utmVGGf6Pfx3mB0', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955098775989321730, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk2Njg0NiwiZXhwIjoxNzU1MDUzMjQ2fQ.k0ZtMBVImeP_WleOE55S5edBXEQ-3s_ABWIQX9Byrxc', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955098911524057090, 2, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZEhMUCIsImlhdCI6MTc1NDk2Njg3OSwiZXhwIjoxNzU1MDUzMjc5fQ.2tU31skR51OsmldtlVVSKYXoyMLtHqnB2tWHzlR9_PI', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955116929373179905, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk3MTE3NCwiZXhwIjoxNzU1MDU3NTc0fQ.KnZScyB-N7gPsvwYMpsGqKX9Ob3BpBqWzdY2rFKbczQ', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955145369107333122, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk3Nzk1NSwiZXhwIjoxNzU1MDY0MzU1fQ.XlVJJ8B_8UIh5tSVAazoBU1FHTKrXtiYQ5zQCYsZEjI', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955146796525457409, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk3ODI5NSwiZXhwIjoxNzU1MDY0Njk1fQ.bSv6omOX9XGf4DEb1sjODVtcPI9lUTQyw_nJosgCDXg', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955151001885659138, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk3OTI5OCwiZXhwIjoxNzU1MDY1Njk4fQ.uGrcUBWcS7s_DP1Uu56mGklt8OWy_psJ-Dw8sJQI6_A', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955168579152564225, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk4MzQ4OSwiZXhwIjoxNzU1MDY5ODg5fQ.icX8bWeXLUkv2oqwGsg846SJ8-k1BMxMFvM_OfgKzoY', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955170039403683841, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk4MzgzNywiZXhwIjoxNzU1MDcwMjM3fQ.ngs24p4kQOvoVKWPXPsBEcyl-6qd_Q_LQdRvF3WMoM0', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955183033911140354, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NDk4NjkzNSwiZXhwIjoxNzU1MDczMzM1fQ.Kt228huASH4YB8JiOlDbu-juVfXpG15p-nG8fcBCXy0', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955462113214656513, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NTA1MzQ3MywiZXhwIjoxNzU1MTM5ODczfQ.tErwGdKx3BUVCXFmLoQMuZtqPq9xQD71sQHw0wN3tfc', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955492480759951362, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NTA2MDcxMywiZXhwIjoxNzU1MTQ3MTEzfQ.TB4UmSE0Bmz1FwuF0Ycho4LvgLTbrH0R8RzLmMX5NOA', 'ACCESS', 0, 0, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955572156199096322, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NTA3OTcwOSwiZXhwIjoxNzU1MTY2MTA5fQ.PwX2-UTvyA8ps_Wag-CdssYIN80pZW77xv1Zli_HrlY', 'ACCESS', 0, 0, '2025-08-13 18:08:29', '2025-08-13 18:08:29', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1955974267751698433, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NTE3NTU4MCwiZXhwIjoxNzU1MjYxOTgwfQ.IdO95tt_VW2xoXA1hg2tWSQO-p2SCU80LQJBEigqxjo', 'ACCESS', 0, 0, '2025-08-14 20:46:20', '2025-08-14 20:46:20', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1956251809511403521, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NTI0MTc1MSwiZXhwIjoxNzU1MzI4MTUxfQ.DtpLanWXv1_rP7s5J-pCAXdkEnXb8hellTQF7dpl-LU', 'ACCESS', 0, 0, '2025-08-15 15:09:11', '2025-08-15 15:09:11', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1956616647106400258, 1, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZCIsImlhdCI6MTc1NTMyODczNSwiZXhwIjoxNzU1NDE1MTM1fQ.ND9KUJtsZGmASr2g8oiDG1cK9bFak8IrBVwSE4I0XWU', 'ACCESS', 0, 0, '2025-08-16 15:18:55', '2025-08-16 15:18:55', NULL, NULL);
INSERT INTO `token` (`id`, `user_id`, `token`, `token_type`, `expired`, `revoked`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1956628531243175938, 2, 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJEYXZpZEhMUCIsImlhdCI6MTc1NTMzMTU2OCwiZXhwIjoxNzU1NDE3OTY4fQ.oNt2VhoEKcssPqiBJu8lT2qcvPvDqMUXyyk5H5IRdeU', 'ACCESS', 0, 0, '2025-08-16 16:06:08', '2025-08-16 16:06:08', NULL, NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1, 'David', 'lysf15520112973@163.com', '$2a$10$G3KNWvjNkRKrExgQsA6ppOD2qkYW.RiCN9HhGYiVMdchwPSoPxUwG', NULL, '用户未填写', '用户未填写', 1, NULL, NULL, '2025-07-12 00:00:00', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `user` (`user_id`, `username`, `email`, `password`, `avatar`, `introduction`, `address`, `status`, `last_login_ip`, `last_login`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (2, 'DavidHLP', '1372998589@qq.com', '$2a$10$v.rEgO20nH7DHW0nOnDPTOkxIdL4D44arGzBrHhWmamKPSSGuTQ2G', NULL, '用户未填写', '用户未填写', 1, NULL, NULL, '2025-07-26 16:32:50', '2025-08-13 15:40:57', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for user_content_views
-- ----------------------------
DROP TABLE IF EXISTS `user_content_views`;
CREATE TABLE `user_content_views` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浏览记录ID，主键',
  `user_id` bigint NOT NULL COMMENT '浏览用户的ID，关联到user.user_id',
  `content_id` bigint NOT NULL COMMENT '被浏览内容的ID (可能是题目ID或题解ID)',
  `content_type` enum('SOLUTION') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次浏览时间',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_at` bigint DEFAULT NULL,
  `update_at` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_content_view` (`user_id`,`content_id`),
  CONSTRAINT `fk_view_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户对内容（题目/题解）的独立浏览记录表';

-- ----------------------------
-- Records of user_content_views
-- ----------------------------
BEGIN;
INSERT INTO `user_content_views` (`id`, `user_id`, `content_id`, `content_type`, `created_at`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (9, 1, 26, 'SOLUTION', '2025-08-17 14:10:48', '2025-08-17 14:10:48', '2025-08-17 14:10:48', NULL, NULL);
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
INSERT INTO `user_role` (`user_id`, `role_id`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1, 1, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `user_role` (`user_id`, `role_id`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1, 2, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `user_role` (`user_id`, `role_id`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (1, 3, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
INSERT INTO `user_role` (`user_id`, `role_id`, `create_time`, `update_time`, `create_at`, `update_at`) VALUES (2, 3, '2025-08-13 15:40:57', '2025-08-13 15:40:57', NULL, NULL);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
