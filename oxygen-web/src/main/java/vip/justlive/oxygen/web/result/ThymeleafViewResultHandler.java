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

import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * thymeleaf 视图处理
 *
 * @author wubo
 */
@Slf4j
public class ThymeleafViewResultHandler implements ResultHandler {

  private static final boolean THYMELEAF_SUPPORTED = ClassUtils
      .isPresent("org.thymeleaf.Thymeleaf");
  private static final String SUFFIX = ".html";

  private ThymeleafResolver resolver;

  @Override
  public boolean support(Class<?> type, Object data) {
    if (THYMELEAF_SUPPORTED && Result.class.isAssignableFrom(type) && data != null
        && ViewResult.class.isAssignableFrom(data.getClass())) {
      ViewResult result = (ViewResult) data;
      return result.getPath() != null && result.getPath().endsWith(SUFFIX);
    }
    return false;
  }

  @Override
  public void apply(RoutingContext ctx, Object data) {
    ViewResult result = (ViewResult) data;
    Request request = ctx.request();
    Response response = ctx.response();
    response.setContentType(Constants.TEXT_HTML);
    check();
    try {
      response.getOut().write(
          resolver.handler(request, response, result.getPath(), result.getData())
              .getBytes(Charset.forName(response.getEncoding())));
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private synchronized void check() {
    if (resolver == null) {
      resolver = new ThymeleafResolver();
    }
  }
}
