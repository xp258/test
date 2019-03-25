/*
 *  Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */
package vip.justlive.oxygen.web.result;

import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * Redirect结果处理
 *
 * @author wubo
 */
public class RedirectResultHandler implements ResultHandler {

  private static final String PROTOCOLS_REGEX = "^\\w+://.*";
  private static final int DEFAULT_HTTP_PORT = 80;
  private static final int DEFAULT_HTTPS_PORT = 443;

  @Override
  public boolean support(Class<?> type, Object data) {
    return Result.class.isAssignableFrom(type) && data != null && RedirectResult.class
        .isAssignableFrom(data.getClass());
  }

  @Override
  public void apply(RoutingContext ctx, Object data) {
    RedirectResult result = (RedirectResult) data;
    Request request = ctx.request();
    Response response = ctx.response();
    response.setStatus(result.getStatus());

    StringBuilder sb = new StringBuilder();
    if (result.getUrl().matches(PROTOCOLS_REGEX)) {
      sb.append(result.getUrl());
    } else {
      if (request.isSecure()) {
        sb.append(Constants.HTTPS_PREFIX);
      } else {
        sb.append(Constants.HTTP_PREFIX);
      }
      sb.append(request.getHost());
      if (request.getPort() != DEFAULT_HTTP_PORT && request.getPort() != DEFAULT_HTTPS_PORT) {
        sb.append(Constants.COLON).append(request.getPort());
      }
      if (!result.getUrl().startsWith(Constants.SLASH)) {
        sb.append(ctx.requestPath()).append(Constants.SLASH);
      }
      sb.append(result.getUrl());
    }
    response.setHeader("Location", sb.toString());
  }
}
