package com.vitality.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class VitalityApplication {
    public static void main(String[] args) {
        SpringApplication.run(VitalityApplication.class, args);
        log.info("Vitality Application Started");
    }
}