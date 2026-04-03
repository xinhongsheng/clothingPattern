package com.xhs.clothingpatternbackend.trace;

import cn.hutool.core.util.IdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
*@Author: 小辛同学
*@CreateTime: 2026-04-03
*@Description: 
*@Version: 1.0
*/
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class TraceFilter extends OncePerRequestFilter {

    public static Logger logger = LoggerFactory.getLogger(TraceFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = IdUtil.fastSimpleUUID();
        TraceUtils.setTraceId(traceId);

        logger.info("traceId={}, 请求start：{}", traceId, request.getRequestURL().toString());
        long st = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long et = System.currentTimeMillis();
            logger.info("traceId={}, 请求end：{}，耗时(ms)：{}", traceId, request.getRequestURL().toString(), (et - st));
            TraceUtils.removeTraceId();
        }
    }
}
