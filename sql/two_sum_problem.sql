-- 插入"两数之和"题目数据
INSERT INTO `problems` (
    `title`, 
    `description`, 
    `input_format`, 
    `output_format`, 
    `sample_input`, 
    `sample_output`, 
    `hint`, 
    `time_limit`, 
    `memory_limit`, 
    `difficulty`, 
    `tags`, 
    `solved_count`, 
    `submission_count`, 
    `created_by`, 
    `is_visible`
) VALUES (
    '两数之和',
    '给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出和为目标值 target 的那两个整数，并返回它们的数组下标。

你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。

你可以按任意顺序返回答案。

**示例 1：**
输入：nums = [2,7,11,15], target = 9
输出：[0,1]
解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。

**示例 2：**
输入：nums = [3,2,4], target = 6
输出：[1,2]

**示例 3：**
输入：nums = [3,3], target = 6
输出：[0,1]

**约束条件：**
- 2 <= nums.length <= 10^4
- -10^9 <= nums[i] <= 10^9
- -10^9 <= target <= 10^9
- 只会存在一个有效答案',
    
    '第一行包含一个整数 n，表示数组的长度。
第二行包含 n 个整数，表示数组 nums 的元素。
第三行包含一个整数 target，表示目标值。',
    
    '输出两个整数，表示两个数的下标（从0开始），用空格分隔。如果有多个答案，输出任意一个即可。',
    
    '4
2 7 11 15
9',
    
    '0 1',
    
    '可以使用哈希表来优化时间复杂度。遍历数组时，对于每个元素，检查 target - 当前元素 是否已经在哈希表中。',
    
    1000,  -- 时间限制1秒
    128,   -- 内存限制128MB
    'Easy', -- 简单难度
    '["数组", "哈希表"]', -- 标签
    0,     -- 初始通过数为0
    0,     -- 初始提交数为0
    1,     -- 创建者ID（假设为1）
    1      -- 可见
);

-- 获取刚插入的题目ID（假设为1，实际应该使用LAST_INSERT_ID()）
SET @problem_id = LAST_INSERT_ID();

-- 插入测试用例
-- 测试用例1：示例1
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES 
(@problem_id, '4\n2 7 11 15\n9', '0 1', 10, 1);

-- 测试用例2：示例2
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES 
(@problem_id, '3\n3 2 4\n6', '1 2', 10, 1);

-- 测试用例3：示例3
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES 
(@problem_id, '2\n3 3\n6', '0 1', 10, 1);

-- 测试用例4：边界情况 - 最小数组
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES 
(@problem_id, '2\n1 2\n3', '0 1', 10, 0);

-- 测试用例5：负数情况
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES 
(@problem_id, '4\n-1 -2 -3 -4\n-6', '2 3', 10, 0);

-- 测试用例6：包含0的情况
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES 
(@problem_id, '3\n0 4 3\n0', '0 1', 10, 0);

-- 测试用例7：较大数组
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES 
(@problem_id, '6\n1 3 5 7 9 11\n16', '3 4', 10, 0);

-- 测试用例8：目标值为0
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES 
(@problem_id, '4\n-3 4 3 90\n0', '0 2', 10, 0);

-- 测试用例9：相同元素
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES 
(@problem_id, '4\n5 5 5 5\n10', '0 1', 10, 0);

-- 测试用例10：大数值
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES 
(@problem_id, '3\n1000000000 -1000000000 0\n0', '0 1', 10, 0);
