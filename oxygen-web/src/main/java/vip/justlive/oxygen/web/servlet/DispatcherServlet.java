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
package vip.justlive.oxygen.web.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.ioc.IocPlugin;
import vip.justlive.oxygen.web.exception.ExceptionHandler;
import vip.justlive.oxygen.web.hook.WebHook;
import vip.justlive.oxygen.web.http.Cookie;
import vip.justlive.oxygen.web.http.HttpMethod;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.RequestParse;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.Route;
import vip.justlive.oxygen.web.router.RouteHandler;
import vip.justlive.oxygen.web.router.Router;
import vip.justlive.oxygen.web.router.RoutingContext;
import vip.justlive.oxygen.web.router.RoutingContextImpl;

/**
 * 请求分发器
 *
 * @author wubo
 */
@Slf4j
public class DispatcherServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final List<RequestParse> REQUEST_PARSES = new LinkedList<>();
  private static final List<WebHook> WEB_HOOKS = new LinkedList<>();

  public static void load() {
    REQUEST_PARSES.addAll(IocPlugin.beanStore().getCastBeanMap(RequestParse.class).values());
    Collections.sort(REQUEST_PARSES);
    WEB_HOOKS.addAll(IocPlugin.beanStore().getCastBeanMap(WebHook.class).values());
    Collections.sort(WEB_HOOKS);
  }

  public static void clear() {
    REQUEST_PARSES.clear();
  }

  private void doService(HttpServletRequest req, HttpServletResponse resp, HttpMethod httpMethod) {
    String requestPath = req.getServletPath();
    if (log.isDebugEnabled()) {
      log.debug("DispatcherServlet accept request for [{}] on method [{}]", requestPath,
          httpMethod);
    }

    Request.set(req);
    Response.set(resp);

    if (requestPath.length() > 1 && requestPath.endsWith(Constants.ROOT_PATH)) {
      requestPath = requestPath.substring(0, requestPath.length() - 1);
    }

    RoutingContext ctx = new RoutingContextImpl(Request.current(), Response.current(), requestPath);
    try {
      RouteHandler handler = Router.lookupStatic(requestPath);
      if (handler == null) {
        Route route = Router.lookup(httpMethod, requestPath);
        if (route == null) {
          handlerNotFound(ctx);
          return;
        }
        Request.current().setRoute(route);
        for (RequestParse requestParse : REQUEST_PARSES) {
          if (requestParse.supported(req)) {
            requestParse.handle(req);
          }
        }
        handler = route.handler();
      }

      if (!invokeBefore(ctx)) {
        return;
      }
      handler.handle(ctx);
      invokeAfter(ctx);
    } catch (Exception e) {
      handlerError(ctx, e);
    } finally {
      invokeFinished(ctx);
      copyResponse(Response.current(), resp);
      Request.clear();
      Response.clear();
    }
  }

  private void invokeFinished(RoutingContext ctx) {
    for (int i = WEB_HOOKS.size() - 1; i >= 0; i--) {
      WEB_HOOKS.get(i).finished(ctx);
    }
  }

  private void invokeAfter(RoutingContext ctx) {
    for (int i = WEB_HOOKS.size() - 1; i >= 0; i--) {
      WEB_HOOKS.get(i).after(ctx);
    }
  }

  private boolean invokeBefore(RoutingContext ctx) {
    for (WebHook webHook : WEB_HOOKS) {
      if (!webHook.before(ctx)) {
        return false;
      }
    }
    return true;
  }

  private void copyResponse(Response response, HttpServletResponse resp) {
    if (response.getContentType() != null) {
      resp.setContentType(response.getContentType());
    }
    resp.setStatus(response.getStatus());
    resp.setCharacterEncoding(response.getEncoding());
    response.getHeaders().forEach(resp::addHeader);
    for (Cookie cookie : response.getCookies().values()) {
      javax.servlet.http.Cookie jCookie = new javax.servlet.http.Cookie(cookie.getName(),
          cookie.getValue());
      jCookie.setPath(cookie.getPath());
      jCookie.setSecure(cookie.isSecure());
      if (cookie.getMaxAge() != null) {
        jCookie.setMaxAge(cookie.getMaxAge());
      }
      if (cookie.getDomain() != null) {
        jCookie.setDomain(cookie.getDomain());
      }
      resp.addCookie(jCookie);
    }
    if (response.getOut().size() > 0) {
      try {
        resp.getOutputStream().write(response.getOut().toByteArray());
      } catch (IOException e) {
        log.error("write outputStream error", e);
      }
    }
  }

  private void handlerNotFound(RoutingContext ctx) {
    if (log.isDebugEnabled()) {
      log.debug("DispatcherServlet not found path [{}] on method [{}]", ctx.requestPath(),
          ctx.request().getMethod());
    }
    IocPlugin.beanStore().getBean(ExceptionHandler.class)
        .handle(ctx, Exceptions.fail("No handler found"), 404);
  }

  private void handlerError(RoutingContext ctx, Exception e) {
    log.error("DispatcherServlet occurs an error for path [{}]", ctx.requestPath(), e);
    Bootstrap.invokeOnExceptionPlugins();
    IocPlugin.beanStore().getBean(ExceptionHandler.class).handle(ctx, e, 500);
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
