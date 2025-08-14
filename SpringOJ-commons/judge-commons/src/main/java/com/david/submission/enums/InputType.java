package com.david.submission.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum InputType {
    // ========= 标量 =========
    STRING("字符串"),
    CHAR("字符"),
    BOOLEAN("布尔"),
    BYTE("字节"),
    SHORT("短整型"),
    INT("整型"),
    LONG("长整型"),
    FLOAT("单精度浮点"),
    DOUBLE("双精度浮点"),
    BIG_INTEGER("大整数"),
    BIG_DECIMAL("高精度小数"),

    // ========= 一维数组 =========
    STRING_ARRAY("字符串数组"),
    CHAR_ARRAY("字符数组"),
    BOOLEAN_ARRAY("布尔数组"),
    INT_ARRAY("整型数组"),
    LONG_ARRAY("长整型数组"),
    FLOAT_ARRAY("单精度数组"),
    DOUBLE_ARRAY("双精度数组"),

    // ========= 二维数组 / 矩阵 =========
    STRING_2D_ARRAY("字符串矩阵"),
    CHAR_2D_ARRAY("字符矩阵"),
    BOOLEAN_2D_ARRAY("布尔矩阵"),
    INT_2D_ARRAY("整型矩阵"),
    LONG_2D_ARRAY("长整型矩阵"),
    FLOAT_2D_ARRAY("单精度矩阵"),
    DOUBLE_2D_ARRAY("双精度矩阵"),

    // ========= List =========
    LIST_STRING("字符串列表"),
    LIST_CHAR("字符列表"),
    LIST_BOOLEAN("布尔列表"),
    LIST_INT("整型列表"),
    LIST_LONG("长整型列表"),
    LIST_FLOAT("单精度列表"),
    LIST_DOUBLE("双精度列表"),
    LIST_LIST_INT("整型列表的列表"),
    LIST_LIST_STRING("字符串列表的列表"),

    // ========= Set =========
    SET_INT("整型集合"),
    SET_LONG("长整型集合"),
    SET_STRING("字符串集合"),

    // ========= Map / HashMap =========
    MAP_STRING_STRING("Map<String,String>"),
    MAP_STRING_INT("Map<String,Integer>"),
    MAP_INT_INT("Map<Integer,Integer>"),
    MAP_INT_STRING("Map<Integer,String>"),

    // ========= 链表/树/图 等常见结构 =========
    LIST_NODE_INT("链表(整型)"),
    LIST_NODE_STRING("链表(字符串)"),
    TREE_NODE_INT("二叉树(整型)"),
    TREE_NODE_STRING("二叉树(字符串)"),
    GRAPH_ADJ_LIST("图-邻接表"),
    GRAPH_ADJ_MATRIX("图-邻接矩阵"),

    // ========= 其他与扩展 =========
    INTERVAL_INT_ARRAY("区间数组<int[]>"),
    POINT_INT("点(x,y)-整型"),
    CUSTOM("自定义");

    @EnumValue
    private final String name;
    private final String description;

    InputType(String description) {
        this.name = name();
        this.description = description;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InputType fromString(String value) {
        for (InputType t : InputType.values()) {
            if (t.name.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("未知的输入类型: " + value);
    }

    @JsonValue
    public String getName() {
        return name;
    }

	@Override
    public String toString() {
        return this.name;
    }
}
