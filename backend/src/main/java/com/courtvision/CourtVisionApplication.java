package com.courtvision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CourtVisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourtVisionApplication.class, args);
    }

}
