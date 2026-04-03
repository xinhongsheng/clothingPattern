package com.xhs.clothingpatternbackend.trace;

import com.xhs.clothingpatternbackend.common.BaseResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

/**
*@Author: 小辛同学
*@CreateTime: 2026-04-03
*@Description: 追踪id的切面
*@Version: 1.0
*/
@Aspect
@Order
public class ResultTraceIdAspect {
    @Pointcut("execution(* com.xhs..*Controller.*(..)) ||execution(* com.xhs.clothingpatternbackend.exception.GlobalExceptionHandler.*(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object object = pjp.proceed();
        if (object instanceof BaseResponse) {
            ((BaseResponse<?>) object).setTraceId(TraceUtils.getTraceId());
        }
        return object;
    }

}
