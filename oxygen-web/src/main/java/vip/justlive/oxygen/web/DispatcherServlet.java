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
package vip.justlive.oxygen.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.web.mapping.Action;
import vip.justlive.oxygen.web.mapping.Mappings;
import vip.justlive.oxygen.web.mapping.Request.HttpMethod;

/**
 * 请求分发器
 *
 * @author wubo
 */
@Slf4j
public class DispatcherServlet extends HttpServlet {

  private void doService(HttpServletRequest req, HttpServletResponse resp, HttpMethod httpMethod) {
    String requestPath = getRequestPath(req);
    if (log.isDebugEnabled()) {
      log.debug("DispatcherServlet handler request for [{}] on method [{}]", requestPath,
          httpMethod);
    }

    Action action = Mappings.findActionByPath(requestPath, httpMethod);
    if (action == null) {
      // TODO not found
      return;
    }


  }


  private String getRequestPath(HttpServletRequest req) {
    return req.getRequestURI();
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (HttpMethod.PATCH.name().equals(req.getMethod())) {
      doService(req, resp, HttpMethod.PATCH);
    } else {
      super.service(req, resp);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.GET);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.POST);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.DELETE);
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.HEAD);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.PUT);
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.OPTIONS);
  }


  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) {
    doService(req, resp, HttpMethod.TRACE);
  }


}
