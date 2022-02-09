package com.strod.moduleinit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface ModuleInit {
    /**
     * The priority of moduleInit, ModuleInit will be excute them follow the priority.
     * 值越小，优先级越高，越早调用
     */
    int priority();

    /**
     * The name of moduleInit, may be used to generate javadoc.
     */
    String name() default "Default";
}
