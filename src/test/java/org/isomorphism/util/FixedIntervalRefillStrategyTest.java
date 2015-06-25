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

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class FixedIntervalRefillStrategyTest
{
  private static final long N = 5;                     // 5 tokens
  private static final long P = 10;                    // every 10
  private static final TimeUnit U = TimeUnit.SECONDS;  // seconds

  private final MockTicker ticker = new MockTicker();
  private final FixedIntervalRefillStrategy strategy = new FixedIntervalRefillStrategy(ticker, N, P, U);

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
    for (int i = 0; i < P - 1; i++) {
      ticker.advance(1, U);
      assertEquals(0, strategy.refill());
    }
  }

  @Test
  public void testRefillEveryPeriod()
  {
    strategy.refill();

    ticker.advance(P, U);
    assertEquals(N, strategy.refill());

    ticker.advance(P, U);
    assertEquals(N, strategy.refill());

    ticker.advance(P, U);
    assertEquals(N, strategy.refill());
  }

  @Test
  public void testRefillEveryOtherPeriod()
  {
    strategy.refill();

    // Move time forward two periods, since we're skipping a period next time we should add double the tokens.
    ticker.advance(2 * P, U);
    assertEquals(2 * N, strategy.refill());

    ticker.advance(2 * P, U);
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
    ticker.advance(P + 1, U);
    assertEquals(N, strategy.refill());

    // t = 2P+1
    ticker.advance(P, U);
    assertEquals(N, strategy.refill());

    // t = 3P
    ticker.advance(P - 1, U);
    assertEquals(N, strategy.refill());

    // t = 4P-1
    ticker.advance(P - 1, U);
    assertEquals(0, strategy.refill());

    // t = 4P
    ticker.advance(1, U);
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

    for (int i = 0; i < P - 1; i++) {
      assertEquals(P - i, strategy.getDurationUntilNextRefill(TimeUnit.SECONDS));
      ticker.advance(1, U);
    }
  }

  @Test
  public void testDurationAtSecondRefillTime()
  {
    strategy.refill();
    ticker.advance(P, U);

    assertEquals(0, strategy.getDurationUntilNextRefill(TimeUnit.SECONDS));
  }

  @Test
  public void testDurationInProperUnits()
  {
    strategy.refill();

    assertEquals(10000, strategy.getDurationUntilNextRefill(TimeUnit.MILLISECONDS));
  }

  private static final class MockTicker extends Ticker
  {
    private long now = 0;

    @Override
    public long read()
    {
      return now;
    }

    public void advance(long delta, TimeUnit unit)
    {
      now += unit.toNanos(delta);
    }
  }
}
