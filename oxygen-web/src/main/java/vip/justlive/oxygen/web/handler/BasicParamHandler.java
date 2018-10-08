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
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.mapping.DataBinder;
import vip.justlive.oxygen.web.mapping.DataBinder.SCOPE;

/**
 * Params参数处理
 *
 * @author wubo
 */
public class BasicParamHandler implements ParamHandler {

  @Override
  public boolean supported(DataBinder dataBinder) {
    return dataBinder.getScope() == SCOPE.PARAM;
  }

  @Override
  public Object handle(DataBinder dataBinder) {
    Request request = Request.current();
    DefaultConverterService converterService = DefaultConverterService.sharedConverterService();
    if (converterService.canConverter(String.class, dataBinder.getType())) {
      String value = request.getParam(dataBinder.getName());
      return converterService.convert(value, dataBinder.getType());
    }
    // 复杂类型

    return null;
  }
}
