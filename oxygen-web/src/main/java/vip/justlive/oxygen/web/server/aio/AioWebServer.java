/*
 * Copyright (C) 2019 the original author or authors.
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

package vip.justlive.oxygen.web.server.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.net.aio.core.GroupContext;
import vip.justlive.oxygen.core.net.aio.core.Server;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.server.WebServer;

/**
 * aio web server
 *
 * @author wubo
 */
public class AioWebServer implements WebServer {

  private int port;
  private Server server;

  @Override
  public void listen(int port) {
    this.port = port;
    WebConf webConf = ConfigFactory.load(WebConf.class);
    GroupContext groupContext = new GroupContext(new HttpServerAioHandler(webConf));
    groupContext.setAioListener(new HttpServerAioListener());
    this.server = new Server(groupContext);
    Bootstrap.start();
    try {
      this.server.start(new InetSocketAddress(this.port));
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
    groupContext.getScheduledExecutor().scheduleWithFixedDelay(
        new ConnectionTimeoutUpdater(groupContext, webConf.getAioIdleTimeout(),
            webConf.getAioRequestTimeout()), webConf.getAioIdleTimeout(),
        webConf.getAioIdleTimeout(), TimeUnit.MILLISECONDS);
    this.server.sync();
  }

  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }

  @Override
  public int getPort() {
    return port;
  }
}
