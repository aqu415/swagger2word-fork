package org.word;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author cuixiuyin
 * @description
 * @date: 2018/12/19 21:32
 */
@SpringBootApplication
@EnableSwagger2
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        //
        SpringApplication.run(Application.class, args);
    }
}
