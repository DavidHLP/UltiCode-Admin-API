//package com.david.chain.handler.Java;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import com.david.SandBoxSpringApplication;
//import com.david.chain.utils.JudgmentContext;
//import com.david.chain.utils.JudgmentResult;
//import com.david.enums.JudgeStatus;
//import com.david.enums.LanguageType;
//import com.david.testcase.TestCase;
//import com.david.testcase.TestCaseInput;
//import com.david.testcase.TestCaseOutput;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.*;
//
///**
// * Java 判题处理器集成测试
// * 测试完整的 JSON 格式判题流程：格式化 -> 编译 -> 运行 -> 比较
// */
//@SpringBootTest(classes = SandBoxSpringApplication.class)
//public class JavaHandlersTest {
//
//    @Autowired
//    private JavaFormatCodeHandler formatHandler;
//
//    @Autowired
//    private JavaCompileHandler compileHandler;
//
//    @Autowired
//    private JavaRunHandler runHandler;
//
//    @Autowired
//    private JavaCompareHandler compareHandler;
//
//    /**
//     * 测试完整的判题链流程 - 正确答案场景
//     */
//    @Test
//    public void testCompleteJudgeFlow_CorrectAnswer() {
//        // 准备测试数据
//        JudgmentContext context = createTestContext_TwoSum();
//
//        // 1. 格式化代码
//        Boolean formatResult = formatHandler.handleRequest(context);
//        assertTrue(formatResult, "代码格式化应该成功");
//        assertNotNull(context.getRunCode(), "应该生成可运行代码");
//
//        // 调试：打印生成的代码
//        System.out.println("=== Generated Code ===");
//        System.out.println(context.getRunCode());
//        System.out.println("=== End Generated Code ===");
//
//        assertTrue(context.getRunCode().contains("class Solution"), "应该包含 Solution 类");
//        assertTrue(context.getRunCode().contains("class Main"), "应该包含 Main 类");
//
//        // 2. 编译代码
//        Boolean compileResult = compileHandler.handleRequest(context);
//        if (!compileResult) {
//            System.out.println("=== Compile Error Info ===");
//            System.out.println(context.getCompileInfo());
//            System.out.println("=== End Compile Error Info ===");
//        }
//        assertTrue(compileResult, "代码编译应该成功");
//        assertEquals(JudgeStatus.CONTINUE, context.getJudgeStatus(), "编译成功后状态应为 CONTINUE");
//
//        // 3. 运行代码
//        Boolean runResult = runHandler.handleRequest(context);
//        assertTrue(runResult, "代码运行应该成功");
//        assertNotNull(context.getJudgmentResults(), "应该有运行结果");
//        assertFalse(context.getJudgmentResults().isEmpty(), "运行结果不应为空");
//
//        // 4. 比较结果
//        Boolean compareResult = compareHandler.handleRequest(context);
//        assertTrue(compareResult, "结果比较应该成功");
//        assertEquals(JudgeStatus.ACCEPTED, context.getJudgeStatus(), "正确答案应该得到 ACCEPTED");
//
//        // 验证每个测试用例的结果
//        for (JudgmentResult result : context.getJudgmentResults()) {
//            assertEquals(JudgeStatus.ACCEPTED, result.getJudgeStatus(),
//                "每个测试用例都应该通过");
//        }
//    }
//
//    /**
//     * 测试错误答案场景
//     */
//    @Test
//    public void testCompleteJudgeFlow_WrongAnswer() {
//        // 准备错误答案的测试数据 - 只用一个测试用例来简化调试
//        JudgmentContext context = createSimpleWrongAnswerContext();
//
//        // 执行完整流程
//        formatHandler.handleRequest(context);
//        compileHandler.handleRequest(context);
//        runHandler.handleRequest(context);
//        compareHandler.handleRequest(context);
//
//        // 验证结果
//        assertEquals(JudgeStatus.WRONG_ANSWER, context.getJudgeStatus(),
//            "错误答案应该得到 WRONG_ANSWER");
//    }
//
//    /**
//     * 测试编译错误场景
//     */
//    @Test
//    public void testCompleteJudgeFlow_CompileError() {
//        // 准备编译错误的测试数据
//        JudgmentContext context = createTestContext_CompileError();
//
//        // 执行格式化和编译
//        formatHandler.handleRequest(context);
//        Boolean compileResult = compileHandler.handleRequest(context);
//
//        // 验证结果
//        assertFalse(compileResult, "编译错误的代码应该编译失败");
//        assertEquals(JudgeStatus.COMPILE_ERROR, context.getJudgeStatus(),
//            "应该得到 COMPILE_ERROR 状态");
//        assertNotNull(context.getCompileInfo(), "应该有编译错误信息");
//    }
//
//    /**
//     * 创建 Two Sum 问题的测试上下文（正确答案）
//     */
//    private JudgmentContext createTestContext_TwoSum() {
//        // 测试用例数据
//        List<TestCase> testCases = Arrays.asList(
//            createTestCase(1L,
//                Arrays.asList(
//                    createTestCaseInput("[2,7,11,15]", "LIST_INTEGER", 0),
//                    createTestCaseInput("9", "INTEGER", 1)
//                ),
//                createTestCaseOutput("[0,1]", "LIST_INTEGER")
//            ),
//            createTestCase(2L,
//                Arrays.asList(
//                    createTestCaseInput("[3,2,4]", "LIST_INTEGER", 0),
//                    createTestCaseInput("6", "INTEGER", 1)
//                ),
//                createTestCaseOutput("[1,2]", "LIST_INTEGER")
//            )
//        );
//
//        // 正确的 Two Sum 解决方案
//        String solutionCode =
//            "public List<Integer> twoSum(List<Integer> nums, Integer target) {\n" +
//            "    Map<Integer, Integer> map = new HashMap<>();\n" +
//            "    for (int i = 0; i < nums.size(); i++) {\n" +
//            "        int complement = target - nums.get(i);\n" +
//            "        if (map.containsKey(complement)) {\n" +
//            "            return Arrays.asList(map.get(complement), i);\n" +
//            "        }\n" +
//            "        map.put(nums.get(i), i);\n" +
//            "    }\n" +
//            "    return new ArrayList<>();\n" +
//            "}";
//
//        return JudgmentContext.builder()
//            .problemId(1L)
//            .submissionId(1001L)
//            .solutionFunctionName("twoSum")
//            .language(LanguageType.JAVA)
//            .solutionCode(solutionCode)
//            .testCases(testCases)
//            .judgeStatus(JudgeStatus.PENDING)
//            .build();
//    }
//
//    /**
//     * 创建简化的错误答案测试上下文（只有一个测试用例）
//     */
//    private JudgmentContext createSimpleWrongAnswerContext() {
//        List<TestCase> testCases = Arrays.asList(
//            createTestCase(1L,
//                Arrays.asList(
//                    createTestCaseInput("[2,7,11,15]", "LIST_INTEGER", 0),
//                    createTestCaseInput("9", "INTEGER", 1)
//                ),
//                createTestCaseOutput("[0,1]", "LIST_INTEGER")
//            )
//        );
//
//        // 错误的解决方案（总是返回 [0,0]）
//        String wrongSolutionCode =
//            "public List<Integer> twoSum(List<Integer> nums, Integer target) {\n" +
//            "    return Arrays.asList(0, 0);\n" +
//            "}";
//
//        return JudgmentContext.builder()
//            .problemId(1L)
//            .submissionId(1002L)
//            .solutionFunctionName("twoSum")
//            .language(LanguageType.JAVA)
//            .solutionCode(wrongSolutionCode)
//            .testCases(testCases)
//            .judgeStatus(JudgeStatus.PENDING)
//            .build();
//    }
//
//    /**
//     * 创建错误答案的测试上下文
//     */
//    private JudgmentContext createTestContext_WrongAnswer() {
//        List<TestCase> testCases = Arrays.asList(
//            createTestCase(1L,
//                Arrays.asList(
//                    createTestCaseInput("[2,7,11,15]", "LIST_INTEGER", 0),
//                    createTestCaseInput("9", "INTEGER", 1)
//                ),
//                createTestCaseOutput("[0,1]", "LIST_INTEGER")
//            )
//        );
//
//        // 错误的解决方案（总是返回 [0,0]）
//        String wrongSolutionCode =
//            "public List<Integer> twoSum(List<Integer> nums, Integer target) {\n" +
//            "    return Arrays.asList(0, 0);\n" +
//            "}";
//
//        return JudgmentContext.builder()
//            .problemId(1L)
//            .submissionId(1002L)
//            .solutionFunctionName("twoSum")
//            .language(LanguageType.JAVA)
//            .solutionCode(wrongSolutionCode)
//            .testCases(testCases)
//            .judgeStatus(JudgeStatus.PENDING)
//            .build();
//    }
//
//    /**
//     * 创建编译错误的测试上下文
//     */
//    private JudgmentContext createTestContext_CompileError() {
//        List<TestCase> testCases = Arrays.asList(
//            createTestCase(1L,
//                Arrays.asList(
//                    createTestCaseInput("[2,7,11,15]", "LIST_INTEGER", 0),
//                    createTestCaseInput("9", "INTEGER", 1)
//                ),
//                createTestCaseOutput("[0,1]", "LIST_INTEGER")
//            )
//        );
//
//        // 编译错误代码（缺少分号）
//        String compileErrorCode =
//            "public List<Integer> twoSum(List<Integer> nums, Integer target) {\n" +
//            "    // 缺少分号导致编译错误\n" +
//            "    return Arrays.asList(0, 1)\n" +
//            "}";
//
//        return JudgmentContext.builder()
//            .problemId(1L)
//            .submissionId(1003L)
//            .solutionFunctionName("twoSum")
//            .language(LanguageType.JAVA)
//            .solutionCode(compileErrorCode)
//            .testCases(testCases)
//            .judgeStatus(JudgeStatus.PENDING)
//            .build();
//    }
//
//    /**
//     * 创建测试用例
//     */
//    private TestCase createTestCase(Long id, List<TestCaseInput> inputs, TestCaseOutput output) {
//        TestCase testCase = new TestCase();
//        testCase.setId(id);
//        testCase.setTestCaseInput(inputs);
//        testCase.setTestCaseOutput(output);
//        return testCase;
//    }
//
//    /**
//     * 创建测试用例输入
//     */
//    private TestCaseInput createTestCaseInput(String content, String type, Integer order) {
//        TestCaseInput input = new TestCaseInput();
//        input.setInputContent(content);
//        input.setInputType(type);
//        input.setOrderIndex(order);
//        return input;
//    }
//
//    /**
//     * 创建测试用例输出
//     */
//    private TestCaseOutput createTestCaseOutput(String output, String type) {
//        TestCaseOutput testOutput = new TestCaseOutput();
//        testOutput.setOutput(output);
//        testOutput.setOutputType(type);
//        return testOutput;
//    }
//}
