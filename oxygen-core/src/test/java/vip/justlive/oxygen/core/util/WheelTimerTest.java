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

package vip.justlive.oxygen.core.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author wubo
 */
public class WheelTimerTest {

  @Test
  public void test() {
    WheelTimer timer = new WheelTimer(1000, 60);
    test(timer);
    timer = new WheelTimer(1000, 60, 2);
    test(timer);
  }

  private void test(WheelTimer timer) {
    timer.start();

    AtomicInteger count = new AtomicInteger();
    Callable<Integer> callable = count::incrementAndGet;

    timer.addTask(callable, 2, TimeUnit.SECONDS);
    Future<Integer> fuc = timer.addTask(() -> {
      timer.stop();
      return 1;
    }, 3, TimeUnit.SECONDS);
    timer.addTask(callable, 5, TimeUnit.SECONDS);

    ThreadUtils.sleep(500);
    Assert.assertEquals(0, count.get());

    ThreadUtils.sleep(3000);
    Assert.assertEquals(1, count.get());

    ThreadUtils.sleep(3000);
    Assert.assertEquals(2, count.get());

    try {
      fuc.get();
    } catch (Exception e) {
      Assert.assertTrue(e.getCause() instanceof IllegalStateException);
      Assert
          .assertEquals("WheelTimer.stop() cannot be called from Task", e.getCause().getMessage());
    }

    timer.stop();
  }
}