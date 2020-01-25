package vip.justlive.oxygen.core.util.json;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * @author wubo
 */
public class JsonWriterTest {

  @Test
  public void toJson() {
    JsonWriter writer = new JsonWriter();

    // Primitive
    Assert.assertEquals("1", writer.toJson(1));
    Assert.assertEquals("23.3", writer.toJson(23.3f));
    Assert.assertEquals("9024512353132414", writer.toJson(9024512353132414L));
    Assert.assertEquals("true", writer.toJson(true));

    Assert.assertEquals("null", writer.toJson(null));

    Date date = new Date();
    Assert.assertEquals(Long.toString(date.getTime()), writer.toJson(date));
    LocalDateTime time = LocalDateTime.now();
    Assert.assertEquals("\"" + time.toString() + "\"", writer.toJson(time));

    Bean bean = new Bean();
    bean.a = 1;
    bean.b = true;
    bean.c = time;

    Assert.assertEquals("{\"a\":1,\"b\":true,\"c\":\"" + time.toString() + "\",\"d\":null}",
        writer.toJson(bean));

    Map<String, Object> map = MoreObjects.mapOf("a", 1, "b", true, "c", time);
    Assert
        .assertEquals("{\"a\":1,\"b\":true,\"c\":\"" + time.toString() + "\"}", writer.toJson(map));

    List<Object> list = Arrays.asList(1, 2, 3, bean);
    Assert.assertEquals("[1,2,3,{\"a\":1,\"b\":true,\"c\":\"" + time.toString() + "\",\"d\":null}]",
        writer.toJson(list));

    int[] array = new int[]{3, 4, 5};
    Assert.assertEquals("[3,4,5]", writer.toJson(array));

    Assert.assertEquals("\"UTF-8\"", writer.toJson(StandardCharsets.UTF_8));
    Assert.assertEquals("\"Asia/Shanghai\"", writer.toJson(ZoneId.of("Asia/Shanghai")));
    Assert.assertEquals("1", writer.toJson(new AtomicReference<>(1)));
    Assert.assertEquals("[1,2]", writer.toJson(new AtomicIntegerArray(new int[]{1, 2})));

  }

  @Data
  public static class Bean {

    private int a;
    private boolean b;
    private LocalDateTime c;
    private Date d;
  }
}
