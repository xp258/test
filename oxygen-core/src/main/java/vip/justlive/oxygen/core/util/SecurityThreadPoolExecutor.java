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

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 增加安全校验的线程池
 *
 * 设置addWorkerFirst=true可先创建线程达到maximumPoolSize，而不是等队列满了才创建线程
 *
 * @author wubo
 */
@Slf4j
@Accessors(chain = true)
public class SecurityThreadPoolExecutor extends ThreadPoolExecutor {

  private static final Method ADD_WORKER;

  static {
    Method method = null;
    try {
      method = ThreadPoolExecutor.class
          .getDeclaredMethod("addWorker", Runnable.class, boolean.class);
      method.setAccessible(true);
    } catch (NoSuchMethodException e) {
      log.warn("ThreadPoolExecutor.addWorker cannot found", e);
    }
    ADD_WORKER = method;
  }

  @Setter
  @Getter
  private SecurityChecker securityChecker;
  @Getter
  @Setter
  private boolean addWorkerFirst;

  private boolean addWorkerFailed;

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
  }

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
  }

  public SecurityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
      RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
  }

  @Override
  public void execute(Runnable command) {
    if (addWorkerFirst && getCorePoolSize() < getMaximumPoolSize() && addTaskToWorker(command)) {
      return;
    }
    super.execute(command);
  }

  @Override
  public void shutdown() {
    if (securityChecker != null) {
      securityChecker.checkPermission();
    }
    super.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    if (securityChecker != null) {
      securityChecker.checkPermission();
    }
    return super.shutdownNow();
  }

  private boolean addTaskToWorker(Runnable command) {
    if (ADD_WORKER == null || addWorkerFailed) {
      return false;
    }
    try {
      return (boolean) ADD_WORKER.invoke(this, command, false);
    } catch (@SuppressWarnings("squid:S1181") Throwable throwable) {
      log.warn("ThreadPoolExecutor.addWorker invoke failed", throwable);
      addWorkerFailed = true;
      return false;
    }
  }
}
