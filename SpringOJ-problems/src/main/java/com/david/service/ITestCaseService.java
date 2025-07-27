package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.TestCase;

import java.util.List;

/**
 * <p>
 *  测试用例服务类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
public interface ITestCaseService extends IService<TestCase> {

    /**
     * 根据题目ID获取测试用例列表
     * @param problemId 题目ID
     * @return 测试用例列表
     */
    List<TestCase> getTestCasesByProblemId(Long problemId);

    /**
     * 批量导入测试用例
     * @param problemId 题目ID
     * @param testCaseDataList 测试用例数据列表（包含输入输出内容）
     * @return 导入的测试用例列表
     */
    List<TestCase> batchImportTestCases(Long problemId, List<TestCaseData> testCaseDataList);

    /**
     * 测试用例数据传输对象
     */
    class TestCaseData {
        private String input;
        private String output;
        private Integer score;
        private Boolean isSample;

        // Getters and Setters
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public Boolean getIsSample() { return isSample; }
        public void setIsSample(Boolean isSample) { this.isSample = isSample; }
    }

    /**
     * 测试用例验证结果
     */
    class TestCaseValidationResult {
        private boolean valid;
        private List<String> missingFiles;
        private List<String> errors;

        public TestCaseValidationResult(boolean valid, List<String> missingFiles, List<String> errors) {
            this.valid = valid;
            this.missingFiles = missingFiles;
            this.errors = errors;
        }

        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getMissingFiles() { return missingFiles; }
        public void setMissingFiles(List<String> missingFiles) { this.missingFiles = missingFiles; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}
