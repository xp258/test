package vip.justlive.oxygen.ioc;


@Configuration
public class Conf {

  @Bean
  Inter noDepBean() {
    return new NoDepBean();
  }

}
