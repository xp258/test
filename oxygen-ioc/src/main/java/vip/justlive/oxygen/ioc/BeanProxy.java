package vip.justlive.oxygen.ioc;

/**
 * bean proxy
 *
 * @author wubo
 */
public interface BeanProxy {

  /**
   * 定义bean
   *
   * @param clazz 类
   * @param args 构造参数
   * @return obj
   */
  Object proxy(Class<?> clazz, Object... args);
}
