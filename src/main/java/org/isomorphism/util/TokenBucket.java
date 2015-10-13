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

import java.util.concurrent.TimeUnit;

/**
 * A token bucket is used for rate limiting access to a portion of code.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Token_bucket">Token Bucket on Wikipedia</a>
 * @see <a href="http://en.wikipedia.org/wiki/Leaky_bucket">Leaky Bucket on Wikipedia</a>
 */
public interface TokenBucket
{
  /**
   * Returns the capacity of this token bucket.  This is the maximum number of tokens that the bucket can hold at
   * any one time.
   *
   * @return The capacity of the bucket.
   */
  long getCapacity();

  /**
   * Returns the current number of tokens in the bucket.  If the bucket is empty then this method will return 0.
   *
   * @return The current number of tokens in the bucket.
   */
  long getNumTokens();

  /**
   * Returns the amount of time in the specified time unit until the next group of tokens can be added to the token
   * bucket.
   *
   * @see org.isomorphism.util.TokenBucket.RefillStrategy#getDurationUntilNextRefill(java.util.concurrent.TimeUnit)
   * @param unit The time unit to express the return value in.
   * @return The amount of time until the next group of tokens can be added to the token bucket.
   */
  long getDurationUntilNextRefill(TimeUnit unit) throws UnsupportedOperationException;

  /**
   * Attempt to consume a single token from the bucket.  If it was consumed then {@code true} is returned, otherwise
   * {@code false} is returned.
   *
   * @return {@code true} if a token was consumed, {@code false} otherwise.
   */
  boolean tryConsume();

  /**
   * Attempt to consume a specified number of tokens from the bucket.  If the tokens were consumed then {@code true}
   * is returned, otherwise {@code false} is returned.
   *
   * @param numTokens The number of tokens to consume from the bucket, must be a positive number.
   * @return {@code true} if the tokens were consumed, {@code false} otherwise.
   */
  boolean tryConsume(long numTokens);

  /**
   * Consume a single token from the bucket.  If no token is currently available then this method will block until a
   * token becomes available.
   */
  void consume();

  /**
   * Consumes multiple tokens from the bucket.  If enough tokens are not currently available then this method will block
   * until
   *
   * @param numTokens The number of tokens to consume from teh bucket, must be a positive number.
   */
  void consume(long numTokens);

  /**
   * Refills the bucket with the specified number of tokens.  If the bucket is currently full or near capacity then
   * fewer than {@code numTokens} may be added.
   *
   * @param numTokens The number of tokens to add to the bucket.
   */
  void refill(long numTokens);

  /** Encapsulation of a refilling strategy for a token bucket. */
  static interface RefillStrategy
  {
    /**
     * Returns the number of tokens to add to the token bucket.
     *
     * @return The number of tokens to add to the token bucket.
     */
    long refill();

    /**
     * Returns the amount of time in the specified time unit until the next group of tokens can be added to the token
     * bucket.  Please note, depending on the {@code SleepStrategy} used by the token bucket, tokens may not actually
     * be added until much after the returned duration.  If for some reason the implementation of
     * {@code RefillStrategy} doesn't support knowing when the next batch of tokens will be added, then an
     * {@code UnsupportedOperationException} may be thrown.  Lastly, if the duration until the next time tokens will
     * be added to the token bucket is less than a single unit of the passed in time unit then this method will
     * return 0.
     *
     * @param unit The time unit to express the return value in.
     * @return The amount of time until the next group of tokens can be added to the token bucket.
     */
    long getDurationUntilNextRefill(TimeUnit unit) throws UnsupportedOperationException;
  }

  /** Encapsulation of a strategy for relinquishing control of the CPU. */
  static interface SleepStrategy
  {
    /**
     * Sleep for a short period of time to allow other threads and system processes to execute.
     */
    void sleep();
  }
}
