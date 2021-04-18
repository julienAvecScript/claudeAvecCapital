package com.github.therealjlb.claude;

import io.contek.invoker.commons.actor.http.IHttpContext;

public class ClaudeContext implements IHttpContext {

    private String baseURL;

    public ClaudeContext() {
        this.baseURL = "https://api.pro.coinbase.com";
    }

    @Override
    public String getBaseUrl() {
        return this.baseURL;
    }
}
