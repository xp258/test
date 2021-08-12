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
package vip.justlive.oxygen.jdbc.job;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.job.JobInfo;
import vip.justlive.oxygen.core.job.JobStore;
import vip.justlive.oxygen.core.job.JobTrigger;
import vip.justlive.oxygen.core.job.Signaler;
import vip.justlive.oxygen.core.job.TriggerFiredResult;
import vip.justlive.oxygen.jdbc.Jdbc;
import vip.justlive.oxygen.jdbc.JdbcException;
import vip.justlive.oxygen.jdbc.record.Entity;

/**
 * jdbc实现的job store
 *
 * @author wubo
 */
@Slf4j
public class JdbcJobStore implements JobStore {

  public static final String LOCK_JOB_ACCESS = "job_access";
  public static final String LOCK_TRIGGER_ACCESS = "trigger_access";

  private Signaler signaler;
  private String dataSourceName;

  private final TriggerConverter converter = new TriggerConverter();

  @Override
  public void initialize(Signaler signaler) {
    this.signaler = signaler;
    this.dataSourceName = "";
  }

  @Override
  public void storeJob(JobInfo jobInfo, boolean replaceExisting) {
    executeInLock(LOCK_JOB_ACCESS, conn -> {
      Entity<JobInfoEntity> model = Entity.parse(JobInfoEntity.class);
      JobInfoEntity entity = model.findOne(new JobInfoEntity().setJobKey(jobInfo.getKey()));
      if (entity != null) {
        if (!replaceExisting) {
          throw new IllegalArgumentException("job '" + jobInfo.getKey() + "' already exists");
        }
        model.updateById(conn,
            new JobInfoEntity().setId(entity.getId()).setDescription(jobInfo.getDescription())
                .setParam(jobInfo.getParam()).setHandlerClass(jobInfo.getHandlerClass()));
      } else {
        model.insert(
            new JobInfoEntity().setJobKey(jobInfo.getKey()).setDescription(jobInfo.getDescription())
                .setParam(jobInfo.getParam()).setHandlerClass(jobInfo.getHandlerClass()));
      }
      return null;
    });
  }

  @Override
  public JobInfo getJobInfo(String jobKey) {
    return executeInLock(LOCK_JOB_ACCESS, conn -> {
      JobInfoEntity entity = Entity.parse(JobInfoEntity.class)
          .findOne(conn, new JobInfoEntity().setJobKey(jobKey));
      if (entity == null) {
        return null;
      }
      return new JobInfo().setKey(entity.getJobKey()).setDescription(entity.getDescription())
          .setHandlerClass(entity.getHandlerClass()).setParam(entity.getParam());
    });
  }

  @Override
  public void removeJob(String jobKey) {
    executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      Entity<JobInfoEntity> model = Entity.parse(JobInfoEntity.class);
      JobInfoEntity entity = model.findOne(conn, new JobInfoEntity().setJobKey(jobKey));
      if (entity == null) {
        return null;
      }
      Entity.parse(JobTriggerEntity.class)
          .delete(conn, new JobTriggerEntity().setJobKey(entity.getJobKey()));
      model.deleteById(conn, entity.getId());
      return null;
    });
  }

  @Override
  public void storeTrigger(JobTrigger trigger) {
  }

  @Override
  public List<JobTrigger> getJobTrigger(String jobKey) {
    return executeInLock(LOCK_TRIGGER_ACCESS, conn -> {
      List<JobTriggerEntity> list = Entity.parse(JobTriggerEntity.class)
          .find(conn, new JobTriggerEntity().setJobKey(jobKey));
      List<JobTrigger> triggers = new ArrayList<>(list.size());
      for (JobTriggerEntity entity : list) {
        JobTrigger jobTrigger = converter.convert(entity);
        if (jobTrigger != null) {
          triggers.add(jobTrigger);
        }
      }
      return triggers;
    });
  }

  @Override
  public void removeTrigger(String triggerKey) {
  }

  @Override
  public void pauseJob(String jobKey) {
  }

  @Override
  public void pauseTrigger(String triggerKey) {
  }

  @Override
  public void resumeJob(String jobKey) {
  }

  @Override
  public void resumeTrigger(String triggerKey) {
  }

  @Override
  public List<JobTrigger> acquireNextTriggers(long maxTimestamp, int maxSize) {
    return null;
  }

  @Override
  public void releaseTrigger(JobTrigger trigger) {
  }

  @Override
  public TriggerFiredResult triggerFired(JobTrigger trigger) {
    return null;
  }

  @Override
  public void triggerCompleted(JobTrigger trigger, int state) {

    signaler.schedulingChange();
  }

  private final ThreadLocal<Set<String>> lockOwners = new ThreadLocal<>();

  private Set<String> getThreadLocks() {
    Set<String> threadLocks = lockOwners.get();
    if (threadLocks == null) {
      threadLocks = new HashSet<>();
      lockOwners.set(threadLocks);
    }
    return threadLocks;
  }

  protected <T> T executeInLock(String lockName, Function<Connection, T> function) {
    boolean isLocked = false;
    Connection conn = null;
    try {
      conn = Jdbc.getConnection(dataSourceName);
      try {
        conn.setAutoCommit(false);
      } catch (SQLException e) {
        throw JdbcException.wrap(e);
      }
      if (lockName != null) {
        isLocked = obtainLock(conn, lockName);
      }
      return function.apply(conn);
    } finally {
      try {
        releaseLock(lockName, isLocked);
      } finally {
        cleanupConnection(conn);
      }
    }
  }

  protected boolean obtainLock(Connection conn, String lockName) {
    if (!getThreadLocks().contains(lockName)) {
      // executeSQL(conn, lockName, expandedSQL, expandedInsertSQL);
      if (log.isDebugEnabled()) {
        log.debug("Lock '{}' given to: {}", lockName, Thread.currentThread().getName());
      }
      getThreadLocks().add(lockName);
    } else if (log.isDebugEnabled()) {
      log.debug("Lock '{}' Is already owned by: {}", lockName, Thread.currentThread().getName());
    }
    return true;
  }

  protected void releaseLock(String lockName, boolean isLocked) {
    if (!isLocked) {
      return;
    }
    Set<String> locks = getThreadLocks();
    if (locks.contains(lockName)) {
      if (log.isDebugEnabled()) {
        log.debug("Lock '{}' returned by: {}", lockName, Thread.currentThread().getName());
      }
      locks.remove(lockName);
      if (locks.isEmpty()) {
        lockOwners.remove();
      }
    } else if (log.isDebugEnabled()) {
      log.warn(
          "Lock '{}' attempt to return by: {} -- but not owner!",
          lockName,
          Thread.currentThread().getName());
    }
  }

  protected void cleanupConnection(Connection conn) {
    if (conn != null) {
      try {
        conn.commit();
        conn.close();
      } catch (SQLException e) {
        throw JdbcException.wrap(e);
      }
    }
  }
}
