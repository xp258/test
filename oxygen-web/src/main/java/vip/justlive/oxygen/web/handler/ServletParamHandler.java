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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.mapping.DataBinder;

/**
 * servlet内置参数处理
 *
 * @author wubo
 */
public class ServletParamHandler implements ParamHandler {

  @Override
  public boolean supported(DataBinder dataBinder) {
    return dataBinder.getType() == HttpServletResponse.class
        || dataBinder.getType() == HttpServletRequest.class;
  }

  @Override
  public Object handle(DataBinder dataBinder) {
    if (dataBinder.getType() == HttpServletRequest.class) {
      return Request.current().getOriginalRequest();
    }
    if (dataBinder.getType() == HttpServletResponse.class) {
      return Response.current().getOriginalResponse();
    }
    return null;
  }
}
