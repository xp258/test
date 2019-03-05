package vip.justlive.oxygen.aop;

import org.junit.Test;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.ioc.BeanStore;

/**
 * @author wubo
 */
public class AopPluginTest {

  @Test
  public void test() {
    Bootstrap.start();
    BeanStore.getBean(Conf.class).print();
  }
}
