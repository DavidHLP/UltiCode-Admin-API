# SpringOJ "两数之和" 题目测试指南

## 🚀 快速测试步骤

### 1. 启动服务
确保以下服务正在运行：
- SpringOJ-judge (默认端口: 8080)
- SpringOJ-sandbox (默认端口: 8081)
- MySQL数据库
- Docker服务

### 2. 插入题目数据
```bash
# 在项目根目录执行
mysql -u root -p spring_oj < sql/two_sum_problem.sql
```

### 3. API测试示例

#### 3.1 获取题目信息
```bash
curl -X GET "http://localhost:8080/api/problem/1" \
     -H "Content-Type: application/json"
```

#### 3.2 提交正确的Java解法
```bash
curl -X POST "http://localhost:8080/api/judge/submit" \
     -H "Content-Type: application/json" \
     -d '{
       "problemId": 1,
       "language": "JAVA",
       "userId": 1,
       "sourceCode": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int n = sc.nextInt();\n        int[] nums = new int[n];\n        for (int i = 0; i < n; i++) {\n            nums[i] = sc.nextInt();\n        }\n        int target = sc.nextInt();\n        \n        Map<Integer, Integer> map = new HashMap<>();\n        for (int i = 0; i < n; i++) {\n            int complement = target - nums[i];\n            if (map.containsKey(complement)) {\n                System.out.println(map.get(complement) + \" \" + i);\n                return;\n            }\n            map.put(nums[i], i);\n        }\n    }\n}"
     }'
```

#### 3.3 提交错误的解法（测试Wrong Answer）
```bash
curl -X POST "http://localhost:8080/api/judge/submit" \
     -H "Content-Type: application/json" \
     -d '{
       "problemId": 1,
       "language": "JAVA",
       "userId": 1,
       "sourceCode": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int n = sc.nextInt();\n        for (int i = 0; i < n; i++) {\n            sc.nextInt();\n        }\n        int target = sc.nextInt();\n        System.out.println(\"0 0\"); // 错误答案\n    }\n}"
     }'
```

#### 3.4 提交编译错误的代码
```bash
curl -X POST "http://localhost:8080/api/judge/submit" \
     -H "Content-Type: application/json" \
     -d '{
       "problemId": 1,
       "language": "JAVA",
       "userId": 1,
       "sourceCode": "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello\");\n        // 语法错误：缺少分号\n        int x = 5\n    }\n}"
     }'
```

#### 3.5 查询提交结果
```bash
# 替换{submissionId}为实际的提交ID
curl -X GET "http://localhost:8080/api/judge/submission/{submissionId}" \
     -H "Content-Type: application/json"
```

## 📝 测试用例验证

### 样例测试用例
1. **输入**: `4\n2 7 11 15\n9` → **期望输出**: `0 1`
2. **输入**: `3\n3 2 4\n6` → **期望输出**: `1 2`
3. **输入**: `2\n3 3\n6` → **期望输出**: `0 1`

### 边界测试用例
4. **负数**: `4\n-1 -2 -3 -4\n-6` → **期望输出**: `2 3`
5. **包含0**: `3\n0 4 3\n0` → **期望输出**: `0 1`
6. **大数值**: `3\n1000000000 -1000000000 0\n0` → **期望输出**: `0 1`

## 🔍 预期结果

### 正确提交的响应示例：
```json
{
  "code": 200,
  "message": "代码提交成功，正在判题中...",
  "data": 12345
}
```

### 查询结果的响应示例：
```json
{
  "code": 200,
  "message": "提交记录获取成功",
  "data": {
    "id": 12345,
    "userId": 1,
    "problemId": 1,
    "language": "JAVA",
    "status": "ACCEPTED",
    "score": 100,
    "timeUsed": 245,
    "memoryUsed": 2048,
    "createdAt": "2025-07-22T12:30:00"
  }
}
```

## ⚠️ 常见问题排查

1. **服务未启动**: 检查SpringOJ-judge和SpringOJ-sandbox服务状态
2. **数据库连接失败**: 检查MySQL服务和连接配置
3. **Docker未运行**: 确保Docker服务正在运行
4. **题目不存在**: 确认已执行SQL脚本插入题目数据
5. **编译失败**: 检查代码语法和Docker镜像是否正确
