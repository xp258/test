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
 * 最大尝试次数终止策略
 *
 * @param <T>
 * @author wubo
 */
public class MaxAttemptStopStrategy<T> implements StopStrategy<T> {

  /**
   * 最大尝试次数
   */
  private final long maxAttempt;

  public MaxAttemptStopStrategy(long maxAttempt) {
    if (maxAttempt < 1) {
      throw new IllegalArgumentException(String.format("maxAttempt [%s] must be > 1", maxAttempt));
    }
    this.maxAttempt = maxAttempt;
  }

  @Override
  public boolean check(Attempt<T> attempt) {
    return attempt.getAttemptNumber() > maxAttempt;
  }
}
