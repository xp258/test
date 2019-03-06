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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.aop.interceptor.Interceptor;

/**
 * proxy interceptor store
 *
 * @author wubo
 */
@UtilityClass
public class ProxyInterceptorStore {

  private static final Map<Method, List<Interceptor>> STORE = new ConcurrentHashMap<>(16, 1);

  /**
   * 添加拦截
   *
   * @param method 方法
   * @param interceptors 拦截器
   */
  public static void addInterceptors(Method method, List<Interceptor> interceptors) {
    STORE.computeIfAbsent(method, k -> new LinkedList<>());
    STORE.get(method).addAll(interceptors);
    Collections.sort(STORE.get(method));
  }

  /**
   * 根据方法获取拦截器
   *
   * @param method 方法
   * @return interceptors
   */
  public static List<Interceptor> get(Method method) {
    return STORE.get(method);
  }

  /**
   * clean
   */
  public static void clean() {
    STORE.clear();
  }
}
