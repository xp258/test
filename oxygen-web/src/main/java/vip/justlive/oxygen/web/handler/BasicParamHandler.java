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

import vip.justlive.oxygen.core.convert.DefaultConverterService;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.web.http.Request;

/**
 * Params参数处理
 *
 * @author wubo
 */
public class BasicParamHandler implements ParamHandler {

  @Override
  public boolean supported(Class<?> type) {
    return true;
  }

  @Override
  public Object handle(Class<?> type) {
    Request request = Request.current();
    DefaultConverterService converterService = DefaultConverterService.sharedConverterService();
    if (ClassUtils.isPrimitive(type)) {
      if (converterService.canConverter(String.class, type)) {
      }
    }
    return null;
  }
}
