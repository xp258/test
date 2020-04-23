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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.util.ThreadUtils;

/**
 * @author wubo
 */
@Slf4j
public class WheelTimerTest {


  @Test
  public void test() throws ExecutionException, InterruptedException {
    WheelTimer timer = new WheelTimer(1000, 5);
    timer.start();

    AtomicInteger count = new AtomicInteger();
    Callable<Integer> callable = () -> {
      int c = count.incrementAndGet();
      log.info("xxx {}", c);
      return c;
    };

    timer.addTask(callable, 2, TimeUnit.SECONDS);
    timer.addTask(callable, 3, TimeUnit.SECONDS);
    Future<Integer> future = timer.addTask(callable, 7, TimeUnit.SECONDS);

    Assert.assertEquals(0, count.get());

    ThreadUtils.sleep(2000);
    Assert.assertEquals(1, count.get());

    ThreadUtils.sleep(3000);
    Assert.assertEquals(2, count.get());

    ThreadUtils.sleep(3000);
    Assert.assertEquals(3, count.get());

    System.out.println(future.get());

    timer.stop();
  }

  public void test1() {
    WheelTimer timer = new WheelTimer(1000, 60, 2);
    timer.start();

    timer.addTask(() -> call(2), 2, TimeUnit.MILLISECONDS);
    timer.addTask(() -> call(5), 5, TimeUnit.MILLISECONDS);
    timer.addTask(() -> call(3), 3, TimeUnit.SECONDS);
    timer.addTask(() -> call(6), 6, TimeUnit.SECONDS);
    for (int i = 0; i < 1000; i++) {
      final int a = 1000 - i;
      timer.addTask(() -> call(a), a, TimeUnit.SECONDS);
    }

    ThreadUtils.sleep(200000);
    timer.stop();
  }

  private Integer call(int i) {
    log.info("call {}", i);
    return i;
  }

}