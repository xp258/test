package vip.justlive.oxygen.aop;

import vip.justlive.oxygen.ioc.Bean;

/**
 * @author wubo
 */
@Bean
public class Conf {

  @Before(annotation = Log.class)
  public void log(Invocation invocation) {
    System.out.println("aop before log " + invocation.getMethod());
  }

  @Before(annotation = Log.class)
  public boolean log1(Invocation invocation) {
    System.out.println("aop before log1 " + invocation.getMethod());
    return false;
  }

  @After(annotation = Log.class)
  public void log2(Invocation invocation) {
    System.out.println("aop after log2 " + invocation.getMethod());
  }

  @Catching(annotation = Log.class)
  public void log3(Invocation invocation) {
    System.out.println("aop catching log3 " + invocation.getMethod());
  }

  @Log
  public void print(){
    System.out.println("print");
  }
}
