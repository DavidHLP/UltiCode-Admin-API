package com.david.chain.utils;

public record RunResult(
		boolean success,
		int exitCode,
		String output,
		String error,
		long executionTime,
		long memoryUsage
) {
	public boolean isSuccess() {
		return success;
	}


	public String getOutput() {
		return output;
	}

	public String getError() {
		return error;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public long getMemoryUsage() {
		return memoryUsage;
	}
}
