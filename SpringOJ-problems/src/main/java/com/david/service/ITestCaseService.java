package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.TestCase;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  测试用例服务类
 * 支持与判题机文件系统的集成管理
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
     * 创建测试用例并同步生成文件
     * @param testCase 测试用例信息
     * @param inputContent 输入文件内容
     * @param outputContent 输出文件内容
     * @return 创建的测试用例
     * @throws IOException 文件操作异常
     */
    TestCase createTestCaseWithFiles(TestCase testCase, String inputContent, String outputContent) throws IOException;

    /**
     * 更新测试用例并同步更新文件
     * @param testCase 测试用例信息
     * @param inputContent 输入文件内容（可为null表示不更新）
     * @param outputContent 输出文件内容（可为null表示不更新）
     * @return 更新的测试用例
     * @throws IOException 文件操作异常
     */
    TestCase updateTestCaseWithFiles(TestCase testCase, String inputContent, String outputContent) throws IOException;

    /**
     * 删除测试用例并清理相关文件
     * @param testCaseId 测试用例ID
     * @return 是否删除成功
     * @throws IOException 文件操作异常
     */
    boolean deleteTestCaseWithFiles(Long testCaseId) throws IOException;

    /**
     * 批量导入测试用例
     * @param problemId 题目ID
     * @param testCaseDataList 测试用例数据列表（包含输入输出内容）
     * @return 导入的测试用例列表
     * @throws IOException 文件操作异常
     */
    List<TestCase> batchImportTestCases(Long problemId, List<TestCaseData> testCaseDataList) throws IOException;

    /**
     * 获取测试用例文件内容
     * @param testCaseId 测试用例ID
     * @return 测试用例文件内容
     * @throws IOException 文件读取异常
     */
    TestCaseContent getTestCaseContent(Long testCaseId) throws IOException;

    /**
     * 验证测试用例文件完整性
     * @param problemId 题目ID
     * @return 验证结果
     */
    TestCaseValidationResult validateTestCaseFiles(Long problemId);

    /**
     * 测试用例数据传输对象
     */
    class TestCaseData {
        private String inputFileName;
        private String outputFileName;
        private String inputContent;
        private String outputContent;
        private Integer score;
        private Boolean isSample;

        // Getters and Setters
        public String getInputFileName() { return inputFileName; }
        public void setInputFileName(String inputFileName) { this.inputFileName = inputFileName; }
        public String getOutputFileName() { return outputFileName; }
        public void setOutputFileName(String outputFileName) { this.outputFileName = outputFileName; }
        public String getInputContent() { return inputContent; }
        public void setInputContent(String inputContent) { this.inputContent = inputContent; }
        public String getOutputContent() { return outputContent; }
        public void setOutputContent(String outputContent) { this.outputContent = outputContent; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public Boolean getIsSample() { return isSample; }
        public void setIsSample(Boolean isSample) { this.isSample = isSample; }
    }

    /**
     * 测试用例内容对象
     */
    class TestCaseContent {
        private String inputContent;
        private String outputContent;

        public TestCaseContent(String inputContent, String outputContent) {
            this.inputContent = inputContent;
            this.outputContent = outputContent;
        }

        // Getters and Setters
        public String getInputContent() { return inputContent; }
        public void setInputContent(String inputContent) { this.inputContent = inputContent; }
        public String getOutputContent() { return outputContent; }
        public void setOutputContent(String outputContent) { this.outputContent = outputContent; }
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
