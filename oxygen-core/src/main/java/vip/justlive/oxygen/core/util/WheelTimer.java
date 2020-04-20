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

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 时间轮
 *
 * @author wubo
 */
@Slf4j
public class WheelTimer {

  private static final int STATE_INIT = 0;
  private static final int STATE_STARTED = 1;
  private static final int STATE_SHUTDOWN = 2;
  private static final int MAX_SIZE_PER_TAKE;
  private static final ThreadFactory FACTORY;
  private static final ScheduledExecutorService SCHEDULED;

  static {
    MAX_SIZE_PER_TAKE = Integer.getInteger("wheel_timer.max_size_per_take", 100_000);
    FACTORY = new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.NORM_PRIORITY)
        .setNameFormat("wheel-task-%d").build();
    int poolSize = Integer.getInteger("wheel_timer.scheduled_pool_size", 2);
    SCHEDULED = ThreadUtils.newScheduledExecutor(poolSize, "wheel-timer-%d");
  }

  private final long duration;
  private final Slot[] slots;
  private final Executor executor;
  private final int mask;
  private final Queue<Task<?>> tasks = new ConcurrentLinkedQueue<>();
  private final AtomicInteger state = new AtomicInteger(STATE_INIT);
  private final Set<Long> taskThreadIds = Collections.synchronizedSet(new HashSet<>());

  private long startTime;
  private long tick;

  public WheelTimer(long duration, int ticks) {
    this(duration, ticks, 0);
  }

  public WheelTimer(long duration, int ticks, int taskPoolSize) {
    this.duration = duration;
    this.slots = createSlots(ticks);
    this.mask = this.slots.length - 1;
    if (taskPoolSize <= 0) {
      this.executor = MoreObjects.directExecutor();
    } else {
      this.executor = new ThreadPoolExecutor(taskPoolSize, taskPoolSize, 0, TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(), FACTORY);
    }
  }

  public <T> Future<T> addTask(Callable<T> callable, long delay, TimeUnit unit) {
    long deadline = System.currentTimeMillis() + unit.toMillis(delay) - startTime;
    if (delay > 0 && deadline < 0) {
      deadline = Long.MAX_VALUE;
    }
    start();
    Task<T> task = new Task<>(deadline, callable, this);
    tasks.offer(task);
    return task;
  }


  public void start() {
    int stateValue = state.get();
    if (stateValue != STATE_INIT && stateValue != STATE_STARTED) {
      throw new IllegalStateException("WheelTimer.state is illegal");
    }
    if (!state.compareAndSet(STATE_INIT, STATE_STARTED)) {
      return;
    }
    startTime = System.currentTimeMillis();
    log.info("WheelTimer started.");
    start0();
  }

  public void stop() {
    if (taskThreadIds.contains(Thread.currentThread().getId())) {
      throw new IllegalStateException("WheelTimer.stop() cannot be called from Task");
    }
    if (!state.compareAndSet(STATE_STARTED, STATE_SHUTDOWN) && !state
        .compareAndSet(STATE_INIT, STATE_SHUTDOWN) && state.get() != STATE_SHUTDOWN) {
      throw new IllegalStateException("WheelTimer.state is illegal");
    }
    if (executor instanceof ExecutorService) {
      ((ExecutorService) executor).shutdown();
    }
    taskThreadIds.clear();
    log.info("WheelTimer stopped.");
  }

  private Slot[] createSlots(int ticks) {
    if (ticks <= 0) {
      throw new IllegalArgumentException("ticks must be greater than 0: " + ticks);
    }
    int normalizedTicks = 1;
    while (normalizedTicks < ticks) {
      normalizedTicks <<= 1;
    }
    Slot[] sts = new Slot[normalizedTicks];
    for (int i = 0; i < sts.length; i++) {
      sts[i] = new Slot(this);
    }
    return sts;
  }

  private void moveTaskToSlot() {
    Task<?> task = tasks.poll();
    for (int i = 1; task != null && i < MAX_SIZE_PER_TAKE; i++) {
      if (task.isCancelled() || task.isDone()) {
        continue;
      }
      long calculated = task.deadline / duration;
      task.rounds = (calculated - tick) / slots.length;
      int index = (int) (Math.max(calculated, tick) & mask);
      slots[index].addTask(task);
      task = tasks.poll();
    }
  }

  private void start0() {
    if (state.get() != STATE_STARTED) {
      return;
    }
    long deadline = duration * (tick + 1);
    long current = System.currentTimeMillis() - startTime;
    long sleepTime = deadline - current;
    try {
      if (sleepTime <= 0) {
        moveTaskToSlot();
        int idx = (int) (tick & mask);
        slots[idx].runTask(deadline);
        tick++;
      }
    } finally {
      if (state.get() == STATE_STARTED) {
        SCHEDULED.schedule(this::start0, Math.max(0, sleepTime), TimeUnit.MILLISECONDS);
      }
    }
  }

  @RequiredArgsConstructor
  static class Slot {

    private final WheelTimer timer;
    private Task<?> head;
    private Task<?> tail;

    void runTask(long deadline) {
      Task<?> task = head;
      while (task != null) {
        Task<?> next = task.next;
        if (task.rounds <= 0) {
          next = remove(task);
          if (task.deadline <= deadline) {
            timer.executor.execute(task);
          } else {
            throw new IllegalStateException(
                String.format("task.deadline (%d) > deadline (%d)", task.deadline, deadline));
          }
        } else if (task.isCancelled()) {
          next = remove(task);
        } else {
          task.rounds--;
        }
        task = next;
      }
    }

    void addTask(Task<?> task) {
      if (head == null) {
        head = tail = task;
      } else {
        tail.next = task;
        task.prev = tail;
        tail = task;
      }
    }

    Task<?> remove(Task<?> task) {
      Task<?> next = task.next;
      if (task.prev != null) {
        task.prev.next = next;
      }
      if (task.next != null) {
        task.next.prev = task.prev;
      }
      if (task == head) {
        if (task == tail) {
          tail = null;
          head = null;
        } else {
          head = next;
        }
      } else if (task == tail) {
        tail = task.prev;
      }
      task.prev = null;
      task.next = null;
      return next;
    }
  }

  static class Task<T> implements RunnableFuture<T> {

    private final long deadline;
    private final Callable<T> callable;
    private final CompletableFuture<T> future;
    private final WheelTimer timer;

    private Task<?> next;
    private Task<?> prev;
    private long rounds;

    Task(long deadline, Callable<T> callable, WheelTimer timer) {
      this.deadline = deadline;
      this.callable = callable;
      this.timer = timer;
      this.future = new CompletableFuture<>();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
      return future.isCancelled();
    }

    @Override
    public boolean isDone() {
      return future.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
      return future.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      return future.get(timeout, unit);
    }

    @Override
    public void run() {
      if (future.isCancelled()) {
        return;
      }
      Long id = Thread.currentThread().getId();
      try {
        timer.taskThreadIds.add(id);
        future.complete(callable.call());
      } catch (Throwable e) {
        future.completeExceptionally(e);
      } finally {
        timer.taskThreadIds.remove(id);
      }
    }
  }
}
