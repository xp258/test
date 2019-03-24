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

import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;

/**
 * impl of RoutingContext
 *
 * @author wubo
 */
public class RoutingContextImpl implements RoutingContext {

  private final Request request;
  private final Response response;
  private final String requestPath;

  public RoutingContextImpl(Request request, Response response, String requestPath) {
    this.request = request;
    this.response = response;
    this.requestPath = requestPath;
  }

  @Override
  public Request request() {
    return request;
  }

  @Override
  public Response response() {
    return response;
  }

  @Override
  public String requestPath() {
    return requestPath;
  }

  @Override
  public <T> T bindParam(Class<T> clazz) {
    return null;
  }

  @Override
  public <T> T bindCookie(Class<T> clazz) {
    return null;
  }

  @Override
  public <T> T bindHeader(Class<T> clazz) {
    return null;
  }

  @Override
  public <T> T bindPathvar(Class<T> clazz) {
    return null;
  }
}
