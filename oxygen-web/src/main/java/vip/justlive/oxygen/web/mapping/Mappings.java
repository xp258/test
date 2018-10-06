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
package vip.justlive.oxygen.web.mapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.ioc.BeanStore;
import vip.justlive.oxygen.core.ioc.IocPlugin;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;
import vip.justlive.oxygen.core.util.Checks;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.web.mapping.Request.HttpMethod;

/**
 * 解析注解并用于根据path获取action
 *
 * @author wubo
 */
public class Mappings {

  private static final Map<HttpMethod, Map<String, Action>> ACTION_MAP = new ConcurrentHashMap<>(8,
      1f);

  static {
    for (HttpMethod httpMethod : HttpMethod.values()) {
      ACTION_MAP.put(httpMethod, new ConcurrentHashMap<>(4, 1f));
    }
  }

  static String makePath(String parent, String child) {
    Checks.notNull(parent);
    Checks.notNull(child);
    StringBuilder sb = new StringBuilder();
    if (!parent.startsWith(Constants.ROOT_PATH)) {
      sb.append(Constants.ROOT_PATH);
    }
    sb.append(parent);
    if (parent.endsWith(Constants.ROOT_PATH)) {
      sb.deleteCharAt(sb.length() - 1);
    }
    if (!child.startsWith(Constants.ROOT_PATH)) {
      sb.append(Constants.ROOT_PATH);
    }
    sb.append(child);
    if (child.endsWith(Constants.ROOT_PATH)) {
      sb.deleteCharAt(sb.length() - 1);
    }
    // TODO path vars
    return sb.toString();
  }

  /**
   * 初始化
   */
  public static void init() {
    parseRouter();
  }

  static void parseRouter() {
    Set<Class<?>> routerClasses = ClassScannerPlugin.getTypesAnnotatedWith(Router.class);
    if (routerClasses == null || routerClasses.isEmpty()) {
      return;
    }
    for (Class<?> clazz : routerClasses) {
      Router router = clazz.getAnnotation(Router.class);
      if (router == null) {
        continue;
      }

      String rootPath = router.value();
      Object routerBean = IocPlugin.instanceBean(clazz);
      BeanStore.putBean(clazz.getName(), routerBean);

      parseRequest(rootPath, routerBean);
    }
  }

  /**
   * 销毁
   */
  public static void destroy() {
    ACTION_MAP.clear();
  }

  static void parseRequest(String rootPath, Object routerBean) {
    Class<?> clazz = routerBean.getClass();
    Class<?> actualClass = ClassUtils.getCglibActualClass(clazz);
    try {
      for (Method method : actualClass.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(Request.class)) {
          continue;
        }
        Method requestMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
        Request request = method.getAnnotation(Request.class);
        String path = request.value();
        HttpMethod[] httpMethods = request.method();
        String routePath = makePath(rootPath, path);
        Action action = new Action(routePath, routerBean, requestMethod);
        HttpMethod[] realMethods = httpMethods;
        if (realMethods.length == 0) {
          realMethods = HttpMethod.values();
        }
        for (HttpMethod httpMethod : realMethods) {
          ACTION_MAP.get(httpMethod).put(action.getPath(), action);
        }
      }
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * 根据路径和请求类型获取Action
   *
   * @param path 请求路径
   * @param httpMethod 请求类型
   * @return action
   */
  public static Action findActionByPath(String path, HttpMethod httpMethod) {
    Map<String, Action> actionMap = ACTION_MAP.get(httpMethod);
    Action action = actionMap.get(path);
    if (action != null) {
      return action;
    }
    // TODO path vars
    return null;
  }
}
