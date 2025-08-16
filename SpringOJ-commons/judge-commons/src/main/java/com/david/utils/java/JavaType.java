package com.david.utils.java;

import lombok.Getter;

/**
 * Java 类型名的统一声明与映射工具。
 * 注意：此枚举仅存储类型名字符串（便于模板/动态代码生成），不直接引用 Class 对象，避免不必要的类依赖。
 */
@Getter
public enum JavaType {
    // ========= 基本类型（primitive） =========
    BOOLEAN("boolean"),
    BYTE("byte"),
    CHAR("char"),
    SHORT("short"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),

    // ========= 常用引用类型（包装/标准） =========
    BOOLEAN_WRAPPER("Boolean"),
    BYTE_WRAPPER("Byte"),
    CHAR_WRAPPER("Character"),
    SHORT_WRAPPER("Short"),
    INT_WRAPPER("Integer"),
    LONG_WRAPPER("Long"),
    FLOAT_WRAPPER("Float"),
    DOUBLE_WRAPPER("Double"),
    STRING("String"),
    BIG_INTEGER("BigInteger"),
    BIG_DECIMAL("BigDecimal"),
    OBJECT("Object"),
    VOID("void"),

    // ========= 一维数组 =========
    BOOLEAN_ARRAY("boolean[]"),
    BYTE_ARRAY("byte[]"),
    CHAR_ARRAY("char[]"),
    SHORT_ARRAY("short[]"),
    INT_ARRAY("int[]"),
    LONG_ARRAY("long[]"),
    FLOAT_ARRAY("float[]"),
    DOUBLE_ARRAY("double[]"),
    STRING_ARRAY("String[]"),

    // ========= 二维数组 =========
    BOOLEAN_2D_ARRAY("boolean[][]"),
    CHAR_2D_ARRAY("char[][]"),
    INT_2D_ARRAY("int[][]"),
    LONG_2D_ARRAY("long[][]"),
    FLOAT_2D_ARRAY("float[][]"),
    DOUBLE_2D_ARRAY("double[][]"),
    STRING_2D_ARRAY("String[][]"),

    // ========= 常用集合/映射（使用全限定包名，避免与用户代码冲突） =========
    LIST_STRING("List<String>"),
    LIST_CHAR("List<Character>"),
    LIST_BOOLEAN("List<Boolean>"),
    LIST_INT("List<Integer>"),
    LIST_LONG("List<Long>"),
    LIST_FLOAT("List<Float>"),
    LIST_DOUBLE("List<Double>"),
    LIST_LIST_INT("List<List<Integer>>"),
    LIST_LIST_STRING("List<List<String>>"),

    SET_INT("Set<Integer>"),
    SET_LONG("Set<Long>"),
    SET_STRING("Set<String>"),

    MAP_STRING_STRING("Map<String,String>"),
    MAP_STRING_INT("Map<String,Integer>"),
    MAP_INT_INT("Map<Integer,Integer>"),
    MAP_INT_STRING("Map<Integer,String>");

    private final String typeName;

    JavaType(String typeName) {
        this.typeName = typeName;
    }

}
