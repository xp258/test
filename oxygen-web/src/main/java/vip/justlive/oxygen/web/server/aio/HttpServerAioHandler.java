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

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.net.aio.core.AioHandler;
import vip.justlive.oxygen.core.net.aio.core.ChannelContext;
import vip.justlive.oxygen.core.net.http.HttpMethod;
import vip.justlive.oxygen.core.util.Bytes;
import vip.justlive.oxygen.core.util.HttpHeaders;
import vip.justlive.oxygen.core.util.Strings;
import vip.justlive.oxygen.web.Context;
import vip.justlive.oxygen.web.WebConf;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.http.Response;
import vip.justlive.oxygen.web.result.ResultHandler;
import vip.justlive.oxygen.web.router.RouteHandler;
import vip.justlive.oxygen.web.router.Router;
import vip.justlive.oxygen.web.router.RoutingContext;
import vip.justlive.oxygen.web.router.RoutingContextImpl;

/**
 * http aio处理
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class HttpServerAioHandler implements AioHandler {

  private final WebConf webConf;

  @Override
  public ByteBuffer encode(Object data, ChannelContext channelContext) {
    Response response = (Response) data;
    Bytes bytes = new Bytes();
    // 状态行
    bytes.write(response.getRequest().getProtocol()).write(Bytes.SPACE)
        .write(Integer.toString(response.getStatus())).write(Bytes.CR).write(Bytes.LF);

    byte[] body = new byte[0];
    if (response.getOut() != null) {
      body = response.getOut().toByteArray();
    }

    // content-length
    response.setHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(body.length));
    // content-type
    if (response.getHeader(HttpHeaders.CONTENT_TYPE) == null && response.getContentType() != null) {
      String contentType = response.getContentType();
      if (!contentType.contains(HttpHeaders.CHARSET)) {
        contentType += Strings.SEMICOLON.concat(HttpHeaders.CHARSET).concat(Strings.EQUAL)
            .concat(response.getEncoding());
      }
      response.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }
    // 响应头
    response.getHeaders().forEach(
        (k, v) -> bytes.write(k).write(Bytes.COLON).write(Bytes.SPACE).write(v).write(Bytes.CR)
            .write(Bytes.LF));
    bytes.write(Bytes.CR).write(Bytes.LF);

    // 响应体
    if (body.length > 0) {
      bytes.write(body);
    }
    return ByteBuffer.wrap(bytes.toArray());
  }

  @Override
  public Object decode(ByteBuffer buffer, int readableSize, ChannelContext channelContext) {
    RequestBuilder builder = parseStatusText(buffer);
    if (builder != null && parseHeaders(builder, buffer) && parseBody(builder, buffer)) {
      return builder.build(channelContext);
    }
    return null;
  }

  @Override
  public void handle(Object data, ChannelContext channelContext) {
    Request request = (Request) data;
    Response response = new Response(request);
    request.local();
    response.local();
    final RoutingContext ctx = new RoutingContextImpl(request, response);
    try {
      Context.parseRequest(request);
      RouteHandler handler = Router.lookup(request);
      if (handler == null) {
        RouteHandler.notFound(ctx);
        return;
      }
      if (!Context.invokeBefore(ctx)) {
        return;
      }
      handler.handle(ctx);
      for (ResultHandler resultHandler : Context.HANDLERS) {
        if (resultHandler.support(response.getResult())) {
          resultHandler.apply(ctx, response.getResult());
          break;
        }
      }
      Context.invokeAfter(ctx);
    } catch (Exception e) {
      RouteHandler.error(ctx, e);
    } finally {
      Context.invokeFinished(ctx);
      channelContext.write(response);
      Request.clear();
      Response.clear();
    }
  }


  private RequestBuilder parseStatusText(ByteBuffer buffer) {
    int position = buffer.position();
    String method = null;
    String requestUrl = null;
    byte[] data;
    while (buffer.hasRemaining()) {
      byte b = buffer.get();
      if (b == ' ') {
        int curr = buffer.position();
        data = new byte[curr - position - 1];
        getData(data, buffer, curr, position);
        position = curr;
        String line = new String(data);
        if (method == null) {
          method = line;
        } else if (requestUrl == null) {
          requestUrl = line;
        } else {
          break;
        }
      } else if (lineFinished(buffer, b)) {
        int curr = buffer.position();
        data = new byte[curr - position - 2];
        getData(data, buffer, curr, position);
        return new RequestBuilder(method, requestUrl, new String(data));
      }
    }
    return null;
  }

  private boolean parseHeaders(RequestBuilder builder, ByteBuffer buffer) {
    if (!buffer.hasRemaining()) {
      return false;
    }
    int position = buffer.position();
    if (headerFinished(buffer)) {
      return true;
    }
    String name = null;
    List<String> values = new LinkedList<>();
    byte[] data;
    while (buffer.hasRemaining()) {
      byte b = buffer.get();
      int curr = buffer.position();
      if (name == null && b == Bytes.COLON) {
        data = new byte[curr - position - 1];
        getData(data, buffer, curr, position);
        name = new String(data);
        position = curr;
      } else if (name != null && b == Bytes.COMMA) {
        data = new byte[curr - position - 1];
        getData(data, buffer, curr, position);
        values.add(new String(data).trim());
      } else if (name != null && lineFinished(buffer, b)) {
        data = new byte[curr - position - 2];
        getData(data, buffer, curr, position);
        values.add(new String(data).trim());
        builder.headers.put(name, values.toArray(new String[0]));
        name = null;
        values.clear();
        position = curr;
        if (headerFinished(buffer)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean parseBody(RequestBuilder builder, ByteBuffer buffer) {
    String contentLength = builder.getContentLength();
    if (contentLength != null) {
      int length = Integer.parseInt(contentLength);
      if (buffer.remaining() < length) {
        return false;
      }
      if (length == 0) {
        return true;
      }
      builder.body = new byte[length];
      buffer.get(builder.body);
      return true;
    }
    // 找不到contentLength当没有body处理
    return true;
  }

  private boolean headerFinished(ByteBuffer buffer) {
    return buffer.get() == Bytes.CR && buffer.hasRemaining() && buffer.get() == Bytes.LF;
  }

  private boolean lineFinished(ByteBuffer buffer, byte curr) {
    return curr == Bytes.LF && buffer.get(buffer.position() - 2) == Bytes.CR;
  }

  private void getData(byte[] data, ByteBuffer buffer, int curr, int index) {
    buffer.position(index);
    buffer.get(data);
    buffer.position(curr);
  }

  @RequiredArgsConstructor
  private class RequestBuilder {

    final String method;
    final String requestUri;
    final String version;
    final Map<String, String[]> headers = new LinkedHashMap<>();
    byte[] body;

    String getContentLength() {
      for (Map.Entry<String, String[]> entry : headers.entrySet()) {
        if (entry.getKey().equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
          return entry.getValue()[0];
        }
      }
      return null;
    }

    Request build(ChannelContext channelContext) {
      Request request = new Request(HttpMethod.valueOf(method), requestUri, version,
          webConf.getContextPath(), body);
      request.getHeaders().putAll(headers);
      request.addAttribute(Request.ORIGINAL_REQUEST, channelContext);
      return request;
    }
  }
}