package com.david.utils.java;

import com.david.testcase.dto.TestCaseInputDto;
import com.david.testcase.dto.TestCaseOutputDto;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全新的 JSON 导向 Java 代码生成器。
 * 
 * 核心设计原则：
 * 1. 严格 JSON：所有测试用例必须是合法 JSON 格式
 * 2. 类型安全：基于新的 JavaType 枚举系统
 * 3. 简化模板：专注核心功能，移除复杂的兼容性处理
 * 4. 统一标准：使用 Jackson 进行所有 JSON 处理
 */
@Component
public class JavaCodeUtils {

    /**
     * 生成 Solution 类模板代码
     * 
     * @param solutionFunctionName 函数名
     * @param testCaseOutput 测试用例输出（决定返回类型）
     * @param testCaseInputs 测试用例输入列表（决定参数列表）
     * @return 完整的 Solution 类模板
     */
    public String generateSolutionClass(
            String solutionFunctionName,
            TestCaseOutputDto testCaseOutput,
            List<TestCaseInputDto> testCaseInputs) {
        
        if (solutionFunctionName == null || solutionFunctionName.trim().isEmpty()) {
            throw new IllegalArgumentException("解题函数名不能为空");
        }
        
        // 确定返回类型
        JavaType returnType = JavaType.VOID;
        if (testCaseOutput != null && testCaseOutput.getOutputType() != null) {
            returnType = parseJavaType(testCaseOutput.getOutputType());
        }
        
        // 排序并构建参数列表
        List<TestCaseInputDto> sortedInputs = sortInputsByOrder(testCaseInputs);
        List<JavaType> inputTypes = new ArrayList<>();
        StringBuilder paramDecls = new StringBuilder();
        
        for (int i = 0; i < sortedInputs.size(); i++) {
            TestCaseInputDto input = sortedInputs.get(i);
            JavaType inputType = parseJavaType(input.getInputType());
            inputTypes.add(inputType);
            
            if (i > 0) paramDecls.append(", ");
            paramDecls.append(inputType.getTypeName())
                     .append(" ")
                     .append(sanitizeParameterName(input.getTestCaseName(), i));
        }
        
        // 检查是否需要导入 java.util
        boolean needUtilImport = needsUtilImport(returnType, inputTypes);
        
        // 生成代码
        StringBuilder code = new StringBuilder();
        
        if (needUtilImport) {
            code.append("import java.util.*;\n\n");
        }
        
        code.append("class Solution {\n")
            .append("    public ")
            .append(returnType.getTypeName())
            .append(" ")
            .append(solutionFunctionName)
            .append("(")
            .append(paramDecls)
            .append(") {\n")
            .append("        // TODO: 实现你的算法\n");
        
        if (returnType != JavaType.VOID) {
            String defaultReturn = getDefaultReturnStatement(returnType);
            code.append("        ").append(defaultReturn).append(";\n");
        }
        
        code.append("    }\n")
            .append("}\n");
        
        return code.toString();
    }

