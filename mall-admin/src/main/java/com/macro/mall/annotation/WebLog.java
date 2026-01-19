package com.macro.mall.annotation;

import java.lang.annotation.*;

/**
 * 自定义注解，用于标记需要记录操作日志的方法
 * Created by macro on 2024/01/19.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLog {
    
    /**
     * 操作描述
     */
    String description() default "";
}
