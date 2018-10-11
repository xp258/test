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

import java.nio.charset.Charset;
import lombok.Data;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * multipart item
 *
 * @author wubo
 */
@Data
public class MultipartItem {

  private String disposition;
  private Charset charset;
  private String name;
  private String filename;
  private byte[] body;
  private String value;

  void parse() {
    for (String line : disposition.split(Constants.SEMICOLON)) {
      String[] props = line.split(Constants.EQUAL);
      if (props.length == 2) {
        String key = props[0].trim();
        String val = props[1].replace("\"", "").trim();
        if (Constants.FORM_DATA_NAME.equals(key)) {
          name = val;
        } else if (Constants.FORM_DATA_FILENAME.equals(key)) {
          filename = val;
        }
      }
    }
    if (filename == null) {
      value = new String(body, charset);
    }
  }

  /**
   * 是否为属性
   *
   * @return true表示属性
   */
  public boolean isProperty() {
    return filename == null && value != null;
  }
}