    /**
     * 生成支持多测试用例的主程序代码
     * 
     * @param solutionFunctionName 目标函数名
     * @param outputType 返回类型
     * @param allInputTypes 所有测试用例的输入类型列表
     * @param allInputValues 所有测试用例的输入值列表（JSON 格式）
     * @param allExpectedOutputs 所有测试用例的期望输出列表（JSON 格式）
     * @return 完整的主程序代码
     */
    public String generateMultiTestCaseMainClass(
            String solutionFunctionName,
            String outputType,
            List<List<String>> allInputTypes,
            List<List<String>> allInputValues,
            List<String> allExpectedOutputs) {
        
        validateMultiTestCaseInput(allInputTypes, allInputValues, allExpectedOutputs);
        
        JavaType javaOutputType = parseJavaType(outputType);
        int testCaseCount = allInputTypes.size();
        
        StringBuilder testCaseCode = new StringBuilder();
        testCaseCode.append("            boolean shouldStop = false;\n");
        
        // 生成每个测试用例的执行逻辑
        for (int i = 0; i < testCaseCount; i++) {
            List<String> inputTypeNames = allInputTypes.get(i);
            List<String> inputValues = allInputValues.get(i);
            String expectedOutput = allExpectedOutputs.get(i);
            
            if (inputTypeNames.size() != inputValues.size()) {
                throw new IllegalArgumentException("测试用例[" + i + "]的输入类型与输入值数量不匹配");
            }
            
            // 转换为 JavaType
            List<JavaType> javaInputTypes = inputTypeNames.stream()
                    .map(this::parseJavaType)
                    .collect(Collectors.toList());
            
            // 生成参数表达式
            String paramExpression = JavaFormationUtils.buildParameterList(inputValues, javaInputTypes);
            
            testCaseCode
                        .append("            // 测试用例 ").append(i).append("\n")
                        .append("            if (!shouldStop) {\n")
                        .append("                try {\n")
                        .append("                    ").append(javaOutputType.getTypeName())
                        .append(" actualResult = solution.")
                        .append(solutionFunctionName)
                        .append("(")
                        .append(paramExpression)
                        .append(");\n")
                        .append("                    \n")
                        .append("                    String actualJson = objectToJson(actualResult);\n")
                        .append("                    String expectedJson = normalizeJson(")
                        .append(escapeStringLiteral(expectedOutput))
                        .append(");\n")
                        .append("                    \n")
                        .append("                    if (compareJson(actualJson, expectedJson)) {\n")
                        .append("                        results.add(createSuccessResult(")
                        .append(i)
                        .append(", actualJson));\n")
                        .append("                    } else {\n")
                        .append("                        results.add(createFailureResult(")
                        .append(i)
                        .append(", actualJson, expectedJson));\n")
                        .append("                        shouldStop = true; // 遇到失败即停止\n")
                        .append("                    }\n")
                        .append("                } catch (Exception e) {\n")
                        .append("                    results.add(createErrorResult(")
                        .append(i)
                        .append(", e.getMessage()));\n")
                        .append("                    shouldStop = true; // 遇到异常即停止\n")
                        .append("                }\n")
                        .append("            }\n")
                        .append("            \n");
        }
        
        String template = """
            import java.util.*;
            import com.fasterxml.jackson.databind.ObjectMapper;
            import com.fasterxml.jackson.databind.JsonNode;

            public class Main {
                private static final ObjectMapper MAPPER = new ObjectMapper();

                public static void main(String[] args) {
                    try {
                        Solution solution = new Solution();
                        List<String> results = new ArrayList<>();
                        
            %s
                        // 输出结果
                        System.out.println("[" + String.join(",", results) + "]");
                    } catch (Exception e) {
                        System.err.println("运行时发生致命错误: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                /**
                 * 将对象转换为 JSON 字符串
                 */
                private static String objectToJson(Object obj) {
                    try {
                        return MAPPER.writeValueAsString(obj);
                    } catch (Exception e) {
                        throw new RuntimeException("对象序列化为 JSON 失败", e);
                    }
                }

                /**
                 * 标准化 JSON 字符串
                 */
                private static String normalizeJson(String jsonStr) {
                    try {
                        JsonNode node = MAPPER.readTree(jsonStr);
                        return MAPPER.writeValueAsString(node);
                    } catch (Exception e) {
                        throw new RuntimeException("JSON 标准化失败: " + jsonStr, e);
                    }
                }

                /**
                 * 比较两个 JSON 字符串是否相等
                 */
                private static boolean compareJson(String json1, String json2) {
                    try {
                        JsonNode node1 = MAPPER.readTree(json1);
                        JsonNode node2 = MAPPER.readTree(json2);
                        return node1.equals(node2);
                    } catch (Exception e) {
                        return false; // JSON 解析失败视为不相等
                    }
                }

                /**
                 * 创建成功结果
                 */
                private static String createSuccessResult(int testCaseIndex, String actualJson) {
                    try {
                        Map<String, Object> result = new HashMap<>();
                        result.put("testCaseIndex", testCaseIndex);
                        result.put("success", true);
                        result.put("output", actualJson);
                        return MAPPER.writeValueAsString(result);
                    } catch (Exception e) {
                        throw new RuntimeException("创建成功结果失败", e);
                    }
                }

                /**
                 * 创建失败结果
                 */
                private static String createFailureResult(int testCaseIndex, String actualJson, String expectedJson) {
                    try {
                        Map<String, Object> result = new HashMap<>();
                        result.put("testCaseIndex", testCaseIndex);
                        result.put("success", false);
                        result.put("actualOutput", actualJson);
                        result.put("expectedOutput", expectedJson);
                        return MAPPER.writeValueAsString(result);
                    } catch (Exception e) {
                        throw new RuntimeException("创建失败结果失败", e);
                    }
                }

                /**
                 * 创建错误结果
                 */
                private static String createErrorResult(int testCaseIndex, String errorMessage) {
                    try {
                        Map<String, Object> result = new HashMap<>();
                        result.put("testCaseIndex", testCaseIndex);
                        result.put("success", false);
                        result.put("error", errorMessage);
                        return MAPPER.writeValueAsString(result);
                    } catch (Exception e) {
                        throw new RuntimeException("创建错误结果失败", e);
                    }
                }
            }
            """;
        
        String generatedCode = String.format(template, testCaseCode.toString());
        
        // 临时修复：替换可能存在的 return 语句
        generatedCode = generatedCode.replace(
            "return; // 遇到失败即停止", 
            "// 遇到失败即停止，但继续输出结果"
        );
        generatedCode = generatedCode.replace(
            "return; // 遇到异常即停止", 
            "// 遇到异常即停止，但继续输出结果"
        );
        
        return generatedCode;
    }

