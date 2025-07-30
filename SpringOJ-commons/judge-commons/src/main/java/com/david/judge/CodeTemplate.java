package com.david.judge;
/**
 * 代码模板接口
 */
public class CodeTemplate {
    public static final String JAVA_SOLUTION_TEMPLATE = """
class Solution {
    public int[] twoSum(int[] nums, int target) {
        // Write your code here
        return null;
    }
}
""";

    public static final String JAVA_MAIN_WRAPPER_TEMPLATE = """
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Read input for nums
        String numsStr = scanner.nextLine();
        int[] nums = Arrays.stream(numsStr.substring(1, numsStr.length() - 1).split(","))
                            .map(String::trim)
                            .mapToInt(Integer::parseInt)
                            .toArray();

        // Read input for target
        int target = scanner.nextInt();

        Solution solution = new Solution();
        int[] result = solution.twoSum(nums, target);

        System.out.println(Arrays.toString(result));
        scanner.close();
    }
}
""";
}