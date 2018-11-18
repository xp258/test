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

/**
 * 终止策略
 *
 * @param <T> 泛型
 * @author wubo
 */
public interface StopStrategy<T> {

  /**
   * 检查是否终止
   *
   * @param attempt 尝试
   * @return true终止
   */
  boolean check(Attempt<T> attempt);

  /**
   * never stop
   *
   * @param <T> 泛型
   * @return NoStopStrategy
   */
  static <T> StopStrategy<T> neverStop() {
    return new NoStopStrategy<>();
  }

  /**
   * 最大尝试次数终止器
   *
   * @param maxAttempt 最大尝试次数
   * @param <T> 泛型
   * @return MaxAttemptStopStrategy
   */
  static <T> StopStrategy<T> maxAttemptStopStrategy(long maxAttempt) {
    return new MaxAttemptStopStrategy<>(maxAttempt);
  }
}
