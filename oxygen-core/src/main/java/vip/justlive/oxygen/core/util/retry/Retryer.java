/*
 * Copyright (C) 2018 justlive1
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package vip.justlive.oxygen.core.util.retry;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * 重试器
 *
 * @param <T> 泛型
 * @author wubo
 */
public class Retryer<T> {

  private final long waitTime;
  private final TimeLimiter<T> timeLimiter;
  private final Predicate<Attempt<T>> retryPredicate;
  private final List<StopStrategy<T>> stopStrategies;
  private final boolean sleepOrWait;

  Retryer(long waitTime, TimeLimiter<T> timeLimiter, Predicate<Attempt<T>> retryPredicate,
      List<StopStrategy<T>> stopStrategies,
      boolean sleepOrWait) {
    this.waitTime = waitTime;
    this.timeLimiter = timeLimiter;
    this.retryPredicate = retryPredicate;
    this.stopStrategies = stopStrategies;
    this.sleepOrWait = sleepOrWait;
  }

  /**
   * 执行
   *
   * @param callable Callbale
   * @return result
   */
  public T call(Callable<T> callable) {
    long startTime = System.currentTimeMillis();
    long attempNums = 0;
    while (true) {
      attempNums++;
      Attempt<T> attempt;
      try {
        T value = timeLimiter.call(callable);
        attempt = new Attempt<>(attempNums, value, System.currentTimeMillis() - startTime);
      } catch (Exception e) {
        attempt = new Attempt<>(attempNums, e, System.currentTimeMillis() - startTime);
      }

      if (!retryPredicate.test(attempt)) {
        if (attempt.hasException()) {
          throw new RetryException(attempt.getException());
        } else {
          return attempt.getResult();
        }
      }

      for (StopStrategy<T> stopStrategy : stopStrategies) {
        if (stopStrategy.check(attempt)) {
          throw new RetryException(attempt.getException());
        }
      }

      try {
        if (sleepOrWait) {
          Thread.sleep(waitTime);
        } else {
          synchronized (this) {
            this.wait(waitTime);
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

}
