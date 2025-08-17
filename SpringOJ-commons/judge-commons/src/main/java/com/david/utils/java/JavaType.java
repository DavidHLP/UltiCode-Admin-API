package com.david.utils.java;

import lombok.Getter;

/**
 * 全新的 JSON 导向 Java 类型枚举。
 * 
 * 设计原则：
 * 1. 严格 JSON 格式：所有输入输出必须是合法的 JSON
 * 2. 类型安全：每种类型对应唯一的 JSON 表示和 Java 类型
 * 3. 简化映射：移除复杂的多维数组和集合嵌套，专注核心类型
 * 4. 明确语义：JSON 格式示例直接体现在类型定义中
 */
@Getter
public enum JavaType {
    
    // ========= JSON 基础类型 =========
    
    /** JSON: 42, -100, 0 */
    INTEGER("int", "整数"),
    
    /** JSON: 3.14, -2.5, 0.0 */
    DOUBLE("double", "浮点数"),
    
    /** JSON: "hello", "world", "" */
    STRING("String", "字符串"),
    
    /** JSON: true, false */
    BOOLEAN("boolean", "布尔值"),
    
    // ========= JSON 数组类型 =========
    
    /** JSON: [1, 2, 3, 4] */
    INT_ARRAY("int[]", "整数数组"),
    
    /** JSON: [1.1, 2.2, 3.3] */
    DOUBLE_ARRAY("double[]", "浮点数组"),
    
    /** JSON: ["hello", "world", "test"] */
    STRING_ARRAY("String[]", "字符串数组"),
    
    /** JSON: [true, false, true] */
    BOOLEAN_ARRAY("boolean[]", "布尔数组"),
    
    // ========= JSON 二维数组类型 =========
    
    /** JSON: [[1, 2], [3, 4], [5, 6]] */
    INT_2D_ARRAY("int[][]", "二维整数数组"),
    
    /** JSON: [["hello", "world"], ["foo", "bar"]] */
    STRING_2D_ARRAY("String[][]", "二维字符串数组"),
    
    /** JSON: [[true, false], [false, true]] */
    BOOLEAN_2D_ARRAY("boolean[][]", "二维布尔数组"),
    
    // ========= JSON 集合类型（使用 List，简化处理） =========
    
    /** JSON: [1, 2, 3] -> List<Integer> */
    LIST_INTEGER("List<Integer>", "整数列表"),
    
    /** JSON: ["a", "b", "c"] -> List<String> */
    LIST_STRING("List<String>", "字符串列表"),
    
    /** JSON: [[1, 2], [3, 4]] -> List<List<Integer>> */
    LIST_LIST_INTEGER("List<List<Integer>>", "嵌套整数列表"),
    
    /** JSON: [["a", "b"], ["c", "d"]] -> List<List<String>> */
    LIST_LIST_STRING("List<List<String>>", "嵌套字符串列表"),
    
    // ========= 特殊类型 =========
    
    /** 无返回值 */
    VOID("void", "无返回值");

    private final String javaType;
    private final String description;

    JavaType(String javaType, String description) {
        this.javaType = javaType;
        this.description = description;
    }

    /**
     * 获取 Java 类型名
     */
    public String getTypeName() {
        return javaType;
    }

    /**
     * 检查是否为数组类型
     */
    public boolean isArray() {
        return javaType.endsWith("[]");
    }

    /**
     * 检查是否为集合类型
     */
    public boolean isList() {
        return javaType.startsWith("List<");
    }

    /**
     * 检查是否为基础类型（非数组、非集合）
     */
    public boolean isPrimitive() {
        return !isArray() && !isList() && !javaType.equals("void");
    }

    /**
     * 获取默认值的 JSON 表示
     */
    public String getDefaultJsonValue() {
        return switch (this) {
            case INTEGER -> "0";
            case DOUBLE -> "0.0";
            case STRING -> "\"\"";
            case BOOLEAN -> "false";
            case INT_ARRAY, DOUBLE_ARRAY, STRING_ARRAY, BOOLEAN_ARRAY,
                 INT_2D_ARRAY, STRING_2D_ARRAY, BOOLEAN_2D_ARRAY,
                 LIST_INTEGER, LIST_STRING, LIST_LIST_INTEGER, LIST_LIST_STRING -> "[]";
            case VOID -> null;
        };
    }

    /**
     * 根据 JSON 值推断可能的类型（用于验证）
     */
    public static boolean isValidJsonForType(String jsonValue, JavaType type) {
        if (jsonValue == null || jsonValue.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = jsonValue.trim();
        
        return switch (type) {
            case INTEGER -> trimmed.matches("-?\\d+");
            case DOUBLE -> trimmed.matches("-?\\d+(\\.\\d+)?");
            case STRING -> trimmed.startsWith("\"") && trimmed.endsWith("\"");
            case BOOLEAN -> "true".equals(trimmed) || "false".equals(trimmed);
            case INT_ARRAY, DOUBLE_ARRAY, STRING_ARRAY, BOOLEAN_ARRAY,
                 INT_2D_ARRAY, STRING_2D_ARRAY, BOOLEAN_2D_ARRAY,
                 LIST_INTEGER, LIST_STRING, LIST_LIST_INTEGER, LIST_LIST_STRING ->
                    trimmed.startsWith("[") && trimmed.endsWith("]");
            case VOID -> false;
        };
    }
}
