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
package vip.justlive.oxygen.web.http;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.web.mapping.Mapping.HttpMethod;

/**
 * multipart请求解析
 *
 * @author wubo
 */
public class MultipartRequestParse extends AbstractRequestParse {

  @Override
  public boolean supported(HttpServletRequest req) {
    return HttpMethod.POST.name().equalsIgnoreCase(req.getMethod()) && req.getContentType()
        .toLowerCase(Locale.ENGLISH).startsWith(Constants.MULTIPART);
  }

  @Override
  public void handle(HttpServletRequest req) {
    Request request = Request.current();
    String contentType = request.getHeader(Constants.CONTENT_TYPE);
    // TODO
  }
}
