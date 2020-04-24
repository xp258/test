/*
 * Copyright (C) 2020 the original author or authors.
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

package vip.justlive.oxygen.core.util.timer;

import lombok.extern.slf4j.Slf4j;

/**
 * 周期任务
 *
 * @author wubo
 */
@Slf4j
public class PeriodTask extends Task<Void> {

  final long period;
  final WheelTimer timer;

  PeriodTask(long deadline, Runnable runnable, long period, WheelTimer timer) {
    super(deadline, runnable);
    this.period = period;
    this.timer = timer;
  }

  @Override
  public void run() {
    if (!runAndReset()) {
      return;
    }
    if (period > 0) {
      deadline = System.currentTimeMillis() + period;
    } else {
      deadline -= period;
    }
    timer.addTask(this);
  }
}
