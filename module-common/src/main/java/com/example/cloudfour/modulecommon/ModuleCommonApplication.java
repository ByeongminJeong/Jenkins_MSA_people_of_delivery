package com.example.cloudfour.modulecommon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class
        }
)
//@EnableDiscoveryClient
public class ModuleCommonApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModuleCommonApplication.class, args);
    }
}
