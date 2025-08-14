package com.david.chain;

import com.david.chain.utils.JudgmentContext;

public abstract class Handler {
    protected Handler nextHandler;

    public void setNext(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public abstract void handleRequest(JudgmentContext judgmentContext);
}
