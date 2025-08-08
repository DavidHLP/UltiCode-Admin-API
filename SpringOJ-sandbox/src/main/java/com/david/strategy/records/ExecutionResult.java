package com.david.strategy.records;

/**
 * 执行结果记录
 */
public record ExecutionResult(String stdout, String stderr, long timeUsed, boolean completed) {

	public boolean hasError() {
		return !stderr.trim().isEmpty();
	}

	public boolean isSuccessful() {
		return completed && !hasError();
	}
}
