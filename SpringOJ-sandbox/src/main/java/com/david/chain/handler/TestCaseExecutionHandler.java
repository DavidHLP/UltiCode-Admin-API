package com.david.chain.handler;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.david.chain.JudgeHandler;
import com.david.chain.utils.JudgeContext;
import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.dto.TestCaseResult;
import com.david.judge.enums.JudgeStatus;
import com.david.strategy.LanguageStrategy;
import com.david.strategy.records.ExecutionResult;
import com.david.strategy.utils.ExecutionConfig;
import com.david.strategy.utils.MemoryMonitor;
import com.david.strategy.utils.OutputProcessingConfig;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试用例执行处理器 - 通用化
 */
@Slf4j
@Component
public class TestCaseExecutionHandler extends JudgeHandler {

	@Override
	protected JudgeResult doHandle(JudgeContext context) {
		try {
			return executeAllTestCases(context);
		} catch (Exception e) {
			log.error("测试用例执行失败: language={}",
					context.getLanguageStrategy().getLanguageType(), e);
			return errorResult(context.getRequest(), "测试执行失败: " + e.getMessage());
		}
	}

	private JudgeResult executeAllTestCases(JudgeContext context) {
		SandboxExecuteRequest request = context.getRequest();
		LanguageStrategy strategy = context.getLanguageStrategy();
		ExecutionConfig execConfig = strategy.getExecutionConfig();

		JudgeResult.JudgeResultBuilder resultBuilder = JudgeResult.builder()
				.submissionId(request.getSubmissionId())
				.status(JudgeStatus.ACCEPTED)
				.score(0)
				.timeUsed(0)
				.memoryUsed(0);

		List<TestCaseResult> testCaseResults = new ArrayList<>();
		List<String> inputs = request.getInputs();
		List<String> expectedOutputs = request.getExpectedOutputs();

		for (int i = 0; i < inputs.size(); i++) {
			TestCaseResult testResult = runSingleTestCase(
					context, execConfig, inputs.get(i), expectedOutputs.get(i), i + 1);

			testCaseResults.add(testResult);

			// 更新总体结果
			updateOverallResult(resultBuilder, testResult);

			// 如果某个测试用例失败，可以选择是否继续执行后续测试用例
			if (testResult.getStatus() != JudgeStatus.ACCEPTED && shouldStopOnFailure()) {
				break;
			}
		}

		return resultBuilder.testCaseResults(testCaseResults).build();
	}

	private TestCaseResult runSingleTestCase(JudgeContext context, ExecutionConfig execConfig,
	                                         String input, String expectedOutput, int testCaseId) {
		TestCaseResult.TestCaseResultBuilder resultBuilder = TestCaseResult.builder()
				.testCaseId((long) testCaseId)
				.status(JudgeStatus.ACCEPTED)
				.score(0)
				.memoryUsed(0);

		try {
			// 启动内存监控
			MemoryMonitor memoryMonitor = startMemoryMonitoring(context);

			// 执行程序
			ExecutionResult executionResult = executeProgram(context, execConfig, input);

			// 停止内存监控
			long maxMemory = stopMemoryMonitoring(memoryMonitor);

			resultBuilder.timeUsed((int) executionResult.timeUsed())
					.memoryUsed((int) (maxMemory / 1024)); // bytes to KB

			// 检查执行结果
			if (!executionResult.completed()) {
				return resultBuilder
						.status(JudgeStatus.TIME_LIMIT_EXCEEDED)
						.errorMessage("时间超限")
						.build();
			}

			if (!executionResult.stderr().trim().isEmpty()) {
				return resultBuilder
						.status(JudgeStatus.RUNTIME_ERROR)
						.errorMessage("运行时错误: " + executionResult.stderr())
						.build();
			}

			// 比较输出
			return compareOutput(context, resultBuilder, executionResult.stdout(), expectedOutput);

		} catch (Exception e) {
			log.error("测试用例执行异常: testCaseId={}, language={}",
					testCaseId, context.getLanguageStrategy().getLanguageType(), e);
			return resultBuilder
					.status(JudgeStatus.SYSTEM_ERROR)
					.errorMessage("系统错误: " + e.getMessage())
					.build();
		}
	}

