package com.david.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.TestCase;
import com.david.mapper.TestCaseMapper;
import com.david.service.ITestCaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  测试用例服务实现类
 * 支持与判题机文件系统的集成管理
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@Slf4j
@Service
public class TestCaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements ITestCaseService {

    @Value("${judge.testcase.base-path:/tmp/oj-testcases}")
    private String testCaseBasePath;
    
    private static final String INPUT_DIR = "input";
    private static final String OUTPUT_DIR = "output";

    @Override
    public List<TestCase> getTestCasesByProblemId(Long problemId) {
        QueryWrapper<TestCase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemId);
        queryWrapper.orderByAsc("id");
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCase createTestCaseWithFiles(TestCase testCase, String inputContent, String outputContent) throws IOException {
        // 1. 保存数据库记录
        save(testCase);
        log.info("创建测试用例数据库记录成功: testCaseId={}, problemId={}", testCase.getId(), testCase.getProblemId());
        
        try {
            // 2. 保存文件
            saveTestCaseFiles(testCase.getProblemId(), testCase.getInputFile(), inputContent, 
                            testCase.getOutputFile(), outputContent);
            log.info("创建测试用例文件成功: testCaseId={}", testCase.getId());
            
            return testCase;
        } catch (IOException e) {
            // 文件创建失败，回滚数据库操作
            removeById(testCase.getId());
            log.error("创建测试用例文件失败，已回滚数据库操作: testCaseId={}", testCase.getId(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCase updateTestCaseWithFiles(TestCase testCase, String inputContent, String outputContent) throws IOException {
        // 1. 更新数据库记录
        updateById(testCase);
        log.info("更新测试用例数据库记录成功: testCaseId={}", testCase.getId());
        
        // 2. 更新文件（如果提供了内容）
        if (inputContent != null || outputContent != null) {
            try {
                if (inputContent != null) {
                    saveInputFile(testCase.getProblemId(), testCase.getInputFile(), inputContent);
                }
                if (outputContent != null) {
                    saveOutputFile(testCase.getProblemId(), testCase.getOutputFile(), outputContent);
                }
                log.info("更新测试用例文件成功: testCaseId={}", testCase.getId());
            } catch (IOException e) {
                log.error("更新测试用例文件失败: testCaseId={}", testCase.getId(), e);
                throw e;
            }
        }
        
        return testCase;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTestCaseWithFiles(Long testCaseId) throws IOException {
        TestCase testCase = getById(testCaseId);
        if (testCase == null) {
            log.warn("测试用例不存在: testCaseId={}", testCaseId);
            return false;
        }
        
        // 1. 删除数据库记录
        boolean dbResult = removeById(testCaseId);
        if (!dbResult) {
            log.error("删除测试用例数据库记录失败: testCaseId={}", testCaseId);
            return false;
        }
        
        // 2. 删除文件（即使失败也不影响数据库操作）
        try {
            deleteTestCaseFiles(testCase.getProblemId(), testCase.getInputFile(), testCase.getOutputFile());
            log.info("删除测试用例成功: testCaseId={}", testCaseId);
        } catch (IOException e) {
            log.warn("删除测试用例文件失败，但数据库记录已删除: testCaseId={}", testCaseId, e);
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TestCase> batchImportTestCases(Long problemId, List<TestCaseData> testCaseDataList) throws IOException {
        List<TestCase> createdTestCases = new ArrayList<>();
        
        for (TestCaseData data : testCaseDataList) {
            TestCase testCase = new TestCase();
            testCase.setProblemId(problemId);
            testCase.setInputFile(data.getInputFileName());
            testCase.setOutputFile(data.getOutputFileName());
            testCase.setScore(data.getScore() != null ? data.getScore() : 10);
            testCase.setSample(data.getIsSample() != null ? data.getIsSample() : false);
            
            TestCase created = createTestCaseWithFiles(testCase, data.getInputContent(), data.getOutputContent());
            createdTestCases.add(created);
        }
        
        log.info("批量导入测试用例成功: problemId={}, count={}", problemId, createdTestCases.size());
        return createdTestCases;
    }

    @Override
    public TestCaseContent getTestCaseContent(Long testCaseId) throws IOException {
        TestCase testCase = getById(testCaseId);
        if (testCase == null) {
            throw new IllegalArgumentException("测试用例不存在: " + testCaseId);
        }
        
        String inputContent = readInputFile(testCase.getProblemId(), testCase.getInputFile());
        String outputContent = readOutputFile(testCase.getProblemId(), testCase.getOutputFile());
        
        return new TestCaseContent(inputContent, outputContent);
    }

    @Override
    public TestCaseValidationResult validateTestCaseFiles(Long problemId) {
        List<TestCase> testCases = getTestCasesByProblemId(problemId);
        List<String> missingFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (TestCase testCase : testCases) {
            // 检查输入文件
            if (!fileExists(problemId, testCase.getInputFile(), true)) {
                missingFiles.add("输入文件: " + testCase.getInputFile());
            }
            
            // 检查输出文件
            if (!fileExists(problemId, testCase.getOutputFile(), false)) {
                missingFiles.add("输出文件: " + testCase.getOutputFile());
            }
            
            // 检查文件内容是否可读
            try {
                readInputFile(problemId, testCase.getInputFile());
                readOutputFile(problemId, testCase.getOutputFile());
            } catch (IOException e) {
                errors.add("文件读取错误: " + e.getMessage());
            }
        }
        
        boolean valid = missingFiles.isEmpty() && errors.isEmpty();
        return new TestCaseValidationResult(valid, missingFiles, errors);
    }

    // ========== 私有方法 ==========

    private void saveTestCaseFiles(Long problemId, String inputFileName, String inputContent, 
                                  String outputFileName, String outputContent) throws IOException {
        saveInputFile(problemId, inputFileName, inputContent);
        saveOutputFile(problemId, outputFileName, outputContent);
    }

    private void saveInputFile(Long problemId, String inputFileName, String content) throws IOException {
        Path filePath = getInputFilePath(problemId, inputFileName);
        ensureDirectoryExists(filePath.getParent());
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("保存输入文件: problemId={}, fileName={}, size={}", problemId, inputFileName, content.length());
    }

    private void saveOutputFile(Long problemId, String outputFileName, String content) throws IOException {
        Path filePath = getOutputFilePath(problemId, outputFileName);
        ensureDirectoryExists(filePath.getParent());
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("保存输出文件: problemId={}, fileName={}, size={}", problemId, outputFileName, content.length());
    }

    private String readInputFile(Long problemId, String inputFileName) throws IOException {
        Path filePath = getInputFilePath(problemId, inputFileName);
        if (!Files.exists(filePath)) {
            throw new IOException("输入文件不存在: " + filePath);
        }
        return Files.readString(filePath);
    }

    private String readOutputFile(Long problemId, String outputFileName) throws IOException {
        Path filePath = getOutputFilePath(problemId, outputFileName);
        if (!Files.exists(filePath)) {
            throw new IOException("输出文件不存在: " + filePath);
        }
        return Files.readString(filePath);
    }

    private void deleteTestCaseFiles(Long problemId, String inputFileName, String outputFileName) throws IOException {
        Path inputPath = getInputFilePath(problemId, inputFileName);
        Path outputPath = getOutputFilePath(problemId, outputFileName);
        
        if (Files.exists(inputPath)) {
            Files.delete(inputPath);
            log.debug("删除输入文件: {}", inputPath);
        }
        
        if (Files.exists(outputPath)) {
            Files.delete(outputPath);
            log.debug("删除输出文件: {}", outputPath);
        }
    }

    private boolean fileExists(Long problemId, String fileName, boolean isInputFile) {
        Path filePath = isInputFile ? getInputFilePath(problemId, fileName) : getOutputFilePath(problemId, fileName);
        return Files.exists(filePath);
    }

    private Path getInputFilePath(Long problemId, String fileName) {
        return Paths.get(testCaseBasePath, "problem_" + problemId, INPUT_DIR, fileName);
    }

    private Path getOutputFilePath(Long problemId, String fileName) {
        return Paths.get(testCaseBasePath, "problem_" + problemId, OUTPUT_DIR, fileName);
    }

    private void ensureDirectoryExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.debug("创建目录: {}", directory);
        }
    }
}
