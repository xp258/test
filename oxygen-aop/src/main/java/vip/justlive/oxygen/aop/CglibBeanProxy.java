package vip.justlive.oxygen.aop;

import vip.justlive.oxygen.ioc.BeanProxy;

/**
 * cglib bean proxy
 *
 * @author wubo
 */
public class CglibBeanProxy implements BeanProxy {

  @Override
  public Object proxy(Class<?> clazz, Object... args) {
    return CglibProxy.proxy(clazz, args);
  }
}
