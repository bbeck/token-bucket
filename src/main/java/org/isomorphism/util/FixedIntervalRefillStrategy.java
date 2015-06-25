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

import java.util.concurrent.TimeUnit;

/**
 * A token bucket refill strategy that will provide N tokens for a token bucket to consume every T units of time.
 * The tokens are refilled in bursts rather than at a fixed rate.  This refill strategy will never allow more than
 * N tokens to be consumed during a window of time T.
 */
public class FixedIntervalRefillStrategy implements TokenBucketImpl.RefillStrategy
{
  private final Ticker ticker;
  private final long numTokensPerPeriod;
  private final long periodDurationInNanos;
  private long lastRefillTime;
  private long nextRefillTime;

  /**
   * Create a FixedIntervalRefillStrategy.
   *
   * @param ticker             A ticker to use to measure time.
   * @param numTokensPerPeriod The number of tokens to add to the bucket every period.
   * @param period             How often to refill the bucket.
   * @param unit               Unit for period.
   */
  public FixedIntervalRefillStrategy(Ticker ticker, long numTokensPerPeriod, long period, TimeUnit unit)
  {
    this.ticker = ticker;
    this.numTokensPerPeriod = numTokensPerPeriod;
    this.periodDurationInNanos = unit.toNanos(period);
    this.lastRefillTime = -periodDurationInNanos;
    this.nextRefillTime = -periodDurationInNanos;
  }

  @Override
  public synchronized long refill()
  {
    long now = ticker.read();
    if (now < nextRefillTime) {
      return 0;
    }

    // We now know that we need to refill the bucket with some tokens, the question is how many.  We need to count how
    // many periods worth of tokens we've missed.
    long numPeriods = Math.max(0, (now - lastRefillTime) / periodDurationInNanos);

    // Move the last refill time forward by this many periods.
    lastRefillTime += numPeriods * periodDurationInNanos;

    // ...and we'll refill again one period after the last time we refilled.
    nextRefillTime = lastRefillTime + periodDurationInNanos;

    return numPeriods * numTokensPerPeriod;
  }

  @Override
  public long getDurationUntilNextRefill(TimeUnit unit)
  {
    long now = ticker.read();
    return unit.convert(Math.max(0, nextRefillTime - now), TimeUnit.NANOSECONDS);
  }
}

