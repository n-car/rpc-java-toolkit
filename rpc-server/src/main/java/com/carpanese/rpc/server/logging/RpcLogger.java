package com.carpanese.rpc.server.logging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Structured logger for RPC operations
 */
public class RpcLogger {
    private final RpcLoggerOptions options;
    private final PrintStream output;
    private final Gson gson;
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    public RpcLogger(RpcLoggerOptions options) {
        this(options, System.out);
    }

    public RpcLogger(RpcLoggerOptions options, PrintStream output) {
        this.options = options;
        this.output = output;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Log an error message
     */
    public void error(String message, Object metadata, Throwable error) {
        log(RpcLogLevel.ERROR, message, metadata, error);
    }

    public void error(String message) {
        error(message, null, null);
    }

    /**
     * Log a warning message
     */
    public void warn(String message, Object metadata) {
        log(RpcLogLevel.WARN, message, metadata, null);
    }

    public void warn(String message) {
        warn(message, null);
    }

    /**
     * Log an info message
     */
    public void info(String message, Object metadata) {
        log(RpcLogLevel.INFO, message, metadata, null);
    }

    public void info(String message) {
        info(message, null);
    }

    /**
     * Log a debug message
     */
    public void debug(String message, Object metadata) {
        log(RpcLogLevel.DEBUG, message, metadata, null);
    }

    public void debug(String message) {
        debug(message, null);
    }

    /**
     * Log a trace message
     */
    public void trace(String message, Object metadata) {
        log(RpcLogLevel.TRACE, message, metadata, null);
    }

    public void trace(String message) {
        trace(message, null);
    }

    /**
     * Core logging method
     */
    public void log(RpcLogLevel level, String message, Object metadata, Throwable error) {
        if (options.getLevel() == RpcLogLevel.SILENT) {
            return;
        }

        if (level.getLevel() > options.getLevel().getLevel()) {
            return;
        }

        String timestamp = options.isIncludeTimestamp() 
            ? TIME_FORMATTER.format(Instant.now()) 
            : null;

        if (options.getFormat() == RpcLogFormat.JSON) {
            logJson(level, message, metadata, error, timestamp);
        } else {
            logText(level, message, metadata, error, timestamp);
        }
    }

    private void logJson(RpcLogLevel level, String message, Object metadata, Throwable error, String timestamp) {
        Map<String, Object> logEntry = new HashMap<>();
        
        if (timestamp != null) {
            logEntry.put("timestamp", timestamp);
        }
        
        logEntry.put("level", level.name());
        logEntry.put("message", message);
        
        if (metadata != null) {
            logEntry.put("metadata", metadata);
        }
        
        if (error != null) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("message", error.getMessage());
            errorInfo.put("type", error.getClass().getName());
            logEntry.put("error", errorInfo);
        }

        output.println(gson.toJson(logEntry));
    }

    private void logText(RpcLogLevel level, String message, Object metadata, Throwable error, String timestamp) {
        StringBuilder sb = new StringBuilder();
        
        if (timestamp != null) {
            sb.append("[").append(timestamp).append("] ");
        }
        
        sb.append("[").append(level.name()).append("] ");
        sb.append(message);
        
        if (metadata != null) {
            sb.append(" | ").append(gson.toJson(metadata));
        }
        
        output.println(sb.toString());
        
        if (error != null) {
            error.printStackTrace(output);
        }
    }
}
