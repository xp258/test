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
package vip.justlive.oxygen.web.router;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.web.http.HttpMethod;

/**
 * 路由
 *
 * @author wubo
 */
@Slf4j
public class Route {

  private static final Pattern REGEX_PATH_GROUP = Pattern.compile("\\{(\\w+)[}]");
  private static final Map<HttpMethod, Map<String, RouteHandler>> SIMPLE_HANDLERS = new EnumMap<>(
      HttpMethod.class);
  private static final Map<HttpMethod, Map<String, RouteHandler>> REGEX_HANDLERS = new EnumMap<>(
      HttpMethod.class);
  private static final List<Route> ROUTES = new LinkedList<>();


  static {
    for (HttpMethod httpMethod : HttpMethod.values()) {
      SIMPLE_HANDLERS.put(httpMethod, new ConcurrentHashMap<>(4, 1f));
      REGEX_HANDLERS.put(httpMethod, new ConcurrentHashMap<>(4, 1f));
    }
  }

  /**
   * 方法
   */
  private Set<HttpMethod> methods = new HashSet<>(1, 1f);
  /**
   * 请求路径
   */
  private String path;
  /**
   * 是否为正则请求
   */
  private boolean regex;
  /**
   * 路由处理方式
   */
  private RouteHandler handler;

  private List<String> pathVars = new LinkedList<>();

  private Route() {
  }

  /**
   * 构建router
   *
   * @return route
   */
  public static Route router() {
    Route route = new Route();
    ROUTES.add(route);
    return route;
  }

  /**
   * build
   */
  public static void build() {
    ROUTES.forEach(Route::buildRoute);
  }

  /**
   * lookup handler
   *
   * @param method request method
   * @param path request path
   * @return handler
   */
  public static RouteHandler lookup(HttpMethod method, String path) {
    RouteHandler handler = SIMPLE_HANDLERS.get(method).get(path);
    if (handler != null) {
      return handler;
    }
    for (Map.Entry<String, RouteHandler> entry : REGEX_HANDLERS.get(method).entrySet()) {
      if (Pattern.compile(entry.getKey()).matcher(path).matches()) {
        return entry.getValue();
      }
    }
    return null;
  }

  /**
   * clear
   */
  public static void clear() {
    SIMPLE_HANDLERS.clear();
    REGEX_HANDLERS.clear();
  }

  /**
   * 设置method
   *
   * @param httpMethod 请求方法
   * @return router
   */
  public Route method(HttpMethod httpMethod) {
    methods.add(httpMethod);
    return this;
  }

  /**
   * add path
   *
   * @param path request path
   * @return router
   */
  public Route path(String path) {
    Matcher matcher = REGEX_PATH_GROUP.matcher(path);
    int start = 0;
    if (matcher.find(start)) {
      do {
        pathVars.add(matcher.group(1));
        start = matcher.end();
      } while (matcher.find(start));
      path = path.replaceAll(Constants.REGEX_PATH_VAR, Constants.REGEX_PATH_VAR_REPLACE);
      this.regex = true;
    }
    this.path = path;
    return this;
  }

  /**
   * 添加处理器
   *
   * @param handler 处理
   * @return router
   */
  public Route handler(RouteHandler handler) {
    this.handler = handler;
    return this;
  }

  /**
   * build route
   */
  private void buildRoute() {
    if (methods.isEmpty()) {
      methods.addAll(Arrays.asList(HttpMethod.values()));
    }
    for (HttpMethod method : methods) {
      RouteHandler exist;
      if (this.regex) {
        exist = REGEX_HANDLERS.get(method).putIfAbsent(this.path, handler);
      } else {
        exist = SIMPLE_HANDLERS.get(method).putIfAbsent(this.path, handler);
      }
      if (exist != null) {
        throw Exceptions.fail(String.format("path [%s] already exists", path));
      }
    }
  }

}
