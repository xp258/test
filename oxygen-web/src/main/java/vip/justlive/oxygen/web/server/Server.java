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
package vip.justlive.oxygen.web.server;

import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.util.ServiceLoaderUtils;
import vip.justlive.oxygen.web.router.Router;

/**
 * server 启动类
 *
 * @author wubo
 */
@Slf4j
public class Server {

  private List<Router> routers = new LinkedList<>();

  private WebServer webServer;

  private volatile boolean ready = false;

  /**
   * web server
   *
   * @return webServer
   */
  public static Server server() {
    Server server = new Server();
    server.webServer = ServiceLoaderUtils.loadSerivce(WebServer.class);
    return server;
  }

  /**
   * 路由
   *
   * @return router
   */
  public Router router() {
    Router router = new Router();
    routers.add(router);
    return router;
  }

  public void start() {
    if (!ready) {
      ready = true;
      Bootstrap.initConfig();
      prepareRouters();
      webServer.start();
    }
  }

  public void stop() {
    if (ready) {
      ready = false;
      webServer.stop();
    }
  }

  private void prepareRouters() {
    routers.forEach(Router::build);
  }

}
