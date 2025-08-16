package com.david.utils.java;

import com.david.testcase.dto.TestCaseInputDto;
import com.david.testcase.dto.TestCaseOutputDto;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JavaCodeUtils {

    /**
     * 根据给定的方法名和返回类型，生成一个 Solution 类的代码字符串。
     *
     * @param solutionFunctionName 函数名
     * @param testCaseOutput       测试用例期望输出（决定返回类型）
     * @param testCaseInputs       测试用例输入列表（决定参数列表），按 orderIndex 升序
     * @return 可直接展示给用户的完整 Solution 类模板
     * @throws IllegalArgumentException 当内部推导类型或参数声明异常时抛出
     */
    public String generateSolutionClass(
            String solutionFunctionName,
            TestCaseOutputDto testCaseOutput,
            List<TestCaseInputDto> testCaseInputs) {
        String returnType = toJavaTypeName(testCaseOutput == null ? null : testCaseOutput.getOutputType());
        String params = buildParamDecls(testCaseInputs);
        boolean needUtilImport = usesCollectionType(returnType);
        if (!needUtilImport && testCaseInputs != null) {
            for (TestCaseInputDto in : testCaseInputs) {
                if (usesCollectionType(toJavaTypeName(in.getInputType()))) {
                    needUtilImport = true;
                    break;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        if (needUtilImport) {
            sb.append("import java.util.*;\n\n");
        }
        sb.append("class Solution {\n");
        sb.append("    public ")
                .append(returnType)
                .append(' ')
                .append(solutionFunctionName)
                .append('(')
                .append(params)
                .append(") {\n");
        sb.append("        // TODO: 实现你的算法\n");
        if (!"void".equals(returnType)) {
            sb.append("        ").append(defaultReturnStatement(returnType)).append(";\n");
        }
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * 生成支持多测试用例的 Main 类（fail-fast：遇到第一个失败即停止）。
     * JSON 安全策略：
     * - 统一使用 Jackson（ObjectMapper）序列化；
     * - 字符串若非 JSON 则作为字符串再次序列化，保证输出始终很合法 JSON；
     * - 比较时优先按 JSON 结构比对，失败再按字符串等值比对。
     *
     * @param solutionFunctionName 目标函数名
     * @param outputType           返回类型（可为 JavaType 枚举名或 Java 类型名）
     * @param allInputTypes        每个测试用例的输入类型列表的列表
     * @param allInputValues       每个测试用例的输入值列表的列表
     * @param allExpectedOutputs   每个测试用例的期望输出列表（字符串形式，将按 JSON 尝试解析）
     * @return 完整 Main 类源码
     * @throws IllegalArgumentException 当入参列表大小不一致或为空，或测试用例配置非法时抛出
     */
    public String generateMultiTestCaseMainClass(
            String solutionFunctionName,
            String outputType,
            List<List<String>> allInputTypes,
            List<List<String>> allInputValues,
            List<String> allExpectedOutputs) {
        if (allInputTypes == null || allInputValues == null || allExpectedOutputs == null ||
                allInputTypes.size() != allInputValues.size() || allInputTypes.size() != allExpectedOutputs.size()) {
            throw new IllegalArgumentException("allInputTypes、allInputValues 与 allExpectedOutputs 大小不一致或为空");
        }

        String javaOutputType = toJavaTypeName(outputType);
        StringBuilder testCaseCalls = new StringBuilder();
        testCaseCalls.append("for (int testIndex = 0; testIndex < ").append(allInputTypes.size())
                .append("; testIndex++) {\n");

        for (int i = 0; i < allInputTypes.size(); i++) {
            List<String> inputTypes = allInputTypes.get(i);
            List<String> inputValues = allInputValues.get(i);

            if (inputTypes.size() != inputValues.size()) {
                throw new IllegalArgumentException("测试用例[" + i + "]的inputTypes与inputValues长度不一致");
            }

            // 将类型键/枚举名统一映射为可编译的 Java 类型名
            List<String> javaInputTypes = new ArrayList<>();
            for (String t : inputTypes) {
                javaInputTypes.add(toJavaTypeName(t));
            }

            String argsExpr = JavaFormationUtils.buildArgumentList(inputValues, javaInputTypes);
            String expectedOutput = allExpectedOutputs.get(i);

            testCaseCalls.append("                if (testIndex == ").append(i).append(") {\n")
                    .append("                    try {\n")
                    .append("                        ").append(javaOutputType).append(" actualResult = solution.")
                    .append(solutionFunctionName).append("(").append(argsExpr).append(");\n")
                    .append("                        \n")
                    .append("                        // 标准化实际结果和期望结果为可比较的格式\n")
                    .append("                        String actualJson = normalizeForComparison(actualResult);\n")
                    .append("                        String expectedJson = normalizeForComparison(")
                    .append(JavaFormationUtils.toJavaStringLiteral(expectedOutput)).append(");\n")
                    .append("                        \n")
                    .append("                        // 解析并比较 JSON（严格：解析失败视为错误）\n")
                    .append("                        JsonNode actualNode = MAPPER.readTree(actualJson);\n")
                    .append("                        JsonNode expectedNode = MAPPER.readTree(expectedJson);\n")
                    .append("                        boolean isMatch = actualNode.equals(expectedNode);\n")
                    .append("                        \n")
                    .append("                        if (isMatch) {\n")
                    .append("                            // 成功：添加成功标记\n")
                    .append("                            Map<String, Object> successInfo = new HashMap<>();\n")
                    .append("                            successInfo.put(\"testCaseIndex\", ").append(i).append(");\n")
                    .append("                            successInfo.put(\"success\", true);\n")
                    .append("                            successInfo.put(\"output\", actualJson);\n")
                    .append("                            results.add(asJson(successInfo));\n")
                    .append("                        } else {\n")
                    .append("                            // 失败：记录错误信息并停止\n")
                    .append("                            Map<String, Object> errorInfo = new HashMap<>();\n")
                    .append("                            errorInfo.put(\"testCaseIndex\", ").append(i).append(");\n")
                    .append("                            errorInfo.put(\"success\", false);\n")
                    .append("                            errorInfo.put(\"actualOutput\", actualJson);\n")
                    .append("                            errorInfo.put(\"expectedOutput\", expectedJson);\n")
                    .append("                            results.add(asJson(errorInfo));\n")
                    .append("                            break; // 遇到第一个失败就停止\n")
                    .append("                        }\n")
                    .append("                    } catch (Exception e) {\n")
                    .append("                        // 运行时异常：记录异常信息并停止\n")
                    .append("                        Map<String, Object> errorInfo = new HashMap<>();\n")
                    .append("                        errorInfo.put(\"testCaseIndex\", ").append(i).append(");\n")
                    .append("                        errorInfo.put(\"success\", false);\n")
                    .append("                        errorInfo.put(\"error\", e.getMessage());\n")
                    .append("                        results.add(asJson(errorInfo));\n")
                    .append("                        break; // 遇到异常也停止\n")
                    .append("                    }\n")
                    .append("                }\n");
        }

        testCaseCalls.append("            }");

        String codeTemplate = """
                     import java.util.*;
                     import com.fasterxml.jackson.databind.ObjectMapper;
                     import com.fasterxml.jackson.databind.JsonNode;

                     public class Main {
                         private static final ObjectMapper MAPPER = new ObjectMapper();

                         /**
                        * 使用 Jackson 直接序列化，失败即抛出异常
                        */
                       private static String asJson(Object obj) {
                           try {
                               return MAPPER.writeValueAsString(obj);
                           } catch (Exception e) {
                               throw new RuntimeException("JSON serialization failed", e);
                           }
                       }

                         /**
                          * 标准化对象为可比较的 JSON 格式，仅使用 Jackson，不做兜底
                          */
                         private static String normalizeForComparison(Object obj) {
                             try {
                                 if (obj instanceof String) {
                                     String str = (String) obj;
                                     try {
                                         JsonNode node = MAPPER.readTree(str);
                                         return MAPPER.writeValueAsString(node);
                                     } catch (Exception e) {
                                         return MAPPER.writeValueAsString(str);
                                     }
                                 }
                                 return MAPPER.writeValueAsString(obj);
                             } catch (Exception e) {
                                 throw new RuntimeException("JSON normalization failed", e);
                             }
                         }

                         public static void main(String[] args) {
                             try {
                                 Solution solution = new Solution();
                                 List<String> results = new ArrayList<>();
                                \s
                                 %s
                                \s
                                 // 输出结果（可能是完整数组或早期终止）
                                 System.out.println("[" + String.join(",", results) + "]");
                             } catch (Exception e) {
                                 System.err.println("运行时发生致命错误: " + e.getMessage());
                                 e.printStackTrace();
                             }
                         }
                     }
                \s""";

        return String.format(codeTemplate, testCaseCalls);
    }

    /**
     * 合并已有 Main 源码与 Solution 源码，保持 package/import 位置正确，移除重复导入。
     *
     * @param MainClass     原始 Main 源码
     * @param SolutionClass 原始 Solution 源码
     * @return 合并后的完整源码
     * @throws IllegalArgumentException 当任一源码为 null/空时抛出
     */
    public String generateMainFixSolutionClass(String MainClass, String SolutionClass) {
        if (MainClass == null || SolutionClass == null) {
            throw new IllegalArgumentException("MainClass or SolutionClass cannot be null");
        }
        String mainSrc = MainClass.trim();
        String solutionSrc = SolutionClass.trim();
        if (mainSrc.isEmpty() || solutionSrc.isEmpty()) {
            throw new IllegalArgumentException("MainClass or SolutionClass cannot be empty");
        }
        // 提取可选的 package 与所有 import，确保它们位于文件顶部
        String packageLine = "";
        String mainBody = mainSrc;

        // 提取 package（若存在，取第一条）
        java.util.regex.Pattern pkgPattern = java.util.regex.Pattern.compile("(?m)^\\s*package\\s+[^;]+;\\s*$");
        java.util.regex.Matcher pkgMatcher = pkgPattern.matcher(mainBody);
        if (pkgMatcher.find()) {
            packageLine = pkgMatcher.group().trim();
            mainBody = pkgMatcher.replaceFirst("").trim();
        }

        // 提取所有 import
        java.util.regex.Pattern impPattern = java.util.regex.Pattern.compile("(?m)^\\s*import\\s+[^;]+;\\s*$");
        java.util.regex.Matcher impMatcher = impPattern.matcher(mainBody);
        StringBuilder imports = new StringBuilder();
        while (impMatcher.find()) {
            String imp = impMatcher.group().trim();
            if (!imports.isEmpty())
                imports.append('\n');
            imports.append(imp);
        }
        // 移除 mainBody 中的 import 语句
        mainBody = impMatcher.replaceAll("").trim();

        StringBuilder merged = new StringBuilder();
        if (!packageLine.isEmpty()) {
            merged.append(packageLine).append('\n').append('\n');
        }
        if (!imports.isEmpty()) {
            merged.append(imports).append('\n').append('\n');
        }
        merged.append(solutionSrc).append('\n').append('\n').append(mainBody).append('\n');
        return merged.toString();
    }

    // ============================= Helpers =============================

    private String toJavaTypeName(String typeKeyOrName) {
        if (typeKeyOrName == null || typeKeyOrName.isBlank()) {
            return "void"; // 缺省视为无返回
        }
        try {
            return JavaType.valueOf(typeKeyOrName).getTypeName();
        } catch (IllegalArgumentException ex) {
            // 不是 JavaType 枚举名，直接当作已是 Java 类型名使用
            return typeKeyOrName;
        }
    }

    private String buildParamDecls(List<TestCaseInputDto> inputs) {
        if (inputs == null || inputs.isEmpty())
            return "";
        List<TestCaseInputDto> list = new ArrayList<>(inputs);
        list.sort(
                java.util.Comparator.comparing(
                        i -> i.getOrderIndex() == null ? Integer.MAX_VALUE : i.getOrderIndex()));
        java.util.List<String> decls = new java.util.ArrayList<>();
        for (int idx = 0; idx < list.size(); idx++) {
            TestCaseInputDto in = list.get(idx);
            String typeName = toJavaTypeName(in.getInputType());
            String paramName = sanitizeParamName(in.getTestCaseName(), idx);
            decls.add(typeName + " " + paramName);
        }
        return String.join(", ", decls);
    }

    private String sanitizeParamName(String raw, int index) {
        String base = (raw == null || raw.isBlank()) ? ("arg" + index) : raw.trim();
        // 非法字符替换为下划线
        base = base.replaceAll("[^A-Za-z0-9_]", "_");
        // 不能以数字开头
        if (base.isEmpty() || Character.isDigit(base.charAt(0))) {
            base = "arg_" + base;
        }
        return base;
    }

    private String defaultReturnStatement(String returnTypeName) {
        return switch (returnTypeName) {
            case "boolean" -> "return false";
            case "byte", "short", "int", "long", "float", "double" -> "return 0";
            case "char" -> "return '\\0'";
            case "void" -> ""; // 不会使用
            default -> "return null"; // 引用类型或数组/泛型
        };
    }

    private boolean usesCollectionType(String typeName) {
        if (typeName == null)
            return false;
        String t = typeName.replace(" ", "");
        return t.startsWith("List<")
                || t.startsWith("Set<")
                || t.startsWith("Map<")
                || t.contains("List<")
                || t.contains("Set<")
                || t.contains("Map<");
    }

}