package com.david.enums;

import com.david.enums.interfaces.LimitType;
import com.david.problem.enums.ProblemDifficulty;

/**
 * Java 语言的默认资源限制等级。
 * <p>以秒（s）与兆字节（MB）为单位，提供对 {@link LimitType} 的实现。</p>
 */
public enum JavaLimitType implements LimitType {
    /** 简单题：时间 1s，内存 256MB */
    EASY(1, 256),
    /** 中等题：时间 2s，内存 256MB */
    MEDIUM(2, 256),
    /** 困难题：时间 3s，内存 256MB */
    HARD(3, 256);

    /** 时间限制（秒） */
    private final int timeLimitSeconds;
    /** 内存限制（MB） */
    private final int memoryLimitMB;

    JavaLimitType(int timeLimitSeconds, int memoryLimitMB) {
        this.timeLimitSeconds = timeLimitSeconds;
        this.memoryLimitMB = memoryLimitMB;
    }

    /**
     * 根据题目难度解析对应的限制。
     * 若 difficulty 为空或不匹配，回退为 MEDIUM。
     *
     * @param difficulty 题目难度
     * @return 对应的 JavaLimitType
     */
    public static JavaLimitType fromDifficulty(ProblemDifficulty difficulty) {
        if (difficulty == null) return MEDIUM;
	    return switch (difficulty) {
		    case EASY -> EASY;
		    case HARD -> HARD;
		    default -> MEDIUM;
	    };
    }

    /**
     * 从字符串解析限制等级（不区分大小写），不匹配时回退为 MEDIUM。
     * @param name 名称：EASY/MEDIUM/HARD
     * @return 对应的 JavaLimitType
     */
    public static JavaLimitType fromString(String name) {
        if (name == null) return MEDIUM;
	    return switch (name.trim().toUpperCase()) {
		    case "EASY" -> EASY;
		    case "HARD" -> HARD;
		    default -> MEDIUM;
	    };
    }

    /**
     * 获取时间限制（秒）。
     * @return 时间限制（秒）
     */
    @Override
    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    /**
     * 获取内存限制（MB）。
     * @return 内存限制（MB）
     */
    @Override
    public int getMemoryLimitMB() {
        return memoryLimitMB;
    }
}