    /**
     * 合并 Solution 类和主程序代码
     */
    public String generateMainFixSolutionClass(String mainClass, String solutionClass) {
        if (mainClass == null || solutionClass == null) {
            throw new IllegalArgumentException("主程序代码和 Solution 代码不能为空");
        }
        
        String trimmedMain = mainClass.trim();
        String trimmedSolution = solutionClass.trim();
        
        if (trimmedMain.isEmpty() || trimmedSolution.isEmpty()) {
            throw new IllegalArgumentException("主程序代码和 Solution 代码不能为空");
        }
        
        // 提取 package 声明（如果有）
        String packageDecl = extractPackageDeclaration(trimmedMain);
        String mainWithoutPackage = removePackageDeclaration(trimmedMain);
        
        // 提取 import 语句
        List<String> imports = extractImports(mainWithoutPackage);
        String mainWithoutImports = removeImports(mainWithoutPackage);
        
        // 构建最终代码
        StringBuilder result = new StringBuilder();
        
        if (!packageDecl.isEmpty()) {
            result.append(packageDecl).append("\n\n");
        }
        
        if (!imports.isEmpty()) {
            for (String importStmt : imports) {
                result.append(importStmt).append("\n");
            }
            result.append("\n");
        }
        
        result.append(trimmedSolution).append("\n\n")
              .append(mainWithoutImports);
        
        return result.toString();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 解析字符串为 JavaType
     */
    private JavaType parseJavaType(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return JavaType.VOID;
        }
        
        try {
            return JavaType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 如果不是枚举值，尝试根据字符串推断
            return inferJavaTypeFromString(typeStr.trim());
        }
    }

    /**
     * 根据字符串推断 JavaType
     */
    private JavaType inferJavaTypeFromString(String typeStr) {
        return switch (typeStr.toLowerCase()) {
            case "int", "integer" -> JavaType.INTEGER;
            case "double" -> JavaType.DOUBLE;
            case "string" -> JavaType.STRING;
            case "boolean" -> JavaType.BOOLEAN;
            case "int[]", "integer[]" -> JavaType.INT_ARRAY;
            case "double[]" -> JavaType.DOUBLE_ARRAY;
            case "string[]" -> JavaType.STRING_ARRAY;
            case "boolean[]" -> JavaType.BOOLEAN_ARRAY;
            case "int[][]", "integer[][]" -> JavaType.INT_2D_ARRAY;
            case "string[][]" -> JavaType.STRING_2D_ARRAY;
            case "boolean[][]" -> JavaType.BOOLEAN_2D_ARRAY;
            case "list<integer>", "list<int>" -> JavaType.LIST_INTEGER;
            case "list<string>" -> JavaType.LIST_STRING;
            case "list<list<integer>>", "list<list<int>>" -> JavaType.LIST_LIST_INTEGER;
            case "list<list<string>>" -> JavaType.LIST_LIST_STRING;
            default -> throw new IllegalArgumentException("不支持的类型: " + typeStr);
        };
    }

