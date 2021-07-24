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

import lombok.Getter;
import lombok.Setter;

/**
 * jobTrigger基础类
 *
 * @author wubo
 */
@Getter
@Setter
public abstract class CoreJobTrigger implements JobTrigger {

  private String jobKey;

  private Long startTime;
  private Long endTime;

  private Long previousFireTime;
  private Long nextFireTime;
}
