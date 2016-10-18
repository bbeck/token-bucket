package org.isomorphism.annotation;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the rate limiter.
 */
public class RateLimitMethodAnnotationInterceptor implements MethodInterceptor {

    ConcurrentHashMap<String, TokenBucket>
            TOKEN_BUCKET_MAP = new ConcurrentHashMap<String, TokenBucket>();

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        final RateLimitMethodAnnotation rateLimitMethod =
                methodInvocation.getMethod().getAnnotation(RateLimitMethodAnnotation.class);

        final String rateLimitId = rateLimitMethod.id();
        final long tps = rateLimitMethod.tps();

        boolean proceedMethodCall = tryProceed(rateLimitId, tps);

        while(!proceedMethodCall) {
            Thread.sleep(getDurationTillRefillInMilliSecond(rateLimitId, tps));
            proceedMethodCall = tryProceed(rateLimitId, tps);
        }

        return methodInvocation.proceed();
    }

    boolean tryProceed(final String tokenBucketId, final long tps) {

        TokenBucket tokenBucket = TOKEN_BUCKET_MAP.get(tokenBucketId);

        if (tokenBucket == null) {
            tokenBucket = buildTokenBucket(tps);
            TOKEN_BUCKET_MAP.put(tokenBucketId, tokenBucket);
        }

        return tokenBucket.tryConsume();
    }

    private long getDurationTillRefillInMilliSecond(final String tokenBucketId, long tps) {
        final TokenBucket tokenBucket = TOKEN_BUCKET_MAP.get(tokenBucketId);

        if (tokenBucket == null) {
            TOKEN_BUCKET_MAP.put(tokenBucketId, buildTokenBucket(tps));
        }

        return tokenBucket.getDurationUntilNextRefill(TimeUnit.MILLISECONDS);

    }

    private TokenBucket buildTokenBucket(final long tps) {
        return TokenBuckets.builder().withCapacity(tps)
                .withFixedIntervalRefillStrategy(1, 1, TimeUnit.SECONDS)
                .build();
    }
}
