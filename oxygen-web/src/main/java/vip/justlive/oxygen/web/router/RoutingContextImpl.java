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

  public RoutingContextImpl(Request request, Response response) {
    this.request = request;
    this.response = response;
  }

  @Override
  public Request request() {
    return request;
  }

  @Override
  public Response response() {
    return response;
  }
}
