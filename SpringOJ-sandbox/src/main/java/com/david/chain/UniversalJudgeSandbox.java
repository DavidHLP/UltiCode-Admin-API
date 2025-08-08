package com.david.chain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.david.chain.utils.JudgeContext;
import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.enums.JudgeStatus;
import com.david.judge.enums.LanguageType;
import com.david.strategy.LanguageStrategyFactory;
import com.github.dockerjava.api.DockerClient;

import lombok.extern.slf4j.Slf4j;

/**
 * 通用多语言判题执行器
 */
@Slf4j
@Component
public class UniversalJudgeSandbox {

	private final JudgeHandler handlerChain;
	private final DockerClient dockerClient;
	private final LanguageStrategyFactory strategyFactory;

	public UniversalJudgeSandbox(
			JudgeChainBuilder chainBuilder,
			DockerClient dockerClient,
			LanguageStrategyFactory strategyFactory) {
		this.handlerChain = chainBuilder.buildChain();
		this.dockerClient = dockerClient;
		this.strategyFactory = strategyFactory;
	}

	/**
	 * 执行判题
	 */
	public JudgeResult execute(SandboxExecuteRequest request) {
		// 验证语言支持
		validateLanguageSupport(request.getLanguage());

		// 创建判题上下文
		JudgeContext context = JudgeContext.builder()
				.request(request)
				.dockerClient(dockerClient)
				.build();

		try {
			log.info("开始判题: submissionId={}, language={}",
					request.getSubmissionId(), request.getLanguage());

			// 执行责任链
			JudgeResult result = handlerChain.handle(context);

			log.info("判题完成: submissionId={}, status={}, score={}",
					request.getSubmissionId(), result.getStatus(), result.getScore());

			return result;

		} catch (Exception e) {
			log.error("判题执行异常: submissionId={}, language={}",
					request.getSubmissionId(), request.getLanguage(), e);
			return createSystemErrorResult(request, e);
		} finally {
			// 确保资源清理
			cleanupResources(context);
		}
	}

	/**
	 * 批量执行判题
	 */
	public List<JudgeResult> executeBatch(List<SandboxExecuteRequest> requests) {
		return requests.parallelStream()
				.map(this::execute)
				.collect(Collectors.toList());
	}

	/**
	 * 获取支持的语言列表
	 */
	public Set<LanguageType> getSupportedLanguages() {
		return strategyFactory.getSupportedLanguages();
	}

	/**
	 * 检查语言是否支持
	 */
	public boolean isLanguageSupported(LanguageType language) {
		return strategyFactory.getSupportedLanguages().contains(language);
	}

	private void validateLanguageSupport(LanguageType language) {
		if (!isLanguageSupported(language)) {
			throw new UnsupportedOperationException(
					String.format("不支持的编程语言: %s. 支持的语言: %s",
							language, getSupportedLanguages()));
		}
	}

	private JudgeResult createSystemErrorResult(SandboxExecuteRequest request, Exception e) {
		return JudgeResult.builder()
				.submissionId(request.getSubmissionId())
				.status(JudgeStatus.SYSTEM_ERROR)
				.score(0)
				.timeUsed(0)
				.memoryUsed(0)
				.errorMessage("系统错误: " + e.getMessage())
				.build();
	}

	private void cleanupResources(JudgeContext context) {
		try {
			// 清理Docker容器
			if (context.getContainerId() != null) {
				try {
					dockerClient.stopContainerCmd(context.getContainerId()).exec();
					dockerClient.removeContainerCmd(context.getContainerId()).withForce(true).exec();
					log.debug("容器清理成功: {}", context.getContainerId());
				} catch (Exception e) {
					log.warn("容器清理失败: {}", context.getContainerId(), e);
				}
			}

			// 清理临时目录
			if (context.getTempDir() != null) {
				try {
					Path path = Path.of(context.getTempDir());
					if (Files.exists(path)) {
						Files.walk(path)
								.sorted(Comparator.reverseOrder())
								.map(Path::toFile)
								.forEach(File::delete);
						log.debug("临时目录清理成功: {}", context.getTempDir());
					}
				} catch (IOException e) {
					log.warn("临时目录清理失败: {}", context.getTempDir(), e);
				}
			}
		} catch (Exception e) {
			log.error("资源清理异常", e);
		}
	}
}
