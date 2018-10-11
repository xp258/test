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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * multipart请求解析
 *
 * @author wubo
 */
public class MultipartRequestParse extends AbstractRequestParse {

  private static final byte[] LINE_SEPARATOR = {Constants.CR, Constants.LF};
  private static final byte[] STREAM_TERMINATOR = {Constants.DASH, Constants.DASH};

  @Override
  public boolean supported(HttpServletRequest req) {
    return Request.current().isMultipart();
  }

  @Override
  public void handle(HttpServletRequest req) {
    Request request = Request.current();
    try {
      MultipartSourceStream multipart = new MultipartSourceStream(req.getInputStream(), request);
      multipart.init();
      Map<String, MultipartItem> map = request.getMultipart().getData();
      for (MultipartItem item : multipart.items) {
        map.put(item.getName(), item);
        if (item.isProperty()) {
          margeParam(request.getParams(), item.getName(), item.getValue());
        }
      }
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public class MultipartSourceStream {

    private final InputStream inputStream;
    private final Request request;
    private final byte[] boundary;
    private final byte[] endBoundary;
    private final Charset charset;

    private byte[] buf = new byte[0x800];
    private List<byte[]> lines = new LinkedList<>();
    private ByteArrayOutputStream current = new ByteArrayOutputStream();
    private int index = 0;
    List<MultipartItem> items = new LinkedList<>();

    MultipartSourceStream(InputStream inputStream, Request request) throws IOException {
      this.inputStream = inputStream;
      this.request = request;
      this.charset = Charset.forName(request.getEncoding());
      current.reset();
      current.write(STREAM_TERMINATOR);
      current.write(request.getMultipart().getBoundary());
      this.boundary = current.toByteArray();
      current.write(STREAM_TERMINATOR);
      this.endBoundary = current.toByteArray();
      current.reset();
    }

    void init() throws IOException {
      parseLines();
      parseMultipartItem(items, 0);
    }

    private void parseLines() throws IOException {
      while (true) {
        int len = inputStream.read(buf);
        if (len == -1) {
          if (current.size() > 0) {
            lines.add(current.toByteArray());
            current.reset();
          }
          break;
        }
        handleBuf(len);
      }
    }

    private void handleBuf(int len) {
      for (int i = 0; i < len; i++) {
        if (LINE_SEPARATOR[index] == buf[i]) {
          if (LINE_SEPARATOR.length == index + 1) {
            index = 0;
            lines.add(current.toByteArray());
            current.reset();
          } else {
            index++;
          }
        } else {
          if (index > 0) {
            current.write(LINE_SEPARATOR, 0, index);
          }
          current.write(buf[i]);
        }
      }
    }

    void parseMultipartItem(List<MultipartItem> items, int index)
        throws IOException {
      byte[] line = lines.get(index);
      if (Arrays.equals(line, boundary)) {
        MultipartItem item = new MultipartItem();
        item.setDisposition(new String(lines.get(++index), request.getEncoding()));
        item.setCharset(charset);
        int[] bounds = getBodyBounds(lines, index);
        if (bounds.length == 0) {
          item.setBody(new byte[0]);
        } else {
          current.reset();
          for (int i = bounds[0]; i < bounds[1]; i++) {
            current.write(lines.get(i));
          }
          item.setBody(current.toByteArray());
        }
        item.parse();
        items.add(item);
        int next = bounds[1];
        if (next < lines.size()) {
          parseMultipartItem(items, next);
        }
      }
    }

    int[] getBodyBounds(List<byte[]> lines, int index) {
      int tmp = index;
      int max = lines.size();
      while (tmp < max) {
        byte[] line = lines.get(++tmp);
        if (line.length == 0) {
          break;
        }
      }
      if (tmp == max) {
        return new int[0];
      }
      int start = ++tmp;
      while (tmp < max) {
        byte[] line = lines.get(++tmp);
        if (Arrays.equals(line, boundary) || Arrays.equals(line, endBoundary)) {
          break;
        }
      }
      return new int[]{start, tmp};
    }
  }
}
