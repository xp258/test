package vip.justlive.oxygen.core.util.json;

import lombok.experimental.UtilityClass;

/**
 * json
 *
 * @author wubo
 */
@UtilityClass
public class Json {

  private final JsonWriter WRITER = new JsonWriter();

  /**
   * object to string
   *
   * @param value object
   * @return string
   */
  public String toJson(Object value) {
    return WRITER.toJson(value);
  }

}