	private ExecutionResult executeProgram(JudgeContext context, ExecutionConfig execConfig, String input)
			throws InterruptedException {

		// 构建执行命令，处理内存限制参数
		String[] executeCommand = processExecuteCommand(context, execConfig);

		// 设置运行时环境变量
		List<String> envVars = execConfig.getRuntimeEnvVars() != null ?
				execConfig.getRuntimeEnvVars().entrySet().stream()
						.map(entry -> entry.getKey() + "=" + entry.getValue())
						.collect(Collectors.toList()) :
				Collections.emptyList();

		// 创建执行命令
		ExecCreateCmdResponse execCreateCmd = context.getDockerClient()
				.execCreateCmd(context.getContainerId())
				.withCmd(executeCommand)
				.withUser(execConfig.getRunAsUser())
				.withWorkingDir(execConfig.getExecuteWorkDir())
				.withEnv(envVars)
				.withAttachStdin(true)
				.withAttachStdout(true)
				.withAttachStderr(true)
				.exec();

		return executeWithInput(context, execCreateCmd.getId(), input);
	}

	private String[] processExecuteCommand(JudgeContext context, ExecutionConfig execConfig) {
		String[] originalCommand = execConfig.getExecuteCommand();
		String memoryParam = execConfig.getMemoryLimitParam();

		// 如果有内存限制参数模板，替换占位符
		if (memoryParam != null && !memoryParam.isEmpty()) {
			int memoryLimit = context.getRequest().getMemoryLimit();
			String processedMemoryParam = String.format(memoryParam, memoryLimit);

			// 在命令中插入内存参数（通常在第二个位置）
			List<String> commandList = new ArrayList<>(Arrays.asList(originalCommand));
			if (commandList.size() > 1) {
				commandList.add(1, processedMemoryParam);
			}
			return commandList.toArray(new String[0]);
		}

		return originalCommand;
	}

	private TestCaseResult compareOutput(JudgeContext context,
	                                     TestCaseResult.TestCaseResultBuilder resultBuilder,
	                                     String actualOutput, String expectedOutput) {

		LanguageStrategy strategy = context.getLanguageStrategy();
		OutputProcessingConfig outputConfig = strategy.getOutputConfig();

		// 使用语言特定的输出处理配置
		String normalizedActual = normalizeOutput(actualOutput, outputConfig);
		String normalizedExpected = normalizeOutput(expectedOutput, outputConfig);

		if (Objects.equals(normalizedActual, normalizedExpected)) {
			return resultBuilder
					.status(JudgeStatus.ACCEPTED)
					.score(10) // 可配置化
					.build();
		} else {
			String errorMessage = String.format("输出不匹配. 期望: %s, 实际: %s",
					normalizedExpected, normalizedActual);
			log.debug("输出比较失败: language={}, {}",
					context.getLanguageStrategy().getLanguageType(), errorMessage);
			return resultBuilder
					.status(JudgeStatus.WRONG_ANSWER)
					.errorMessage(errorMessage)
					.build();
		}
	}

	private String normalizeOutput(String output, OutputProcessingConfig config) {
		if (output == null) return "";

		String result = output;

		// 应用自定义标准化函数
		if (config.getOutputNormalizer() != null) {
			result = config.getOutputNormalizer().apply(result);
		}

		// 去除空白字符
		if (config.isTrimWhitespace()) {
			result = result.replaceAll("\\s+", " ").trim();
		}

		// 忽略大小写
		if (config.isIgnoreCase()) {
			result = result.toLowerCase();
		}

		return result;
	}

	private void updateOverallResult(JudgeResult.JudgeResultBuilder resultBuilder, TestCaseResult testResult) {
		JudgeResult currentResult = resultBuilder.build();
		resultBuilder
				.status(testResult.getStatus() != JudgeStatus.ACCEPTED ?
						testResult.getStatus() : currentResult.getStatus())
				.score(testResult.getStatus() == JudgeStatus.ACCEPTED ?
						currentResult.getScore() + testResult.getScore() : currentResult.getScore())
				.timeUsed(currentResult.getTimeUsed() + testResult.getTimeUsed())
				.memoryUsed(Math.max(currentResult.getMemoryUsed(), testResult.getMemoryUsed()));
	}

	private boolean shouldStopOnFailure() {
		// 可配置是否在第一个失败的测试用例后停止
		return false; // 默认继续执行所有测试用例
	}
}