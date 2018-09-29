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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;
import vip.justlive.oxygen.jdbc.interceptor.JdbcInterceptor;

/**
 * jdbc操作类
 *
 * @author wubo
 */
public class Jdbc {

  static final String TEMPLATE = "datasource.%s";
  static final String PRIMARY_KEY = Jdbc.class.getSimpleName();
  static final List<JdbcInterceptor> JDBC_INTERCEPTORS = new ArrayList<>(4);
  static final Map<String, DataSource> DATA_SOURCE_MAP = new ConcurrentHashMap<>(2, 1f);


  /**
   * 添加主数据源
   *
   * @param dataSource 数据源
   */
  public static void addPrimaryDataSource(DataSource dataSource) {
    addDataSource(PRIMARY_KEY, dataSource);
  }

  /**
   * 添加数据源
   *
   * @param name 数据源名称
   * @param dataSource 数据源
   */
  public static void addDataSource(String name, DataSource dataSource) {
    DataSource local = DATA_SOURCE_MAP.putIfAbsent(name, dataSource);
    if (local != null) {
      throw new IllegalArgumentException(String.format("数据源名称[%s]已存在", name));
    }
  }

  public static Connection getConnection(String dataSourceName) {
    try {
      DataSource dataSource = DATA_SOURCE_MAP.get(dataSourceName);
      if (dataSource == null) {
        throw new NullPointerException();
      }
      return dataSource.getConnection();
    } catch (SQLException e) {
      throw Exceptions.wrap(e);
    }
  }

  public static <T> T query(String sql, ResultSetHandler<T> handler, Object... params) {
    return query(getConnection(PRIMARY_KEY), sql, handler, params);
  }

  public static <T> T query(String dataSourceName, String sql, ResultSetHandler<T> handler,
      Object... params) {
    return query(getConnection(dataSourceName), sql, handler, params);
  }

  public static <T> T query(String sql, ResultSetHandler<T> handler, List<Object> params) {
    return query(getConnection(JdbcPlugin.class.getSimpleName()), sql, handler, params);
  }

  public static <T> T query(String dataSourceName, String sql, ResultSetHandler<T> handler,
      List<Object> params) {
    return query(getConnection(dataSourceName), sql, handler, params);
  }

  public static <T> T query(Connection connection, String sql, ResultSetHandler<T> handler,
      Object... params) {
    return query(connection, sql, handler, Arrays.asList(params));
  }

  public static <T> T query(Connection connection, String sql, ResultSetHandler<T> handler,
      List<Object> params) {
    return query(connection, sql, handler, params, true);
  }

  public static <T> T query(Connection connection, String sql, ResultSetHandler<T> handler,
      List<Object> params, boolean closeCon) {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    T result = null;
    try {
      before(sql, params);
      stmt = connection.prepareStatement(sql);
      fillStatement(stmt, params);
      rs = stmt.executeQuery();
      result = handler.handle(rs);
      after(sql, params, rs, result);
    } catch (Exception e) {
      onException(sql, params, e);
      throw Exceptions.wrap(e);
    } finally {
      close(rs);
      close(stmt);
      if (closeCon) {
        close(connection);
      }
      onFinally(sql, params, result);
    }
    return result;
  }

  private static void fillStatement(PreparedStatement stmt, List<Object> params)
      throws SQLException {
    if (params != null && !params.isEmpty()) {
      for (int i = 0, len = params.size(); i < len; i++) {
        stmt.setObject(i + 1, params.get(i));
      }
    }
  }

  public static void close(AutoCloseable rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (Exception e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  private static void before(String sql, List<Object> params) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.before(sql, params);
    }
  }

  private static void after(String sql, List<Object> params, ResultSet rs, Object result) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.after(sql, params, rs, result);
    }
  }

  private static void onException(String sql, List<Object> params, Exception e) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.onException(sql, params, e);
    }
  }

  private static void onFinally(String sql, List<Object> params, Object result) {
    for (JdbcInterceptor interceptor : JDBC_INTERCEPTORS) {
      interceptor.onFinally(sql, params, result);
    }
  }


}
