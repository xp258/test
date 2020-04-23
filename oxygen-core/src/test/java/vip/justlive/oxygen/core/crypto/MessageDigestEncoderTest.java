package vip.justlive.oxygen.core.crypto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author wubo
 */
public class MessageDigestEncoderTest {

  @Test
  public void test() {

    MessageDigestEncoder encoder = new MessageDigestEncoder("MD5");

    String source = "111111";
    assertTrue(encoder.match(source, "96e79218965eb72c92a549dd5a330112"));

    encoder.setUseSalt(true);
    String raw = encoder.encode(source);
    System.out.println(raw);
    assertTrue(encoder.match(source, raw));

    DelegateEncoder e = new DelegateEncoder("MD5");
    assertTrue(e.match(source, "{MD5}96e79218965eb72c92a549dd5a330112"));

    e = new DelegateEncoder("SHA-1");
    System.out.println(e.encode(source));
    assertTrue(e.match(source, "{SHA-1}3d4f2bf07dc1be38b20cd6e46949a1071f9d0e3d"));


    e = new DelegateEncoder("SHA-256");
    System.out.println(e.encode(source));
    assertTrue(e.match(source, "{SHA-256}bcb15f821479b4d5772bd0ca866c00ad5f926e3580720659cc80d39c9d09802a"));


  }
}