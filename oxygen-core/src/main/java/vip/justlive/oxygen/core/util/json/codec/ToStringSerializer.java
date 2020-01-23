package vip.justlive.oxygen.core.util.json.codec;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * ToString 序列化,优先级较低
 *
 * @author wubo
 */
public class ToStringSerializer implements Serializer {

  private static final List<Class<?>> WRAPPER = Arrays
      .asList(Instant.class, JapaneseDate.class, LocalDateTime.class, LocalDate.class,
          LocalTime.class, OffsetDateTime.class, OffsetTime.class, ZonedDateTime.class,
          Charset.class, Pattern.class, Locale.class, URI.class, URL.class, UUID.class);

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean supported(Class<?> type, Object value) {
    return value != null && (Number.class.isAssignableFrom(type) || ClassUtils.isPrimitive(type)
        || WRAPPER.contains(type));
  }

  @Override
  public void serialize(Object value, StringBuilder buf) {
    boolean wrapper = WRAPPER.contains(value.getClass());
    if (wrapper) {
      buf.append('"');
    }
    buf.append(value.toString());
    if (wrapper) {
      buf.append('"');
    }
  }
}
