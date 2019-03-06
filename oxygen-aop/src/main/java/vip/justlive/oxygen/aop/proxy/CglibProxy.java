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
package vip.justlive.oxygen.aop.proxy;

import java.lang.reflect.Method;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import vip.justlive.oxygen.aop.Invocation;
import vip.justlive.oxygen.aop.interceptor.Interceptor;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * cglib代理
 *
 * @author wubo
 */
@Slf4j
public class CglibProxy implements MethodInterceptor {

  private static final CglibProxy CGLIB_PROXY = new CglibProxy();

  /**
   * 代理
   *
   * @param targetClass 目标
   * @param args 参数
   * @param <T> 泛型
   * @return 代理对象
   */
  public static <T> T proxy(Class<T> targetClass, Object... args) {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(targetClass);
    enhancer.setCallback(CGLIB_PROXY);
    Class<?>[] classes = ClassUtils.getConstructorArgsTypes(targetClass, args);
    return targetClass.cast(enhancer.create(classes, args));
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy)
      throws Throwable {
    List<Interceptor> interceptors = ProxyInterceptorStore.get(method);
    if (interceptors == null) {
      return methodProxy.invokeSuper(obj, args);
    }
    Invocation invocation = new Invocation(obj, method, args);
    boolean interrupted = doBefore(interceptors, invocation);
    try {
      if (interrupted && invocation.getReturnValue() != null) {
        if (log.isDebugEnabled()) {
          log.debug("aop intercepted and return an updated value before invoke super method {}",
              invocation);
        }
      } else {
        invocation.setReturnValue(methodProxy.invokeSuper(obj, args));
      }
    } catch (Exception e) {
      doCatching(interceptors, invocation);
      throw e;
    }
    doAfter(interceptors, invocation);
    return invocation.getReturnValue();
  }

  private boolean doBefore(List<Interceptor> interceptors, Invocation invocation) {
    for (int index = 0, len = interceptors.size(); index < len; index++) {
      if (!interceptors.get(index).before(invocation)) {
        if (log.isDebugEnabled()) {
          log.debug("before aop intercepted, total size:{}, current index: {}", interceptors.size(),
              index);
        }
        return true;
      }
    }
    return false;
  }

  private boolean doCatching(List<Interceptor> interceptors, Invocation invocation) {
    for (int index = interceptors.size() - 1; index >= 0; index--) {
      if (!interceptors.get(index).catching(invocation)) {
        if (log.isDebugEnabled()) {
          log.debug("catching aop intercepted, total size:{}, current index: {}",
              interceptors.size(), index);
        }
        return true;
      }
    }
    return false;
  }

  private boolean doAfter(List<Interceptor> interceptors, Invocation invocation) {
    for (int index = interceptors.size() - 1; index >= 0; index--) {
      if (!interceptors.get(index).after(invocation)) {
        if (log.isDebugEnabled()) {
          log.debug("after aop intercepted, total size:{}, current index: {}",
              interceptors.size(), index);
        }
        return true;
      }
    }
    return false;
  }

}
