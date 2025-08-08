package com.david.chain.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.david.chain.JudgeHandler;
import com.david.chain.utils.JudgeContext;
import com.david.dto.JudgeResult;
import com.david.judge.enums.JudgeStatus;
import com.david.strategy.LanguageStrategy;
import com.david.strategy.records.ExecutionResult;
import com.david.strategy.utils.CompilationConfig;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 代码编译处理器 - 通用化
 */
@Slf4j
@Component
public class CompilationHandler extends JudgeHandler {

	@Override
	protected JudgeResult doHandle(JudgeContext context) {
		LanguageStrategy strategy = context.getLanguageStrategy();
		CompilationConfig compileConfig = strategy.getCompilationConfig();

		// 如果不需要编译，直接跳过
		if (!compileConfig.isNeedsCompilation()) {
			log.info("语言 {} 不需要编译，跳过编译步骤", strategy.getLanguageType());
			return continueResult();
		}

		try {
			return performCompilation(context, compileConfig);
		} catch (Exception e) {
			log.error("编译失败: language={}", strategy.getLanguageType(), e);
			return errorResult(context.getRequest(), "编译失败: " + e.getMessage());
		}
	}

	private JudgeResult performCompilation(JudgeContext context, CompilationConfig compileConfig) {
		try {
			// 设置编译环境变量
			Map<String, String> compileEnv = compileConfig.getCompileEnvVars();
			List<String> envVars = compileEnv != null ?
					compileEnv.entrySet().stream()
							.map(entry -> entry.getKey() + "=" + entry.getValue())
							.collect(Collectors.toList()) :
					Collections.emptyList();

			// 创建编译执行命令
			ExecCreateCmdResponse execCmd = context.getDockerClient()
					.execCreateCmd(context.getContainerId())
					.withCmd(compileConfig.getCompileCommand())
					.withWorkingDir(compileConfig.getCompileWorkDir())
					.withEnv(envVars)
					.withAttachStdout(true)
					.withAttachStderr(true)
					.exec();

			// 执行编译并捕获输出
			ExecutionResult compileResult = captureExecutionOutput(
					context, execCmd.getId(), compileConfig.getCompileTimeoutSeconds() * 1000L);

			// 检查编译结果
			if (!compileResult.stderr().trim().isEmpty()) {
				return JudgeResult.builder()
						.submissionId(context.getRequest().getSubmissionId())
						.status(JudgeStatus.COMPILE_ERROR)
						.compileInfo(compileResult.stderr().trim())
						.build();
			}

			log.info("编译成功: language={}", context.getLanguageStrategy().getLanguageType());
			return continueResult();

		} catch (Exception e) {
			log.error("编译执行异常: language={}", context.getLanguageStrategy().getLanguageType(), e);
			return errorResult(context.getRequest(), "编译错误: " + e.getMessage());
		}
	}
}
