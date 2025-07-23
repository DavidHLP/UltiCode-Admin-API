package com.david.service.impl;

import com.david.service.ITestCaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 测试用例文件服务实现类
 * 基于本地文件系统管理测试用例文件
 */
@Slf4j
@Service
public class TestCaseFileServiceImpl implements ITestCaseFileService {
    
    @Value("${judge.testcase.base-path:/tmp/oj-testcases}")
    private String testCaseBasePath;
    
    private static final String INPUT_DIR = "input";
    private static final String OUTPUT_DIR = "output";
    
    @Override
    public String readInputFile(Long problemId, String inputFileName) throws IOException {
        Path filePath = getInputFilePath(problemId, inputFileName);
        if (!Files.exists(filePath)) {
            throw new IOException("输入文件不存在: " + filePath);
        }
        String content = Files.readString(filePath);
        log.debug("读取输入文件成功: problemId={}, fileName={}, size={}", problemId, inputFileName, content.length());
        return content;
    }
    
    @Override
    public String readOutputFile(Long problemId, String outputFileName) throws IOException {
        Path filePath = getOutputFilePath(problemId, outputFileName);
        if (!Files.exists(filePath)) {
            throw new IOException("输出文件不存在: " + filePath);
        }
        String content = Files.readString(filePath);
        log.debug("读取输出文件成功: problemId={}, fileName={}, size={}", problemId, outputFileName, content.length());
        return content;
    }
    
    @Override
    public void saveInputFile(Long problemId, String inputFileName, String content) throws IOException {
        Path filePath = getInputFilePath(problemId, inputFileName);
        ensureDirectoryExists(filePath.getParent());
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.info("保存输入文件成功: problemId={}, fileName={}, size={}", problemId, inputFileName, content.length());
    }
    
    @Override
    public void saveOutputFile(Long problemId, String outputFileName, String content) throws IOException {
        Path filePath = getOutputFilePath(problemId, outputFileName);
        ensureDirectoryExists(filePath.getParent());
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.info("保存输出文件成功: problemId={}, fileName={}, size={}", problemId, outputFileName, content.length());
    }
    
    @Override
    public boolean fileExists(Long problemId, String fileName, boolean isInputFile) {
        Path filePath = isInputFile ? getInputFilePath(problemId, fileName) : getOutputFilePath(problemId, fileName);
        return Files.exists(filePath);
    }
    
    /**
     * 获取输入文件路径
     */
    private Path getInputFilePath(Long problemId, String fileName) {
        return Paths.get(testCaseBasePath, "problem_" + problemId, INPUT_DIR, fileName);
    }
    
    /**
     * 获取输出文件路径
     */
    private Path getOutputFilePath(Long problemId, String fileName) {
        return Paths.get(testCaseBasePath, "problem_" + problemId, OUTPUT_DIR, fileName);
    }
    
    /**
     * 确保目录存在，如果不存在则创建
     */
    private void ensureDirectoryExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.debug("创建目录: {}", directory);
        }
    }
}
