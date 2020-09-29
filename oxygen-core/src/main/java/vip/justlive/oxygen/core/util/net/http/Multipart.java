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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.Bytes;
import vip.justlive.oxygen.core.util.base.HttpHeaders;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.SnowflakeId;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.io.FileUtils;
import vip.justlive.oxygen.core.util.io.IoUtils;

/**
 * multipart
 *
 * @author wubo
 */
@Getter
public class Multipart {

  private static final String DEFAULT_BOUNDARY = "----oxygen_" + SnowflakeId.defaultNextId();

  private final String boundary;
  private List<Part> parts;

  public Multipart() {
    this.boundary = DEFAULT_BOUNDARY;
  }

  public Multipart add(String name, String value) {
    if (parts == null) {
      parts = new ArrayList<>();
    }
    parts.add(new Part(name, value));
    return this;
  }

  public Multipart add(String name, File file) {
    return add(name, file, null);
  }

  public Multipart add(String name, File file, String filename) {
    if (parts == null) {
      parts = new ArrayList<>();
    }
    parts.add(new Part(name, file, filename));
    return this;
  }

}
