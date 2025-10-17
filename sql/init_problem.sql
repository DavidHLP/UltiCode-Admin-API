SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 基础词表 & 系统用户
-- =========================
INSERT INTO difficulties (id, code, sort_key) VALUES
                                                  (1, 'easy',   1),
                                                  (2, 'medium', 2),
                                                  (3, 'hard',   3)
    ON DUPLICATE KEY UPDATE code=VALUES(code), sort_key=VALUES(sort_key);

INSERT INTO categories (id, code, name) VALUES
                                            (1, 'algorithms', 'Algorithms'),
                                            (2, 'data-structures', 'Data Structures')
    ON DUPLICATE KEY UPDATE code=VALUES(code), name=VALUES(name);

-- 标签：确保常见 LC 标签存在
INSERT INTO tags (id, slug, name) VALUES
                                      (3,  'string',      'String'),
                                      (10, 'array',       'Array'),
                                      (11, 'hash-table',  'Hash Table')
    ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 语言：提供 C++/Python/Java（与模板对应）
INSERT INTO languages (id, code, display_name, runtime_image, is_active) VALUES
                                                                             (1, 'cpp17',      'C++17',      NULL, 1),
                                                                             (2, 'python3.11', 'Python 3.11',NULL, 1),
                                                                             (3, 'java17',     'Java 17',    NULL, 1)
    ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), is_active=VALUES(is_active);

-- 管理员（题目创建者）
INSERT INTO users (id, username, email, password_hash, status)
VALUES (1, 'admin', 'admin@example.com', '$2y$dummyhash', 1)
    ON DUPLICATE KEY UPDATE email=VALUES(email);

