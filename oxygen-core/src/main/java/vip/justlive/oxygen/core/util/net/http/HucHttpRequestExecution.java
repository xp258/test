/*
 * Copyright (C) 2020 the original author or authors.
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
package vip.justlive.oxygen.core.util.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.function.Function;
import javax.net.ssl.HttpsURLConnection;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Bytes;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.io.FileUtils;
import vip.justlive.oxygen.core.util.io.IoUtils;
import vip.justlive.oxygen.core.util.json.Json;

/**
 * HttpURLConnection 实现的执行器
 *
 * @author wubo
 */
public class HucHttpRequestExecution implements HttpRequestExecution {

  private static final String FORM_DATA = "Content-Disposition: form-data; name=\"%s\"";
  private static final String FORM_FILE_DATA = "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"";

  public static final HucHttpRequestExecution HUC = new HucHttpRequestExecution();

  @Override
  public HttpResponse execute(HttpRequest request) throws IOException {
    String httpUrl = request.getUrl();
    if (request.getQueryParam() != null) {
      String queryString = MoreObjects.beanToQueryString(request.getQueryParam(), true);
      if (!request.getUrl().contains(Strings.QUESTION_MARK)) {
        httpUrl += Strings.QUESTION_MARK;
      } else if (!request.getUrl().endsWith(Strings.AND)) {
        httpUrl += Strings.AND;
      }
      httpUrl += queryString;
    }
    HttpURLConnection connection = buildConnection(httpUrl, request);
    if (request.getConnectTimeout() >= 0) {
      connection.setConnectTimeout(request.getConnectTimeout());
    }
    if (request.getReadTimeout() >= 0) {
      connection.setReadTimeout(request.getReadTimeout());
    }
    connection.setInstanceFollowRedirects(request.isFollowRedirects());
    connection.setRequestMethod(request.getMethod().name());
    connection.setUseCaches(false);
    setContentType(request);
    // add headers
    request.getHeaders().forEach(connection::addRequestProperty);
    boolean nonOutput = request.getMethod() == HttpMethod.GET || (
        request.getBody() == null && request.getMultipart() == null);
    connection.setDoOutput(!nonOutput);
    if (nonOutput) {
      connection.connect();
    } else {
      byte[] bytes = getFunc(request).apply(request.getBody());
      connection.setFixedLengthStreamingMode(bytes.length);
      connection.connect();
      try (OutputStream out = connection.getOutputStream()) {
        out.write(bytes);
        out.flush();
      }
    }
    return new HttpResponse(connection, request.getCharset());
  }

  private Function<Object, byte[]> getFunc(HttpRequest request) {
    if (request.getHttpBody() == HttpBody.FORM) {
      return obj -> formBodyConvert(obj, request.getCharset());
    } else if (request.getHttpBody() == HttpBody.JSON) {
      return obj -> jsonBodyConvert(obj, request.getCharset());
    } else if (request.getHttpBody() == HttpBody.MULTIPART) {
      return obj -> multipartConvert(obj, request.getMultipart(), request.getCharset());
    } else if (request.getHttpBody() == HttpBody.OTHERS && request.getFunc() != null) {
      return request.getFunc();
    }
    return this::noneBodyConvert;
  }

  private HttpURLConnection buildConnection(String httpUrl, HttpRequest request)
      throws IOException {
    HttpURLConnection connection;
    if (request.getProxy() != null) {
      connection = (HttpURLConnection) new URL(httpUrl).openConnection(request.getProxy());
    } else {
      connection = (HttpURLConnection) new URL(httpUrl).openConnection();
    }
    if (connection instanceof HttpsURLConnection) {
      HttpsURLConnection https = (HttpsURLConnection) connection;
      if (request.getSslSocketFactory() != null) {
        https.setSSLSocketFactory(request.getSslSocketFactory());
      }
      if (request.getHostnameVerifier() != null) {
        https.setHostnameVerifier(request.getHostnameVerifier());
      }
    }
    return connection;
  }

  private void setContentType(HttpRequest request) {
    String contentType = request.getHeaders().get(HttpHeaders.CONTENT_TYPE);
    if (contentType != null && !contentType.contains(HttpHeaders.CHARSET)) {
      request.getHeaders()
          .put(HttpHeaders.CONTENT_TYPE, contentType + ";charset=" + request.getCharset().name());
    }
  }

  private byte[] noneBodyConvert(Object obj) {
    return new byte[0];
  }

  private byte[] formBodyConvert(Object obj, Charset charset) {
    if (obj == null) {
      return new byte[0];
    }
    return MoreObjects.beanToQueryString(obj, true).getBytes(charset);
  }

  private byte[] jsonBodyConvert(Object json, Charset charset) {
    if (json == null) {
      return new byte[0];
    }
    if (json instanceof String) {
      return ((String) json).getBytes(charset);
    } else {
      return Json.toJson(json).getBytes(charset);
    }
  }

  private byte[] multipartConvert(Object body, Multipart multipart, Charset charset) {
    if (body != null) {
      MoreObjects.beanToMap(body).forEach((k, v) -> multipart.add(k, v.toString()));
    }
    if (multipart.getParts() == null || multipart.getParts().isEmpty()) {
      return new byte[0];
    }
    Bytes bytes = new Bytes();
    for (Part part : multipart.getParts()) {
      if (part.isFile()) {
        appendFile(multipart.getBoundary(), part, bytes, charset);
      } else {
        appendValue(multipart.getBoundary(), part, bytes, charset);
      }
    }
    appendEnd(multipart.getBoundary(), bytes, charset);
    return bytes.toArray();
  }

  private void appendEnd(String boundary, Bytes bytes, Charset charset) {
    bytes.write(Strings.DASH).write(Strings.DASH).write(boundary, charset).write(Strings.DASH)
        .write(Strings.DASH).write(Bytes.CR).write(Bytes.LF);
  }

  private void appendValue(String boundary, Part part, Bytes bytes, Charset charset) {
    bytes.write(Strings.DASH).write(Strings.DASH).write(boundary, charset).write(Bytes.CR)
        .write(Bytes.LF);
    bytes.write(String.format(FORM_DATA, part.getName()), charset).write(Bytes.CR).write(Bytes.LF);
    bytes.write(Bytes.CR).write(Bytes.LF);
    bytes.write(part.getValue(), charset).write(Bytes.CR).write(Bytes.LF);
  }

  private void appendFile(String boundary, Part part, Bytes bytes, Charset charset) {
    bytes.write(Strings.DASH).write(Strings.DASH).write(boundary, charset).write(Bytes.CR)
        .write(Bytes.LF);
    bytes.write(String.format(FORM_FILE_DATA, part.getName(), part.getFilename()), charset)
        .write(Bytes.CR).write(Bytes.LF);
    bytes.write(HttpHeaders.CONTENT_TYPE).write(Bytes.COLON).write(Bytes.SPACE).write(MoreObjects
        .firstNonNull(FileUtils.parseMimeType(part.getFilename()),
            HttpHeaders.APPLICATION_OCTET_STREAM));
    bytes.write(Bytes.CR).write(Bytes.LF).write(Bytes.CR).write(Bytes.LF);
    try (InputStream input = Files.newInputStream(part.getFile().toPath())) {
      IoUtils.copy(input, bytes);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
    bytes.write(Bytes.CR).write(Bytes.LF);
  }

}
