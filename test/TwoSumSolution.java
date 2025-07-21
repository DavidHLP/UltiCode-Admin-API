import java.util.*;

/**
 * 两数之和 - 正确解法示例
 * 时间复杂度: O(n)
 * 空间复杂度: O(n)
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        // 读取数组长度
        int n = sc.nextInt();
        
        // 读取数组元素
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }
        
        // 读取目标值
        int target = sc.nextInt();
        
        // 使用哈希表求解
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) {
                // 找到答案，输出下标
                System.out.println(map.get(complement) + " " + i);
                return;
            }
            map.put(nums[i], i);
        }
        
        sc.close();
    }
}
