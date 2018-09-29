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
package vip.justlive.oxygen.core.config;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.convert.DefaultConverterService;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.io.PropertiesLoader;
import vip.justlive.oxygen.core.io.PropertySource;
import vip.justlive.oxygen.core.util.ReflectUtils;

/**
 * 配置工厂
 *
 * @author wubo
 */
@Slf4j
public class ConfigFactory {

  /**
   * 存储解析过的配置类
   */
  private static final Map<Class<?>, Object> FACTORY = new ConcurrentHashMap<>();
  /**
   * 配置属性集合
   */
  private static final Properties PROPS = new Properties();
  /**
   * 配置资源包装
   */
  private static final PropertySource SOURCE_WRAPPER = () -> PROPS;
  /**
   * 用于生成临时编号
   */
  private static final AtomicLong ATOMIC = new AtomicLong();
  /**
   * 临时编号前缀
   */
  private static final String TMP_PREFIX = "ConfigFactory.tmp.%s";

  private ConfigFactory() {
  }

  /**
   * 加载配置文件
   *
   * @param locations 路径
   */
  public static void loadProperties(String... locations) {
    loadProperties(StandardCharsets.UTF_8, true, locations);
  }

  /**
   * 加载配置文件，设置编码和忽略找不到的资源
   *
   * @param charset 字符集
   * @param ignoreNotFound 忽略未找到
   * @param locations 路径
   */
  public static void loadProperties(Charset charset, boolean ignoreNotFound, String... locations) {
    PropertiesLoader loader = new PropertiesLoader(locations);
    loader.setCharset(charset);
    loader.setIgnoreNotFound(ignoreNotFound);
    loadProperties(loader);
  }

  /**
   * 加载配置文件，传入配置属性资源
   *
   * @param source 属性源
   */
  public static void loadProperties(PropertySource source) {
    PROPS.putAll(source.props());
  }

  /**
   * 获取配置属性
   *
   * @param key 属性键值
   * @return 属性值
   */
  public static String getProperty(String key) {
    return SOURCE_WRAPPER.getProperty(key);
  }

  /**
   * 获取配置属性，可设置默认值
   *
   * @param key 属性键值
   * @param defaultValue 默认值
   * @return 属性值
   */
  public static String getProperty(String key, String defaultValue) {
    return SOURCE_WRAPPER.getProperty(key, defaultValue);
  }

  /**
   * 加载配置类，需要有{@link Value}注解
   *
   * @param clazz 类
   * @param <T> 泛型类
   * @return 配置类
   */
  public static <T> T load(Class<T> clazz) {
    Object obj = FACTORY.get(clazz);
    if (obj != null) {
      return clazz.cast(obj);
    }

    T val = parse(clazz);
    Object other = FACTORY.putIfAbsent(clazz, val);
    if (other != null) {
      val = clazz.cast(other);
    }
    return val;
  }

  /**
   * 加载配置类，需要有{@link Value}注解
   *
   * @param bean 对象
   */
  public static void load(Object bean) {
    load(bean, null);
  }

  /**
   * 加载配置类，需要有{@link Value}注解
   *
   * @param bean 对象
   * @param valueConfig 注解
   */
  public static void load(Object bean, ValueConfig valueConfig) {
    if (bean != null) {
      parse(bean, valueConfig);
    }
  }

  /**
   * 清除配置
   */
  public static void clear() {
    FACTORY.clear();
    PROPS.clear();
  }

  /**
   * 获取所有属性名称
   *
   * @return keys
   */
  public static Set<String> keys() {
    return PROPS.stringPropertyNames();
  }

  /**
   * 解析
   *
   * @param clazz 类
   * @param <T> 泛型类
   * @return 配置类
   */
  protected static <T> T parse(Class<T> clazz) {
    T obj;
    try {
      obj = clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw Exceptions.wrap(e);
    }
    return clazz.cast(parse(obj, null));
  }

  protected static Object parse(Object obj, ValueConfig valueConfig) {
    Class<?> clazz = obj.getClass();
    if (valueConfig == null && clazz.isAnnotationPresent(ValueConfig.class)) {
      valueConfig = clazz.getAnnotation(ValueConfig.class);
    }
    Field[] fields = ReflectUtils.getAllDeclaredFields(clazz);
    for (Field field : fields) {
      if (field.isAnnotationPresent(Value.class)) {
        Value val = field.getAnnotation(Value.class);
        Object value = getProperty(val.value(), field.getType());
        if (value != null) {
          ReflectUtils.setValue(obj, field, value);
        }
      } else if (valueConfig != null) {
        StringBuilder name = new StringBuilder(valueConfig.value());
        if (name.length() > 0) {
          name.append(Constants.DOT);
        }
        name.append(field.getName());
        Object value = getProperty(name.toString(), field.getType(), false);
        if (value != null) {
          ReflectUtils.setValue(obj, field, value);
        }
      }
    }
    return obj;
  }

  private static Object getProperty(String key, Class<?> type) {
    return getProperty(key, type, true);
  }

  private static Object getProperty(String key, Class<?> type, boolean wrap) {
    String value;
    if (wrap) {
      String tmpKey = String.format(TMP_PREFIX, ATOMIC.getAndIncrement());
      PROPS.setProperty(tmpKey, key);
      value = getProperty(tmpKey);
      PROPS.remove(tmpKey);
    } else {
      value = getProperty(key);
    }
    if (value == null || value.getClass() == type) {
      return value;
    }
    return DefaultConverterService.sharedConverterService().convert(value, type);
  }

}
