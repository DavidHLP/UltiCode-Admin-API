package com.david.utils;

import com.david.service.ITestCaseFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 测试用例初始化工具
 * 用于初始化题目的测试用例文件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestCaseInitializer implements CommandLineRunner {
    
    private final ITestCaseFileService testCaseFileService;
    
    @Override
    public void run(String... args) throws Exception {
        // 初始化两数之和题目的测试用例（假设题目ID为1）
        initializeTwoSumTestCases(1L);
    }
    
    /**
     * 初始化两数之和题目的测试用例
     */
    private void initializeTwoSumTestCases(Long problemId) {
        log.info("开始初始化两数之和题目测试用例: problemId={}", problemId);
        
        try {
            // 测试用例1：基本示例
            saveTestCase(problemId, "input1.txt", "4 9\n2 7 11 15", "output1.txt", "0 1");
            
            // 测试用例2：另一个基本示例
            saveTestCase(problemId, "input2.txt", "3 6\n3 2 4", "output2.txt", "1 2");
            
            // 测试用例3：相同元素
            saveTestCase(problemId, "input3.txt", "2 6\n3 3", "output3.txt", "0 1");
            
            // 测试用例4：大数组
            saveTestCase(problemId, "input4.txt", "6 10\n1 5 3 7 9 2", "output4.txt", "1 3");
            
            // 测试用例5：负数
            saveTestCase(problemId, "input5.txt", "4 0\n-3 4 3 90", "output5.txt", "0 2");
            
            // 测试用例6：边界情况
            saveTestCase(problemId, "input6.txt", "5 8\n2 5 5 11 1", "output6.txt", "1 2");
            
            // 测试用例7：最小数组
            saveTestCase(problemId, "input7.txt", "2 3\n1 2", "output7.txt", "0 1");
            
            // 测试用例8：零和负数混合
            saveTestCase(problemId, "input8.txt", "4 -1\n-1 0 1 2", "output8.txt", "0 1");
            
            log.info("两数之和题目测试用例初始化完成: problemId={}", problemId);
            
        } catch (IOException e) {
            log.error("初始化测试用例失败: problemId={}", problemId, e);
        }
    }
    
    /**
     * 保存单个测试用例
     */
    private void saveTestCase(Long problemId, String inputFileName, String inputContent, 
                             String outputFileName, String outputContent) throws IOException {
        
        // 检查文件是否已存在，避免重复创建
        if (!testCaseFileService.fileExists(problemId, inputFileName, true)) {
            testCaseFileService.saveInputFile(problemId, inputFileName, inputContent);
            log.debug("保存输入文件: problemId={}, fileName={}", problemId, inputFileName);
        }
        
        if (!testCaseFileService.fileExists(problemId, outputFileName, false)) {
            testCaseFileService.saveOutputFile(problemId, outputFileName, outputContent);
            log.debug("保存输出文件: problemId={}, fileName={}", problemId, outputFileName);
        }
    }
}
