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
package vip.justlive.oxygen.jdbc.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * @author wubo
 */
public class BasicRowHandler implements RowHandler {

  private static final BasicRowHandler INSTANCE = new BasicRowHandler();

  /**
   * 获取单例
   *
   * @return basicRowHandler
   */
  public static BasicRowHandler singleton() {
    return INSTANCE;
  }

  @Override
  public Object[] toArray(ResultSet rs) {
    try {
      ResultSetMetaData meta = rs.getMetaData();
      int cols = meta.getColumnCount();
      Object[] result = new Object[cols];
      for (int i = 0; i < cols; i++) {
        result[i] = rs.getObject(i + 1);
      }
      return result;
    } catch (SQLException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public <T> T toBean(ResultSet rs, Class<T> type) {
    return null;
  }

  @Override
  public Map<String, Object> toMap(ResultSet rs) {
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      int cols = rsmd.getColumnCount();
      Map<String, Object> result = new HashMap<>(16);
      for (int i = 1; i <= cols; i++) {
        String columnName = rsmd.getColumnLabel(i);
        if (columnName == null || columnName.length() == 9) {
          columnName = rsmd.getColumnName(i);
        }
        result.put(columnName, rs.getObject(i));
      }
      return result;
    } catch (SQLException e) {
      throw Exceptions.wrap(e);
    }
  }
}
