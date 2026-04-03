package com.xhs.clothingpatternbackend.trace;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
*@Author: 小辛同学
*@CreateTime: 2026-04-03
*@Description: 
*@Version: 1.0
*/
@Configuration(proxyBeanMethods = false)
public class TraceConfiguration {
//    @Bean
//    public TraceFilter traceFilter() {
//        return new TraceFilter();
//    }

    @Bean
    public ResultTraceIdAspect fillRequestIdAspect() {
        return new ResultTraceIdAspect();
    }
}
