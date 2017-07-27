/*
 * Copyright 2012-2014 Brandon Beck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.isomorphism.util;

import com.google.common.base.Ticker;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FixedIntervalRefillStrategyTest
{
  private static final long N = 5;                     // 5 tokens
  private static final Duration P = Duration.of(10, SECONDS);                    // every 10
  private static final TimeUnit U = TimeUnit.SECONDS;  // seconds

  private final MockTicker ticker = new MockTicker();
  private final FixedIntervalRefillStrategy strategy = new FixedIntervalRefillStrategy(ticker, N, P);

  @Test
  public void testFirstRefill()
  {
    assertEquals(N, strategy.refill());
  }

  @Test
  public void testNoRefillUntilPeriodUp()
  {
    strategy.refill();

    // Another refill shouldn't come for P time units.
    for (int i = 0; i < P.getSeconds() - 1; i++) {
      ticker.advance(Duration.of(1, SECONDS));
      assertEquals(0, strategy.refill());
    }
  }

  @Test
  public void testRefillEveryPeriod()
  {
    strategy.refill();

    ticker.advance(P);
    assertEquals(N, strategy.refill());

    ticker.advance(P);
    assertEquals(N, strategy.refill());

    ticker.advance(P);
    assertEquals(N, strategy.refill());
  }

  @Test
  public void testRefillEveryOtherPeriod()
  {
    strategy.refill();

    // Move time forward two periods, since we're skipping a period next time we should add double the tokens.
    ticker.advance(P.multipliedBy(2));
    assertEquals(2 * N, strategy.refill());

    ticker.advance(P.multipliedBy(2));
    assertEquals(2 * N, strategy.refill());
  }

  @Test
  public void testRefillOnNonEvenPeriods()
  {
    // The strategy is configured to refill tokens every P time units.  So we should only get refills at 0, P, 2P, 3P,
    // etc.  Any other time should return 0 tokens.

    // t = 0
    assertEquals(N, strategy.refill());

    // t = P+1
    ticker.advance(P.plus(1, SECONDS));
    assertEquals(N, strategy.refill());

    // t = 2P+1
    ticker.advance(P);
    assertEquals(N, strategy.refill());

    // t = 3P
    ticker.advance(P.minus(1, SECONDS));
    assertEquals(N, strategy.refill());

    // t = 4P-1
    ticker.advance(P.minus(1, SECONDS));
    assertEquals(0, strategy.refill());

    // t = 4P
    ticker.advance(Duration.of(1, SECONDS));
    assertEquals(N, strategy.refill());
  }

  @Test
  public void testDurationUntilFirstRefill()
  {
    // A refill has never happened, so one is supposed to happen immediately.
    assertEquals(0, strategy.getDurationUntilNextRefill(TimeUnit.SECONDS));
  }

  @Test
  public void testDurationAfterFirstRefill()
  {
    strategy.refill();

    for (int i = 0; i < P.getSeconds() - 1; i++) {
      assertEquals(P.getSeconds() - i, strategy.getDurationUntilNextRefill().getSeconds());
      ticker.advance(Duration.of(1, SECONDS));
    }
  }

  @Test
  public void testDurationAtSecondRefillTime()
  {
    strategy.refill();
    ticker.advance(P);

    assertTrue(strategy.getDurationUntilNextRefill().isZero());
  }

  @Test
  public void testDurationInProperUnits()
  {
    strategy.refill();

    assertEquals(P, strategy.getDurationUntilNextRefill());
  }

  private static final class MockTicker extends Ticker
  {
    private long now = 0;

    @Override
    public long read()
    {
      return now;
    }

    public void advance(Duration delta)
    {
      now += delta.toNanos();
    }
  }
}
