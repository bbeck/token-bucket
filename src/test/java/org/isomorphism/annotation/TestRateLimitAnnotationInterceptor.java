package org.isomorphism.annotation;

import org.isomorphism.util.TokenBucket;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Rate limit test class.
 */
public class TestRateLimitAnnotationInterceptor {

    private static final String TEST_TOKEN_BUCKET_ID = "TEST_ID";

    @Test
    public void testRateLimitInterceptorTryProceedOnMaxValueTPS() {
        final RateLimitMethodAnnotationInterceptor interceptor =
                new RateLimitMethodAnnotationInterceptor();

        boolean canProceed =
                interceptor.tryProceed(TEST_TOKEN_BUCKET_ID, Long.MAX_VALUE);

        assertTrue(canProceed == true);
    }

    @Test
    public void testRateLimitInterceptorDistinctTokenBuckets() {
        final RateLimitMethodAnnotationInterceptor interceptor =
                new RateLimitMethodAnnotationInterceptor();

        boolean canProceedFirst =
                interceptor.tryProceed("TEST_ID_1", Long.MAX_VALUE);

        assertTrue(canProceedFirst == true);

        boolean canProceedSecond =
                interceptor.tryProceed("TEST_ID_2", Long.MAX_VALUE);

        assertTrue(canProceedSecond == true);

        boolean canProceedThird =
                interceptor.tryProceed("TEST_ID_3", Long.MAX_VALUE);

        assertTrue(canProceedThird == true);

        ConcurrentHashMap<String, TokenBucket> tokenBucketMap
                = interceptor.TOKEN_BUCKET_MAP;

        assertTrue(interceptor.TOKEN_BUCKET_MAP.size() == 3);
        assertTrue(tokenBucketMap.containsKey("TEST_ID_1"));
        assertTrue(tokenBucketMap.containsKey("TEST_ID_2"));
        assertTrue(tokenBucketMap.containsKey("TEST_ID_3"));
    }

    @Test
    public void testRateLimitInterceptorClashingTokenBuckets() {
        final RateLimitMethodAnnotationInterceptor interceptor =
                new RateLimitMethodAnnotationInterceptor();

        boolean canProceedFirst =
                interceptor.tryProceed("TEST_ID_1", Long.MAX_VALUE);

        assertTrue(canProceedFirst == true);

        boolean canProceedSecond =
                interceptor.tryProceed("TEST_ID_1", Long.MAX_VALUE);

        assertTrue(canProceedSecond == true);

        ConcurrentHashMap<String, TokenBucket> tokenBucketMap
                = interceptor.TOKEN_BUCKET_MAP;

        assertTrue(interceptor.TOKEN_BUCKET_MAP.size() == 1);
        assertTrue(tokenBucketMap.containsKey("TEST_ID_1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRateLimitInterceptorIllegalArgumentZeroTPS() {
        final RateLimitMethodAnnotationInterceptor interceptor =
                new RateLimitMethodAnnotationInterceptor();

        interceptor.tryProceed("TEST_ID_1", 0l);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRateLimitInterceptorIllegalArgumentNegativeTPS() {
        final RateLimitMethodAnnotationInterceptor interceptor =
                new RateLimitMethodAnnotationInterceptor();

        interceptor.tryProceed("TEST_ID_1", Long.MIN_VALUE);
    }

    @Test
    public void testRateLimitInterceptorCannotProceedNoTPS() {
        final RateLimitMethodAnnotationInterceptor interceptor =
                new RateLimitMethodAnnotationInterceptor();

        boolean tryProceed = interceptor.tryProceed("TEST_ID_1", 1);
        boolean tryProceedImmediate = interceptor.tryProceed("TEST_ID_1", 1);

        // In most modern processors, tryProceed Immediate is way faster than one TPS
        // So this test will pass with extremely high probability.

        assertTrue(tryProceed);
        assertFalse(tryProceedImmediate);
    }
}
