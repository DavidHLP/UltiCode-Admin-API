package com.david.chain.handler.Java;

import com.david.chain.Handler;
import com.david.chain.utils.JudgmentContext;
import com.david.chain.utils.TestCaseContext;
import com.david.chain.utils.TestCaseOutputContext;
import com.david.utils.java.JavaFormationUtils;

import java.util.List;

public class JavaFormatOutputHandler extends Handler {

	@Override
	public Boolean handleRequest(JudgmentContext judgmentContext) {
		if (judgmentContext == null) return false;
		List<TestCaseContext> cases = judgmentContext.getTestCaseContexts();
		if (cases == null || cases.isEmpty()) {
			return next(judgmentContext);
		}

		for (TestCaseContext c : cases) {
			if (c == null) continue;
			TestCaseOutputContext out = c.getTestCaseOutputContext();
			if (out == null) continue;
			String type = out.getOutputType();
			String val = out.getOutput();
			String normalized = JavaFormationUtils.ensureJsonLiteral(val, type);
			out.setOutput(normalized);
		}

		return next(judgmentContext);
	}

	private Boolean next(JudgmentContext ctx) {
		return this.nextHandler == null ? Boolean.TRUE : this.nextHandler.handleRequest(ctx);
	}
}

