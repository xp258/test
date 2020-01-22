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

package vip.justlive.oxygen.core.util.json;

import java.util.ArrayList;
import java.util.List;
import vip.justlive.oxygen.core.util.json.codec.Serializer;

/**
 * json writer
 *
 * @author wubo
 */
public class JsonWriter {

  private final List<Serializer> serializers = new ArrayList<>();

  /**
   * 添加序列化
   *
   * @param serializer 序列化
   */
  public void addSerializer(Serializer serializer) {
    if (serializers.contains(serializer)) {
      return;
    }
    serializers.add(serializer);
  }

  public String toJson(Object value) {
    String json = serializer(value);
    if (json != null) {
      return json;
    }
    return null;
  }

  private String serializer(Object value) {
    for (Serializer serializer : serializers) {
      String rt = serializer.serialize(value);
      if (rt != null) {
        return rt;
      }
    }
    return null;
  }
}
