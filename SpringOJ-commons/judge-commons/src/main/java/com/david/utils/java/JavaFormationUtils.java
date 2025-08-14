package com.david.utils.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class JavaFormationUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 确保提供的值是合法的 JSON 字面量：
     * - 若本身已很合法 JSON（数字、布尔、数组、对象、带引号的字符串），直接返回去首尾空格后的原值；
     * - 若不是合法 JSON 且类型为字符串/字符，则包装为 JSON 字符串（自动转义特殊字符）；
     * - 其它情况返回去首尾空格后的原值。
     * 目的：保证传入 ObjectMapper 的文本均合法，以便在运行时代码中正确反序列化。
     */
    public static String ensureJsonLiteral(String rawValue, String typeName) {
        if (rawValue == null) return "null";
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) return "\"\""; // 空字符串 -> ""

        // 已经很合法 JSON 则直接返回
        if (isValidJson(trimmed)) {
            return trimmed;
        }

        // 对于字符串/字符类型，统一包装为 JSON 字符串
        if (isStringLike(typeName)) {
            try {
                return MAPPER.writeValueAsString(trimmed);
            } catch (JsonProcessingException e) {
                // 理论不会触发；出现异常则回退到手动转义
                return quoteJavaString(trimmed);
            }
        }

        // 对于布尔类型，兼容 True/FALSE 等情况，归一化为小写 true/false
        if (isBooleanLike(typeName)) {
            if ("true".equalsIgnoreCase(trimmed)) return "true";
            if ("false".equalsIgnoreCase(trimmed)) return "false";
        }

        // 其它类型（数字/布尔/数组/对象等）这里不做强转，直接返回原值（可能后续步骤会提示错误）
        return trimmed;
    }

    /**
     * 生成在 Java 源码中可直接赋值的字符串字面量（含双引号且内部已转义）。
     * 例如：传入 [1,2] => "[1,2]"； 传入 "abc" => "\"abc\""。
     */
    public static String toJavaStringLiteral(String rawText) {
        if (rawText == null) return "null";
        try {
            // 利用 JSON 字符串转义的特性，得到可作为 Java 源码字符串字面量放入的内容
            return MAPPER.writeValueAsString(rawText);
        } catch (JsonProcessingException e) {
            return quoteJavaString(rawText);
        }
    }

    /**
     * 根据类型名生成用于 ObjectMapper 反序列化的类型令牌代码片段：
     * - 非泛型：返回类似 "int.class"、"String[].class"、"Integer.class"；
     * - 泛型：返回 "new com.fasterxml.jackson.core.type.TypeReference<...>(){}"。
     */
    public static String buildJacksonTypeToken(String typeName) {
        String t = normalize(typeName);
        if (isGenericType(t)) {
            return "new " + TypeReference.class.getName() + "<" + t + ">(){}";
        }
        return classLiteralOf(t);
    }

    /**
     * 是否为泛型类型（简单通过是否包含 '<' 判断）。
     */
    public static boolean isGenericType(String typeName) {
        return typeName != null && typeName.contains("<");
    }

    /**
     * 判断是否为字符串相关类型（String / Character / char）。
     */
    public static boolean isStringLike(String typeName) {
        String t = normalize(typeName);
        return Objects.equals(t, "String") || Objects.equals(t, "Character") || Objects.equals(t, "char");
    }

    /**
     * 判断是否为布尔相关类型（boolean / Boolean）。
     */
    public static boolean isBooleanLike(String typeName) {
        String t = normalize(typeName);
        return Objects.equals(t, "boolean") || Objects.equals(t, "Boolean");
    }

    // =============== helpers ===============

    private static boolean isValidJson(String text) {
        try {
            MAPPER.readTree(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String quoteJavaString(String s) {
        String escaped = s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return '"' + escaped + '"';
    }

    private static String classLiteralOf(String typeName) {
	    return switch (typeName) {
		    // primitives
		    case "boolean", "byte", "char", "short", "int", "long", "float", "double" -> typeName + ".class";
		    default ->

			    // arrays（含多维）或直接引用类型
			    // 这里直接使用简名的 .class（如 String[][].class、Integer.class）
				    typeName + ".class";
	    };

    }

    private static String normalize(String typeName) {
        return typeName == null ? "" : typeName.trim();
    }
}
