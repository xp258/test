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
package vip.justlive.oxygen.web.result;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.nio.charset.Charset;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.router.RoutingContext;

/**
 * json result
 *
 * @author wubo
 */
public class JsonResultHandler implements ResultHandler {

  @Override
  public boolean support(Class<?> type, Object data) {
    return !Result.class.isAssignableFrom(type) && type != Void.TYPE;
  }

  @Override
  public void apply(RoutingContext ctx, Object data) {
    Response response = ctx.response();
    response.setContentType(Constants.APPLICATION_JSON);
    try {
      response.getOut()
          .write(JSON.toJSONString(data).getBytes(Charset.forName(response.getEncoding())));
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
