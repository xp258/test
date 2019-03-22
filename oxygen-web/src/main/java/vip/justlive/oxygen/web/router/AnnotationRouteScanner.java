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

import java.lang.reflect.Method;
import java.util.Set;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.ResourceUtils;
import vip.justlive.oxygen.ioc.IocPlugin;
import vip.justlive.oxygen.web.annotation.Mapping;
import vip.justlive.oxygen.web.annotation.Router;
import vip.justlive.oxygen.web.http.HttpMethod;
import vip.justlive.oxygen.web.server.Server;

/**
 * 注解route扫描器
 *
 * @author wubo
 */
public class AnnotationRouteScanner {

  private final Server server;

  public AnnotationRouteScanner(Server server) {
    this.server = server;
  }

  public void scan() {
    Set<Class<?>> routerClasses = ClassScannerPlugin
        .getTypesAnnotatedWith(Router.class);
    if (routerClasses == null || routerClasses.isEmpty()) {
      return;
    }
    for (Class<?> clazz : routerClasses) {
      Router router = clazz.getAnnotation(Router.class);
      if (router == null) {
        continue;
      }
      String rootPath = router.value();
      if (!IocPlugin.tryInstance(clazz, clazz.getName(), Integer.MAX_VALUE)) {
        throw Exceptions.fail(String.format("实例化Router失败 %s", clazz));
      }
      parseRouter(rootPath, IocPlugin.beanStore().getBean(clazz.getName()));
    }
  }

  private void parseRouter(String rootPath, Object routerBean) {
    Class<?> clazz = routerBean.getClass();
    Class<?> actualClass = ClassUtils.getCglibActualClass(clazz);
    try {
      for (Method method : actualClass.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(Mapping.class)) {
          continue;
        }
        Method requestMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
        Mapping mapping = method.getAnnotation(Mapping.class);
        String routePath = ResourceUtils.concat(rootPath, mapping.value());
        Route route = server.router().path(routePath);
        for (HttpMethod httpMethod : mapping.method()) {
          route.method(httpMethod);
        }
        route.handler(new AnnotationRouteHandler(routerBean, requestMethod, method));
      }
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
  }

}
