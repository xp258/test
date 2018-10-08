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
import lombok.Data;
import vip.justlive.oxygen.web.WebPlugin;
import vip.justlive.oxygen.web.handler.ParamHandler;
import vip.justlive.oxygen.web.view.View;

/**
 * action
 *
 * @author wubo
 */
@Data
public class Action {

  private final String path;
  private final Object router;
  private final Method method;

  /**
   * 是否为视图
   *
   * @return true为视图
   */
  public boolean isView() {
    return method.getReturnType() == View.class;
  }

  /**
   * 是否需要渲染视图
   *
   * @return true需要渲染
   */
  public boolean needRenderView() {
    return method.getReturnType() != Void.TYPE;
  }

  /**
   * 执行action
   *
   * @return result
   * @throws Exception 抛出异常
   */
  public Object invoke() throws Exception {
    Object[] args = new Object[method.getParameterCount()];
    if (args.length > 0) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      for (int i = 0; i < args.length; i++) {
        Class<?> type = parameterTypes[i];
        ParamHandler paramHandler = WebPlugin.findParamHandlerByType(type);
        if (paramHandler != null) {
          Object result = paramHandler.handle(type);
          args[i] = result;
        }
      }
    }
    return method.invoke(router, args);
  }
}
