#!/bin/bash

# 测试"两数之和"题目的完整流程脚本

echo "=== SpringOJ 两数之和题目测试 ==="

# 1. 插入题目数据
echo "1. 插入题目数据到数据库..."
mysql -u root -p spring_oj < ../sql/two_sum_problem.sql
if [ $? -eq 0 ]; then
    echo "✅ 题目数据插入成功"
else
    echo "❌ 题目数据插入失败"
    exit 1
fi

# 2. 启动服务
echo "2. 启动SpringOJ服务..."
echo "请确保以下服务已启动："
echo "   - SpringOJ-judge (端口: 8080)"
echo "   - SpringOJ-sandbox (端口: 8081)"
echo "   - MySQL数据库"
echo "   - Docker服务"

# 3. 测试API接口
echo "3. 测试API接口..."

# 获取题目信息
echo "3.1 获取题目信息..."
curl -X GET "http://localhost:8080/api/problem/1" \
     -H "Content-Type: application/json" | jq .

# 提交Java解法
echo "3.2 提交Java解法..."
curl -X POST "http://localhost:8080/api/judge/submit" \
     -H "Content-Type: application/json" \
     -d '{
       "problemId": 1,
       "language": "JAVA", 
       "userId": 1,
       "sourceCode": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int n = sc.nextInt();\n        int[] nums = new int[n];\n        for (int i = 0; i < n; i++) {\n            nums[i] = sc.nextInt();\n        }\n        int target = sc.nextInt();\n        \n        Map<Integer, Integer> map = new HashMap<>();\n        for (int i = 0; i < n; i++) {\n            int complement = target - nums[i];\n            if (map.containsKey(complement)) {\n                System.out.println(map.get(complement) + \" \" + i);\n                return;\n            }\n            map.put(nums[i], i);\n        }\n    }\n}"
     }' | jq .

echo "测试完成！请查看判题结果。"
