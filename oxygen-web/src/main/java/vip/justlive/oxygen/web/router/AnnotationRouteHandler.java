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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.web.annotation.CookieParam;
import vip.justlive.oxygen.web.annotation.HeaderParam;
import vip.justlive.oxygen.web.annotation.Param;
import vip.justlive.oxygen.web.annotation.PathParam;
import vip.justlive.oxygen.web.mapping.DataBinder;
import vip.justlive.oxygen.web.mapping.DataBinder.SCOPE;

/**
 * annotation route handler
 *
 * @author wubo
 */
public class AnnotationRouteHandler implements RouteHandler {

  private final Object router;
  private final Method proxyMethod;
  private final Method method;
  private final DataBinder[] dataBinders;

  public AnnotationRouteHandler(Object router, Method proxyMethod, Method method) {
    this.router = router;
    this.proxyMethod = proxyMethod;
    this.method = method;
    this.dataBinders = new DataBinder[method.getParameterCount()];
    init();
  }

  private void init() {
    Parameter[] parameters = method.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      DataBinder dataBinder = new DataBinder();
      dataBinder.setName(parameters[i].getName());
      dataBinder.setType(parameters[i].getType());
      parseAnnotation(dataBinder, parameters[i]);
      dataBinders[i] = dataBinder;
    }
  }

  private void parseAnnotation(DataBinder dataBinder, Parameter parameter) {
    Param param = parameter.getAnnotation(Param.class);
    if (param != null) {
      fillDataBinder(dataBinder, param.value(), param.defaultValue());
      return;
    }
    PathParam pathParam = parameter.getAnnotation(PathParam.class);
    if (pathParam != null) {
      fillDataBinder(dataBinder, pathParam.value(), pathParam.defaultValue());
      dataBinder.setScope(SCOPE.PATH);
      return;
    }
    HeaderParam headerParam = parameter.getAnnotation(HeaderParam.class);
    if (headerParam != null) {
      fillDataBinder(dataBinder, headerParam.value(), headerParam.defaultValue());
      dataBinder.setScope(SCOPE.HEADER);
      return;
    }
    CookieParam cookieParam = parameter.getAnnotation(CookieParam.class);
    if (cookieParam != null) {
      fillDataBinder(dataBinder, cookieParam.value(), cookieParam.defaultValue());
      dataBinder.setScope(SCOPE.COOKIE);
    }
  }

  private void fillDataBinder(DataBinder dataBinder, String name, String defaultValue) {
    if (name.length() > 0) {
      dataBinder.setName(name);
    }
    if (defaultValue.length() > 0) {
      dataBinder.setDefaultValue(defaultValue);
    }
  }

  @Override
  public void handle(RoutingContext ctx) {
    try {
      Object[] args = prepareArgs(ctx);
      proxyMethod.invoke(router, args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw Exceptions.wrap(e);
    }
  }

  private Object[] prepareArgs(RoutingContext ctx) {
    Object[] args = new Object[dataBinders.length];
    for (int i = 0; i < dataBinders.length; i++) {
    }
    return args;
  }

}
