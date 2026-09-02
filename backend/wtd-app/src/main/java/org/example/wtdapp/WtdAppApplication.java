package org.example.wtdapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WtdAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WtdAppApplication.class, args);
    }

}
