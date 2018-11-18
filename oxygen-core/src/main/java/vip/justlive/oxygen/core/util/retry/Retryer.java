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

/**
 * 重试器
 *
 * @param <T> 泛型
 * @author wubo
 */
public class Retryer<T> {

  Retryer(List<StopStrategy<T>> stopStrategies, long waitTime,
      TimeLimiter<T> timeLimiter) {
    this.stopStrategies = stopStrategies;
    this.waitTime = waitTime;
    this.timeLimiter = timeLimiter;
  }

  private final List<StopStrategy<T>> stopStrategies;
  private final long waitTime;
  private final TimeLimiter<T> timeLimiter;


}
