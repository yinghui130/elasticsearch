package cn.gov.customs.qingdao.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class ElasticsearchApplication {

  public static void main(String[] args) {
    System.setProperty("es.set.netty.runtime.available.processors", "false");
    SpringApplication.run(ElasticsearchApplication.class, args);
  }

}
