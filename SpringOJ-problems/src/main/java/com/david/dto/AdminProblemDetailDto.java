package com.david.dto;

import com.david.entity.*;
import lombok.Data;

import java.util.List;

@Data
public class AdminProblemDetailDto {
    private Problem problem;
    private List<ProblemLocale> locales;
    private List<ProblemLanguageConfig> languageConfigs;
    private ProblemStat stat;

    private List<Tag> allTags;
    private List<Long> selectedTagIds;

    private List<TestcaseGroupDto> groups;

    @Data
    public static class TestcaseGroupDto {
        private TestcaseGroup group;
        private List<TestcaseDto> testcases;
    }

    @Data
    public static class TestcaseDto {
        private Testcase testcase;
        private List<TestcaseStep> steps;
    }
}

