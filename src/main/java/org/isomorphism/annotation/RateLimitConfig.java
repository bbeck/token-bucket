package org.isomorphism.annotation;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * Configuration for rate limiting.
 */
public class RateLimitConfig extends AbstractModule {

    protected void configure() {
        bindInterceptor(Matchers.any(),
                Matchers.annotatedWith(RateLimitMethodAnnotation.class),
                new RateLimitMethodAnnotationInterceptor());
    }
}
