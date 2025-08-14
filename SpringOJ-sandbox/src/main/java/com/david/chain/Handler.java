package com.david.chain;

import com.david.chain.utils.JudgmentContext;

import lombok.Setter;

@Setter
public abstract class Handler {
    protected Handler nextHandler;

    public abstract Boolean handleRequest(JudgmentContext judgmentContext);
}
