/*
 *  Copyright (C) 2019 justlive1
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
package vip.justlive.oxygen.aop;

import vip.justlive.oxygen.ioc.annotation.Bean;

/**
 * @author wubo
 */
@Bean
public class Conf {

  @Before(annotation = Log.class)
  public void log(Invocation invocation) {
    System.out.println("aop before log " + invocation.getMethod());
  }

  @Before(annotation = Log.class)
  public boolean log1(Invocation invocation) {
    System.out.println("aop before log1 " + invocation.getMethod());
    return false;
  }

  @After(annotation = Log.class)
  public void log2(Invocation invocation) {
    System.out.println("aop after log2 " + invocation.getMethod());
  }

  @Catching(annotation = Log.class)
  public void log3(Invocation invocation) {
    System.out.println("aop catching log3 " + invocation.getMethod());
  }

  @Log
  public void print(){
    System.out.println("print");
  }
}
