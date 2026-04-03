package com.xhs.clothingpatternbackend.trace;

import org.slf4j.MDC;

/**
*@Author: 小辛同学
*@CreateTime: 2026-04-03
*@Description: 
*@Version: 1.0
*/
public class TraceUtils {
    public static final String TRACE_ID = "traceId";
    public static ThreadLocal<String> traceIdThreadLocal = new ThreadLocal<>();

    public static String getTraceId() {
        return traceIdThreadLocal.get();
    }

    public static void setTraceId(String traceId) {
        traceIdThreadLocal.set(traceId);
        MDC.put(TRACE_ID, traceId);
    }

    public static void removeTraceId() {
        traceIdThreadLocal.remove();
        MDC.remove(TRACE_ID);
    }
}
