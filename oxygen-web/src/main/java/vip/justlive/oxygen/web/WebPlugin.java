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
package vip.justlive.oxygen.web;

import vip.justlive.oxygen.core.Plugin;
import vip.justlive.oxygen.web.mapping.Mappings;

/**
 * web插件
 *
 * @author wubo
 */
public class WebPlugin implements Plugin {

  @Override
  public int order() {
    return Integer.MIN_VALUE + 60;
  }

  @Override
  public void start() {
    Mappings.init();
  }

  @Override
  public void stop() {
    Mappings.destroy();
  }
}
