-- 两数之和题目数据
-- 基于 spring_oj.sql 结构创建的完整题目数据

-- 插入两数之和题目
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
输出：[0,1]',
    '第一行包含两个整数 n 和 target，分别表示数组长度和目标值。
第二行包含 n 个整数，表示数组 nums 的元素。',
    '输出一行，包含两个整数，表示和为 target 的两个数的下标（下标从0开始），用空格分隔。如果下标 i < j，则先输出 i 再输出 j。',
    '4 9
2 7 11 15',
    '0 1',
    '可以使用哈希表来优化时间复杂度到 O(n)。
遍历数组时，对于每个元素 nums[i]，检查 target - nums[i] 是否在哈希表中。',
    2000,
    128,
    'Easy',
    '["数组", "哈希表", "双指针"]',
    0,
    0,
    1,
    1
);

-- 获取刚插入的题目ID（假设为1，实际使用时可能需要调整）
SET @problem_id = LAST_INSERT_ID();

-- 插入测试用例
-- 测试用例1：基本示例
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES
(@problem_id, 'input1.txt', 'output1.txt', 10, 1);

-- 测试用例2：另一个基本示例
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES
(@problem_id, 'input2.txt', 'output2.txt', 10, 1);

-- 测试用例3：相同元素
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES
(@problem_id, 'input3.txt', 'output3.txt', 15, 0);

-- 测试用例4：大数组
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES
(@problem_id, 'input4.txt', 'output4.txt', 15, 0);

-- 测试用例5：负数
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES
(@problem_id, 'input5.txt', 'output5.txt', 15, 0);

-- 测试用例6：边界情况
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES
(@problem_id, 'input6.txt', 'output6.txt', 15, 0);

-- 测试用例7：最小数组
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES
(@problem_id, 'input7.txt', 'output7.txt', 10, 0);

-- 测试用例8：零和负数混合
INSERT INTO `test_cases` (`problem_id`, `input_file`, `output_file`, `score`, `is_sample`) VALUES
(@problem_id, 'input8.txt', 'output8.txt', 10, 0);

/*
测试用例文件内容说明：

input1.txt:
4 9
2 7 11 15

output1.txt:
0 1

input2.txt:
3 6
3 2 4

output2.txt:
1 2

input3.txt:
2 6
3 3

output3.txt:
0 1

input4.txt:
6 10
1 5 3 7 9 2

output4.txt:
1 3

input5.txt:
4 0
-3 4 3 90

output5.txt:
0 2

input6.txt:
5 8
2 5 5 11 1

output6.txt:
1 2

input7.txt:
2 3
1 2

output7.txt:
0 1

input8.txt:
4 -1
-1 0 1 2

output8.txt:
0 1
*/
