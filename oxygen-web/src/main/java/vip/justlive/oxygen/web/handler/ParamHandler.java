/*
 * Copyright (C) 2018 justlive1
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
package vip.justlive.oxygen.web.handler;

/**
 * 参数处理
 *
 * @author wubo
 */
public interface ParamHandler {

  /**
   * 是否支持
   *
   * @param type 参数类型
   * @return true表示支持
   */
  boolean supported(Class<?> type);

  /**
   * 处理参数
   */
  Object handle(Class<?> type);
}
