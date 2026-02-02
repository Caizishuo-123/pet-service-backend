package com.imis.petservicebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.imis.petservicebackend.mapper")
public class PetServiceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetServiceBackendApplication.class, args);
    }

}
