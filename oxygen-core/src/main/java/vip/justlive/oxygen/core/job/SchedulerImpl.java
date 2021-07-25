/*
 * Copyright (C) 2021 the original author or authors.
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
package vip.justlive.oxygen.core.job;

import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * 调度实现类
 * 
 * @author wubo
 *
 */
@RequiredArgsConstructor
public class SchedulerImpl implements Scheduler {

  private final JobStore jobStore;

  @Override
  public void start() {
    // TODO Auto-generated method stub

  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isShutdown() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void addJob(JobInfo jobInfo, boolean replaceExisting) {
    jobStore.storeJob(jobInfo, replaceExisting);
  }

  @Override
  public void scheduleJob(JobInfo jobInfo, JobTrigger trigger) {
    Long nextFireTime = trigger.computeNextFireTime(System.currentTimeMillis());
    if (nextFireTime == null) {
      throw new IllegalArgumentException(
          "job will never fire with triggerKey '" + trigger.getKey() + "'");
    }
    jobStore.storeJob(jobInfo, true);
    jobStore.storeTrigger(trigger);
  }

  @Override
  public void scheduleJob(JobTrigger trigger) {
    Long nextFireTime = trigger.computeNextFireTime(System.currentTimeMillis());
    if (nextFireTime == null) {
      throw new IllegalArgumentException(
          "job will never fire with triggerKey '" + trigger.getKey() + "'");
    }
    jobStore.storeTrigger(trigger);
  }

  @Override
  public void removeJob(String jobKey) {
    jobStore.removeJob(jobKey);
  }

  @Override
  public void triggerJob(String jobKey) {
    FixedTimeJobTrigger trigger = new FixedTimeJobTrigger(UUID.randomUUID().toString(), jobKey);
    long now = System.currentTimeMillis();
    trigger.setStartTime(now);
    trigger.computeNextFireTime(now);
    jobStore.storeTrigger(trigger);
  }

  @Override
  public void pauseJob(String jobKey) {
    jobStore.pauseJob(jobKey);
  }

  @Override
  public void pauseTrigger(String triggerKey) {
    jobStore.pauseTrigger(triggerKey);
  }

  @Override
  public void resumeJob(String jobKey) {
    jobStore.resumeJob(jobKey);
  }

  @Override
  public void resumeTrigger(String triggerKey) {
    jobStore.resumeTrigger(triggerKey);
  }
}
