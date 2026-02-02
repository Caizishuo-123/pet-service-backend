package com.imis.petservicebackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tencent.cos")
public class CosProperties {

    private String secretId;
    private String secretKey;
    private String region;
    private String bucketName;
    private String baseUrl;
}
