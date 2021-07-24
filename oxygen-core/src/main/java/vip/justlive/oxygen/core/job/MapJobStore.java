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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * map实现的job store
 *
 * @author wubo
 */
public class MapJobStore implements JobStore {

  private final Map<String, JobInfo> jobInfos = new ConcurrentHashMap<>(4);


  @Override
  public void storeJob(JobInfo jobInfo, boolean replaceExisting) {
    if (replaceExisting) {
      jobInfos.put(jobInfo.getKey(), jobInfo);
    } else {
      jobInfos.putIfAbsent(jobInfo.getKey(), jobInfo);
    }
  }

  @Override
  public JobInfo getJobInfo(String jobKey) {
    return jobInfos.get(jobKey);
  }

  @Override
  public void removeJob(String jobKey) {
    jobInfos.remove(jobKey);
  }

  @Override
  public void storeTrigger(JobTrigger trigger) {

  }

  @Override
  public JobTrigger getJobTrigger(String jobKey) {
    return null;
  }

  @Override
  public void removeTrigger(String jobKey) {

  }

  @Override
  public List<JobTrigger> acquireNextTriggers(long maxTimestamp, int maxSize) {
    return null;
  }

  @Override
  public void releaseTrigger(JobTrigger trigger) {

  }

  @Override
  public void triggerFired(JobTrigger trigger) {

  }

  @Override
  public void triggerCompleted(JobTrigger trigger, int state) {

  }
}
