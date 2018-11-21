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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import vip.justlive.oxygen.core.Bootstrap;

/**
 * @author wubo
 */
public class BatchTest {

  @Before
  public void before() {
    Bootstrap.start();
    Jdbc.update("create table system (id int primary key auto_increment, key int, value int);");
  }

  @Test
  public void test() {
    Batch batch = Batch.use();
    batch.addBatch("insert into system (key, value) values (?,?)", 1, 3);
    batch.addBatch("insert into system (key, value) values (?,?)", Arrays.asList(4, 4));
    batch.commit();
    List<Map<String, Object>> map = Jdbc.queryForMapList("select * from system");
    Assert.assertEquals(2, map.size());

    Batch batch1 = Batch.use();
    batch1.addBatch("insert into system (key,value) values(1,2)");
    batch1.addBatch("insert into system (key,value) values(2,2)");
    batch1.commit();
    map = Jdbc.queryForMapList("select * from system");
    Assert.assertEquals(4, map.size());
  }
}