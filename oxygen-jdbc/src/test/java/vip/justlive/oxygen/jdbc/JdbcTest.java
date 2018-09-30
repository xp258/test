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
package vip.justlive.oxygen.jdbc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;

/**
 * @author wubo
 */
public class JdbcTest {

  @Before
  public void before() {
    Bootstrap.start();
    Jdbc.update("create table test (id int primary key, key varchar, value varchar);");
    Jdbc.update("insert into test (id, key, value) values (?, ?, ?), (2, 'a', 'c')", 1, "b", "b");
  }

  @Test
  public void test() {
    String sql = "select * from test where key = ?";
    Assert.assertEquals(3, Jdbc.query(sql, ResultSetHandler.mapHandler(), "a").size());
    Assert.assertEquals(1, Jdbc.query(sql, ResultSetHandler.mapListHandler(), "a").size());
    Assert.assertEquals(3, Jdbc.query(sql, ResultSetHandler.arrayHandler(), "a").length);
    Assert.assertEquals(1, Jdbc.query(sql, ResultSetHandler.arrayListHandler(), "a").size());

  }
}