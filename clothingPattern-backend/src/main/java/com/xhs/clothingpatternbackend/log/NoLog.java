package com.xhs.clothingpatternbackend.log;

import java.lang.annotation.*;

/**
 * @Author: 小辛同学
 * @CreateTime: 2026-04-03
 * @Description: 不打印日志注解
 * @Version: 1.0
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoLog {
}
