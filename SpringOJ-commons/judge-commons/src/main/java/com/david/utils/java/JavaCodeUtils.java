package com.david.utils.java;

import org.springframework.stereotype.Component;

@Component
public class JavaCodeUtils {

	/**
	 * 根据给定的方法名和返回类型，生成一个Solution类的代码字符串。
	 *
	 * @param solutionFunctionName 函数名
	 * @param outputType           返回类型
	 * @return 拼接好的Solution类代码字符串
	 */
	public String generateSolutionClass(String solutionFunctionName, String outputType) {
		// 定义代码模板
		String codeTemplate = """
				class Solution {
				    public %s %s(String s) {
				       \s
				    }
				}""";

		// 使用参数填充模板并返回结果
		return String.format(codeTemplate, outputType, solutionFunctionName);
	}

	/**
	 * 生成一个带 main 方法的可运行类，演示如何调用 Solution.<functionName>(String) 并打印结果。
	 *
	 * @param solutionFunctionName 目标函数名
	 * @param outputType           目标函数返回类型（例如："int"、"String"、"int[]" 等）
	 * @param inputType            输入参数类型（例如："String"、"int"、"int[]" 等）
	 * @param output                输出参数 符合输出参数类型的值
	 * @param input                输入参数 符合输入参数类型的值
	 * @return 完整的 Main 类源码
	 */
	public String generateMainClass(String solutionFunctionName, String outputType, String inputType , String output , String input) {
        String codeTemplate = """
                import java.util.*;
                class Main {
                    private static String toStr(Object o) {
                        if (o == null) return "null";
                        Class<?> c = o.getClass();
                        if (c.isArray()) {
                            if (o instanceof int[]) return java.util.Arrays.toString((int[]) o);
                            if (o instanceof long[]) return java.util.Arrays.toString((long[]) o);
                            if (o instanceof short[]) return java.util.Arrays.toString((short[]) o);
                            if (o instanceof byte[]) return java.util.Arrays.toString((byte[]) o);
                            if (o instanceof char[]) return java.util.Arrays.toString((char[]) o);
                            if (o instanceof boolean[]) return java.util.Arrays.toString((boolean[]) o);
                            if (o instanceof float[]) return java.util.Arrays.toString((float[]) o);
                            if (o instanceof double[]) return java.util.Arrays.toString((double[]) o);
                            return java.util.Arrays.deepToString((Object[]) o);
                        }
                        return String.valueOf(o);
                    }
                    public static void main(String[] args) {
                        try {
                            Solution solution = new Solution();
                            String s = %s;
                            %s expected = %s;
                            %s result = solution.%s(s);
                            boolean ok = java.util.Objects.deepEquals(expected, result);
                            System.out.println(ok);
                            System.out.println(toStr(result));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            """;
        return String.format(codeTemplate, input, outputType, output, outputType, solutionFunctionName);
    }

	public String generateMainFixSolutionClass(String MainClass, String SolutionClass)
	{
		if (MainClass == null || SolutionClass == null) {
			throw new IllegalArgumentException("MainClass or SolutionClass cannot be null");
		}
		String mainSrc = MainClass.trim();
		String solutionSrc = SolutionClass.trim();
		if (mainSrc.isEmpty() || solutionSrc.isEmpty()) {
			throw new IllegalArgumentException("MainClass or SolutionClass cannot be empty");
		}
		return solutionSrc +
				"\n\n" +
				mainSrc +
				'\n';
	}
}