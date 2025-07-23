package com.david.service;

import java.io.IOException;

/**
 * 测试用例文件服务接口
 * 负责管理和读取测试用例的输入输出文件
 */
public interface ITestCaseFileService {
    
    /**
     * 读取测试用例输入文件内容
     * @param problemId 题目ID
     * @param inputFileName 输入文件名
     * @return 文件内容字符串
     * @throws IOException 文件读取异常
     */
    String readInputFile(Long problemId, String inputFileName) throws IOException;
    
    /**
     * 读取测试用例输出文件内容
     * @param problemId 题目ID
     * @param outputFileName 输出文件名
     * @return 文件内容字符串
     * @throws IOException 文件读取异常
     */
    String readOutputFile(Long problemId, String outputFileName) throws IOException;
    
    /**
     * 保存测试用例输入文件
     * @param problemId 题目ID
     * @param inputFileName 输入文件名
     * @param content 文件内容
     * @throws IOException 文件写入异常
     */
    void saveInputFile(Long problemId, String inputFileName, String content) throws IOException;
    
    /**
     * 保存测试用例输出文件
     * @param problemId 题目ID
     * @param outputFileName 输出文件名
     * @param content 文件内容
     * @throws IOException 文件写入异常
     */
    void saveOutputFile(Long problemId, String outputFileName, String content) throws IOException;
    
    /**
     * 检查测试用例文件是否存在
     * @param problemId 题目ID
     * @param fileName 文件名
     * @param isInputFile 是否为输入文件
     * @return 文件是否存在
     */
    boolean fileExists(Long problemId, String fileName, boolean isInputFile);
}
