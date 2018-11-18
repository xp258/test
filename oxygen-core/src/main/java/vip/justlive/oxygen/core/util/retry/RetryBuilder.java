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
import vip.justlive.oxygen.core.util.ThreadUtils;

/**
 * 重试构造器
 *
 * @param <T> 泛型
 * @author wubo
 */
public class RetryBuilder<T> {

  private RetryBuilder() {
  }

  /**
   * 等待时间
   */
  private long waitTime = 0;
  private List<StopStrategy<T>> stopStrategies = new LinkedList<>();
  /**
   * 超时限制器
   */
  private TimeLimiter<T> timeLimiter;

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
    this.stopStrategies.add(StopStrategy.maxAttemptStopStrategy(maxAttempt));
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
   * 构造同步重试器
   *
   * @return Retryer
   */
  public Retryer<T> build() {

    return new Retryer<>(stopStrategies, waitTime, timeLimiter);
  }

}
