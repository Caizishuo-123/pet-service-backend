package com.imis.petservicebackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosConfig {

    @Bean
    public COSClient cosClient(CosProperties props) {
        COSCredentials cred = new BasicCOSCredentials(
                props.getSecretId(),
                props.getSecretKey()
        );

        ClientConfig clientConfig = new ClientConfig(
                new Region(props.getRegion())
        );

        return new COSClient(cred, clientConfig);
    }
}