    /**
     * 根据顺序索引对输入参数排序
     */
    private List<TestCaseInputDto> sortInputsByOrder(List<TestCaseInputDto> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return new ArrayList<>();
        }
        
        return inputs.stream()
                .sorted(Comparator.comparing(input -> 
                    input.getOrderIndex() != null ? input.getOrderIndex() : Integer.MAX_VALUE))
                .collect(Collectors.toList());
    }

    /**
     * 清理参数名
     */
    private String sanitizeParameterName(String rawName, int index) {
        String base = (rawName == null || rawName.trim().isEmpty()) ? 
                      ("arg" + index) : rawName.trim();
        
        // 替换非法字符
        base = base.replaceAll("[^A-Za-z0-9_]", "_");
        
        // 确保不以数字开头
        if (base.isEmpty() || Character.isDigit(base.charAt(0))) {
            base = "arg_" + base;
        }
        
        return base;
    }

    /**
     * 检查是否需要导入 java.util
     */
    private boolean needsUtilImport(JavaType returnType, List<JavaType> inputTypes) {
        if (returnType.isList()) {
            return true;
        }
        
        return inputTypes.stream().anyMatch(JavaType::isList);
    }

    /**
     * 获取默认返回语句
     */
    private String getDefaultReturnStatement(JavaType returnType) {
        return switch (returnType) {
            case INTEGER -> "return 0";
            case DOUBLE -> "return 0.0";
            case STRING -> "return \"\"";
            case BOOLEAN -> "return false";
            case INT_ARRAY -> "return new int[0]";
            case DOUBLE_ARRAY -> "return new double[0]";
            case STRING_ARRAY -> "return new String[0]";
            case BOOLEAN_ARRAY -> "return new boolean[0]";
            case INT_2D_ARRAY -> "return new int[0][0]";
            case STRING_2D_ARRAY -> "return new String[0][0]";
            case BOOLEAN_2D_ARRAY -> "return new boolean[0][0]";
            case LIST_INTEGER, LIST_STRING, LIST_LIST_INTEGER, LIST_LIST_STRING -> 
                "return new ArrayList<>()";
            case VOID -> "";
        };
    }

    /**
     * 验证多测试用例输入的合法性
     */
    private void validateMultiTestCaseInput(
            List<List<String>> allInputTypes,
            List<List<String>> allInputValues,
            List<String> allExpectedOutputs) {
        
        if (allInputTypes == null || allInputValues == null || allExpectedOutputs == null) {
            throw new IllegalArgumentException("测试用例参数不能为空");
        }
        
        if (allInputTypes.size() != allInputValues.size() || 
            allInputTypes.size() != allExpectedOutputs.size()) {
            throw new IllegalArgumentException("测试用例参数数量不一致");
        }
        
        if (allInputTypes.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个测试用例");
        }
    }

    /**
     * 转义字符串字面量
     */
    private String escapeStringLiteral(String str) {
        if (str == null) return "null";
        
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(str);
        } catch (Exception e) {
            throw new IllegalArgumentException("字符串转义失败: " + str, e);
        }
    }

    /**
     * 提取 package 声明
     */
    private String extractPackageDeclaration(String code) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile("^\\s*package\\s+[^;]+;\\s*", java.util.regex.Pattern.MULTILINE);
        java.util.regex.Matcher matcher = pattern.matcher(code);
        return matcher.find() ? matcher.group().trim() : "";
    }

    /**
     * 移除 package 声明
     */
    private String removePackageDeclaration(String code) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile("^\\s*package\\s+[^;]+;\\s*", java.util.regex.Pattern.MULTILINE);
        return pattern.matcher(code).replaceFirst("").trim();
    }

    /**
     * 提取 import 语句
     */
    private List<String> extractImports(String code) {
        List<String> imports = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile("^\\s*import\\s+[^;]+;\\s*", java.util.regex.Pattern.MULTILINE);
        java.util.regex.Matcher matcher = pattern.matcher(code);
        
        while (matcher.find()) {
            imports.add(matcher.group().trim());
        }
        
        return imports;
    }

    /**
     * 移除 import 语句
     */
    private String removeImports(String code) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile("^\\s*import\\s+[^;]+;\\s*", java.util.regex.Pattern.MULTILINE);
        return pattern.matcher(code).replaceAll("").trim();
    }
}
