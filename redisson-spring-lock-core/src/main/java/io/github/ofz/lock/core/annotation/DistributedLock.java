package io.github.ofz.lock.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String key();

    String prefix() default "lock";

    int waitTime() default 5;

    int leaseTime() default 30;

    int retry() default 1;

    long delay() default 1000;

    double multiplier() default 0;

}
