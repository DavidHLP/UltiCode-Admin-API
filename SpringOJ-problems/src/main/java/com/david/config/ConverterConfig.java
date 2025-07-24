package com.david.config;

import com.david.judge.enums.CategoryType;
import com.david.judge.enums.ProblemDifficulty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConverterConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToCategoryTypeConverter());
        registry.addConverter(new StringToProblemDifficultyConverter());
    }

    /**
     * String到CategoryType的转换器
     */
    public static class StringToCategoryTypeConverter implements Converter<String, CategoryType> {
        @Override
        public CategoryType convert(String source) {
            try {
                return CategoryType.fromString(source);
            } catch (IllegalArgumentException e) {
                // 如果直接转换失败，尝试按枚举名称转换
                try {
                    return CategoryType.valueOf(source.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("无法转换为CategoryType: " + source);
                }
            }
        }
    }

    /**
     * String到ProblemDifficulty的转换器
     */
    public static class StringToProblemDifficultyConverter implements Converter<String, ProblemDifficulty> {
        @Override
        public ProblemDifficulty convert(String source) {
            try {
                return ProblemDifficulty.fromString(source);
            } catch (IllegalArgumentException e) {
                // 如果直接转换失败，尝试按枚举名称转换
                try {
                    return ProblemDifficulty.valueOf(source.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("无法转换为ProblemDifficulty: " + source);
                }
            }
        }
    }
}
