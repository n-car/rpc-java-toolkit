package com.carpanese.rpc.server.logging;

/**
 * Log levels matching Express-style logging
 */
public enum RpcLogLevel {
    SILENT(0),
    ERROR(1),
    WARN(2),
    INFO(3),
    DEBUG(4),
    TRACE(5);

    private final int level;

    RpcLogLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isEnabled(RpcLogLevel minimumLevel) {
        return this.level <= minimumLevel.level;
    }
}
