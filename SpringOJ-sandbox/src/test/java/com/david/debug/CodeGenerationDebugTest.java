package com.david.debug;

import com.david.chain.handler.Java.JavaFormatCodeHandler;
import com.david.chain.utils.JudgmentContext;
import com.david.enums.JudgeStatus;
import com.david.enums.LanguageType;
import com.david.testcase.TestCase;
import com.david.testcase.TestCaseInput;
import com.david.testcase.TestCaseOutput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * 专门用于调试代码生成问题的测试类
 */
@SpringBootTest
public class CodeGenerationDebugTest {
    
    @Autowired
    private JavaFormatCodeHandler formatHandler;
    
    @Test
    public void testGenerateSimpleCode() {
        // 创建一个最简单的测试用例
        JudgmentContext context = createSimpleContext();
        
        // 执行格式化
        formatHandler.handleRequest(context);
        
        // 打印生成的代码
        System.out.println("=== 生成的完整代码 ===");
        System.out.println(context.getRunCode());
        System.out.println("=== 代码结束 ===");
        
        // 验证代码不为空
        assert context.getRunCode() != null && !context.getRunCode().isEmpty();
    }
    
    private JudgmentContext createSimpleContext() {
        // 创建测试用例输入
        TestCaseInput input1 = TestCaseInput.builder()
                .inputContent("[1,2,3]")
                .inputType("LIST_INTEGER")
                .orderIndex(0)
                .build();
        
        TestCaseInput input2 = TestCaseInput.builder()
                .inputContent("6")
                .inputType("INTEGER")
                .orderIndex(1)
                .build();
        
        // 创建测试用例输出
        TestCaseOutput output = TestCaseOutput.builder()
                .output("[0,1]")
                .outputType("LIST_INTEGER")
                .build();
        
        // 创建测试用例
        TestCase testCase = TestCase.builder()
                .id(1L)
                .testCaseInput(Arrays.asList(input1, input2))
                .testCaseOutput(output)
                .build();
        
        // 错误的解决方案代码
        String wrongSolutionCode = 
            "public List<Integer> twoSum(List<Integer> nums, Integer target) {\n" +
            "    return Arrays.asList(0, 0);\n" +
            "}";
        
        return JudgmentContext.builder()
                .problemId(1L)
                .submissionId(1L)
                .solutionFunctionName("twoSum")
                .language(LanguageType.JAVA)
                .solutionCode(wrongSolutionCode)
                .testCases(Arrays.asList(testCase))
                .judgeStatus(JudgeStatus.PENDING)
                .build();
    }
}
