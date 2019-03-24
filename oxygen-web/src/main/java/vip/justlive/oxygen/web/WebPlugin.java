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
package vip.justlive.oxygen.web;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.ResourceUtils;
import vip.justlive.oxygen.core.util.ServiceLoaderUtils;
import vip.justlive.oxygen.ioc.IocPlugin;
import vip.justlive.oxygen.web.annotation.Mapping;
import vip.justlive.oxygen.web.annotation.Router;
import vip.justlive.oxygen.web.handler.DefaultErrorHandler;
import vip.justlive.oxygen.web.handler.ErrorHandler;
import vip.justlive.oxygen.web.http.HttpMethod;
import vip.justlive.oxygen.web.http.RequestParse;
import vip.justlive.oxygen.web.router.AnnotationRouteHandler;
import vip.justlive.oxygen.web.router.Route;
import vip.justlive.oxygen.web.view.ViewResolver;

/**
 * web插件
 *
 * @author wubo
 */
@Slf4j
public class WebPlugin implements Plugin {

  static final List<RequestParse> REQUEST_PARSES = new LinkedList<>();
  static final Map<Integer, ErrorHandler> ERROR_HANDLERS = new HashMap<>(2, 1f);

  private static final Set<ViewResolver> VIEW_RESOLVERS = new HashSet<>(4);

  /**
   * 添加视图解析器
   *
   * @param viewResolver 视图解析器
   */
  public static void addViewResolver(ViewResolver viewResolver) {
    VIEW_RESOLVERS.add(viewResolver);
    if (log.isDebugEnabled()) {
      log.debug("add a view resolver [{}]", viewResolver.getClass());
    }
  }

  /**
   * 获取视图解析
   *
   * @param data Action执行返回
   * @return ViewResolver
   */
  public static ViewResolver findViewResolver(Object data) {
    for (ViewResolver viewResolver : VIEW_RESOLVERS) {
      if (viewResolver.supported(data)) {
        return viewResolver;
      }
    }
    return null;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 60;
  }

  @Override
  public void start() {
    loadStaticMapping();
    loadAnnotationRouter();
    loadRequestParse();
    loadViewResolver();
    loadErrorHandler();
    vip.justlive.oxygen.web.router.Router.build();
  }

  @Override
  public void stop() {
    REQUEST_PARSES.clear();
    ERROR_HANDLERS.clear();
  }

  private void loadRequestParse() {
    for (RequestParse requestParse : ServiceLoaderUtils.loadServices(RequestParse.class)) {
      REQUEST_PARSES.add(requestParse);
      if (log.isDebugEnabled()) {
        log.debug("loaded a request parser [{}]", requestParse.getClass());
      }
    }
  }

  private void loadViewResolver() {
    for (ViewResolver viewResolver : ServiceLoaderUtils.loadServices(ViewResolver.class)) {
      addViewResolver(viewResolver);
    }
  }

  private void loadStaticMapping() {
    WebConf conf = ConfigFactory.load(WebConf.class);
    String[] paths = conf.getDefaultStaticPaths();
    if (paths == null) {
      return;
    }
    vip.justlive.oxygen.web.router.Router.staticRoute().prefix(conf.getDefaultStaticPrefix())
        .locations(Arrays.asList(paths));
  }

  private void loadErrorHandler() {
    WebConf webConf = ConfigFactory.load(WebConf.class);
    try {
      ErrorHandler errorHandler;
      if (webConf.getError404Handler() != null && webConf.getError404Handler().length() > 0) {
        errorHandler = (ErrorHandler) ClassUtils
            .forName(webConf.getError404Handler(), ClassUtils.getDefaultClassLoader())
            .newInstance();
      } else {
        errorHandler = new DefaultErrorHandler(webConf.getError404Page(), Constants.NOT_FOUND);
      }
      ERROR_HANDLERS.put(Constants.NOT_FOUND, errorHandler);
      if (webConf.getError500Handler() != null && webConf.getError500Handler().length() > 0) {
        errorHandler = (ErrorHandler) ClassUtils
            .forName(webConf.getError500Handler(), ClassUtils.getDefaultClassLoader())
            .newInstance();
      } else {
        errorHandler = new DefaultErrorHandler(webConf.getError500Page(), Constants.SERVER_ERROR);
      }
      ERROR_HANDLERS.put(Constants.SERVER_ERROR, errorHandler);
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  private void loadAnnotationRouter() {
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
        Route route = vip.justlive.oxygen.web.router.Router.router().path(routePath);
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
