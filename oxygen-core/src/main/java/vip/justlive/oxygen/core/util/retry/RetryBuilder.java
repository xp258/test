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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import vip.justlive.oxygen.core.util.ThreadUtils;

/**
 * 重试构造器
 *
 * @param <T> 泛型
 * @author wubo
 */
public class RetryBuilder<T> {

  private static final long DEAULT_MAX_ATTEMPT = 3;

  /**
   * 等待时间
   */
  private long waitTime = 0;
  /**
   * 使用sleep还是wait
   */
  private boolean sleepOrWait = true;
  private Predicate<Attempt<T>> retryPredicate = attempt -> false;
  private List<StopStrategy<T>> stopStrategies = new LinkedList<>();
  /**
   * 超时限制器
   */
  private TimeLimiter<T> timeLimiter;

  private RetryBuilder() {
  }

  /**
   * 创建构造器
   *
   * @return builder
   */
  public static <T> RetryBuilder<T> newBuilder() {
    return new RetryBuilder<>();
  }

  /**
   * 设置最大尝试次数
   *
   * @param maxAttempt 最大尝试次数
   * @return builder
   */
  public RetryBuilder<T> withMaxAttempt(long maxAttempt) {
    this.stopStrategies.add(new MaxAttemptStopStrategy<>(maxAttempt));
    return this;
  }

  /**
   * 设置尝试之间的等待时间
   *
   * @param waitTime 等待时间
   * @return builder
   */
  public RetryBuilder<T> withWaitTime(long waitTime) {
    if (waitTime < 0) {
      throw new IllegalArgumentException(String.format("waitTime [%s] mast be >= 0", waitTime));
    }
    this.waitTime = waitTime;
    return this;
  }

  /**
   * 设置超时限制
   *
   * @param timeout 超时时间
   * @param timeUnit 时间单位
   * @return builder
   */
  public RetryBuilder<T> withTimeLimit(long timeout, TimeUnit timeUnit) {
    return withTimeLimit(timeout, timeUnit,
        ThreadUtils.newThreadPool(5, 10, 100, 1000, "retry-time-limiter-%d"));
  }

  /**
   * 设置超时限制
   *
   * @param timeout 超时时间
   * @param timeUnit 时间单位
   * @param executorService 线程池
   * @return builder
   */
  public RetryBuilder<T> withTimeLimit(long timeout, TimeUnit timeUnit,
      ExecutorService executorService) {
    this.timeLimiter = TimeLimiter.fixedTimeLimit(timeout, timeUnit, executorService);
    return this;
  }

  /**
   * 使用sleep进行阻塞
   *
   * @return builder
   */
  public RetryBuilder<T> withSleepBlock() {
    this.sleepOrWait = true;
    return this;
  }

  /**
   * 使用wait进行阻塞
   *
   * @return builder
   */
  public RetryBuilder<T> withWaitBlock() {
    this.sleepOrWait = false;
    return this;
  }

  /**
   * 发生异常重试
   *
   * @return builder
   */
  public RetryBuilder<T> retryIfException() {
    return retryIf(Attempt::hasException);
  }

  /**
   * 发生指定异常进行重试
   *
   * @param exceptionClass 异常类型
   * @return builder
   */
  public RetryBuilder<T> retryIfException(Class<? extends Exception> exceptionClass) {
    return retryIf(attempt -> attempt.hasException() && exceptionClass
        .isAssignableFrom(attempt.getException().getClass()));
  }

  /**
   * 判断结果是否重试
   *
   * @param resultPredicate 结果判断
   * @return builder
   */
  public RetryBuilder<T> retryIfResult(Predicate<T> resultPredicate) {
    return retryIf(attempt -> !attempt.hasException() && resultPredicate.test(attempt.getResult()));
  }

  /**
   * 判断是否重试
   *
   * @param predicate 判断
   * @return builder
   */
  public RetryBuilder<T> retryIf(Predicate<Attempt<T>> predicate) {
    this.retryPredicate = this.retryPredicate.or(predicate);
    return this;
  }

  /**
   * 构造同步重试器
   *
   * @return Retryer
   */
  public Retryer<T> build() {
    if (timeLimiter == null) {
      timeLimiter = TimeLimiter.noTimeLimit();
    }
    if (stopStrategies.isEmpty()) {
      stopStrategies.add(new MaxAttemptStopStrategy<>(DEAULT_MAX_ATTEMPT));
    }
    return new Retryer<>(waitTime, timeLimiter, retryPredicate, stopStrategies, sleepOrWait);
  }

}
