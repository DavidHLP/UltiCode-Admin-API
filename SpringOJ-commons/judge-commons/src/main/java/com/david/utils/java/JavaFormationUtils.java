package com.david.utils.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class JavaFormationUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 确保提供的值转换为"合法 JSON 字面量"。
     * 安全策略：统一使用 Jackson 内置方法，避免手写序列化和多次转换问题。
     * - 所有类型统一通过 Jackson 处理，确保输出格式一致性
     * - 避免双重序列化：先检测是否已为有效JSON，再决定处理策略
     * - 类型安全：根据目标类型进行适当的值转换和验证
     *
     * @param rawValue 原始文本值（可能为非 JSON）
     * @param typeName 目标 Java 类型名，用于判定处理策略
     * @return 可安全用于 JSON 解析/比较的字符串（是一个合法的 JSON 片段）
     * @throws IllegalArgumentException 当值与目标类型不匹配时抛出
     */
    public static String ensureJsonLiteral(String rawValue, String typeName) {
        if (rawValue == null) {
            return "null";
        }

        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return isStringLike(typeName) ? "\"\"" : "null";
        }

        try {
            // 首先尝试解析为 JSON，检查是否已经是有效的 JSON 格式
            JsonNode existingNode = MAPPER.readTree(trimmed);

            // 根据目标类型验证和转换值
            if (isStringLike(typeName)) {
                // 字符串类型：如果解析出的是字符串节点，直接返回；否则将解析结果序列化为字符串
                if (existingNode.isTextual()) {
                    return MAPPER.writeValueAsString(existingNode.asText());
                } else {
                    return MAPPER.writeValueAsString(existingNode.toString());
                }
            } else if (isArrayLike(typeName)) {
                // 数组类型：必须是数组节点
                if (!existingNode.isArray()) {
                    throw new IllegalArgumentException("期望数组类型，但输入不是 JSON 数组: " + trimmed);
                }
                return existingNode.toString();
            } else if (isBooleanLike(typeName)) {
                // 布尔类型：标准化布尔值
                if (existingNode.isBoolean()) {
                    return String.valueOf(existingNode.asBoolean());
                } else if (existingNode.isTextual()) {
                    String text = existingNode.asText();
                    if ("true".equalsIgnoreCase(text))
                        return "true";
                    if ("false".equalsIgnoreCase(text))
                        return "false";
                    throw new IllegalArgumentException("无效的布尔值: " + text);
                }
                throw new IllegalArgumentException("无法转换为布尔值: " + trimmed);
            } else {
                // 其他类型（数字等）：直接返回标准化的 JSON
                return existingNode.toString();
            }
        } catch (JsonProcessingException e) {
            // 输入不是有效的 JSON，根据目标类型进行处理
            if (isStringLike(typeName)) {
                // 字符串类型：使用 Jackson 序列化原始值
                try {
                    return MAPPER.writeValueAsString(trimmed);
                } catch (JsonProcessingException ex) {
                    throw new IllegalArgumentException("无法序列化字符串值: " + trimmed, ex);
                }
            } else if (isBooleanLike(typeName)) {
                // 布尔类型：尝试解析文本布尔值
                if ("true".equalsIgnoreCase(trimmed))
                    return "true";
                if ("false".equalsIgnoreCase(trimmed))
                    return "false";
                throw new IllegalArgumentException("无效的布尔值: " + trimmed);
            } else if (isArrayLike(typeName)) {
                // 数组类型：输入必须是有效的 JSON 数组
                throw new IllegalArgumentException("数组类型输入必须是有效的 JSON 数组: " + trimmed, e);
            } else {
                // 其他类型：严格模式下不接受非 JSON 输入，直接抛出异常
                throw new IllegalArgumentException("输入不是合法 JSON，且不匹配目标类型: " + trimmed, e);
            }
        }
    }

    /**
     * 生成在 Java 源码中可直接赋值的字符串字面量（含双引号且内部已转义）。
     * 完全使用 Jackson 内置序列化，确保转义的正确性和安全性。
     * 例如：传入 [1,2] => "[1,2]"； 传入 "abc" => "\"abc\""。
     *
     * @param rawText 原始字符串
     * @return Java 源码中的字符串字面量表达式
     * @throws IllegalArgumentException 当序列化失败时抛出
     */
    public static String toJavaStringLiteral(String rawText) {
        if (rawText == null) {
            return "null";
        }
        try {
            // 使用 Jackson 的标准序列化确保正确转义
            return MAPPER.writeValueAsString(rawText);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("无法序列化字符串为 Java 字面量: " + rawText, e);
        }
    }

    /**
     * 判断是否为字符串相关类型。
     *
     * @param typeName Java 类型名
     * @return true 表示 String/Character/char
     */
    public static boolean isStringLike(String typeName) {
        String t = normalize(typeName);
        return Objects.equals(t, "String") || Objects.equals(t, "Character") || Objects.equals(t, "char");
    }

    /**
     * 判断是否为布尔相关类型。
     *
     * @param typeName Java 类型名
     * @return true 表示 boolean/Boolean
     */
    public static boolean isBooleanLike(String typeName) {
        String t = normalize(typeName);
        return Objects.equals(t, "boolean") || Objects.equals(t, "Boolean");
    }

    /**
     * 判断是否为数组类型（例如：int[]、String[]、double[][]）。
     *
     * @param typeName Java 类型名
     * @return true 表示数组类型
     */
    public static boolean isArrayLike(String typeName) {
        String t = normalize(typeName);
        return t.endsWith("[]");
    }

    /**
     * 提取一维元素类型（对多维数组，剥一层[]）。
     * 例如："int[][]" -> "int[]"；"String[]" -> "String"。
     *
     * @param typeName 数组类型名
     * @return 子元素类型名
     */
    public static String elementTypeOfArray(String typeName) {
        String t = normalize(typeName);
        if (!t.endsWith("[]"))
            return t;
        return t.substring(0, t.length() - 2);
    }

    /**
     * 将原始文本验证并标准化为合法 JSON 数组字面量。
     * 完全基于Jackson内置解析和序列化，确保格式正确性和类型安全。
     *
     * @param rawValue      原始文本值
     * @param arrayTypeName 目标数组类型名（用于错误提示）
     * @return 标准化的 JSON 数组文本
     * @throws IllegalArgumentException 当 rawValue 不是合法 JSON 数组时抛出
     */
    public static String formatArrayLiteral(String rawValue, String arrayTypeName) {
        String text = rawValue == null ? "[]" : rawValue.trim();

        if (text.isEmpty()) {
            return "[]";
        }

        try {
            JsonNode node = MAPPER.readTree(text);
            if (!node.isArray()) {
                throw new IllegalArgumentException(
                        String.format("期望 JSON 数组类型，但输入为 %s 类型: %s",
                                node.getNodeType(), text));
            }

            // 使用 Jackson 重新序列化确保格式标准化，避免空格/换行等格式差异
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    String.format("数组类型 '%s' 的输入必须为合法 JSON 数组格式，输入值: %s",
                            arrayTypeName, text),
                    e);
        }
    }

    // =============== helpers ===============

    /**
     * 已删除手写字符串转义方法，统一使用 Jackson 的 writeValueAsString 方法确保安全性。
     * 该方法的功能已被 toJavaStringLiteral 中的 Jackson 序列化完全替代。
     */

    private static String normalize(String typeName) {
        return typeName == null ? "" : typeName.trim();
    }

    /**
     * 根据类型和值，生成可直接写入 Java 源码中的表达式（作为形参传入 Solution 方法）。
     * 支持：
     * - 原始类型与包装类型（boolean/Boolean、整型/浮点型等）
     * - String/char/Character
     * - 一维/多维数组（如 int[]、String[][]），数组输入必须为合法 JSON 数组
     *
     * @param typeName 目标 Java 类型名
     * @param rawValue 原始文本值
     * @return Java 源码中的字面量/初始化表达式
     * @throws IllegalArgumentException 当数组输入不是合法 JSON 数组，或与目标类型不匹配时抛出
     */
    public static String buildJavaExpression(String typeName, String rawValue) {
        String t = normalize(typeName);
        if (t.isEmpty())
            throw new IllegalArgumentException("typeName 不能为空");

        if (isArrayLike(t)) {
            // 严格模式：输入必须是合法 JSON 数组
            String json = formatArrayLiteral(rawValue == null ? "[]" : rawValue, t);
            try {
                JsonNode node = MAPPER.readTree(json);
                if (!node.isArray()) {
                    throw new IllegalArgumentException("期望数组 JSON，但得到：" + json);
                }
                return buildArrayInit(t, node);
            } catch (Exception e) {
                throw new IllegalArgumentException("无法解析为数组：" + e.getMessage(), e);
            }
        }

        // 非数组
        return buildNonArrayLiteral(t, rawValue);
    }

    /**
     * 生成多参数调用的参数列表表达式，形如：arg1 , arg2 , arg3。
     *
     * @param inputs 各参数的原始文本值，顺序与 types 对应
     * @param types  各参数的 Java 类型名，顺序与 inputs 对应
     * @return 用于 Java 源码的方法调用参数表达式
     * @throws IllegalArgumentException 当 inputs 或 types 为空，或两者长度不一致时抛出
     */
    public static String buildArgumentList(List<String> inputs, List<String> types) {
        if (inputs == null || types == null || inputs.size() != types.size()) {
            throw new IllegalArgumentException("inputs 与 types 为空或大小不一致");
        }
        List<String> args = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            args.add(buildJavaExpression(types.get(i), inputs.get(i)));
        }
        return String.join(" , ", args);
    }

    // =============== internal generators ===============

    /**
     * 基于JsonNode构建Java数组初始化表达式
     * 递归处理多维数组，确保类型安全和格式正确
     */
    private static String buildArrayInit(String arrayTypeName, JsonNode arrayNode) {
        if (!arrayNode.isArray()) {
            throw new IllegalArgumentException(
                    String.format("期望数组节点，但得到 %s: %s",
                            arrayNode.getNodeType(), arrayNode));
        }

        String elemType = elementTypeOfArray(arrayTypeName);
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(normalize(arrayTypeName));
        sb.append("{ ");

        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode item = arrayNode.get(i);
            if (i > 0) {
                sb.append(" , ");
            }

            if (isArrayLike(elemType)) {
                // 多维数组递归处理
                if (!item.isArray()) {
                    throw new IllegalArgumentException(
                            String.format("多维数组类型 '%s' 的子项必须为数组，但得到 %s: %s",
                                    elemType, item.getNodeType(), item));
                }
                sb.append(buildArrayInit(elemType, item));
            } else {
                // 基础类型元素处理
                sb.append(nonArrayLiteralFromNode(elemType, item));
            }
        }
        sb.append(" }");
        return sb.toString();
    }

    /**
     * 构建非数组类型的Java字面量表达式
     * 严格的类型验证和安全的值转换
     */
    private static String buildNonArrayLiteral(String typeName, String rawValue) {
        String v = rawValue == null ? "" : rawValue.trim();

        if (isStringLike(typeName)) {
            return toJavaStringLiteral(v);
        }

        if (isBooleanLike(typeName)) {
            String normalized = v.toLowerCase();
            if ("true".equals(normalized)) {
                return "true";
            }
            if ("false".equals(normalized)) {
                return "false";
            }
            throw new IllegalArgumentException(
                    String.format("无效的布尔值 '%s'，期望 'true' 或 'false'", rawValue));
        }

        if ("char".equals(typeName) || "Character".equals(typeName)) {
            if (v.isEmpty()) {
                return "'\\0'";
            }
            char c = v.charAt(0);
            return "'" + escapeJavaChar(c) + "'";
        }

        // 数字类型验证：确保输入是有效的数字格式
        if (isNumericType(typeName)) {
            if (v.isEmpty()) {
                return "0";
            }
            // 基本数字格式验证
            try {
                Double.parseDouble(v); // 基础数字格式验证
                return v;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format("数字类型 '%s' 的值格式无效: %s", typeName, rawValue), e);
            }
        }

        // 其他类型直接返回
        return v;
    }

    /**
     * 从JsonNode构建非数组类型的Java字面量
     * 基于节点类型进行安全的值提取和转换
     */
    private static String nonArrayLiteralFromNode(String typeName, JsonNode node) {
        if (isStringLike(typeName)) {
            String value = node.isTextual() ? node.asText() : node.toString();
            return toJavaStringLiteral(value);
        }

        if (isBooleanLike(typeName)) {
            if (node.isBoolean()) {
                return node.asBoolean() ? "true" : "false";
            } else if (node.isTextual()) {
                String text = node.asText().toLowerCase();
                if ("true".equals(text))
                    return "true";
                if ("false".equals(text))
                    return "false";
                throw new IllegalArgumentException(
                        String.format("JsonNode 文本值无法转换为布尔类型: %s", node.asText()));
            }
            throw new IllegalArgumentException(
                    String.format("JsonNode 类型 %s 无法转换为布尔类型", node.getNodeType()));
        }

        if ("char".equals(typeName) || "Character".equals(typeName)) {
            String s = node.isTextual() ? node.asText() : node.toString();
            if (s.isEmpty()) {
                return "'\\0'";
            }
            char c = s.charAt(0);
            return "'" + escapeJavaChar(c) + "'";
        }

        // 数字和其他类型
        if (node.isNumber()) {
            return node.asText();
        } else if (node.isTextual()) {
            return node.asText();
        } else {
            return node.toString();
        }
    }

    /**
     * Java字符转义处理
     * 处理特殊字符的转义序列
     */
    private static String escapeJavaChar(char c) {
        return switch (c) {
            case '\\' -> "\\\\";
            case '\'' -> "\\'";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\t' -> "\\t";
            case '\b' -> "\\b";
            case '\f' -> "\\f";
            case '"' -> "\\\"";
            default -> {
                // 处理不可打印字符
                if (c < 32 || c > 126) {
                    yield String.format("\\u%04x", (int) c);
                }
                yield String.valueOf(c);
            }
        };
    }

    /**
     * 检查是否为数字类型
     */
    private static boolean isNumericType(String typeName) {
        if (typeName == null)
            return false;
        String normalized = typeName.trim();
        return "byte".equals(normalized) || "Byte".equals(normalized) ||
                "short".equals(normalized) || "Short".equals(normalized) ||
                "int".equals(normalized) || "Integer".equals(normalized) ||
                "long".equals(normalized) || "Long".equals(normalized) ||
                "float".equals(normalized) || "Float".equals(normalized) ||
                "double".equals(normalized) || "Double".equals(normalized);
    }
}
