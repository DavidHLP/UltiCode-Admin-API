package com.david.utils;

import com.david.judge.enums.LanguageType;
import com.david.utils.java.JavaCodeUtils;
import com.david.utils.java.JavaType;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeUtils {
    private final JavaCodeUtils javaCodeUtils;
    public String generateSolutionClass(LanguageType language,SolutionDto solutionDto) {
        return switch (language) {
            case JAVA -> javaCodeUtils.generateSolutionClass(solutionDto.getSolutionFunctionName(), JavaType.valueOf(solutionDto.getOutputType()).getTypeName());
            default -> throw new IllegalArgumentException("暂不支持的语言生成模板: " + language);
        };
    }
}