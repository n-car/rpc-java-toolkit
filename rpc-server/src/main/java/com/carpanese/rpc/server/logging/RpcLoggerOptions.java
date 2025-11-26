package com.carpanese.rpc.server.logging;

/**
 * Logging configuration options
 */
public class RpcLoggerOptions {
    private RpcLogLevel level = RpcLogLevel.INFO;
    private RpcLogFormat format = RpcLogFormat.TEXT;
    private boolean includeTimestamp = true;

    public RpcLoggerOptions() {
    }

    public RpcLogLevel getLevel() {
        return level;
    }

    public RpcLoggerOptions setLevel(RpcLogLevel level) {
        this.level = level;
        return this;
    }

    public RpcLogFormat getFormat() {
        return format;
    }

    public RpcLoggerOptions setFormat(RpcLogFormat format) {
        this.format = format;
        return this;
    }

    public boolean isIncludeTimestamp() {
        return includeTimestamp;
    }

    public RpcLoggerOptions setIncludeTimestamp(boolean includeTimestamp) {
        this.includeTimestamp = includeTimestamp;
        return this;
    }
}
