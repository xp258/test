/*
 * Copyright (C) 2019 justlive1
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
package vip.justlive.oxygen.web.jetty;

import java.io.File;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.server.WebServer;

/**
 * jetty web server
 *
 * @author wubo
 */
@Slf4j
public class JettyWebServer implements WebServer {

  private int port;
  private Server server;

  @Override
  public void listen(int port) {
    WebConf webConf = ConfigFactory.load(WebConf.class);
    JettyConf jettyConf = ConfigFactory.load(JettyConf.class);
    this.port = webConf.getPort() != null ? webConf.getPort() : port;

    server = new Server();

    ServerConnector connector = new ServerConnector(server);
    server.addConnector(connector);
    configConnector(connector, jettyConf);

    WebAppContext webapp = new WebAppContext();
    server.setHandler(webapp);
    webapp.setContextPath(webConf.getContextPath());

    try {
      configWebapp(webapp, jettyConf);
      server.start();
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
    log.info("jetty started and listened on port [{}] with context path [{}]", this.port,
        webConf.getContextPath());
  }

  @Override
  public void stop() {
    if (server == null) {
      return;
    }
    try {
      server.stop();
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public int getPort() {
    return port;
  }

  private void configConnector(ServerConnector connector, JettyConf jettyConf) {
    connector.setPort(this.port);
    connector.setIdleTimeout(jettyConf.getIdleTimeout());
    connector.setAcceptQueueSize(jettyConf.getAcceptQueueSize());
    connector.setStopTimeout(jettyConf.getStopTimeout());
    connector.setReuseAddress(jettyConf.isReuseAddress());
  }

  private void configWebapp(WebAppContext webapp, JettyConf jettyConf) throws URISyntaxException {
    webapp.setVirtualHosts(jettyConf.getVirtualHosts());
    webapp.setMaxFormContentSize(jettyConf.getMaxFormContentSize());
    webapp.setMaxFormKeys(jettyConf.getMaxFormKeys());
    webapp.setParentLoaderPriority(true);
    webapp.setConfigurationDiscovered(jettyConf.isConfigurationDiscovered());
    webapp.setTempDirectory(
        new File(ConfigFactory.load(CoreConf.class).getBaseTempDir(), "jetty-temp"));
    webapp.setResourceBase(getClass().getResource(Constants.ROOT_PATH).toURI().toASCIIString());
    webapp.addServlet(new ServletHolder("jsp", JettyJspServlet.class), "*.jsp");
    webapp.setConfigurations(new Configuration[]{new AnnotationConfiguration()});
  }
}
