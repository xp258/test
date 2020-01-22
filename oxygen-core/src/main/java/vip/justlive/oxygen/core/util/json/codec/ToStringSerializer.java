package vip.justlive.oxygen.core.util.json.codec;

import java.util.Arrays;
import java.util.List;

/**
 * ToString 序列化,优先级较低
 *
 * @author wubo
 */
public class ToStringSerializer implements Serializer {

  private static final List<Class<?>> TYPES = Arrays.asList(Boolean.class, boolean.class);

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean supported(Class<?> type, Object value) {
    return Number.class.isAssignableFrom(type) || TYPES.contains(type);
  }

  @Override
  public void serialize(Object value, StringBuilder buf) {
    buf.append(value.toString());
  }
}
