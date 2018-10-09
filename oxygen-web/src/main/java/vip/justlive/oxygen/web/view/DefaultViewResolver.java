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
package vip.justlive.oxygen.web.view;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 默认视图解析
 *
 * @author wubo
 */
public class DefaultViewResolver implements ViewResolver {

  @Override
  public void resolveView(HttpServletRequest request, HttpServletResponse response, Object data) {
    try {
      if (data != null && data.getClass() == View.class) {
        View view = (View) data;
        if (view.isRedirect()) {
          response.sendRedirect(request.getContextPath() + view.getPath());
        } else {
          if (view.getData() != null) {
            view.getData().forEach(request::setAttribute);
          }
          request.getRequestDispatcher(view.getPath()).forward(request, response);
        }
      } else {
        response.getWriter().print(JSON.toJSONString(data));
      }
    } catch (IOException | ServletException e) {
      throw Exceptions.wrap(e);
    }
  }
}
