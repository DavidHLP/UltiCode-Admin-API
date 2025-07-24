export interface Quiz {
  id: number
  title: string
  difficulty: '简单' | '中等' | '困难'
  description: string
  tags: string[]
  initialCode: {
    [language: string]: string
  }
  testCases: {
    name: string
    inputs: { [key: string]: string }
  }[]
}

export const twoSumMock: Quiz = {
  id: 1,
  title: '1. 两数之和',
  difficulty: '简单',
  tags: ['数组', '哈希表'],
  description: `### 题目描述

给定一个整数数组 \`nums\` 和一个整数目标值 \`target\`，请你在该数组中找出和为 \`target\` 的那 **两个** 整数，并返回它们的数组下标。

你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在目标结果里不能重复出现。

你可以按任意顺序返回答案。

---

### 示例

* **示例 1:**
    > **输入:** \`nums = [2,7,11,15]\`, \`target = 9\`
    > **输出:** \`[0,1]\`
    > **解释:** 因为 \`nums[0] + nums[1] == 9\`，所以返回 \`[0, 1]\`。

* **示例 2:**
    > **输入:** \`nums = [3,2,4]\`, \`target = 6\`
    > **输出:** \`[1,2]\`

* **示例 3:**
    > **输入:** \`nums = [3,3]\`, \`target = 6\`
    > **输出:** \`[0,1]\`

---

### 提示

* \`2 <= nums.length <= 10⁴\`
* \`-10⁹ <= nums[i] <= 10⁹\`
* \`-10⁹ <= target <= 10⁹\`
* **只会存在一个有效答案**
  `,
  initialCode: {
    java: `class Solution {
    public int[] twoSum(int[] nums, int target) {

    }
}`,
    python: `class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
`,
    cpp: `class Solution {
public:
    vector<int> twoSum(vector<int>& nums, int target) {

    }
};`,
  },
  testCases: [
    {
      name: 'Case 1',
      inputs: {
        nums: '[2,7,11,15]',
        target: '9',
      },
    },
    {
      name: 'Case 2',
      inputs: {
        nums: '[3,2,4]',
        target: '6',
      },
    },
    {
      name: 'Case 3',
      inputs: {
        nums: '[3,3]',
        target: '6',
      },
    },
  ],
}
