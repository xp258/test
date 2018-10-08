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
package vip.justlive.oxygen.web.http;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;

/**
 * Request
 *
 * @author wubo
 */
@Data
public class Request implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final String ACCEPT = "Accept";
  public static final String ACCEPT_CHARSET = "Accept-Charset";
  public static final String ACCEPT_ENCODING = "Accept-Encoding";
  public static final String CHARSET = "charset";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String COOKIE = "Cookie";
  public static final String HOST_NAME = "Host";
  public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
  public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
  public static final String X_FORWARDED_FOR = "X-Forwarded-For";
  public static final String X_REAL_IP = "X-Real-IP";

  private static final ThreadLocal<Request> LOCAL = new ThreadLocal<>();
  private static final String[] EMPTY = new String[0];

  private final transient HttpServletRequest originalRequest;

  /**
   * 主机名
   */
  private String host;
  /**
   * 请求路径 去除host:port
   */
  private String path;
  /**
   * 请求全路径 http://host:port/path?queryString
   */
  private String url;
  /**
   * 请求类型
   */
  private String method;
  /**
   * 客户端ip
   */
  private String remoteAddress;
  /**
   * 端口
   */
  private Integer port;
  /**
   * 内容类型
   */
  private String contentType = "text/html";
  /**
   * 字符集编码
   */
  private String encoding = StandardCharsets.UTF_8.name();
  /**
   * queryString
   */
  private String queryString;

  /**
   * query params
   */
  private Map<String, String[]> queryParams;

  /**
   * headers
   */
  private Map<String, String[]> headers;

  /**
   * cookies
   */
  private Map<String, Cookie> cookies;

  /**
   * 根据key获取查询参数 最多返回第一个值
   *
   * @param key 键
   * @return value
   */
  public String getParam(String key) {
    String[] values = getParams(key);
    if (values.length > 0) {
      return values[0];
    }
    return null;
  }

  /**
   * 根据key获取查询参数数组
   *
   * @param key 键
   * @return values
   */
  public String[] getParams(String key) {
    if (queryParams != null) {
      String[] values = queryParams.get(key);
      if (values != null) {
        return values;
      }
    }
    return EMPTY;
  }

  /**
   * 根据key获取header值 最多返回第一个值
   *
   * @param key 键
   * @return value
   */
  public String getHeader(String key) {
    String[] values = getHeaders(key.toLowerCase());
    if (values.length > 0) {
      return values[0];
    }
    return null;
  }

  /**
   * 根据key获取header数组
   *
   * @param key 键
   * @return values
   */
  public String[] getHeaders(String key) {
    if (headers != null) {
      String[] values = headers.get(key);
      if (values != null) {
        return values;
      }
    }
    return EMPTY;
  }

  /**
   * 根据key获取cookie
   *
   * @param key 键
   * @return cookie
   */
  public Cookie getCookie(String key) {
    if (cookies != null) {
      return cookies.get(key);
    }
    return null;
  }

  /**
   * 根据key获取cookie值
   *
   * @param key 键
   * @return value of cookie
   */
  public String getCookieValue(String key) {
    Cookie cookie = getCookie(key);
    if (cookie != null) {
      return cookie.getValue();
    }
    return null;
  }

  /**
   * 设置线程值Request
   *
   * @param originalRequest 原始request
   */
  public static void set(HttpServletRequest originalRequest) {
    Request request = new Request(originalRequest);
    LOCAL.set(request);
  }

  /**
   * 当前请求的request
   *
   * @return request
   */
  public static Request current() {
    return LOCAL.get();
  }

  /**
   * 清除request数据
   */
  public static void clear() {
    LOCAL.remove();
  }
}
