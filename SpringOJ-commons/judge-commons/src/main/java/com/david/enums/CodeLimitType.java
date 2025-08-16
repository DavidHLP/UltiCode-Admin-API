package com.david.enums;

import com.david.enums.interfaces.LimitType;
import com.david.problem.enums.ProblemDifficulty;

import java.util.Objects;
import java.util.function.Function;

/**
 * 语言级资源限制路由器。
 * <p>根据语言与题目难度解析为具体的 {@link LimitType} 实现。</p>
 */
public enum CodeLimitType {
    /** Java 语言限制：通过难度映射到 {@link JavaLimitType} */
    JAVA(JavaLimitType::fromDifficulty);

    /** 语言 -> (难度 -> 限制) 的解析函数 */
    private final Function<ProblemDifficulty, LimitType> resolver;

    CodeLimitType(Function<ProblemDifficulty, LimitType> resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver cannot be null");
    }

    /**
     * 由语言类型解析枚举项。
     *
     * @param language 语言
     * @return 对应的 CodeLimitType，未知语言暂时回退为 JAVA
     */
    public static CodeLimitType fromLanguage(LanguageType language) {
        if (language == null) return JAVA;
	    return switch (language) {
		    case JAVA -> JAVA;
		    default ->
			    // 可在此扩展其他语言
				    JAVA;
	    };
    }

    /**
     * 按难度获取对应限制。
     *
     * @param difficulty 题目难度，null 时使用默认难度 MEDIUM
     * @return 具体限制实现，非 null
     */
    public LimitType getLimit(ProblemDifficulty difficulty) {
        ProblemDifficulty d = (difficulty == null) ? ProblemDifficulty.MEDIUM : difficulty;
        return resolver.apply(d);
    }

    /**
     * 获取默认限制（默认难度 MEDIUM）。
     * @return 默认限制
     */
    public LimitType getDefaultLimit() {
        return getLimit(ProblemDifficulty.MEDIUM);
    }
}
