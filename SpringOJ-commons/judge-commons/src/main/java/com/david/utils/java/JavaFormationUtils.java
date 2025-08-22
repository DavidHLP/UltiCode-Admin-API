package com.david.utils.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 全新的 JSON 导向格式化工具类。
 * 
 * 核心设计原则：
 * 1. 严格 JSON：所有输入必须是合法 JSON 格式
 * 2. 类型安全：根据 JavaType 严格验证和转换
 * 3. 零容错：不合法输入直接抛出异常，不做兜底处理
 * 4. 统一序列化：全部使用 Jackson ObjectMapper
 */
@Component
public class JavaFormationUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 验证 JSON 输入是否符合指定类型，并返回规范化的 JSON 字符串
     * 
     * @param jsonInput 输入的 JSON 字符串
     * @param javaType  目标 Java 类型
     * @return 规范化的 JSON 字符串
     * @throws IllegalArgumentException 当输入不是合法 JSON 或类型不匹配时
     */
    public static String validateAndNormalizeJson(String jsonInput, JavaType javaType) {
        if (jsonInput == null || jsonInput.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON 输入不能为空");
        }

        String trimmed = jsonInput.trim();

        // 首先验证是否是合法的 JSON
        JsonNode jsonNode;
        try {
            jsonNode = MAPPER.readTree(trimmed);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("输入不是合法的 JSON 格式: " + trimmed, e);
        }

        // 验证 JSON 类型是否匹配 JavaType
        validateJsonNodeType(jsonNode, javaType);

        // 返回标准化的 JSON 字符串
        try {
            return MAPPER.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 序列化失败", e);
        }
    }

    /**
     * 将 JSON 字符串转换为 Java 代码中的字面量表达式
     * 
     * @param jsonValue JSON 格式的值
     * @param javaType  目标 Java 类型
     * @return Java 代码字面量表达式
     */
    public static String jsonToJavaLiteral(String jsonValue, JavaType javaType) {
        String normalizedJson = validateAndNormalizeJson(jsonValue, javaType);

        try {
            JsonNode node = MAPPER.readTree(normalizedJson);
            return convertJsonNodeToJavaLiteral(node, javaType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 解析失败", e);
        }
    }

    /**
     * 构建方法参数列表的 Java 代码表达式
     * 
     * @param jsonInputs JSON 格式的输入值列表
     * @param javaTypes  对应的 Java 类型列表
     * @return 形如 "arg1, arg2, arg3" 的参数表达式
     */
    public static String buildParameterList(List<String> jsonInputs, List<JavaType> javaTypes) {
        if (jsonInputs == null || javaTypes == null || jsonInputs.size() != javaTypes.size()) {
            throw new IllegalArgumentException("输入列表与类型列表长度不匹配");
        }

        StringBuilder params = new StringBuilder();
        for (int i = 0; i < jsonInputs.size(); i++) {
            if (i > 0) {
                params.append(", ");
            }
            params.append(jsonToJavaLiteral(jsonInputs.get(i), javaTypes.get(i)));
        }
        return params.toString();
    }

    /**
     * 获取类型的 Java 导入语句
     */
    public static String getRequiredImports(List<JavaType> types) {
        boolean needUtilImport = types.stream().anyMatch(
                type -> type.isList() || type.isArray() && type != JavaType.INT_ARRAY && type != JavaType.DOUBLE_ARRAY);

        return needUtilImport ? "import java.util.*;" : "";
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证 JsonNode 是否符合指定的 JavaType
     */
    private static void validateJsonNodeType(JsonNode node, JavaType javaType) {
        switch (javaType) {
            case INTEGER:
                if (!node.isIntegralNumber()) {
                    throw new IllegalArgumentException("期望整数类型，但输入为: " + node.getNodeType());
                }
                break;

            case DOUBLE:
                if (!node.isNumber()) {
                    throw new IllegalArgumentException("期望数值类型，但输入为: " + node.getNodeType());
                }
                break;

            case STRING:
                if (!node.isTextual()) {
                    throw new IllegalArgumentException("期望字符串类型，但输入为: " + node.getNodeType());
                }
                break;

            case BOOLEAN:
                if (!node.isBoolean()) {
                    throw new IllegalArgumentException("期望布尔类型，但输入为: " + node.getNodeType());
                }
                break;

            case INT_ARRAY:
            case DOUBLE_ARRAY:
            case STRING_ARRAY:
            case BOOLEAN_ARRAY:
            case INT_2D_ARRAY:
            case STRING_2D_ARRAY:
            case BOOLEAN_2D_ARRAY:
            case LIST_INTEGER:
            case LIST_STRING:
            case LIST_LIST_INTEGER:
            case LIST_LIST_STRING:
                if (!node.isArray()) {
                    throw new IllegalArgumentException("期望数组类型，但输入为: " + node.getNodeType());
                }
                validateArrayElements(node, javaType);
                break;

            case VOID:
                throw new IllegalArgumentException("VOID 类型不能用于输入验证");
        }
    }

    /**
     * 验证数组元素类型
     */
    private static void validateArrayElements(JsonNode arrayNode, JavaType javaType) {
        for (JsonNode element : arrayNode) {
            switch (javaType) {
                case INT_ARRAY:
                case LIST_INTEGER:
                    if (!element.isIntegralNumber()) {
                        throw new IllegalArgumentException("整数数组元素必须为整数，但发现: " + element.getNodeType());
                    }
                    break;

                case DOUBLE_ARRAY:
                    if (!element.isNumber()) {
                        throw new IllegalArgumentException("浮点数组元素必须为数值，但发现: " + element.getNodeType());
                    }
                    break;

                case STRING_ARRAY:
                case LIST_STRING:
                    if (!element.isTextual()) {
                        throw new IllegalArgumentException("字符串数组元素必须为字符串，但发现: " + element.getNodeType());
                    }
                    break;

                case BOOLEAN_ARRAY:
                    if (!element.isBoolean()) {
                        throw new IllegalArgumentException("布尔数组元素必须为布尔值，但发现: " + element.getNodeType());
                    }
                    break;

                case INT_2D_ARRAY:
                    if (!element.isArray()) {
                        throw new IllegalArgumentException("二维数组元素必须为数组，但发现: " + element.getNodeType());
                    }
                    validateArrayElements(element, JavaType.INT_ARRAY);
                    break;

                case STRING_2D_ARRAY:
                    if (!element.isArray()) {
                        throw new IllegalArgumentException("二维数组元素必须为数组，但发现: " + element.getNodeType());
                    }
                    validateArrayElements(element, JavaType.STRING_ARRAY);
                    break;

                case BOOLEAN_2D_ARRAY:
                    if (!element.isArray()) {
                        throw new IllegalArgumentException("二维数组元素必须为数组，但发现: " + element.getNodeType());
                    }
                    validateArrayElements(element, JavaType.BOOLEAN_ARRAY);
                    break;

                case LIST_LIST_INTEGER:
                    if (!element.isArray()) {
                        throw new IllegalArgumentException("嵌套列表元素必须为数组，但发现: " + element.getNodeType());
                    }
                    validateArrayElements(element, JavaType.LIST_INTEGER);
                    break;

                case LIST_LIST_STRING:
                    if (!element.isArray()) {
                        throw new IllegalArgumentException("嵌套列表元素必须为数组，但发现: " + element.getNodeType());
                    }
                    validateArrayElements(element, JavaType.LIST_STRING);
                    break;

                // 基础类型不应该在数组元素验证中出现
                case INTEGER:
                case DOUBLE:
                case STRING:
                case BOOLEAN:
                case VOID:
                    throw new IllegalArgumentException("基础类型 " + javaType + " 不能用于数组元素验证");
            }
        }
    }

    /**
     * 将 JsonNode 转换为 Java 字面量表达式
     */
    private static String convertJsonNodeToJavaLiteral(JsonNode node, JavaType javaType) {
        switch (javaType) {
            case INTEGER:
                return String.valueOf(node.asInt());

            case DOUBLE:
                return String.valueOf(node.asDouble());

            case STRING:
                try {
                    return MAPPER.writeValueAsString(node.asText());
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("字符串序列化失败", e);
                }

            case BOOLEAN:
                return String.valueOf(node.asBoolean());

            case INT_ARRAY:
                return buildArrayLiteral(node, "int", javaType);

            case DOUBLE_ARRAY:
                return buildArrayLiteral(node, "double", javaType);

            case STRING_ARRAY:
                return buildArrayLiteral(node, "String", javaType);

            case BOOLEAN_ARRAY:
                return buildArrayLiteral(node, "boolean", javaType);

            case INT_2D_ARRAY:
                return build2DArrayLiteral(node, "int", javaType);

            case STRING_2D_ARRAY:
                return build2DArrayLiteral(node, "String", javaType);

            case BOOLEAN_2D_ARRAY:
                return build2DArrayLiteral(node, "boolean", javaType);

            case LIST_INTEGER:
                return buildListLiteral(node, "Integer");

            case LIST_STRING:
                return buildListLiteral(node, "String");

            case LIST_LIST_INTEGER:
                return buildNestedListLiteral(node, "Integer");

            case LIST_LIST_STRING:
                return buildNestedListLiteral(node, "String");

            default:
                throw new IllegalArgumentException("不支持的类型转换: " + javaType);
        }
    }

    /**
     * 构建一维数组字面量
     */
    private static String buildArrayLiteral(JsonNode arrayNode, String elementType, JavaType javaType) {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(elementType).append("[] {");

        for (int i = 0; i < arrayNode.size(); i++) {
            if (i > 0)
                sb.append(", ");
            JsonNode element = arrayNode.get(i);

            switch (elementType) {
                case "int":
                    sb.append(element.asInt());
                    break;
                case "double":
                    sb.append(element.asDouble());
                    break;
                case "String":
                    try {
                        sb.append(MAPPER.writeValueAsString(element.asText()));
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException("字符串数组元素序列化失败", e);
                    }
                    break;
                case "boolean":
                    sb.append(element.asBoolean());
                    break;
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 构建二维数组字面量
     */
    private static String build2DArrayLiteral(JsonNode arrayNode, String elementType, JavaType javaType) {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(elementType).append("[][] {");

        for (int i = 0; i < arrayNode.size(); i++) {
            if (i > 0)
                sb.append(", ");
            JsonNode subArray = arrayNode.get(i);

            sb.append("{");
            for (int j = 0; j < subArray.size(); j++) {
                if (j > 0)
                    sb.append(", ");
                JsonNode element = subArray.get(j);

                switch (elementType) {
                    case "int":
                        sb.append(element.asInt());
                        break;
                    case "String":
                        try {
                            sb.append(MAPPER.writeValueAsString(element.asText()));
                        } catch (JsonProcessingException e) {
                            throw new IllegalArgumentException("字符串二维数组元素序列化失败", e);
                        }
                        break;
                    case "boolean":
                        sb.append(element.asBoolean());
                        break;
                }
            }
            sb.append("}");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 构建 List 字面量
     */
    private static String buildListLiteral(JsonNode arrayNode, String elementType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Arrays.asList(");

        for (int i = 0; i < arrayNode.size(); i++) {
            if (i > 0)
                sb.append(", ");
            JsonNode element = arrayNode.get(i);

            switch (elementType) {
                case "Integer":
                    sb.append(element.asInt());
                    break;
                case "String":
                    try {
                        sb.append(MAPPER.writeValueAsString(element.asText()));
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException("字符串列表元素序列化失败", e);
                    }
                    break;
            }
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * 构建嵌套 List 字面量
     */
    private static String buildNestedListLiteral(JsonNode arrayNode, String elementType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Arrays.asList(");

        for (int i = 0; i < arrayNode.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(buildListLiteral(arrayNode.get(i), elementType));
        }

        sb.append(")");
        return sb.toString();
    }
}
