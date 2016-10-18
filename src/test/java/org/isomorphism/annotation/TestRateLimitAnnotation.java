package org.isomorphism.annotation;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by arunavs on 9/18/16.
 */
public class TestRateLimitAnnotation {

    private static final int TEST_VAL = 1;

    @RateLimitMethodAnnotation(tps = Long.MAX_VALUE, id="stubMethod")
    public int stubMethod() {
        return TEST_VAL;
    }

    @Test
    public void testRateLimitInterceptorSanityTest() {

        final RateLimitConfig config = new RateLimitConfig();
        Injector injector = Guice.createInjector(config);
        TestRateLimitAnnotation testInstance =
                injector.getInstance(TestRateLimitAnnotation.class);

        // Checks whether stub method was called after Rate limit was initialized
        int retVal = testInstance.stubMethod();
        assertTrue(retVal == TEST_VAL);
    }
}
