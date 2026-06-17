package com.kidmily.algoga_server.global.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    String key();
    long waitTime() default 5L;
    long leaseTime() default 3L;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
