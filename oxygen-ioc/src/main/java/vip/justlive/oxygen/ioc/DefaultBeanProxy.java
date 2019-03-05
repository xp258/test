package vip.justlive.oxygen.ioc;

import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * 默认 bean proxy
 *
 * @author wubo
 */
public class DefaultBeanProxy implements BeanProxy {

  @Override
  public Object proxy(Class<?> clazz, Object... args) {
    try {
      return clazz.getConstructor(ClassUtils.getConstructorArgsTypes(clazz, args))
          .newInstance(args);
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

}
