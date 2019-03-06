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
package vip.justlive.oxygen.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import vip.justlive.oxygen.aop.annotation.Aspect;
import vip.justlive.oxygen.aop.annotation.Aspect.TYPE;
import vip.justlive.oxygen.aop.interceptor.AfterInterceptor;
import vip.justlive.oxygen.aop.interceptor.BeforeInterceptor;
import vip.justlive.oxygen.aop.interceptor.CatchingInterceptor;
import vip.justlive.oxygen.aop.interceptor.Interceptor;
import vip.justlive.oxygen.aop.proxy.ProxyInterceptorStore;
import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.ioc.IocPlugin;

/**
 * aop插件
 *
 * @author wubo
 */
public class AopPlugin implements Plugin {

  private static final Map<TYPE, Map<Class<? extends Annotation>, List<AopWrapper>>> CACHE = new ConcurrentHashMap<>(
      8, 0.75F);

  static {
    for (TYPE type : TYPE.values()) {
      CACHE.put(type, new HashMap<>(4));
    }
  }

  /**
   * 获取aop增强方法
   *
   * @param type 类型
   * @param targetAnnotation 目标注解
   * @return 增强方法
   */
  public static List<AopWrapper> getAopMethod(TYPE type,
      Class<? extends Annotation> targetAnnotation) {
    Map<Class<? extends Annotation>, List<AopWrapper>> listMultimap = CACHE.get(type);
    if (listMultimap != null) {
      return listMultimap.get(targetAnnotation);
    }
    return Collections.emptyList();
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 20;
  }

  @Override
  public void start() {
    handleAspect();
    prepareAspect();
  }

  @Override
  public void stop() {
    ProxyInterceptorStore.clean();
  }

  void handleAspect() {
    IocPlugin.beanStore().getBeans().forEach(
        bean -> ClassUtils.getMethodsAnnotatedWith(bean.getClass(), Aspect.class)
            .forEach(method -> handleMethod(method, bean)));
  }

  private void prepareAspect() {
    for (Object bean : IocPlugin.beanStore().getBeans()) {
      for (Method method : ClassUtils.getCglibActualClass(bean.getClass()).getDeclaredMethods()) {
        handleAnnotation(method);
      }
    }
  }

  private void handleAnnotation(Method method) {
    Annotation[] annotations = method.getAnnotations();
    List<Interceptor> list = new LinkedList<>();
    for (Annotation annotation : annotations) {
      parseBeforeInterceptor(annotation, list);
      parseAfterInterceptor(annotation, list);
      parseCatchingInterceptor(annotation, list);
    }
    ProxyInterceptorStore.addInterceptors(method, list);
  }

  private void parseBeforeInterceptor(Annotation annotation, List<Interceptor> list) {
    List<AopWrapper> aopWrappers = AopPlugin.getAopMethod(TYPE.BEFORE, annotation.annotationType());
    if (aopWrappers == null) {
      return;
    }
    aopWrappers.forEach(aopWrapper -> list.add(new BeforeInterceptor(aopWrapper)));
  }

  private void parseAfterInterceptor(Annotation annotation, List<Interceptor> list) {
    List<AopWrapper> aopWrappers = AopPlugin.getAopMethod(TYPE.AFTER, annotation.annotationType());
    if (aopWrappers == null) {
      return;
    }
    aopWrappers.forEach(aopWrapper -> list.add(new AfterInterceptor(aopWrapper)));
  }

  private void parseCatchingInterceptor(Annotation annotation, List<Interceptor> list) {
    List<AopWrapper> aopWrappers = AopPlugin
        .getAopMethod(TYPE.CATCHING, annotation.annotationType());
    if (aopWrappers == null) {
      return;
    }
    aopWrappers.forEach(aopWrapper -> list.add(new CatchingInterceptor(aopWrapper)));
  }

  private void handleMethod(Method method, Object bean) {
    Aspect aspect = method.getAnnotation(Aspect.class);
    AopWrapper wrapper;
    try {
      wrapper = new AopWrapper(bean,
          bean.getClass().getMethod(method.getName(), method.getParameterTypes()), aspect.order());
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
    for (TYPE type : aspect.type()) {
      CACHE.get(type).computeIfAbsent(aspect.annotation(), k -> new LinkedList<>());
      CACHE.get(type).get(aspect.annotation()).add(wrapper);
    }
  }

}
