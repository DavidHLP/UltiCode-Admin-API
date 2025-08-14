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
        // 生成 JSON 字面量（保证合法）
        String inputJsonLiteral = JavaFormationUtils.toJavaStringLiteral(
                JavaFormationUtils.ensureJsonLiteral(input, inputType)
        );
        String expectedJsonLiteral = JavaFormationUtils.toJavaStringLiteral(
                JavaFormationUtils.ensureJsonLiteral(output, outputType)
        );

        // 输入传递给 solution(String s) 作为 JSON 文本，这里不反序列化为具体类型

        String codeTemplate = """
                import java.util.*;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import com.fasterxml.jackson.databind.JsonNode;

                class Main {
                    private static final ObjectMapper MAPPER = new ObjectMapper();

                    private static String asJson(Object o) {
                        try { return MAPPER.writeValueAsString(o); } catch (Exception e) { return String.valueOf(o); }
                    }

                    public static void main(String[] args) {
                        try {
                            Solution solution = new Solution();
                            String s = %s; // JSON 文本输入

                            // 期望输出按 JSON 读取为树
                            JsonNode expectedNode = MAPPER.readTree(%s);

                            %s result = solution.%s(s);
                            String resultJson = asJson(result);
                            JsonNode resultNode = MAPPER.readTree(resultJson);

                            boolean ok = java.util.Objects.equals(expectedNode, resultNode);
                            System.out.println(ok);
                            System.out.println(resultJson);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            """;

        // 占位：
        // %s1 -> inputJsonLiteral
        // %s2 -> expectedJsonLiteral
        // %s3 -> outputType
        // %s4 -> solutionFunctionName

        return String.format(
                codeTemplate,
                inputJsonLiteral,     // %s1
                expectedJsonLiteral,  // %s2
                outputType,           // %s3
                solutionFunctionName  // %s4
        );
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