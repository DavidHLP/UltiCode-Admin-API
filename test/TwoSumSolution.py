# 两数之和 - Python解法示例
# 时间复杂度: O(n)
# 空间复杂度: O(n)

def two_sum():
    # 读取数组长度
    n = int(input())
    
    # 读取数组元素
    nums = list(map(int, input().split()))
    
    # 读取目标值
    target = int(input())
    
    # 使用字典求解
    num_map = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in num_map:
            # 找到答案，输出下标
            print(num_map[complement], i)
            return
        num_map[num] = i

if __name__ == "__main__":
    two_sum()
