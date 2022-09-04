package org.word.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "swagger.custom")
public class SwaggerCustomProperties {

    //  目标swagger json默认地址
    private String url;
}
