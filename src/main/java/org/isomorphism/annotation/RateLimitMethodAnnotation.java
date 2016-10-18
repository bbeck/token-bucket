package org.isomorphism.annotation;

/**
 * This annotation supports rate limiting a java method.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimitMethodAnnotation {

    // Permissible transactions per second.
    long tps() default Long.MAX_VALUE;

    // The identifier for the rate limiter. A distinct token bucket is defined
    // per id.
    String id();
}
