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
package vip.justlive.oxygen.jdbc.record;

import java.util.List;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.jdbc.page.Page;

/**
 * record utils
 *
 * @author wubo
 * @deprecated Replaced by Entity
 */
@Deprecated
@UtilityClass
public class Record {

  /**
   * 根据主键获取record
   *
   * @param clazz 类
   * @param id    主键
   * @param <T>   泛型
   * @return 记录
   */
  public <T> T findById(Class<T> clazz, Object id) {
    return Entity.parse(clazz).findById(id);
  }

  /**
   * 根据主键获取records
   *
   * @param clazz 类
   * @param ids   主键
   * @param <T>   泛型
   * @return 记录
   */
  public <T> List<T> findByIds(Class<T> clazz, List<?> ids) {
    return Entity.parse(clazz).findByIds(ids);
  }

  /**
   * 根据record属性值获取集合
   *
   * @param obj record
   * @param <T> 泛型
   * @return list
   */
  public <T> List<T> find(T obj) {
    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) obj.getClass();
    return Entity.parse(clazz).find(obj);
  }

  /**
   * 根据record属性值获取一个值
   *
   * @param obj record
   * @param <T> 泛型
   * @return list
   */
  public <T> T findOne(T obj) {
    return findOne(obj, true);
  }

  /**
   * 根据record属性值获取一个值
   *
   * @param obj     record
   * @param throwEx 是否抛出异常
   * @param <T>     泛型
   * @return list
   */
  public <T> T findOne(T obj, boolean throwEx) {
    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) obj.getClass();
    return Entity.parse(clazz).findOne(obj, throwEx);
  }

  /**
   * 获取所有集合
   *
   * @param clazz 类型
   * @param <T>   泛型
   * @return list
   */
  public <T> List<T> findAll(Class<T> clazz) {
    return Entity.parse(clazz).findAll();
  }

  /**
   * 获取分页
   *
   * @param obj  record
   * @param page 分页参数
   * @param <T>  泛型
   * @return list
   */
  public <T> List<T> page(T obj, Page<T> page) {
    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) obj.getClass();
    return Entity.parse(clazz).page(obj, page);
  }

  /**
   * count
   *
   * @param obj record
   * @param <T> 泛型
   * @return count
   */
  public <T> int count(T obj) {
    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) obj.getClass();
    return Entity.parse(clazz).count(obj);
  }

  /**
   * 修改record
   *
   * @param obj record
   * @param <T> 泛型
   * @return updated
   */
  public <T> int updateById(T obj) {
    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) obj.getClass();
    return Entity.parse(clazz).updateById(obj);
  }

  /**
   * 插入record
   *
   * @param obj record
   * @param <T> 泛型
   * @return updated
   */
  public <T> int insert(T obj) {
    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) obj.getClass();
    return Entity.parse(clazz).insert(obj);
  }

  /**
   * 批量插入
   *
   * @param list 集合
   * @param <T>  泛型
   */
  public <T> void insertBatch(List<T> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) list.get(0).getClass();
    Entity.parse(clazz).insertBatch(list);
  }

  /**
   * 根据id删除record
   *
   * @param clazz record
   * @param id    主键
   * @param <T>   泛型
   * @return updated
   */
  public <T> int deleteById(Class<T> clazz, Object id) {
    return Entity.parse(clazz).deleteById(id);
  }

  /**
   * 根据ids删除record
   *
   * @param clazz record
   * @param ids   注解列表
   * @param <T>   泛型
   * @return updated
   */
  public <T> int deleteByIds(Class<T> clazz, List<?> ids) {
    return Entity.parse(clazz).deleteByIds(ids);
  }

  /**
   * 根据record属性删除
   *
   * @param obj record
   * @param <T> 泛型
   * @return updated
   */
  public <T> int delete(T obj) {
    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) obj.getClass();
    return Entity.parse(clazz).delete(obj);
  }

}