-- =========================
-- Problem 1: 1-two-sum  (LeetCode #1)
-- =========================
INSERT INTO problems
(id, slug, problem_type, difficulty_id, category_id, creator_id,
 solution_entry, time_limit_ms, memory_limit_kb, is_public, meta_json)
VALUES
    (1, '1-two-sum', 'coding', 1, 1, 1,
     'twoSum', 1000, 262144, 1,
     JSON_OBJECT(
             'leetcode_style', TRUE,
             'frontend_id', 1,
             'paid_only', FALSE,
             'companies', JSON_ARRAY('Google','Amazon','Facebook'),
             'frequency', 0.53
     ))
    ON DUPLICATE KEY UPDATE
                         slug=VALUES(slug),
                         solution_entry=VALUES(solution_entry),
                         meta_json=VALUES(meta_json);

-- 题面（i18n）
INSERT INTO problem_statements
(id, problem_id, lang_code, title, description_md, constraints_md, examples_md)
VALUES
    (101, 1, 'en', '1. Two Sum',
     'Given an integer array **nums** and an integer **target**, return the indices of the two numbers such that they add up to **target**.',
     '- 2 ≤ |nums| ≤ 10^5\n- -10^9 ≤ nums[i], target ≤ 10^9\n- Exactly one solution exists.',
     '**Example**\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]'),
    (102, 1, 'zh-CN', '1. 两数之和',
     '给定整数数组 **nums** 与整数 **target**，请返回使得 `nums[i] + nums[j] = target` 的两个下标（i ≠ j）。',
     '- 2 ≤ |nums| ≤ 10^5\n- -10^9 ≤ nums[i], target ≤ 10^9\n- 题目保证恰好存在一个解。',
     '**示例**\n输入：nums = [2,7,11,15], target = 9\n输出：[0,1]')
    ON DUPLICATE KEY UPDATE
                         title=VALUES(title),
                         description_md=VALUES(description_md),
                         constraints_md=VALUES(constraints_md),
                         examples_md=VALUES(examples_md);

-- 标签
INSERT INTO problem_tags (problem_id, tag_id) VALUES
                                                  (1, 10),  -- array
                                                  (1, 11)   -- hash-table
    ON DUPLICATE KEY UPDATE tag_id=VALUES(tag_id);

-- 数据集（v1 激活）
INSERT INTO datasets
(id, problem_id, name, is_active, checker_type, float_abs_tol, float_rel_tol, created_by)
VALUES
    (1, 1, 'v1', 1, 'text', NULL, NULL, 1)
    ON DUPLICATE KEY UPDATE
                         is_active=VALUES(is_active),
                         checker_type=VALUES(checker_type);

-- 样例/隐藏分组
INSERT INTO testcase_groups (id, dataset_id, name, is_sample, weight) VALUES
                                                                          (1, 1, 'samples', 1, 1),
                                                                          (2, 1, 'hidden',  0, 1)
    ON DUPLICATE KEY UPDATE
                         name=VALUES(name),
                         is_sample=VALUES(is_sample),
                         weight=VALUES(weight);

-- 用例（JSON 入参 + 期望 JSON 输出）
INSERT INTO testcases
(id, group_id, order_index, input_file_id, output_file_id, input_json, output_json, output_type, score)
VALUES
    -- 样例
    (1, 1, 1, NULL, NULL,
     JSON_OBJECT('nums', JSON_ARRAY(2,7,11,15), 'target', 9),
     JSON_ARRAY(0,1), 'json', 10),
    (2, 1, 2, NULL, NULL,
     JSON_OBJECT('nums', JSON_ARRAY(3,2,4), 'target', 6),
     JSON_ARRAY(1,2), 'json', 10),
    -- 隐藏
    (3, 2, 1, NULL, NULL,
     JSON_OBJECT('nums', JSON_ARRAY(-1,-2,-3,-4,-5), 'target', -8),
     JSON_ARRAY(2,4), 'json', 10),
    (4, 2, 2, NULL, NULL,
     JSON_OBJECT('nums', JSON_ARRAY(1,2,3,4,5,6,7,8,9,10), 'target', 19),
     JSON_ARRAY(8,9), 'json', 10)
    ON DUPLICATE KEY UPDATE
                         input_json=VALUES(input_json),
                         output_json=VALUES(output_json),
                         output_type=VALUES(output_type),
                         score=VALUES(score);

-- 语言模板（LeetCode 样式：仅类/方法）
INSERT INTO problem_language_configs
(id, problem_id, language_id, function_name, starter_code)
VALUES
    (1101, 1, 1, 'twoSum',
     '// LeetCode-style C++17 (method only)
     #include <bits/stdc++.h>
     using namespace std;
     class Solution {
     public:
         vector<int> twoSum(vector<int>& nums, int target) {
             unordered_map<int,int> mp;
             for (int i = 0; i < (int)nums.size(); ++i) {
                 int need = target - nums[i];
                 if (mp.count(need)) return {mp[need], i};
                 mp[nums[i]] = i;
             }
             return {};
         }
     };'),
    (1102, 1, 2, 'twoSum',
     '# LeetCode-style Python 3.11 (method only)
     from typing import List

     class Solution:
         def twoSum(self, nums: List[int], target: int) -> List[int]:
             mp = {}
             for i, x in enumerate(nums):
                 if target - x in mp:
                     return [mp[target - x], i]
                 mp[x] = i
             return []
     '),
    (1103, 1, 3, 'twoSum',
     '// LeetCode-style Java 17 (method only)
     import java.util.*;
     class Solution {
         public int[] twoSum(int[] nums, int target) {
             Map<Integer, Integer> mp = new HashMap<>();
             for (int i = 0; i < nums.length; i++) {
                 int need = target - nums[i];
                 if (mp.containsKey(need)) return new int[]{mp.get(need), i};
                 mp.put(nums[i], i);
             }
             return new int[0];
         }
     }')
    ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), starter_code=VALUES(starter_code);

-- 回填激活数据集
UPDATE problems
SET active_dataset_id = (SELECT id FROM datasets WHERE problem_id=1 AND is_active=1 LIMIT 1)
WHERE id = 1;

-- =========================
-- Problem 2: 344-reverse-string (LeetCode #344)
-- =========================
INSERT INTO problems
(id, slug, problem_type, difficulty_id, category_id, creator_id,
 solution_entry, time_limit_ms, memory_limit_kb, is_public, meta_json)
VALUES
    (2, '344-reverse-string', 'coding', 1, 1, 1,
     'reverseString', 1000, 262144, 1,
     JSON_OBJECT(
             'leetcode_style', TRUE,
             'frontend_id', 344,
             'paid_only', FALSE,
             'companies', JSON_ARRAY('Microsoft','Amazon'),
             'frequency', 0.27
     ))
    ON DUPLICATE KEY UPDATE
                         slug=VALUES(slug),
                         solution_entry=VALUES(solution_entry),
                         meta_json=VALUES(meta_json);

INSERT INTO problem_statements
(id, problem_id, lang_code, title, description_md, constraints_md, examples_md)
VALUES
    (201, 2, 'en', '344. Reverse String',
     'Write a function that returns the reversed string of **s**.

  > Note: The original LeetCode version modifies `char[]/List[str]` in-place. Here we use a return-value variant for judging.',
     '- 1 ≤ |s| ≤ 10^5',
     '**Example**\nInput: s = "hello"\nOutput: "olleh"'),
    (202, 2, 'zh-CN', '344. 反转字符串',
     '编写函数返回字符串 **s** 的反转结果。
  > 注：LeetCode 原题为原地修改字符数组，这里改为返回值版本以便判题。',
     '- 1 ≤ |s| ≤ 10^5',
     '**示例**\n输入：s = "hello"\n输出："olleh"')
    ON DUPLICATE KEY UPDATE
                         title=VALUES(title),
                         description_md=VALUES(description_md),
                         constraints_md=VALUES(constraints_md),
                         examples_md=VALUES(examples_md);

-- 标签
INSERT INTO problem_tags (problem_id, tag_id) VALUES
    (2, 3)  -- string
    ON DUPLICATE KEY UPDATE tag_id=VALUES(tag_id);

-- 数据集（v1 激活）
INSERT INTO datasets
(id, problem_id, name, is_active, checker_type, created_by)
VALUES
    (2, 2, 'v1', 1, 'text', 1)
    ON DUPLICATE KEY UPDATE
                         is_active=VALUES(is_active),
                         checker_type=VALUES(checker_type);

-- 样例/隐藏分组
INSERT INTO testcase_groups (id, dataset_id, name, is_sample, weight) VALUES
                                                                          (3, 2, 'samples', 1, 1),
                                                                          (4, 2, 'hidden',  0, 1)
    ON DUPLICATE KEY UPDATE
                         name=VALUES(name),
                         is_sample=VALUES(is_sample),
                         weight=VALUES(weight);

-- 用例（JSON 入参 + 期望 JSON 输出；输出为 JSON 字符串）
INSERT INTO testcases
(id, group_id, order_index, input_json, output_json, output_type, score)
VALUES
    (5, 3, 1, JSON_OBJECT('s','hello'), JSON_QUOTE('olleh'), 'json', 10),
    (6, 3, 2, JSON_OBJECT('s','ab'),    JSON_QUOTE('ba'),    'json', 10),
    (7, 4, 1, JSON_OBJECT('s','racecar'), JSON_QUOTE('racecar'), 'json', 10),
    (8, 4, 2, JSON_OBJECT('s','OpenAI GPT'), JSON_QUOTE('TPG IAnepO'), 'json', 10)
    ON DUPLICATE KEY UPDATE
                         input_json=VALUES(input_json),
                         output_json=VALUES(output_json),
                         output_type=VALUES(output_type),
                         score=VALUES(score);

-- 语言模板（LeetCode 样式：仅类/方法）
INSERT INTO problem_language_configs
(id, problem_id, language_id, function_name, starter_code)
VALUES
    (2101, 2, 1, 'reverseString',
     '// LeetCode-style C++17 (method only, returns string)
     // For in-place version: void reverseString(vector<char>& s)
     #include <bits/stdc++.h>
     using namespace std;
     class Solution {
     public:
         string reverseString(string s) {
             reverse(s.begin(), s.end());
             return s;
         }
     };'),
    (2102, 2, 2, 'reverseString',
     '# LeetCode-style Python 3.11 (method only, returns string)
     class Solution:
         def reverseString(self, s: str) -> str:
             return s[::-1]

     # In-place version:
     # from typing import List
     # class Solution:
     #     def reverseString(self, s: List[str]) -> None:
     #         s.reverse()
     '),
    (2103, 2, 3, 'reverseString',
     '// LeetCode-style Java 17 (method only, returns string)
     // In-place version: void reverseString(char[] s)
     class Solution {
         public String reverseString(String s) {
             return new StringBuilder(s).reverse().toString();
         }
     }')
    ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), starter_code=VALUES(starter_code);

-- 回填激活数据集
UPDATE problems
SET active_dataset_id = (SELECT id FROM datasets WHERE problem_id=2 AND is_active=1 LIMIT 1)
WHERE id = 2;

-- 统一把样例组命名为 samples（幂等）
UPDATE testcase_groups
SET name = 'samples'
WHERE is_sample = 1
  AND dataset_id IN (SELECT id FROM datasets WHERE problem_id IN (1,2));

SET FOREIGN_KEY_CHECKS = 1;
