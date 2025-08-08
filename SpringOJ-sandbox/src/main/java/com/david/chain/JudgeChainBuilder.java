package com.david.chain;

import java.util.List;

import org.springframework.stereotype.Component;

import com.david.chain.handler.CompilationHandler;
import com.david.chain.handler.DockerContainerHandler;
import com.david.chain.handler.EnvironmentSetupHandler;
import com.david.chain.handler.TestCaseExecutionHandler;

/**
 * 判题链构建器
 */
@Component
public class JudgeChainBuilder {

	private final EnvironmentSetupHandler environmentHandler;
	private final DockerContainerHandler containerHandler;
	private final CompilationHandler compilationHandler;
	private final TestCaseExecutionHandler executionHandler;

	public JudgeChainBuilder(
			EnvironmentSetupHandler environmentHandler,
			DockerContainerHandler containerHandler,
			CompilationHandler compilationHandler,
			TestCaseExecutionHandler executionHandler) {
		this.environmentHandler = environmentHandler;
		this.containerHandler = containerHandler;
		this.compilationHandler = compilationHandler;
		this.executionHandler = executionHandler;
	}

	public JudgeHandler buildChain() {
		// 构建标准责任链
		environmentHandler
				.setNext(containerHandler)
				.setNext(compilationHandler)
				.setNext(executionHandler);

		return environmentHandler;
	}

	public JudgeHandler buildCustomChain(List<JudgeHandler> customHandlers) {
		// 支持自定义责任链
		if (customHandlers == null || customHandlers.isEmpty()) {
			return buildChain();
		}

		JudgeHandler head = customHandlers.get(0);
		JudgeHandler current = head;

		for (int i = 1; i < customHandlers.size(); i++) {
			current = current.setNext(customHandlers.get(i));
		}

		return head;
	}
}
