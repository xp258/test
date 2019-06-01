/*
 * Copyright (C) 2019 justlive1
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

package vip.justlive.oxygen.core.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import lombok.Data;
import vip.justlive.oxygen.core.util.IOUtils;

/**
 * http response
 *
 * @author wubo
 */
@Data
public class HttpResponse implements Closeable {

  private final int code;
  private final String message;
  private final InputStream body;

  @Override
  public void close() throws IOException {
    if (this.body != null) {
      IOUtils.drain(this.body);
      this.body.close();
    }
  }
}